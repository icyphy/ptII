# Ptolemy II test suite definitions
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1996-2003 The Regents of the University of California.
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


# This file contains support code for the Tcl test suite.  It is
# normally sourced by the individual files in the test suite before
# they run their tests.  This improved approach to testing was designed
# and initially implemented by Mary Ann May-Pumphrey of Sun Microsystems.
#
# Copyright (c) 1990-2003 The Regents of the University of California.
# Copyright (c) 1994 Sun Microsystems, Inc.
#
# ========================================================================
# >>>>>>>>>>>>>>>> INCLUDES MODIFICATIONS FOR [incr Tcl] <<<<<<<<<<<<<<<<<
#
#  AUTHOR:  Michael J. McLennan       Phone: (610)712-2842
#           AT&T Bell Laboratories   E-mail: michael.mclennan@att.com
#     RCS:  $Id$
# ========================================================================
#             Copyright (c) 1993-2003  AT&T Bell Laboratories
# ------------------------------------------------------------------------
#
# See the file "license.terms" for information on usage and redistribution
# of this file, and for a DISCLAIMER OF ALL WARRANTIES.
#

package require java

# Load up Tcl procs to print out enums
if {[info procs _testEnums] == "" } then {
    source [file join $PTII util testsuite testEnums.tcl]
}

if {[info procs enumToFullNames] == "" } then {
    source [file join $PTII util testsuite enums.tcl]
}

if {[info procs description2TclBlend] == "" } then {
    source [file join $PTII util testsuite description.tcl]
}


if {[info procs _testClone] == "" } then {
    source [file join $PTII util testsuite testParameters.tcl]
}

if ![info exists FAILED] {
    set FAILED 0
}
if ![info exists KNOWN_FAILED] {
    set KNOWN_FAILED 0
}
if ![info exists PASSED] {
    set PASSED 0
}
if ![info exists NEWLY_PASSED] {
    set NEWLY_PASSED 0
}
if ![info exists VERBOSE] {
    set VERBOSE 0
}
if ![info exists TESTS] {
    set TESTS {}
}


# Set this to true to see the known failures
if ![info exists showKnownFailures] {
    set showKnownFailures false
}

# To set this to true, set the ptolemy.ptII.isRunningNightlyBuild
# property with
# make JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true 
set isRunningNightlyBuild \
   [java::call System getProperty ptolemy.ptII.isRunningNightlyBuild]

if {"$isRunningNightlyBuild" == "true"} {
    # If we are running the nightly build, then always show the known failures
    set showKnownFailures true
}

proc print_verbose {test_name test_description contents_of_test code answer {testtype "NORMAL"}} {
    global FAILED KNOWN_FAILED VERBOSE errorInfo
    global isRunningNightlyBuild showKnownFailures
    puts "\n"
    if {$VERBOSE \
	    ||"$showKnownFailures" == "true" \
	    || ("$showKnownFailures" != "true" \
		    && "$testtype" == "NORMAL")} {
	puts "==== $test_name $test_description"
	puts "==== Contents of test case:"
	puts "$contents_of_test"
    } else {
	puts "==== $test_name $test_description"
    }
    if {$code != 0} {
	if {$testtype == "NORMAL"} {
	    incr FAILED
	    if {$code == 1} {
		puts "==== Test generated error:"
		jdkStackTrace
	    } elseif {$code == 2} {
		puts "==== Test generated return exception;  result was:"
		puts $answer
	    } elseif {$code == 3} {
		puts "==== Test generated break exception"
	    } elseif {$code == 4} {
		puts "==== Test generated continue exception"
	    } else {
		puts "==== Test generated exception $code;  message was:"
		puts $answer
	    }
	} else {
	    incr KNOWN_FAILED
	    if {$code == 1} {
		puts ".... Test generated KNOWN error:"
		if {"$showKnownFailures" == "true"} {
		    jdkStackTrace
		} else { 
		    puts "To see the results, start up ptjacl, do \
		         'set showKnownFailures true'"
		    puts "       and then resource the .tcl file"
		}
	    } elseif {$code == 2} {
		puts ".... Test generated KNOWN return exception;  result was:"
		puts $answer
	    } elseif {$code == 3} {
		puts ".... Test generated KNOWN break exception"
	    } elseif {$code == 4} {
		puts ".... Test generated KNOWN continue exception"
	    } else {
		puts ".... Test generated KNOWN exception $code;  message was:"
		puts $answer
	    }
	}
    } else {
        puts "==== Result was:"
        puts "$answer"
    }
}

