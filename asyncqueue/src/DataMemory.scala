package me.jiuyang.utils.asyncqueue
import chisel3._
import chisel3.util.log2Ceil

class DataMemory(width: Int, depth: Int, narrow: Boolean) extends RawModule {
  val dataQueue: SyncReadMem[UInt] = SyncReadMem(depth, UInt(width.W))

  // write IO
  val writeEnable: Bool = IO(Input(Bool()))
  val writeData: UInt = IO(Input(UInt(width.W)))
  val writeIndex: UInt = IO(Input(UInt(log2Ceil(depth).W)))
  when(writeEnable)(dataQueue.write(writeIndex, writeData))

  // read IO
  val readEnable: Bool = IO(Input(Bool()))

  // narrow read IO
  val readDataAndIndex: Option[(UInt, UInt)] = if (narrow) Some((
    IO(Output(UInt(width.W))).suggestName("data"),
    IO(Input(UInt(log2Ceil(depth).W))).suggestName("index")
  )) else None
  readDataAndIndex.foreach { case (readData, readIndex) => readData := dataQueue.read(readIndex) }

  // broad read IO
  val fullReadData: Option[Vec[UInt]] = if (narrow) None else Some(IO(Output(Vec(depth, UInt(width.W)))))
  fullReadData.foreach(_.zipWithIndex.foreach { case (data, index) => data := dataQueue.read(index.U) })
}
