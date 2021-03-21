package com.rwgs.scalapostgres.tutorial.domain

import io.circe.generic.JsonCodec

@JsonCodec
final case class User(id: BigDecimal,
                      name: String,
                      email: String,
                      active: Boolean = true)
