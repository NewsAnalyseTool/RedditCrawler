package example

import play.api.libs.json.{JsArray, JsValue, Json}
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

class PostFormatter {
  def format(json: JsValue, ignoreEmptySelftext: Boolean = false): JsValue = {
    val posts = (json \ "data" \ "children").as[JsArray].value
    val formattedPosts = posts.flatMap { post =>
      val data = post \ "data"
      val subreddit = (data \ "subreddit").as[String]
      val selftext = (data \ "selftext").as[String]

      if (subreddit == "PoliticalDiscussion") {
        val createdUtc = (data \ "created_utc").as[Long]
        val instant = Instant.ofEpochSecond(createdUtc)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formattedDate = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        Some(
          Json.obj(
            "_id" -> (data \ "id").as[String],
            "url" -> (data \ "url").as[String],
            "title" -> (data \ "title").as[String],
            "date" -> formattedDate,
            "subreddit" -> subreddit,
            "comments" -> (data \ "num_comments").as[Int],
            "selftext" -> selftext  // Include selftext in the JSON object
            // Add other fields as needed
          )
        )
      } else {
        None
      }
    }
    Json.toJson(formattedPosts)
  }
}
