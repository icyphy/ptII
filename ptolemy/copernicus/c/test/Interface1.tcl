# Tests Interface functionality.
#
# @Author: Ankush Varma
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

test Interface1-1.1 {Generate all required files for Interface1_Main.java} {

    set outputDir testOutput/Interface1
    set className Interface1_Main
    set lib $outputDir/j2c_lib
    set ptII ../../../..
    set gcDir $ptII/vendors/gc/gc
    
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

    # Remove the directory for auto-generated natives.
    if {[file isdirectory "natives"]} {
	file delete -force "natives"
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
    exec javac $javaFile
    
    set errors $className-err.txt

    # Generate the code.
    exec java -classpath $classpath ptolemy.copernicus.c.JavaToC \
            $classpath -lib $lib -gcDir $gcDir $className
                
   
    exec make depend -s -f $makeFile
    #This creates the .mk file.
    exec make -s -f $mkFile

    # Move all generated files to the output directory.
    file rename -force \
         $cFile $mainCFile $oFile $mainOFile $hFile $iFile $makeFile\
            $mkFile $exeFile $classFile $outputDir 
    
    foreach i "[glob {Interface1*.[cho]}] [glob {Interface1*.class}]" {
        file rename -force $i $outputDir
    }

    # Run the automatically generated executible.
    cd $outputDir

    # The nightly build does not have . in the path, so we use ./ here.
    set exeFile ".[java::call System getProperty file.separator]$exeFile"
    set output [exec $exeFile]
    
    # Turn newlines into spaces.
    regsub -all "\n" $output " " output
    regsub -all "
" $output "" output
    
    # Check if the output is correct.
    set template "Class 1 Class 2"
    
    # Test output
    string first $template $output
  
} {0}

