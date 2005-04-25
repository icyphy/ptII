# Tests Copernicus C Code generation for the Embedded CaffeineMark
# benchmarks.
#
# @Author: Christopher Hylands, Shuvra S. Bhattacharyya, Ankush Varma
#
# @Version: $Id$
#
# @Copyright (c) 2000-2005 The Regents of the University of California.
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

test CaffeineApp-1.1 {Generate all required files for CaffeineApp.java} {

    set className CaffeineApp
    set outputDir testOutput/$className
    set lib $outputDir/j2c_lib
    set commandLineArgs {}
    
    # Adds the .java suffix after a space.
    set javaFile [concat $className ".java"]
    # Remove that space.
    regsub " " $javaFile "" javaFile

    regsub ".java" $javaFile ".class"  classFile
    regsub ".java" $javaFile ".c"      cFile
    regsub ".java" $javaFile ".h"      hFile
    regsub ".java" $javaFile "_i.h"    iFile
    regsub ".java" $javaFile ".o"      oFile
    regsub ".java" $javaFile ".make"   makeFile 
    regsub ".java" $javaFile ".mk"     mkFile
    regsub ".java" $javaFile ".exe"    exeFile
    regsub ".java" $javaFile "_main.c" mainCFile
    regsub ".java" $javaFile "_main.o" mainOFile

    # The List of other classes used.
    set otherClasses {  \
        BenchmarkAtom   \
        FloatAtom       \
        LoopAtom        \
        SieveAtom       \
        StringAtom      \
        BenchmarkUnit   \
        LogicAtom       \
        MethodAtom  \
        StopWatch
    }
    
    # Remove the .out directory if it exists.
    if {[file isdirectory $outputDir]} {
	file delete -force $outputDir
    } 
    
    # Create the output directory.
    file mkdir $outputDir

    # Remove the directory for auto-generated natives.
    if {[file isdirectory "natives"]} {
	file delete -force "natives"
    }

    # We need to get the classpath so that we can run if we are running
    # under Javascope, which includes classes in a zip file
    # Caffeine.jar is added here because it contains the class files for
    # the benchmark.
    set caffeineJar .[java::field java.io.File pathSeparator]Caffeine.jar
    set builtinClasspath [java::call System getProperty "java.class.path"]
    set rtjar [java::call System getProperty "sun.boot.class.path"]
    set classpath .[java::field java.io.File\
            pathSeparator]$builtinClasspath[java::field java.io.File\
            pathSeparator]$rtjar[java::field java.io.File\
            pathSeparator]$PTII/vendors/cm/
    
    # Copy all the other classes to this directory so that they can be
    # compiled. This neesds to be done because they do not have a built-in
    # package.
    foreach i $otherClasses {
        exec cp $PTII/vendors/cm/$i.class .
    }
        
    # Generate the .class file. This is not needed for built-in classes
    # like java.lang.Object.
    # Also not needed for classes extracted from jar files.
    exec javac $javaFile
    
    set args [java::new {String[]} 4 \
        [list \
        $classpath \
        "-lib" \
        $lib \
        $className \
        ]]

    # Generate the code.
    #java::call ptolemy.copernicus.c.JavaToC $args 
    exec java -Xmx600m -classpath $classpath ptolemy.copernicus.c.JavaToC \
        $classpath -lib $lib $className
    

    generateCExec make depend -s -f $makeFile
    # This creates the .mk file.
    
    generateCExec make -s -f $mkFile

    # The list of files corresponding to the other classes.
    set otherFiles {}
    foreach i $otherClasses {
        lappend otherFiles [glob $i*.c] 
        # There may be a separate .h and _i.h file.
        foreach j [glob $i*.h] {
            lappend otherFiles $j
        }
        lappend otherFiles [glob $i*.o]
        lappend otherFiles [glob $i*.class]
    }

    # The nightly build does not have . in the path, so we use ./ here.
    set exeFile ".[java::call System getProperty file.separator]$exeFile"
    set output [generateCExec $exeFile]
    
    # Turn newlines into spaces.
    regsub -all "\n" $output " " output
    regsub -all "" $output "" output
    
    # Move all generated files to the output directory.
    foreach i $otherFiles {
        file rename -force $i $outputDir
    }

    file rename -force $classFile $cFile $mainCFile $oFile $mainOFile $hFile \
	    $iFile $makeFile $exeFile $mkFile $outputDir
    
    
    string first $output $output
    
} {0}

