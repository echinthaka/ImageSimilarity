package com.chinthaka.imagesimilarity.storage

import java.io.{FileInputStream, File}
import java.nio.file.{Files, Paths}

import com.chinthaka.imagesimilarity.util.FileUtils

class FileBasedStorage extends StorageEngine[String, String] {

  private val storageFolder: String = "ImageStorage"

  Files.createDirectories(Paths.get(storageFolder))

  /**
   * Stores the given content and returns a unique id that can be used to refer the saved content later
   * @param content
   * @return
   */
  override def store(content: Array[Byte]): String = {
    val uuid = java.util.UUID.randomUUID.toString
    FileUtils.saveToFile(content, new File(storageFolder, s"${uuid}.png"))
    uuid
  }

  override def addToKey(key: String, value: String): Unit = ???

  /**
   * Retrieves a stored object if available
   *
   * @param id
   * @return
   */
  override def retrieve(id: String): Option[Array[Byte]] = {
    try {
      val is = new FileInputStream(new File(storageFolder, s"$id.png"))
      Option(Stream.continually(is.read).takeWhile(_ != -1).map(_.toByte).toArray)
    } catch {
      case e: Exception => println("Exception ", e)
        None
    }
  }

  override def storeKeyValues(key: String, values: List[String]): Unit = ???

  override def retrieveKeyValues(key: String): List[String] = ???
}
