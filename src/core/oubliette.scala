package oubliette

import guillotine.*
import joviality.*, filesystems.unix
import anticipation.*, integration.jovialityPath
import serpentine.*
import imperial.*
import rudiments.*, environments.system
import telekinesis.*
import turbulence.*
import parasitism.*
import gossamer.*
import eucalyptus.*
import tetromino.*
import gastronomy.*

class Jvm(funnel: Funnel[Text], task: Task[Unit], process: Process[Text]):
  
  def addClasspath[T](path: T)(using pi: PathInterpreter[T]): Unit =
    funnel.put(t"path\t${pi.getPath(path)}\n")
  
  def addArg(arg: Text): Unit = funnel.put(t"arg\t$arg\n")
  def setMain(main: Text): Unit = funnel.put(t"main\t$main\n")
  def start(): Unit = funnel.stop()
  def pid: Pid = process.pid
  def await(): ExitStatus = process.exitStatus()
  def stderr(rubrics: Rubric*)(using Allocator): DataStream = process.stderr(rubrics*)
  def stdout(rubrics: Rubric*)(using Allocator): DataStream = process.stdout(rubrics*)
  def stdin(in: DataStream)(using Allocator): Unit throws StreamCutError = process.stdin(in)
  def abort(): Unit = funnel.put(t"exit\t2\n")
    
