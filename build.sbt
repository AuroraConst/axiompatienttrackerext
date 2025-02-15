import sbt._
import sbt.Keys._
import org.scalajs.linker.interface.ModuleSplitStyle
import scala.sys.process._ // Add this import


lazy val copyFileJSArtifactsToMedia = taskKey[Unit]("Copies a file from source to destination")
lazy val viteMainJSFolder = taskKey[File]("file location of main.js and main.js.map from target directory of the viteview project")
lazy val extensionMediaFolder = taskKey[File]("file folder location of the media folder of the extension")

extensionMediaFolder := {
  baseDirectory.value / "media"
}

copyFileJSArtifactsToMedia := {

    // val sourceFile = baseDirectory.value / "src" / "main" / "resources" / "source.txt"
    val fastOptFile = (Compile / fastLinkJSOutput ).value // / "target" / "destination.txt"
    val sourceMain =  (viteview / viteMainJSFolder).value / "main.js"
    val sourceMainMap = (viteview / viteMainJSFolder).value / "main.js.map"
    val destinationMain = baseDirectory.value / "media" / "main.js"
    val destinationMainMap = baseDirectory.value / "media" / "main.js.map"

    println(s"sourceMain $sourceMain")
    println(s"sourceMainMap $sourceMainMap")
    println(s"*****")

    println(s"destinationMain $destinationMain")
    println(s"destinationMainMap $destinationMainMap")

    IO.copyFile(sourceMain, destinationMain)
    IO.copyFile(sourceMainMap, destinationMainMap)
  }

lazy val installDependencies = Def.task[Unit] {
  val base = (ThisProject / baseDirectory).value
  val log = (ThisProject / streams).value.log
  if (!(base / "node_module").exists) {
    val pb =
      new java.lang.ProcessBuilder("npm.cmd", "install")
        .directory(base)
        .redirectErrorStream(true)

    pb ! log
  }
}

lazy val open = taskKey[Unit]("open vscode")
def openVSCodeTask: Def.Initialize[Task[Unit]] =
  Def
    .task[Unit] {
      val base = baseDirectory.value
      val log = streams.value.log

      val path = base.getCanonicalPath
      val pathOut = baseDirectory.value / "out" 
      println(s"Path: $path")
      println(s"PathOut: $pathOut")
      s"code.cmd  --extensionDevelopmentPath=$path" ! log
      ()
    }.dependsOn(copyFileJSArtifactsToMedia)
    // .dependsOn(installDependencies)

val commonSettings = Seq(
  scalaVersion := DependencyVersions.scala,
  // CommonJS
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) 
    .withModuleSplitStyle(
      ModuleSplitStyle.SmallModulesFor(List("viteview")))
  },
  scalacOptions ++=  Seq("-Yretain-trees",//necessary in zio-json if any case classes have default parameters
    "-Xmax-inlines","60",
    "-Wunused:imports"), //setting max inlines to accomodate > 32 fields in case classes

  libraryDependencies  ++= Dependencies.upickle.value,
  libraryDependencies ++= Dependencies.borerJson.value,
  libraryDependencies ++= Dependencies.scalajsmacrotaskexecutor.value,

)    


lazy val viteview = project
  .in(file("modules/viteview"))
  .settings(commonSettings,
  libraryDependencies ++= Dependencies.laminar.value,
  libraryDependencies ++= Dependencies.shapeless3.value,
  libraryDependencies ++= Dependencies.aurorajslibs.value,
  libraryDependencies ++= Dependencies.scalatest.value,
  // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,
  viteMainJSFolder := {
    val sourceMain = (Compile / fastLinkJSOutput ).value 
    println(s"sourceMain $sourceMain")
    sourceMain
  },
  externalNpm := baseDirectory.value,
  

  
  )
  .enablePlugins(
    ScalaJSPlugin,
    ScalablyTypedConverterExternalNpmPlugin,
    
  )


lazy val root = project
  .in(file("."))
  .settings(
    commonSettings,
    moduleName := "cat", //TODO change to $name$
    Compile / fastOptJS / artifactPath := baseDirectory.value / "out" / "extension.js",
    Compile / fullOptJS / artifactPath := baseDirectory.value / "out" / "extension.js",
    open := openVSCodeTask.dependsOn(Compile / fastOptJS).value,
        // CommonJS
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },

    // Tell ScalablyTyped that we manage `npm install` ourselves
    externalNpm := baseDirectory.value,

    libraryDependencies  ++= Dependencies.upickle.value,

    // publishMarketplace := publishMarketplaceTask.dependsOn(fullOptJS in Compile).value
  )
  .enablePlugins(
    ScalaJSPlugin,
    ScalablyTypedConverterExternalNpmPlugin
  )
  .dependsOn(viteview)
