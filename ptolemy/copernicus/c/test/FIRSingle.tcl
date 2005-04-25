# Tests Copernicus C Code generation for the singleClass FIR example.
#
# @Author Ankush Varma
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#

# This test generates three files: FIRSingle.c, FIRSingle.h, and
# FIRSingle_i.h), and compiles,links, and executes these.  The output will
# be that produced by a single invocation of the filter on the given data
# (as specified in FIRconfig.h).  It uses FIRSingleMain.c as a wrapper. To
# change parameters of the filter, change the corresponding values in
# FIRconfig.h. 
#
# A corresponding Ptolemy II model is provided in FIRtest.xml. This model
# drives an equivalent FIR filter with an equivalent input stream. Thus,
# one can compare the output of the generated C code with the output of
# what a corresponding Ptolemy II actor would produce.


test FIRSingle-1.1 {Generate .c, _i.h, and .h files for FIR \
        in singlclass mode and compile into an exe} {
    
    set outputDir testOutput/FIRSingle.out
    set runtimeDir ../../../runtime
    #set gcDir $PTII/vendors/gc/gc/
    
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
    set classpath .[java::field java.io.File \
            pathSeparator]$builtinClasspath[java::field java.io.File \
            pathSeparator]$rtjar

    # Generate the class file.
    exec javac FIRSingle.java

    # Generate the code using singleClass compilation mode.
    exec java -classpath $classpath ptolemy.copernicus.c.JavaToC $classpath \
            -compileMode singleClass FIRSingle

    # NOTE: JavaToC expects the class file to be converted (in this case
    # FIRSingle.class) to be in the directory from which it is invoked. It
    # outputs the generated code files to this directory.  However, here
    # FIRSingle.class is in c/test/ whereas we want the generated code to
    # go to c/test/FIRSingle.out/ . We solve this by automatically moving
    # the generated files to the FIRSingle.out directory after they are
    # created. A better method to solve this might exist.

    # Move the generated files to the FIRSingle.out directory.
    foreach fileName {FIRSingle.c FIRSingle.h FIRSingle_i.h FIRSingle.class} {
        if ([file readable $fileName]) {
            exec mv $fileName $outputDir/$fileName
        }
    }


    cd $outputDir

    # Generate the required .o files.
    exec gcc -c -I $runtimeDir FIRSingle.c
    exec gcc -c -I $runtimeDir -I . ../../FIRSingleMain.c
    exec gcc -c    $runtimeDir/pccg_runtime_single.c

    # Link the .o files into the executable.
    set exeFile firSingle.exe
    eval exec gcc -o $exeFile [glob *.o]

    # Run the executible.
    # The nightly build does not have . in the path, so we use ./ here.
    set exeFile ".[java::call System getProperty file.separator]$exeFile"
    set output [exec $exeFile]
    
    # Turn newlines into spaces.
    regsub -all "\n" $output "" output
    regsub -all "" $output "" output

    # Check if the output is correct.
    set template "11.000000 4.000000 9.000000 0.000000 0.000000 0.000000 0.000000 0.000000 0.000000 0.000000 "
    string first $template $output
    
} {0}

