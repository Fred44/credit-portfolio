package forms.user

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.Messages

import scala.concurrent.Await

/**
 * Created by fred on 04/09/15.
 */
object PasswordChangeForm {


  /**
   *
   * @param currentPassword
   * @param newPassword
   */
  case class Data (currentPassword: String, newPassword: String)
}
