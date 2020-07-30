package com.ubirch.idservice.client.model

import org.joda.time.{DateTime, DateTimeZone}

/**
  * Represents a Data Transfer Object for the Public Key
  *
  * @param algorithm      Represents the algorithm that the key supports
  * @param created        Represents the creation time for the key. This is value is set by the user.
  * @param hwDeviceId     Represents the owner id of the identity
  * @param pubKey         Represents the public key
  * @param pubKeyId       Represents the public key id. If not provided, it is set as the pubKey.
  * @param validNotAfter  Represents when in the future the key should not be valid anymore.
  * @param validNotBefore Represents when in the future the key should be valid from.
  */
case class PublicKeyInfo(
                          algorithm: String,
                          created: DateTime = DateTime.now(DateTimeZone.UTC),
                          hwDeviceId: String,
                          pubKey: String,
                          pubKeyId: String,
                          prevPubKeyId: Option[String],
                          validNotAfter: Option[DateTime] = None,
                          validNotBefore: DateTime = DateTime.now(DateTimeZone.UTC)
                        )


/**
  * Represents a public key info and its signature. Used for Json Requests.
  *
  * @param pubKeyInfo Represents a Data Transfer Object for the Public Key
  * @param signature  Represents the signature of the pubKeyInfo
  */
case class PublicKey(pubKeyInfo: PublicKeyInfo, signature: String, prevSignature: Option[String] = None)


/**
  * Represents a Deletion Requests.
  *
  * @param publicKey Represents the public key.
  * @param signature Represents the signature of the publicKey
  */
case class PublicKeyDelete(publicKey: String, signature: String)
