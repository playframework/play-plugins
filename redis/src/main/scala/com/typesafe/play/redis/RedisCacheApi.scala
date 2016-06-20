package com.typesafe.play.redis

import java.io._
import javax.inject.{Inject, Singleton}

import biz.source_code.base64Coder.Base64Coder
import org.sedis.Pool
import play.api.Logger
import play.api.cache.CacheApi

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag


@Singleton
class RedisCacheApi @Inject()(val namespace: String, sedisPool: Pool, classLoader: ClassLoader) extends CacheApi {

  private val namespacedKey: (String => String) = { x => s"$namespace::$x" }

  def get[T](userKey: String)(implicit ct: ClassTag[T]): Option[T] = {
    Logger.trace(s"Reading key ${namespacedKey(userKey)}")

    try {
      val rawData = sedisPool.withJedisClient { client => client.get(namespacedKey(userKey)) }
      rawData match {
        case null =>
          None
        case _ =>
          val data: Seq[String] = rawData.split("-")
          val bytes = Base64Coder.decode(data.last)
          data.head match {
            case "oos" => Some(withObjectInputStream(bytes)(_.readObject().asInstanceOf[T]))
            case "string" => Some(withDataInputStream(bytes)(_.readUTF().asInstanceOf[T]))
            case "int" => Some(withDataInputStream(bytes)(_.readInt().asInstanceOf[T]))
            case "long" => Some(withDataInputStream(bytes)(_.readLong().asInstanceOf[T]))
            case "boolean" => Some(withDataInputStream(bytes)(_.readBoolean().asInstanceOf[T]))
            case _ => throw new IOException(s"was not able to recognize the type of serialized value. The type was ${data.head} ")
          }
      }
    } catch {
      case ex: Exception =>
        Logger.warn("could not deserialize key:" + namespacedKey(userKey), ex)
        None
    }
  }

  def getOrElse[A: ClassTag](userKey: String, expiration: Duration)(orElse: => A) = {
    get[A](userKey).getOrElse {
      val value = orElse
      set(userKey, value, expiration)
      value
    }
  }

  def remove(userKey: String): Unit = sedisPool.withJedisClient(_.del(namespacedKey(userKey)))

  def set(userKey: String, value: Any, expiration: Duration) {
    val expirationInSec = if (expiration == Duration.Inf) 0 else expiration.toSeconds.toInt
    val key = namespacedKey(userKey)

    var oos: ObjectOutputStream = null
    var dos: DataOutputStream = null
    try {
      val baos = new ByteArrayOutputStream()
      val prefix = value match {
        case _: String =>
          dos = new DataOutputStream(baos)
          dos.writeUTF(value.asInstanceOf[String])
          "string"
        case _: Int =>
          dos = new DataOutputStream(baos)
          dos.writeInt(value.asInstanceOf[Int])
          "int"
        case _: Long =>
          dos = new DataOutputStream(baos)
          dos.writeLong(value.asInstanceOf[Long])
          "long"
        case _: Boolean =>
          dos = new DataOutputStream(baos)
          dos.writeBoolean(value.asInstanceOf[Boolean])
          "boolean"
        case _: Serializable =>
          oos = new ObjectOutputStream(baos)
          oos.writeObject(value)
          oos.flush()
          "oos"
        case _ =>
          throw new IOException("could not serialize: " + value.toString)
      }

      val redisV = prefix + "-" + new String(Base64Coder.encode(baos.toByteArray))
      Logger.trace(s"Setting key $key to $redisV")

      sedisPool.withJedisClient { client =>
        client.set(key, redisV)
        if (expirationInSec != 0) client.expire(key, expirationInSec)
      }
    } catch {
      case ex: IOException =>
        Logger.warn("could not serialize key:" + key + " and value:" + value.toString + " ex:" + ex.toString)
      case ex: Exception =>
        Logger.warn("Unhandled exception trying to set a value in redis for the following key: " + key, ex)
    } finally {
      if (oos != null) oos.close()
      if (dos != null) dos.close()
    }
  }

  private class ClassLoaderObjectInputStream(stream: InputStream) extends ObjectInputStream(stream) {
    override protected def resolveClass(desc: ObjectStreamClass) = {
      Class.forName(desc.getName, false, classLoader)
    }
  }

  private def withDataInputStream[T](bytes: Array[Byte])(f: DataInputStream => T): T = {
    val dis = new DataInputStream(new ByteArrayInputStream(bytes))
    try f(dis) finally dis.close()
  }

  private def withObjectInputStream[T](bytes: Array[Byte])(f: ObjectInputStream => T): T = {
    val ois = new ClassLoaderObjectInputStream(new ByteArrayInputStream(bytes))
    try f(ois) finally ois.close()
  }
}
