package me.jiuyang.utils.asyncqueue

import chisel3._
import chisel3.util._

class AsyncQueue(width: Int, depth: Int, sync: Int, narrow: Boolean) extends Module {
  val enqueue: AsyncEnqueueIO = IO(new AsyncEnqueueIO(width))
  val sourceModule: AsyncQueueSource =
    withClockAndReset(enqueue.clock, enqueue.reset)(Module(new AsyncQueueSource(width, depth, sync)))
  sourceModule.enqueue <> enqueue.source

  val dequeue: AsyncDequeueIO = IO(new AsyncDequeueIO(width))
  val sinkModule: AsyncQueueSink =
    withClockAndReset(enqueue.clock, enqueue.reset)(Module(new AsyncQueueSink(width, depth, sync, narrow)))
  dequeue.sink <> sinkModule.dequeue

  // read/write index bidirectional sync
  sourceModule.readIndexGray := withClockAndReset(enqueue.clock, enqueue.reset.asAsyncReset)(ShiftRegisters(sinkModule.readIndexGray, sync).last)
  sinkModule.writeIndexGray := withClockAndReset(dequeue.clock, dequeue.reset.asAsyncReset)(ShiftRegisters(sourceModule.writeIndexGray, sync).last)

  val memoryModule: DataMemory = withClock(dequeue.clock)(Module(new DataMemory(width, depth, narrow)))
  memoryModule.writeEnable := sourceModule.writeEnable
  memoryModule.writeData := sourceModule.writeData
  memoryModule.writeIndex := sourceModule.writeIndex
  memoryModule.readEnable := sinkModule.readEnable
  (memoryModule.readDataAndIndex zip sinkModule.readDataAndIndex).foreach {
    case ((memoryData, memoryIndex), (sinkData, sinkIndex)) =>
      sinkData := withClock(dequeue.clock)(RegNext(memoryData))
      memoryIndex := sinkIndex
    case _ =>
  }

  (memoryModule.fullReadData zip sinkModule.fullReadData).foreach {
    case (memoryFullData, sinkFullData) =>
      sinkFullData := withClock(dequeue.clock)(RegNext(memoryFullData))
    case _ =>
  }
}
