# Tests for the SDFScheduler class
#
# @Author: Steve Neuendorffer
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

test cycle-1.0 {Cycle Scheduling tests} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Actor1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Actor2]
    set a3 [java::new ptolemy.domains.sdf.lib.Delay $toplevel Delay]

    set r1 [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]
    set r2 [$toplevel connect [java::field $a2 output] \
            [java::field [java::cast ptolemy.actor.lib.Transformer $a3] input] \
            R2]
    set r3 [$toplevel connect \
            [java::field [java::cast ptolemy.actor.lib.Transformer $a3] \
            output] [java::field $a1 input] R3]

    $scheduler setValid false
    
#    set l1 [java::new ptolemy.kernel.util.StreamListener]
#    $scheduler addDebugListener $l1

    $toplevel preinitialize
    set sched1 [_testEnums schedule $scheduler]
    list $sched1
} {{{Actor1 Actor2 Delay}}}

test cycle-1.1 {Cycle execution tests} {
    set iter [$director getAttribute iterations]
    _testSetToken $iter [java::new {ptolemy.data.IntToken int} 6]

    set l1 [java::new ptolemy.kernel.util.StreamListener]
    $director addDebugListener $l1
    catch {set r1 [$manager run]} s1
    list $s1 $r1
} {{} {}}
