package storage

import com.typesafe.config.ConfigFactory
import storage.constants.{DefaultConfigValues, DefaultConfigKeys}
import DefaultConfigValues._
import storage.constants.DefaultConfigKeys
import DefaultConfigKeys._

import scala.util.Try


case class MongoConfig(host: String = defaultHost,
                       port: Int = defaultPort,
                       user: String = defaultUser,
                       password: String = defaultPassword,
                       dbName: String = defaultDbName,
                       messages: String = defaultMessagesCollection,
                       topics: String  = defaultTopicsCollection,
                       users: String = defaultUsersCollection )

object MongoConfig {
  private val config = ConfigFactory.load()

  def load(): MongoConfig =
    MongoConfig(
      getString(host, defaultHost),
      getInt(port, defaultPort),
      getString(user, defaultUser),
      getString(password, defaultPassword),
      getString(dbname, defaultDbName),
      getString(messagesCollection, defaultMessagesCollection),
      getString(topicsCollection, defaultTopicsCollection),
      getString(usersCollection, defaultUsersCollection)
    )
  private def getString(key: String, defaultValue: String) = Try(config.getString(key)).getOrElse(defaultValue)
  private def getInt(key: String, defaultValue: Int) = Try(config.getInt(key)).getOrElse(defaultValue)
}

