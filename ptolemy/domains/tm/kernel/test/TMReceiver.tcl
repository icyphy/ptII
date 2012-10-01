# Tests for the TMReceiver class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2012 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test TMReceiver-4.1 {hasRoom} {
    set rec [java::new ptolemy.domains.tm.kernel.TMReceiver]
    # hasRoom(int) always returns true
    list [$rec hasRoom] [$rec hasRoom -1] [$rec hasRoom 0] [$rec hasRoom 10]
} {1 1 1 1}


######################################################################
####
#
test TMReceiver-4.2 {hasToken} {
    set rec [java::new ptolemy.domains.tm.kernel.TMReceiver]
    list [$rec hasToken] [$rec hasToken -1] [$rec hasToken 0] [$rec hasToken 10]
} {0 1 1 0}


######################################################################
####
#
test TMReceiver-4.2 {put with a uncontained receiver } {
    set rec42 [java::new ptolemy.domains.tm.kernel.TMReceiver]
    # Don't set the priority attribute of the port
    catch {$rec42 put [java::new ptolemy.data.IntToken 2]} errMsg
    list $errMsg
} {{ptolemy.kernel.util.InternalErrorException: put() requires that the port has a container}}

######################################################################
####
#
test TMReceiver-4.3 {put port that does not have a container} {
    set rec43 [java::new ptolemy.domains.tm.kernel.TMReceiver]
    set ioPort [java::new ptolemy.actor.IOPort]
    $ioPort setName IOPort1
    $rec43 setContainer $ioPort
    # Don't set the priority attribute of the port
    catch {$rec43 put [java::new ptolemy.data.IntToken 2]} errMsg
    list $errMsg
} {{ptolemy.kernel.util.InternalErrorException: put() requires that the port 'ptolemy.actor.IOPort {.IOPort1}' that contains this receiver be itself contained}}

######################################################################
####
#
test TMReceiver-4.4 {put port that has a container} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName top
    set manager [java::new ptolemy.actor.Manager $w topManager]

    set director \
            [java::new ptolemy.domains.tm.kernel.TMDirector $e0 TMDirector]

    set a0 [java::new ptolemy.actor.TypedAtomicActor $e0 A0]
    set ioPort [java::new ptolemy.actor.TypedIOPort $a0 ioPort1]

    set rec44 [java::new ptolemy.domains.tm.kernel.TMReceiver]
    $rec44 setContainer $ioPort

    # This test is fairly complex.  If it craps out, consider
    # removing the code from here own down, except for the puts
    # themselves.

    # cover debug() clauses
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set debugListener [java::new ptolemy.kernel.util.StreamListener $printStream]

    $director addDebugListener $debugListener

    # Call preinitialize() so that TMDirector._eventQueue is initialized
    $director preinitialize


    # Don't set the priority attribute of the port
    $rec44 put [java::new ptolemy.data.IntToken 2]

    # Don't set the priority attribute of the port
    set priorityIOPort1 [java::new ptolemy.data.expr.Parameter \
			   $ioPort "priority" \
			   [java::new ptolemy.data.IntToken 4]]

    $rec44 put [java::new ptolemy.data.IntToken 7]

    $printStream flush
    $director removeDebugListener $debugListener
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" debugOutput
    list $debugOutput

} {{.top.TMDirector Preinitializing ...
Updating TMDirector parameter _iconDescription
attribute ptolemy.kernel.util.SingletonConfigurableAttribute {.top.TMDirector._iconDescription} changed
Updating TMDirector parameter startTime
attribute ptolemy.data.expr.Parameter {.top.TMDirector.startTime} value undefined changed
Updating TMDirector parameter stopTime
attribute ptolemy.data.expr.Parameter {.top.TMDirector.stopTime} value undefined changed
Updating TMDirector parameter preemptive
Updating TMDirector parameter defaultTaskExecutionTime
Updating TMDirector parameter synchronizeToRealTime
Invoking preinitialize():  .top.A0
.top.TMDirector Finished preinitialize().
enqueue event: to TMEvent(token = 2, priority = 5, destination = ptolemy.actor.TypedAtomicActor {.top.A0}, hasStarted = false, processingTime = -1.0)
enqueue event: to TMEvent(token = 7, priority = 4, destination = ptolemy.actor.TypedAtomicActor {.top.A0}, hasStarted = false, processingTime = -1.0)
}}





