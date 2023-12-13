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
  private val collection: MongoCollection[Document] = database.getCollection("redditTestData")

  def upsertDocument(document: Document): Unit = {
    val id = document.getString("_id")
    val updateOptions = new UpdateOptions().upsert(true)

    // Remove the _id field from the document for the update
    val updateDoc = Document(document)
    updateDoc.remove("_id")

    // Create the update document using the Document companion object
    val update = Document("$set" -> updateDoc)

    val updateObservable = collection.updateOne(equal("_id", id), update, updateOptions)
    observeResults(updateObservable)
  }

  def insertDocument(document: Document): Unit = {
    val insertObservable = collection.insertOne(document)
    observeResults(insertObservable)
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
