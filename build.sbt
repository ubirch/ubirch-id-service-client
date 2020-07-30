// see http://www.scala-sbt.org/0.13/docs/Parallel-Execution.html for details
concurrentRestrictions in Global := Seq(
  Tags.limit(Tags.Test, 1)
)

val commonSettings = Seq(

  scalaVersion := "2.11.12",
  organization := "com.ubirch.key",
  homepage := Some(url("http://ubirch.com")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/ubirch/ubirch-key-service"),
    "scm:git:git@github.com:ubirch/ubirch-key-service.git"
  )),
  version := "0.1.0-SNAPSHOT",
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases") //,
  )
)

/*
 * MODULES
 ********************************************************/

lazy val root = (project in file("."))
  .settings(
    name := "ubirch-id-service-client",
    description := "REST client of the id-service",
    commonSettings,
    skip in publish := true,
    libraryDependencies ++= depClientRest
  )

/*
 * MODULE DEPENDENCIES
 ********************************************************/

lazy val depClientRest = Seq(
  // https://mvnrepository.com/artifact/org.redisson/redisson
  redisson,
  ubirchDate,
  akkaHttp,
  akkaStream,
  akkaSlf4j,
  ubirchResponse,
  ubirchDeepCheckModel,
  ubirchUtilRedisUtil,
  ubirchUtilRedisTestUtil % "test",
  scalatest % "test",
  ubirchCrypto % "test"
) ++ scalaLogging


/*
 * DEPENDENCIES
 ********************************************************/

// VERSIONS
val akkaV = "2.5.11"
val akkaHttpV = "10.1.3"

val scalaTestV = "3.0.5"

val logbackV = "1.2.3"
val logbackESV = "1.5"
val slf4jV = "1.7.25"
val log4jV = "2.9.1"
val scalaLogV = "3.7.2"
val scalaLogSLF4JV = "2.1.2"

// GROUP NAMES
val ubirchUtilG = "com.ubirch.util"
val akkaG = "com.typesafe.akka"

val scalatest = "org.scalatest" %% "scalatest" % scalaTestV

val scalaLogging = Seq(
  "org.slf4j" % "slf4j-api" % slf4jV,
  "org.slf4j" % "log4j-over-slf4j" % slf4jV,
  "org.slf4j" % "jul-to-slf4j" % slf4jV,
  "ch.qos.logback" % "logback-core" % logbackV,
  "ch.qos.logback" % "logback-classic" % logbackV,
  "net.logstash.logback" % "logstash-logback-encoder" % "5.0",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % scalaLogSLF4JV,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLogV,
  "com.internetitem" % "logback-elasticsearch-appender" % logbackESV
)

val akkaHttp = akkaG %% "akka-http" % akkaHttpV
val akkaSlf4j = akkaG %% "akka-slf4j" % akkaV
val akkaStream = akkaG %% "akka-stream" % akkaV

val redisson = "org.redisson" % "redisson" % "3.7.5"

val excludedLoggers = Seq(
  ExclusionRule(organization = "com.typesafe.scala-logging"),
  ExclusionRule(organization = "org.slf4j"),
  ExclusionRule(organization = "ch.qos.logback")
)

val ubirchCrypto = "com.ubirch" % "ubirch-crypto" % "2.1.0" excludeAll (excludedLoggers: _*)
val ubirchDate = ubirchUtilG %% "date" % "0.5.3" excludeAll (excludedLoggers: _*)
val ubirchDeepCheckModel = ubirchUtilG %% "deep-check-model" % "0.3.1" excludeAll (excludedLoggers: _*)
val ubirchUtilRedisTestUtil = ubirchUtilG %% "redis-test-util" % "0.5.2"
val ubirchUtilRedisUtil = ubirchUtilG %% "redis-util" % "0.5.2"
val ubirchResponse = ubirchUtilG %% "response-util" % "0.5.0" excludeAll (excludedLoggers: _*)

/*
 * RESOLVER
 ********************************************************/

val resolverSeebergerJson = Resolver.bintrayRepo("hseeberger", "maven")
