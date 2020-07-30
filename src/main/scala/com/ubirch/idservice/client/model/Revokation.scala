package com.ubirch.idservice.client.model

import com.ubirch.util.date.DateUtil
import org.joda.time.DateTime

case class SignedRevoke(revokation: Revokation,
                        signature: String
                       )

// fields should be ordered alphabetically as some client libs only produce JSON with alphabetically ordered fields!!!
case class Revokation(curveAlgorithm: String,
                      publicKey: String,
                      revokationDate: DateTime = DateUtil.nowUTC
                     )
