package com.zivver.webpush

import java.security.{KeyPairGenerator, PublicKey, SecureRandom}

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider

/**
  * Encryption object to mange payload ECDH encryption.
  */
object Encryption {

  private val localCurve = KeyPairGenerator.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME)
  localCurve.initialize(ECNamedCurveTable.getParameterSpec("prime256v1"))

  def encrypt(buffer: Array[Byte], userPublicKey: PublicKey, userAuth: Array[Byte]): Encrypted = {
    val serverKeys = localCurve.generateKeyPair
    //noinspection ScalaStyle
    val salt = SecureRandom.getSeed(16)
    val ciphertext = HttpEce.encrypt(serverKeys, buffer, salt, userPublicKey, userAuth)
    Encrypted(serverKeys.getPublic, salt, ciphertext)
  }

  case class Encrypted(publicKey: PublicKey, salt: Array[Byte], ciphertext: Array[Byte])

}
