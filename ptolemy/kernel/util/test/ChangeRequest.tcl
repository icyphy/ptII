# Test ChangeRequest
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
#### ChangeRequest
#

test ChangeRequest-1.0 {test simple run with only parameter changes} {
    set t [java::new ptolemy.kernel.util.test.ChangeRequestTest]
    $t start
    $t mutate
    enumToTokenValues [$t finish]
} {1 2.0 2.0 2.0 2.0}

test ChangeRequest-2.0 {test elaborate run with graph rewiring} {
    $t start
    $t insertFeedback
    enumToTokenValues [$t finish]
} {2.0 6.0 7.0 8.0 9.0}

test ChangeRequest-3.0 {test DE example with no mutations} {
    set t [java::new ptolemy.kernel.util.test.TestDE]
    $t start
    # $t insertFeedback
    enumToObjects [$t finish]
} {0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0}

test ChangeRequest-3.1 {test DE example with period change} {
    $t start
    $t doublePeriod
    enumToObjects [$t finish]
} {0.0 1.0 2.0 3.0 5.0 7.0 9.0 11.0}

test ChangeRequest-3.2 {test DE example with inserted actor} {
    set t [java::new ptolemy.kernel.util.test.TestDE]
    $t start
    $t insertClock
    enumToObjects [$t finish]
} {0.0 1.0 2.0 2.5 3.0 4.0 4.5 5.0 6.0 6.5 7.0 8.0 8.5 9.0 10.0 10.5}


test ChangeRequest-4.0 {StreamChangeListener} {
    set t [java::new ptolemy.kernel.util.test.ChangeRequestTest]
    
    set changeRequest [$t mutateConst2ChangeRequest]

    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamChangeListener \
	    $printStream]

    # Try removing the listener before adding it.
    $changeRequest removeChangeListener $listener    

    $changeRequest addChangeListener $listener

    # Add the listener twice to get coverage of a basic block.
    $changeRequest addChangeListener $listener

    $t start
    jdkCapture {
	$t mutate

	puts "[[java::call Thread currentThread] getName] \
		Before call to waitForCompletionTask"
	$t waitForCompletionTask
	#puts "[[java::call Thread currentThread] getName] \
	#	After call to waitForCompletionTask"
	#puts "[[java::call Thread currentThread] getName] \
	#	Before call to \$t finish"
	enumToTokenValues [$t finish]
	puts "[[java::call Thread currentThread] getName] \
		After call to \$t finish"
	# This will always return immeadiately because the change is
	# not pending
	$changeRequest waitForCompletion
    } stdoutResults
    $printStream flush
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list [$changeRequest isErrorReported] $output $stdoutResults
} {0 {StreamChangeRequest.changeExecuted(): Changing Const to 2.0 succeeded
} {main  Before call to waitForCompletionTask
waitForCompletionThread About to wait for completion
waitForCompletionThread Done waiting for completion
main  After call to $t finish
}} {This test started failing once we upgraded to Java 1.4.  I'm not sure why}

test ChangeRequest-4.1 {StreamChangeListener} {
    set t [java::new ptolemy.kernel.util.test.ChangeRequestTest]
    
    set changeRequest [$t mutateBadChangeRequest]

    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamChangeListener \
	    $printStream]

    # Try removing the listener before adding it.
    $changeRequest removeChangeListener $listener    

    $changeRequest addChangeListener $listener

    # Add the listener twice to get coverage of a basic block.
    $changeRequest addChangeListener $listener

    $t start
    $t mutate

    catch {$t finish} errMsg

    $printStream flush
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list [$changeRequest isErrorReported] $output 
} {0 {StreamChangeRequest.changeFailed(): Change request that always throws an Exception failed: java.lang.Exception: Always Thrown Exception
}}


test ChangeRequest-6.1 {isErrorReported, setErrorReported} {
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamChangeListener \
	    $printStream]

    set r1 [$changeRequest isErrorReported]
    $changeRequest setErrorReported 1
    set r2 [$changeRequest isErrorReported]
    list $r1 $r2
} {0 1}

test ChangeRequest-7.1 {isPersistent, setPersistent} {
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamChangeListener \
	    $printStream]

    set r1 [$changeRequest isPersistent]
    $changeRequest setPersistent 0
    set r2 [$changeRequest isPersistent]
    list $r1 $r2
} {1 0}

test ChangeRequest-7.1 {setDescription} {
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamChangeListener \
	    $printStream]

    set r1 [$changeRequest getDescription]
    set r2 [$changeRequest setDescription "A different description"]
    set r3 [$changeRequest getDescription]
    list $r1 $r2 $r3
} {{Change request that always throws an Exception} {} {A different description}}
