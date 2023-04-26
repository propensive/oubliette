/*
    Oubliette, version [unreleased]. Copyright 2023 Jon Pretty, Propensive OÜ.

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
import anticipation.*, fileApi.galileiApi
import serpentine.*
import imperial.*
import rudiments.*
import digression.*
import ambience.*, environments.system
import turbulence.*
import parasitism.*
import gossamer.*
import eucalyptus.*

given realm: Realm = Realm(t"oubliette")

object Jvm:
  given Show[Jvm] = jvm => t"ʲᵛᵐ"+jvm.pid.show.drop(3)

class Jvm(funnel: Funnel[Text], task: Task[Unit], process: /*{*}*/ Process[Text]):
  def addClasspath[T](path: T)(using pi: GenericPathReader[T]): Unit =
    funnel.put(t"path\t${pi.getPath(path)}\n")
  
  def addArg(arg: Text): Unit = funnel.put(t"arg\t$arg\n")
  def setMain(main: Text): Unit = funnel.put(t"main\t$main\n")
  def start(): Unit = funnel.stop()
  def pid: Pid = process.pid
  def await(): ExitStatus = process.exitStatus()
  def preload(classes: List[Text]): Unit = classes.foreach { cls => funnel.put(t"load\t$cls") }
  
  def stderr()(using streamCut: CanThrow[StreamCutError], writable: /*{*}*/ Writable[java.io.OutputStream, Bytes])
            : /*{writable}*/ LazyList[Bytes] =
    process.stderr()
  
  def stdout()(using streamCut: CanThrow[StreamCutError], writable: /*{*}*/ Writable[java.io.OutputStream, Bytes])
            : /*{writable}*/ LazyList[Bytes] =
    process.stdout()
  
  def stdin(in: /*{*}*/ LazyList[Bytes])(using writable: /*{*}*/ Writable[java.io.OutputStream, Bytes])
           : /*{in, writable, process}*/ Unit = process.stdin(in)
  def abort(): Unit = funnel.put(t"exit\t2\n")
    

object Jdk:
  given Show[Jdk] = jdk => t"Jdk(${jdk.version}:${jdk.base.path.fullname})"

case class Jdk(version: Int, base: Directory) extends Shown[Jdk]:

  private lazy val javaBin: File throws IoError = (base / p"bin" / p"java").file(Expect)

  def launch[P: GenericPathReader]
            (classpath: List[P], main: Text, args: List[Text])
            (using Log, Monitor, Classpath)
            : Jvm throws IoError | StreamCutError | EnvError | ClasspathRefError =
    val jvm: Jvm = init()
    classpath.foreach(jvm.addClasspath(_))
    jvm.setMain(main)
    args.foreach(jvm.addArg(_))
    jvm.start()
    jvm

  def init()(using log: Log, monitor: Monitor, classpath: Classpath)
          : Jvm throws IoError | StreamCutError | EnvError | ClasspathRefError =
    val runDir: DiskPath = Xdg.Run.User.current()
    
    val base: Directory = (runDir / p"oubliette").directory(Ensure)
    val classDir: Directory = (base / p"_oubliette").directory(Ensure)
    val classfile: DiskPath = classDir / p"Run.class"
    val resource: ClasspathRef = classpath / p"oubliette" / p"_oubliette" / p"Run.class"
  
    if !classfile.exists() then resource.writeTo(classfile.file(Create))

    val socket: DiskPath = base.tmpPath(t".sock")
    sh"sh -c 'mkfifo $socket'".exec[Unit]()
    val fifo = socket.fifo(Expect)
    val funnel: Funnel[Text] = Funnel()
    
    val task: Task[Unit] = Task(t"java"):
      funnel.stream.map(_.sysBytes).appendTo(fifo)
      Bytes().appendTo(fifo)
    
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
      Log.info(t"Installing `adoptium` script to $dest")
      val file = dest.file(Create)
      (classpath / p"oubliette" / p"adoptium").writeTo(file)
      file.setPermissions(executable = true)
    else Log.fine(t"`adoptium` script is already installed at $dest")
    
    Adoptium(dest)

case class Adoptium(script: DiskPath):
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
        catch case err: PathError => throw NoValidJdkError(launchVersion, jre)
      case _ =>
        throw NoValidJdkError(launchVersion, jre)
  
  def check(version: Maybe[Int], jre: Boolean = false)(using env: Environment, log: Log)
           : Boolean throws EnvError =
    val launchVersion = version.or(env.javaSpecificationVersion)
    Log.info(t"Checking if ${if jre then t"JRE" else t"JDK"} ${launchVersion} is installed")
    val jreOpt = if jre then sh"-o" else sh""
    sh"$script check -v $launchVersion $jreOpt".exec[ExitStatus]() == ExitStatus.Ok
