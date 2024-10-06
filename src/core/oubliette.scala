/*
    Oubliette, version [unreleased]. Copyright 2024 Jon Pretty, Propensive OÜ.

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
import galilei.*, filesystemOptions.dereferenceSymlinks
import serpentine.*, pathHierarchies.unix
import anticipation.*, filesystemInterfaces.galileiApi
import serpentine.*
import imperial.*
import symbolism.*
import rudiments.*, workingDirectories.virtualMachine
import digression.*
import ambience.*, environments.virtualMachine, systemProperties.virtualMachine
import turbulence.*
import parasite.*
import fulminate.*
import contingency.*, strategies.throwUnsafely
import spectacular.*
import hieroglyph.*, charDecoders.utf8
import gossamer.*
import eucalyptus.*
import vacuous.*
import hellenism.*

given realm: Realm = realm"oubliette"

object Jvm:
  given Show[Jvm] = jvm => t"JVM(${jvm.pid.inspect})"

class Jvm(spool: Spool[Text], task: Async[Unit], process: /*{*}*/ Process[?, Text]):
  def addClasspath[PathType: GenericPath](path: PathType): Unit = spool.put(t"path\t${path.pathText}\n")
  def addArg(arg: Text): Unit = spool.put(t"arg\t$arg\n")
  def setMain(main: Text): Unit = spool.put(t"main\t$main\n")
  def start(): Unit = spool.stop()
  def pid: Pid = process.pid
  def await(): Exit = process.exitStatus()
  def preload(classes: List[Text]): Unit = classes.each { cls => spool.put(t"load\t$cls") }

  def stderr()(using streamCut: Tactic[StreamError], writable: /*{*}*/ Writable[java.io.OutputStream, Bytes]): LazyList[Bytes] =
    process.stderr()

  def stdout()(using streamCut: Tactic[StreamError], writable: /*{*}*/ Writable[java.io.OutputStream, Bytes]): LazyList[Bytes] =
    process.stdout()

  def stdin(in: /*{*}*/ LazyList[Bytes])(using writable: /*{*}*/ Writable[java.io.OutputStream, Bytes]): Unit = process.stdin(in)
  def abort(): Unit = spool.put(t"exit\t2\n")

object Jdk:
  given Jdk is Inspectable = jdk => t"Jdk(${jdk.version}:${jdk.base.path.fullname})"

case class Jdk(version: Int, base: Directory):
  import filesystemOptions.doNotCreateNonexistent
  private lazy val javaBin: File raises IoError = (base / p"bin" / p"java").as[File]

  def launch[PathType: GenericPath]
            (classpath: List[PathType], main: Text, args: List[Text])
            (using Log[Text], Monitor, Classpath, Tactic[IoError], Tactic[StreamError], Tactic[EnvironmentError], Tactic[ClasspathError], Tactic[PathError], Tactic[SystemPropertyError])
            : Jvm =
    val jvm: Jvm = init()
    classpath.each(jvm.addClasspath(_))
    jvm.setMain(main)
    args.each(jvm.addArg(_))
    jvm.start()
    jvm

  def init()(using log: Log[Text], monitor: Monitor, classpath: Classpath)(using Tactic[IoError], Tactic[StreamError], Tactic[EnvironmentError], Tactic[ClasspathError], Tactic[PathError], Tactic[SystemPropertyError])
          : Jvm =
    val runDir: Path = Base.Run.User.current()

    import filesystemOptions.createNonexistent, filesystemOptions.createNonexistentParents
    val base: Directory = (runDir / p"oubliette").as[Directory]
    val classDir: Directory = (base / p"_oubliette").as[Directory]
    val classfile: Path = classDir / p"Run.class"
    val resource: ClasspathRef = classpath / p"oubliette" / p"_oubliette" / p"Run.class"

    if !classfile.exists() then resource.writeTo(classfile.as[File])

    val socket: Path = base.tmpPath(t".sock")
    sh"sh -c 'mkfifo $socket'".exec[Unit]()
    val fifo = socket.as[Fifo]
    val spool: Spool[Text] = Spool()

    val task: Async[Unit] = Async:
      spool.stream.map(_.sysBytes).appendTo(fifo)
      Bytes().appendTo(fifo)

    Log.info(t"Launching new JVM")
    val process: Process[?, Text] = sh"$javaBin -cp ${base.path} _oubliette.Run $socket".fork()
    Log.fine(t"JVM started with ${process.pid}")

    Jvm(spool, task, process)


case class NoValidJdkError(version: Int, jre: Boolean = false)
extends Error(m"a valid JDK for specification version $version cannot be found")

object Adoptium:
  def install()(using log: Log[Text], classpath: Classpath)(using Tactic[IoError], Tactic[StreamError], Tactic[ClasspathError])
             : Adoptium =
    import filesystemOptions.createNonexistent, filesystemOptions.createNonexistentParents
    val dest = ((Home.Local.Share() / p"oubliette" / p"bin").as[Directory].path / p"adoptium")
    if !dest.exists() then
      Log.info(t"Installing `adoptium` script to $dest")
      val file = dest.as[File]
      (classpath / p"oubliette" / p"adoptium").writeTo(file)
      file.setPermissions(executable = true)
    else Log.fine(t"`adoptium` script is already installed at $dest")

    Adoptium(dest)

case class Adoptium(script: Path):
  def get(version: Optional[Int], jre: Boolean = false, early: Boolean = false, force: Boolean = false)
         (using env: Environment, log: Log[Text])(using Tactic[NoValidJdkError], Tactic[EnvironmentError], Tactic[IoError])
         : Jdk =

    val launchVersion = version.or(env.javaSpecificationVersion)
    val earlyOpt = if early then sh"-e" else sh""
    val forceOpt = if force then sh"-f" else sh""
    val jreOpt = if jre then sh"-o" else sh""

    val install: Boolean = force || !check(version, jre)

    if install
    then Log.info(t"Installing Adoptium OpenJDK™${if jre then t" JRE" else t""} version ${launchVersion}")

    val proc = sh"$script get -v $launchVersion $earlyOpt $forceOpt $jreOpt".fork[Text]()

    proc.exitStatus() match
      case Exit.Ok =>
        try
          val dir = Unix.parse(proc.await()).as[Directory]
          if install then Log.fine(t"Installation to $dir completed successfully")
          Jdk(launchVersion, dir)
        catch case err: PathError => abort(NoValidJdkError(launchVersion, jre))
      case _ =>
        abort(NoValidJdkError(launchVersion, jre))

  def check(version: Optional[Int], jre: Boolean = false)(using env: Environment, log: Log[Text])(using Tactic[EnvironmentError])
           : Boolean =
    val launchVersion = version.or(env.javaSpecificationVersion)
    Log.info(t"Checking if ${if jre then t"JRE" else t"JDK"} ${launchVersion} is installed")
    val jreOpt = if jre then sh"-o" else sh""
    sh"$script check -v $launchVersion $jreOpt".exec[Exit]() == Exit.Ok
