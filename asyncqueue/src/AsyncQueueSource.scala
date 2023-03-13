package me.jiuyang.utils.asyncqueue

import chisel3._
import chisel3.util.{DecoupledIO, isPow2, log2Ceil}
class AsyncQueueSource(width: Int, depth: Int, sync: Int) extends Module {
  require(depth > 0 && isPow2(depth), "todo")
  require(sync >= 2, "todo")
  private val depthWidth: Int = log2Ceil(depth)
  /** Enqueue Decoupled IO. */
  val enqueue: DecoupledIO[UInt] = IO(Flipped(DecoupledIO(UInt(width.W))))

  val writeIndexGray: UInt = withReset(reset.asAsyncReset)(Helper.grayCounter(depthWidth + 1, enqueue.fire, false.B, "writeIndex"))
  val writeIndexGrayReg: UInt = withReset(reset.asAsyncReset)(RegNext(next = writeIndexGray, init = 0.U).suggestName("writeIndexReg"))
  val readIndexGray: UInt = IO(Input(UInt((depthWidth + 1).W)))

  /** ready signal to indicate [[DecoupledIO]] this queue is not full, can still enqueue new data. */
  val full: Bool = writeIndexGray === (readIndexGray ^ (depth | depth >> 1).U)
  val ready: Bool = !full
  val readyReg: Bool = withReset(reset.asAsyncReset)(RegNext(next = ready, init = false.B).suggestName("readyReg"))

  // enqueue from [[DecoupledIO]]
  enqueue.ready := readyReg

  // port to access memory
  val writeEnable: Bool = IO(Output(Bool()))
  writeEnable := enqueue.fire
  val writeData: UInt = IO(Output(UInt(width.W)))
  writeData := enqueue.bits
  val writeIndex: UInt = IO(Output(UInt(log2Ceil(depth).W)))
  writeIndex := writeIndexGrayReg(depthWidth, 0)
}
