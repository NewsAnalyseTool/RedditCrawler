package example
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.{IndexOptions, Indexes, UpdateOptions}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Manages MongoDB operations for storing and updating documents.
 *
 * Initializes database connection and sets up a unique index on the date field.
 *
 * @param env Configuration map with database connection string.
 */
class MongoDBHandler(env: mutable.Map[String, String]) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val mongoClient: MongoClient = MongoClient(env.getOrElse("CONNECTION_STRING", ""))
  private val database: MongoDatabase = mongoClient.getDatabase("Reddit")
  private val collection: MongoCollection[Document] = database.getCollection("reddit_raw_data")

  collection.createIndex(Indexes.ascending("date"), IndexOptions().unique(true)).toFuture().onComplete {
    case Success(result) => logger.info(s"Index creation result: $result")
    case Failure(e) => logger.error("Failed to create index", e)
  }

  /**
   * Inserts a document if no existing document has the same date.
   *
   * @param document Document to insert.
   */
  def insertDocument(document: Document): Unit = {
    val date = document.getString("date")

    collection.countDocuments(equal("date", date)).head().flatMap { count =>
      if (count > 0) {
        logger.info(s"Document with date $date already exists. Skipping insertion.")
        Future.successful(())
      } else {
        val insertObservable = collection.insertOne(document)
        insertObservable.toFuture()
      }
    }.onComplete {
      case Success(_) => logger.info(s"Insertion completed for date $date")
      case Failure(e) => logger.error(s"Failed to insert document with date $date", e)
    }
  }


  /**
   * Logs results of MongoDB operations.
   *
   * @param observable MongoDB operation to observe.
   * @tparam T Type of the operation result.
   */
  private def observeResults[T](observable: Observable[T]): Unit = {
    observable.subscribe(new Observer[T] {
      override def onNext(result: T): Unit = logger.info(s"Processed: $result")
      override def onError(e: Throwable): Unit = logger.error(s"Failed: $e")
      override def onComplete(): Unit = logger.info("Completed")
    })
    Await.result(observable.toFuture(), 10.seconds)
  }
}
