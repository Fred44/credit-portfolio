package forms.tools

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._


/**
  * Created by fred on 27/08/15.
  */
object CapitalForm {

  import utils.FormFieldImplicits._

  val form = Form(
     mapping(
       "apport" -> optional(bigDecimal.verifying(min(BigDecimal(0)))),
       "mensualite" -> bigDecimal.verifying(min(BigDecimal(0))),
       "taux" -> bigDecimal.verifying(min(BigDecimal(0)), max(BigDecimal(50))),
       "assurance" -> default (bigDecimal.verifying (min (BigDecimal(0)), max (BigDecimal(20))), BigDecimal(0)),
       "duree" -> number (min = 0),
       "typeDuree" -> nonEmptyText
     )(Data.apply)(Data.unapply)
   )

  case class Data (
                    apport: Option[BigDecimal],
                    mensualite: BigDecimal,
                    taux: BigDecimal,
                    assurance: BigDecimal,
                    duree: Int,
                    typeDuree: String = "A")
 }
