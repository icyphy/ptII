# Tests for the ODFThread class
#
# @Author: John S. Davis II
#
# @Version: %W%	%G%
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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

######################################################################
####
#
test ODFThread-2.1 {hasMinRcvrTime - No simultaneous Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor $topLevel "actor"] 

    set rcvr1 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr3 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set triple1 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr1 0.0 1]
    set triple2 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr2 0.5 2]
    set triple3 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr3 2.5 3]

    set thread [java::new ptolemy.domains.odf.kernel.ODFThread $actor $dir]

    $thread updateRcvrList $triple1
    $thread updateRcvrList $triple2
    $thread updateRcvrList $triple3

    list [$thread getCurrentTime] [$thread hasMinRcvrTime]

} {0.0 1}

######################################################################
####
#
test ODFThread-2.2 {hasMinRcvrTime - Simultaneous Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor $topLevel "actor"] 

    set rcvr1 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr3 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set triple1 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr1 5.0 1]
    set triple2 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr2 5.0 2]
    set triple3 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr3 18.0 3]

    set thread [java::new ptolemy.domains.odf.kernel.ODFThread $actor $dir]

    $thread updateRcvrList $triple1
    $thread updateRcvrList $triple2
    $thread updateRcvrList $triple3

    list [$thread getCurrentTime] [$thread hasMinRcvrTime]

} {0.0 0}

######################################################################
####
#
test ODFThread-2.3 {getHighestPriorityTriple - Simultaneous Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor $topLevel "actor"] 

    set rcvr1 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr3 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set triple1 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr1 5.0 1]
    set triple2 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr2 5.0 2]
    set triple3 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr3 6.0 3]

    set thread [java::new ptolemy.domains.odf.kernel.ODFThread $actor $dir]

    $thread updateRcvrList $triple1
    $thread updateRcvrList $triple2
    $thread updateRcvrList $triple3
    set newtriple [$thread getHighestPriorityTriple]

    list [$thread getCurrentTime] [expr {$triple2 == $newtriple} ]

} {0.0 1}

######################################################################
####
#
test ODFThread-2.4 {getHighestPriorityTriple - No Simultaneous Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor $topLevel "actor"] 

    set rcvr1 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set triple1 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr1 15.0 1]
    set triple2 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr2 5.0 2]

    set thread [java::new ptolemy.domains.odf.kernel.ODFThread $actor $dir]

    $thread updateRcvrList $triple1
    $thread updateRcvrList $triple2
    set newtriple [$thread getHighestPriorityTriple]

    list [$thread getCurrentTime] [expr {$triple2 == $newtriple} ]

} {0.0 1}

######################################################################
####
#
test ODFThread-2.5 {getNextTime()} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor $topLevel "actor"] 

    set rcvr1 [java::new ptolemy.domains.odf.kernel.ODFReceiver]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.ODFReceiver]
    set rcvr3 [java::new ptolemy.domains.odf.kernel.ODFReceiver]
    set triple1 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr1 15.0 2]
    set triple2 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr2 5.0 1]
    set triple3 [java::new ptolemy.domains.odf.kernel.RcvrTimeTriple $rcvr3 6.0 3]

    set thread [java::new ptolemy.domains.odf.kernel.ODFThread $actor $dir]

    $thread updateRcvrList $triple1
    $thread updateRcvrList $triple2
    $thread updateRcvrList $triple3
    set newrcvr [$thread getFirstRcvr]

    list [$thread getNextTime] [expr {$rcvr2 == $newrcvr} ]

} {5.0 1}
