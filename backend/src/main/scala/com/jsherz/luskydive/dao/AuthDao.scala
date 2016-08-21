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

import java.util.UUID

import akka.event.LoggingAdapter
import com.jsherz.luskydive.core.ApiKey
import com.jsherz.luskydive.services.DatabaseService
import com.jsherz.luskydive.util.FutureError._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{-\/, \/}


trait AuthDao {

  /**
    * Authenticate a user, either returning the Left(error) or Right(memberUuid).
    *
    * @param apiKey
    * @return
    */
  def authenticate(apiKey: UUID): Future[String \/ UUID]

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
    * Authenticate a user, either returning the Left(error) or Right(memberUuid).
    *
    * @param apiKey
    * @return
    */
  override def authenticate(apiKey: UUID): Future[String \/ UUID] = {
    get(apiKey).map {
      case Some(key) => {
        if (key.expiresAt < Timestamp.valueOf())
      }
      case None => -\/(AuthDaoErrors.invalidApiKey)
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
