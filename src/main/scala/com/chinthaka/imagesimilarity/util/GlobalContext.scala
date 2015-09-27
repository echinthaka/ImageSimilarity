package com.chinthaka.imagesimilarity.util

import com.typesafe.config.{ConfigFactory, Config}

/**
 * @author - Eran Withana (eran.chinthaka@gmail.com)
 */
object GlobalContext {

  var hostName = "localhost"
  val config: Config = ConfigFactory.load
  val serverPort = config.getInt("app.server.port")
  val numberOfBinsInHistogram = 16
}
