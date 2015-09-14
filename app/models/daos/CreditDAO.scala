package models.daos

import models.{Operation, Credit, User}
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Created by fred on 31/08/15.
 */
trait CreditDAO {

  /**
   * Find user's credits
   *
   * @param user
   * @return
   */
  def find(user: User): Future[Seq[(Credit, Seq[Operation])]]

  /**
   * Return a credit count for a given user
   *
   * @param user
   * @return
   */
  def count(user: User): Future[Int]

  /**
   * Get a credit by its ID
   *
   * @param creditID
   * @return
   */
  def find(user: User, creditID: Long): Future[Option[(Credit, Seq[Operation])]]


  /**
   * Save a credit
   *
   * @param credit
   * @return
   */
  def save(credit: Credit): Future[Credit]

  /**
   * Remove a credit from the DB
   *
   * @param user
   * @param creditID
   * @return
   */
  def remove(user: User, creditID: Long): Future[Int]

  /**
   * Insert an operation in a credit
   *
   * @param creditID
   * @param operation
   * @return
   */
  def insertOperation(creditID: Long, operation: Operation): Unit

  /**
   * Remove an operation from a given credit.
   *
   * @param creditID
   * @param date
   * @param typ
   * @return
   */
  def removeOperation(creditID: Long, date: DateTime, typ: String): Future[Int]

}
