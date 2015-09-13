package com.chinthaka.imagesimilarity.util

import java.io.{File, FileOutputStream, BufferedOutputStream}
import java.net.URL
import java.nio.channels.{Channels, ReadableByteChannel}

/**
 * @author - Eran Withana (eran.chinthaka@gmail.com)
 */
object FileUtils {

  def saveToFile(fileContent: Array[Byte], location: File) {
    val bos = new BufferedOutputStream(new FileOutputStream(location))
    Stream.continually(bos.write(fileContent))
    bos.close()
  }

  def downloadFile(url: String, file: File) {
    val locationURL: URL = new URL(url);
    val rbc: ReadableByteChannel = Channels.newChannel(locationURL.openStream());
    val fos: FileOutputStream = new FileOutputStream(file);
    fos.getChannel().transferFrom(rbc, 0, Long.MaxValue);
  }

}
