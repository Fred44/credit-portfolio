package models

import org.joda.time.{DateTime}

import scala.math.BigDecimal.RoundingMode

/**
 * Created by fred on 03/02/15.
 */

case class Assurance (taux: BigDecimal, base: String = "CI")

case class Credit (
                    id: Option[Long] = None,
                    userId: Long,
                    nom: String,
                    capital: BigDecimal,
                    taux: BigDecimal,
                    duree: Int,
                    depart: DateTime,
                    assurance: Assurance = Assurance(0),
                    creation: DateTime = DateTime.now
                    ) {

  val mensualite = CreditCalculation.mensualite(capital, taux, duree, assurance.taux)

  override def toString = {
    f"""Capital : $capital%,16.2f €
       |Taux :    $taux%16.2f %%
       |Durée :   $duree%16d m
       |Commence :${depart.toString("dd-MM-yyyy")}%16s
     """.stripMargin
  }
}

object CreditCalculation {

  def capital (mensualite: BigDecimal, taux: BigDecimal, duree: Int, assurance: BigDecimal): BigDecimal = {
    val r = taux / 1200
    val m = mensualite
    val tA = assurance / 100

    if (r == 0) m * duree
    else (m - m * (1+r).pow(-duree)) * 12 / (12 * r + tA - (1+r).pow(-duree) * tA)
  }

  def mensualite (capital: BigDecimal, taux: BigDecimal, duree: Int, assurance: BigDecimal = 0): BigDecimal = {
    val r = taux / 1200
    val a = capital * assurance / 1200
    if (r == 0) (capital / duree + a).setScale(2, RoundingMode.CEILING)
    else (capital * r / (1 - (1 + r).pow(-duree)) + a).setScale(2, RoundingMode.CEILING)
  }
}