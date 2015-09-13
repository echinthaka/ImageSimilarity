package com.chinthaka.imagesimilarity.util

import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.helper.{opencv_imgproc => imgproc}
import org.bytedeco.javacpp.indexer.FloatBufferIndexer
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._

/**
 * @author - Eran Withana (eran.chinthaka@gmail.com)
 */
class ImageComparator(imagePath: String, flags: Int = CV_LOAD_IMAGE_COLOR, numberOfBins: Int = 32) {

  val referenceImage = imread(imagePath, flags)
  val hist = new ColorHistogram()
  hist.numberOfBins = numberOfBins

  val referenceHistogram = hist.getHistogram(referenceImage)


  /**
   * Compare the reference image with the given input image and return similarity score.
   */
  def compare(image: Mat): Double = {
    val inputH = hist.getHistogram(image)
    compareHist(referenceHistogram, inputH, CV_COMP_INTERSECT)
  }
}
