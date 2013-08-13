/*
 * Copyright (C) 2011-2013 org.bayswater
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

package org.bayswater.musicrest.tools

import javax.mail._
import javax.mail.internet._
import java.util.Properties._
import scalaz._
import Scalaz._
import org.bayswater.musicrest.model.{User, UserRef}
import org.bayswater.musicrest.MusicRestSettings

object Email {
  
  val musicRestSettings = MusicRestSettings
  
  private def send(recipientAddress:String, subject: String, content:String): Unit = {
    // Set up the mail object
    val properties = System.getProperties
    properties.put("mail.smtp.host", musicRestSettings.mailHost)
    properties.put("mail.smtp.auth", "true")
    properties.put("mail.smtp.port", musicRestSettings.mailPort)
    if ("smtp.gmail.com" == musicRestSettings.mailHost) {      
      properties.put("mail.smtp.starttls.enable", "true")
    }
    // properties.put("mail.smtp.starttls.enable","true");
    val session = Session.getDefaultInstance(properties, MusicRestMailAuthenticator)       
    println(s"mail session set up OK to recipient $recipientAddress")
    println(s"host ${musicRestSettings.mailHost}:${musicRestSettings.mailPort}")
    
    val message = new MimeMessage(session)

    // Set the from, to, subject, body text
    message.setFrom(new InternetAddress(musicRestSettings.mailFromAddress))
    message.setRecipients(Message.RecipientType.TO, recipientAddress)
    message.setSubject(subject)
    message.setText(content)

    // And send it
    Transport.send(message)  
  }
  
  def sendRegistrationMessage(user:User, isPreRegistered: Boolean): Validation[String, String] = {
    val url = "http://localhost:8080/musicrest/user/validate/" + user.uuid
    val subject = "Musicrest user validation"
    val message = if (isPreRegistered) { 
      "Thanks for signing up! \n\n" +
      "Your account has been created for you, you can login with the following credentials " +
      "------------------------\n" +
      "Username: " + user.name + "\n" +
      "Password: " + user.password + "\n" +
      "------------------------\n\n"       
    }
    else {           
      "Thanks for signing up! \n\n" +
      "Your account has been created, you can login with the following credentials after you have activated your account " +
      "by pressing the url below. \n\n" +
      "------------------------\n" +
      "Username: " + user.name + "\n" +
      "Password: " + user.password + "\n" +
      "------------------------\n\n" + 
      "Please click this link to activate your account: " + url + "\n"
    }
    
    try {
       send(user.email, subject, message)        
       println("sent email confirmation")
       ("User: " + user.name + " notified").success
    }
    catch {
      case e:Exception => ("problem sending registration email for user: " + user.name + " : " + e.getMessage).fail
    }
  }
  
  def sendPasswordMessage(userRef:UserRef): Validation[String, String] = {
 
    val subject = "Musicrest password reminder"
    val message =
      "------------------------\n" +
      "Username: " + userRef.name + "\n" +
      "Password: " + userRef.password + "\n" +
      "------------------------\n\n" 
    
    try {
       send(userRef.email, subject, message)        
       println("sent email confirmation")
       ("User: " + userRef.name + " password reminder").success
    }
    catch {
      case e:Exception => ("problem sending registration email for user: " + userRef.name + " : " + e.getMessage).fail
    }   
  }
  
  object MusicRestMailAuthenticator extends Authenticator {
    override def getPasswordAuthentication(): PasswordAuthentication =  new PasswordAuthentication(musicRestSettings.mailLogin, musicRestSettings.mailPassword)    
  }

}