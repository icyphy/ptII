# Tests Copernicus C Code generation for the Simple example
#
# @Author: Christopher Hylands, Shuvra S. Bhattacharyya, Ankush Varma
#
# @Version: $Id$
#
# @Copyright (c) 2000-2002 The Regents of the University of California.
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

test Simple-1.1 {Generate all required files for Simple.java} {

    set outputDir Simple.out
    set className Simple
    
    # Adds the .java suffix after a space.
    set javaFile [concat $className ".java"]
    # Remove that space.
    regsub " " $javaFile "" javaFile

    regsub ".java" $javaFile ".class" classFile
    regsub ".java" $javaFile ".c"     cFile
    regsub ".java" $javaFile ".h"     hFile
    regsub ".java" $javaFile "_i.h"   iFile
    regsub ".java" $javaFile ".o"     oFile
    regsub ".java" $javaFile ".make"  mkFile 

    # Check if the .out directory exists.
    if {[file isdirectory $outputDir]} {
        # Remove all files generated in the previous run.
	file delete -force [glob -nocomplain $outputDir/*]
    } else {
        # Create the Simple.out directory.
        file mkdir $outputDir
    }


    # We need to get the classpath so that we can run if we are running
    # under Javascope, which includes classes in a zip file
    set builtinClasspath [java::call System getProperty "java.class.path"]
    set rtjar [java::call System getProperty "sun.boot.class.path"]
    set classpath .[java::field java.io.File\
            pathSeparator]$builtinClasspath[java::field java.io.File\
            pathSeparator]$rtjar
    
    # Generate the .class file. This is not needed for built-in classes
    # like java.lang.Object.
    # exec javac $javaFile
    
    set args [java::new {String[]} 4 \
        [list \
        $classpath \
        "-lib" \
        $outputDir/j2c_lib \
        $className \
        ]]

    set errors $className-err.txt
    # Generate the code.
    #java::call ptolemy.copernicus.c.JavaToC main $args 
    exec java -classpath $classpath ptolemy.copernicus.c.JavaToC $classpath -lib $outputDir/j2c_lib $className
    
    # NOTE: JavaToC expects the class file to be converted to be in the
    # directory from which it is invoked. It outputs the generated code
    # files to this directory.  However, here Simple.class is in c/test/
    # whereas we want the generated code to go to c/test/SimpleSingle.out/
    # . We solve this by automatically moving the generated files to the
    # SimpleSingle.out directory after they are created. A better method to
    # solve this might exist.

    # Move the generated files to the SimpleSingle.out directory.
    file rename -force $cFile $hFile $iFile $mkFile $outputDir
    
    cd $outputDir

    # Check if the generated code compiles into a .o file.
    # A .exe file cannot be generated because we are in singleClass mode.
    # Not done for built-in(system) classes.
    # exec gcc -c -I ../../runtime $cFile

    # Test the existence of the generated files.
    # The existence of the .o file means that it compiled correctly. 
    list \
    	    [file readable $cFile] \
    	    [file readable $hFile] \
    	    [file readable $iFile] \
            [file isdirectory j2c_lib]

} {1 1 1 1}

