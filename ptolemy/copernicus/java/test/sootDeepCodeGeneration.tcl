# Framework for testing soot shallow code generation
#
# @Author: Christopher Hylands
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

if {[info procs jdkClassPathSeparator] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}

# Read in a model, generate code and run it
proc sootDeepCodeGeneration {model} {
    global relativePathToPTII

    # We need to get the classpath so that we can run if we are running
    # under Javascope, which includes classes in a zip file
    set builtinClasspath [java::call System getProperty "java.class.path"]
    set rtjar [java::call System getProperty "sun.boot.class.path"]

    set sootClasspath $relativePathToPTII/vendors/soot/1.2.2/jasminclasses.jar[java::field java.io.File pathSeparator]$relativePathToPTII/vendors/soot/1.2.2/sootclasses.jar

    set classpath $relativePathToPTII[java::field java.io.File pathSeparator].[java::field java.io.File pathSeparator]$sootClasspath[java::field java.io.File pathSeparator]$builtinClasspath[java::field java.io.File pathSeparator]$rtjar
    
    set targetPackage ptolemy.copernicus.java.test.cg

    # time out after so many ms.
    set watchDogTimeOut 600000
    set args [java::new {String[]} 45 \
	    [list \
	    $model "-d" $relativePathToPTII \
	    "-p" "wjtp.watchDog" "time:$watchDogTimeOut" \
	    "-p" "wjtp.at" "targetPackage:$targetPackage" \
	    "-p" "wjtp.mt" "targetPackage:$targetPackage" \
	    "-p" "wjtp.clt" "targetPackage:$targetPackage" \
	    "-p" "wjtp.fot" "targetPackage:$targetPackage" \
	    "-p" "wjtp.ffat" "targetPackage:$targetPackage" \
	    "-p" "wjtp.ffpt" "targetPackage:$targetPackage" \
	    "-p" "wjtp.idt" "targetPackage:$targetPackage" \
	    "-p" "wjtp.iat" "targetPackage:$targetPackage" \
	    "-p" "wjtp.ipt" "targetPackage:$targetPackage" \
	    "-p" "wjtp.itt" "debug,targetPackage:$targetPackage" \
	    "-p" "wjtp.ttn" "targetPackage:$targetPackage" \
	    "-p" "wjtp.ts" "debug" \
	    "-p" "jtp.iee" "enabled" \
	    ]]

    set main [java::new ptolemy.copernicus.java.Main $args]
    set toplevel [$main readInModel $model]
    $main initialize $toplevel
    $main addTransforms
    set modelName ptolemy.copernicus.java.test.cg.[$toplevel getName]

    # Make a stab at getting the iterations
    set director [$toplevel getDirector]
    set iterations [$director getAttribute iterations]
    if { $iterations == [java::null] } {
	puts "WARNING: iterations parameter not found in\n '$modelName',\n \
		perhaps this model is not SDF?"
    } else {
	set iterationsValue [[java::cast ptolemy.data.IntToken \
	    [[java::cast ptolemy.data.expr.Parameter \
	    $iterations]  getToken]] doubleValue]
	puts "iterationsValue = $iterationsValue"
    }


    # See KernelMain.generateCode for a description of why this is necessary
    $args set 0 "java.lang.Object"
    java::call soot.Main setReservedNames
    java::call soot.Main setCmdLineArgs $args
    set main [java::new soot.Main]
    set ccl [java::new soot.ConsoleCompilationListener]
    java::call soot.Main addCompilationListener $ccl
    $main run
    #set thread [java::new Thread main]
    #$thread start

    # Find the new classes in the new Scene.
    # Soot tries to be smart and refresh the scene in between compilations, so so must we.
    java::call ptolemy.copernicus.kernel.PtolemyUtilities loadSootReferences

#    exec java -Xmx132m -classpath $classpath \
#	    ptolemy.copernicus.java.Main 
#           $model -d $relativePathToPTII \
#	    -p wjtp.at targetPackage:ptolemy.copernicus.java.test.cg \
#	    -p wjtp.mt targetPackage:ptolemy.copernicus.java.test.cg




#    set applicationArguments [java::new {java.lang.String[]} 4 [list \
#	    "-class" $modelName \
#	    "-iterations" "10" \
#	    ]]
#
#    set application [java::new ptolemy.actor.gui.CompositeActorApplication]
#    $application processArgs $applicationArguments
#    set models [listToObjects [$application models]]
#    $application waitForFinish
#    set result {}
#    foreach model $models {
#        set modelc [java::cast ptolemy.actor.gui.test.TestModel $model]
#        lappend result [listToStrings [$modelc getResults]]
#    }
#    list $result

#    return [exec java -Xfuture -classpath $classpath ptolemy.actor.gui.CompositeActorApplication -iterations 10 -class $modelName]
    puts "sootDeepCodeGeneration {$model}: running $modelName"

    return [exec java -Xfuture -classpath $classpath ptolemy.copernicus.java.test.cg.Main]
}


# Generate code for all the xml files in a directory.
proc autoDeepCG {autoDirectory} {
    foreach file [glob $autoDirectory/*.xml] {
	puts "------------------ testing $file"
	set time [java::call System currentTimeMillis]
	test "Auto" "Automatic test in file $file" {
	    sootDeepCodeGeneration $file
	    list {}
	} {{}}
	java::call System gc
	puts "[java::call ptolemy.actor.Manager timeAndMemory $time]"
    }
}
