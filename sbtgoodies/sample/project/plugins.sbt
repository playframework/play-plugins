// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// SbtGoodies plugin
addSbtPlugin("com.typesafe" % "play-plugins-sbtgoodies" % "0.2")

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1-08072012")
