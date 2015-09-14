package models

import org.joda.time.DateTime

/**
 * A token used for reset password and sign up operations
 *
 * @param uuid the token id
 * @param email the user email
 * @param creationTime the creation time
 * @param expirationTime the expiration time
 * @param isSignUp a boolean indicating wether the token was created for a sign up action or not
 */
case class MailToken(uuid: String, email: String, creationTime: DateTime, expirationTime: DateTime, isSignUp: Boolean) {
  def isExpired = expirationTime.isBeforeNow
}