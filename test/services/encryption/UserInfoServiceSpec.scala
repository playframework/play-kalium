package services.encryption

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest

class UserInfoServiceSpec extends PlaySpec with GuiceOneAppPerTest {

  "encryption info service" should {

    "symmetrically encrypt data" in {
      val service = app.injector.instanceOf(classOf[DefaultEncryptionService])
      val encryptedMap = service.encrypt(UserInfo(terriblePerson = true))
      val decryptedUserInfo = service.decrypt(encryptedMap)
      decryptedUserInfo.terriblePerson mustBe true
    }

  }

}
