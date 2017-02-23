package com.zivver.webpush

import java.nio.ByteBuffer
import java.security._
import java.security.interfaces.ECPublicKey
import javax.crypto._
import javax.crypto.spec.{GCMParameterSpec, SecretKeySpec}

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider

/**
  * A simplified implementation of HTTP ECE (Encrypted Content Encoding).
  */
object HttpEce {

  def encrypt(keys: KeyPair, buffer: Array[Byte], salt: Array[Byte], dh: PublicKey, authSecret: Array[Byte]): Array[Byte] = {
    val (key_, nonce_) = deriveKey(keys, salt, dh, authSecret)
    val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding", BouncyCastleProvider.PROVIDER_NAME)
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key_, "AES"), new GCMParameterSpec(16 * 8, nonce_))
    cipher.update(Array.ofDim[Byte](2))
    cipher.doFinal(buffer)
  }

  //noinspection ScalaStyle
  private def deriveKey(keys: KeyPair, salt: Array[Byte], dh: PublicKey, authSecret: Array[Byte]) = {
    val (secret, context) = deriveDH(dh, keys)
    val derived = hkdfExpand(secret, authSecret, buildInfo("auth", Array.ofDim[Byte](0)), 32)
    (hkdfExpand(derived, salt, buildInfo("aesgcm", context), 16),
      hkdfExpand(derived, salt, buildInfo("nonce", context), 12))
  }

  private def deriveDH(publicKey: PublicKey, keys: KeyPair) = {
    val keyAgreement: KeyAgreement = KeyAgreement.getInstance("ECDH")
    keyAgreement.init(keys.getPrivate)
    keyAgreement.doPhase(publicKey, true)
    (keyAgreement.generateSecret,
      "P-256".getBytes ++ Array.ofDim[Byte](1) ++ lengthPrefix(publicKey) ++ lengthPrefix(keys.getPublic))
  }

  private def lengthPrefix(key: Key): Array[Byte] = {
    val bytes = Utils.publicKeyToBytes(key.asInstanceOf[ECPublicKey])
    //    Cast an integer to a two-byte array
    Array((bytes.length >> 8).toByte, (bytes.length & 0xff).toByte) ++ bytes
  }

  private def buildInfo(typeString: String, context: Array[Byte]): Array[Byte] = {
    val buffer: ByteBuffer = ByteBuffer.allocate(19 + typeString.length + context.length)
    buffer.put("Content-Encoding: ".getBytes, 0, 18)
    buffer.put(typeString.getBytes, 0, typeString.length)
    buffer.put(Array.ofDim[Byte](1), 0, 1)
    buffer.put(context, 0, context.length)
    buffer.array
  }

  private def hkdfExpand(ikm: Array[Byte], salt: Array[Byte], info: Array[Byte], length: Int): Array[Byte] = {
    val hkdf = new HKDFBytesGenerator(new SHA256Digest)
    hkdf.init(new HKDFParameters(ikm, salt, info))
    val okm: Array[Byte] = Array.ofDim[Byte](length)
    hkdf.generateBytes(okm, 0, length)
    okm
  }
}
