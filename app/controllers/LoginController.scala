package controllers

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.mvc._
import services.encryption.EncryptionService
import services.session.SessionService


@Singleton
class LoginController @Inject()(action: UserInfoAction,
                                sessionGenerator: SessionGenerator,
                                cc: ControllerComponents) extends AbstractController(cc) {

  def login = action { implicit request: UserRequest[AnyContent] =>
    val successFunc = { userInfo: UserInfo =>
      val (sessionId, encryptedCookie) = sessionGenerator.createSession(userInfo)
      val session = request.session + (SESSION_ID -> sessionId)
      Redirect(routes.HomeController.index())
        .withSession(session)
        .withCookies(encryptedCookie)
    }

    val errorFunc = { badForm: Form[UserInfo] =>
      BadRequest(views.html.index(badForm)).flashing(FLASH_ERROR -> "Could not login!")
    }

    form.bindFromRequest().fold(errorFunc, successFunc)
  }

}

@Singleton
class SessionGenerator @Inject()(sessionService: SessionService,
                                 userInfoService: EncryptionService,
                                 factory: UserInfoCookieBakerFactory) {

  def createSession(userInfo: UserInfo): (String, Cookie) = {
    // create a user info cookie with this specific secret key
    val secretKey = userInfoService.newSecretKey
    val cookieBaker = factory.createCookieBaker(secretKey)
    val userInfoCookie = cookieBaker.encodeAsCookie(Some(userInfo))

    // Tie the secret key to a session id, and store the session id in client side cookie
    val sessionId = sessionService.create(secretKey)
    (sessionId, userInfoCookie)
  }

}
