[<img alt="GitHub Workflow" src="https://img.shields.io/github/workflow/status/propensive/oubliette/Build/main?style=for-the-badge" height="24">](https://github.com/propensive/oubliette/actions)
[<img src="https://img.shields.io/maven-central/v/com.propensive/oubliette-core?color=2465cd&style=for-the-badge" height="24">](https://search.maven.org/artifact/com.propensive/oubliette-core)
[<img src="https://img.shields.io/discord/633198088311537684?color=8899f7&label=DISCORD&style=for-the-badge" height="24">](https://discord.gg/7b6mpF6Qcf)
<img src="/doc/images/github.png" valign="middle">

# Oubliette

TBC

## Features

- launch JVM instances by specifying command-line parameters
- achieve near-instantaneous startup times by preloading classes


## Availability

The current latest release of Oubliette is __0.4.0__.

## Getting Started

_Oubliette_ provides a convenient way to launch a new JVM instance by specifying the classpath, main method and
arguments to be invoked. Classes may be pre-loaded, providing near-instantaneous JVM startup time. This provides
similar functionality to invoking a main method through a classloader, but with significantly better isolation
from the initiating JVM.


## Related Projects

The following _Scala One_ libraries are dependencies of _Oubliette_:

[![Anticipation](https://github.com/propensive/anticipation/raw/main/doc/images/128x128.png)](https://github.com/propensive/anticipation/) &nbsp; [![Galilei](https://github.com/propensive/galilei/raw/main/doc/images/128x128.png)](https://github.com/propensive/galilei/) &nbsp; [![Guillotine](https://github.com/propensive/guillotine/raw/main/doc/images/128x128.png)](https://github.com/propensive/guillotine/) &nbsp; [![Imperial](https://github.com/propensive/imperial/raw/main/doc/images/128x128.png)](https://github.com/propensive/imperial/) &nbsp;

No other _Scala One_ libraries are dependents of _Oubliette_.

## Status

Oubliette is classified as __embryonic__. Propensive defines the following five stability levels for open-source projects:

- _embryonic_: for experimental or demonstrative purposes only, without any guarantees of longevity
- _fledgling_: of proven utility, seeking contributions, but liable to significant redesigns
- _maturescent_: major design decisions broady settled, seeking probatory adoption and refinement
- _dependable_: production-ready, subject to controlled ongoing maintenance and enhancement; tagged as version `1.0` or later
- _adamantine_: proven, reliable and production-ready, with no further breaking changes ever anticipated

Oubliette is designed to be _small_. Its entire source code currently consists of 105 lines of code.

## Building

Oubliette can be built on Linux or Mac OS with Irk, by running the `irk` script in the root directory:
```sh
./irk
```

This script will download `irk` the first time it is run, start a daemon process, and run the build. Subsequent
invocations will be near-instantaneous.

## Contributing

Contributors to Oubliette are welcome and encouraged. New contributors may like to look for issues marked
<a href="https://github.com/propensive/oubliette/labels/good%20first%20issue"><img alt="label: good first issue"
src="https://img.shields.io/badge/-good%20first%20issue-67b6d0.svg" valign="middle"></a>.

We suggest that all contributors read the [Contributing Guide](/contributing.md) to make the process of
contributing to Oubliette easier.

Please __do not__ contact project maintainers privately with questions. While it can be tempting to repsond to
such questions, private answers cannot be shared with a wider audience, and it can result in duplication of
effort.

## Author

Oubliette was designed and developed by Jon Pretty, and commercial support and training is available from
[Propensive O&Uuml;](https://propensive.com/).



## Name

An _oubliette_ is a dungeon into which a prisonner could be condemned for a lifetime, no longer interacting with the outside world; metaphorically, a separate JVM instance.

## License

Oubliette is copyright &copy; 2022-23 Jon Pretty & Propensive O&Uuml;, and is made available under the
[Apache 2.0 License](/license.md).
