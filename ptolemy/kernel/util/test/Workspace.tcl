# Tests for the Workspace class
#
# @Author: Edward A. Lee, Lukito Muliadi
#
# @Version: $Id$ 
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
    set w [java::new ptolemy.kernel.util.Workspace]
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
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n1 [java::new ptolemy.kernel.util.NamedObj $w N1]
    set n2 [java::new ptolemy.kernel.util.NamedObj $w N2]
    set n3 [java::new ptolemy.kernel.util.NamedObj $w N3]
    enumToFullNames [$w directory]
} {.N1 .N2 .N3}

test Workspace-3.2 {Add objects to the wrong workspace} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n1 [java::new ptolemy.kernel.util.NamedObj N1]
    catch {$w add $n1} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: W and .N1:
Cannot add an item to the directory of a workspace that it is not in.}}

test Workspace-3.3 {Add objects twice to the workspace directory} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n1 [java::new ptolemy.kernel.util.NamedObj $w N1]
    catch {$w add $n1} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: W and .N1:
Object is already listed in the workspace directory.}}

test Workspace-3.4 {Test directoryList} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n1 [java::new ptolemy.kernel.util.NamedObj $w N1]
    set n2 [java::new ptolemy.kernel.util.NamedObj $w N2]
    set n3 [java::new ptolemy.kernel.util.NamedObj $w N3]
    listToFullNames [$w directoryList]
} {.N1 .N2 .N3}


######################################################################
####
#
test Workspace-4.1 {Remove objects from the workspace directory} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set version1 [$w getVersion]
    set n1 [java::new ptolemy.kernel.util.NamedObj $w N1]
    set n2 [java::new ptolemy.kernel.util.NamedObj $w N2]
    set n3 [java::new ptolemy.kernel.util.NamedObj $w N3]
    set version2 [$w getVersion]
    $w remove $n2
    set version3 [$w getVersion]
    list $version1 \
	    $version2 \
	    [enumToFullNames [$w directory]] \
	    $version3
} {1 7 {.N1 .N3} 8}

test Workspace-4.2 {Call getContainer} {
    # NOTE: Uses previous setup
    expr {[$w getContainer] == [java::null]}
} {1}

test Workspace-4.3 {Call description} {
    # NOTE: Uses previous setup
    $w description
} {ptolemy.kernel.util.Workspace {W} directory {
    {ptolemy.kernel.util.NamedObj {.N1} attributes {
    }}
    {ptolemy.kernel.util.NamedObj {.N3} attributes {
    }}
}}

test Workspace-4.4 {Call toString} {
    # NOTE: Uses previous setup
    $w toString
} {ptolemy.kernel.util.Workspace {W}}

test Workspace-4.4 {Remove all objects from the workspace directory} {
    # NOTE: Uses previous setup
    set version1 [$w getVersion]
    $w removeAll
    set result1 [enumToFullNames [$w directory]]
    set version2 [$w getVersion]
    list $version1 $result1 $version2
} {8 {} 9}

######################################################################
####
#
test Workspace-5.1 {Test multi-thread access} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set t [java::new ptolemy.kernel.util.test.TestWorkspace T $w]
    $t profile
} {}

test Workspace-5.2 {Test multi-thread access} {
    # NOTE: Uses previous setup
    $t start
    # Give the thread a chance to start up.
    sleep 1
    $t profile
} {T.getReadAccess()
T.doneReading()
T.getReadAccess()
T.doneReading()
T.getReadAccess()
T.doneReading()
T.getWriteAccess()
T.doneWriting()
}

test Workspace-5.3 {Test multi-thread access} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set t1 [java::new ptolemy.kernel.util.test.TestWorkspace T1 $w]
    set t2 [java::new ptolemy.kernel.util.test.TestWorkspace T2 $w]
    $t1 start
    $t2 start
    # Give the threads a chance to start up.
    sleep 1
    list [$t1 profile] [$t2 profile]
} {{T1.getReadAccess()
T1.doneReading()
T1.getReadAccess()
T1.doneReading()
T1.getReadAccess()
T1.doneReading()
T1.getWriteAccess()
T1.doneWriting()
} {T2.getReadAccess()
T2.doneReading()
T2.getReadAccess()
T2.doneReading()
T2.getReadAccess()
T2.doneReading()
T2.getWriteAccess()
T2.doneWriting()
}}

