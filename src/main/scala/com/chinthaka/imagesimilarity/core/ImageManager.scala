package com.chinthaka.imagesimilarity.core

import com.chinthaka.imagesimilarity.util.Histogram1D
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_core._

/**
 * @author - Eran Withana (eran.chinthaka@gmail.com)
 */
class ImageManager {


}

object ImageManager {

  def calculateHistograms(fileLocation: String, flags: Int = CV_LOAD_IMAGE_COLOR): (String, String) = {

    val image = imread(fileLocation, flags)
    val lowResHist = new Histogram1D(16).getHistogramAsArray(image)

    val highRestHist = new Histogram1D(256).getHistogramAsArray(image)

    (lowResHist.mkString(","), highRestHist.mkString(","))

  }

  def loadImage(fileLocation: String, flags: Int = CV_LOAD_IMAGE_COLOR): Mat = {
    imread(fileLocation, flags)
  }
}
