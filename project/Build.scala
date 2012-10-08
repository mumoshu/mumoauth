import com.typesafe.sbtscalariform.ScalariformPlugin.ScalariformKeys
import com.typesafe.sbtscalariform.ScalariformPlugin._
import sbt._
import Keys._
import PlayProject._
import scalariform.formatter.preferences._

object ProjectBuild extends Build {

  lazy val main = Project("mumoauth", file(".")).settings(
    ScalariformKeys.preferences := FormattingPreferences().
      setPreference(DoubleIndentClassDeclaration, true).
      setPreference(PreserveDanglingCloseParenthesis, false).
      setPreference(MultilineScaladocCommentsStartOnFirstLine, true),

    organization := "com.github.mumoshu.oauth",
    name := "oauth",
    scalacOptions += "-deprecation",
    libraryDependencies += "org.specs2" %% "specs2" % "1.9" % "test",
    libraryDependencies += "commons-codec" % "commons-codec" % "1.6"
  ).settings(
    scalariformSettings: _*
  )

  val appDependencies = Seq()

  lazy val play =   PlayProject("example", "0.1-SNAPSHOT", appDependencies, path = file("play2-example"), mainLang = SCALA).settings(
  ).dependsOn(main)


}
