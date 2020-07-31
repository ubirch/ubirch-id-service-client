package com.ubirch.idservice.client.config

import java.net.URLEncoder

import com.ubirch.util.config.ConfigBase

object IdClientConfig extends ConfigBase {

  /**
    * The host the REST API runs on.
    *
    * @return host
    */
  private def host = config.getString(IdClientConfigKeys.HOST)

  val urlCheck = s"$host${IdClientConfigKeys.pathCheck}"

  val urlDeepCheck = s"$host${IdClientConfigKeys.pathDeepCheck}"

  val pubKey = s"$host${IdClientConfigKeys.pathPubKey}"

  val pubKeyRevoke = s"$host${IdClientConfigKeys.pathPubKeyRevoke}"

  def findPubKey(pubKeyString: String): String = {
    s"$pubKey/${URLEncoder.encode(pubKeyString, "UTF-8")}"
  }

  def currentlyValidPubKeys(hardwareId: String) = s"$host${IdClientConfigKeys.pathPubKeyCurrentHardwareId(hardwareId)}"

  /**
    * @return maximum time-to-live in seconds for records to cache
    */
  def maxTTL: Int = config.getInt(IdClientConfigKeys.maxTTL)
}
