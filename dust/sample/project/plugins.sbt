// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Typesafe SNAPSHOT repository
resolvers += Resolver.url("Typesafe Ivy Snapshots Repository", url("http://repo.typesafe.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns)

// The Dust plugin
addSbtPlugin("com.typesafe" % "play-plugins-dust" % "1.4.1")

// Use the Play sbt plugin for Play projects
