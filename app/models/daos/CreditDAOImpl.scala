package models.daos

import models._
import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
 * Created by fred on 31/08/15.
 */
class CreditDAOImpl extends CreditDAO with DAOSlick {

  import driver.api._

  /**
   * Find user's credits
   *
   * @param user
   * @return
   */
  override def find(user: User): Future[Seq[(Credit, Seq[Operation])]] = {
    val qry = for {
      (c, o) <- slickCredits.filter(_.userID === user.serID) joinLeft slickOperations on (_.id === _.creditID)
    } yield (c, o)

    db.run(qry.result).map { xs =>
      xs.groupBy(_._1).map { case (c, ops) =>
        (c, ops.collect {
          case (_, Some(DBOperation (_, "MENS", d, f, v, _))) =>
            MensualiteOperation (d, f, v)
          case (_, Some(DBOperation (_, "REMB", d, f, v, _))) =>
            RemboursementOperation (d, f, v)
          case (_, Some(DBOperation (_, "TAUX", d, f, v, _))) =>
            TauxOperation (d, f, v)
        })
      }.toSeq
    }
  }

  /**
   * Return a credit count for a given user
   *
   * @param user
   * @return
   */
  override def count(user: User): Future[Int] = {
    val qry = slickCredits.filter(_.userID === user.serID).length
    db.run(qry.result)
  }

  /**
   * Get a credit by its ID
   *
   * @param creditID
   * @return
   */
  override def find(user: User, creditID: Long): Future[Option[(Credit, Seq[Operation])]] = {
    val qry = for {
      (c, o) <- slickCredits.filter(r => r.userID === user.serID && r.id === creditID) joinLeft slickOperations on (_.id === _.creditID)
    } yield (c, o)

    db.run(qry.result).map { xs =>
      xs.groupBy(_._1).map { case (c, ops) =>
        (c, ops.collect {
          case (_, Some(DBOperation(_, "MENS", d, f, v, _))) =>
            MensualiteOperation(d, f, v)
          case (_, Some(DBOperation(_, "REMB", d, f, v, _))) =>
            RemboursementOperation(d, f, v)
          case (_, Some(DBOperation(_, "TAUX", d, f, v, _))) =>
            TauxOperation(d, f, v)
        })
      }.headOption
    }
  }

  /**
   * Remove a credit from the DB
   *
   * @param user
   * @param creditID
   * @return
   */
  override def remove(user: User, creditID: Long): Future[Int] = {
    val actions = (for {
      cnt <- slickCredits.filter( c => c.id === creditID && c.userID === user.serID).delete
      _ <- slickOperations.filter(_.creditID === creditID).delete
    } yield (cnt)).transactionally
    db.run(actions)
  }

  /**
   * Save a credit
   *
   * @param credit
   * @return
   */
  override def save(credit: Credit): Future[Credit] = {
    credit.id match {
      case Some(id) =>
        val qry = for {
          c <- slickCredits.filter(_.id === credit.id)
        } yield (c.nom, c.capital, c.taux, c.tauxAssurance, c.baseAssurance, c.duree, c.depart)
        db.run(
          qry.update(
            credit.nom,
            credit.capital,
            credit.taux,
            credit.assurance.taux, credit.assurance.base,
            credit.duree,
            credit.depart
          )
        ).map(_ => credit)
      case None =>
        db.run(
          (slickCredits returning slickCredits.map(_.id)).
            into ((c, id) => c.copy(id = Some(id))) += credit
        )
    }
  }

  /**
   * Insert an operation to a credit.
   *
   * @param creditID
   * @param operation
   */
  override def insertOperation(creditID: Long, operation: Operation): Unit = {
    val qry = operation match {
      case MensualiteOperation(d, f, v) =>
        slickOperations += DBOperation(creditID, "MENS", d, f, v, DateTime.now)
      case RemboursementOperation(d, f, v) =>
        slickOperations += DBOperation(creditID, "REMB", d, f, v, DateTime.now)
      case TauxOperation(d, f, v) =>
        slickOperations += DBOperation(creditID, "TAUX", d, f, v, DateTime.now)
    }
    db.run(DBIO.seq(qry))
  }

  /**
   * Remove an operation from a given credit
   *
   * @param creditID
   * @param date
   * @param typ
   * @return
   */
  override def removeOperation(creditID: Long, date: DateTime, typ: String): Future[Int] = {
    val qry = slickOperations.filter(o => o.creditID === creditID && o.date === date && o.typ === typ)
    db.run(qry.delete)
  }

}
