package com.chinthaka.imagesimilarity

import com.chinthaka.imagesimilarity.business.ImageSimilarityBService
import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class BusinessLayerServletSpec extends ScalatraSpec { def is =
  "GET / on BusinessLayerServlet"                     ^
    "should return status 200"                  ! root200^
                                                end

  addServlet(classOf[ImageSimilarityBService], "/*")

  def root200 = get("/") {
    status must_== 200
  }
}
