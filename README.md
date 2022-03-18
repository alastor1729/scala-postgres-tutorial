# DoobieScalaPostgresTutorial
Basic example of using Http4s + Doobie + Postgres (local environment) combination with minimal implementations.

Forked from here:
https://github.com/emmettna/scalapostgrestutorial

(article that relates to above forked-repo: https://emmettna.medium.com/fastest-way-to-write-db-postgresql-68c204bdc68d)



// TODOs:
1) change hard-coded-values by creating Config.scala file
2) add "testContainers" library and use this library
3) ScalaCheck / or Specs2
4) Migrations ( FLYWAY ) - check "banno-business" project
   ** ex: Flyway Banno example (https://github.com/Banno/banno-business#banno-organizations-persistence)

PS, for now:
* No need for Jenkins / CICD stuff needed (for now)
* No need for ScalaFmt PlugIn
