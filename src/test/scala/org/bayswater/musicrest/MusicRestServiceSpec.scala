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

package org.bayswater.musicrest

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import MediaTypes._
import org.bayswater.musicrest.abc.Tune.AbcType
import spray.http.HttpHeaders._
import scalaz.Validation
import org.bayswater.musicrest.model.{TuneModel,User, UnregisteredUser}
import org.bayswater.musicrest.TestData._


/* These tests exercise URLs which on the whole do NOT require content negotiation */
class MusicRestServiceSpec extends RoutingSpec with MusicRestService {
  def actorRefFactory = system
  
   val before = {basicUsers; basicGenres; insertTune}
  
   val newUserUuid =  unregisteredUser.fold (
      e => "non-existent uri",
      s => s.uuid
      )

  "MusicRestService" should {

    "return a greeting for GET requests to the root path" in {
      Get("/musicrest") ~> musicRestRoute ~> check {
        entityAs[String] must contain("MusicRest version")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> musicRestRoute ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put("/musicrest") ~> sealRoute(musicRestRoute) ~> check {
        status === MethodNotAllowed
        entityAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }    
    
    "allow a valid user to insert a tune and return its name" in {
       Post("/musicrest/genre/irish/tune", HttpEntity(`application/x-www-form-urlencoded`, TestData.frahers)) ~>  Authorization(BasicHttpCredentials("administrator", "adm1n1str80r")) ~> musicRestRoute ~> check 
         { 
         mediaType === MediaTypes.`text/plain`
         entityAs[String] must contain("fraher's jig-jig") 
         }
    }    
    
    "allow a valid user to insert a tune with a unicode name and not be confused" in {
       Post("/musicrest/genre/scandi/tune", HttpEntity(`application/x-www-form-urlencoded`, TestData.hälleforsnäs)) ~>  Authorization(BasicHttpCredentials("administrator", "adm1n1str80r")) ~> musicRestRoute ~> check 
         { 
         contentType === ContentType(MediaTypes.`text/plain`, HttpCharsets.`UTF-8`)
         entityAs[String] must contain("hälleforsnäs") 
         }
    }
    
   "allow a valid user to post a tune for a test transcode and return its name" in {
       Post("/musicrest/genre/irish/transcode", HttpEntity(`application/x-www-form-urlencoded`, TestData.figForaKiss))  ~>  Authorization(BasicHttpCredentials("administrator", "adm1n1str80r")) ~> musicRestRoute ~> check 
         { 
         mediaType === MediaTypes.`text/plain`
         entityAs[String] must contain("a fig for a kiss-slip jig") 
         }
    }       
   
    
     /* Tunes requiring no content negotiation - the media type is supplied in the url*/
    "return text/html content when requested in the url for tunes of this type" in {
      Get("/musicrest/genre/irish/tune/noon+lasses-reel/html") ~> musicRestRoute ~> check {
        entityAs[String] must contain("T: Noon Lasses")
        entityAs[String] must contain("M: 4/4")
        mediaType === MediaTypes.`text/html`
      }
    }   
    
    "return text/vnd.abc content when requested in the url for tunes of this type" in {
      Get("/musicrest/genre/irish/tune/noon+lasses-reel/abc") ~> musicRestRoute ~> check {
        entityAs[String] must contain("T: Noon Lasses")
        entityAs[String] must contain("M: 4/4")
        // we always add an S: header for this MIME type into the ABC even if one is absent when submitted
        entityAs[String] must contain("S: ")
        mediaType === AbcType
      }
    }   
    
    "return audio/wav content when requested in the url for tunes of this type" in {
      Get("/musicrest/genre/irish/tune/noon+lasses-reel/wav") ~> musicRestRoute ~> check {
        if (mediaType == MediaTypes.`text/plain`) {
          println(s"wav error ${entityAs[String]}")
        }
        mediaType === MediaTypes.`audio/wav`
      }
    }    
    
    "return audio/midi content when requested in the url for tunes of this type" in {
      Get("/musicrest/genre/irish/tune/noon+lasses-reel/midi") ~> musicRestRoute ~> check {
        mediaType === MediaTypes.`audio/midi`
      }
    }        
    
    "return application/pdf content when requested in the url for tunes of this type" in {
      Get("/musicrest/genre/irish/tune/noon+lasses-reel/pdf") ~> musicRestRoute ~> check {
        mediaType === MediaTypes.`application/pdf`
      }
    }
     
    "return application/postscript content when requested for tunes of this type" in {
      Get("/musicrest/genre/irish/tune/noon+lasses-reel/ps") ~> musicRestRoute ~> check {
        mediaType === MediaTypes.`application/postscript`
      }
    }      
    
    "return image/png content when requested for tunes of this type" in {
      Get("/musicrest/genre/irish/tune/noon+lasses-reel/png") ~> musicRestRoute ~> check {
        mediaType === MediaTypes.`image/png`
      }
    }   
    
    /** if this works, so should bad requests for other binary image types */ 
    "return a sensible error when requested for images of tunes that don't exist" in {
      Get("/musicrest/genre/irish/tune/unknown-tune/png") ~> musicRestRoute ~> check {
        status === BadRequest
        entityAs[String] === "unknown-tune not found in genre irish for image/png"
      }
    }   
    
    "return true when requested for tunes that do exist" in {
      Get("/musicrest/genre/irish/tune/noon+lasses-reel/exists") ~> musicRestRoute ~> check {
        entityAs[String] must contain("true")
        mediaType === MediaTypes.`text/plain`
      }
    }    
        
    "return false when requested for tunes that don't exist" in {
      Get("/musicrest/genre/irish/tune/unknown-tune/exists") ~> musicRestRoute ~> check {
        entityAs[String] must contain("false")
        mediaType === MediaTypes.`text/plain`
      }
    }    
    
    "return true when requested for genres that do exist" in {
      Get("/musicrest/genre/irish/exists") ~> musicRestRoute ~> check {
        entityAs[String] must contain("true")
        mediaType === MediaTypes.`text/plain`
      }
    }        
    
    "return false when requested for genres that don't exist" in {
      Get("/musicrest/genre/unknown-genre/exists") ~> musicRestRoute ~> check {
        entityAs[String] must contain("false")
        mediaType === MediaTypes.`text/plain`
      }
    }    
    

      
    /* can't test this yet - we'll wait for the marshalling changes in Spray
    "return a comprehensible error message when requested for tunes that don't exist" in {
      Get("/musicrest/genre/irish/tune/nosuchtune/abc") ~> musicRestRoute ~> check {
        rejection must === ("No such Tune")
      }
    }    
    */
    
   /* Activate these next two tests if you have email from Musicrest configured and working 
    * and alter the email setting in TestData.newUserFormData to an email of yours
    * so that you can confirm the message is sent properly
    */
    /* 
   "allow a new user to be added" in {
       Post("/musicrest/user", TestData.newUser1FormData)  ~> musicRestRoute ~> check 
         { 
         entityAs[String] must contain("User: mike-mcgoldrick OK") 
         // mediaType === MediaTypes.`text/plain`
         mediaType === MediaTypes.`text/html`
         }
    }   
   */
    /* 
   "allow a pre-registered user to be added by the administrator" in {
       Post("/musicrest/user", TestData.newUser2FormData)  ~> Authorization(BasicHttpCredentials("administrator", "adm1n1str80r")) ~> musicRestRoute ~> check 
         { 
         entityAs[String] must contain("User: kevin-crawford OK") 
         // mediaType === MediaTypes.`text/plain`
         mediaType === MediaTypes.`text/html`
         }
    }   
   */
   "allow a user to reset his password" in {
       Post("/musicrest/user/password/reset", TestData.newPasswordFormData)  ~>  Authorization(BasicHttpCredentials("test user", "passw0rd1")) ~> musicRestRoute ~> check 
         { 
         entityAs[String] must contain("test user: password changed") 
         // mediaType === MediaTypes.`text/plain`
         mediaType === MediaTypes.`text/plain`
         }
    }   
   
   "register a new user" in {
       Get("/musicrest/user/validate/" + newUserUuid)  ~> musicRestRoute ~> check 
          { 
           mediaType === MediaTypes.`text/html`
           entityAs[String] must contain("User: mathias OK") 
          }
    }
    
 
  }
  
 def insertTune = {
   val dbName = "tunedbtest"
   val tuneModel = TuneModel()
   tuneModel.delete("irish")
   val validNoonLasses = abcFor(noonLasses)
   validNoonLasses.fold(e => println("unexpected error in test data: " + e), s => tuneModel.insert("irish", s))  
  }  

}




