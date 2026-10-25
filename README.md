This is the Ptolemy II README.md File

Ptolemy II consists of a group of Java packages that support
heterogeneous concurrent modeling and design.

The Ptolemy II documentation can be found in $PTII/doc/index.htm or [http://ptolemy.berkeley.edu/ptolemyII/ptIIlatest/doc/index.htm](http://ptolemy.berkeley.edu/ptolemyII/ptIIlatest/doc/index.htm)

* To develop Ptolemy II code, we recommend that you follow the
[Ptolemy II Eclipse Instructions located in $PTII/doc/eclipse/index.htm or [https://ptolemy.berkeley.edu/ptolemyII/ptIIlatest/doc/eclipse/index.htm](https://ptolemy.berkeley.edu/ptolemyII/ptIIlatest/doc/eclipse/index.htm)
* To build from the command line, see [Building on Linux](#building-on-linux) below, $PTII/doc/install_index.htm or [https://www.icyphy.org/ptII/](https://www.icyphy.org/ptII/)

* [Main Ptolemy II website](http://ptolemy.berkeley.edu/ptolemyII)
* [Contributing](CONTRIBUTING.md)
* [Downloads and Travis Logs](https://icyphy.github.io/ptII/)
* [Wiki page for Ptolemy II Travis instance](https://wiki.eecs.berkeley.edu/ptexternal/Main/Travis)
* [![Build Status](https://travis-ci.org/icyphy/ptII.svg?branch=master)](https://travis-ci.org/icyphy/ptII)

## Building on Linux

There are no prebuilt Linux packages, so Ptolemy II is built from the git
sources. The build takes about a minute on a typical machine. A shallow
clone is roughly 800 MB.

### 1. Requirements

* A **JDK, version 11 to 21** (tested with 11, 17 and 21). JDK 25 currently
  fails to build, see [Troubleshooting](#troubleshooting). A JRE alone is
  not enough because `configure` needs `javac`.
* `git`
* A graphical session to run the GUI (`vergil`).

Apache Ant is bundled in the repository, so you do not need to install it.

### 2. Install the prerequisites

**Debian / Ubuntu / Linux Mint**

```bash
sudo apt update
sudo apt install openjdk-17-jdk git
```

**Fedora / RHEL / Rocky / AlmaLinux**

```bash
sudo dnf install java-17-openjdk-devel git
```

**Arch / Manjaro**

```bash
sudo pacman -S jdk17-openjdk git
```

**openSUSE**

```bash
sudo zypper install java-17-openjdk-devel git
```

Check that the compiler is available:

```bash
javac -version    # should print 11 to 21
```

If several Java versions are installed (for example a newer default JDK),
select the one you want before building:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64   # adjust to your system
export PATH="$JAVA_HOME/bin:$PATH"
```

You can also pass `--with-java=DIR` to `./configure`.

### 3. Get the source

```bash
git clone --depth=50 --branch=master --single-branch https://github.com/icyphy/ptII
cd ptII
export PTII=`pwd`
```

### 4. Build

```bash
export LC_ALL=C.UTF-8    # see "Troubleshooting" if your locale is not UTF-8
./configure
$PTII/bin/ant
```

`configure` prints many `WARNING` messages about optional packages that are
not installed. These are normal. The build succeeded if `ant` ends with
`BUILD SUCCESSFUL`.

### 5. Run

```bash
$PTII/bin/vergil
```

`vergil -version` prints the version without opening a window. Run
`$PTII/bin/ant -p` to list the other build targets.

To avoid retyping, add these to your `~/.bashrc` (or your shell's profile):

```bash
export PTII=/path/to/ptII
export PATH="$PTII/bin:$PATH"
```

### Rebuilding after a change in your environment

If you install or switch to a different JDK, install an optional package, or
move the tree, reconfigure:

```bash
cd $PTII
rm -f config.*
./configure
$PTII/bin/ant
```

### Troubleshooting

* **`configure: Failed to find javac`** (followed by a message about
  `/usr/java`): only a JRE is installed. Install a JDK as shown above.
* **`unmappable character ... for encoding US-ASCII`** during the build:
  your locale is not UTF-8, which is common in minimal containers, CI and
  some SSH sessions, and JDKs before 18 then default to ASCII. Run
  `export LC_ALL=C.UTF-8` and build again.
* **`Invalid CEN header (invalid zip64 extra data field size)`** while reading
  `lib/jna-4.0.0-variadic.jar`: this happens with JDK 25. Build with JDK 21 or
  17 instead (set `JAVA_HOME`, put `$JAVA_HOME/bin` first in `PATH`, then run
  `rm -f config.*; ./configure`).
* **Many `configure: WARNING` messages** about optional packages (OpenCV,
  JavaFX, JAI, CUnit, JavaCC, Matlab, ...), and `Unrecognized option: -d32`:
  harmless. Only an error at the end of the `ant` output means the build
  failed.
* **Messages about macOS `.app` bundles or `MacJVM.tar.gz`**: harmless on
  Linux; the build still succeeds.
* **Warning that `/usr/bin/java` is not the same as the JDK's `java`**: your
  `PATH` finds a different JDK first. Set `JAVA_HOME` and put
  `$JAVA_HOME/bin` first in `PATH`, as shown above.
* **`vergil` seems to hang at start-up**: the first run updates the accessors
  repository with `git pull`. Set `PT_NO_NET=1` to skip this when offline.
* **No window appears over SSH**: `vergil` is a GUI program and needs a
  display, for example `ssh -X`, or run it in a local desktop session.

### Optional packages

Most dependencies (Colt, Diva, Jython, Ptjacl) ship inside the tree. Others
are only needed for specific features, for example JavaCC if you modify the
expression parser. See $PTII/doc/install.htm for the full list.
