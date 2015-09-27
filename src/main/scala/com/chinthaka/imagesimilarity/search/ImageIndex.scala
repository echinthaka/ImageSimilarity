package com.chinthaka.imagesimilarity.search

import com.chinthaka.imagesimilarity.db.ImageMetadata
import com.chinthaka.imagesimilarity.storage.ImageMetadataStorage
import com.chinthaka.imagesimilarity.util.GlobalContext
import collection.mutable.{HashMap, MultiMap}
import collection.immutable.{Set, Seq}
import net.sf.javaml.core.kdtree.KDTree

/**
 * @author - Eran Withana (ewithana@comprehend.com) 
 */
object ImageIndex {

  var nodeCount = 0
  val kdTree: KDTree = new KDTree(GlobalContext.numberOfBinsInHistogram)

  def update(newImageMetadata: ImageMetadata) {
    val attributes: Array[Double] = newImageMetadata.lowResHist.split(",").map(_.toDouble)
    kdTree.insert(attributes, newImageMetadata.uuid)
    nodeCount += 1
  }

  def getSimilarImages(baseImage: ImageMetadata, numberOfSimilarImages: Int): Set[String] = {

    val baseAttributes: Array[Double] = baseImage.lowResHist.split(",").map(_.toDouble)

    if (nodeCount == 0) {
      Set.empty
    } else {
      try {
        val itemsToRetrieve = if (nodeCount < numberOfSimilarImages) nodeCount else numberOfSimilarImages
        val simialrImages = kdTree.nearest(baseAttributes, numberOfSimilarImages).map(_.toString)
        simialrImages.filter(_ != baseImage.uuid).take(numberOfSimilarImages).toSet
      } catch {
        case e: Exception => e.printStackTrace()
        Set.empty
      }
    }
  }

}
