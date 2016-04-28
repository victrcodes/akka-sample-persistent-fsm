name := "akka-sample-persistent-fsm"

version := "1.0"

scalaVersion := "2.11.8"

mainClass := Some("PersistentFSMExample")

fork := true

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.4.0",
	"com.typesafe.akka" %% "akka-persistence" % "2.4.0",
	"org.iq80.leveldb" % "leveldb" % "0.7",
	"org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
)