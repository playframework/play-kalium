package services.session

import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

/**
 * A session service that ties session id to secret key using cache
 */
@Singleton
class SessionService @Inject() (cacheApi: play.api.cache.CacheApi) {

  def create(secretKey: Array[Byte]): Future[String] = {
    val sessionId = newSessionId()
    cacheApi.set(sessionId, secretKey)
    Future.successful(sessionId)
  }

  def lookup(sessionId: String): Future[Option[Array[Byte]]] = {
    Future.successful(cacheApi.get(sessionId))
  }

  def put(sessionId: String, secretKey: Array[Byte]): Future[Unit] = {
    Future.successful(cacheApi.set(sessionId, secretKey))
  }

  def delete(sessionId: String): Future[Unit] = {
    Future.successful(cacheApi.remove(sessionId))
  }

  private val sr = new java.security.SecureRandom()

  private def newSessionId(): String = {
    new java.math.BigInteger(130, sr).toString(32)
  }
}
