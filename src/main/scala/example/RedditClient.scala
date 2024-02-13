package example

import play.api.libs.json.{JsValue, Json}
import sttp.client3.{HttpURLConnectionBackend, UriContext, basicRequest}

import scala.collection.mutable
import scala.util.Try

/**
 * Client for fetching Reddit data, handling authentication and rate limits.
 *
 * @param env A map containing environment configuration such as client ID, secret, etc.
 * @param rateLimitHandler An instance of RateLimitHandler to manage API call rate limits.
 */
class RedditClient(env: mutable.Map[String, String], rateLimitHandler: RateLimitHandler) {
  val backend = HttpURLConnectionBackend()

  /**
   * Retrieves an access token for Reddit API authentication.
   *
   * @return An access token as a String. Returns an empty string in case of failure.
   */
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

  /**
   * Fetches posts from a specific subreddit using the Reddit API.
   *
   * @param token     The access token for API authentication.
   * @param postLimit The maximum number of posts to fetch.
   * @param postType  The type of posts to fetch (e.g., "new", "hot").
   * @param subreddit The subreddit from which to fetch posts.
   * @return Either an error message or the fetched posts as JsValue.
   */
  def getPosts(token: String, postLimit: Int, postType: String, subreddit: String): Either[String, JsValue] = {
    val username = env.getOrElse("USERNAME", "")

    val request = basicRequest
      .header("User-Agent", s"RedditAPIApp/1.0 by $username")
      .header("Authorization", s"bearer $token")
      .get(uri"https://oauth.reddit.com/r/$subreddit/$postType?limit=$postLimit")

    val response = Try(request.send(backend)).getOrElse(return Left("Failed to send request."))

    response.code match {
      case code if code.code == 429 =>
        rateLimitHandler.handle()
        getAccessToken()
        return Left("Rate limited.")
      case _ =>
    }

    response.body match {
      case Right(content) =>
        Right(Json.parse(content))

      case Left(error) =>
        println(s"Error fetching posts: $error")
        Left(s"Error fetching posts: $error")
    }
  }
}
