package models

import org.joda.time.DateTime

/**
 * Created by fred on 14/03/15.
 */
trait Operation {
  val date: DateTime
  val frais: BigDecimal
}

case class MensualiteOperation    (date: DateTime, frais: BigDecimal = 0, mensualite: BigDecimal) extends Operation
case class TauxOperation          (date: DateTime, frais: BigDecimal = 0, taux: BigDecimal) extends Operation
case class RemboursementOperation (date: DateTime, frais: BigDecimal = 0, montant: BigDecimal) extends Operation
