package storage

import com.mongodb.BasicDBObjectBuilder
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient

class MongoContext(val config: MongoConfig) {
  val mongoClient = MongoClient()
  val mongoDB = mongoClient(config.dbName)

  implicit val options: DBObject = BasicDBObjectBuilder.start().add("capped", true).add("size", 2000000000l).get()

  def messagesCollection = getOrCreate(config.messages)

  def topicsCollection = getOrCreate(config.topics)

  def usersCollection = getOrCreate(config.users)


  private def getOrCreate(collName: String) =
      if (mongoDB.collectionExists(collName)) {
        mongoDB(collName)
      } else {
        mongoDB.createCollection(collName, options)
        mongoDB(collName)
      }


  def drop = {
    mongoDB.dropDatabase()
  }


}
