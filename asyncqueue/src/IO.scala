package me.jiuyang.utils.asyncqueue

import chisel3._
import chisel3.util.{Decoupled, DecoupledIO}

class AsyncEnqueueIO(width: Int) extends Bundle {
  val clock: Clock = Input(Clock())
  val reset: Bool = Input(Bool())
  val source: DecoupledIO[UInt] = Flipped(Decoupled(UInt(width.W)))
}
class AsyncDequeueIO(width: Int) extends Bundle {
  val clock: Clock = Input(Clock())
  val reset: Bool = Input(Bool())
  val sink: DecoupledIO[UInt] = Decoupled(UInt(width.W))
}
