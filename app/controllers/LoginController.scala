package controllers

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.mvc._
import services.session.SessionService
import services.encryption.EncryptionService


@Singleton
class LoginController @Inject()(action: UserInfoAction,
                                sessionService: SessionService,
                                userInfoService: EncryptionService,
                                factory: UserInfoCookieBakerFactory,
                                cc: ControllerComponents) extends AbstractController(cc) {

  def login = action { implicit request: UserRequest[AnyContent] =>
    val successFunc = { userInfo: UserInfo =>
      // create a user info cookie with this specific secret key
      val secretKey = userInfoService.newSecretKey
      val cookieBaker = factory.createCookieBaker(secretKey)
      val userInfoCookie = cookieBaker.encodeAsCookie(Some(userInfo))

      // Tie the secret key to a session id, and store the session id in client side cookie
      val sessionId = sessionService.create(secretKey)
      val session = request.session + (SESSION_ID -> sessionId)

      play.api.Logger.info("Created a new username " + userInfo)

      // client sees the session cookie with the session id, but never sees the secret key.
      Redirect(routes.HomeController.index()).withSession(session).withCookies(userInfoCookie)
    }

    val errorFunc = { badForm: Form[UserInfo] =>
      BadRequest(views.html.index(badForm)).flashing(FLASH_ERROR -> "Could not login!")
    }

    form.bindFromRequest().fold(errorFunc, successFunc)
  }

}
