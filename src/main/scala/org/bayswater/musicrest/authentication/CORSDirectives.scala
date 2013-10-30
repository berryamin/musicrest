package org.bayswater.musicrest.authentication

import spray.http._
import spray.routing._
import spray.http.HttpHeaders._
/** This is a temporary CORS directive while we wait for
 *  the real one to find its way into a Spray release.
 */
trait CORSDirectives  { this: HttpService =>
  private def respondWithCORSHeaders(origin: String) =
    respondWithHeaders(      
      HttpHeaders.`Access-Control-Allow-Origin`(SomeOrigins(List(origin))),
      HttpHeaders.`Access-Control-Allow-Credentials`(true)
    )
  private def respondWithCORSHeadersAllOrigins =
    respondWithHeaders(      
      HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins),
      HttpHeaders.`Access-Control-Allow-Credentials`(true)
    )

  def corsFilter(origins: List[String])(route: Route) =
    if (origins.contains("*"))
      respondWithCORSHeadersAllOrigins(route)
    else
      optionalHeaderValueByName("Origin") {
        case None => 
          route        
        case Some(clientOrigin) => {
          if (origins.contains(clientOrigin))
            respondWithCORSHeaders(clientOrigin)(route)
          else {
            // Maybe, a Rejection will fit better
            complete(StatusCodes.Forbidden, "Invalid origin")
          }      
        }
      }
}
