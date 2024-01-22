package example

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson._
import play.api.libs.json.{JsBoolean, JsNumber, JsString, JsValue}
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
      val desiredPostType = "new" // Options: rising, hot, new, trending
      val subreddits = List("news", "worldnews", "politics", "technology",
        "science", "environment", "economics",
        "Coronavirus", "TrueReddit", "UpliftingNews")

      subreddits.foreach { subreddit =>
        redditClient.getPosts(token, 1000, desiredPostType, subreddit) match {
          case Right(json) =>
            val formattedPosts = postFormatter.format(json, ignoreEmptySelftext = true)
            println(json)
            println(formattedPosts)
            savePostsToMongo(formattedPosts)
          case Left(error) =>
            println(s"Error fetching posts from $subreddit: $error")
        }
      }
    } else {
      println("Failed to obtain access token.")
    }
  }


  def savePostsToMongo(posts: JsValue): Unit = {
    val dbHandler = new MongoDBHandler(env)
    val postsSeq: Seq[JsValue] = posts.as[Seq[JsValue]]

    postsSeq.foreach { postJson =>
      val bsonDocument = BsonDocument()

      postJson.as[Map[String, JsValue]].foreach {
        case ("_id", JsString(value)) =>
          // Convert string to ObjectId if possible, otherwise MongoDB will generate it
          try {
            bsonDocument.append("_id", BsonObjectId(value))
          } catch {
            case _: IllegalArgumentException => // Skip setting _id if invalid, MongoDB will auto-generate it
          }
        case (key, JsString(value)) => bsonDocument.append(key, BsonString(value))
        case (key, JsNumber(value)) => bsonDocument.append(key, BsonString(value.toString))
        case (key, JsBoolean(value)) => bsonDocument.append(key, BsonString(value.toString))
        case _ => // Ignore other cases or handle as needed
      }

      // Create a MongoDB Document from the BsonDocument
      val document = Document(bsonDocument)

      // Insert the document into MongoDB
      dbHandler.insertDocument(document)
    }
    logger.info("Posts processed for MongoDB successfully!")
  }


}
