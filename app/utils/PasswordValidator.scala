package utils

import play.api.data.validation.{Valid, Invalid, Constraint}

/**
 * Created by fred on 16/08/15.
 */
trait PasswordValidator {

  /**
   * Validates a password
   *
   * @param password the supplied password
   * @return Right if the password is valid or Left with an error message otherwise
   */
  def validate(password: String): Either[(String, Seq[Any]), Unit]
}
