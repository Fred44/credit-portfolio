package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import forms.CreditForm
import models._
import models.daos.CreditDAO
import org.joda.time.DateTime
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Created by fred on 11/03/15.
 */
class MesCreditsController @Inject() (
                                      val messagesApi: MessagesApi,
                                      val creditDAO: CreditDAO,
                                      val env: Environment[User, CookieAuthenticator])
    extends Silhouette[User, CookieAuthenticator] {

  val maxCredit = 5

  def all = SecuredAction.async { implicit req =>
    val user = req.identity

    creditDAO.find(user).flatMap { cs =>
      val amortissements = cs.map { c => Amortissement(c._1, c._2) }
      val totaux = Map (
        "capital" -> amortissements.map(_.credit.capital).sum,
        "cout" -> amortissements.map(_.cout()).sum,
        "du" -> amortissements.map(_.reste()).sum
      )
      Future.successful(Ok(views.html.list(user)(amortissements, totaux)))
    }
  }

  def show (id: Long, typ: String = "an") = SecuredAction.async { implicit req =>

    creditDAO.find(req.identity, id).flatMap {
      case Some((credit, ops)) =>
        val amo = Amortissement(credit, ops)
        val ech = amo.echeance(DateTime.now)

        val stats = CreditStats(
          credit.taux,
          ech.date,
          ech.mensualite,
          ech.amortissement,
          ech.interets,
          credit.capital,
          amo.amorti(),
          credit.capital - amo.amorti(),
          credit.depart,
          amo.fin,
          credit.duree,
          amo.dureeEffective,
          amo.dureeRestante(),
          amo.cout(),
          amo.cout(DateTime.now))

        val echeances = typ match {
          case "mois" => amo.echeances
          case _ => amo.amortissementSynth
        }

        Future.successful(Ok(views.html.show(req.identity, credit, stats, ops, echeances, typ)))
      case None =>
        Future.successful(BadRequest ("credit not found"))
    }
  }

  def startCreate = SecuredAction.async { implicit req =>
    creditDAO.count(req.identity).flatMap { count =>
      if (count < maxCredit)
        Future.successful(Ok (views.html.create (req.identity)(CreditForm.form.bindFromRequest().discardingErrors)))
      else
        Future.successful(Redirect(routes.MesCreditsController.all()).flashing (
          "error" -> s"Seulement ${maxCredit} crédits sont autorisés"
        ))
    }
  }

  def startEdit (id: Long) = SecuredAction.async { implicit req =>
      creditDAO.find(req.identity, id).flatMap {
        case Some((c, o)) =>
          Future.successful(Ok(
            views.html.edit(
              req.identity,
              c,
              CreditForm.form.fill(CreditForm.Data(c.nom, c.capital, c.taux, c.assurance.taux, c.duree, CreditForm.DUREE_TYPE_MOIS, c.depart)))
            )
          )
        case None =>
          Future.successful(BadRequest("Credit not found"))
      }
  }

  def handleCreate = SecuredAction.async { implicit req =>

    CreditForm.form.bindFromRequest.fold (
      form => {
        Future.successful(BadRequest (views.html.create (req.identity)(form)))

      }, data => {
        val c = Credit(
          userId = req.identity.serID.get,
          nom = data.nom,
          capital = data.capital,
          taux = data.taux,
          duree = if (data.typeDuree == CreditForm.DUREE_TYPE_AN) data.duree * 12 else data.duree,
          depart = data.depart
        )
        creditDAO.save(c).flatMap { c =>
          Future.successful(Redirect(routes.MesCreditsController.all()).flashing (
            "success" -> "Le crédit a été ajouté avec succes"
          ))
        }
      }
    )
  }

  def handleEdit (id: Long) = SecuredAction.async { implicit req =>
    creditDAO.find(req.identity, id).flatMap {
      case Some((c, o)) =>
        CreditForm.form.bindFromRequest.fold (
          form => {
            Future.successful(BadRequest (views.html.edit (req.identity, c, form)))
          }, data => {
            creditDAO.save(c.copy(
                  nom = data.nom,
                  capital = data.capital,
                  taux = data.taux,
                  assurance = Assurance(data.assurance),
                  duree = if (data.typeDuree == CreditForm.DUREE_TYPE_MOIS) data.duree else data.duree * 12,
                  depart = data.depart
            )).flatMap { c =>
              Future.successful(Redirect (routes.MesCreditsController.show(id)))
            }
          }
        )
      case None =>
        Future.successful(BadRequest ("credit not found"))
    }
  }

  def handleRemove (id: Long) = SecuredAction.async { implicit req =>
    creditDAO.remove(req.identity, id).flatMap { n =>
      if (n > 0)
        Future.successful(Redirect(routes.MesCreditsController.all()))
      else
        Future.successful(Redirect(routes.MesCreditsController.show(id)))
    }
  }
}
