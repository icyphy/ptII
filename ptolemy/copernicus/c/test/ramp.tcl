# Test translation of a simple Ptolemy II ramp model to C.
#
# @Author: Shuvra S. Bhattacharyya, Contributor: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2003 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more 
# information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkClassPathSeparator] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#

# Generate C code for a simple Ptolemy II ramp model. The output will be
# placed in the directory ramp.out. The output consists of the generated
# C code (.c, .i.h, and .h files), a diagnostic output file ramp-out.txt,
# and a file ramp-err.txt that contains error and warning messages that
# resulted during code generation.

# To adapt this template for another Ptolemy II model (xml file), just
# adapt the description above, the test header below, and the value of the
# modelName variable below.

test ramp-1.1 {Generate .c, .i.h, and .h files for ramp.xml} {
    # The model name. If adapting this test file for another model, just
    # change the value of this variable.
    set modelName ramp

    set outputDirectory testOutput/$modelName.out
    file delete -force $outputDirectory
    file mkdir $outputDirectory

    # Set up the classpath.
    set relativePathToPTII ../../../../../ptII
    set builtinClasspath [java::call System getProperty "java.class.path"]
    set rtjar [java::call System getProperty "sun.boot.class.path"]
    set ptolemyClasspath $relativePathToPTII
    set sootJar $relativePathToPTII/lib/sootclasses.jar
    set jasmineJar $relativePathToPTII/lib/jasminclasses.jar
    set separator [java::field java.io.File pathSeparator]
    puts "ptolemyClasspath: $ptolemyClasspath"
    puts "separator: $separator"
    set classpath $ptolemyClasspath$separator$sootJar
    puts "rtjar: $rtjar"
    puts "buildintClasspath: $builtinClasspath"
    # Need buildinClasspath so that we can find JavaScope.zip
    set classpath $classpath$separator$jasmineJar$separator$rtjar$separator$builtinClasspath

    # Set up arguments to C code generation.
    set dummyDirectory ../../nonexistent/something
    set debugFlag --debug
    set phaseName wjtp.snapshot1
    set modelFile $modelName.xml
    set diagnostics $outputDirectory/$modelName-out.txt
    set errors $outputDirectory/$modelName-err.txt
    set outputDirectorySpecifier outDir:$outputDirectory

    # Run C code generation.
    if {[java::call System getProperty "path.separator"] == ";"} {
	exec "java -classpath $classpath ptolemy.copernicus.c.Main $modelFile -d $dummyDirectory $debugFlag -p $phaseName $outputDirectorySpecifier > $diagnostics 2> $errors" 
    } else {
	# The double quotes above cause problems under Solaris, and the
	# > and 2> cause problems when code coverage is run for some reason

	# -stderrok means that if make generates output on stderr, then
	# exec will _not_ report this as an error. 
	# -stderrok was introduced in $PTII/lib/ptjacl.jar on 8/14/02

	exec -stderrok java -classpath $classpath ptolemy.copernicus.c.Main $modelFile -d $dummyDirectory $debugFlag -p $phaseName $outputDirectorySpecifier
    }

    # Make sure all the output files were created.
    list  \
        [file readable $outputDirectory/.CGModelramp.c] \
        [file readable $outputDirectory/.CGModelramp.h] \
        [file readable $outputDirectory/.CGModelramp_i.h] \
        [file readable $outputDirectory/.CGramp.c] \
        [file readable $outputDirectory/.CGramp.h] \
        [file readable $outputDirectory/.CGramp_i.h] \
        [file readable $outputDirectory/.CGsink.c] \
        [file readable $outputDirectory/.CGsink.h] \
        [file readable $outputDirectory/.CGsink_i.h] \
        [file readable $outputDirectory/.Main.c] \
        [file readable $outputDirectory/.Main.h] \
        [file readable $outputDirectory/.Main_i.h] \
} {1 1 1 1 1 1 1 1 1 1 1 1}


