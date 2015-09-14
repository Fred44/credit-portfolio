package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.user.{StartSignUpForm, SignUpForm}
import models.services.{MailService, UserService}
import models.User
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action

import scala.concurrent.Future

/**
 * The sign up controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info repository implementation.
 * @param avatarService The avatar service implementation.
 * @param passwordHasher The password hasher implementation.
 */
class SignUpController @Inject() (
     val messagesApi: MessagesApi,
     val env: Environment[User, CookieAuthenticator],
     userService: UserService,
     authInfoRepository: AuthInfoRepository,
     avatarService: AvatarService,
     passwordHasher: PasswordHasher,
     mailer: MailService)
     extends MailTokenOperation(userService) {


  def handleStartSignUp = Action.async { implicit req =>
    StartSignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.user.startSignUp(form))),
      data => {
        val email = data.toLowerCase

        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            Future.successful(Redirect(routes.ApplicationController.startSignUp).flashing("error" -> Messages("user.exists")))
          case None => {
            createToken(email, isSignUp = true).flatMap { token =>
              mailer.sendSignUpEmail(email, token.uuid)
              userService.saveToken(token)
            }
            Future.successful(Redirect(routes.ApplicationController.signIn).flashing("success" -> Messages("signUp.thankYouCheckEmail"), "Email" -> email))
          }
        }
      }
    )
  }

  def finishSignUp(token: String) = Action.async { implicit req =>
    executeForToken(token, true, { _ =>
      Future.successful(Ok(views.html.user.signUp(SignUpForm.form, token)))
    })
  }

  def handleFinishSignUp(token: String) = Action.async { implicit req =>
    executeForToken(token, true, {
      t =>
        SignUpForm.form.bindFromRequest.fold(
          form => Future.successful(BadRequest(views.html.user.signUp(form, token))),
          data => {
            val loginInfo = LoginInfo(CredentialsProvider.ID, t.email)
            val authInfo = passwordHasher.hash(data.password1)
            val user = User(
              loginInfo = loginInfo,
              firstName = Some(data.firstName),
              lastName = Some(data.lastName),
              fullName = Some(data.firstName + " " + data.lastName),
              email = Some(t.email),
              avatarURL = None
            )
            for {
              avatar <- avatarService.retrieveURL(t.email)
              user <- userService.save(user.copy(avatarURL = avatar))
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authenticator <- env.authenticatorService.create(loginInfo)
              value <- env.authenticatorService.init(authenticator)
              result <- env.authenticatorService.embed(value, Redirect(routes.ApplicationController.index))
            } yield {
              mailer.sendWelcomeEmail(user)
              env.eventBus.publish(SignUpEvent(user, req, request2Messages))
              env.eventBus.publish(LoginEvent(user, req, request2Messages))
              result
            }
          }
        )
    })
  }
}
