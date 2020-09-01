package com.ubirch.idservice.client.config

object IdClientConfigKeys {

  protected val base = "ubirchIdService.client"
  val HOST = s"$base.rest.host"

  val maxTTL = s"$base.redis.cache.maxTTL" // seconds

}
