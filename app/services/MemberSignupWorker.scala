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

package services

import java.util

import com.twilio.sdk.TwilioRestClient
import com.twilio.sdk.resource.factory.MessageFactory
import com.twilio.sdk.resource.instance.Message
import dao.{MemberDAO, SettingsDAO}
import models.Settings
import net.greghaines.jesque.ConfigBuilder
import net.greghaines.jesque.worker.{MapBasedJobFactory, Worker, WorkerImpl}
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Logger, Play}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Handles texting new members if they signed up with a phone number.
  */
class MemberSignupWorker(val twilioRestClient: TwilioRestClient, val memberDao: MemberDAO, val settingsDao: SettingsDAO) {
  /**
    * The mapping of queue actions that are handled to the class that handles them.
    */
  private val queueJobFactory: MapBasedJobFactory = new MapBasedJobFactory(Map(
    (Queues.SIGNUP_ACTION, classOf[MemberSignupAction])
  ).asJava)

  /**
    * Called when the worker is started. Used to configure and run the queue worker.
    *
    * @param args
    */
  def main(args: Array[String]): Unit = {
    val result = for {
      accountSid <- sys.env.get("TWILIO_ACCOUNT_SID") // Found at https://www.twilio.com/user/account/settings
      authToken <- sys.env.get("TWILIO_AUTH_TOKEN")
      from <- sys.env.get("FROM_NUMBER") // The number that messages are sent from.
    } yield {
      // Create Play application
      val playApp = GuiceApplicationBuilder().build()
      val dbConfigProvider = playApp.injector.instanceOf[DatabaseConfigProvider]

      // Create DAOs
      val memberDao = new MemberDAO(dbConfigProvider)
      val settingsDao = new SettingsDAO(dbConfigProvider)

      // Setup Twilio client & start worker
      val twilioRestClient = new TwilioRestClient(accountSid, authToken)
      val worker = new MemberSignupWorker(playApp, twilioRestClient, memberDao, settingsDao)
    }

    if (result.isEmpty) {
      Console.println("You must specify the TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN and FROM_NUMBER environment " +
        "variables.")
    }
  }

  /**
    * Called by the queue worker listener. Sends a welcome text if the user specified their phone number.
    *
    * @param id
    */
  def signupMember(id: Int) {
    // Lookup member & send a text if they have a number configured
    memberDao.get(id) onComplete {
      case Success(maybeMember) => maybeMember match {
        case Some(member) => {
          if (member.phoneNumber == None) {
            Logger.debug("Member doesn't have a phone number - not sending message.")
          } else {
            Logger.info(s"Sending message for member ${member.id.get} - '${member.name}' / '${member.phoneNumber.get}")

            val formattedNumber = formatPhoneNumber(member.phoneNumber.get)

            sendSMS(member.name, formattedNumber)
          }
        }

        case None => throw new RuntimeException(s"Member not found with ID ${id}")
      }

      case Failure(err) => {
        Logger.error("Failed to lookup member!", err)
        throw new RuntimeException("Unable to lookup member - see previous error.")
      }
    }
  }

  /**
    * Ensures a UK mobile number is in the format:
    *
    * +447123123123
    *
    * @param phoneNumber
    * @return
    */
  private def formatPhoneNumber(phoneNumber: String): String = {
    if (phoneNumber.startsWith("07")) {
      "+447" + phoneNumber.substring(2)
    } else if (phoneNumber.startsWith("447")) {
      "+" + phoneNumber
    } else {
      phoneNumber
    }
  }

  /**
    * Sends a welcome text message.
    *
    * @param name Member's name
    * @param phoneNumber Member's UK mobile number
    * @param from From number
    */
  private def sendSMS(name: String, phoneNumber: String, from: String) {
    settingsDao.get(Settings.WelcomeText) onComplete {
      case Success(maybeWelcomeMessage) => maybeWelcomeMessage match {
        case Some(welcomeMessage) => {
          val params = new util.ArrayList[NameValuePair]()
          params.add(new BasicNameValuePair("Body", welcomeMessage.value.replace("@@name@@", name)))
          params.add(new BasicNameValuePair("To", formatPhoneNumber(phoneNumber)))
          params.add(new BasicNameValuePair("From", from))

          val messageFactory: MessageFactory = twilioRestClient.getAccount().getMessageFactory
          val message: Message = messageFactory.create(params)

          Logger.debug(s"Sent message sid '${message.getSid}' to '${name}' (${phoneNumber})")
        }

        case None => throw new RuntimeException("Failed to lookup the welcome text message.")
      }

      case Failure(err) => throw new RuntimeException("Failed to lookup the welcome message.", err)
    }
  }

  // Jesque
  val config = new ConfigBuilder().build()
  val worker: Worker = new WorkerImpl(config, util.Arrays.asList(Queues.SIGNUP), queueJobFactory)
  val workerThread: Thread = new Thread(worker)

  workerThread.start()

  // Ensure that the worker is removed properly when shutdown
  sys.addShutdownHook({
    Logger.info("Shutting down...")

    workerThread.interrupt()
    worker.end(true)
    workerThread.join()

    Logger.info("Stopped.")
  })

  Logger.info("Started member sign-up queue worker.")
}

/**
  * An action that handles member sign-up queue messages.
  */
class MemberSignupAction(memberId: Int) extends Runnable {
  /**
    * Called when a new queue item is received by this worker. Used to send the member a text message.
    */
  override def run(): Unit = {
    MemberSignupWorker.signupMember(memberId)
  }
}
