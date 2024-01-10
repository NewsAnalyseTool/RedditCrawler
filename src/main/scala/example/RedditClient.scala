package example

import play.api.libs.json.{JsValue, Json}
import sttp.client3.{HttpURLConnectionBackend, UriContext, basicRequest}

import scala.collection.mutable
import scala.util.Try

class RedditClient(env: mutable.Map[String, String], rateLimitHandler: RateLimitHandler) {
  val backend = HttpURLConnectionBackend()

  def getAccessToken(): String = {
    val clientId = env.getOrElse("CLIENT_ID", "")
    val clientSecret = env.getOrElse("CLIENT_SECRET", "")
    val username = env.getOrElse("USERNAME", "")
    val password = env.getOrElse("PASSWORD", "")

    val request = basicRequest
      .header("User-Agent", s"RedditAPIApp/1.0 by $username")
      .auth.basic(clientId, clientSecret)
      .post(uri"https://www.reddit.com/api/v1/access_token")
      .body(Map("grant_type" -> "password", "username" -> username, "password" -> password))
      .send(backend)

    request.body match {
      case Left(errorResponse) =>
        println(s"Failed to obtain access token. Error: ${errorResponse}")
        ""

      case Right(successResponse) =>
        (Json.parse(successResponse) \ "access_token").asOpt[String] match {
          case Some(token) =>
            println(s"Successfully obtained access token: $token")
            token
          case None =>
            println(s"Unexpected response format. Response: $successResponse")
            ""
        }
    }
  }

  def getPosts(token: String, postLimit: Int, postType: String): Either[String, JsValue] = {
    val username = env.getOrElse("USERNAME", "")

    val request = basicRequest
      .header("User-Agent", s"RedditAPIApp/1.0 by $username")
      .header("Authorization", s"bearer $token")
      .get(uri"https://oauth.reddit.com/r/PoliticalDiscussion/$postType?limit=$postLimit") // Update the URL here

    val response = Try(request.send(backend)).getOrElse(return Left("Failed to send request."))

    response.code match {
      case code if code.code == 429 =>
        rateLimitHandler.handle()
        getAccessToken()
        return Left("Rate limited.")
      case _ =>
    }

    response.body match {
      case Right(content) => Right(Json.parse(content))
      case Left(error) =>
        println(s"Error fetching posts: $error")
        Left(s"Error fetching posts: $error")
    }
  }

}

