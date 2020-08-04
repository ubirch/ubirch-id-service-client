package com.ubirch.idservice.client.config

object IdClientRouteKeys {

  final val apiPrefix = "api"
  final val serviceName = "keyService"
  final val currentVersion = "v1"
  final val check = "check"
  final val deepCheck = "deepCheck"
  final val pubKey = "pubkey"
  final val current = "current"
  final val hardwareId = "hardwareId"
  final val revoke = "revoke"
  val pathPrefix = s"/$apiPrefix/$serviceName/$currentVersion"
  val pathCheck = s"$pathPrefix/$check"
  val pathDeepCheck = s"$pathPrefix/$deepCheck"
  val pathPubKey = s"$pathPrefix/$pubKey"
  val pathPubKeyRevoke = s"$pathPubKey/$revoke"
  private val pathPubKeyCurrentHardwareId = s"$pathPubKey/$current/$hardwareId"

  def pathPubKeyCurrentHardwareId(hardwareId: String): String = s"$pathPubKeyCurrentHardwareId/$hardwareId"

}
