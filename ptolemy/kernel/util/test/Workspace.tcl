# Tests for the Workspace class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#
test Workspace-2.1 {Create a Workspace, set the name, change it} {
    set w [java::new pt.kernel.util.Workspace]
    set result1 [$w getName]
    $w setName A
    set result2 [$w getName]
    $w setName B
    set result3 [$w getName]
    $w setName {}
    set result4 [$w getName]
    list $result1 $result2 $result3 $result4
} {{} A B {}}

######################################################################
####
#
test Workspace-3.1 {Add objects to the workspace directory} {
    set w [java::new pt.kernel.util.Workspace W]
    set n1 [java::new pt.kernel.util.NamedObj $w N1]
    set n2 [java::new pt.kernel.util.NamedObj $w N2]
    set n3 [java::new pt.kernel.util.NamedObj $w N3]
    enumToFullNames [$w directory]
} {W.N1 W.N2 W.N3}

test Workspace-3.2 {Add objects to the wrong workspace} {
    set w [java::new pt.kernel.util.Workspace W]
    set n1 [java::new pt.kernel.util.NamedObj N1]
    catch {$w add $n1} msg
    list $msg
} {{pt.kernel.util.IllegalActionException: W and .N1: Cannot add an item to the directory of a workspace that it is not in.}}

test Workspace-3.3 {Add objects twice to the workspace directory} {
    set w [java::new pt.kernel.util.Workspace W]
    set n1 [java::new pt.kernel.util.NamedObj $w N1]
    catch {$w add $n1} msg
    list $msg
} {{pt.kernel.util.IllegalActionException: W and W.N1: Object is already listed in the workspace directory.}}

######################################################################
####
#
test Workspace-4.1 {Remove objects from the workspace directory} {
    set w [java::new pt.kernel.util.Workspace W]
    set n1 [java::new pt.kernel.util.NamedObj $w N1]
    set n2 [java::new pt.kernel.util.NamedObj $w N2]
    set n3 [java::new pt.kernel.util.NamedObj $w N3]
    $w remove $n2
    enumToFullNames [$w directory]
} {W.N1 W.N3}

test Workspace-4.2 {Remove all objects from the workspace directory} {
    # NOTE: Uses previous setup
    $w removeAll
    enumToFullNames [$w directory]
} {}

######################################################################
####
#
test Workspace-5.1 {Test multi-thread access} {
    set w [java::new pt.kernel.util.Workspace W]
    set t [java::new pt.kernel.util.test.TestWorkspace T $w]
    $t profile
} {}

test Workspace-5.2 {Test multi-thread access} {
    # NOTE: Uses previous setup
    $t start
    # Give the thread a chance to start up.
    sleep 1
    $t profile
} {T.read()
T.doneReading()
T.read()
T.doneReading()
T.read()
T.doneReading()
T.write()
T.doneWriting()
}

test Workspace-5.3 {Test multi-thread access} {
    set w [java::new pt.kernel.util.Workspace W]
    set t1 [java::new pt.kernel.util.test.TestWorkspace T1 $w]
    set t2 [java::new pt.kernel.util.test.TestWorkspace T2 $w]
    $t1 start
    $t2 start
    # Give the threads a chance to start up.
    sleep 1
    list [$t1 profile] [$t2 profile]
} {{T1.read()
T1.doneReading()
T1.read()
T1.doneReading()
T1.read()
T1.doneReading()
T1.write()
T1.doneWriting()
} {T2.read()
T2.doneReading()
T2.read()
T2.doneReading()
T2.read()
T2.doneReading()
T2.write()
T2.doneWriting()
}}
