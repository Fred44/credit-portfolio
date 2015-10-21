package utils

import java.text.NumberFormat
import java.util.Locale

import play.api.data.{FormError, Forms}
import play.api.data.format.{Formats, Formatter}

/**
 * Created by fred on 29/09/15.
 */
object FormFieldImplicits {

  private val fmt = NumberFormat.getNumberInstance(Locale.FRANCE)

  implicit def bigDecimalFmt = new Formatter[BigDecimal] {
    def bind(key: String, data: Map[String, String]) = {
      Formats.stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[BigDecimal]
          .either {
            val bd = BigDecimal(fmt.parse(s.replace('.', ',')).doubleValue())
            if (bd.scale > 2) {
              throw new java.lang.ArithmeticException("Invalid precision")
            }
            bd
          }
          .left.map { e =>
            Seq(
              e match {
                case _:java.lang.ArithmeticException => FormError(key, "error.real.precision", Seq(2))
                case _ => FormError(key, "error.real", Nil)
              }
            )
          }
      }
    }
    def unbind(key: String, value: BigDecimal) = Map(key -> fmt.format(value))
  }

  val bigDecimal = Forms.of[BigDecimal]
}
