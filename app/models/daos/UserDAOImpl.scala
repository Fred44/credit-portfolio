package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
 * Created by fred on 14/08/15.
 */
class UserDAOImpl extends UserDAO with DAOSlick {

  import driver.api._

  override def all: Future[Seq[User]] = {
    val qry = for {
      dbUser <- slickUsers
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbUser, dbLoginInfo)
    db.run(qry.result).map { xs =>
      xs.map {
        case (user: DBUser, loginInfo: DBLoginInfo) =>
          User(
            user.userID,
            LoginInfo(loginInfo.providerID, loginInfo.providerKey),
            user.firstName,
            user.lastName,
            user.fullName,
            user.email,
            user.avatarURL,
            user.isAdmin,
            user.creationDate)
      }
    }
  }

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  override def find(loginInfo: LoginInfo): Future[Option[User]] = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(_.id === dbUserLoginInfo.userID)
    } yield dbUser
    db.run(userQuery.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        User(
          user.userID,
          loginInfo,
          user.firstName,
          user.lastName,
          user.fullName,
          user.email,
          user.avatarURL,
          user.isAdmin,
          user.creationDate)
      }
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  override def save(user: User): Future[User] = {
    val dbUser = DBUser(user.serID, user.firstName, user.lastName, user.fullName, user.email, user.avatarURL, user.isAdmin, user.creationDate)
    val dbLoginInfo = DBLoginInfo(None, user.loginInfo.providerID, user.loginInfo.providerKey)
    // We don't have the LoginInfo id so we try to get it first.
    // If there is no LoginInfo yet for this user we retrieve the id on insertion.
    val loginInfoAction = {
      val retrieveLoginInfo = slickLoginInfos.filter(
        info => info.providerID === user.loginInfo.providerID &&
          info.providerKey === user.loginInfo.providerKey).result.headOption
      val insertLoginInfo = slickLoginInfos.returning(slickLoginInfos.map(_.id)).
        into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
      for {
        loginInfoOption <- retrieveLoginInfo
        loginInfo <- loginInfoOption.map(DBIO.successful(_)).getOrElse(insertLoginInfo)
      } yield loginInfo
    }

    val actions = (for {
      u <- dbUser.userID match {
        case Some(id) =>
          DBIO.successful(dbUser)
        case None =>
          (slickUsers returning slickUsers.map(_.id)).
            into ((user, id) => user.copy(userID = Some(id))) += dbUser
      }
      loginInfo <- loginInfoAction
      _ <- slickUserLoginInfos += DBUserLoginInfo(u.userID.get, loginInfo.id.get)
    } yield ()).transactionally

    db.run(actions).map(_ => user)
  }

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  override def find(userID: Long): Future[Option[User]] = {
    val query = for {
      dbUser <- slickUsers.filter(_.id === userID)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbUser, dbLoginInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (user, loginInfo) =>
          User(
            user.userID,
            LoginInfo(loginInfo.providerID, loginInfo.providerKey),
            user.firstName,
            user.lastName,
            user.fullName,
            user.email,
            user.avatarURL,
            user.isAdmin,
            user.creationDate)
      }
    }
  }

  /**
   * Find a user by its email.
   *
   * @param email
   * @return the found user or none.
   */
  override def findByEmail(email: String): Future[Option[User]] = {
    val query = for {
      dbUser <- slickUsers.filter(_.email === email)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbUser, dbLoginInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (user, loginInfo) =>
          User(
            user.userID,
            LoginInfo(loginInfo.providerID, loginInfo.providerKey),
            user.firstName,
            user.lastName,
            user.fullName,
            user.email,
            user.avatarURL,
            user.isAdmin,
            user.creationDate)
      }
    }
  }
}
