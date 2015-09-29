package utils

import java.text.NumberFormat
import java.util.Locale

import play.api.data.Forms
import play.api.data.format.Formatter

/**
 * Created by fred on 29/09/15.
 */
object FormFieldImplicits {

  private val fmt = NumberFormat.getNumberInstance(Locale.FRANCE)

  implicit def bigDecimalFmt = new Formatter[BigDecimal] {
    def bind(key: String, data: Map[String, String]) = Right(BigDecimal(fmt.parse(data(key).replace('.', ',')).doubleValue()))
    def unbind(key: String, value: BigDecimal) = Map(key -> fmt.format(value))
  }

  val bigDecimal = Forms.of[BigDecimal]
}
