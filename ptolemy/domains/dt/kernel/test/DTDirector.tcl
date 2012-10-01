# Tests for the DTDirector class
#
# @Author: Christopher Hylands (based on Director.tcl by Edward A. Lee)
#
# @Version: $Id$
#
# @Copyright (c) 1997-2012 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


set w [java::new ptolemy.kernel.util.Workspace W]
set manager [java::new ptolemy.actor.Manager $w M]

######################################################################
####
#
test Director-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.domains.dt.kernel.DTDirector]
    $d1 setName D1
    set d2 [java::new ptolemy.domains.dt.kernel.DTDirector $w]
    $d2 setName D2
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.dt.kernel.DTDirector $e0 D3]

    # These methods could be abstract, but are not for testing purposes
    # so we call them here
    # do not call methods that access time objects before initialization $d1 fireAtCurrentTime $e0

    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 .E0.D3}

######################################################################
####
#
test Director-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [java::cast ptolemy.domains.dt.kernel.DTDirector [$d2 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {.M .D2 .E0}

######################################################################
####
#
test Director-4.1 {Test _makeDirectorOf} {
    # NOTE: Uses the setup above
    $e0 setManager $manager
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {.E0.D3 .D4 {.D2 .E0}}

######################################################################
####
#
test Director-5.1 {Test action methods} {
    set e2 [java::new ptolemy.actor.TypedCompositeActor]
    set manager [java::new ptolemy.actor.Manager]
    $e2 setName top2
    $e2 setManager $manager
    set director \
            [java::new ptolemy.domains.dt.kernel.DTDirector $e2 DTDirector]

    set iterationsParam [getParameter $director iterations]
    $iterationsParam setToken [java::new ptolemy.data.IntToken 5];
    
    set periodparam [getParameter $director period]
    $periodparam setToken [java::new ptolemy.data.DoubleToken 1.5];

    set b1 [java::new ptolemy.actor.lib.CurrentTime $e2 B1]
    set b2 [java::new ptolemy.actor.lib.Recorder $e2 B2]
    $e2 connect [java::field [java::cast ptolemy.actor.lib.Source $b1] output] 	    [java::field [java::cast ptolemy.actor.lib.Sink $b2] input]     R1
    #$a1 clear
    #set allowParam [getParameter $d3 allowDisconnectedGraphs]
    #$allowParam setToken [java::new ptolemy.data.BooleanToken true];
    #puts [$e2 exportMoML]
    $manager initialize
    set r1 [$director getNextIterationTime]
    #[$e2 getManager] execute
    $director iterate 3
    set r2 [$director getNextIterationTime]
    $director stop
    $director wrapup
    list $r1 $r2 [enumToTokenValues [$b2 getRecord 0]] \
	[$director getNextIterationTime] \
	[$director getCurrentTime]
} {1.5 6.0 {0.0 1.5 3.0} 1.5 0.0}

