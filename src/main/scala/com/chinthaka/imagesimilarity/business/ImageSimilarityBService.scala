package com.chinthaka.imagesimilarity.business

import java.util.logging.Logger

import com.chinthaka.imagesimilarity.common.FileHandlingService
import com.chinthaka.imagesimilarity.constants.ImageStoreServiceConstants
import com.chinthaka.imagesimilarity.constants.DataStorageServiceConstants
import com.chinthaka.imagesimilarity.web.Template
import org.scalatra.servlet.SizeConstraintExceededException
import org.scalatra.{Ok, BadRequest, RequestEntityTooLarge, InternalServerError}
import spray.json.DefaultJsonProtocol._
import spray.json._
import spray.json.lenses.JsonLenses._

import scala.xml.Node
import scalaj.http.{Http, MultiPart}
import com.chinthaka.imagesimilarity.util.GlobalContext._

class ImageSimilarityBService extends FileHandlingService {

  val logger: Logger = Logger.getLogger(this.getClass.getName)

  def displayPage(content: Seq[Node]) = Template.page("Image Similarity Service", content, url(_))

  def makeFinalImage(baseImageId: String, firstSimilarImage: String, secondSimilarImage: String, thirdSimilarImage: String): Seq[Node] = {
    <p>Uploaded Image</p>
      <img src={baseImageId}/>
  }

  error {
          case e: SizeConstraintExceededException =>
            RequestEntityTooLarge(displayPage(
                                               <p>The file you uploaded exceeded the 10 MB limit.</p>))
        }


  /**
   * Serves the first interaction with this service.
   *
   * TODO: move the display page into a template inside WEB-INF
   */
  get("/") {
             val businessService = s"http://$hostName:$serverPort/business"
             displayPage(
                          <form action={businessService} method="post" enctype="multipart/form-data">
                            <p>File to upload:
                              <input type="file" name="file"/>
                            </p>
                            <p>
                              <input type="submit" value="Upload"/>
                            </p>
                          </form>
                          <p>
                            Upload a file using the above form. After you hit "Upload"
                            the file will be uploaded and your browser will start
                            downloading it.
                          </p>

                          <p>
                            The maximum file size accepted is 10 MB.
                          </p>)
           }

  /**
   * Gets the uploaded file and interacts with ImageStoreService and ImageSimilarityService to serve the business requirements
   *
   */
  post("/") {
              fileParams.get("file") match {
                case Some(file) => {

                  logger.info("[ImageSimilarityBService] Reeived an image. Storing it in image store")
                  val imageServiceResponse = Http(s"http://$hostName:$serverPort${ImageStoreServiceConstants.HTTPPath}")
                                             .postMulti(MultiPart(ImageStoreServiceConstants.InputFileParamName,
                                                                  "image.png", "image/png", file.get)).asString

                  if (imageServiceResponse.is2xx) {
                    // extract the unique id assigned to the image that just got uploaded
                    val id = imageServiceResponse.body.asJson.extract[String]('id)

                    logger.info(s"[ImageSimilarityBService] Image stored with the id $id. Retrieving similar images")

                    // ask for similar images from image similarity service
                    val imageSimServiceResponse = Http(s"http://$hostName:$serverPort${ImageStoreServiceConstants.HTTPPath}/${ImageStoreServiceConstants.SimilarImages}/$id").asString

                    if (imageSimServiceResponse.is2xx) {
                      val similarImagesList = imageSimServiceResponse.body.asJson.extract[String]('similarImages)
                      val similarImageURLs = if (similarImagesList.nonEmpty)
                                               similarImagesList.split(",").toList.map(uuid => s"http://$hostName:$serverPort${DataStorageServiceConstants.HTTPPath}/$uuid")
                                             else List.empty

                      logger.info(s"[ImageSimilarityBService] Found ${similarImageURLs.size} similar images with ids $similarImagesList")

                      val baseImageURL = s"http://$hostName:$serverPort${DataStorageServiceConstants.HTTPPath}/$id"

                      displayPage(
                                   <p>Uploaded Image</p>
                                     <img src={baseImageURL} height="250" width="250"/>
                                   <p>Similar Images</p>
                                   ++ {if (similarImageURLs.nonEmpty) similarImageURLs.map(similarImage => <img src={similarImage} height="250" width="250"/>).toSeq else <p>No similar images found</p>}
                                 )

                    } else {
                      displayPage(<p>Did not find any similar images</p>)
                    }

                  } else {

                    logger.warning(imageServiceResponse.body)
                    InternalServerError(reason = "Could not store image in image store service")
                  }

                }

                case None =>
                  BadRequest(displayPage(
                                          <p>
                                            Hey! You forgot to select a file.
                                          </p>))
              }
            }
}