object Jvm:
  private val classData: Bytes = t"yv66vgAAAD0ApgoAAgADBwAEDAAFAAYBABBqYXZhL2xhbmcvT2JqZWN0AQAGPGluaXQ+AQADKClWCQAIAAkHAAoMAAsADAEADl9vdWJsaWV0dGUvUnVuAQAHcGVuZGluZwEAFUxqYXZhL3V0aWwvQXJyYXlMaXN0OwoADgAPBwAQDAARABIBABNqYXZhL3V0aWwvQXJyYXlMaXN0AQAHaXNFbXB0eQEAAygpWgoADgAUDAAVABYBAARzaXplAQADKClJBwAYAQAMamF2YS9uZXQvVVJMCgAOABoMABsAHAEAB3RvQXJyYXkBACgoW0xqYXZhL2xhbmcvT2JqZWN0OylbTGphdmEvbGFuZy9PYmplY3Q7BwAeAQAXamF2YS9uZXQvVVJMQ2xhc3NMb2FkZXIJAAgAIAwAIQAiAQALY2xhc3NMb2FkZXIBABdMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwoAHQAkDAAFACUBACkoW0xqYXZhL25ldC9VUkw7TGphdmEvbGFuZy9DbGFzc0xvYWRlcjspVgcAJwEADGphdmEvaW8vRmlsZQoAJgApDAAFACoBABUoTGphdmEvbGFuZy9TdHJpbmc7KVYKACYALAwALQAGAQAMZGVsZXRlT25FeGl0BwAvAQAWamF2YS9pby9CdWZmZXJlZFJlYWRlcgcAMQEAEmphdmEvaW8vRmlsZVJlYWRlcgoAMAAzDAAFADQBABEoTGphdmEvaW8vRmlsZTspVgoALgA2DAAFADcBABMoTGphdmEvaW8vUmVhZGVyOylWCgAuADkMADoAOwEACHJlYWRMaW5lAQAUKClMamF2YS9sYW5nL1N0cmluZzsIAD0BAAEJCgA/AEAHAEEMAEIAQwEAEGphdmEvbGFuZy9TdHJpbmcBAAVzcGxpdAEAJyhMamF2YS9sYW5nL1N0cmluZzspW0xqYXZhL2xhbmcvU3RyaW5nOwoAPwBFDABGABYBAAhoYXNoQ29kZQgASAEABHBhdGgKAD8ASgwASwBMAQAGZXF1YWxzAQAVKExqYXZhL2xhbmcvT2JqZWN0OylaCABOAQAEbG9hZAgAUAEABGV4aXQIAFIBAARtYWluCABUAQADYXJnCgAmAFYMAFcAWAEABXRvVVJJAQAQKClMamF2YS9uZXQvVVJJOwoAWgBbBwBcDABdAF4BAAxqYXZhL25ldC9VUkkBAAV0b1VSTAEAECgpTGphdmEvbmV0L1VSTDsKAA4AYAwAYQBMAQADYWRkCgAIAGMMAGQABgEABnVwZGF0ZQoAZgBnBwBoDABpAGoBABVqYXZhL2xhbmcvQ2xhc3NMb2FkZXIBAAlsb2FkQ2xhc3MBACUoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvQ2xhc3M7CgBsAG0HAG4MAG8AcAEAEWphdmEvbGFuZy9JbnRlZ2VyAQAIcGFyc2VJbnQBABUoTGphdmEvbGFuZy9TdHJpbmc7KUkKAHIAcwcAdAwAUAB1AQAQamF2YS9sYW5nL1N5c3RlbQEABChJKVYJAAgAdwwAeAB5AQAJbWFpbkNsYXNzAQASTGphdmEvbGFuZy9TdHJpbmc7CQAIAHsMAHwADAEACG1haW5BcmdzCgA/AH4MAH8AgAEACXN1YnN0cmluZwEAFShJKUxqYXZhL2xhbmcvU3RyaW5nOwoAJgCCDACDABIBAAZkZWxldGUHAIUBAA9qYXZhL2xhbmcvQ2xhc3MHAIcBABNbTGphdmEvbGFuZy9TdHJpbmc7CgCEAIkMAIoAiwEACWdldE1ldGhvZAEAQChMamF2YS9sYW5nL1N0cmluZztbTGphdmEvbGFuZy9DbGFzczspTGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZDsKAI0AjgcAjwwAkACRAQAYamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kAQAGaW52b2tlAQA5KExqYXZhL2xhbmcvT2JqZWN0O1tMamF2YS9sYW5nL09iamVjdDspTGphdmEvbGFuZy9PYmplY3Q7CgBmAJMMAJQAlQEAFGdldFN5c3RlbUNsYXNzTG9hZGVyAQAZKClMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwoADgADAQAJU2lnbmF0dXJlAQAlTGphdmEvdXRpbC9BcnJheUxpc3Q8TGphdmEvbmV0L1VSTDs+OwEAKUxqYXZhL3V0aWwvQXJyYXlMaXN0PExqYXZhL2xhbmcvU3RyaW5nOz47AQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEADVN0YWNrTWFwVGFibGUBABYoW0xqYXZhL2xhbmcvU3RyaW5nOylWAQAKRXhjZXB0aW9ucwcAoAEAE2phdmEvaW8vSU9FeGNlcHRpb24HAKIBACZqYXZhL2xhbmcvUmVmbGVjdGl2ZU9wZXJhdGlvbkV4Y2VwdGlvbgEACDxjbGluaXQ+AQAKU291cmNlRmlsZQEACFJ1bi5qYXZhACEACAACAAAABAAKACEAIgAAABoACwAMAAEAlwAAAAIAmAAKAHwADAABAJcAAAACAJkACgB4AHkAAAAEAAEABQAGAAEAmgAAAB0AAQABAAAABSq3AAGxAAAAAQCbAAAABgABAAAACAAKAGQABgABAJoAAABbAAQAAQAAACqyAAe2AA2aACOyAAe2ABO9ABdLsgAHKrYAGVe7AB1ZKrIAH7cAI7MAH7EAAAACAJsAAAAWAAUAAAAPAAkAEAATABEAGwASACkAFACcAAAAAwABKQAJAFIAnQACAJoAAAJFAAYABwAAAY67ACZZKgMytwAoTCu2ACu7AC5ZuwAwWSu3ADK3ADVNLLYAOE4txgEVLRI8tgA+OgQZBAMyOgUCNgYZBbYARKsAAAAAfwAAAAUAAXpWAAAAcgAvuR4AAABSADLE5gAAAEIAMwW5AAAAYgA0ZCUAAAAyGQUSR7YASZkARgM2BqcAQBkFEk22AEmZADYENganADAZBRJPtgBJmQAmBTYGpwAgGQUSUbYASZkAFgY2BqcAEBkFElO2AEmZAAYHNgYVBqoAAABzAAAAAAAAAAQAAAAhAAAAPAAAAE0AAABaAAAAZLIAB7sAJlkZBAQytwAotgBVtgBZtgBfV6cAOrgAYrIAHxkEBDK2AGVXpwApGQQEMrgAa7gAcacAHBkEBDKzAHanABKyAHotB7YAfbYAX1enAAMstgA4Tqf+7bIAdscABwS4AHG4AGIrtgCBV7IAH7IAdrYAZToEGQQSUQS9AIRZAxKGU7YAiDoFsgB6tgATvQA/OgayAHoZBrYAGVcZBQEEvQACWQMZBlO2AIxXsQAAAAIAmwAAAHYAHQAAABcACwAYAA8AGgAfABsAJAAdACgAHgAwACAA4AAiAPgAIwD7ACUA/gAmAQkAJwEMACkBFgAqARkALAEgAC0BIwAvAS8AMAEyADUBNwA2AToAOAFEADkBRwA6AUwAPAFXAD0BaQA+AXQAPwF9AEABjQBBAJwAAAApAA/+ACQHACYHAC4HAD/+AEsHAIYHAD8BDw8PDwwiGhAMCfkADvoABwkAngAAAAYAAgCfAKEACACjAAYAAQCaAAAAQwACAAAAAAAfuACSswAfuwAOWbcAlrMAB7sADlm3AJazAHoBswB2sQAAAAEAmwAAABIABAAAAAkABgAKABAACwAaAAwAAQCkAAAAAgCl".decode[Base64]

  def launch[P: PathInterpreter](classpath: List[P], main: Text, args: List[Text])(using Log, Monitor)
            : Jvm throws IoError | StreamCutError | EnvError =
    val jvm: Jvm = init()
    classpath.foreach(jvm.addClasspath(_))
    jvm.setMain(main)
    args.foreach(jvm.addArg(_))
    jvm.start()
    jvm

  def init()(using log: Log, monitor: Monitor): Jvm throws IoError | StreamCutError | EnvError =
    given Allocator = allocators.default
    val runDir: DiskPath[Unix] = Xdg.Run.User.current()
    
    val base: Directory[Unix] = (runDir / p"oubliette").directory(Ensure)
    val classDir: Directory[Unix] = (base / p"_oubliette").directory(Ensure)
    val classfile: DiskPath[Unix] = classDir / p"Run.class"
    
    if !classfile.exists() then classData.writeTo(classfile.file(Create))

    val socket: DiskPath[Unix] = base.tmpPath(t".sock")
    sh"sh -c 'mkfifo $socket'".exec[Unit]()
    val fifo = socket.fifo(Expect)
    val funnel: Funnel[Text] = Funnel()
    val task: Task[Unit] = Task(t"java")(funnel.stream.writeTo(fifo))
    Log.info(t"Launching new JVM")
    val process: Process[Text] = sh"java -cp $base _oubliette.Run $socket".fork()
    Log.fine(t"JVM started with PID ${process.pid}")
    
    Jvm(funnel, task, process)

given oubliette: Realm(t"oubliette")

// object Adoptium:
//   enum ReleaseType:
//     case Ga, Ea
  
//   enum Architecture:
//     case X64, X86, X32, Ppc64, Ppc64Le, S390x, Aarch64, Arm, SparkV9, RiscV64
//   def search(version: Int, release: ReleaseType, architecture: Architecture): List[JvmBuild] =
    
//     url"https://api.adoptium.net/v4/assets/feature_releases/${version.show}/${release.show}"