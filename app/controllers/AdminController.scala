package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette, Environment, Authorization}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.daos.{UserDAO, DAOSlick}
import models.{User}
import play.api.i18n.{MessagesApi, Messages}
import play.api.mvc.{Request, RequestHeader}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Created by fred on 07/04/15.
 */
class IsAdmin extends Authorization[User, CookieAuthenticator] {

  override def isAuthorized[B](user: User, authenticator: CookieAuthenticator)(
    implicit request: Request[B], messages: Messages) = {

    Future.successful(user.isAdmin)
  }
}

class AdminController @Inject() (
                                  val messagesApi: MessagesApi,
                                  val env: Environment[User, CookieAuthenticator],
                                  val userDAO: UserDAO)
  extends Silhouette[User, CookieAuthenticator] with AdminAction {

//  override protected def notAuthorizedPage()(implicit req: RequestHeader): Html = views.html.notAuthorized()

  def users = SecuredAction(new IsAdmin).async { implicit req =>
    listUsers.flatMap { r =>
      Future.successful(Ok(views.html.admin.users (req.identity)(r)))
    }
  }
}

trait AdminAction extends DAOSlick {

  import driver.api._

  def listUsers: Future[Seq[(User, Int)]] = {
    val q1 = for {
      u <- slickUsers
    } yield (u, slickCredits.filter(_.userID === u.id).length)

    db.run(q1.result).map { xs =>
      xs.map { case (u, cnt) =>
        (User(
          u.userID,
          LoginInfo("", ""),
          u.firstName,
          u.lastName,
          u.fullName,
          u.email,
          u.avatarURL,
          u.isAdmin,
          u.creationDate
        ), cnt)
      }
    }
  }

}