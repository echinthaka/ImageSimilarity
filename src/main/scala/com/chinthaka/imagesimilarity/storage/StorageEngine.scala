package com.chinthaka.imagesimilarity.storage

abstract class StorageEngine[K, V] {

  /**
   * Stores the given content and returns a unique id that can be used to refer the saved content later
   * @param content
   * @return
   */
  def store(content: Array[Byte]): K

  /**
   * Retrieves a stored object if available
   *
   * @param id
   * @return
   */
  def retrieve(id: K): Option[Array[Byte]]

  def storeKeyValues(key: K, values: List[V])

  def retrieveKeyValues(key: K): List[V]

  def addToKey(key: K, value: V)


}
