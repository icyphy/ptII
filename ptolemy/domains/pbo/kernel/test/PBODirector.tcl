# Tests for the PBODirector class
#
# @Author: Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1999-2000 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test PBODirector-2.1 {Constructor tests} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set d1 [java::new ptolemy.domains.pbo.kernel.PBODirector]
    $d1 setName D1
    set d2 [java::new ptolemy.domains.pbo.kernel.PBODirector $w]
    $d2 setName D2
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.pbo.kernel.PBODirector $e0 D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 W.D2 W.E0.D3}

######################################################################
####
#
test PBODirector-3.1 {Test clone} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set d4 [java::cast ptolemy.domains.pbo.kernel.PBODirector [$d2 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {W.Manager}

######################################################################
####
#
test PBODirector-4.1 {Test _makeDirectorOf} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.pbo.kernel.PBODirector $e0 D3]
    set e1 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e1 setName E1
    $e1 setManager $manager
    $e1 setDirector $d3
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {W.E1.D3 W.D4 {W.E0 W.E1}}

######################################################################
####
#
test PBODirector-5.1 {Test action methods} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.pbo.kernel.PBODirector $e0 D3]
    $e0 setManager $manager
    $e0 setDirector $d3

    set a1 [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set p1 [java::new ptolemy.data.expr.Parameter $a1 firingPeriod]
    _testSetToken $p1 [java::new ptolemy.data.DoubleToken 1.0]
    set p1 [java::new ptolemy.data.expr.Parameter $a1 delay]
    _testSetToken $p1 [java::new ptolemy.data.DoubleToken 0.4]

    set a2 [java::new ptolemy.actor.lib.Recorder $e0 Recorder]
    set p2 [java::new ptolemy.data.expr.Parameter $a2 firingPeriod]
    _testSetToken $p2 [java::new ptolemy.data.DoubleToken 1.2]
    set p2 [java::new ptolemy.data.expr.Parameter $a2 delay]
    _testSetToken $p2 [java::new ptolemy.data.DoubleToken 0.4]

    $e0 connect \
	    [java::field [java::cast ptolemy.actor.lib.Source $a1] output]\
	    [java::field [java::cast ptolemy.actor.lib.Sink $a2] input] R1

    set listener [java::new ptolemy.kernel.util.StreamListener]
    $manager addDebugListener $listener
    $d3 addDebugListener $listener

    set listener [java::new ptolemy.actor.DefaultExecutionListener]
    $manager addExecutionListener $listener

    $manager run
    set record [$a2 getRecord 0]
    list [enumToStrings $record]
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(1)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(5)
}}
