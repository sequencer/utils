import mill._
import mill.scalalib._
import $file.dependencies.chisel.common

trait AsyncQueueModule extends ScalaModule
  with dependencies.chisel.common.HasChisel {
  def chiselModule: dependencies.chisel.common.ChiselModule
}

// Public APIs
trait HasAsyncQueueModule extends ScalaModule 
  with dependencies.chisel.common.HasChisel {
  def asyncQueueModule: AsyncQueueModule
  override def moduleDeps = super.moduleDeps ++ Seq(asyncQueueModule)
}
