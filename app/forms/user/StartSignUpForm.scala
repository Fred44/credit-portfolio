package forms.user

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

/**
 * Created by fred on 17/08/15.
 */
object StartSignUpForm {

  val form = Form(
    "email" -> email.verifying(nonEmpty)
  )
}
