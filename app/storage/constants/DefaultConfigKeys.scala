package storage.constants

object DefaultConfigKeys {
  val mongo = "mongo"
  val host = s"$mongo.host"
  val port = s"$mongo.port"
  val user = s"$mongo.user"
  val dbname = s"$mongo.dbname"
  val password = s"$mongo.password"
  val messagesCollection = s"$mongo.messages"
  val topicsCollection = s"$mongo.topics"
  val usersCollection = s"$mongo.users"
}
