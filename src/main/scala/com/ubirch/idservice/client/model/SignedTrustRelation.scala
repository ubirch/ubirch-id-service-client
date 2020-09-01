package com.ubirch.idservice.client.model

import org.joda.time.{DateTime, DateTimeZone}

/**
  * author: cvandrei
  * since: 2018-09-06
  */
case class SignedTrustRelation(trustRelation: TrustRelation, signature: String)

// fields should be ordered alphabetically as some client libs only produce JSON with alphabetically ordered fields!!!
case class TrustRelation(
                          created: DateTime = DateTime.now(DateTimeZone.UTC),
                          curveAlgorithm: String,
                          sourcePublicKey: String,
                          targetPublicKey: String,
                          trustLevel: Int = 50, // value range: 1, ..., 100 (higher values have more weight)
                          validNotAfter: Option[DateTime] = None
                        )
