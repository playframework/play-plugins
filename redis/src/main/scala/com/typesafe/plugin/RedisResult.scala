package com.typesafe.plugin

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.mvc._
import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global

case class RedisResult(status: Int,
                       headers: Map[String, String],
                       body: Array[Byte]) extends Serializable

object RedisResult
{
  /**
   * Extracts the content as bytes.
   * From play/api/test/Helpers.scala
   */
  def contentAsBytes(of: Result): Array[Byte] = of match {
    case r @ SimpleResult(_, bodyEnumerator) => {
      var readAsBytes = Enumeratee.map[r.BODY_CONTENT](r.writeable.transform(_)).transform(Iteratee.consume[Array[Byte]]())
      bodyEnumerator(readAsBytes).flatMap(_.run).value1.get
    }
    case p:PlainResult => Array[Byte]()
    case AsyncResult(p) => contentAsBytes(p.await.get)
  }

  /**
   * Extracts the Status code of this Result value.
   * From play/api/test/Helpers.scala
   */
  def status(of: Result): Int = of match {
    case PlainResult(status, _) => status
    case AsyncResult(p) => status(p.await.get)
  }

  /**
   * Extracts all Headers of this Result value.
   * From play/api/test/Helpers.scala
   */
  def headers(of: Result): Map[String, String] = of match {
    case PlainResult(_, headers) => headers
    case AsyncResult(p) => headers(p.await.get)
  }

  def wrapResult(result:Result):RedisResult = {
    RedisResult(status(result),
                 headers(result),
                 contentAsBytes(result))
  }

  def unwrapResult(cachedResult:RedisResult) = {
    SimpleResult(ResponseHeader(cachedResult.status, cachedResult.headers),
                 Enumerator(cachedResult.body))
  }
}
