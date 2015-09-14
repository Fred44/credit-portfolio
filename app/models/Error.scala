package models

import play.api.i18n.Messages

/**
 * Created by fred on 23/03/15.
 */
class Error (code: String, args: String *) extends Exception (code) {

//  override def getLocalizedMessage = Messages (code, args: _*)
}

//class IllegalOperationError (code: String, args: String*) extends Error (code, args)
