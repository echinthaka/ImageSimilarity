package com.chinthaka.imagesimilarity.api

import java.io.File
import java.nio.file.Files
import java.util.logging.Logger

import com.chinthaka.imagesimilarity.common.FileHandlingService
import com.chinthaka.imagesimilarity.constants.DataStorageServiceConstants
import com.chinthaka.imagesimilarity.constants.ImageStoreServiceConstants._
import com.chinthaka.imagesimilarity.core.ImageManager
import com.chinthaka.imagesimilarity.db.{ImageMetadata, PostgresImageMetadataStorage}
import com.chinthaka.imagesimilarity.search.ImageIndex
import com.chinthaka.imagesimilarity.util.{GlobalContext, ImageComparator, FileUtils}
import org.scalatra.{BadRequest, Ok, InternalServerError}
import spray.json._
import spray.json.DefaultJsonProtocol._
import com.chinthaka.imagesimilarity.util.GlobalContext._
import com.chinthaka.imagesimilarity.storage.ImageMetadataStorage

import spray.json.lenses.JsonLenses._

import scalaj.http.{Http, MultiPart}

class ImageService extends FileHandlingService {

  val NumberOfSimilarImagesToBeReturned = GlobalContext.config.getInt("app.business.similarImagesCount")
  val logger: Logger = Logger.getLogger(this.getClass.getName)


  post("/") {
              fileParams.get(InputFileParamName) match {
                case Some(inputFile) => {

                  try {
                    logger.info("[ImageService] Got an image for image service. First storing it")
                    // Store the image using data storage service
                    // TOOD: Fix localhost:8080 and get it either from storage service or request
                    val imageStorageResponse = Http(s"http://$hostName:$serverPort${DataStorageServiceConstants.HTTPPath}")
                                               .postMulti(MultiPart(DataStorageServiceConstants.InputFileParamName, "image.png", "image/png", inputFile.get))
                                               .asString

                    if (imageStorageResponse.is2xx) {

                      val id = imageStorageResponse.body.asJson.extract[String]('id)
                      logger.info(s"[ImageService] Image successfully stored with the id $id. Calculating histograms")

                      // save file locally and calculate histograms
                      val imageFile: File = new File("/tmp", s"${id}.png")
                      FileUtils.saveToFile(inputFile.get(), imageFile)
                      logger.info(s"File saved locally to calculate histogram => ${imageFile.getAbsolutePath}")

                      // calculate two histograms
                      val (lowResHist, highResHist) = ImageManager.calculateHistograms(imageFile.getAbsolutePath)
                      logger.info(s"[ImageService] Histograms calculated for image $id")

                      val newImageMetadata: ImageMetadata = new ImageMetadata(id, lowResHist, highResHist)
                      ImageService.imageMetadataStorage.insertImageMetadata(newImageMetadata)
                      ImageIndex.update(newImageMetadata)
                      logger.info(s"[ImageService] Image histogram data stored in DB for image $id")

                      Ok(Map("id" -> id).toJson)

                    } else {
                      val message = "Image couldn't be stored with the image storage service"
                      logger.warning(s"[ImageService] $message")
                      InternalServerError(reason = message)
                    }
                  } catch {
                    case exception: Exception => {
                      exception.printStackTrace()
                      logger.warning(s"Exception occurred => $exception")
                      InternalServerError(reason = exception.getMessage)
                    }
                  }
                }

                case None =>
                  BadRequest(<p>Hey! didn't see a file.</p>)
              }
            }

  def findBestMatchesWithHistogramEvaluation(baseImageUUID: String, matchedImageIds: List[String], itemsToBeRetrieved: Int): List[String] = {

    if (matchedImageIds.size <= itemsToBeRetrieved) {
      matchedImageIds
    } else {
      // for now, lets bring down all the images here, re-built the histogram and compare with one another. Need to find out a way to save the histogram and use
      // that for comparison instead of working with images again.

      // create a temporary directory
      val tempFolderPath = Files.createTempDirectory(baseImageUUID)
      val tempFolder = tempFolderPath.toFile

      // download base file
      val baseImageURL = s"http://$hostName:$serverPort${DataStorageServiceConstants.HTTPPath}/$baseImageUUID"
      val baseImage: File = new File(tempFolder, s"$baseImageUUID.png")
      FileUtils.downloadFile(baseImageURL, baseImage)
      val imageComparator: ImageComparator = new ImageComparator(baseImage.getAbsolutePath)

      // download files to compare and compare those. We will get List[(imageId, similarityScore)] at the end
      val matchedImages: List[(String, Double)] = matchedImageIds.map(matchedImageId => {

        val imageURL = s"http://$hostName:$serverPort${DataStorageServiceConstants.HTTPPath}/$matchedImageId"
        val matchedImage: File = new File(tempFolder, s"$matchedImageId.png")
        FileUtils.downloadFile(baseImageURL, matchedImage)

        // now compare
        val similarityScore: Double = imageComparator.compare(ImageManager.loadImage(matchedImage.getAbsolutePath))
        logger.info(s"$baseImageUUID -> $matchedImageId = $similarityScore")

        (matchedImageId, similarityScore)
      })

      // now get the top element
      matchedImages.sortWith(_._2 > _._2).take(itemsToBeRetrieved).map(_._1)
    }

  }

  get(s"/${SimilarImages}/:$BaseImageIdParam") {
                                                 val baseImageId = params(BaseImageIdParam).toString
                                                 logger.info(s"[ImageService] Looking for similar images for image $baseImageId. Retrieving image metadata")

                                                 // first retrieve image metadata for the base image
                                                 val baseImageMetadataOption: Option[List[ImageMetadata]] = ImageService.imageMetadataStorage
                                                                                                            .retrieveImagesWithProperty(ImageMetadata.UUID,
                                                                                                                                        baseImageId)
                                                 baseImageMetadataOption match {
                                                   case Some(baseImageMetaDataList) => {
                                                     val baseImageMetaData = baseImageMetaDataList.head

                                                     val similarImages: Set[String] = ImageIndex.getSimilarImages(baseImageMetaData,
                                                                                                                  NumberOfSimilarImagesToBeReturned)
                                                     logger.info(s"[ImageService] Found ${similarImages.size} images that are similar to ${baseImageId}")

                                                     if (similarImages.size > 0) {
                                                       Ok(Map("similarImages" -> similarImages.mkString(",")).toJson)
                                                     } else {
                                                       BadRequest(s"There are no similar images for the given image with id $baseImageId")
                                                     }
                                                   }

                                                   case _ => BadRequest(s"Couldn't find an image with the given id $baseImageId")
                                                 }
                                               }
}

object ImageService {
  val imageMetadataStorage: ImageMetadataStorage = PostgresImageMetadataStorage
  val initialImageMetadata: List[ImageMetadata] = imageMetadataStorage.retrieveNewImageMetadata
  initialImageMetadata.map(ImageIndex.update)
}
