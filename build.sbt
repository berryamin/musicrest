import AssemblyKeys._ 

assemblySettings

jarName in assembly := "musicrest-2.10.2.jar"

organization  := "org.bayswater.musicrest"

version       := "0.1"

scalaVersion  := "2.10.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")


resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/",
  // for temporary Casbah
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies ++= Seq(
  "io.spray"            %   "spray-can"          % "1.2-M8",
  "io.spray"            %   "spray-routing"      % "1.2-M8",
  "io.spray"            %   "spray-caching"      % "1.2-M8",
  "io.spray"            %   "spray-testkit"      % "1.2-M8",
  "com.typesafe.akka"   %%  "akka-actor"         % "2.2.0-RC1",
  "com.typesafe.akka"   %%  "akka-testkit"       % "2.2.0-RC1",
  "org.scalaz"          %   "scalaz-core_2.10"   % "7.0.0",
  "org.mongodb"         %%  "casbah"             % "2.6.2",
  "net.liftweb"         %%  "lift-json"          % "2.5",
  "javax.mail"          %   "mail"               % "1.4",
  "org.specs2"          %%  "specs2"             % "1.14" % "test"
)

fork := true

javaOptions in run += "-Dconfig.file=/home/john/Development/Workspace/Spray/musicrest-2.10/conf/musicrest.conf"

javaOptions in test += "-Dconfig.file=/home/john/Development/Workspace/Spray/musicrest-2.10/conf/test.conf"

net.virtualvoid.sbt.graph.Plugin.graphSettings

seq(Revolver.settings: _*)
