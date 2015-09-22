package forms

import models.MensualiteOperation
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

/**
 * Created by fred on 01/09/15.
 */
object MensualiteOperationForm {

  val form: Form[MensualiteOperation] = Form (
    mapping (
      "date" -> jodaDate("MM/yyyy"),
      "frais" -> bigDecimal.verifying(min(BigDecimal(0))),
      "mensualite" -> bigDecimal.verifying(min(BigDecimal(0)))
    ) (MensualiteOperation.apply) (MensualiteOperation.unapply)
  )
}
