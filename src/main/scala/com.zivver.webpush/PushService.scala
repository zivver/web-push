package com.zivver.webpush

import java.security.interfaces.{ECPrivateKey, ECPublicKey}
import java.util.Base64

import com.zivver.webpush.Encryption.Encrypted
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import pdi.jwt.Jwt
import pdi.jwt.JwtAlgorithm.ES256

import scala.concurrent.duration._

/**
  * Push service.
  */
case class PushService(publicKey: ECPublicKey, privateKey: ECPrivateKey, subject: String, exp: FiniteDuration = 12.hours) {

  private val base64encoder = Base64.getUrlEncoder
  private val defaultTtl: Int = 2419200
  private val httpClient: HttpClient = HttpClients.createDefault

  /**
    * Send a data free push notification.
    *
    * @param subscription Browser subscription object.
    * @return HttpResponse from push server.
    */
  def send(subscription: Subscription): HttpResponse = send(subscription, None, defaultTtl)

  /**
    * Send a data free push notification.
    *
    * @param subscription Browser subscription object.
    * @param ttl          Suggestion to the message server for how long it should keep the message
    *                     and attempt to deliver it.
    * @return HttpResponse from push server.
    */
  def send(subscription: Subscription, ttl: Int): HttpResponse = send(subscription, None, ttl)

  /**
    * Sends a data bearing push notification.
    *
    * @param subscription Browser subscription object.
    * @param payload      Push notification payload.
    * @param ttl          Optional suggestion to the message server for how long it should keep the message
    *                     and attempt to deliver it. If not specified default value will be used.
    * @return HttpResponse from push server.
    */
  def send(subscription: Subscription, payload: String, ttl: Int): HttpResponse = send(subscription, Some(payload.getBytes), ttl)

  def send(subscription: Subscription, payload: String): HttpResponse = send(subscription, Some(payload.getBytes), defaultTtl)

  /**
    *
    * Sends a data bearing push notification.
    *
    * @param subscription Browser subscription object.
    * @param payload      Push notification data as a Byte Array.
    * @param ttl          Optional suggestion to the message server for how long it should keep the message
    *                     and attempt to deliver it. If not specified default value will be used.
    * @return HttpResponse from push server.
    */
  def send(subscription: Subscription, payload: Array[Byte], ttl: Int = defaultTtl): HttpResponse = send(subscription, Some(payload), ttl)

  /**
    * Returns the server public key as a URL safe base64 string.
    */
  def publicKeyToBase64: String = {
    base64encoder.withoutPadding().encodeToString(Utils.publicKeyToBytes(publicKey.asInstanceOf[ECPublicKey]))
  }

  private def send(subscription: Subscription, payload: Option[Array[Byte]], ttl: Int) = {

    val httpPost = new HttpPost(subscription.endpoint)

    payload.fold(vapidHeaders(subscription.origin, ttl)) {
      p =>
        val (encryptionHeaders, content) = handleEncryption(p, subscription)
        httpPost.setEntity(new ByteArrayEntity(content))
        vapidHeaders(subscription.origin, ttl) ++ encryptionHeaders
    }.foreach { case (k, v) => httpPost.addHeader(new BasicHeader(k, v)) }
    httpClient.execute(httpPost)
  }

  private def vapidHeaders(origin: String, ttl: Int): Map[String, String] = {
    Map(
      "TTL" -> ttl.toString,
      "Authorization" -> (
        "WebPush " + Jwt.encode(Utils.toJsonString(Map(
          "aud" -> origin,
          "exp" -> ((System.currentTimeMillis() + exp.toMillis) / 1000).toString,
          "sub" -> subject
        )), privateKey, ES256)),
      "Crypto-Key" -> ("p256ecdsa=" + publicKeyToBase64)
    )
  }

  private def handleEncryption(payload: Array[Byte], subscription: Subscription): (Map[String, String], Array[Byte]) = {
    val encrypted: Encrypted = Encryption.encrypt(payload, subscription.publicKey, subscription.auth)
    (Map(
      "Content-Encoding" -> "aesgcm",
      "Encryption" -> ("keyid=p256dh;salt=" + base64encoder.withoutPadding().encodeToString(encrypted.salt)),
      "Crypto-Key" -> ("keyid=p256dh;dh=" + base64encoder.encodeToString(Utils.publicKeyToBytes(encrypted.publicKey.asInstanceOf[ECPublicKey])) +
        ";p256ecdsa=" + base64encoder.withoutPadding().encodeToString(Utils.publicKeyToBytes(publicKey.asInstanceOf[ECPublicKey])))
    ), encrypted.ciphertext)
  }
}
