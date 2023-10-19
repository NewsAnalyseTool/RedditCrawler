package example

import sttp.client3._
import play.api.libs.json._
import scala.io.Source
import scala.collection.mutable
import java.lang.Thread.sleep
import scala.concurrent.duration._
import scala.util.Try

object RedditAPI {
  // Load env variables
  private def loadEnv(): mutable.Map[String, String] = {
    val env = mutable.Map[String, String]()
    for (line <- Source.fromFile(".env").getLines()) {
      line.split("=").map(_.trim) match {
        case Array(key, value) =>
          env(key) = value.replaceAll("\"", "") // Remove double quotes
        case _ =>
          println(s"Warning: Skipping malformed line in .env: $line")
      }
    }
    env
  }

  private val env = loadEnv()
  val clientId = env.getOrElse("CLIENT_ID", "")
  val clientSecret = env.getOrElse("CLIENT_SECRET", "")
  val redirectUri = env.getOrElse("REDIRECT_URI", "")
  val username = env.getOrElse("USERNAME", "")
  val password = env.getOrElse("PASSWORD", "")

  val RateLimitPause: FiniteDuration = 1.second
  var exponentialBackoff: FiniteDuration = 1.second

  val backend = HttpURLConnectionBackend()

  def handleRateLimit(): Unit = {
    println(s"Rate limited by Reddit. Waiting for $exponentialBackoff and then retrying...")
    sleep(exponentialBackoff.toMillis)
    exponentialBackoff *= 2
  }

  def getAccessToken(): String = {
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

  def getHotPostsWorldwide(token: String, postLimit: Int): Unit = {
    sleep(RateLimitPause.toMillis)

    val request = basicRequest
      .header("User-Agent", s"RedditAPIApp/1.0 by $username")
      .header("Authorization", s"bearer $token")
      .get(uri"https://oauth.reddit.com/r/all/hot?limit=$postLimit")


    val response = Try(request.send(backend)).getOrElse(return)

    response.code match {
      case code if code.code == 429 => handleRateLimit(); getAccessToken()
      case _ =>
    }

    val responseBody = response.body match {
      case Right(content) => content
      case Left(error) =>
        println(s"Error fetching hot posts worldwide: $error")
        return
    }

    val json = Json.parse(responseBody)
    val posts = (json \ "data" \ "children").as[JsArray].value

    val formattedPosts = posts.map { post =>
      val data = post \ "data"
      val title = (data \ "title").as[String]
      val selftext = (data \ "selftext").as[String]
      val subredditName = (data \ "subreddit").as[String]

      Json.obj(
        "Subreddit" -> subredditName,
        "Title" -> title,
        "Text" -> selftext
      )
    }
    println(Json.prettyPrint(Json.toJson(formattedPosts)))
  }

  def main(args: Array[String]): Unit = {
    val token = getAccessToken()
    if (token.nonEmpty) getHotPostsWorldwide(token, 100)
    else println("Failed to obtain access token.")
  }

}


