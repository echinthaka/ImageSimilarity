package com.chinthaka.imagesimilarity.search

import com.chinthaka.imagesimilarity.db.ImageMetadata
import weka.core._
import weka.core.neighboursearch.KDTree

/**
 * @author - Eran Withana (ewithana@comprehend.com) 
 */
object KDTree {

  def insertIntoWeka(datapoints: List[ImageMetadata]): Instances = {

    // lets first find out how many attributes we need.

    val histogramBinsCount = if (datapoints.nonEmpty) datapoints.head.lowResHist.split(",").size else 16

    val attributes = new FastVector(histogramBinsCount)
    (0 to histogramBinsCount - 1).toStream.foreach(attrCount => {attributes.addElement(new Attribute(attrCount.toString, attrCount))})

    val wekaPoints = new Instances("ImageLowResHistogram", attributes, 0)

    datapoints.map(dataPoint => {
      val instance = new Instance(histogramBinsCount)
      dataPoint.lowResHist.split(",").map(_.toDouble).zipWithIndex.foreach({ case (value, index) => {
        instance.setValue(attributes.elementAt(index).asInstanceOf[Attribute], value)
      }
                                                                           })
      instance.setDataset(wekaPoints)
      wekaPoints.add(instance)
    })
    wekaPoints
  }

  def createKDTree(wekaPoints: Instances): KDTree = {
    val tree = new KDTree()

    try {
      tree.setInstances(wekaPoints)
      val df: EuclideanDistance = new EuclideanDistance(wekaPoints);
      df.setDontNormalize(true);

      tree.setDistanceFunction(df);
    } catch {
      case e: Exception => e.printStackTrace()
    }
    tree
  }

  def createKDTree(datapoints: List[ImageMetadata]): KDTree = {
    val wekaPoints = insertIntoWeka(datapoints)
    createKDTree(wekaPoints)
  }

  def createInstance(imageMetaData: ImageMetadata): Instance = {
    val histogramBinsCount = imageMetaData.lowResHist.split(",").size

    val attributes = new FastVector(histogramBinsCount)
    (0 to histogramBinsCount - 1).toStream.foreach(attrCount => {
      attributes.addElement(new Attribute(attrCount.toString, attrCount))
    })

    // Create empty instance with three attribute values
    val instance: Instance = new Instance(histogramBinsCount)
    imageMetaData.lowResHist.split(",").map(_.toDouble).zipWithIndex.foreach({ case (value, index) => {
      instance.setValue(attributes.elementAt(index).asInstanceOf[Attribute], value)
    }})

    instance
  }

}
