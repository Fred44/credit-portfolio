package models

import org.joda.time.DateTime

/**
 * Created by fred on 08/04/15.
 */

case class Echeance (
                      num: Int,
                      date: DateTime,
                      mensualite: BigDecimal,
                      interets: BigDecimal,
                      amortissement: BigDecimal,
                      capital: BigDecimal,
                      taux: BigDecimal,
                      assurance: BigDecimal) {

  override def toString = f"[ $num%3d \t ${date.toDate}%tm-${date.toDate}%tY \t $mensualite%,6.2f \t $interets%,6.2f \t $amortissement%,10.2f \t $capital%,10.2f \t $taux%2.2f ]"

}
