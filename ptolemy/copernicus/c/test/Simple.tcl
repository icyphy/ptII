# Tests Copernicus C Code generation for the Simple example.
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

    set outputDir testOutput/Simple.out
    set className Simple
    
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

    # Remove the .out directory if it exists.
    if {[file isdirectory $outputDir]} {
	file delete -force $outputDir
    } 
    
    # Create the output directory.
    file mkdir $outputDir


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

    # Take the class file to the output directory. All other files can now
    # be generated there.
    exec javac $javaFile
    
    # Generate the code.
    #java::call ptolemy.copernicus.c.JavaToC main $args
    exec java -classpath $classpath ptolemy.copernicus.c.JavaToC $classpath -lib $outputDir/j2c_lib $className
   
    exec make depend -s -f $makeFile
    #This creates the .mk file.
    exec make -s -f $mkFile
    
    # Run the automatically generated executible.
    exec $exeFile

    # Move all generated files to the output directory.
    file rename -force $cFile $mainCFile $oFile $mainOFile $hFile $iFile $makeFile\
            $mkFile $exeFile $outputDir
    
} {}

