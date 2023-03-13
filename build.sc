import mill._
import mill.scalalib._
import mill.scalalib.publish._
import mill.scalalib.scalafmt._
import $file.dependencies.chisel.build
import $file.common

object v {
  val scala = "2.13.10"
}

object mychisel extends dependencies.chisel.build.Chisel(v.scala) {
  override def millSourcePath = os.pwd / "dependencies" / "chisel"
}

object asyncqueue extends common.AsyncQueueModule {
  def chiselModule = mychisel
  def scalaVersion = v.scala
  def macroParadiseIvy  =  None
}
