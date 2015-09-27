package com.chinthaka.imagesimilarity.search

import java.io.File

import com.chinthaka.imagesimilarity.core.ImageManager
import com.chinthaka.imagesimilarity.db.ImageMetadata
import org.specs2.mutable._

/**
 * @author - Eran Withana (ewithana@comprehend.com) 
 */
object ImageIndexTest extends Specification {
  val testImageFolder = new File("src/test/resources/initial_images")

  "Image Index" should {

    // get all images from the test images
    val initialImages: Map[String, ImageMetadata] = initializeImageIndex


    "should retrieve proper similar images" in {

      // now find the similar images for the boat image, we should find 3
      val boatImageMetadata: ImageMetadata = initialImages("boat_1.png")

      val similarImagesToBoatImage: Set[String] = ImageIndex.getSimilarImages(boatImageMetadata, 3)
      similarImagesToBoatImage.size must be equalTo 3
      similarImagesToBoatImage.contains(initialImages("boat_2.png").uuid) must be equalTo true
      similarImagesToBoatImage.contains(initialImages("boat_3.png").uuid) must be equalTo true
      similarImagesToBoatImage.contains(initialImages("beach.png").uuid) must be equalTo true

      // now find similar images for the pyramid image, we should find 3 since it gets from what it has
      val pyramidImage = new File(testImageFolder, "pyramid.png")
      val pyramidImageMetadata: ImageMetadata = createImageMetadataFromFile(pyramidImage)

      val similarImagesToPyramidImage: Set[String] = ImageIndex.getSimilarImages(pyramidImageMetadata, 3)
      similarImagesToPyramidImage.size must be equalTo 3
    }
  }

  def createImageMetadataFromFile(testImageFile: File): ImageMetadata = {
    val (lowResHist, highResHist) = ImageManager.calculateHistograms(testImageFile.getAbsolutePath)
    val uuid = java.util.UUID.randomUUID.toString
    new ImageMetadata(uuid, lowResHist, highResHist)
  }

  def initializeImageIndex: Map[String, ImageMetadata] = {
    testImageFolder.listFiles.map(imageFile => {
      val (lowResHist, highResHist) = ImageManager.calculateHistograms(imageFile.getAbsolutePath)
      val uuid = java.util.UUID.randomUUID.toString
      println(s"$uuid -> ${imageFile.getName}")
      val newImageMetadata: ImageMetadata = new ImageMetadata(uuid, lowResHist, highResHist)
      ImageIndex.update(newImageMetadata)
      imageFile.getName -> newImageMetadata
    }).toMap
  }

}
