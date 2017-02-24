package services.encryption

import play.api.libs.crypto.CryptoConfig
import play.api.libs.json.Format
import play.api.mvc._

import scala.concurrent.duration._

/**
 * An encrypted cookie baker that serializes using the encryption service and JSON implicits.
 */
abstract class EncryptedCookieBaker[A: Format](
  secretKey: Array[Byte],
  encryptionService: EncryptionService,
  val cryptoConfig: CryptoConfig
) extends CookieBaker[Option[A]] {

  def expirationDate: FiniteDuration

  def COOKIE_NAME: String

  override val isSigned = true
  override val path: String = "/"
  override val emptyCookie: Option[A] = None

  override lazy val maxAge: Option[Int] = Option(expirationDate).map(_.toSeconds.toInt)

  override protected def serialize(jsonClass: Option[A]): Map[String, String] = {
    encryptionService.encrypt(secretKey, jsonClass)
  }

  override protected def deserialize(stringMap: Map[String, String]): Option[A] = {
    encryptionService.decrypt(secretKey, stringMap)
  }
}
