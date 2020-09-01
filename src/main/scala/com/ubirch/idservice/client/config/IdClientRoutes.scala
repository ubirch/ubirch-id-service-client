package com.ubirch.idservice.client.config

import java.net.URLEncoder

import com.ubirch.idservice.client.config.IdClientConfig.host

object IdClientRoutes {

  val urlCheck = s"$host${IdClientRouteKeys.pathCheck}"

  val urlDeepCheck = s"$host${IdClientRouteKeys.pathDeepCheck}"

  val pubKey = s"$host${IdClientRouteKeys.pathPubKey}"

  val pubKeyRevoke = s"$host${IdClientRouteKeys.pathPubKeyRevoke}"

  def findPubKey(pubKeyString: String): String = {
    s"$pubKey/${URLEncoder.encode(pubKeyString, "UTF-8")}"
  }

  def currentlyValidPubKeys(hardwareId: String) = s"$host${IdClientRouteKeys.pathPubKeyCurrentHardwareId(hardwareId)}"

}
