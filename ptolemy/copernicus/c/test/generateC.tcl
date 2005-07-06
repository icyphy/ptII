# Tests Copernicus C Code generation for the Array2DInt example.
#
# @Author: Ankush Varma, Christopher Hylands
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

# Generate C code for a particular className.
# This proc expects the class to appear as $className.class
# and will generate output in testOutput/$className/
# If the global variable VERBOSE is set to 1, then
# status messages are printed
# The commandLineArgs argument is optional.  If it is set, then
# its value will be passed to the generated executable.
proc generateC {className {commandLineArgs {}}} {
    global VERBOSE

    if ![info exists VERBOSE] {
	set VERBOSE 0
    }

    if {$VERBOSE} {
	puts "Entering: [pwd]: generateC $className $commandLineArgs"
    }
    set outputDir testOutput/$className
    set lib testOutput/$className/j2c_lib
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
    # under Javascope, which includes classes in a zip file.
    set builtinClasspath [java::call System getProperty "java.class.path"]
    set rtjar [java::call System getProperty "sun.boot.class.path"]
    set classpath .[java::field java.io.File\
	    pathSeparator]$builtinClasspath[java::field java.io.File\
            pathSeparator]$rtjar
   
    # Generate the .class file. This is not needed for built-in classes
    # like java.lang.Object.
    generateCExec javac $javaFile
    
    set javaToCVerboseOption false
    if {$VERBOSE} {
	set javaToCVerboseOption true
    }

    # Generate the code.
    generateCExec java -Xmx600m -classpath $classpath \
	    ptolemy.copernicus.c.JavaToC \
	    $classpath -lib $lib -gcDir $gcDir \
	    -verbose $javaToCVerboseOption \
	    $className
    #exec -stderrok [generateCExec make -s -f $mkFile] 1>$outputDir/out.txt 2>$outputDir/err.txt
    set error ""
    if [catch {generateCExec make -s -f $makeFile} error] {
	puts "make -s -f $makeFile failed:\n$error\n[jdkStackTrace]" 
    }
    
    # Move all generated files to the output directory.
    file rename -force $cFile $mainCFile $oFile $mainOFile $hFile \
	    $iFile $makeFile $outputDir
    
    # Solaris: life is better if we copy the classFile rather than move it.
    # Most tests call java on the class after calling generateC, so
    # the .class file better still be there.
    file copy -force $classFile $outputDir

    # exefile may not have been created due to compilation errors.
    if [file exists $exeFile] {
        file rename -force $exeFile $outputDir
        

        # Run the automatically generated executable.
        cd $outputDir

	if {$VERBOSE} {
	    puts "Changed to [pwd], $exeFile exists!"
	}

        # The nightly build does not have . in the path, so we use ./ here.
        set exeFile ".[java::call System getProperty file.separator]$exeFile"
        if {$commandLineArgs == {}} {
            set output [generateCExec $exeFile]
        } else {
            set output [generateCExec $exeFile $commandLineArgs]
        }

        # Turn newlines into spaces.
        regsub -all "\n" $output " " output
        regsub -all "
" $output "" output
    
        return $output 
    }
    #else

    if {$VERBOSE} {
	puts "Exiting: [pwd]: generateC $className $commandLineArgs"
    }

    return $error
}


# Pass the args to 'exec -stderrok' and return the results.
# If the global variable VERBOSE is set to 1, then first print out the args.
#
proc generateCExec {args} {
    global VERBOSE

    if ![info exists VERBOSE] {
	set VERBOSE 0
    }

    if {$VERBOSE} {
	puts "about to exec $args"
    }

    # -stderrok means that if make generates output on stderr, then
    # exec will _not_ report this as an error. 
    # -stderrok was introduced in $PTII/lib/ptjacl.jar on 8/14/02
    #
    # It would be nice if we had a version of exec that would
    # print the results as it was generated.
    set results [eval exec -stderrok $args]  

    if {$VERBOSE} {
	puts "exec returned '$results'"
    }
    return $results
} 
