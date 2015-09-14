package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import org.joda.time.DateTime

/**
 * The user object.
 *
 * @param serID The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param firstName Maybe the first name of the authenticated user.
 * @param lastName Maybe the last name of the authenticated user.
 * @param fullName Maybe the full name of the authenticated user.
 * @param email Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 * @param creationDate Maybe the of the registration.
 */
case class User (serID: Option[Long] = None,
                    loginInfo: LoginInfo,
                    firstName: Option[String],
                    lastName: Option[String],
                    fullName: Option[String],
                    email: Option[String],
                    avatarURL: Option[String],
                    isAdmin: Boolean = false,
                    creationDate: DateTime = DateTime.now) extends Identity
