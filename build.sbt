name := "finch-skeleton"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finch-core" % "0.17.0",
  "com.github.finagle" %% "finch-generic" % "0.17.0",
  "com.github.finagle" %% "finch-circe" % "0.17.0",
  "io.circe" %% "circe-core" % "0.9.1",
  "io.circe" %% "circe-generic" % "0.9.1",
  "io.circe" %% "circe-parser" % "0.9.1",
  "com.twitter" %% "twitter-server" % "18.2.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0" classifier "models",
)