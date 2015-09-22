package forms

import models.TauxOperation
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

/**
 * Created by fred on 02/09/15.
 */
object TauxOperationForm {

  val form: Form[TauxOperation] = Form (
    mapping (
      "date" -> jodaDate("MM/yyyy"),
      "frais" -> bigDecimal.verifying(min(BigDecimal(0))),
      "taux" -> bigDecimal.verifying(min(BigDecimal(0)), max(BigDecimal(50)))
    ) (TauxOperation.apply) (TauxOperation.unapply)
  )
}
