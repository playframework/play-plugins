package controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

object Application extends Controller {
  def index = Action {
    Thread.sleep(2)
    Ok
  }

  def singleParam(p: String) = Action {
    Thread.sleep(2)
    Ok
  }

  def twoParams(p1: String, p2: String) = Action {
    Thread.sleep(2)
    Ok
  }

  def async = Action.async {
    Future {
      Thread.sleep(2)
      Ok
    }
  }

  def syncFailure = Action {
    Thread.sleep(2)
    if (true) throw new RuntimeException
    Ok
  }

  def asyncFailure = Action.async {
    Future {
      Thread.sleep(2)
      if (true) throw new RuntimeException
      Ok
    }
  }

  def error = Action {
    Thread.sleep(2)
    ServiceUnavailable
  }
}
