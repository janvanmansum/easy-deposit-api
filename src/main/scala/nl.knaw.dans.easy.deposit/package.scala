/**
 * Copyright (C) 2018 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy

import java.nio.file.Path
import java.util.UUID

import nl.knaw.dans.easy.deposit.State.State
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.joda.time.DateTime
import org.scalatra.{ ActionResult, BadRequest, InternalServerError }

package object deposit extends DebugEnhancedLogging {

  sealed abstract class DepositException(msg: String, cause: Throwable) extends Exception(msg, cause)

  case class NoSuchDepositException(user: String, id: UUID, cause: Throwable = null)
    extends DepositException(s"Deposit with id $id not found for user $user", cause)

  case class IllegalStateTransitionException(user: String, id: UUID, oldState: State, newState: State)
    extends DepositException(s"Cannot transition from $oldState to $newState (deposit id: $id, user: $user)", null)

  case class ConfigurationException(msg: String) extends IllegalArgumentException(s"Configuration error: $msg")

  object State extends Enumeration {
    type State = Value
    val DRAFT, SUBMITTED, IN_PROGRESS, REJECTED, ARCHIVED = Value
  }

  import State._

  /**
   * Summary information about a deposit.
   */
  case class DepositInfo(id: UUID, title: String, state: State, stateDescription: String, timestamp: DateTime)

  case class StateInfo(state: State, stateDescription: String)

  /**
   * Information about a file in the deposit
   *
   * @param fileName the simple filename of the file
   * @param dirPath  path of the containing directory, relative to the content base directory
   * @param sha1sum  the SHA-1 checksum of the file data
   */
  case class FileInfo(fileName: String, dirPath: Path, sha1sum: String)


  def internalErrorResponse(t: Throwable): ActionResult = {
    logger.error(s"Not expected exception: ${ t.getMessage }", t)
    InternalServerError("Internal Server Error")
  }

  def badDocResponse(t: Throwable): ActionResult = {
    logger.error(s"Invalid ${ t.getMessage }:${ t.getCause.getClass.getName } ${ t.getCause.getMessage }")
    BadRequest(s"Bad Request. The ${ t.getMessage } document is malformed.")
  }
}
