package controllers;

import play.api.mvc._
import play.api.libs.json._

import views.html._

object Application extends Controller {
  def frame = Action {
    Ok(index.render());
  }

  def data() = Action {
	val json = Json.obj(
	  "name" -> "Json",
	  "count" -> 1
	)

	Ok(json);
  }
}