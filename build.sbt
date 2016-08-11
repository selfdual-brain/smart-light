name := "philips-test"

organization := "assessment"

version := "1.0.0"

scalaVersion := "2.11.7"

// Twitter Server
resolvers += "twttr" at "https://maven.twttr.com/"

libraryDependencies ++= {
  val akkaVersion = "2.4.2"
  val scalaTestVersion = "2.2.3"
  val finatraVersion = "2.1.4"

  Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "ch.qos.logback" % "logback-classic" % "1.1.7",

    // Finatra Http
    "com.twitter.finatra" %% "finatra-http" % finatraVersion,
    "com.twitter.finatra" %% "finatra-httpclient" % finatraVersion,
    "com.twitter.finatra" %% "finatra-slf4j" % finatraVersion,
    "com.twitter" %% "finagle-stats" % "6.33.0" excludeAll (
      ExclusionRule("asm") // exclude because of conflict creating test report with PegDown
      ),

    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",

    // Test
    "org.scalatest" % "scalatest_2.11" % scalaTestVersion % "test",
    "org.pegdown" % "pegdown" % "1.4.2" % "test", // needed by scalatest for html report
    "org.scalacheck" %% "scalacheck" % "1.12.4" % "test", // needed by scalatest for property based tests
    "org.testng" % "testng" % "6.8.21" % "test",
    "org.hamcrest" % "hamcrest-all" % "1.3" % "test",
    "org.mockito" % "mockito-core" % "2.0.43-beta" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test",

    // Finatra test utils -- The Full Monty. All is needed.
    "com.twitter.finatra" %% "finatra-http" % finatraVersion % "test",
    "com.twitter.finatra" %% "finatra-jackson" % finatraVersion % "test",
    "com.twitter.inject" %% "inject-server" % finatraVersion % "test",
    "com.twitter.inject" %% "inject-app" % finatraVersion % "test",
    "com.twitter.inject" %% "inject-core" % finatraVersion % "test",
    "com.twitter.inject" %% "inject-modules" % finatraVersion % "test",
    "com.google.inject.extensions" % "guice-testlib" % "4.0" % "test",

    "com.twitter.finatra" %% "finatra-http" % finatraVersion % "test" classifier "tests",
    "com.twitter.finatra" %% "finatra-jackson" % finatraVersion % "test" classifier "tests",
    "com.twitter.inject" %% "inject-app" % finatraVersion % "test" classifier "tests",
    "com.twitter.inject" %% "inject-core" % finatraVersion % "test" classifier "tests",
    "com.twitter.inject" %% "inject-modules" % finatraVersion % "test" classifier "tests",
    "com.twitter.inject" %% "inject-server" % finatraVersion % "test" classifier "tests",
    "org.specs2" %% "specs2-mock" % "3.7.2" % "test"
  )

}