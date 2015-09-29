package forms.tools

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._


/**
 * Created by fred on 27/08/15.
 */
object MensualiteForm {

  import utils.FormFieldImplicits._

  val form = Form(
    mapping(
      "capital" -> bigDecimal.verifying(min(BigDecimal(0))),
      "taux" -> bigDecimal.verifying(min(BigDecimal(0)), max(BigDecimal(50))),
      "duree" -> number (min = 0),
      "typeDuree" -> nonEmptyText,
      "assurance" -> default(bigDecimal.verifying(min(BigDecimal(0)), max(BigDecimal(20))), BigDecimal(0))
    )(Data.apply)(Data.unapply)
  )

  case class Data (
                    capital: BigDecimal,
                    taux: BigDecimal,
                    duree: Int,
                    typeDuree: String = "A",
                    assurance: BigDecimal)
}
