# Tests for the Manager class
#
# @Author: Yuhong Xiong, Edward A. Lee
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test Manager-8.1 {Test type checking} {
    set director [java::new ptolemy.actor.Director]
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setName E0
    $e0 setManager $manager

    #create e1
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    $p1 setOutput true
    # set type 
    set t1 [[java::new ptolemy.data.IntToken] getType]
    $p1 setTypeEquals $t1

    #create e2
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 P2]
    $p2 setInput true
    # set type using an instance of Type
    $p2 setTypeEquals [java::field ptolemy.data.type.BaseType DOUBLE]

    #link up p1, p2
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1

    $director preinitialize
    $manager resolveTypes
    set rt1 [[$p1 getType] toString]
    set rt2 [[$p2 getType] toString]
    list $rt1 $rt2
} {int double}

######################################################################
####
#
test Manager-8.2 {Test run-time type checking, TypedIOPort.broadcast(token)} {
    #use setup above
    set token [java::new {ptolemy.data.IntToken int} 3]
    $director preinitialize

    # cover debug() clause in TypedIOPort.broadcast
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamListener $printStream]
    $p1 addDebugListener $listener

    $p1 broadcast $token

    $p1 removeDebugListener $listener

    set rtoken [$p2 get 0]
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list $output [[$rtoken getClass] getName] \
	    [[java::cast ptolemy.data.DoubleToken $rtoken] doubleValue]
} {{broadcast 3
} ptolemy.data.DoubleToken 3.0}

######################################################################
####
#
test Manager-8.2 {Test run-time type checking, TypedIOPort.broadcast(token)} {

    #use setup above
    set tokenArray [java::new {ptolemy.data.IntToken[]} 3 \
	    [list \
	    [java::new  {ptolemy.data.IntToken int} 0]  \
	    [java::new  {ptolemy.data.IntToken int} 1]  \
	    [java::new  {ptolemy.data.IntToken int} 2]]]


    $director preinitialize

    # cover debug() clause in TypedIOPort.broadcast
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamListener $printStream]
    $p1 addDebugListener $listener

    # Have to use 1 here or we get a full mail
    $p1 broadcast $tokenArray 1

    $p1 removeDebugListener $listener

    set rtoken [$p2 get 0]
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list $output [[$rtoken getClass] getName] \
	    [[java::cast ptolemy.data.DoubleToken $rtoken] doubleValue]
} {{broadcast token array of length 1
} ptolemy.data.DoubleToken 0.0}

######################################################################
####
#
test Manager-8.3 {Test run-time type checking} {
    #use setup above
    set token [java::new ptolemy.data.DoubleToken]
    $director preinitialize
    catch {$p1 broadcast $token} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Run-time type checking failed. Token 0.0 with type double is incompatible with port type: int
  in .E0.E1.P1}}

######################################################################
####
#
test Manager-8.4 {Test type resolution} {
    # use the setup above
    $p1 setTypeEquals [java::field ptolemy.data.type.BaseType UNKNOWN]

    catch {$manager resolveTypes} msg
    list $msg
} {{ptolemy.actor.TypeConflictException: Type conflicts occurred in .E0 on the following inequalities:
  (ptolemy.actor.TypedIOPort {.E0.E1.P1}, unknown) <= (ptolemy.actor.TypedIOPort {.E0.E2.P2}, double)
}}

######################################################################
####
#
test Manager-8.5 {Test type resolution} {
    # use the setup above
    set tInt [[java::new ptolemy.data.IntToken] getType]
    $p1 setTypeEquals $tInt
    $p2 setTypeEquals [java::field ptolemy.data.type.BaseType UNKNOWN]

    $manager resolveTypes
    set rt1 [[$p1 getType] toString]
    set rt2 [[$p2 getType] toString]
    list $rt1 $rt2
} {int int}

######################################################################
####
#
test Manager-8.6 {Test type resolution} {
    set director [java::new ptolemy.actor.Director]
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setName E0
    $e0 setManager $manager

    #create e1, a source actor
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    $p1 setOutput true
    set tDouble [[java::new ptolemy.data.DoubleToken] getType]
    $p1 setTypeEquals $tDouble

    #create e2, a fork
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 E2]
    set p21 [java::new ptolemy.actor.TypedIOPort $e2 P21]
    set p22 [java::new ptolemy.actor.TypedIOPort $e2 P22]
    set p23 [java::new ptolemy.actor.TypedIOPort $e2 P23]
    $p21 setInput true
    $p22 setOutput true
    $p23 setOutput true

    #create e3, a sink actor
    set e3 [java::new ptolemy.actor.TypedAtomicActor $e0 E3]
    set p3 [java::new ptolemy.actor.TypedIOPort $e3 P3]
    $p3 setInput true

    #create e4, a sink actor
    set e4 [java::new ptolemy.actor.TypedAtomicActor $e0 E4]
    set p4 [java::new ptolemy.actor.TypedIOPort $e4 P4]
    $p4 setInput true
    $p4 setTypeEquals $tDouble

    #link up p1-p21, p22-p3, p23-p4
    set r12 [java::new ptolemy.actor.TypedIORelation $e0 R12]
    $p1 link $r12
    $p21 link $r12

    set r23 [java::new ptolemy.actor.TypedIORelation $e0 R23]
    $p22 link $r23
    $p3 link $r23

    set r24 [java::new ptolemy.actor.TypedIORelation $e0 R24]
    $p23 link $r24
    $p4 link $r24

    $director preinitialize
    $manager resolveTypes
    set rt1 [[$p1 getType] toString]
    set rt21 [[$p21 getType] toString]
    set rt22 [[$p22 getType] toString]
    set rt23 [[$p23 getType] toString]
    set rt3 [[$p3 getType] toString]
    set rt4 [[$p4 getType] toString]

    list $rt1 $rt21 $rt22 $rt23 $rt3 $rt4
} {double double double double double double}

