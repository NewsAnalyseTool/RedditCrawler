package example

import play.api.libs.json._
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

class PostFormatter {
  def format(json: JsValue, ignoreEmptySelftext: Boolean = false): JsValue = {
    val posts = (json \ "data" \ "children").as[JsArray].value
    val formattedPosts = posts.flatMap { post =>
      val data = post \ "data"
      val category = (data \ "subreddit").as[String]
      val text = (data \ "selftext").as[String]

      if (ignoreEmptySelftext && text.isEmpty) {
        None // Skip post if ignoreEmptySelftext is true and selftext is empty
      } else {
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
            "category" -> JsString(category),
            "comments" -> JsNumber((data \ "num_comments").as[Int]),
            "text" -> JsString(text),
            "source" -> "Reddit"
          )
        )
      }
    }
    Json.toJson(formattedPosts)
  }
}
