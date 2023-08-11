[<img alt="GitHub Workflow" src="https://img.shields.io/github/actions/workflow/status/propensive/oubliette/main.yml?style=for-the-badge" height="24">](https://github.com/propensive/oubliette/actions)
[<img src="https://img.shields.io/discord/633198088311537684?color=8899f7&label=DISCORD&style=for-the-badge" height="24">](https://discord.gg/7b6mpF6Qcf)
<img src="/doc/images/github.png" valign="middle">

# Oubliette

__Launch new JVMs from Scala by remote control__

_Oubliette_ provides a convenient way to launch a new JVM instance by specifying the classpath, main method and
arguments to be invoked. Classes may be pre-loaded, providing near-instantaneous JVM startup time. This provides
similar functionality to invoking a main method through a classloader, but with significantly better isolation
from the initiating JVM.

## Features

- launch JVM instances by specifying command-line parameters
- achieve near-instantaneous startup times by preloading classes
- automatically download Adoptium JDKs before launching


## Availability

Oubliette has not yet been published as a binary.

## Getting Started

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




## Status

Oubliette is classified as __embryonic__. For reference, Scala One projects are
categorized into one of the following five stability levels:

- _embryonic_: for experimental or demonstrative purposes only, without any guarantees of longevity
- _fledgling_: of proven utility, seeking contributions, but liable to significant redesigns
- _maturescent_: major design decisions broady settled, seeking probatory adoption and refinement
- _dependable_: production-ready, subject to controlled ongoing maintenance and enhancement; tagged as version `1.0.0` or later
- _adamantine_: proven, reliable and production-ready, with no further breaking changes ever anticipated

Projects at any stability level, even _embryonic_ projects, are still ready to
be used, but caution should be taken if there is a mismatch between the
project's stability level and the importance of your own project.

Oubliette is designed to be _small_. Its entire source code currently consists
of 108 lines of code.

## Building

Oubliette can be built on Linux or Mac OS with [Fury](/propensive/fury), however
the approach to building is currently in a state of flux, and is likely to
change.

## Contributing

Contributors to Oubliette are welcome and encouraged. New contributors may like to look for issues marked
<a href="https://github.com/propensive/oubliette/labels/beginner">beginner</a>.

We suggest that all contributors read the [Contributing Guide](/contributing.md) to make the process of
contributing to Oubliette easier.

Please __do not__ contact project maintainers privately with questions unless
there is a good reason to keep them private. While it can be tempting to
repsond to such questions, private answers cannot be shared with a wider
audience, and it can result in duplication of effort.

## Author

Oubliette was designed and developed by Jon Pretty, and commercial support and training is available from
[Propensive O&Uuml;](https://propensive.com/).



## Name

An _oubliette_ is a dungeon into which a prisonner could be condemned for a lifetime, no longer interacting with the outside world; metaphorically, a separate JVM instance.

In general, Scala One project names are always chosen with some rationale, however it is usually
frivolous. Each name is chosen for more for its _uniqueness_ and _intrigue_ than its concision or
catchiness, and there is no bias towards names with positive or "nice" meanings—since many of the
libraries perform some quite unpleasant tasks.

Names should be English words, though many are obscure or archaic, and it should be noted how
willingly English adopts foreign words. Names are generally of Greek or Latin origin, and have
often arrived in English via a romance language.

## License

Oubliette is copyright &copy; 2023 Jon Pretty & Propensive O&Uuml;, and is made available under the
[Apache 2.0 License](/license.md).
