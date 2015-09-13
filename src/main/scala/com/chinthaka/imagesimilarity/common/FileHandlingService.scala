package com.chinthaka.imagesimilarity.common

import com.chinthaka.imagesimilarity.ImageServiceStack
import org.scalatra.{FlashMapSupport, ScalatraServlet}
import org.scalatra.servlet.{MultipartConfig, FileUploadSupport}

trait FileHandlingService extends ImageServiceStack with ScalatraServlet with FileUploadSupport with FlashMapSupport {

  configureMultipartHandling(MultipartConfig(maxFileSize = Some(10 * 1024 * 1024)))


}
