package zio.mock.module

import zio.mock.{Mock, Proxy}
import zio.stream.ZSink
import zio.{URLayer, ZIO, ZLayer}

/** Example module used for testing ZIO Mock framework.
  */
object StreamModuleMock extends Mock[StreamModule] {

  object Sink   extends Sink[Any, String, Int, Nothing, List[Int]]
  object Stream extends Stream[Any, String, Int]

  val compose: URLayer[Proxy, StreamModule] =
    ZLayer.fromZIO(
      ZIO
        .service[Proxy]
        .flatMap { proxy =>
          withRuntime[Proxy, StreamModule] { rts =>
            ZIO.succeed {
              new StreamModule {
                def sink(a: Int) =
                  rts.unsafeRun(proxy(Sink, a).catchAll(error => ZIO.succeed(ZSink.fail[String](error).dropLeftover)))

                def stream(a: Int) = rts.unsafeRun(proxy(Stream, a))
              }
            }
          }
        }
    )
}
