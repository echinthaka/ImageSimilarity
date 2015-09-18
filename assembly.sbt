jarName in assembly := "imageservice.jar"

test in assembly := {}

mainClass in assembly := Some("com.chinthaka.imagesimilarity.server.JettyServer$")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case PathList(ps @ _*) if ps.last endsWith   "Log.class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith   "pom.properties" => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last endsWith   "Logger.class" => MergeStrategy.first
  case PathList("org", "slf4j", "impl", ps @ _*) => MergeStrategy.first
  case "application.conf" => MergeStrategy.concat
  case "META-INF/*.*"     => MergeStrategy.discard
  case x => old(x)
}
                                                          }