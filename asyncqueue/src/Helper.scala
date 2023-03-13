package me.jiuyang.utils.asyncqueue

import chisel3._

private[asyncqueue] object Helper {
  def grayCounter(bits: Int, increment: Bool = true.B, clear: Bool = false.B, name: String = "binary"): UInt = {
    val incremented: UInt = Wire(UInt(bits.W))
    val binary: UInt = RegNext(next = incremented, init = 0.U).suggestName(name)
    incremented := Mux(clear, 0.U, binary + increment.asUInt)
    incremented ^ ((incremented >> 1): UInt)
  }
}