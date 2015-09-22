package forms

import models.RemboursementOperation
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

/**
 * Created by fred on 02/09/15.
 */
object RembourserOperationForm {

  val form: Form[RemboursementOperation] = Form (
    mapping (
      "date" -> jodaDate("MM/yyyy"),
      "frais" -> bigDecimal.verifying(min(BigDecimal(0))),
      "montant" -> bigDecimal.verifying(min(BigDecimal(0)))
    ) (RemboursementOperation.apply) (RemboursementOperation.unapply)
  )
}
