package example

import org.mongodb.scala._
import play.api.libs.json.JsValue

import scala.util.{Failure, Success}
import org.mongodb.scala.bson.collection.immutable.Document

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

class MongoDBHandler(env: mutable.Map[String, String]) {
  private val mongoClient: MongoClient = MongoClient(env.getOrElse("CONNECTION_STRING", ""))
  private val database: MongoDatabase = mongoClient.getDatabase("Projektstudium")
  private val collection: MongoCollection[Document] = database.getCollection("redditRawData")

  // Inserts a single document into the MongoDB collection
  def insertDocument(document: Document): Unit = {
    val insertObservable = collection.insertOne(document)
    observeResults(insertObservable)
  }

  // Inserts multiple documents into the MongoDB collection
  def insertDocumentsFromDocs(documents: Seq[Document]): Unit = {
    val insertObservable = collection.insertMany(documents)
    observeResults(insertObservable)
  }

  // Inserts multiple JSON posts into the MongoDB collection
  def insertDocumentsFromJson(posts: Seq[JsValue]): Unit = {
    val documents = posts.map(post => Document(post.toString()))
    insertDocumentsFromDocs(documents)
  }

  // Helper function to handle MongoDB observables
  private def observeResults[T](observable: Observable[T]): Unit = {
    observable.subscribe(new Observer[T] {
      override def onNext(result: T): Unit = println(s"Inserted: $result")
      override def onError(e: Throwable): Unit = println(s"Failed: $e")
      override def onComplete(): Unit = println("Completed")
    })
    // Waiting for the operation to complete
    Await.result(observable.toFuture(), 10.seconds)
  }
}
