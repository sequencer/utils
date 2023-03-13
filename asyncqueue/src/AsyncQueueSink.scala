package me.jiuyang.utils.asyncqueue

import chisel3._
import chisel3.util.{Decoupled, DecoupledIO, isPow2, log2Ceil}

class AsyncQueueSink(width: Int, depth: Int, sync: Int, narrow: Boolean = true) extends Module {
  require(depth > 0 && isPow2(depth), "todo")
  require(sync >= 2, "todo")
  private val depthWidth: Int = log2Ceil(depth)
  /** Dequeue Decoupled IO. */
  val dequeue: DecoupledIO[UInt] = IO(Decoupled(UInt(width.W)))

  val readIndexGray: UInt = withReset(reset.asAsyncReset)(Helper.grayCounter(depthWidth + 1, dequeue.fire, false.B, "readIndex"))
  val readIndexGrayReg: UInt = withReset(reset.asAsyncReset)(RegNext(next = readIndexGray, init = 0.U).suggestName("readIndexReg"))
  val writeIndexGray: UInt = IO(Input(UInt((depthWidth + 1).W)))

  /** ready signal to indicate [[DecoupledIO]] this queue is not empty, can still dequeue new data. */
  val empty: Bool = readIndexGray === writeIndexGray
  val valid: Bool = !empty
  val validReg: Bool = withReset(reset.asAsyncReset)(RegNext(next = valid, init = false.B).suggestName("validReg"))

  // dequeue to [[DecoupledIO]]
  dequeue.valid := validReg

  // port to access memory
  val readEnable: Bool = IO(Output(Bool()))
  readEnable := valid

  val readDataAndIndex: Option[(UInt, UInt)] = if (narrow) Some((
    IO(Input(UInt(width.W))).suggestName("data"),
    IO(Output(UInt(log2Ceil(depth).W))).suggestName("index")
  )) else None
  readDataAndIndex.foreach {
    case (data, index) =>
      dequeue.bits := data
      index := readIndexGray(depthWidth, 0)
  }

  // This register does not NEED to be reset, as its contents will not
  // be considered unless the asynchronously reset deq valid, register is set.
  // It is possible that bits latches when the source domain is reset / has power cut
  // This is safe, because isolation gates brought mem low before the zeroed [[writeIndex]] reached us.

  val fullReadData: Option[Vec[UInt]] = if (narrow) None else Some(IO(Input(Vec(depth, width))))
  fullReadData.foreach(fullData => dequeue.bits := fullData(readIndexGray(depthWidth, 0)))
}
