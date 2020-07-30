package com.ubirch.idservice.client

import java.util.{Base64, UUID}

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.crypto.utils.Curve
import com.ubirch.crypto.{GeneratorKeyFactory, PrivKey}
import com.ubirch.idservice.client.model.{PublicKey, PublicKeyDelete, PublicKeyInfo}
import com.ubirch.util.date.DateUtil
import com.ubirch.util.json.JsonFormats
import org.json4s.Formats
import org.json4s.native.Serialization.write
import org.scalatest.{AsyncFeatureSpec, Matchers}


class IdSvcClientSpec extends AsyncFeatureSpec with Matchers with StrictLogging {

  implicit val system: ActorSystem = ActorSystem("idServiceClientTest")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val httpClient: HttpExt = Http()

  implicit def json4sJacksonFormats: Formats = JsonFormats.default

  private val (publicKey: PublicKey, privateKey) = getPublicKey()
  private val signature = privateKey.sign(privateKey.getRawPublicKey)
  private val signatureAsString = Base64.getEncoder.encodeToString(signature)
  private val pubDelete = PublicKeyDelete(publicKey.pubKeyInfo.pubKeyId, signatureAsString)

  feature("check") {

    scenario("check") {

      IdServiceClientCached.check() map { jsonResponseOpt =>
        jsonResponseOpt.nonEmpty shouldBe true
        jsonResponseOpt.get.status shouldBe "OK"
      }
    }

    scenario("deepCheck") {

      IdServiceClientCached.deepCheck() map { deepCheckResponse =>
        deepCheckResponse.status shouldBe true
      }
    }
  }

  feature("publish key") {


    scenario("post public key") {

      IdServiceClientCached.pubKeyPOST(publicKey).map { pubKeyOpt =>
        pubKeyOpt.nonEmpty shouldBe true
        pubKeyOpt.get shouldBe publicKey
      }
    }

    scenario("get public key") {
      IdServiceClientCached.findPubKey(publicKey.pubKeyInfo.pubKeyId).map { publicKeyOpt =>
        publicKeyOpt.nonEmpty shouldBe true
        publicKeyOpt.get shouldBe publicKey
      }
    }

    scenario("get public key cached") {
      IdServiceClientCached.findValidPubKeyCached(publicKey.pubKeyInfo.pubKeyId).map { publicKeyOpt =>
        publicKeyOpt.nonEmpty shouldBe true
        publicKeyOpt.get shouldBe publicKey
      }
    }

    scenario("get valid public keys cached") {
      IdServiceClientCached.currentlyValidPubKeysCached(publicKey.pubKeyInfo.hwDeviceId).map { publicKeyOpt =>
        publicKeyOpt.nonEmpty shouldBe true
        publicKeyOpt.get shouldBe Set(publicKey)
      }
    }

    scenario("delete public key") {

      IdServiceClientCached.pubKeyDELETE(pubDelete).map { success =>
        success shouldBe true
      }
    }

    scenario("don't get public key") {
      IdServiceClientCached.findPubKey(publicKey.pubKeyInfo.pubKeyId).map { publicKeyOpt =>
        publicKeyOpt.nonEmpty shouldBe false
      }
    }


  }


  private def getPublicKey(hardwareDeviceId: String = UUID.randomUUID().toString) = {

    val created = DateUtil.nowUTC
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
      validNotAfter = validNotAfter)

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