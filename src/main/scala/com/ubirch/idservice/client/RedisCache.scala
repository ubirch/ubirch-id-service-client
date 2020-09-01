package com.ubirch.idservice.client

import akka.actor.ActorSystem
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.idservice.client.config.IdClientConfig
import com.ubirch.idservice.client.model.{PublicKey, PublicKeyInfo}
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.redis.RedisClientUtil
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.{ExecutionContextExecutor, Future}

object RedisCache extends StrictLogging {

  private val keyRoot = "keyService.cache"

  def getRedisKeyForPublicKey(publicKey: String): String = s"$keyRoot.publicKey.$publicKey"

  def getRedisKeyForHardwareId(hardwareId: String): String = s"$keyRoot.hardwareId.$hardwareId"

  /**
    * Caches a public key in Redis if necessary.
    * The input result from a request to the `key-service` for a public key.
    *
    * @param publicKeyOpt public key to add to cache
    * @return the unchanged input after trying to cache it if necessary
    */
  def cacheValidPublicKey(publicKeyOpt: Option[PublicKey])
                         (implicit system: ActorSystem): Future[Option[PublicKey]] = {
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    publicKeyOpt match {

      case None =>

        Future(None)

      case Some(result) =>

        val redis = RedisClientUtil.getRedisClient
        val expiry = expireInSeconds(result.pubKeyInfo)
        val pubKeyString = result.pubKeyInfo.pubKey
        val cacheKey = getRedisKeyForPublicKey(pubKeyString)
        val json = Json4sUtil.any2String(result).get
        redis.set[String](cacheKey, json, exSeconds = Some(expiry), NX = true) map {

          case true =>

            logger.debug(s"cached public key: key=$cacheKey (expiry = $expiry seconds)")
            Some(result)

          case false =>

            logger.error(s"failed to add to key-service rest client cache: key=$cacheKey")
            Some(result)

        }

    }

  }

  /**
    * Caches a set of valid public keys in Redis if necessary. The input result from a request to the `key-service` for
    * all currently valid public keys.
    *
    * @param hardwareId      hardwareId the set of public keys belongs to
    * @param publicKeySetOpt set of public keys to add to cache
    * @return the unchanged input after trying to cache it if necessary
    */
  def cacheValidKeys(hardwareId: String, publicKeySetOpt: Option[Set[PublicKey]])
                    (implicit system: ActorSystem): Future[Option[Set[PublicKey]]] = {
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    publicKeySetOpt match {

      case Some(pubKeySet) if pubKeySet.nonEmpty =>

        val redis = RedisClientUtil.getRedisClient

        val expiry = expireInSeconds(pubKeySet)

        val cacheKey = getRedisKeyForHardwareId(hardwareId)

        val json = Json4sUtil.any2String(pubKeySet).get
        redis.set[String](cacheKey, json, exSeconds = Some(expiry), NX = true) map {

          case true =>

            logger.debug(s"cached valid public keys $pubKeySet: key=$cacheKey (expiry = $expiry seconds)")
            Some(pubKeySet)

          case false =>

            logger.error(s"failed to add pubKeys $pubKeySet to rest client cache: key=$cacheKey")
            Some(pubKeySet)

        }

      case _ =>

        Future(Some(Set.empty))

    }
  }

  /**
    * This method checks how long the key still is valid and takes either
    * the configured maxTTL or the keys notValidAfter date, depending on
    * which expires first.
    *
    * @return seconds for cache entry to live
    */
  private def expireInSeconds(pubKeyInfo: PublicKeyInfo): Int = {

    val maxTTL = IdClientConfig.maxTTL
    val now = DateTime.now(DateTimeZone.UTC)
    val maxExpiryDate = now.plusSeconds(maxTTL)
    pubKeyInfo.validNotAfter match {

      case Some(validNotAfter) if validNotAfter.isBefore(maxExpiryDate.getMillis) =>

        (validNotAfter.getMillis - now.getMillis / 1000).toInt

      case _ =>

        maxTTL
    }
  }

  /**
    * This method checks how long the keys are valid and takes either
    * the configured maxTTL or the earliest validNotAfter date of the keys,
    * depending on which expires first.
    *
    * @return seconds for cache entry to live
    */
  private def expireInSeconds(pubKeySet: Set[PublicKey]): Int = {

    val maxTTL = IdClientConfig.maxTTL

    if (pubKeySet.isEmpty) {

      maxTTL

    } else {

      val defaultValidNotAfter = DateTime.now(DateTimeZone.UTC).plusSeconds(maxTTL).getMillis
      val validNotAfterSet = pubKeySet.map(_.pubKeyInfo.validNotAfter).map {
        case None => defaultValidNotAfter
        case Some(validNotAfter) => validNotAfter.getMillis
      }
      val earliestValidNotAfter = validNotAfterSet.min

      if (earliestValidNotAfter >= defaultValidNotAfter) {

        maxTTL

      } else {

        val now = DateTime.now(DateTimeZone.UTC).getMillis
        (earliestValidNotAfter - now / 1000).toInt

      }
    }
  }

}
