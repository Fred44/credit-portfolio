package views.helper

import views.html.helper._

/**
 * Created by fred on 07/09/15.
 */
object Semantic {

  val checkbox = new FieldConstructor {
    def apply(elts: FieldElements) = suiCheckbox(elts)
  }

  implicit val input = new FieldConstructor {
    def apply(elts: FieldElements) = suiInput(elts)
  }
}
