# Tests for the UndoStackAttribute class
#
# @Author: Christopher Hylands Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
test UndoStackAttribute-1.1 {Create an UndoStackAttribute} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    $n setName N
    set p [java::new ptolemy.kernel.undo.UndoStackAttribute $n P]
    $p getFullName
} {.N.P}

######################################################################
####
#
test UndoStackAttribute-2.1 {call getUndoInfo } {
    # Uses 1.1 above
    [$p getUndoInfo $n] toString
} {ptolemy.kernel.undo.UndoStackAttribute {.N.P}}

######################################################################
####
#
test UndoStackAttribute-3.1 {Simple undo/redo test with debugging} {
    set undoAction1 [java::new ptolemy.kernel.undo.test.UndoActionTest \
	"UndoActionTest1"]
    set undoAction2 [java::new ptolemy.kernel.undo.test.UndoActionTest \
	"UndoActionTest2"]

    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    set stack [java::new ptolemy.kernel.undo.UndoStackAttribute $n \
	"my UndoStackAttribute"]
    $stack addDebugListener $listener
    jdkCapture {
	$stack push $undoAction1
	$stack push $undoAction2
	$stack undo
	$stack redo
	$stack undo
	$stack undo
    } stdoutResults

    list $stdoutResults [$listener getMessages]
} {{UndoActionTest.execute(): UndoActionTest2
UndoActionTest.execute(): UndoActionTest1
} {=======> Pushing action onto undo stack:
UndoActionTest-UndoActionTest1
======= Clearing redo stack.

=======> Pushing action onto undo stack:
UndoActionTest-UndoActionTest2
======= Clearing redo stack.

<====== Executing undo action:
UndoActionTest-UndoActionTest2
<====== Executing undo action:
UndoActionTest-UndoActionTest1
}}

######################################################################
####
#
test UndoStackAttribute-4.1 {mergeTopTwo} {
    # Uses 3.1 above
    jdkCapture {
	$stack push $undoAction1
	$stack push $undoAction2
	$stack mergeTopTwo 
    } stdoutResults
    list $stdoutResults [$listener getMessages]
} {{} {=======> Pushing action onto undo stack:
UndoActionTest-UndoActionTest1
======= Clearing redo stack.

=======> Pushing action onto undo stack:
UndoActionTest-UndoActionTest2
======= Clearing redo stack.

<====== Executing undo action:
UndoActionTest-UndoActionTest2
<====== Executing undo action:
UndoActionTest-UndoActionTest1
=======> Pushing action onto undo stack:
UndoActionTest-UndoActionTest1
======= Clearing redo stack.

=======> Pushing action onto undo stack:
UndoActionTest-UndoActionTest2
======= Clearing redo stack.

=======> Merging top two on undo stack:
Merged action.
First part:
UndoActionTest-UndoActionTest2

Second part:
UndoActionTest-UndoActionTest1
}}
