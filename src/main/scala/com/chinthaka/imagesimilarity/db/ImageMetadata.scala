package com.chinthaka.imagesimilarity.db

/**
 * @author - Eran Withana (eran.chinthaka@gmail.com)
 */
case class ImageMetadata(uuid: String,
                        lowResHist: String,
                        highResHist: String) {

}

object ImageMetadata {
  val Schema = "images"
  val Table = "metadata"
  val UUID = "uuid"
  val LowResHist = "low_res_hist"
  val HighResHist = "high_res_hist"
  val Timestamp = "timestamp"
}
