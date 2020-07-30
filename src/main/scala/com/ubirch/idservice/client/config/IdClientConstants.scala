package com.ubirch.idservice.client.config

object IdClientConstants {

  protected val base = "ubirchKeyService.client"

  val HOST = s"$base.rest.host"

  val maxTTL = s"$base.redis.cache.maxTTL" // seconds

  final val apiPrefix = "api"
  final val serviceName = "keyService"
  final val currentVersion = "v1"

  final val check = "check"
  final val deepCheck = "deepCheck"

  final val pubKey = "pubkey"
  final val trust = "trust"
  final val mpack = "mpack"
  final val current = "current"
  final val hardwareId = "hardwareId"
  final val trusted = "trusted"
  final val revoke = "revoke"

  val pathPrefix = s"/$apiPrefix/$serviceName/$currentVersion"

  val pathCheck = s"$pathPrefix/$check"
  val pathDeepCheck = s"$pathPrefix/$deepCheck"

  val pathPubKey = s"$pathPrefix/$pubKey"

  val pathPubKeyMsgPack = s"$pathPrefix/$pubKey/$mpack"

  val pathPubKeyRevoke = s"$pathPubKey/$revoke"

  private val pathPubKeyCurrentHardwareId = s"$pathPubKey/$current/$hardwareId"

  def pathPubKeyCurrentHardwareId(hardwareId: String): String = s"$pathPubKeyCurrentHardwareId/$hardwareId"


}
