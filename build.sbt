name := "easysql-scala"

lazy val commonSettings = Seq(
    organization := "org.easysql",
    version := "0.1",
    scalaVersion := "3.1.2",
    scalacOptions += "-Yexplicit-nulls"
)

lazy val core = project.in(file("core")).settings(commonSettings)
lazy val jdbc = project.in(file("jdbc")).dependsOn(core).settings(commonSettings)
