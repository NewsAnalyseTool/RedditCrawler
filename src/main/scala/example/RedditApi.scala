package example

import org.mongodb.scala.Document
import play.api.libs.json.{JsValue, Json}
import org.slf4j.LoggerFactory
import scala.collection.mutable

object RedditAPI {
  private val logger = LoggerFactory.getLogger(getClass)
  val environment = new Environment
  val env: mutable.Map[String, String] = environment.load()
  def main(args: Array[String]): Unit = {
    val rateLimitHandler = new RateLimitHandler
    val redditClient = new RedditClient(env, rateLimitHandler)
    val postFormatter = new PostFormatter

    val token = redditClient.getAccessToken()
    if (token.nonEmpty) {
      val desiredPostType = "new" //rising, hot, new, trending
      val posts = redditClient.getPosts(token, 1000)
      posts match {
        case Right(json) =>
          val formattedPosts = postFormatter.format(json, ignoreEmptySelftext = true)
          savePostsToMongo(formattedPosts)
        case Left(error) => println(error)
      }
    } else {
      println("Failed to obtain access token.")
    }
  }

  def savePostsToMongo(posts: JsValue): Unit = {
    val dbHandler = new MongoDBHandler(env)
    val postsSeq: Seq[JsValue] = posts.as[Seq[JsValue]]

    postsSeq.foreach { postJson =>
      val postMap = postJson.as[Map[String, JsValue]] - "_id"
      val document = Document(postMap.mapValues(_.toString))
      dbHandler.insertDocument(document)
    }
    logger.info("Posts processed for MongoDB successfully!")
  }
}
