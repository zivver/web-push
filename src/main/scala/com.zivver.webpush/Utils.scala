package com.zivver.webpush

import java.math.BigInteger
import java.security._
import java.security.interfaces.{ECPrivateKey, ECPublicKey}
import java.util.Base64

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.{ECNamedCurveParameterSpec, ECPrivateKeySpec, ECPublicKeySpec}
import org.apache.commons.codec.binary.Hex.decodeHex


object Utils {

  def savePrivateKey(privateKey: ECPrivateKey): Array[Byte] = privateKey.getS.toByteArray

  def base64Decode(base64Encoded: String): Array[Byte] = {
    if (base64Encoded.contains("+") || base64Encoded.contains("/")) Base64.getDecoder.decode(base64Encoded)
    else Base64.getUrlDecoder.decode(base64Encoded)
  }

  def loadPublicKey(encodedPublicKey: String): PublicKey = {
    val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("prime256v1")
    KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME)
      .generatePublic(new ECPublicKeySpec(ecSpec.getCurve.decodePoint(base64Decode(encodedPublicKey)), ecSpec))
  }

  def loadPrivateKey(encodedPrivateKey: String): PrivateKey = {
    KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME)
      .generatePrivate(new ECPrivateKeySpec(new BigInteger(base64Decode(encodedPrivateKey)),
        ECNamedCurveTable.getParameterSpec("prime256v1")))
  }

  def toJsonString(json: Map[String, String]): String = {
    json.map { case (k, v) => s""""$k":"$v"""" }.mkString("{", ",", "}")
  }

  def publicKeyToBytes(publicKey: ECPublicKey): Array[Byte] = {
    val point = publicKey.getW
    val x = point.getAffineX.toString(16)
    val y = point.getAffineY.toString(16)

    val sb = new StringBuilder()
    sb.append("04")
    (1 to (64 - x.length)).foreach(_ => sb.append(0))
    sb.append(x)

    (1 to (64 - y.length)).foreach(_ => sb.append(0))
    sb.append(y)
    decodeHex(sb.toString.toCharArray)
  }

}
