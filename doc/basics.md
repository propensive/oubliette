_Oubliette_ can launch a new JVM, represented by an instance of `Jvm`, from a
particular installed version of a JDK, represented by an instance of `Jdk`.
This can either be done in a single command, `Jdk#launch`, or through a series
of steps, which may make the final invocation step faster in some cases.

The simplest invocation is,
```scala
val jvm: Jvm = jdk.launch(classpath, main, args)
```
where `classpath` is a `List` of paths, `main` is the name of the main class to
launch, and `args` is a `List` of arguments. The paths may be specified in any
type for which a `GenericPathReader` instanceexists, as defined in
[Anticipation](https://github.com/propensive/anticipation/), such as
`java.io.File`.

From a `Jvm` instance, methods such as `Jvm#stdout`, `Jvm#stderr` and
`Jvm#stdin` provide access to the JVM's input and output streams, through a
`LazyList[IArray[Byte]]` interface. Additionally, `Jvm#pid` will give the
running JVM's process ID, and `Jvm#abort` will abort execution.

But a JVM can be created without having it start execution. We can launch a new JVM with,
```scala
val jvm = jdk.init()
```
and provide it with the details it needs to run in steps, with:
- `Jvm#addClasspath` to add a path to the classpath,
- `Jvm#addArg` to add a single `Text` argument to the `main` method,
- `Jvm#setMain` to specify the `main` method, and,
- `Jvm#preload` to load the named classes early

Preloading classes offers the opportunity to prime the JVM instance to be as
ready as possible to invoke the specified `main` method when the time comes.
This results in near-instantaneous startup times, provided the classes can be
loaded long enough before the main method is invoked.

### Specifying a JDK

A `Jdk` instance can be created by specifying its version number, e.g. `17`,
and a path (in any path format) to its home directory, that is, the directory
which contains the `bin` directory in which the `java` executable resides. For
example:
```scala
val jdk = Jdk(14, java.io.File("/usr/lib/jdk"))
```

### Using Adoptium JVMs

[Adoptium](https://adoptium.net) provides prebuild OpenJDK binaries for
download, and Oubliette can automatically download and use them. This is as
simple as installing the Adoptium script to the filesystem with,
`Adoptium.install()`, and calling `get()` on the result, to fetch the most
recent version.

Other options (all with default values) for the `get` method include,
- `version`, the JDK version
- `jre`, should be `true` if a JRE is preferred over a full JDK
- `early`, should be `true` if early-access binaries should be considered
- `force`, to force download again, even if an existing installation exists

If an appropriate JDK can be found, then `get` will return a `Jdk` instance,
from which a `Jvm` can be launched.


