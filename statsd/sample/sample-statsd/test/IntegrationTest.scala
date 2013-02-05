package test

import java.net.{SocketTimeoutException, DatagramPacket, DatagramSocket}
import org.specs2.mutable._
import play.api.test.Helpers._
import play.api.test._
import org.specs2.execute.{AsResult, Result}
import collection.mutable.ListBuffer
import play.api.libs.ws.WS
import concurrent.Await
import concurrent.duration.Duration

object IntegrationTestSpec extends Specification {
  "statsd filters" should {

    "report stats on /" in new Setup {
      makeRequest("/")
      receive(count("sample.routes.get"), timing("sample.routes.get"), combinedTime, combinedSuccess, combined200)
    }

    "report stats on simple path" in new Setup {
      makeRequest("/foo/bar")
      receive(count("sample.routes.foo.bar.get"), timing("sample.routes.foo.bar.get"), combinedTime, combinedSuccess, combined200)
    }

    "report stats on path with dynamic end" in new Setup {
      makeRequest("/single/end/blah")
      receive(count("sample.routes.single.end.param.get"), timing("sample.routes.single.end.param.get"), combinedTime, combinedSuccess, combined200)
    }

    "report stats on path with dynamic middle" in new Setup {
      makeRequest("/single/middle/blah/f")
      receive(count("sample.routes.single.middle.param.f.get"), timing("sample.routes.single.middle.param.f.get"), combinedTime, combinedSuccess, combined200)
    }

    "report stats on path with regex" in new Setup {
      makeRequest("/regex/21")
      receive(count("sample.routes.regex.param.get"), timing("sample.routes.regex.param.get"), combinedTime, combinedSuccess, combined200)
    }

    "report stats on path with wildcard" in new Setup {
      makeRequest("/rest/blah/blah")
      receive(count("sample.routes.rest.param.get"), timing("sample.routes.rest.param.get"), combinedTime, combinedSuccess, combined200)
    }

    "report stats on path with multiple params" in new Setup {
      makeRequest("/multiple/foo/bar")
      receive(count("sample.routes.multiple.param1.param2.get"), timing("sample.routes.multiple.param1.param2.get"), combinedTime, combinedSuccess, combined200)
    }

    "report stats on async action" in new Setup {
      makeRequest("/async")
      receive(count("sample.routes.async.get"), timing("sample.routes.async.get"), combinedTime, combinedSuccess, combined200)
    }

    "report stats on failure" in new Setup {
      makeWsRequest("/sync/failure")
      receive(count("sample.routes.sync.failure.get"), timing("sample.routes.sync.failure.get"), combinedTime, combinedError, combined500)
    }

    "report stats on failure thrown in async" in new Setup {
      makeWsRequest("/async/failure")
      receive(count("sample.routes.async.failure.get"), timing("sample.routes.async.failure.get"), combinedTime, combinedError, combined500)
    }

    "report stats on action returning 503" in new Setup {
      makeRequest("/error", 503)
      receive(count("sample.routes.error.get"), timing("sample.routes.error.get"), combinedTime, combinedError, combined503)
    }

    "report stats on handlerNotFound" in new Setup {
      makeWsRequest("/does/not/exist", 404)
      receive(count("sample.routes.combined.handlerNotFound"), timing("sample.routes.combined.handlerNotFound", 0), timing("sample.routes.combined.time", 0), combinedSuccess, combined404)
    }

  }

  def makeRequest(path: String, expectedStatus: Int = 200) {
    status(route(FakeRequest("GET", path)).get) must_== expectedStatus
  }

  def makeWsRequest(path: String, expectedStatus: Int = 500) {
    Await.result(WS.url("http://localhost:9001" + path).get(), Duration.apply("2s")).status must_== expectedStatus
  }

  trait Setup extends Around {
    lazy val PORT = 57476
    implicit lazy val fakeApp = FakeApplication(additionalConfiguration = Map(
      "statsd.enabled" -> "true",
      "statsd.host" -> "localhost",
      "statsd.port" -> PORT.toString,
      "statsd.stat.prefix" -> "sample"))
    lazy val mockStatsd = {
      val socket = new DatagramSocket(PORT)
      socket.setSoTimeout(2000)
      socket
    }

    def receive(ps: PartialFunction[String, Unit]*) = {
      val expects = ListBuffer(ps :_*)
      for (i <- 1 until ps.size + 1) {
        val buf: Array[Byte] = new Array[Byte](1024)
        val packet = new DatagramPacket(buf, buf.length)
        try {
          mockStatsd.receive(packet)
        }
        catch {
          case s: SocketTimeoutException => failure("Didn't receive message no " + i + " within 2s")
        }
        val data = new String(packet.getData, 0, packet.getLength)
        val matched = expects.collectFirst {
          case expect if expect.isDefinedAt(data) => {
            expects -= expect
            expect(data)
          }
        }
        matched aka("No matching assertion for data: '%s' ".format(data)) must beSome[Unit]
      }

      expects must beEmpty
    }


    def around[T](t: => T)(implicit evidence$1: AsResult[T]) = running(TestServer(9001, fakeApp)) {
      mockStatsd
      try {
        AsResult(t)
      } finally {
        mockStatsd.close()
      }
    }
  }

  def combinedSuccess = count("sample.routes.combined.success")
  def combinedError = count("sample.routes.combined.error")
  def combined200 = count("sample.routes.combined.200")
  def combined404 = count("sample.routes.combined.404")
  def combined500 = count("sample.routes.combined.500")
  def combined503 = count("sample.routes.combined.503")
  def combinedTime = timing("sample.routes.combined.time")

  def count(key: String): PartialFunction[String, Unit] = {
    case Count(k) if k == key => Unit
  }

  def timing(key: String, atLeast: Int = 2): PartialFunction[String, Unit] = {
    case Timing(k, time) if k == key => time must beGreaterThanOrEqualTo(atLeast)
  }

  object Timing {
    val regex = """([^:]+):([0-9]+)\|ms""".r
    def unapply(s: String): Option[(String, Int)] = {
      s match {
        case regex(key, millis) => Some((key, millis.toInt))
        case _ => None
      }
    }
  }

  val Count = """([^:]+):1\|c""".r

}
