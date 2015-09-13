package com.chinthaka.imagesimilarity.test


import java.io.{File, FileNotFoundException, IOException}

import com.chinthaka.imagesimilarity.util.{ColorHistogram, Histogram1D}
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgcodecs._
object OpenCVTester {

  private val colorReductionFactor = 32
  private val hist = new ColorHistogram()

  def main(args: Array[String]) {

//    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    val referenceImageFile = new File("/Users/eran/Downloads/ImageSearch/108103_sm.jpg")
    val reference = loadOrExit(referenceImageFile, CV_LOAD_IMAGE_COLOR)

    val hist = new Histogram1D(16)
    val histArray: Array[Float] = hist.getHistogramAsArray(reference)
    println(histArray.mkString(","))

  }

  def loadOrExit(file: File, flags: Int = CV_LOAD_IMAGE_GRAYSCALE): Mat = {
    // Verify file
    if (!file.exists()) {
      throw new FileNotFoundException("Image file does not exist: " + file.getAbsolutePath)
    }
    // Read input image
    val image = imread(file.getAbsolutePath, flags)
    if (image == null) {
      throw new IOException("Couldn't load image: " + file.getAbsolutePath)
    }
    image
  }

}
