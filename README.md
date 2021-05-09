# BannoScalaPostgresTutorial
Basic example of using Http4s + Doobie + Postgres (local environment) combination with minimal implementations.

Forked from here:
https://github.com/emmettna/scalapostgrestutorial

(article that relates to above forked-repo: https://emmettna.medium.com/fastest-way-to-write-db-postgresql-68c204bdc68d)



// TODOs:
1) change hard-coded-values by creating Config.scala file
2) add "testContainers" library
3) ScalaCheck / or Specs2
4) Migrations ( FLYWAY ) - check "banno-business" project
   4a) follow Flyway Banno example (https://github.com/Banno/banno-business#banno-organizations-persistence)


PS,
Skunk may replace Doobie (in near future)?
** team-based-decision

PPS,
* No need for Jenkins / CICD stuff needed (for now)
* No need for ScalaFmt PlugIn
* maybe need to add Jabberwocky as "CodeOwners"
