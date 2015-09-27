package com.chinthaka.imagesimilarity.core

import java.util.logging.Logger

import com.chinthaka.imagesimilarity.util.{GlobalContext, Histogram1D}
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_core._

/**
 * @author - Eran Withana (eran.chinthaka@gmail.com)
 */

object ImageManager {

  val logger: Logger = Logger.getLogger(this.getClass.getName)

  def calculateHistograms(fileLocation: String, flags: Int = CV_LOAD_IMAGE_COLOR): (String, String) = {

    logger.info(s"Calculating histogram for $fileLocation")

    try {
      val image = imread(fileLocation, flags)
      val lowResHist = new Histogram1D(GlobalContext.numberOfBinsInHistogram).getHistogramAsArray(image)

      val highRestHist = new Histogram1D(256).getHistogramAsArray(image)

      (lowResHist.mkString(","), highRestHist.mkString(","))
    } catch {
      case e: Exception => {
        logger.severe("Exception occurred")
        e.printStackTrace
        throw e
      }
    }

  }

  def loadImage(fileLocation: String, flags: Int = CV_LOAD_IMAGE_COLOR): Mat = {
    imread(fileLocation, flags)
  }
}
