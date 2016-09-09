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

import java.sql.{Date, Timestamp}
import java.util.UUID

import com.jsherz.luskydive.core.MassText
import com.jsherz.luskydive.util.{DateUtil, Errors}

import scala.concurrent.Future
import scalaz.{-\/, \/, \/-}


class StubMassTextDao extends MassTextDao {

  /**
    * Get the mass text with the given UUID.
    *
    * @param uuid
    * @return
    */
  override def get(uuid: UUID): Future[String \/ Option[MassText]] = {
    if (StubMassTextDao.existsMassTextUuid.equals(uuid)) {
      Future.successful(\/-(
        Some(MassText(
          Some(UUID.fromString("b1266b65-40b2-4874-a551-854bf2e2ef26")),
          UUID.fromString("a8df22ad-c2a4-40b6-9939-852421d9b30e"),
          "Roll up, roll up {{ name }}, it's time to get yourself down to our skydiving G.I.A.G - meet you there :D " +
            "#excited #skydiving - Reply \"NOFUN\" to stop these messages.",
          Timestamp.valueOf("2012-09-25 00:00:00.000000")
        )))
      )
    } else if (StubMassTextDao.unknownMassTextUuid.equals(uuid)) {
      Future.successful(\/-(None))
    } else if (StubMassTextDao.serverErrorMassTextUuid.equals(uuid)) {
      Future.successful(-\/(Errors.internalServer))
    } else {
      throw new RuntimeException("unknown uuid used with stub")
    }
  }

  /**
    * Get the number of members that joined between the given dates.
    *
    * @param startDate Start date (inclusive)
    * @param endDate   End date (exclusive)
    * @return
    */
  override def filterCount(startDate: Date, endDate: Date): Future[\/[String, Int]] = {
    if (StubMassTextDao.validStartDate.equals(startDate) && StubMassTextDao.validEndDate.equals(endDate)) {
      Future.successful(\/-(StubMassTextDao.validFilterCount))
    } else if (StubMassTextDao.serverErrorStartDate.equals(startDate) && StubMassTextDao.serverErrorEndDate.equals(endDate)) {
      Future.successful(-\/(Errors.internalServer))
    } else {
      throw new RuntimeException("unknown dates used with stub")
    }
  }

}

object StubMassTextDao {

  val validStartDate = DateUtil.makeDate(2016, 8, 1)
  val validEndDate = DateUtil.makeDate(2016, 9, 1)
  val validFilterCount = 14811

  val serverErrorStartDate = DateUtil.makeDate(2015, 9, 1)
  val serverErrorEndDate = DateUtil.makeDate(2015, 10, 1)

}
