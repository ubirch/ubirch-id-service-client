package com.ubirch.idservice.client.config

import java.net.URLEncoder

import com.ubirch.util.config.ConfigBase

object IdClientConfig extends ConfigBase {

  /**
    * The host the REST API runs on.
    *
    * @return host
    */
  private def host = config.getString(IdClientConstants.HOST)

  val urlCheck = s"$host${IdClientConstants.pathCheck}"

  val urlDeepCheck = s"$host${IdClientConstants.pathDeepCheck}"

  val pubKey = s"$host${IdClientConstants.pathPubKey}"

  val pubKeyRevoke = s"$host${IdClientConstants.pathPubKeyRevoke}"

  def findPubKey(pubKeyString: String): String = {
    s"$pubKey/${URLEncoder.encode(pubKeyString, "UTF-8")}"
  }

  def currentlyValidPubKeys(hardwareId: String) = s"$host${IdClientConstants.pathPubKeyCurrentHardwareId(hardwareId)}"

  /**
    * @return maximum time-to-live in seconds for records to cache
    */
  def maxTTL: Int = config.getInt(IdClientConstants.maxTTL)
}
