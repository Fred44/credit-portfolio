package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasher, Credentials}
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, Environment}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.user.PasswordChangeForm
import models.User
import models.services.{MailService, UserService}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.Messages.Message
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Result, Action}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.{Await, Future}

/**
 * Created by fred on 28/08/15.
 */
class PasswordController @Inject() (
      val messagesApi: MessagesApi,
      val env: Environment[User, CookieAuthenticator],
      credentialsProvider: CredentialsProvider,
      authInfoRepository: AuthInfoRepository,
      passwordHasher: PasswordHasher,
      mailer: MailService,
      userService: UserService)
  extends MailTokenOperation(userService) {

  val startForm = Form(
    "email" -> email.verifying(nonEmpty)
  )

  val changePasswordForm = Form(
    "password" ->
      tuple(
        "password1" -> nonEmptyText.verifying(minLength(4)),
        "password2" -> nonEmptyText.verifying(minLength(4))
      ).verifying(Messages("signUp.passwordsDoNotMatch"), passwords => passwords._1 == passwords._2)
  )

  def startResetPassword = Action.async { implicit req =>
    Future.successful(Ok(views.html.user.startResetPassword(startForm)))
  }

  def handleStartResetPassword = Action.async { implicit req =>
    startForm.bindFromRequest().fold(
      form =>
        Future.successful(BadRequest(views.html.user.startResetPassword(form))),
      data => {
        val email = data.toLowerCase
        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            createToken(email, isSignUp = false).flatMap { token =>
              mailer.sendPasswordResetEmail(user, token.uuid)
              userService.saveToken(token)

              Future.successful(
                Redirect(routes.ApplicationController.signIn)
                  .flashing("success" -> Messages("signUp.thankYouCheckEmail"), "Email" -> email)
              )
            }

          case None =>
            Future.successful(
              Redirect(routes.PasswordController.startResetPassword)
                .flashing(
                  "error" -> "password.reset.unknownEmail"
                )
            )
        }
      }
    )
  }

  def resetPassword(token: String) = Action.async { implicit req =>
    executeForToken(token, false, { _ =>
      Future.successful(Ok(views.html.user.resetPassword(changePasswordForm, token)))
    })
  }

  def handleResetPassword(token: String) = Action.async { implicit req =>
    executeForToken(token, false, { t =>
      changePasswordForm.bindFromRequest().fold(
        form => Future.successful(BadRequest(views.html.user.resetPassword(form, token))),
        data => {
          val authInfo = passwordHasher.hash(data._1)
          val loginInfo = LoginInfo(CredentialsProvider.ID, t.email)
          userService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              for {
                authInfo <- authInfoRepository.update(loginInfo, authInfo)
                authenticator <- env.authenticatorService.create(loginInfo)
                value <- env.authenticatorService.init(authenticator)
                result <- env.authenticatorService.embed(value, Redirect(routes.ApplicationController.index))
              } yield {
                mailer.sendPasswordChangedNotice(user)
                env.eventBus.publish(LoginEvent(user, req, request2Messages))
                result
              }
            case None =>
              Future.successful(
                Redirect(routes.PasswordController.resetPassword(token))
                  .flashing("error" -> "password.error")
              )
          }
        }
      )
    })
  }

  def startChangePassword = SecuredAction.async { implicit req =>
    execute { form =>
      Future.successful(Ok(views.html.user.passwordChange(form)))
    }
  }

  def handleChangePassword = SecuredAction.async { implicit req =>
    execute { form =>
      form.bindFromRequest().fold(
        f => Future.successful(BadRequest(views.html.user.passwordChange(f))),
        data => {
          val authInfo = passwordHasher.hash(data.newPassword)
          val loginInfo = LoginInfo(CredentialsProvider.ID, req.identity.email.get)
          authInfoRepository.update(loginInfo, authInfo).flatMap { _ =>
            mailer.sendPasswordChangedNotice(req.identity)
            Future.successful(
              Redirect(routes.UserProfileController.show)
                .flashing("success" -> Messages("passwordChange.ok"))
            )
          }
        }
      )
    }
  }

  /**
   * checks if the supplied password matches the stored one
   * @param suppliedPassword the password entered in the form
   * @param req the current request
   * @tparam A the type of the user object
   * @return a future boolean
   */
  private def checkCurrentPassword[A](suppliedPassword: String)(implicit req: SecuredRequest[A]): Future[Boolean] = {
    val credentials = Credentials(req.identity.email.get, suppliedPassword)

    credentialsProvider.authenticate(credentials).flatMap { _ =>
      println("Password Ok")
      Future.successful(true)
    }.recover {
      case e: ProviderException =>
        println("password KO")
        false
    }
  }

  private def execute[A](f: Form[PasswordChangeForm.Data] => Future[Result])(implicit request: SecuredRequest[A]): Future[Result] = {
    val form = Form[PasswordChangeForm.Data](
      mapping(
        "currentPassword" ->
          nonEmptyText.verifying(Messages("passwordChange.invalidPassword"), { suppliedPassword =>
            import scala.concurrent.duration._
            Await.result(checkCurrentPassword(suppliedPassword), 10.seconds)
          }),
        "newPassword" ->
          tuple(
            "password1" -> nonEmptyText.verifying(minLength(4)),
            "password2" -> nonEmptyText.verifying(minLength(4))
          ).verifying(Messages("signUp.passwordsDoNotMatch"), passwords => passwords._1 == passwords._2)

      )((currentPassword, newPassword) => PasswordChangeForm.Data(currentPassword, newPassword._1))((changeInfo: PasswordChangeForm.Data) => Some(("", ("", ""))))
    )

    if (request.identity.loginInfo.providerID == CredentialsProvider.ID)
      f(form)
    else
      Future.successful(Forbidden)
  }
}
