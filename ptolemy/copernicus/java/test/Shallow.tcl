# Tests for the MoMLCompiler class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2001 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

proc sootShallowCodeGeneration {model} {
    global relativePathToPTII

    # We need to get the classpath so that we can run if we are running
    # under Javascope, which includes classes in a zip file
    set builtinClasspath [java::call System getProperty "java.class.path"]
    set rtjar [java::call System getProperty "sun.boot.class.path"]

    set sootClasspath $relativePathToPTII/vendors/soot/1.2.2/jasminclasses.jar[java::field java.io.File pathSeparator]$relativePathToPTII/vendors/soot/1.2.2/sootclasses.jar

    set classpath $relativePathToPTII[java::field java.io.File pathSeparator].[java::field java.io.File pathSeparator]$sootClasspath[java::field java.io.File pathSeparator]$builtinClasspath[java::field java.io.File pathSeparator]$rtjar


    set args [java::new {String[]} 9 \
	    [list \
	    $model "-d" $relativePathToPTII \
	    "-p" "wjtp.at" "targetPackage:ptolemy.copernicus.java.test.cg" \
	    "-p" "wjtp.mt" "targetPackage:ptolemy.copernicus.java.test.cg" \
	    ]]
    set main [java::new ptolemy.copernicus.java.Main $args]
    $main initialize
    $main addTransforms
    set toplevel [$main toplevel]

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


#    exec java -Xmx132m -classpath $classpath \
#	    ptolemy.copernicus.java.Main 
#           $model -d $relativePathToPTII \
#	    -p wjtp.at targetPackage:ptolemy.copernicus.java.test.cg \
#	    -p wjtp.mt targetPackage:ptolemy.copernicus.java.test.cg

    set modelName ptolemy.copernicus.java.test.cg.[$toplevel getName]

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

    return [exec java -Xfuture -classpath $classpath ptolemy.actor.gui.CompositeActorApplication -iterations 10 -class $modelName]
}


# Generate code for all the xml files in a directory.
proc autoShallowCG {autoDirectory} {
    foreach file [glob $autoDirectory/*.xml] {
	puts "------------------ testing $file"
	test "Auto" "Automatic test in file $file" {
	    sootShallowCodeGeneration $file
	    list {}
	} {{}}
    }
}


######################################################################
####
#


proc foo {} {
    global relativePathToPTII
      sootShallowCodeGeneration \
  	 [file join $relativePathToPTII ptolemy actor lib test auto IIR.xml]
}
test MoMLCompiler-1.1 {Compile and run the Orthocomm test} {
    set result [sootShallowCodeGeneration \
  	    ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom]
    lrange $result 0 9
} {2 4 6 8 10 12 14 16 18 20}
 
#  autoShallowCG [file join $relativePathToPTII ptolemy actor lib test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy actor lib conversions test auto]
#  #autoShallowCG [file join $relativePathToPTII ptolemy actor lib javasound test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains ct lib test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains de lib test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains dt kernel test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains fsm kernel test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains fsm test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains hdf kernel test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains sdf kernel test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains sdf lib test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains sdf lib vq test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains sr kernel test auto]
#  autoShallowCG [file join $relativePathToPTII ptolemy domains sr lib test auto]

# Print out stats
#doneTests
