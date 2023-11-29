package example

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.UpdateOptions
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

class MongoDBHandler(env: mutable.Map[String, String]) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val mongoClient: MongoClient = MongoClient(env.getOrElse("CONNECTION_STRING", ""))
  private val database: MongoDatabase = mongoClient.getDatabase("Projektstudium")
  private val collection: MongoCollection[Document] = database.getCollection("redditRawData")

  def upsertDocument(document: Document): Unit = {
    val url = document.getString("url")
    val updateOptions = new UpdateOptions().upsert(true)
    val updateObservable = collection.updateOne(equal("url", url), set("document", document), updateOptions)
    observeResults(updateObservable)
  }

  private def observeResults[T](observable: Observable[T]): Unit = {
    observable.subscribe(new Observer[T] {
      override def onNext(result: T): Unit = logger.info(s"Processed: $result")
      override def onError(e: Throwable): Unit = logger.error(s"Failed: $e")
      override def onComplete(): Unit = logger.info("Completed")
    })
    Await.result(observable.toFuture(), 10.seconds)
  }
}
