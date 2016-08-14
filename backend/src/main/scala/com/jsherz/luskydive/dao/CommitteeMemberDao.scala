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

import com.jsherz.luskydive.json.StrippedCommitteeMember
import com.jsherz.luskydive.services.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

/**
  * Used to store and retrieve information about committee members.
  */
trait CommitteeMemberDao {

  /**
    * Get active committee members, sorted by name.
    *
    * @return
    */
  def active(): Future[Seq[StrippedCommitteeMember]]

}

/**
  * Database backed committee member DAO.
  */
class CommitteeMemberDaoImpl(protected override val databaseService: DatabaseService)(implicit val ec: ExecutionContext)
  extends Tables(databaseService) with CommitteeMemberDao {

  import driver.api._

  /**
    * Get active committee members, sorted by name.
    *
    * @return
    */
  override def active(): Future[Seq[StrippedCommitteeMember]] = {
    val lookup = for {
      committee <- CommitteeMembers.filter(_.locked === false).sortBy(_.name)
    } yield (committee.uuid, committee.name)

    db.run(lookup.result).map(_.map(StrippedCommitteeMember.tupled(_)))
  }

}