# This proc is similar to string compare $answer $passing_results
# but before answer passing_result are compared, any instances
# of the _createdBy version string as defined in NamedObj.exportMoML()
# are filtered out.  This make it possible to change the version
# number but not have to update all the tests
proc ptFilterOutVersion {answer passing_results} {

    set createdByRegularExpression \
	    {[ ]*<property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="[0-9][^"]*">\n[ ]*</property>\n}


    regsub -all [java::call System getProperty "line.separator"] \
	    $answer "\n" answer2
    regsub -all $createdByRegularExpression $answer2 {} answer3

    regsub -all [java::call System getProperty "line.separator"] \
	    $passing_results "\n" passing_results2

    regsub -all $createdByRegularExpression $passing_results2 {} passing_results3

    #puts "answer3: $answer3"
    #puts "passing_results3: $passing_results3"
    return [string compare $answer3 $passing_results3]
}


proc test {test_name test_description contents_of_test passing_results {testtype "NORMAL"}} {

    # puts "======== $test_name"

    global VERBOSE
    # Define TY_TESTING in the global context so that we can check to
    # see if it is set inside functions that use dialog boxes.
    global TY_TESTING
    global TESTS PASSED FAILED KNOWN_FAILED NEWLY_PASSED

    # To set this to true, set the ptolemy.ptII.isRunningNightlyBuild
    # property with
    # make JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true 
    global isRunningNightlyBuild

    # Set this so modal dialogs become -- ta-dah! -- non-modal
    set TY_TESTING 1

    if {[string compare $TESTS ""] != 0} then {
        set ok 0
        foreach test $TESTS {
            if [string match $test $test_name] then {
                set ok 1
                break
            }
        }
        if !$ok then return
    }
    set code [catch {uplevel $contents_of_test} answer]
    if {$code != 0} {
        print_verbose $test_name $test_description $contents_of_test \
                $code $answer $testtype
    } elseif {[string compare $answer $passing_results] == 0} then {
        if $VERBOSE then {
            print_verbose $test_name $test_description $contents_of_test \
                    $code $answer
            puts "++++ $test_name PASSED"

        }
	if {$testtype != "NORMAL"} {
            puts "!!!! $test_name was marked as failing, but now passes"
	    incr NEWLY_PASSED
	}
	incr PASSED
    } else {
	if {$testtype == "NORMAL"} {
	    # See if the results are different only in version number
	    if {[ptFilterOutVersion $answer $passing_results] == 0} then {
	    incr PASSED
		if $VERBOSE then {
		    print_verbose $test_name $test_description \
			    $contents_of_test $code $answer
		    puts "++++ $test_name PASSED, though the _createBy numbers were different"
		}
	    } else {
		print_verbose $test_name $test_description $contents_of_test \
			$code $answer $testtype
		puts "---- Result should have been:"
		puts "$passing_results"
		if { [llength [split $passing_results "\n"]] > 7 } {
		    puts "@@@@@ known results is more than 7 lines long, so we run diff"
		    # If the answer is more than 7 lines, try running diff
		    puts [diffText $passing_results $answer]
		    puts "@@@@@ Done running diffText"
		}

		puts "---- $test_name FAILED"
		incr FAILED
	    }
	} else {
	    if {"$isRunningNightlyBuild" == "true"} {
		print_verbose $test_name $test_description $contents_of_test \
		    $code $answer $testtype
		puts "---- KNOWN Failure, Result should have been:"
		puts "$passing_results"
		puts "---- $test_name Failed, but this is a KNOWN failure"
            } else {		
		puts "---- $test_name Failed, but this is a KNOWN failure"
		puts "To see the results, start up ptjacl, do "
		puts "    set isRunningNightlyBuild true"
		puts "and then resource the .tcl file"
	    }

	    incr KNOWN_FAILED
	}
    }
    update

    # Now we're done, reset the variable
    # We place a catch around this in case we have recursive tests
    catch {unset TY_TESTING}
}

proc dotests {file args} {
    global TESTS PASSED FAILED
    set savedTests $TESTS
    set TESTS $args
    source $file
    set TESTS $savedTests
    doneTests
}

# Below here we have Ptolemy II Specific extensions

############################################################################
#### doneTests
# Call this at the bottom of each test file
# If reallyExit exists and is not set to 1, then don't exist
#
proc doneTests {args} {
    global PASSED FAILED KNOWN_FAILED NEWLY_PASSED duration reallyExit

    # Attempt to flush the javascope database.  The alternative is
    # to run jsinstr with JSINTRFLAGS=-IFLUSHCLASS=true
    catch {java::call COM.sun.suntest.javascope.database.js\$ flush}

    # This line must exist so that we can easily parse the results
    # it is all on one line so that the nightly build scripts can search
    # for 'Total Tests'
    puts "Failed: $FAILED \
	    Total Tests: [expr $PASSED + $FAILED + $KNOWN_FAILED] \
	    ((Passed: $PASSED, Newly Passed: $NEWLY_PASSED) \
	    Known Failed: $KNOWN_FAILED) [pwd]"
    flush stderr
    update
    if {![info exists reallyExit] || $reallyExit == 1} {
	after [expr {2 * $duration}] ::tycho::TopLevel::exitProgram
    }
}

