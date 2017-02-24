package controllers

import javax.inject._

import play.api.mvc._

@Singleton
class HomeController @Inject() (
  userAction: UserInfoAction
) extends Controller {

  def index = userAction { implicit request: UserRequest[_] =>
    Ok(views.html.index(form))
  }

}
