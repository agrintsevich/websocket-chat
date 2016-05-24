package storage

object StorageService  {
  def getStorage = {
    implicit val mongoContext = new MongoContext(new MongoConfig())

    val storage = new StorageComponentImpl {
      override val storage: Storage = new StorageImpl
    }.storage
    storage
  }
}
