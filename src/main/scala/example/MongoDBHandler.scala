package example
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.{IndexOptions, Indexes, UpdateOptions}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class MongoDBHandler(env: mutable.Map[String, String]) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val mongoClient: MongoClient = MongoClient(env.getOrElse("CONNECTION_STRING", ""))
  private val database: MongoDatabase = mongoClient.getDatabase("Projektstudium")
  private val collection: MongoCollection[Document] = database.getCollection("redditTestData")

  // Ensuring that the date field has a unique index
  collection.createIndex(Indexes.ascending("date"), IndexOptions().unique(true)).toFuture().onComplete {
    case Success(result) => logger.info(s"Index creation result: $result")
    case Failure(e) => logger.error("Failed to create index", e)
  }

  def upsertDocument(document: Document): Unit = {
    val date = document.getString("date")
    val updateOptions = new UpdateOptions().upsert(true)

    val updateDoc = Document(document)
    updateDoc.remove("_id") // Remove the _id field as we are using date for uniqueness

    val update = Document("$set" -> updateDoc)

    val updateObservable = collection.updateOne(equal("date", date), update, updateOptions)
    observeResults(updateObservable)
  }

  def insertDocument(document: Document): Unit = {
    try {
      val insertObservable = collection.insertOne(document)
      observeResults(insertObservable)
    } catch {
      case e: MongoWriteException if e.getError.getCode == 11000 =>
        logger.error("Duplicate key error, document already exists")
    }
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