######################################################################
####
#
test Manager-8.7 {Test type resolution} {
    # use the setup above
    set tInt [[java::new ptolemy.data.IntToken] getType]
    $p1 setTypeEquals $tInt
    $p4 setTypeEquals $tDouble

    $manager resolveTypes
    set rt1 [[$p1 getType] toString]
    set rt21 [[$p21 getType] toString]
    set rt22 [[$p22 getType] toString]
    set rt23 [[$p23 getType] toString]
    set rt3 [[$p3 getType] toString]
    set rt4 [[$p4 getType] toString]

    list $rt1 $rt21 $rt22 $rt23 $rt3 $rt4
} {int int int int int double}

######################################################################
####
#
test Manager-8.8 {Test type resolution} {
    # use the setup in 8.6
    set tInt [[java::new ptolemy.data.IntToken] getType]
    $p1 setTypeEquals $tDouble
    $p4 setTypeEquals $tInt

    catch {$manager resolveTypes} msg
    list $msg
} {{ptolemy.actor.TypeConflictException: Type conflicts occurred in .E0 on the following inequalities:
  (ptolemy.actor.TypedIOPort {.E0.E2.P23}, double) <= (ptolemy.actor.TypedIOPort {.E0.E4.P4}, int)
}}

######################################################################
####
#
test Manager-10.0 {Test execution listener} {
    set e0 [sdfModel 2]
    set manager [$e0 getManager]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    jdkCapture {
	set listener [java::new ptolemy.actor.StreamExecutionListener]
	$manager addExecutionListener $listener
	$manager run
	$manager removeExecutionListener $listener
    } stdoutResults

    # Strip out the time in ms, which will vary between runs  
    regsub {[0-9]* ms.*$} $stdoutResults "xxx ms" stdoutResultsWithoutTime

    #puts "------- result: [enumToTokenValues [$rec getRecord 0]]"
    #9/02 - 'processing mutations' moved from line 4 to line 2
    list $stdoutResultsWithoutTime [[$manager getState] getDescription] 
} {{preinitializing
resolving types
initializing
executing number 1
wrapping up
idle
Completed execution with 2 iterations
xxx ms
} idle}

######################################################################
####
#
test Manager-10.1 {Test execution listener with one arg} {
    set e0 [sdfModel 2]
    set manager [$e0 getManager]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    jdkCapture {
	set listener [java::new ptolemy.actor.StreamExecutionListener \
		[java::field System out]]

	$manager addExecutionListener $listener
	$manager run
	set exception [java::new ptolemy.actor.NoRoomException \
		"This exception is testing the execution listener"]
	# While we are here, test StreamExecutionListener.executionError()
	$listener executionError $manager $exception
	$manager removeExecutionListener $listener
    } stdoutResults

    # Strip out the time in ms, which will vary between runs  
    regsub {[0-9]* ms.*$} $stdoutResults "xxx ms" stdoutResultsWithoutTime

    # Strip out the stack frames
    regsub -all {	at .*$\n} $stdoutResultsWithoutTime "" \
	    stdoutResultsWithoutStackTrace	   
    #puts "------- result: [enumToTokenValues [$rec getRecord 0]]"
    #9/02 - 'processing mutations' moved from line 4 to line 2
    list $stdoutResultsWithoutStackTrace
} {{preinitializing
resolving types
initializing
executing number 1
wrapping up
idle
Completed execution with 2 iterations
xxx ms
Execution error.
ptolemy.actor.NoRoomException: This exception is testing the execution listener
}}

######################################################################
####
#
test Manager-11.0 {Test execution by execute method} {
    $manager execute
    enumToTokenValues [$rec getRecord 0]
} {0 1}

######################################################################
####
#
test Manager-12.0 {Test execution by fine grain methods} {
    $manager initialize
    $manager iterate
    $manager iterate
    $manager wrapup
    enumToTokenValues [$rec getRecord 0]
} {0 1}

######################################################################
####
#
test Manager-13.0 {Test execution by in another thread} {
    $manager startRun
    sleep 2
    enumToTokenValues [$rec getRecord 0]
} {0 1}

# FIXME: I have no idea how to test pause() and finish().
