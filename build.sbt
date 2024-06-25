val isScala3 = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value).exists(_._1 == 3)
)

name := "sangria-json4s-native"
organization := "org.sangria-graphql"

// TODO: Reactivate after release (cf. https://github.com/json4s/json4s/compare/v3.6.11...v4.0.1)
// Some example:
// * method isDefined(org.json4s.JsonAST#JValue)Boolean in object
//     sangria.marshalling.json4s.native#Json4sNativeInputUnmarshaller in current version
//     does not have a correspondent with same parameter signature
//     among (org.json4s.JValue)Boolean, (java.lang.Object)Boolean
// * method fromResult(org.json4s.JsonAST#JValue)org.json4s.JsonAST#JValue in object
//     sangria.marshalling.json4s.native#Json4sNativeFromInput in current version
//     does not have a correspondent with same parameter signature
//     among (org.json4s.JValue)org.json4s.JValue, (java.lang.Object)java.lang.Object
// Set("org.sangria-graphql" %% "sangria-json4s-native" % "1.0.1")
mimaPreviousArtifacts := Set.empty

description := "Sangria json4s-native marshalling"
homepage := Some(url("https://sangria-graphql.github.io/"))
licenses := Seq(
  "Apache License, ASL Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / crossScalaVersions := Seq("2.12.19", "2.13.14", "3.3.3")
ThisBuild / scalaVersion := crossScalaVersions.value.last
ThisBuild / githubWorkflowPublishTargetBranches := List()
ThisBuild / githubWorkflowBuildPreamble ++= List(
  WorkflowStep.Sbt(List("mimaReportBinaryIssues"), name = Some("Check binary compatibility")),
  WorkflowStep.Sbt(List("scalafmtCheckAll"), name = Some("Check formatting"))
)

scalacOptions ++= Seq("-deprecation", "-feature")
javacOptions ++= Seq("-source", "8", "-target", "8")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria-marshalling-api" % "1.0.8",
  "org.json4s" %% "json4s-native-core" % "4.0.7",
  "org.sangria-graphql" %% "sangria-marshalling-testkit" % "1.0.4" % Test,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)

// Release
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

// Site and docs
enablePlugins(SiteScaladocPlugin)
enablePlugins(GhpagesPlugin)
git.remoteRepo := "git@github.com:org.sangria-graphql/sangria-json4s-native.git"

// nice *magenta* prompt!
ThisBuild / shellPrompt := { state =>
  scala.Console.MAGENTA + Project.extract(state).currentRef.project + "> " + scala.Console.RESET
}

// Additional meta-info
startYear := Some(2016)
organizationHomepage := Some(url("https://github.com/sangria-graphql"))
developers := Developer(
  "OlegIlyenko",
  "Oleg Ilyenko",
  "",
  url("https://github.com/OlegIlyenko")) :: Nil
scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/sangria-graphql/sangria-json4s-native"),
    connection = "scm:git:git@github.com:sangria-graphql/sangria-json4s-native.git"
  ))
