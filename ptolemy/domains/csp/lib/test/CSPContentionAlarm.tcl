# Tests for the CSPContentionAlarm class
#
# @Author: John S. Davis II
#
# @version $Id$
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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
test CSPContentionAlarm-2.1 {Check that alarm "rings" at proper time} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    set dir [java::new ptolemy.domains.csp.kernel.CSPDirector $wspc "director"]
    $topLevel setDirector $dir
    set cntrllr [java::new ptolemy.domains.csp.lib.CSPController $topLevel "cntrllr"] 
    set proc [java::new ptolemy.domains.csp.lib.CSPProcessor $topLevel "proc"] 
    set alarm [java::new ptolemy.domains.csp.lib.CSPContentionAlarm $topLevel "alarm"] 

    set reqOut [$cntrllr getPort "requestOut"]
    set reqIn [$cntrllr getPort "requestIn"]

    set conOut [$cntrllr getPort "contendOut"]
    set conIn [$cntrllr getPort "contendIn"]

    set alarmOut [$alarm getPort "output"]
    set alarmIn [$alarm getPort "input"]

    set pReqOut [$proc getPort "requestOut"]
    set pReqIn [$proc getPort "requestIn"]

    set rel1 [$topLevel connect $reqOut $pReqIn "rel1"]
    set rel2 [$topLevel connect $reqIn $pReqOut "rel2"]
    set rel3 [$topLevel connect $conIn $alarmIn "rel3"]
    set rel4 [$topLevel connect $conIn $alarmOut "rel4"]

    set t [java::new ptolemy.data.Token]
    set e1 [java::new ptolemy.domains.od.kernel.CSPContentionAlarm $t 5.0]
    list [$e1 getTime] 
} {5.0}

