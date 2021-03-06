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

package com.jsherz.luskydive.util

import com.typesafe.config.ConfigFactory

/**
  * The application configuration, as read from resources/application.conf.
  */
trait Config {

  private val config = ConfigFactory.load()
  private val configHttp = config.getConfig("http")
  private val configDb = config.getConfig("database")
  private val configTwilio = config.getConfig("twilio")

  val interface = configHttp.getString("interface")
  val port = configHttp.getInt("port")

  val dbUrl = configDb.getString("url")
  val dbUsername = configDb.getString("username")
  val dbPassword = configDb.getString("password")

  val textMessageReceiveApiKey = config.getString("text_message_receive_api_key")

  val twilioAccountSid = configTwilio.getString("account_sid")
  val twilioAuthToken = configTwilio.getString("auth_token")
  val twilioMessagingServiceSid = configTwilio.getString("messaging_service_sid")

}
