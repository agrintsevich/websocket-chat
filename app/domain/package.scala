import java.util.Date

package object domain {
  case class User(name: String, topics: Seq[Topic] = Seq.empty[Topic])

  case class Message(text: String, date: Date, user: User, topic: Option[Topic] = None)

  case class Topic(name: String, createdBy: User, dateCreated: Date)

}
