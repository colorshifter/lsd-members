
val scalaV = "2.11.8"
val akkaV = "2.4.9-RC2"
val scaldiV = "0.5.7"
val metricsV = "3.1.0"

organization  := "com.jsherz"

name          := "luskydive"

version       := "0.0.0.1"

scalaVersion  := scalaV

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  Seq(
    // Utils
    "com.google.guava" %  "guava"       % "19.0",
    "org.scalaz"       %% "scalaz-core" % "7.2.4",

    // DI
    "org.scaldi"         %% "scaldi"          % scaldiV,
    "org.scaldi"         %% "scaldi-akka"     % scaldiV,

    // Web framework
    "com.typesafe.akka" %% "akka-actor"                        % akkaV,
    "com.typesafe.akka" %% "akka-http-core"                    % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit"                 % akkaV,
    "com.typesafe.akka" %% "akka-remote"                       % akkaV,
    "com.typesafe.akka" %% "akka-slf4j"                        % akkaV,
    "com.typesafe.akka" %% "akka-testkit"                      % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental"            % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,

    "io.dropwizard.metrics" % "metrics-core" % metricsV,
    "io.dropwizard.metrics" % "metrics-healthchecks" % metricsV,

    // Logging
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "org.slf4j"      % "slf4j-api"       % "1.7.21",

    // DB
    "org.postgresql"     %  "postgresql"  % "9.4.1208",
    "org.flywaydb"       %  "flyway-core" % "4.0.3",
    "com.typesafe.slick" %% "slick"       % "3.1.1",
    "com.zaxxer"         %  "HikariCP"    % "2.4.7",

    // Phone number parsing
    "com.googlecode.libphonenumber" % "libphonenumber" % "7.4.1",

    // Testing frameworks
    "org.scalactic" %% "scalactic"    % "2.2.6",
    "org.scalatest" %% "scalatest"    % "2.2.6"   % "test",
    "org.mockito"   %  "mockito-core" % "1.10.19" % "test",

    // Utils
    "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",

    // Misc (version conflicts)
    "org.scala-lang"         %  "scala-reflect"  % scalaV,
    "org.scala-lang"         %  "scala-compiler" % scalaV,
    "org.scala-lang"         %  "scala-library"  % scalaV,
    "org.scala-lang.modules" %% "scala-xml"      % "1.0.4"
  )
}

lazy val IntegrationTest = config("it") extend Test

Revolver.settings

wartremoverErrors ++= Warts.all
