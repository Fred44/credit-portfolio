package models.services

import javax.inject.Inject

import models.User

import play.api.libs.mailer.{Email, MailerClient}
import play.api.{Logger, Configuration}
import play.api.i18n.{MessagesApi, I18nSupport, Messages, Lang}
import play.api.mvc.RequestHeader
import play.twirl.api.{Txt, Html}

/**
 * Created by fred on 18/08/15.
 */
trait MailService {

  def sendSignUpEmail(to: String, token: String)(implicit req: RequestHeader, lang: Lang)
  def sendWelcomeEmail(user: User)(implicit req: RequestHeader, lang: Lang)
  def sendPasswordResetEmail(user: User, token: String)(implicit req: RequestHeader, lang: Lang)
  def sendPasswordChangedNotice(user: User)(implicit req: RequestHeader, lang: Lang)
  def sendEmail(subject: String, recipient: String, body: (Option[Txt], Option[Html]))
}

object MailService {

  class Default @Inject() (configuration: Configuration, mailer: MailerClient, val messagesApi: MessagesApi)
    extends MailService with I18nSupport {

    val fromAddress = configuration.getString("play.mailer.from").get

    override def sendSignUpEmail(to: String, token: String)(implicit req: RequestHeader, lang: Lang): Unit = {
      val body = (None, Some(views.html.mails.signUpEmail(token)))
      sendEmail(Messages("mails.sendSignUpEmail.subject"), to, body)
    }

    override def sendWelcomeEmail(user: User)(implicit req: RequestHeader, lang: Lang): Unit = {
      user.email.map { to =>
        val body = (None, Some(views.html.mails.welcomeEmail(user)))
        sendEmail(Messages("mails.welcomeEmail.subject"), to, body)
      }
    }

    override def sendPasswordResetEmail(user: User, token: String)(implicit req: RequestHeader, lang: Lang): Unit = {
      user.email.map { to =>
        val body = (None, Some(views.html.mails.passwordResetEmail(user, token)))
        sendEmail(Messages("mails.passwordResetEmail.subject"), to, body)
      }
    }

    override def sendPasswordChangedNotice(user: User)(implicit req: RequestHeader, lang: Lang): Unit = {
      user.email.map { to =>
        val body = (None, Some(views.html.mails.passwordChangedNotice(user)))
        sendEmail(Messages("mails.passwordResetOk.subject"), to, body)
      }
    }

    override def sendEmail(subject: String, recipient: String, body: (Option[Txt], Option[Html])) {

      Logger.debug(s"sending email to $recipient")
      Logger.debug(s"mail = [$body]")

      val email = Email(
        subject,
        fromAddress,
        Seq(recipient),
        // sends text, HTML or both...
        bodyText = body._1.map(_.body),
        bodyHtml = body._2.map(_.body)
      )
      mailer.send(email)
    }
  }

}