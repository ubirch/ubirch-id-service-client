package com.ubirch.idservice.client.config

import com.ubirch.util.config.ConfigBase

object IdClientConfig extends ConfigBase {

  /**
   * The host the REST API runs on.
   *
   * @return host
   */
  def host: String = config.getString(IdClientConfigKeys.HOST)

  /**
   * @return maximum time-to-live in seconds for records to cache
   */
  def maxTTL: Int = config.getInt(IdClientConfigKeys.maxTTL)
}
