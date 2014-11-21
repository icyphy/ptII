# Run "auto" tests to generate Functional Mock-up Unit Master Algo. code.
#
# @Author: Christopher Brooks
#
# @Version: $Id: CCGAuto.tcl 67778 2013-10-26 15:50:13Z cxh $
#
# @Copyright (c) 2009-2013 The Regents of the University of California.
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


# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Get the value of ptolemy.ptII.isRunningNightlyBuild and save it,
# then reset the property to the empty string.
# If we are running as the nightly build, we usually want to
# throw an exception if the trainingMode parameter is set to true.
# However, while testing the Test actor itself, we want to 
# be able to set the trainingMode parameter to true

set oldIsRunningNightlyBuild \
    [java::call ptolemy.util.StringUtilities getProperty \
     "ptolemy.ptII.isRunningNightlyBuild"]
java::call System setProperty "ptolemy.ptII.isRunningNightlyBuild" ""

proc test_fmima_cg {model} {
    global PTII	
    set relativeFilename \
	    [java::call ptolemy.util.StringUtilities substituteFilePrefix \
	    $PTII $model {$PTII}]


    
    set args [java::new {String[]} 3 \
		  [list "-generatorPackage" "ptolemy.cg.kernel.generic.program.procedural.fmima" $model]]

    puts "------------------ Invoking the ptolemy/cg Functional Mock-up Unit Master Algoritm (FMIMA)  generator by calling GenericCodeGenerator.generateCode(args).  To replicate: \$PTII/bin/ptcg -generatorPackage ptolemy.cg.kernel.generic.program.procedural.fmima $relativeFilename "
    test "Auto" "Automatic Java ptolemy/cg test in file $relativeFilename" {
	# Remove files from ~/cg so as to force building
	foreach classFile [glob -nocomplain [java::call System getProperty "user.home"]/cg/*.class] { file delete -force $classFile}
	foreach javaFile [glob -nocomplain [java::call System getProperty "user.home"]/cg/*.java] { file delete -force $javaFile}
	foreach mkFile [glob -nocomplain [java::call System getProperty "user.home"]/cg/*.mk] { file delete -force $mkFile}
	set parser [java::new ptolemy.moml.MoMLParser]
	$parser reset
	$parser purgeAllModelRecords

	set timeout 60000
	puts "JavaCGAuto.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	set watchDog [java::new ptolemy.util.test.WatchDog $timeout]

	set returnValue 0
	if [catch {set returnValue \
		       [java::call ptolemy.cg.kernel.generic.GenericCodeGenerator \
			    generateCode $args]} errMsg] {
	    $watchDog cancel
	    error "$errMsg\n[jdkStackTrace]"
	} else {
	    $watchDog cancel
	}
	list $returnValue
    } {0}
}

foreach file [glob auto/*.xml] {
    test_fmima_cg $file
}

# Reset the isRunningNightlyBuild property
java::call System setProperty "ptolemy.ptII.isRunningNightlyBuild" \
    $oldIsRunningNightlyBuild 

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    
# Print out stats
doneTests
