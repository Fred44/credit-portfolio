package forms

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

/**
 * Created by fred on 28/08/15.
 */
object CreditForm {

  val DUREE_TYPE_AN = "A"
  val DUREE_TYPE_MOIS = "M"

  val form = Form (
    mapping (
      "nom" -> nonEmptyText,
      "capital" -> bigDecimal.verifying(min(BigDecimal(0))),
      "taux" -> bigDecimal.verifying(min(BigDecimal(0)), max(BigDecimal(50))),
      "assurance" -> default(bigDecimal.verifying(min(BigDecimal(0)), max(BigDecimal(20))), BigDecimal(0)),
      "duree" -> number (min = 0),
      "typeDuree" -> nonEmptyText,
      "depart" -> jodaDate("dd.MM.yyyy")
    ) (Data.apply) (Data.unapply) verifying ("La durée max est de 30 ans", fields => fields match {
      case c => validateDuree(c).isDefined
    })
  )

  private def validateDuree (c: Data) =
    c.typeDuree match {
      case DUREE_TYPE_AN if c.duree <= 30 =>
        Some (c)
      case DUREE_TYPE_MOIS if c.duree <= 30*12 =>
        Some (c)
      case _ =>
        None
    }

  case class Data (
                    nom: String,
                    capital: BigDecimal,
                    taux: BigDecimal,
                    assurance: BigDecimal,
                    duree: Int,
                    typeDuree: String = DUREE_TYPE_AN,
                    depart: DateTime = DateTime.now
                    )
}
