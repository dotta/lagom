# Creating a new Lagom project without Activator

Create a new directory for your new Lagom project, and create the following files (and the necessary directories) in it: 

1) `project/plugins.sbt` with the following content:

```scala
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.0.0-M1")
```

2) To ensure the proper activator version is used, make sure you have the following in `project/build.properties`:

```scala
sbt.version=0.13.11
```

3) Create a `build.sbt` and add:

```scala
organization in ThisBuild := "<your organization, e.g., com.lightbend>"

// the Scala version that will be used for cross-compiled libraries (this is needed by all Lagom Java projects)
scalaVersion in ThisBuild := "2.11.7"
```

4) Create your first service API and implementation project. For instance, let's say you want to create a greetings service, then you should:

* Create a directory `greetings-api` with the following directory structure (this is the directory structure expected by sbt - see [here](http://www.scala-sbt.org/0.13/docs/Directories.html#Directory+structure)):

```
greetings-api            → API project root
 └ src                   → source folder
   └ main
     └ java              → place here your Java sources
     └ resources         → place here resource files such as application.conf, or logback.xml
 └ test                  → test folder
   └ main
     └ java              → place here your test Java sources
     └ resources         → place here resource files needed for running your tests
```

* Add the `greetings-api` in your `build.sbt`:

```scala
lazy val `greetings-api` = Project("greetings-api", file("greetings-api"))
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslApi
  )
```

* Create a directory `greetings-impl` with the same directory structure of `greetings-api`, and add the following in your `build.sbt`:

```scala
lazy val `greetings-impl` = Project("greetings-impl", file("greetings-impl"))
  .enablePlugins(LagomJava) // This is important!
  .settings(version := "1.0-SNAPSHOT")
  .dependsOn(`greetings-api`)
```

