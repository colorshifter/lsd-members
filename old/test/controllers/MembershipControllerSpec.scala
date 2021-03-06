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

import dao.MemberDAO
import models.Member
import org.scalatest.Matchers._

/**
 * Integration tests with a real (or headless) browser.
 */
class MembershipControllerSpec extends BaseSpec {
  /**
    * An instance of the Member DAO, used for testing.
    */
  private val memberDao = app.injector.instanceOf[MemberDAO]

  "MembershipController" should {
    "direct the user to the alternative form when the 'Alt' button is clicked" in {
      go to ("http://localhost:" + port)
      pageSource must include ("Phone number")

      click on find(linkText("Alt")).value

      eventually { pageSource must include ("E-mail address") }
    }

    "direct the user to the main form when the 'Main' button is clicked" in {
      go to (s"http://localhost:${port}/alt")
      pageSource must include ("E-mail address")

      click on find(linkText("Main")).value

      eventually { pageSource must include ("Phone number") }
    }

    "highlight the correct menu button for each form" in {
      go to ("http://localhost:" + port)
      find(cssSelector("nav .active")).value.text must equal("Main")

      go to (s"http://localhost:${port}/alt")
      eventually { find(cssSelector("nav .active")).value.text must equal("Alt") }
    }

    "highlight the correct menu button for each form when submitted" in {
      go to ("http://localhost:" + port)
      click on find(cssSelector("button[type=submit]")).value
      find(cssSelector("nav .active")).value.text must equal("Main")

      go to (s"http://localhost:${port}/alt")
      click on find(cssSelector("button[type=submit]")).value
      eventually { find(cssSelector("nav .active")).value.text must equal("Alt") }
    }

    "display errors for every field if nothing was entered" in {
      go to ("http://localhost:" + port)
      click on find(cssSelector("button[type=submit]")).value

      eventually {
        find(cssSelector("#name_field .error")).value.text must include ("This field is required")
        find(cssSelector("#phoneNumber_field .error")).value.text must include ("This field is required")
      }
    }

    "display an error if no name was entered" in {
      go to ("http://localhost:" + port)
      click on find("phoneNumber").value
      enter("07123123123")
      click on find(cssSelector("button[type=submit]")).value

      eventually { find(cssSelector("#name_field .error")).value.text must include ("This field is required") }
    }

    "display an error if no mobile phone was entered" in {
      go to ("http://localhost:" + port)
      click on find("name").value
      enter("Joe Bloggs")
      click on find(cssSelector("button[type=submit]")).value

      eventually { find(cssSelector("#phoneNumber_field .error")).value.text must include ("This field is required") }
    }

    "display an error if no e-mail address was entered (alternative form)" in {
      go to (s"http://localhost:${port}/alt")
      click on find("name").value
      enter("Joe Bloggs")
      click on find(cssSelector("button[type=submit]")).value

      eventually { find(cssSelector("#email_field .error")).value.text must include ("This field is required") }
    }

    "display an error if an invalid mobile phone number was entered" in {
      go to ("http://localhost:" + port)
      click on find("name").value
      enter("Joe Bloggs")
      click on find("phoneNumber").value
      enter("4457123123123")
      click on find(cssSelector("button[type=submit]")).value

      eventually { find(cssSelector("#phoneNumber_field .error")).value.text must include ("Invalid mobile number") }
    }

    "display an error if an invalid e-mail address was entered (alternative form)" in {
      go to (s"http://localhost:${port}/alt")
      click on find("name").value
      enter("Joe Bloggs")
      click on find("email").value
      enter("bloggs@localhost")
      click on find(cssSelector("button[type=submit]")).value

      eventually { find(cssSelector("#email_field .error")).value.text must include ("Invalid e-mail address") }
    }

    "display an error if the name entered is longer than the database field length" in {
      go to ("http://localhost:" + port)
      click on find("name").value
      enter("Tyler Freestyla, First of His Rap Game, The Best Ya")
      click on find("phoneNumber").value
      enter("4407123123123")
      click on find(cssSelector("button[type=submit]")).value

      eventually { find(cssSelector("#name_field .error")).value.text must include ("too long") }
    }

    "display an error if the e-mail entered is longer than the database field length" in {
      go to (s"http://localhost:${port}/alt")
      click on find("name").value
      enter("Tyler Freestyla")
      click on find("email").value
      enter("tyler-the-freestyla@has-a-really-cool-website-full-of-exciting-and-interesting-things-mainly-his-really-" +
        "cool-rap-lyrics-did-i-tell-you-its-all-free-to-download-including-step-by-step-guides-and-dance-moves-wow-this-" +
        "email-field-is-laaaaaaarrrrggeeeeeeee.com")
      click on find(cssSelector("button[type=submit]")).value

      eventually { find(cssSelector("#email_field .error")).value.text must include ("too long") }
    }

    "redirect the user to the thank you page if a valid name and phone number were entered" in {
      go to ("http://localhost:" + port)
      click on find("name").value
      enter("Joe Bloggs")
      click on find("phoneNumber").value
      enter("07123123123")
      click on find(cssSelector("button[type=submit]")).value

      eventually { pageSource should include ("Thank you!") }
    }

    "redirect the user to the thank you page if a valid name and e-mail were entered (alternative form)" in {
      go to (s"http://localhost:${port}/alt")
      click on find("name").value
      enter("Joe Bloggs")
      click on find("email").value
      enter("bloggs@localhost.com")
      click on find(cssSelector("button[type=submit]")).value

      eventually { pageSource should include ("Thank you!") }
    }

    "display an error if a member exists with the given phone number" in {
      val member = Member(None, "Joe Bloggs", Some("07123123123"), None)
      memberDao.insert(member).futureValue

      go to (s"http://localhost:${port}")
      click on find("name").value
      enter(member.name)
      click on find("phoneNumber").value
      enter(member.phoneNumber.get)
      click on find(cssSelector("button[type=submit]")).value

      eventually { pageSource should include ("already signed up") }
    }

    "display an error if a member exists with the given e-mail" in {
      val member = Member(None, "Joe Bloggs", None, Some("joe@bloggs.org"))
      memberDao.insert(member).futureValue

      go to (s"http://localhost:${port}/alt")
      click on find("name").value
      enter(member.name)
      click on find("email").value
      enter(member.email.get)
      click on find(cssSelector("button[type=submit]")).value

      eventually { pageSource should include ("already signed up") }
    }
  }
}
