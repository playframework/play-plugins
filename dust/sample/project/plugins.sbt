// Comment to get more information during initialization
logLevel := Level.Warn

// The Dust plugin
addSbtPlugin("com.typesafe" % "play-plugins-dust" % "1.0-SNAPSHOT")

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.0-SNAPSHOT")
