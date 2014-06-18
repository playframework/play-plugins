package com.typesafe.plugin

import play.api.libs.iteratee._
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@SerialVersionUID(7122652360758747455L)
case class RedisResult(status: Int,
                       headers: Map[String, String],
                       body: Array[Byte]) extends Serializable

object RedisResult
{
  def wrapResult(result:Result):Future[RedisResult] = {
    val contentAsBytesFuture = result.body |>>> Iteratee.consume[Array[Byte]]()
    contentAsBytesFuture.map {
      contentAsBytes =>
        RedisResult(result.header.status,
                    result.header.headers,
                    contentAsBytes)
    }
  }

  def unwrapResult(cachedResult:RedisResult) = {
    Result(ResponseHeader(cachedResult.status, cachedResult.headers),
                 Enumerator(cachedResult.body))
  }
}
