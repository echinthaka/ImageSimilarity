import javax.servlet.ServletContext

import com.chinthaka.imagesimilarity.api.{DataStorageService, ImageService}
import com.chinthaka.imagesimilarity.business.ImageSimilarityBService
import com.chinthaka.imagesimilarity.constants.{DataStorageServiceConstants, ImageStoreServiceConstants}
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    context.mount(new ImageSimilarityBService, "/business/*")
    context.mount(new ImageService, s"${ImageStoreServiceConstants.HTTPPath}/*")
    context.mount(new DataStorageService, s"${DataStorageServiceConstants.HTTPPath}/*")
  }
}
