package com.chinthaka.imagesimilarity.api

import com.chinthaka.imagesimilarity.common.FileHandlingService
import com.chinthaka.imagesimilarity.constants.DataStorageServiceConstants
import com.chinthaka.imagesimilarity.storage.FileBasedStorage
import org.scalatra.{BadRequest, Ok}
import org.slf4j.{LoggerFactory, Logger}
import spray.json.DefaultJsonProtocol._
import spray.json._

class DataStorageService extends FileHandlingService {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val imageStoreService = new FileBasedStorage

  post("/") {
              fileParams.get(DataStorageServiceConstants.InputFileParamName) match {
                case Some(inputFile) => {
                  logger.info("Got an image for image store service")
                  val id = imageStoreService.store(inputFile.get())
                  Ok(Map("id" -> id).toJson)
                }

                case None =>
                  BadRequest(<p>Hey! didn't see a file.</p>)
              }
            }

  get("/:id") {
                val imageToRetrieve = params("id").toString
                imageStoreService.retrieve(imageToRetrieve) match {
                  case Some(imageBytes) =>
                    Ok(imageBytes, Map(
                                        "Content-Type" -> "application/octet-stream",
                                        "Content-Disposition" -> "attachment"
                                      ))
                  case None =>
                    BadRequest(s"No image found with the id $imageToRetrieve")
                }
              }

}
