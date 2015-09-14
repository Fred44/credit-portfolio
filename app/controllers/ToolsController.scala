package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Silhouette, Environment}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import forms.tools.{AmortissementForm, CapitalForm, MensualiteForm}
import models._
import org.joda.time.DateTime
import play.api.i18n.MessagesApi

import scala.concurrent.Future

case class ToolsResult (value: BigDecimal, cout: BigDecimal)

/**
 * Created by fred on 27/08/15.
 */
class ToolsController @Inject() (
                                  val messagesApi: MessagesApi,
                                  val env: Environment[User, CookieAuthenticator])
  extends Silhouette[User, CookieAuthenticator] {

  def showMensualite = UserAwareAction.async { implicit req =>
    Future.successful(Ok (views.html.tools.mensualite (MensualiteForm.form.discardingErrors, req.identity)))
  }

  def submitMensualite = UserAwareAction.async { implicit req =>
    MensualiteForm.form.bindFromRequest.fold (
      form => {
        Future.successful(BadRequest (views.html.tools.mensualite (form, req.identity)))
      }, data => {
        val d = if (data.typeDuree == "A") data.duree * 12 else data.duree
        val credit = Credit (None, -1, "", data.capital, data.taux, d, DateTime.now, Assurance (data.assurance))
        val cout = credit.mensualite * d - data.capital
        Future.successful(Ok (views.html.tools.mensualite (MensualiteForm.form.fill(data), req.identity, Some(ToolsResult(credit.mensualite, cout)))))
      }
    )
  }

  def showCapital = UserAwareAction.async { implicit req =>
    Future.successful(Ok (views.html.tools.capital (CapitalForm.form.discardingErrors, req.identity)))
  }

  def submitCapital = UserAwareAction.async { implicit req =>
    CapitalForm.form.bindFromRequest.fold (
      form => {
        Future.successful(BadRequest (views.html.tools.capital (form, req.identity)))
      }, data => {
        val d = if (data.typeDuree == "A") data.duree * 12 else data.duree
        val c = data.apport.getOrElse(BigDecimal(0)) + CreditCalculation.capital (data.mensualite, data.taux, d, data.assurance)
        Future.successful(Ok (views.html.tools.capital (CapitalForm.form.fill(data), req.identity, Some(ToolsResult(c, data.mensualite * d - c)))))
      }
    )
  }

  def showAmortissement = UserAwareAction.async { implicit req =>
    Future.successful(Ok (views.html.tools.amortissement (AmortissementForm.form.discardingErrors, req.identity)))
  }

  def submitAmortissement = UserAwareAction.async { implicit req =>
    AmortissementForm.form.bindFromRequest.fold (
      form => {
        Future.successful(BadRequest (views.html.tools.amortissement (form, req.identity)))
      }, data => {
        val d = if (data.typeDuree == "A") data.duree * 12 else data.duree
        val credit = Credit (None, -1, "", data.capital, data.taux, d, data.depart, Assurance(data.assurance))

        Future.successful(Ok (views.html.tools.amortissement (AmortissementForm.form.fill(data), req.identity, Some(Amortissement(credit)))))
      }
    )
  }
}
