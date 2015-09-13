import com.mojolly.scalate.ScalatePlugin.ScalateKeys._
import com.mojolly.scalate.ScalatePlugin._
import org.scalatra.sbt._
import sbt.Keys._
import sbt._

object ImageSimilarityServiceBuild extends Build {
  val Organization = "org.chinthaka"
  val Name = "ImageSimilarityService"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.10.4"
  val ScalatraVersion = "2.4.0.M3"

  lazy val project = Project (
    "ImageSimilarityServiceBuild",
    file("."),
    settings = ScalatraPlugin.scalatraSettings ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers ++= Seq("Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      "JavaCV maven repo" at "http://maven2.javacv.googlecode.com/git/"),
      classpathTypes += "maven-plugin",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.2" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.2.10.v20150310" % "container;compile",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
        "org.scalaj" %% "scalaj-http" % "1.1.5",
        "io.spray" %%  "spray-json" % "1.3.2",
      // JavaCV Dependencies
      "org.bytedeco" % "javacv" % "1.0",
      "net.virtual-void" %%  "json-lenses" % "0.6.0",
      "org.postgresql" % "postgresql" % "9.4-1200-jdbc4",
      "com.typesafe" % "config" % "1.3.0"

      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  )
}
