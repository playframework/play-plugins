package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def data = Action {
    import play.api.libs.json._
    Ok(
      Json.obj(
        "name" -> "Json",
        "count" -> 1
      )
    )
  }

}