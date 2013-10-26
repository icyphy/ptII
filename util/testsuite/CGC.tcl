# Generate C code for all the models in the auto directory by using the ptolemy/codegen code generator
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005-2013 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if [ file isdirectory auto/knownFailedTests ] {
    foreach file [glob -nocomplain auto/knownFailedTests/*.xml] {
	# Get the name of the current directory relative to $PTII
	set relativeFilename \
		[java::call ptolemy.util.StringUtilities substituteFilePrefix \
		$PTII [file join [pwd] $file] {$PTII}]
	puts "------------------ testing $relativeFilename (Known Failure) "
	test "Auto" "Automatic test in file $relativeFilename" {
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
            set application [createAndExecute $file]
	    set args [java::new {String[]} 1 \
			  [list $file]]

	    set timeout 60000
	    puts "CGC.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	    set watchDog [java::new ptolemy.util.test.WatchDog $timeout]

	    set returnValue 0
	    if [catch {set returnValue \
		       [java::call ptolemy.codegen.kernel.CodeGenerator \
			    generateCode $args]} errMsg] {
	        $watchDog cancel
	        error "$errMsg\n[jdkStackTrace]"
	    } else {
	        $watchDog cancel
	    }
	    list $returnValue
	} {{}} {KNOWN_FAILURE}
    }
}

proc CGC_test {file inline {extraArgs {}} } {
    global PTII	
    set relativeFilename \
	    [java::call ptolemy.util.StringUtilities substituteFilePrefix \
	    $PTII $file {$PTII}]

    puts "------------------ CGC inline=$inline testing $relativeFilename"
    test "Auto" "Automatic CGC test in file $relativeFilename" {
	global haveValgrind
	global env
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
	# Avoid a bug with Pub/Sub where generating code for a Pub/Sub
	# fails on the second run.  r55530 causes this.

	set parser [java::new ptolemy.moml.MoMLParser]
	$parser purgeAllModelRecords	

        set application [createAndExecute $file]

	
	set parser [java::new ptolemy.moml.MoMLParser]
	$parser purgeAllModelRecords	

	if {$extraArgs != {}} {
	    # We might pass -sourceLineBinding true to this method
	    set args [java::new {String[]} [expr {3 + [$extraArgs length]}] \
			  [list "-inline" $inline ]]
	    $args setrange 2 [$extraArgs getrange]
	    $args set [expr {[$args length] -1}] $file
	} else {
	    set args [java::new {String[]} 3 \
			  [list "-inline" $inline $file]]
	}

	set timeout 60000
	puts "CGC.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	set watchDog [java::new ptolemy.util.test.WatchDog $timeout]

	set returnValue 0
	if [catch {set returnValue \
		       [java::call ptolemy.codegen.kernel.CodeGenerator \
			    generateCode $args]} errMsg] {
	    $watchDog cancel
	    error "$errMsg\n[jdkStackTrace]"
	} else {
	    $watchDog cancel
	    if {$haveValgrind} {
		if {[regexp {/mpi/} $relativeFilename] } {
		    puts "CGC.tcl: skipping valgrind on /mpi/ tests"
		} else {    
		    set executableName [string range $file [ expr {[string last / $file] + 1}] [expr {[string length $file] -5}]]
		    set executable "$env(user.home)/codegen/$executableName"
		    puts "Running valgrind $executable"

		    set watchDog [java::new ptolemy.util.test.WatchDog $timeout]
		    set valgrind ""
		    catch {set valgrind [exec -stderrok valgrind $executable]} errMsg
		    $watchDog cancel

		    if {![regexp "ERROR SUMMARY: 0 errors" $valgrind]} {
			error "Valgrind had a non-zero ERROR SUMMARY\n$valgrind\n$errMsg"
		    }
		}
	    }
	}
	list $returnValue
    } {0}
}
set coverageArgs [java::new {String[]} 4 \
		      [list \
			   "-sourceLineBinding" "true" \
			   "-compileTarget" "coverage"]]

if {![catch {exec valgrind --version} errMsg]} {
    set haveValgrind 1
    puts "Valgrind is available"
} else {
    puts "Valgrind is not available"
    set haveValgrind 0
}

foreach file [glob auto/*.xml] {
    # Set NOINLINE in the environment to skip the inline tests:
    # NOINLINE=true $(JTCLSH) $(ROOT)/util/testsuite/CGCNoInline.tcl
    global env
    if { [java::call System getenv NOINLINE] != {true} } {
	CGC_test $file true
    }
    #CGC_test $file false
    CGC_test $file false $coverageArgs
}
if [file isdirectory auto/32bit] {
    if {![java::call ptolemy.actor.lib.jni.PointerToken is32Bit]} {
	puts "Skipping auto/32bit tests because we are on a 64bit machine"
    } else {
	puts "Running auto/32bit tests"
	foreach file [glob auto/32bit/*.xml] {
	    # Set NOINLINE in the environment to skip the inline tests:
	    # NOINLINE=true $(JTCLSH) $(ROOT)/util/testsuite/CGCNoInline.tcl
	    global env
	    if { [java::call System getenv NOINLINE] != {true} } {
		CGC_test $file true
	    }
	    #CGC_test $file false
	    CGC_test $file false $coverageArgs
	}
    }
}
# Print out stats
doneTests
