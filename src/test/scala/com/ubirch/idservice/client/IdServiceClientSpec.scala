package com.ubirch.idservice.client

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import com.github.sebruck.EmbeddedRedis
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.crypto.utils.Curve
import com.ubirch.crypto.{GeneratorKeyFactory, PrivKey}
import com.ubirch.idservice.client.model.{PublicKey, PublicKeyDelete, PublicKeyInfo}
import com.ubirch.util.json.JsonFormats
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.Formats
import org.json4s.native.Serialization.write
import org.scalatest.featurespec.AsyncFeatureSpec
import org.scalatest.matchers.should.Matchers

import java.util.{Base64, UUID}

class IdServiceClientSpec extends AsyncFeatureSpec with Matchers with EmbeddedRedis with StrictLogging {

  implicit val system: ActorSystem = ActorSystem("idServiceClientSpec")
  implicit val httpClient: HttpExt = Http()

  implicit def json4sJacksonFormats: Formats = JsonFormats.default

  private val (publicKey: PublicKey, privateKey) = getPublicKey()
  private val signature = privateKey.sign(privateKey.getRawPublicKey)
  private val signatureAsString = Base64.getEncoder.encodeToString(signature)
  private val pubDelete = PublicKeyDelete(publicKey.pubKeyInfo.pubKeyId, signatureAsString)
  private val pubDeleteFalseSignature = PublicKeyDelete(publicKey.pubKeyInfo.pubKeyId, "signatureAsString")

  Feature("user service checks") {

    Scenario("check") {
      IdServiceClientCached.check() map { jsonResponseOpt =>
        jsonResponseOpt.nonEmpty shouldBe true
        jsonResponseOpt.get.status shouldBe "OK"
      }
    }

    Scenario("deepCheck") {
      IdServiceClientCached.deepCheck() map { deepCheckResponse =>
        deepCheckResponse.status shouldBe true
      }
    }
  }

  Feature("key requests") {

    Scenario("get no valid public keys cached") {
      withRedisAsync(6379) { _ =>
        IdServiceClientCached.currentlyValidPubKeysCached(publicKey.pubKeyInfo.hwDeviceId).map { publicKeyOpt =>
          publicKeyOpt.nonEmpty shouldBe true
          val publicKeySet = publicKeyOpt.get
          publicKeySet.isEmpty shouldBe true
        }
      }
    }

    Scenario("post public key") {
      IdServiceClientCached.pubKeyPOST(publicKey).map { pubKeyOpt =>
        pubKeyOpt.nonEmpty shouldBe true
        pubKeyOpt.get shouldBe publicKey
      }
    }

    Scenario("get public key cached") {
      withRedisAsync(6379) { _ =>
        IdServiceClientCached.findValidPubKeyCached(publicKey.pubKeyInfo.pubKeyId).map { publicKeyOpt =>
          publicKeyOpt.nonEmpty shouldBe true
          publicKeyOpt.get shouldBe publicKey
        }
      }
    }

    Scenario("get valid public keys cached") {
      withRedisAsync(6379) { _ =>
        IdServiceClientCached.currentlyValidPubKeysCached(publicKey.pubKeyInfo.hwDeviceId).map { publicKeyOpt =>
          publicKeyOpt.nonEmpty shouldBe true
          publicKeyOpt.get shouldBe Set(publicKey)
        }
      }
    }

    Scenario("delete public key") {

      IdServiceClientCached.pubKeyDELETE(pubDelete).map { success =>
        success shouldBe true
      }
    }

    Scenario("delete non existing public key") {

      IdServiceClientCached.pubKeyDELETE(pubDeleteFalseSignature).map { success =>
        success shouldBe false
      }
    }

  }

  private def getPublicKey(hardwareDeviceId: String = UUID.randomUUID().toString) = {

    val created = DateTime.now(DateTimeZone.UTC)
    val validNotAfter = Some(created.plusMonths(6))

    val curve = Curve.Ed25519
    val newPrivKey: PrivKey = GeneratorKeyFactory.getPrivKey(curve)
    val newPublicKey: String = Base64.getEncoder.encodeToString(newPrivKey.getRawPublicKey)

    logger.info(s"private key $newPrivKey and it's pubKeyId id $newPublicKey")

    val pubKeyInfo = PublicKeyInfo(
      algorithm = "Ed25519",
      hwDeviceId = hardwareDeviceId,
      pubKey = newPublicKey,
      pubKeyId = newPublicKey,
      prevPubKeyId = None,
      validNotAfter = validNotAfter
    )

    val signature = sign(pubKeyInfo, newPrivKey)

    val publicKey = PublicKey(pubKeyInfo, signature)

    (publicKey, newPrivKey)
  }

  def sign(publicKeyInfo: PublicKeyInfo, privKey: PrivKey): String = {

    val publicKeyInfoAsString = write[PublicKeyInfo](publicKeyInfo)
    val signatureAsBytes = privKey.sign(publicKeyInfoAsString.getBytes)
    val signature = Base64.getEncoder.encodeToString(signatureAsBytes)
    signature
  }

}
