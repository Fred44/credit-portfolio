package models

import com.github.nscala_time.time.Imports._

/**
 * Created by fred on 02/02/15.
 */
object CalcAmo extends App {

  var credit = Credit (None, -1, "", 212931, 3.95, 300, "2007-08-05".toDateTime)

  println (credit)
  //credit.printTableau

  credit = credit//.setMensualite(DateTime.parse("2011-03-05"), 1250)
//                 .rembourser(DateTime.parse("2013-01-05"), 1250)
//                 .setTaux(DateTime.parse("2013-04-05"), 3.3)
//                 .setMensualite(DateTime.parse("2013-04-05"), 1390.61)
//                 .rembourser(DateTime.parse("2014-12-05"), 1390.61)
//                 .setMensualite(DateTime.parse("2015-02-05"), 1600)
                 //.rembourser(DateTime.parse("2015-04-05"), 1600)

  val amo = Amortissement(credit)
  amo.echeances foreach println

  println
  println ("Echeance actuel: " + amo.echeance(DateTime.now))
  println
  println (credit)

}
