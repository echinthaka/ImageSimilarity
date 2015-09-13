package com.chinthaka.imagesimilarity.server

import com.chinthaka.imagesimilarity.util.GlobalContext
import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyServer {
  // this is my entry object as specified in sbt project definition
  def main(args: Array[String]) {

    val port = GlobalContext.config.getInt("app.server.port")

    if (args.length > 0) {
      GlobalContext.hostName = args(0)
    }

    val server = new Server(port)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start
    server.join
  }
}


