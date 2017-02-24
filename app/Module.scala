import com.google.inject.AbstractModule

class Module extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[services.encryption.EncryptionService]).asEagerSingleton()
  }
}