# If there is no update command, define a dummy proc.  Jacl needs this
if {[info command update] == ""} then {
    proc update {} {}
}

############################################################################
#### ptclose
# Return 1 if the first argument (a list of numbers) is close to
# second argument (also a list of numbers), where "close" is within
# epsilon (the third argument).  Otherwise, return 0.
#
proc ptclose {newresults oldresults {epsilon 0.00001}} {
    if {[expr {[epsilonDiff $newresults $oldresults $epsilon] == {}}]} {
        return 1
    } else {
        return 0
    }
}

############################################################################
#### epsilonDiff
# Compare two lists of numbers, if each number in the newresults
# is the different by more than epsilon from the corresponding number
# in old results, then return a message about the difference.
# If the two results lists are within epsilon, then return null
#
proc epsilonDiff {newresults oldresults {epsilon 0.00001} {level 1}} {
    if {[llength $newresults] != [llength $oldresults]} {
	error "epsilonDiff {$newresults} {$oldresults}:\n\
		The length of the two lists is not the same: \
		[llength $newresults] != [llength $oldresults]"
    }
    set returnresults {}
    # Handle complex numbers where the imaginary part is + 0.0i or - 0.0i
    set zeroCheck 0
    foreach newelement $newresults oldelement $oldresults {

	if {$newelement == $oldelement } {
	    # If the strings are equal, continue.
	    continue
	}

	if {[llength $newelement] != [llength $oldelement]} {
	    error "epsilonDiff {$newresults} {$oldresults}:\n\
		    The lengths of these two elements is not the same:\n\
		    '$newelement'\n '$oldelement'"
	}

	if {[llength $newelement] > 1} {
	    # We have a sublist, so call epsilonDiff
	    set tmpresults [epsilonDiff $newelement $oldelement $epsilon \
		    [expr {$level + 1}]]
	    if {$tmpresults != {} } {
		lappend returnresults $tmpresults
	    }
	    continue
	}
	# The numbers might be complex numbers with trailing ','
	# See the ParseTreeEvaluator.tcl tests for examples of this.
	set newelement [string trimright $newelement ","]
	set oldelement [string trimright $oldelement ","]

	# The numbers might be complex numbers with trailing ']'
	# See the ParseTreeEvaluator.tcl tests for examples of this.
	set newelement [string trimright $newelement {]}]
	set oldelement [string trimright $oldelement {]}]

	# The numbers might be complex numbers with trailing 'i'
	set newelement [string trimright $newelement "i"]
	set oldelement [string trimright $oldelement "i"]

	# The numbers might be complex numbers with preceding '['
	# See the ParseTreeEvaluator.tcl tests for examples of this.
	set newelement [string trimleft $newelement {[}]
	set oldelement [string trimleft $oldelement {[}]

	if { $zeroCheck == 1 } {
	    set zeroCheck 0
	    if { "$oldelement" != "0.0" } {
		error "epsilonDiff {$newresults} {$oldresults}:\n\
			Saw a complex number (
		    previous element was + or -)\n, but current\
	            element is not '0.0'"
	    }
	}

	if { "$newelement" == "+" && "$oldelement" == "-" ||
   	    "$newelement" == "-" && "$oldelement" == "+" } {
		# If we have a complex number like {-0.22500000000000 - 0.0i}
		# then we want to compare it with  {-0.22500000000000 + 0.0i}
		# when we handle the next element, which should be 0.0
		set zeroCheck 1
		continue
	}

	if [ catch {
	    if { "$newelement" == "NaN" &&  "$oldelement" != "NaN" }  {
		    lappend returnresults "$newelement== NaN, $oldelement != NaN"
	    } else {
		if [expr {$newelement > $oldelement}] {
		if [expr { $newelement > ($oldelement + $epsilon)}] {
		    lappend returnresults "$newelement > $oldelement + $epsilon"
		}
	    } else {
		if [expr { $newelement < ($oldelement - $epsilon)}] {
		    lappend returnresults "$newelement < $oldelement + $epsilon"
		}
	    }
	}
	} errmsg] {
	    global errorInfo
	    error "epsilonDiff {$newresults} {$oldresults}:\n\
		    error while processing '$newelement' and '$oldelement':\n\
		    $errorInfo"
	}
    }

    # If we are returning out of the top level epsilonDiff, then
    # possibly print a message
    if {$level == 1 && "$returnresults" != ""} {
	return "$newresults\nis not equal to\n$oldresults\n\
		The following elements were not equal:\n $returnresults"
    }

    return $returnresults
}


