/*
    Oubliette, version 0.4.0. Copyright 2022-23 Jon Pretty, Propensive OÜ.

    The primary distribution site is: https://propensive.com/

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
    file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions
    and limitations under the License.
*/

package oubliette

import guillotine.*
import galilei.*, filesystems.unix
import anticipation.*, integration.galileiPath
import serpentine.*
import imperial.*
import rudiments.*, environments.system
import turbulence.*
import parasitism.*
import gossamer.*, encodings.Utf8
import kaleidoscope.*
import eucalyptus.*
import gastronomy.*

object Jvm:
  given Show[Jvm] = jvm => t"ʲᵛᵐ"+jvm.pid.show.drop(3)

class Jvm(funnel: Funnel[Text], task: Task[Unit], process: Process[Text]) extends Shown[Jvm]:
  def addClasspath[T](path: T)(using pi: GenericPathReader[T]): Unit =
    funnel.put(t"path\t${pi.getPath(path)}\n")
  
  def addArg(arg: Text): Unit = funnel.put(t"arg\t$arg\n")
  def setMain(main: Text): Unit = funnel.put(t"main\t$main\n")
  def start(): Unit = funnel.stop()
  def pid: Pid = process.pid
  def await(): ExitStatus = process.exitStatus()
  def preload(classes: List[Text]): Unit = classes.foreach { cls => funnel.put(t"load\t$cls") }
  def stderr(): DataStream = process.stderr()
  def stdout(): DataStream = process.stdout()
  def stdin(in: DataStream): Unit throws StreamCutError = process.stdin(in)
  def abort(): Unit = funnel.put(t"exit\t2\n")
    
given oubliette: Realm(t"oubliette")

object Jdk:
  given Show[Jdk] = jdk => t"ʲᵈᵏ｢${jdk.version}:${jdk.base.path.fullname}｣"

case class Jdk(version: Int, base: Directory[Unix]) extends Shown[Jdk]:

  private lazy val javaBin: File[Unix] throws IoError = (base / p"bin" / p"java").file(Expect)

  def launch[P: GenericPathReader]
            (classpath: List[P], main: Text, args: List[Text])
            (using Log, Monitor, Threading, Classpath)
            : Jvm throws IoError | StreamCutError | EnvError | ClasspathRefError =
    val jvm: Jvm = init()
    classpath.foreach(jvm.addClasspath(_))
    jvm.setMain(main)
    args.foreach(jvm.addArg(_))
    jvm.start()
    jvm

  def init()(using log: Log, monitor: Monitor, classpath: Classpath, threading: Threading)
          : Jvm throws IoError | StreamCutError | EnvError | ClasspathRefError =
    val runDir: DiskPath[Unix] = Xdg.Run.User.current()
    
    val base: Directory[Unix] = (runDir / p"oubliette").directory(Ensure)
    val classDir: Directory[Unix] = (base / p"_oubliette").directory(Ensure)
    val classfile: DiskPath[Unix] = classDir / p"Run.class"
    
    val classData: Bytes throws StreamCutError | ClasspathRefError =
      (classpath / p"oubliette" / p"_oubliette" / p"Run.class").resource.read[Bytes]()
  
    if !classfile.exists() then classData.writeTo(classfile.file(Create))

    val socket: DiskPath[Unix] = base.tmpPath(t".sock")
    sh"sh -c 'mkfifo $socket'".exec[Unit]()
    val fifo = socket.fifo(Expect)
    val funnel: Funnel[Text] = Funnel()
    val task: Task[Unit] = Task(t"java"):
      unsafely(funnel.stream).foreach: item =>
        Log.fine(t"Sending '$item'")
        item.appendTo(fifo)
      
      Bytes().writeTo(fifo)
    
    Log.info(t"Launching new JVM")
    val process: Process[Text] = sh"$javaBin -cp ${base.path} _oubliette.Run $socket".fork()
    Log.fine(t"JVM started with ${process.pid}")
    
    Jvm(funnel, task, process)


case class NoValidJdkError(version: Int, jre: Boolean = false)
extends Error(err"a valid JDK for specification version $version cannot be found")

object Adoptium:
  def install()(using log: Log, classpath: Classpath)
             : Adoptium throws IoError | StreamCutError | ClasspathRefError =
    val dest = ((Home.Local.Share() / p"oubliette" / p"bin").directory(Ensure) / p"adoptium")
    
    if !dest.exists() then
      val text = (classpath / p"oubliette" / p"adoptium").resource.read[Text]()
      Log.info(t"Installing `adoptium` script to $dest")
      text.writeTo(dest.file(Create))
      dest.file(Expect).setPermissions(executable = true)
    else Log.fine(t"`adoptium` script is already installed at $dest")
    
    Adoptium(dest)

case class Adoptium(script: DiskPath[Unix]):
  def get(version: Maybe[Int], jre: Boolean = false, early: Boolean = false, force: Boolean = false)
         (using env: Environment, log: Log)
         : Jdk throws NoValidJdkError | EnvError | IoError =
    
    val launchVersion = version.or(env.javaSpecificationVersion)
    val earlyOpt = if early then sh"-e" else sh""
    val forceOpt = if force then sh"-f" else sh""
    val jreOpt = if jre then sh"-o" else sh""
    
    val install: Boolean = force || !check(version, jre)
    
    if install
    then Log.info(t"Installing Adoptium OpenJDK™${if jre then t" JRE" else t""} version ${launchVersion}")
    
    val proc = sh"$script get -v $launchVersion $earlyOpt $forceOpt $jreOpt".fork[Text]()
    
    proc.exitStatus() match
      case ExitStatus.Ok =>
        try
          val dir = Unix.parse(proc.await()).directory(Expect)
          if install then Log.fine(t"Installation to $dir completed successfully")
          Jdk(launchVersion, dir)
        catch case err: InvalidPathError => throw NoValidJdkError(launchVersion, jre)
      case _ =>
        throw NoValidJdkError(launchVersion, jre)
  
  def check(version: Maybe[Int], jre: Boolean = false)(using env: Environment, log: Log)
           : Boolean throws EnvError =
    val launchVersion = version.or(env.javaSpecificationVersion)
    Log.info(t"Checking if ${if jre then t"JRE" else t"JDK"} ${launchVersion} is installed")
    val jreOpt = if jre then sh"-o" else sh""
    sh"$script check -v $launchVersion $jreOpt".exec[ExitStatus]() == ExitStatus.Ok

