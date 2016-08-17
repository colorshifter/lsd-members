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

package com.jsherz.luskydive.apis

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.jsherz.luskydive.core.Course
import com.jsherz.luskydive.dao.StubCourseDao
import com.jsherz.luskydive.json.{CourseCreateRequest, CourseCreateResponse}
import com.jsherz.luskydive.util.{DateUtil, Util}
import org.mockito.Matchers.any
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.Mockito.{never, verify}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import com.jsherz.luskydive.json.CoursesJsonSupport._

/**
  * Ensures the create course endpoint function correctly.
  */
class CoursesCreateApiSpec extends WordSpec with Matchers with ScalatestRouteTest with BeforeAndAfter {

  private var dao = Mockito.spy(new StubCourseDao())

  private var route = new CoursesApi(dao).route

  private val url = "/courses/create"

  before {
    dao = Mockito.spy(new StubCourseDao())
    route = new CoursesApi(dao).route
  }

  "CoursesApi#create" should {

    "return method not allowed when used with anything other than POST" in {
      Seq(Get, Put, Delete, Patch).foreach { method =>
        method(url) ~> Route.seal(route) ~> check {
          response.status shouldEqual StatusCodes.MethodNotAllowed
        }
      }

      verify(dao, never()).create(any[Course], any[Int])
    }

    "return success = false and the provided error when creating the course fails" in {
      val request = CourseCreateRequest(DateUtil.makeDate(2009, 11, 15), UUID.fromString("009822da-02ce-4524-bd47-38fc093230d1"),
        None, 5000)

      Post(url, request) ~> route ~> check {
        response.status shouldEqual StatusCodes.OK
        responseAs[CourseCreateResponse].success shouldEqual false
        responseAs[CourseCreateResponse].error shouldBe Some("error.invalidNumSpaces")
      }
    }

    "return success = true and no error when creating the course succeeds" in {
      val request = CourseCreateRequest(DateUtil.makeDate(2016, 5, 1), UUID.fromString("a1c5b34d-c4ed-4eb5-b2ab-5f32bd10adfa"),
        Some(UUID.fromString("18f01666-3511-46de-80d0-18db3177aff8")), 5)

      Post(url, request) ~> route ~> check {
        response.status shouldEqual StatusCodes.OK
        responseAs[CourseCreateResponse].success shouldEqual true
        responseAs[CourseCreateResponse].error shouldBe None
      }

      val courseCaptor = ArgumentCaptor.forClass[Course](Course.getClass.asInstanceOf[Class[Course]])

      verify(dao).create(courseCaptor.capture(), org.mockito.Matchers.eq(request.numSpaces))

      val courseSentToDao = courseCaptor.getValue
      courseSentToDao.uuid shouldNot be(None)
      courseSentToDao.date shouldEqual request.date
      courseSentToDao.organiserUuid shouldEqual request.organiserUuid
      courseSentToDao.secondaryOrganiserUuid shouldEqual request.secondaryOrganiserUuid
    }

  }

}