############################################################################
#### openAllFiles
# Open up the files that are passed in as arguments, then destroy
# the windows after a short wait
#
proc openAllFiles {args} {
    global VERBOSE
    global duration
    foreach testfile $args {
	if {$VERBOSE == 1} {
	    puts "$testfile"
	    puts "testDefs.tcl: openAllFiles{}: win = $win"
	}
	if [ file exists [::tycho::expandPath $testfile]] {
	    set win [::tycho::File::openContext $testfile]
	    if [catch {$win displayer windowName}] {
		after [expr {2 * $duration}] removeobj $win
	    } else {
		after [expr {2 * $duration}] removeobj \
			[$win displayer windowName]
	    }
	    update
	}
    }

}

############################################################################
#### removeobj
# This procedure removes an object if it exists.
#
proc removeobj {name} {
    if {[info object $name] != ""} {
	delete object $name
    }
}

# How long windows are kept around, in milliseconds
set duration 4000
set longDuration 8000

############################################################################
#### sleep
# sleep for 'seconds'.
#
proc sleep {seconds} {
    puts -nonewline "sleeping $seconds seconds: "
    set endtime [expr [clock seconds] + $seconds]
    while {[clock seconds] < $endtime} {
	puts -nonewline "."
	update
    }
}

############################################################################
#### cast
# The java::cast command is not present in Tcl Blend 1.0, but it is
# present in 1.1.
# If it is not present, then create a dummy proc so the tests will work
# under Tcl Blend 1.0
#
if {"[info command java::cast]" == ""} {
    proc java::cast {type object} {
        return $object
    }
}

############################################################################
#### jdkPrintArray
# Print a java array.  Used by ptolemy/math/test/ArrayMath.tcl and
# other places
proc jdkPrintArray {javaArrayObj {separator {} } } {
    set result {}
    for {set i 0} {$i < [$javaArrayObj length]} {incr i} {
	set element [$javaArrayObj get $i]
	if [ catch {java::info class $element} ] {
	    if {$separator == {} } {
		lappend result $element
	    } else {
		lappend result "$element$separator"
	    }
	} else {
	    if {$separator == {} } {
		lappend result [$element toString]
	    } else {
		lappend result "[$element toString]$separator"
	    }
	}
    }
    return $result
}

############################################################################
#### jdkStackTrace
# Print the most recent Java stack trace
# Here's an example:
# Create a String
#   set s [java::new {String java.lang.String} "123"]
# Try to get a character beyond the end of the array
#   catch {$s charAt 4} err
#   puts "The error was:\n$err"
#   puts "The stack was:\n[jdkStackTrace]"
proc jdkStackTrace {} {
    global errorCode errorInfo
    if { [string match {JAVA*} $errorCode] } {
	set exception [lindex $errorCode 1]
	set stream [java::new java.io.ByteArrayOutputStream]
	set printWriter [java::new \
		{java.io.PrintWriter java.io.OutputStream} $stream]
	$exception {printStackTrace java.io.PrintWriter} $printWriter
	$printWriter flush

	set exceptionMessage [$exception getMessage]
	puts "    while executing"
	set stack [$stream toString]
	if { [string length $stack] > 10240 } {
	    puts "[string range $stack 0 10240] . . ."
	} else {
	    puts "$stack"
	}
	puts "    while executing"
    }
    puts $errorInfo
}



############################################################################
#### diffText
# Given two text arguments, exec the diff command and return the results
proc diffText {texta textb} {
    set atmpfile "a.diffText"
    set btmpfile "b.diffText"
    set afd [open $atmpfile "w"]
    set bfd [open $btmpfile "w"]

    puts $afd $texta
    puts $bfd $textb
    # Flush
    close $afd
    close $bfd

    # Use catch in case diff cannot be found
    catch {exec diff $atmpfile $btmpfile} results

    file delete -force $atmpfile $btmpfile
    return $results
}

# Set a property that signifies we are running the tests in the
# nightly build
# that classes like actor.lib.NonStrictTest will check to see if
# the training flag is set
#java::call System setProperty ptolemy.ptII.isRunningNightlyBuild true

# To test out the WatchDog, try:
# make JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true \

set isRunningNightlyBuild \
   [java::call System getProperty ptolemy.ptII.isRunningNightlyBuild]

# To test the time out in another fashion, try:
# make JTCLSHFLAGS=-Dptolemy.ptII.timeOutSeconds=10
if ![info exists timeOutSeconds] {
    set timeOutSeconds \
	    [java::call System getProperty ptolemy.ptII.timeOutSeconds]
}

if {"$isRunningNightlyBuild" == "true" \
	|| "$timeOutSeconds" != "" } {
    if {"$timeOutSeconds" == ""} {
	# This timeout should be long enough to run tests in copernicus/c/test
        set timeOutSeconds 4800
    }
    puts "testDefs.tcl: setting time out to\
	$timeOutSeconds seconds"
    java::new util.testsuite.WatchDog [expr {$timeOutSeconds * 1000}]
}

