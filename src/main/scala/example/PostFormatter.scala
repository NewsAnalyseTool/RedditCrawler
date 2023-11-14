package example

import play.api.libs.json.{JsArray, JsValue, Json}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

class PostFormatter {
  def format(json: JsValue, ignoreEmptySelftext: Boolean = true): JsValue = {
    val posts = (json \ "data" \ "children").as[JsArray].value
    val formattedPosts = posts.flatMap { post =>
      val data = post \ "data"
      val selftext = (data \ "selftext").as[String]

      if (!ignoreEmptySelftext || (ignoreEmptySelftext && selftext.nonEmpty)) {
        val createdUtc = (data \ "created_utc").as[Long]
        val instant = Instant.ofEpochSecond(createdUtc)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formattedDate = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        Some(
          Json.obj(
            "Kategorie" -> (data \ "subreddit").as[String],
            "Titel" -> (data \ "title").as[String],
            "Text" -> selftext,
            "Datum" -> formattedDate,
            "isReddit" -> true,
            "Upvotes" -> (data \ "ups").as[Int],
            "Anzahl der Kommentare" -> (data \ "num_comments").as[Int]
          )
        )
      } else {
        None
      }
    }
    Json.toJson(formattedPosts)
  }
}
