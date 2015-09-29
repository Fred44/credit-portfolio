package forms.tools

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._


/**
  * Created by fred on 27/08/15.
  */
object AmortissementForm {

  import utils.FormFieldImplicits._

  val form = Form(
     mapping(
       "capital" -> bigDecimal.verifying(min(BigDecimal(0))),
       "taux" -> bigDecimal.verifying(min(BigDecimal(0)), max(BigDecimal(50))),
       "assurance" -> default (bigDecimal.verifying (min (BigDecimal(0)), max (BigDecimal(20))), BigDecimal(0)),
       "duree" -> number (min = 0),
       "typeDuree" -> nonEmptyText,
       "depart" -> jodaDate("dd/MM/yyyy")
     )(Data.apply)(Data.unapply)
   )


  case class Data (
                    capital: BigDecimal,
                    taux: BigDecimal,
                    assurance: BigDecimal,
                    duree: Int,
                    typeDuree: String = "A",
                    depart: DateTime = DateTime.now)
 }
