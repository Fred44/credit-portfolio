package models

import org.joda.time.DateTime

/**
 * Created by fred on 15/03/15.
 */
case class CreditStats (taux: BigDecimal,
                        dateEcheance: DateTime,
                        mensualiteEcheance: BigDecimal,
                        amortiEcheance: BigDecimal,
                        interetsEchance: BigDecimal,
                        capitalEmprunte: BigDecimal,
                        capitalAmorti: BigDecimal,
                        capitalRestant: BigDecimal,
                        depart: DateTime,
                        fin: DateTime,
                        dureeInitiale: Int,
                        dureeEffective: Int,
                        dureeRestante: Int,
                        cout: BigDecimal,
                        interetsVerses: BigDecimal)