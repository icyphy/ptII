# Tests for the IODependence class
#
# @Author: Haiyang Zheng
#
# @Version: IODependence.tcl,v 1.2 2003/08/23 19:22:10 cxh Exp
#
# @Copyright (c) 2003 The Regents of the University of California.
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
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test IODependence-2.1 {Constructor tests before preinitialization} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setName E1
    set d1 [java::new ptolemy.actor.Director]
    $d1 setName D1
    $e1 setDirector $d1

    $e1 setManager $manager

    set e2 [java::new ptolemy.actor.CompositeActor $e1 E2]
    set d2 [java::new ptolemy.actor.Director]
    $d2 setName D2
    $e2 setDirector $d2

    list [expr {[$e1 getDirector] == $d1}]  \
            [expr {[$e2 getDirector] == $d2}] \
	    [expr {[[java::cast ptolemy.kernel.util.NamedObj $e1] getAttribute "_IODependence"] == [java::null]}] \
            [expr {[[java::cast ptolemy.kernel.util.NamedObj $e2] getAttribute "_IODependence"] == [java::null]}] 
} {1 1 1 1}

######################################################################
####
#
test IODependence-2.2 {Constructor tests after preinitialization} {
    # NOTE: Uses the setup above

	$e1 preinitialize 
	$e1 initialize
	$e1 prefire
	$e1 fire
	
	list    [expr {[[java::cast ptolemy.kernel.util.NamedObj $e1] getAttribute "_IODependence"] == [java::null]}] \
            [expr {[[java::cast ptolemy.kernel.util.NamedObj $e2] getAttribute "_IODependence"] == [java::null]}] 
} {1 1}

######################################################################
####
#
test IODependence-3.1 {Resolve IODependence without feedback loop} {
    # NOTE: Uses the setup above

    set p1 [java::new ptolemy.actor.IOPort $e2 P1 true false]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 false true]

    set r1 [java::new ptolemy.actor.IORelation $e2 R1]
    set r2 [java::new ptolemy.actor.IORelation $e2 R2]

    set e3 [java::new ptolemy.actor.AtomicActor $e2 E3]
    set p3 [java::new ptolemy.actor.IOPort $e3 P3 true false]
    set p4 [java::new ptolemy.actor.IOPort $e3 P4 false true]

	$p1 link $r1
	$p2 link $r2
	$p3 link $r1
	$p4 link $r2
	
	catch {$e1 preinitialize} msg
	
    list    $msg
} {{}}

######################################################################
####
#
test IODependence-3.2 {Resolve IODependence with self feedback loop} {
    # NOTE: Uses the setup above

	# create a self feedback loop
    $p3 setMultiport true
	$p3 link $r2
	
	catch {$e1 preinitialize} msg
	
    list    $msg
} {{}}

######################################################################
####
#
test IODependence-3.3 {Resolve IODependence without feedback loop} {
    # NOTE: Uses the setup above

	# restore 3.1
    $p3 setMultiport false
	$p3 unlink $r2
	
	# create a feedback loop
    set e4 [java::new ptolemy.actor.AtomicActor $e2 E4]
    set p5 [java::new ptolemy.actor.IOPort $e3 P5 true false]
    set p6 [java::new ptolemy.actor.IOPort $e3 P6 false true]
	
	$p6 link $r1
	$p5 link $r2
	
	catch {$e1 preinitialize} msg
	
    list    $msg
} {{}}
