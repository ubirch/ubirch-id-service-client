package com.ubirch.idservice.client

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.ubirch.idservice.client.model.PublicKey
import com.ubirch.util.redis.RedisClientUtil
import org.json4s.native.Serialization.read

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * This class represents a wrapper around the IdServiceClientBase that uses a cache to store the answers
  * by the id-service. If redis connection disappears, errors are logged, but it should still be working.
  */
object IdServiceClientCached extends IdServiceClientBase {

  private val keyRoot = "keyService.cache"

  private def getRedisKeyForPublicKey(publicKey: String): String = s"$keyRoot.publicKey.$publicKey"

  private def getRedisKeyForHardwareId(hardwareId: String): String = s"$keyRoot.hardwareId.$hardwareId"

  /**
   * This method retrieves a specific valid publicKey from the cache or
   * the id-service and caches it, if it's not cached yet.
   */
  def findValidPubKeyCached(publicKey: String)
                           (implicit httpClient: HttpExt, materializer: Materializer, system: ActorSystem): Future[Option[PublicKey]] = {

    val cacheKey = getRedisKeyForPublicKey(publicKey)
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    try {
      val redis = RedisClientUtil.getRedisClient()
      redis.get[String](cacheKey) flatMap {

        case None =>
          super.findPubKey(publicKey) flatMap RedisCache.cacheValidPublicKey

        case Some(json) =>
          val publicKey = read[PublicKey](json)
          val expireSeconds = RedisCache.expireInSeconds(publicKey.pubKeyInfo)
          redis.expire(cacheKey, expireSeconds)
          Future(Some(publicKey))

      }
    } catch {
      case ex: Throwable =>
        logger.error(s"finding public key $publicKey in redis  cache failed; fallback to requesting id-service directly", ex)
        super.findPubKey(publicKey)
    }
  }

  /**
   * This method retrieves all valid publicKeys from the cache or the id-service
   * for a certain hardwareId and caches them, if they are not yet.
   */
  def currentlyValidPubKeysCached(hardwareId: String)
                                 (implicit httpClient: HttpExt, materializer: Materializer, system: ActorSystem): Future[Option[Set[PublicKey]]] = {

    logger.debug(s"currentlyValidPubKeysCached(): hardwareId=$hardwareId")
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    val cacheKey = getRedisKeyForHardwareId(hardwareId)

    try {
      val redis = RedisClientUtil.getRedisClient()
      redis.get[String](cacheKey) flatMap {

        case None =>

          super.currentlyValidPubKeys(hardwareId) flatMap (RedisCache.cacheValidKeys(hardwareId, _))

        case Some(json) =>

          val pubKeySet = read[Set[PublicKey]](json)
          if (pubKeySet.isEmpty)
            super.currentlyValidPubKeys(hardwareId) flatMap (RedisCache.cacheValidKeys(hardwareId, _))
          else {
            val expireSeconds = RedisCache.expireInSeconds(pubKeySet)
            redis.expire(cacheKey, expireSeconds)
            Future(Some(pubKeySet))
          }

      }
    } catch {
      case ex: Throwable =>
        logger.error(s"finding valid pub keys for hardwareId $hardwareId in redis cache failed; fallback to requesting id-service directly", ex)
        super.currentlyValidPubKeys(hardwareId)
    }
  }

}
