package com.ubirch.idservice.client.model

import org.joda.time.{DateTime, DateTimeZone}

case class SignedRevoke(
                         revokation: Revokation,
                         signature: String
                       )

// fields should be ordered alphabetically as some client libs only produce JSON with alphabetically ordered fields!!!
case class Revokation(
                       curveAlgorithm: String,
                       publicKey: String,
                       revokationDate: DateTime = DateTime.now(DateTimeZone.UTC)
                     )
