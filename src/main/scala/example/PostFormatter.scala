package example

import play.api.libs.json._
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
            "_id" -> JsString((data \ "id").as[String]),
            "url" -> JsString((data \ "url").as[String]),
            "title" -> JsString((data \ "title").as[String]),
            "date" -> JsString(formattedDate),
            "subreddit" -> JsString(subreddit),
            "comments" -> JsNumber((data \ "num_comments").as[Int]),
            "selftext" -> JsString(selftext)
          )
        )
      } else {
        None
      }
    }
    Json.toJson(formattedPosts)
  }
}