######################################################################
####
#
test Workspace-6.1 {Test multi-thread access with a mix of ptolemy and non-ptolemy threads} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set t [java::new ptolemy.kernel.util.test.PtestWorkspace T $w]
    $t profile
} {}

test Workspace-6.2 {Test multi-thread access with a mix of ptolemy and non-ptolemy threads} {
    # NOTE: Uses previous setup
    $t start
    # Give the thread a chance to start up.
    sleep 1
    $t profile
} {T.getReadAccess()
T.doneReading()
T.getReadAccess()
T.doneReading()
T.getReadAccess()
T.doneReading()
T.getWriteAccess()
T.doneWriting()
}

test Workspace-6.3 {Test multi-thread access with a mix of ptolemy and non-ptolemy threads} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set t1 [java::new ptolemy.kernel.util.test.PtestWorkspace T1 $w]
    set t2 [java::new ptolemy.kernel.util.test.PtestWorkspace T2 $w]
    set t3 [java::new ptolemy.kernel.util.test.TestWorkspace T3 $w]
    set t4 [java::new ptolemy.kernel.util.test.TestWorkspace T4 $w]
    $t1 start
    $t2 start
    $t3 start
    $t4 start
    # Give the threads a chance to start up.
    sleep 1
    list [$t1 profile] [$t2 profile] [$t3 profile] [$t4 profile]
} {{T1.getReadAccess()
T1.doneReading()
T1.getReadAccess()
T1.doneReading()
T1.getReadAccess()
T1.doneReading()
T1.getWriteAccess()
T1.doneWriting()
} {T2.getReadAccess()
T2.doneReading()
T2.getReadAccess()
T2.doneReading()
T2.getReadAccess()
T2.doneReading()
T2.getWriteAccess()
T2.doneWriting()
} {T3.getReadAccess()
T3.doneReading()
T3.getReadAccess()
T3.doneReading()
T3.getReadAccess()
T3.doneReading()
T3.getWriteAccess()
T3.doneWriting()
} {T4.getReadAccess()
T4.doneReading()
T4.getReadAccess()
T4.doneReading()
T4.getReadAccess()
T4.doneReading()
T4.getWriteAccess()
T4.doneWriting()
}} 

######################################################################
####
#
test Workspace-7.1 {Test isReadOnly, setReadOnly} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set readOnly1 [$w isReadOnly]
    set n1 [java::new ptolemy.kernel.util.NamedObj $w N1]
    $w setReadOnly 1
    set readOnly2 [$w isReadOnly]
    catch {set n2 [java::new ptolemy.kernel.util.NamedObj $w N2]} errMsg
    $w setReadOnly 0
    set readOnly3 [$w isReadOnly]
    set n2 [java::new ptolemy.kernel.util.NamedObj $w N2]
    set n3 [java::new ptolemy.kernel.util.NamedObj $w N3]
    list $readOnly1 $readOnly2 $errMsg $readOnly3 \
	    [enumToFullNames [$w directory]]
} {0 1 {ptolemy.kernel.util.InvalidStateException: W: Trying to relinquish write access on a write-protected workspace.} 0 {.N1 .null .N2 .N3}}

######################################################################
#### The following assumptions are made for this test to work. 
#### 1. When a thread gets a write access, no other thread has a read access
####    on the workspace.
#
test Workspace-8.1 {Test wait(obj) and corresponding methods} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set tr [java::new ptolemy.kernel.util.test.TestWorkspace2 TR $w]
    $tr start
    # Give the threads a chance to start up.
    sleep 1
    list [$tr profile] 
} {{TR.getReadAccess()
TR.getReadAccess()
TR.getReadAccess()
TR.notif.getWriteAccess()
TR.notif.doneWriting()
TR.doneReading()
TR.doneReading()
TR.doneReading()
}}
