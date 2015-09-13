package com.chinthaka.imagesimilarity.api

import java.io.File
import java.nio.file.Files
import java.util.logging.Logger

import com.chinthaka.imagesimilarity.common.FileHandlingService
import com.chinthaka.imagesimilarity.constants.DataStorageServiceConstants
import com.chinthaka.imagesimilarity.constants.ImageStoreServiceConstants._
import com.chinthaka.imagesimilarity.core.ImageManager
import com.chinthaka.imagesimilarity.db.{ImageMetadata, DBManager}
import com.chinthaka.imagesimilarity.util.{GlobalContext, ImageComparator, FileUtils}
import org.scalatra.{BadRequest, Ok, InternalServerError}
import spray.json._
import spray.json.DefaultJsonProtocol._
import com.chinthaka.imagesimilarity.util.GlobalContext._


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
                                               .postMulti(MultiPart(DataStorageServiceConstants.InputFileParamName,
                                                                    "image.png", "image/png", inputFile.get)).asString

                    if (imageStorageResponse.is2xx) {

                      val id = imageStorageResponse.body.asJson.extract[String]('id)
                      logger.info(s"[ImageService] Image successfully stored with the id $id. Calculating histograms")

                      // save file locally and calculate histograms
                      val imageFile: File = new File("/tmp", s"${id}.png")
                      FileUtils.saveToFile(inputFile.get(), imageFile)

                      // calculate two histograms
                      val (lowResHist, highResHist) = ImageManager.calculateHistograms(imageFile.getAbsolutePath)
                      logger.info(s"[ImageService] Histograms calculated for image $id")

                      DBManager.insertImageMetadata(new ImageMetadata(id, lowResHist, highResHist))
                      logger.info(s"[ImageService] Image histogram data stored in DB for image $id")

                      Ok(Map("id" -> id).toJson)

                    } else {
                      val message = "Image couldn't be stored with the image storage service"
                      logger.warning(s"[ImageService] $message")
                      InternalServerError(reason = message)
                    }
                  } catch {
                    case exception: Exception => {
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
                                                 val baseImageMetadataOption: Option[List[ImageMetadata]] = DBManager.retrieveImagesWithProperty(ImageMetadata
                                                                                                                                                 .UUID,
                                                                                                                                                 baseImageId)

                                                 baseImageMetadataOption match {
                                                   case Some(baseImageMetaDataList) => {
                                                     val baseImageMetaData = baseImageMetaDataList.head
                                                     DBManager.retrieveImagesWithProperty(ImageMetadata.LowResHist, baseImageMetaData.lowResHist,
                                                                                          Some(ImageMetadata.UUID), Some(baseImageId)) match {
                                                       case Some(similarImagesList) => {
                                                         logger.info(s"[ImageService] Found ${similarImagesList.size} images that are similar at first level")

                                                         // first see whether we have a perfect match at high res histogram
                                                         val matchedImagesList = similarImagesList.filter(_.highResHist == baseImageMetaData.highResHist)

                                                         val matchedImageIdsList: List[String] = if (matchedImagesList.size >
                                                                                                     NumberOfSimilarImagesToBeReturned) {
                                                           findBestMatchesWithHistogramEvaluation(baseImageId, matchedImagesList.map(_.uuid), 3)
                                                         } else if (matchedImagesList.size < NumberOfSimilarImagesToBeReturned) {

                                                           if (matchedImagesList.size == similarImagesList) {
                                                             // sorry this is all we have
                                                             matchedImagesList.map(_.uuid)
                                                           } else {
                                                             val itemsMatchingOnlyAtLowResLevel = similarImagesList.filterNot(matchedImagesList.toSet)
                                                             matchedImagesList.map(_.uuid) ++ findBestMatchesWithHistogramEvaluation(baseImageId,
                                                                                                                                     itemsMatchingOnlyAtLowResLevel
                                                                                                                                     .map(_.uuid),
                                                                                                                                     NumberOfSimilarImagesToBeReturned -
                                                                                                                                     matchedImagesList.size)
                                                           }
                                                         } else {
                                                           // we have exactly NumberOfSimilarImagesToBeReturned images matched
                                                           matchedImagesList.map(_.uuid)
                                                         }

                                                         Ok(Map("similarImages" -> matchedImageIdsList.mkString(",")).toJson)

                                                       }
                                                       case _ => BadRequest(s"There are no similar images for the given image with id $baseImageId")
                                                     }
                                                   }
                                                   case _ => BadRequest(s"Couldn't find an image with the given id $baseImageId")
                                                 }
                                                 // find images with the same low res histogram


                                                 // now find out whether we have perfect matches with high res hist


                                               }
}
