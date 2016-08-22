/**
  * MIT License
  *
  * Copyright (c) 2016 James Sherwood-Jones <james.sherwoodjones@gmail.com>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */

package com.jsherz.luskydive.dao

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.{Calendar, UUID}

import akka.event.LoggingAdapter
import com.jsherz.luskydive.core.{ApiKey, CommitteeMember}
import com.jsherz.luskydive.services.DatabaseService
import com.jsherz.luskydive.util.FutureError._
import com.jsherz.luskydive.util.EitherFutureExtensions._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{-\/, \/, \/-}


trait AuthDao {

  /**
    * Authenticate a user, either returning the Left(error) or Right(memberUuid).
    *
    * @param apiKey
    * @param time Time at which access is required
    * @return
    */
  def authenticate(apiKey: UUID, time: Timestamp): Future[String \/ UUID]

  /**
    * Get the API key with the given UUID / key.
    *
    * @param apiKey
    * @return
    */
  def get(apiKey: UUID): Future[Option[ApiKey]]

}

class AuthDaoImpl(protected override val databaseService: DatabaseService)
                 (implicit val ec: ExecutionContext, implicit val log: LoggingAdapter)
  extends Tables(databaseService) with AuthDao {

  import driver.api._

  /**
    * Number of hours an API key lasts for after being created or reissued.
    */
  val API_KEY_EXPIRES = 24

  /**
    * Authenticate a user, either returning the Left(error) or Right(memberUuid).
    *
    * @param apiKey
    * @param time Time at which access is required
    * @return
    */
  override def authenticate(apiKey: UUID, time: Timestamp): Future[String \/ UUID] = {
    val lookup = db.run(
      ApiKeys.filter(_.uuid === apiKey).join(CommitteeMembers).on(_.committeeMemberUuid === _.uuid).result.headOption
    )

    for {
      lookupResult <- lookup ifNone AuthDaoErrors.invalidApiKey
      validateResult <- Future(lookupResult.flatMap(validateKey(time)))
      authResult <- validateResult withFutureF extendKeyExpiry(time)
    } yield authResult
  }

  /**
    * Test to see if the given API key is valid at the provided time.
    *
    * Checks that the key hasn't expired and the attached committee member hasn't been locked.
    *
    * @param time
    * @param keyAndCommittee
    * @return
    */
  private def validateKey(time: Timestamp)(keyAndCommittee: (ApiKey, CommitteeMember)): String \/ ApiKey = {
    keyAndCommittee match {
      case (apiKey, committeeMember) => {
        // Ensure key hasn't expired
        if (apiKey.expiresAt.after(time)) {
          // Ensure committee member is allowed to login
          if (!committeeMember.locked) {
            \/-(apiKey)
          } else {
            -\/(AuthDaoErrors.accountLocked)
          }
        } else {
          -\/(AuthDaoErrors.invalidApiKey)
        }
      }
    }
  }

  /**
    * Extend the given key's expiry date by the current time + X hours.
    *
    * @param time
    * @param apiKey
    * @return
    */
  private def extendKeyExpiry(time: Timestamp)(apiKey: ApiKey): Future[String \/ UUID] = {
    val calendar = Calendar.getInstance()
    calendar.setTime(time)
    calendar.add(Calendar.HOUR, API_KEY_EXPIRES)

    val newExpiry = new Timestamp(calendar.getTime.getTime)

    db.run(
      ApiKeys.filter(_.uuid === apiKey.uuid).map(_.expiresAt).update(newExpiry)
    ).map {
      _ => apiKey.committeeMemberUuid
    } withServerError
  }

  /**
    * Get the API key with the given UUID / key.
    *
    * @param apiKey
    * @return
    */
  override def get(apiKey: UUID): Future[Option[ApiKey]] = {
    db.run(ApiKeys.filter(_.uuid === apiKey).result.headOption)
  }

}

object AuthDaoErrors {

  val invalidApiKey = "error.invalidApiKey"

  val accountLocked = "error.accountLocked"

  val internalService = "error.internalService"

}
