# Tests for the TimeKeeper class
#
# @Author: John S. Davis II
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
# Global Variables 
set globalIgnoreTime -1
set globalEndTimeRcvr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue]
set globalInactiveTime [java::field $globalEndTimeRcvr INACTIVE]

######################################################################
####
#
test TimeKeeper-2.1 {instantiate objects} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $topLevel "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]

    set tok [java::new ptolemy.data.Token]

    list 1
} {1}

######################################################################
####
# Continued from above
test TimeKeeper-3.1 {getNextTime()} {
    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr1 put $tok 15.0
    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr2 put $tok 5.0
    set rcvr3 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 3]
    $rcvr3 put $tok 6.0

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    $keeper updateRcvrList $rcvr2
    $keeper updateRcvrList $rcvr3

    set newrcvr [$keeper getFirstRcvr]

    list [$keeper getNextTime] [expr {$rcvr2 == $newrcvr} ]

} {5.0 1}

######################################################################
####
# Continued from above
test TimeKeeper-3.2 {getNextTime()} {
    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 20]
    $rcvr1 put $tok $globalIgnoreTime
    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr2 put $tok 5.0
    set rcvr3 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 3]
    $rcvr3 put $tok 5.0

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    $keeper updateRcvrList $rcvr2
    $keeper updateRcvrList $rcvr3

    set newrcvr [$keeper getFirstRcvr]

    list [$keeper getNextTime] [expr {$rcvr3 == $newrcvr} ]

} {5.0 1}

######################################################################
####
# Continued from above
test TimeKeeper-3.3 {getNextTime()} {
    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 20]
    $rcvr1 put $tok $globalIgnoreTime
    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr2 put $tok $globalInactiveTime

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    $keeper updateRcvrList $rcvr2

    set newrcvr [$keeper getFirstRcvr]

    list [$keeper getNextTime] [expr {$rcvr1 == $newrcvr} ]

} {-1.0 1}

######################################################################
####
# Continued from above
test TimeKeeper-3.4 {getNextTime()} {
    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr1 put $tok $globalInactiveTime
    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 5]
    $rcvr2 put $tok $globalInactiveTime

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    $keeper updateRcvrList $rcvr2

    set newrcvr [$keeper getFirstRcvr]

    list [$keeper getNextTime] [expr {$rcvr2 == $newrcvr} ]

} {-2.0 1}

######################################################################
####
#
test TimeKeeper-4.1 {Call Methods On Uninitialized TimeKeeper} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $topLevel "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    set val 1
    if { [$keeper getCurrentTime] != 0.0 } {
	set val 0
    }
    if { [$keeper getNextTime] != 0.0 } {
	set val 0
    }
    if { ![java::isnull [$keeper getFirstRcvr]] } {
	set val 0
    }

    list $val;

} {1}

######################################################################
####
#
test TimeKeeper-5.1 {Ignore Tokens} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $topLevel "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set tok [java::new ptolemy.data.Token]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr1 put $tok 0.0

    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 4]
    $rcvr2 put $tok $globalIgnoreTime

    set val 0
    if { [$rcvr1 hasToken] == 1 } {
	if { [$rcvr2 hasToken] == 1 } {
	    set val 1
	}
    }

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    $keeper updateRcvrList $rcvr2

    $keeper removeAllIgnoreTokens

    set newVal 1
    if { [$rcvr2 hasToken] == 0 } {
	set newVal 0
    }

    list $val $newVal

} {1 0}

######################################################################
####
# Continued from above.
test TimeKeeper-5.2 {Ignore Tokens} {
    
    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr1 put $tok 0.0

    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 4]
    $rcvr2 put $tok $globalIgnoreTime
    $rcvr2 put $tok 3.0

    set val 0
    if { [$rcvr1 hasToken] == 1 } {
	if { [$rcvr2 hasToken] == 1 } {
	    set val 1
	}
    }

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    $keeper updateRcvrList $rcvr2

    $keeper removeAllIgnoreTokens

    set newVal 1
    if { [$rcvr2 hasToken] == 0 } {
	set newVal 0
    }

    set time [$rcvr2 getRcvrTime]

    list $val $time

} {1 3.0}

######################################################################
####
# Continued from above.
test TimeKeeper-5.3 {Ignore Tokens} {
    
    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr1 put $tok $globalIgnoreTime

    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 4]
    $rcvr2 put $tok $globalIgnoreTime

    set val 0
    if { [$rcvr1 hasToken] == 1 } {
	if { [$rcvr2 hasToken] == 1 } {
	    set val 1
	}
    }

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    $keeper updateRcvrList $rcvr2

    $keeper removeAllIgnoreTokens

    set newVal 1
    if { [$rcvr1 hasToken] == 0 } {
	if { [$rcvr1 hasToken] == 0 } {
	    set newVal 0
	}
    }

    set time [$rcvr2 getRcvrTime]

    list $val $newVal

} {1 0}






