package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.{Assurance, Credit}
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

/**
 * Tables definitions
 */
trait DBTableDefinitions {

  protected val driver: JdbcProfile
  import driver.api._
  import com.github.tototoshi.slick.JdbcJodaSupport._

  case class DBUser (
                      userID: Option[Long],
                      firstName: Option[String],
                      lastName: Option[String],
                      fullName: Option[String],
                      email: Option[String],
                      avatarURL: Option[String],
                      isAdmin: Boolean,
                      creationDate: DateTime
  )

  class Users(tag: Tag) extends Table[DBUser](tag, "USERS_BIS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def firstName = column[Option[String]]("FIRSTNAME")
    def lastName = column[Option[String]]("LASTNAME")
    def fullName = column[Option[String]]("FULLNAME")
    def email = column[Option[String]]("EMAIL")
    def avatarURL = column[Option[String]]("AVATAR_URL")
    def isAdmin     = column[Boolean]("IS_ADMIN")
    def creationDate = column[DateTime]("CREATION_DATE")
    def * = (id.?, firstName, lastName, fullName, email, avatarURL, isAdmin, creationDate) <> (DBUser.tupled, DBUser.unapply)
  }

  case class DBLoginInfo (
                           id: Option[Long],
                           providerID: String,
                           providerKey: String)

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "LOGIN_INFO") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def providerID = column[String]("PROVIDER_ID")
    def providerKey = column[String]("PROVIDER_KEY")
    def * = (id.?, providerID, providerKey) <> (DBLoginInfo.tupled, DBLoginInfo.unapply)
  }

  case class DBUserLoginInfo (
                           userID: Long,
                           loginInfoId: Long)

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "USER_LOGIN_INFO") {
    def userID = column[Long]("USER_ID")
    def loginInfoId = column[Long]("LOGIN_INFO_ID")
    def * = (userID, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  case class DBPasswordInfo (
                              hasher: String,
                              password: String,
                              salt: Option[String],
                              loginInfoId: Long)

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "PASSWORD_INFO") {
    def hasher = column[String]("HASHER")
    def password = column[String]("PASSWORD")
    def salt = column[Option[String]]("SALT")
    def loginInfoId = column[Long]("LOGIN_INFO_ID")
    def * = (hasher, password, salt, loginInfoId) <> (DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }

  case class DBOAuth1Info (
                        id: Option[Long],
                        token: String,
                        secret: String,
                        loginInfoId: Long)

  class OAuth1Infos(tag: Tag) extends Table[DBOAuth1Info](tag, "OAUTH1_INFO") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def token = column[String]("TOKEN")
    def secret = column[String]("SECRET")
    def loginInfoId = column[Long]("LOGIN_INFO_ID")
    def * = (id.?, token, secret, loginInfoId) <> (DBOAuth1Info.tupled, DBOAuth1Info.unapply)
  }

  case class DBOAuth2Info (
                            id: Option[Long],
                            accessToken: String,
                            tokenType: Option[String],
                            expiresIn: Option[Int],
                            refreshToken: Option[String],
                            loginInfoId: Long)

  class OAuth2Infos(tag: Tag) extends Table[DBOAuth2Info](tag, "OAUTH2_INFO") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def accessToken = column[String]("ACCESS_TOKEN")
    def tokenType = column[Option[String]]("TOKEN_TYPE")
    def expiresIn = column[Option[Int]]("EXPIRES_IN")
    def refreshToken = column[Option[String]]("REFRESH_TOKEN")
    def loginInfoId = column[Long]("LOGIN_INFO_ID")
    def * = (id.?, accessToken, tokenType, expiresIn, refreshToken, loginInfoId) <> (DBOAuth2Info.tupled, DBOAuth2Info.unapply)
  }

  case class DBOpenIDInfo (
                            id: String,
                            loginInfoId: Long)

  class OpenIDInfos(tag: Tag) extends Table[DBOpenIDInfo](tag, "OPENID_INFO") {
    def id = column[String]("ID", O.PrimaryKey)
    def loginInfoId = column[Long]("LOGIN_INFO_ID")
    def * = (id, loginInfoId) <> (DBOpenIDInfo.tupled, DBOpenIDInfo.unapply)
  }

  case class DBOpenIDAttribute (
                                 id: String,
                                 key: String,
                                 value: String)

  class OpenIDAttributes(tag: Tag) extends Table[DBOpenIDAttribute](tag, "OPENID_ATTRIBUTES") {
    def id = column[String]("ID")
    def key = column[String]("KEY")
    def value = column[String]("VALUE")
    def * = (id, key, value) <> (DBOpenIDAttribute.tupled, DBOpenIDAttribute.unapply)
  }

  class Credits (tag: Tag) extends Table[Credit](tag, "CREDITS_2") {

    def id         = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def userID     = column[Long]("USER_ID")
    def nom        = column[String]("NOM")

    def capital    = column[BigDecimal]("CAPITAL")
    def taux       = column[BigDecimal]("TAUX")
    def duree      = column[Int]("DUREE")
    def depart     = column[DateTime]("DEPART")

    def tauxAssurance = column[BigDecimal]("TAUX_ASS")
    def baseAssurance = column[String]("BASE_ASS")

    def creation   = column[DateTime]("CREATION")

    def user       = foreignKey ("USER_FK", userID, slickUsers)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    override def * = (id.?, userID, nom, capital, taux, duree, depart, tauxAssurance, baseAssurance, creation).shaped <> (
      { t =>
        Credit (
          id = t._1,
          userId = t._2,
          nom = t._3,
          capital = t._4,
          taux = t._5,
          duree = t._6,
          depart = t._7,
          assurance = Assurance(t._8, t._9),
          creation = t._10
        )
      }, { (c: Credit) =>
      Some {(
        c.id,
        c.userId,
        c.nom,
        c.capital,
        c.taux,
        c.duree,
        c.depart,
        c.assurance.taux,
        c.assurance.base,
        c.creation
        )}
    })
  }

  case class DBOperation (
                           creditID: Long,
                           typ: String,
                           date: DateTime,
                           frais: BigDecimal,
                           valeur: BigDecimal,
                           creation: DateTime)

  class Operations (tag: Tag) extends Table[DBOperation](tag, "OPERATIONS_2") {

    def creditID  = column[Long]("CREDIT_ID")
    def typ       = column[String]("TYP")
    def date      = column[DateTime]("DATE")
    def frais     = column[BigDecimal]("FRAIS")
    def valeur    = column[BigDecimal]("VALEUR")

    def creation   = column[DateTime]("CREATION")

    def credit = foreignKey("CREDIT_FK", creditID, slickCredits)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    override def * = (creditID, typ, date, frais, valeur, creation) <> (DBOperation.tupled, DBOperation.unapply)
  }

  // table query definitions
  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickUserLoginInfos = TableQuery[UserLoginInfos]
  val slickPasswordInfos = TableQuery[PasswordInfos]
  val slickOAuth1Infos = TableQuery[OAuth1Infos]
  val slickOAuth2Infos = TableQuery[OAuth2Infos]
  val slickOpenIDInfos = TableQuery[OpenIDInfos]
  val slickOpenIDAttributes = TableQuery[OpenIDAttributes]
  val slickCredits = TableQuery[Credits]
  val slickOperations= TableQuery[Operations]

  // queries used in multiple places
  def loginInfoQuery(loginInfo: LoginInfo) =
    slickLoginInfos.filter(dbLoginInfo => dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)
}
