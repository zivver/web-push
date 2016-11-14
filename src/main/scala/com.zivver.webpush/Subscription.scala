package com.zivver.webpush

import java.net.{MalformedURLException, URL}
import java.security.PublicKey

case class Subscription(endpoint: String, userPublicKey: String, userAuth: String) {

  @throws[MalformedURLException]
  val origin: String = {
    val url: URL = new URL(endpoint)
    url.getProtocol + "://" + url.getHost
  }

  def publicKey: PublicKey = Utils.loadPublicKey(userPublicKey)
  lazy val auth: Array[Byte] = Utils.base64Decode(userAuth)
}
