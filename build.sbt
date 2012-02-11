import scalariform.formatter.preferences._

ScalariformKeys.preferences := FormattingPreferences().
  setPreference(DoubleIndentClassDeclaration, true).
  setPreference(PreserveDanglingCloseParenthesis, false).
  setPreference(MultilineScaladocCommentsStartOnFirstLine, true)

organization := "com.github.mumoshu.oauth"

name := "oauth"

scalacOptions += "-deprecation"

seq(scalariformSettings: _*)

libraryDependencies += "org.specs2" %% "specs2" % "1.7.1" % "test"

libraryDependencies += "commons-codec" % "commons-codec" % "1.6"
