package com.ubirch.idservice.client

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.ByteString
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.idservice.client.config.IdClientRoutes
import com.ubirch.idservice.client.model._
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.deepCheck.util.DeepCheckResponseUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.{JsonErrorResponse, JsonResponse}
import org.json4s.native.Serialization.read

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A Client to communicate with the id-service. Shouldn't be used, if answers by id-service aren't cached somehow.
  * For a cached implementation use IdServiceClientCached.
  */
trait IdServiceClientBase extends MyJsonProtocol with StrictLogging {

  /**
    * This method calls the deepCheck endpoint of the id-service.
    */
  def check()(implicit httpClient: HttpExt, materializer: Materializer): Future[Option[JsonResponse]] = {

    val url = IdClientRoutes.urlCheck
    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(StatusCodes.OK, _, entity, _) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          Some(read[JsonResponse](body.utf8String))
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        Future(
          logErrorAndReturnNone(s"check() call to key-service failed: url=$url code=$code, status=${res.status}")
        )

    }

  }

  /**
    * This method calls the deepCheck endpoint of the id-service.
    */
  def deepCheck()(implicit httpClient: HttpExt, materializer: Materializer): Future[DeepCheckResponse] = {

    val statusCodes: Set[StatusCode] = Set(StatusCodes.OK, StatusCodes.ServiceUnavailable)

    val url = IdClientRoutes.urlDeepCheck
    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(status, _, entity, _) if statusCodes.contains(status) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          read[DeepCheckResponse](body.utf8String)
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        val errorText = s"deepCheck() call to key-service failed: url=$url code=$code, status=${res.status}"
        logger.error(errorText)
        val deepCheckRes = DeepCheckResponse(status = false, messages = Seq(errorText))
        Future(
          DeepCheckResponseUtil.addServicePrefix("key-service", deepCheckRes)
        )
    }
  }

  /**
    * This method posts a specific pubKey to the id service.
    *
    * @return Some[PublicKey] in case of success else None
    */
  def pubKeyPOST(publicKey: PublicKey)
                (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[PublicKey]] = {

    Json4sUtil.any2String(publicKey) match {

      case Some(pubKeyJsonString: String) =>

        logger.debug(s"pubKey (object): $pubKeyJsonString")
        val url = IdClientRoutes.pubKey
        val req = HttpRequest(
          method = HttpMethods.POST,
          uri = url,
          entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(pubKeyJsonString))
        )
        httpClient.singleRequest(req) flatMap {

          case HttpResponse(StatusCodes.OK, _, entity, _) =>

            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Some(read[PublicKey](body.utf8String))
            }

          case HttpResponse(StatusCodes.BadRequest, _, entity, _) =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              val nok = read[NOK](body.utf8String)
              logger.error(s"pubKeyPOST request failed with error response $nok for $publicKey")
              None
            }

          case res@HttpResponse(code, _, _, _) =>

            res.discardEntityBytes()
            Future(
              logErrorAndReturnNone(s"pubKey() call to key-service failed: url=$url code=$code, status=${res.status}")
            )

        }

      case None =>
        logger.error(s"failed to to convert input to JSON: publicKey=$publicKey")
        Future(None)

    }
  }

  /**
    * This method deletes a specific pubKey from the id service by sending the pubkey
    * and a signature of the private key.
    *
    * @return true in case of success
    */
  def pubKeyDELETE(publicKeyDelete: PublicKeyDelete)
                  (implicit httpClient: HttpExt, materializer: Materializer): Future[Boolean] = {

    Json4sUtil.any2String(publicKeyDelete) match {

      case Some(pubKeyDeleteString: String) =>

        logger.debug(s"pubKeyDelete (object): $pubKeyDeleteString")
        val url = IdClientRoutes.pubKey
        val req = HttpRequest(
          method = HttpMethods.DELETE,
          uri = url,
          entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(pubKeyDeleteString))
        )
        httpClient.singleRequest(req) flatMap {

          case res@HttpResponse(StatusCodes.OK, _, _, _) =>

            res.discardEntityBytes()
            Future(true)

          case res@HttpResponse(code, _, _, _) =>

            res.discardEntityBytes()
            logErrorAndReturnNone(s"pubKeyDELETE() call to key-service failed: url=$url code=$code, status=${res.status}")
            Future(false)

        }

      case None =>
        logger.error(s"failed to to convert input to JSON: publicKeyDelete=$publicKeyDelete")
        Future(false)

    }

  }

  /**
    * This method retrieves a specific (valid) pubKeys from the id service.
    */
  protected def findPubKey(publicKey: String)
                          (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[PublicKey]] = {

    logger.debug(s"publicKey: $publicKey")
    val url = IdClientRoutes.findPubKey(publicKey)
    val req = HttpRequest(
      method = HttpMethods.GET,
      uri = url
    )
    httpClient.singleRequest(req) flatMap {

      case HttpResponse(StatusCodes.OK, _, entity, _) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          Some(read[PublicKey](body.utf8String))
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        logger.warn(s"findPubKey() call to key-service failed: url=$url code=$code, status=${res.status}")
        Future(None)

    }

  }

  /**
    * This method is deprecated and has to become updated, after the revoke endpoint has been added in the new id-service.
    *
    * @return
    */
  @deprecated("this method cannot be used at the moment, but will become re-implemented", "ubirch-id-service-client 0.1.0-SNAPSHOT")
  def pubKeyRevokePOST(signedRevoke: SignedRevoke)
                      (implicit httpClient: HttpExt, materializer: Materializer): Future[Either[JsonErrorResponse, PublicKey]] = {

    Json4sUtil.any2String(signedRevoke) match {

      case Some(revokeJsonString: String) =>

        logger.debug(s"revoke public key (JSON): $revokeJsonString")
        val url = IdClientRoutes.pubKeyRevoke
        val req = HttpRequest(
          method = HttpMethods.POST,
          uri = url,
          entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(revokeJsonString))
        )
        httpClient.singleRequest(req) flatMap {

          case HttpResponse(StatusCodes.OK, _, entity, _) =>

            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Right(read[PublicKey](body.utf8String))
            }

          case res@HttpResponse(code, _, entity, _) =>

            res.discardEntityBytes()
            logger.error(s"pubKeyRevokePOST() call to key-service failed: url=$url code=$code, status=${res.status}")
            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Left(read[JsonErrorResponse](body.utf8String))
            }

        }

      case None =>

        logger.error(s"failed to to convert input to JSON: signedRevoke=$signedRevoke")
        Future(Left(JsonErrorResponse(errorType = "RestClientError", errorMessage = "error before sending the request: failed to convert input to JSON")))

    }

  }

  /**
    * This method retrieves all valid pubKeys from the id service for a certain hardwareId.
    */
  protected def currentlyValidPubKeys(hardwareId: String)
                                     (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[Set[PublicKey]]] = {

    val url = IdClientRoutes.currentlyValidPubKeys(hardwareId)
    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(StatusCodes.OK, _, entity, _) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          Some(read[Set[PublicKey]](body.utf8String))
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        logger.warn(s"currentlyValidPubKeys() call to key-service failed: url=$url, code=$code, status=${res.status}")
        Future(None)
    }

  }

  private def logErrorAndReturnNone[T](
                                        errorMsg: String,
                                        t: Option[Throwable] = None
                                      ): Option[T] = {
    t match {
      case None => logger.error(errorMsg)
      case Some(someThrowable: Throwable) => logger.error(errorMsg, someThrowable)
    }

    None

  }

}
