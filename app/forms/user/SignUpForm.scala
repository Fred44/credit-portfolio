package forms.user

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.Messages

/**
 * The form which handles the sign up process.
 */
object SignUpForm {

  val form = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "password1" -> nonEmptyText.verifying(minLength(4)),
      "password2" -> nonEmptyText.verifying(minLength(4))
    )(Data.apply)(Data.unapply)
      .verifying("signUp.passwordsDoNotMatch", f => f.password1 == f.password2)
  )

  /**
   * The form data.
   *
   * @param firstName The first name of a user.
   * @param lastName The last name of a user.
   * @param password1 The password of the user.
   * @param password2 The password confirmation of the user.
   */
  case class Data(
                   firstName: String,
                   lastName: String,
                   password1: String,
                   password2: String)
}
