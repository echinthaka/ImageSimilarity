package com.chinthaka.imagesimilarity.storage

import com.chinthaka.imagesimilarity.db.ImageMetadata

/**
 * @author - Eran Withana (ewithana@comprehend.com) 
 */
trait ImageMetadataStorage {

  /**
   * Retrieve all the new image metadata items since the last call to this method
   * @return
   */
  def retrieveNewImageMetadata: List[ImageMetadata]

  /**
   * Insert a new image metadata item to the database
   * @param imageMetadata
   */
  def insertImageMetadata(imageMetadata: ImageMetadata)

  def retrieveImagesWithProperty(propertyName: String, value: String, constraintName: Option[String] = None, constraintValue: Option[String] = None): Option[List[ImageMetadata]]

}
