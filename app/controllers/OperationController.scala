package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Silhouette, Environment}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import forms.{TauxOperationForm, RembourserOperationForm, MensualiteOperationForm}
import models._
import models.daos.CreditDAO
import org.joda.time.DateTime
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import com.github.nscala_time.time.Imports._

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by fred on 14/03/15.
 */
case class Stats (duree: Int, fin: DateTime, cout: BigDecimal)

class OperationController @Inject() (
                                      val messagesApi: MessagesApi,
                                      val creditDAO: CreditDAO,
                                      val env: Environment[User, CookieAuthenticator])
  extends Silhouette[User, CookieAuthenticator] {


  def startMensualiteOperation (id: Long) = SecuredAction.async { implicit req =>

    creditDAO.find (req.identity, id).flatMap {
      case Some ((credit, ope)) =>
        val ech = Amortissement(credit, ope).echeance(DateTime.now)
        Future.successful(Ok (views.html.editMensualite(
          req.identity,
          credit,
          MensualiteOperationForm.form.fill (MensualiteOperation (ech.date + 1.month, BigDecimal(0), ech.mensualite)),
          None
        )))

      case None =>
        Future.successful(Redirect("/credit/list").flashing (
          "error" -> "Le crédit n'existe plus"
        ))
    }
  }

  def startRembourserOperation (id: Long) = SecuredAction.async { implicit req =>

    creditDAO.find (req.identity, id).flatMap {
      case Some((credit, ope)) =>
        val ech = Amortissement(credit, ope).echeance(DateTime.now)
        Future.successful(Ok(views.html.remboursement(
          req.identity,
          credit,
          RembourserOperationForm.form.fill(RemboursementOperation(ech.date + 1.month, BigDecimal(0), ech.mensualite)),
          None
        )))

      case None =>
        Future.successful(Redirect("/credit/list").flashing (
          "error" -> "Le crédit n'existe plus"
        ))
    }
  }

  def startTauxOperation (id: Long) = SecuredAction.async { implicit req =>

    creditDAO.find (req.identity, id).flatMap {
      case Some((credit, ope)) =>
        val ech = Amortissement(credit, ope).echeance(DateTime.now)
        Future.successful(Ok(views.html.editTaux(
          req.identity,
          credit,
          TauxOperationForm.form.fill(TauxOperation(ech.date + 1.month, BigDecimal(0), ech.taux)),
          None
        )))

      case None =>
        Future.successful(Redirect("/credit/list").flashing (
          "error" -> "Le crédit n'existe plus"
        ))
    }
  }

  def handleMensualiteOperation (id: Long, save: Boolean) = SecuredAction.async { implicit req =>
    creditDAO.find (req.identity, id).flatMap {
      case Some((credit, ope)) =>
        MensualiteOperationForm.form.bindFromRequest.fold (
          form => {
            Future.successful(BadRequest (views.html.editMensualite(req.identity, credit, form, None)))

          }, data => {
            val o = data.copy(date = data.date.withDayOfMonth(credit.depart.dayOfMonth().get()))

            if (save) {
              creditDAO.insertOperation(credit.id.get, o)
              Future.successful(Redirect (routes.MesCreditsController.show(id)))
            } else {
              val avant = stats (Amortissement(credit, ope))
              val apres = stats (Amortissement (credit, ope.+:(o)))

              Future.successful(Ok (views.html.editMensualite (req.identity, credit, MensualiteOperationForm.form.fill (data), Some((avant,apres)))))
            }
          }
        )
      case None =>
        Future.successful(Redirect("/credit/list").flashing(
          "error" -> "Ce crédit n'existe plus"
        ))
    }
  }

  def handleRembourserOperation (id: Long, save: Boolean) = SecuredAction.async { implicit req =>
    creditDAO.find (req.identity, id).flatMap {
      case Some((credit, ope)) =>
        RembourserOperationForm.form.bindFromRequest().fold (
          form => {
            Future.successful(BadRequest(views.html.remboursement(req.identity, credit, form, None)))

          }, data => {
            val o = data.copy(date = data.date.withDayOfMonth(credit.depart.dayOfMonth().get()))

            if (save) {
              creditDAO.insertOperation(credit.id.get, o)
              Future.successful(Redirect (routes.MesCreditsController.show(id)))
            } else {
              val avant = stats(Amortissement(credit, ope))
              val apres = stats(Amortissement(credit, ope.+:(o)))

              Future.successful(Ok(views.html.remboursement(req.identity, credit, RembourserOperationForm.form.fill(data), Some((avant, apres)))))
            }
          }
        )
      case None =>
        Future.successful(Redirect("/credit/list").flashing(
          "error" -> "Ce crédit n'existe plus"
        ))
    }
  }

  def handleTauxOperation (id: Long, save: Boolean) = SecuredAction.async { implicit req =>
    creditDAO.find(req.identity, id).flatMap {
      case Some((credit, ope)) =>
        TauxOperationForm.form.bindFromRequest().fold (
          form => {
            Future.successful(BadRequest(views.html.editTaux(req.identity, credit, form, None)))

          }, data => {
            val o = data.copy(date = data.date.withDayOfMonth(credit.depart.dayOfMonth().get()))

            if (save) {
              creditDAO.insertOperation(credit.id.get, o)
              Future.successful(Redirect (routes.MesCreditsController.show(id)))
            } else {
              val avant = stats(Amortissement(credit, ope))
              val apres = stats(Amortissement(credit, ope.+:(o)))

              Future.successful(Ok(views.html.editTaux(req.identity, credit, TauxOperationForm.form.fill(data), Some((avant, apres)))))
            }
          }
        )
      case None =>
        Future.successful(Redirect("/credit/list").flashing(
          "error" -> "Ce crédit n'existe plus"
        ))
    }
  }

  def handleRemove (id: Long, date: String, typ: String) = SecuredAction.async { implicit req =>
    creditDAO.find(req.identity, id).flatMap {
      case Some((credit, ope)) =>
        val d = DateTime.parse(date, DateTimeFormat.forPattern("MM.yyyy")).withDayOfMonth(credit.depart.dayOfMonth().get)
        creditDAO.removeOperation(credit.id.get, d, typ)

        Future.successful(Redirect (routes.MesCreditsController.show (id)))
      case None =>
        Future.successful(Redirect ("/credit/list").flashing (
          "error" -> "Ce crédit n'existe plus"
        ))
    }
  }

  private def stats (amo: Amortissement) = {
    Stats(
      duree = amo.dureeEffective,
      fin = amo.fin,
      cout = amo.cout()
    )
  }
}
