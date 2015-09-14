package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api.{Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.services.{UserService}
import models.{User, MailToken}
import org.joda.time.DateTime
import play.api.i18n.{Messages}
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by fred on 04/09/15.
 */
abstract class MailTokenOperation (userService: UserService)
  extends Silhouette[User, CookieAuthenticator] {

  val TokenDuration = 60


  /**
   * Creates a token for mail based operations
   *
   * @param email the email address
   * @param isSignUp a boolean indicating if the token is used for a signup or password reset operation
   * @return a MailToken instance
   */
  def createToken(email: String, isSignUp: Boolean): Future[MailToken] = {
    val now = DateTime.now

    Future.successful(MailToken(
      UUID.randomUUID().toString, email.toLowerCase, now, now.plusMinutes(TokenDuration), isSignUp = isSignUp
    ))
  }

  /**
   * Helper method to execute actions where a token needs to be retrieved from
   * the backing store
   *
   * @param token the token id
   * @param isSignUp a boolean indicating if the token is used for a signup or password reset operation
   * @param f the function that gets invoked if the token exists
   * @param request the current request
   * @return the action result
   */
  protected def executeForToken(token: String, isSignUp: Boolean,
                                f: MailToken => Future[Result])(implicit request: RequestHeader): Future[Result] = {
    userService.findToken(token).flatMap {
      case Some(t) if !t.isExpired && t.isSignUp == isSignUp => f(t)
      case _ =>
        val to = if (isSignUp) routes.ApplicationController.startSignUp else routes.PasswordController.startResetPassword
        Future.successful(Redirect(to).flashing("error" -> Messages("signUp.invalidLink")))
    }
  }
}
