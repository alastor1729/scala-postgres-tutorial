// Scala
val CirceVersion   = "0.13.0"
val doobieVersion  = "0.8.8"
val fuuidVersion   = "0.5.0"
val Http4sVersion  = "0.21.1"
val LogbackVersion = "1.2.3"
val Specs2Version  = "4.8.3"

// Java
val jodaTimeVersion = "2.10.10"  // http://www.joda.org/joda-time/installation.html

lazy val root = (project in file("."))
  .settings(
    organization := "com.rwgs.scalapostgres",
    name := "tutorial",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      "ch.qos.logback"       %   "logback-classic"        % LogbackVersion,
      "io.chrisdavenport"    %%  "fuuid-circe"            % fuuidVersion,
      "io.chrisdavenport"    %%  "fuuid-doobie"           % fuuidVersion,
      "io.chrisdavenport"    %%  "fuuid-http4s"           % fuuidVersion,
      "io.circe"             %%  "circe-generic"          % CirceVersion,
      "io.circe"             %%  "circe-generic-extras"   % CirceVersion,
      "joda-time"            %   "joda-time"              % jodaTimeVersion,
      "org.http4s"           %%  "http4s-blaze-client"    % Http4sVersion,
      "org.http4s"           %%  "http4s-blaze-server"    % Http4sVersion,
      "org.http4s"           %%  "http4s-circe"           % Http4sVersion,
      "org.http4s"           %%  "http4s-dsl"             % Http4sVersion,
      "org.specs2"           %%  "specs2-core"            % Specs2Version % "test",
      "org.tpolecat"         %%  "doobie-core"            % doobieVersion,
      "org.tpolecat"         %%  "doobie-hikari"          % doobieVersion,
      "org.tpolecat"         %%  "doobie-postgres"        % doobieVersion
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")
  )

scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings",
)
