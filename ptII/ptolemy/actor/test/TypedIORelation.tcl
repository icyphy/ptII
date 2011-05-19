# Tests for the TypedIORelation class
#
# @Author: Christopher Hylands
#
# $Id$
#
# @Copyright (c) 1999-2009 The Regents of the University of California.
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

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test TypedIORelation-1.1 {Construct Relations} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set p2 [java::new ptolemy.actor.TypedIOPort $e1 P2]

    #link up p1, p2
    set r0 [java::new ptolemy.actor.TypedIORelation ]
    catch {$p1 link $r0} msg1

    set r1 [java::new ptolemy.actor.TypedIORelation [$e1 workspace]]
    catch {$p1 link $r1} msg2

    set r2 [java::new ptolemy.actor.TypedIORelation $e0 R1]
	catch {$p1 link $r2} msg3
	catch {$p2 link $r2} msg4

    list [$r0 getFullName] [$r1 getFullName] [$r2 getFullName] \
	    $msg1 \
	    $msg2 \
		$msg3 \
		$msg4
} {. . ..R1 {} {ptolemy.kernel.util.IllegalActionException: Attempt to link more than one relation to a single port.
  in .<Unnamed Object>.E1.P1 and .<Unnamed Object>} {ptolemy.kernel.util.IllegalActionException: Attempt to link more than one relation to a single port.
  in .<Unnamed Object>.E1.P1 and .<Unnamed Object>.R1} {}}

test TypedIORelation-1.2 {Attempt to set erroneous container} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.CompositeActor]
    set r1 [java::new ptolemy.actor.TypedIORelation]
    catch {$r1 setContainer $e1} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: TypedIORelation can only be contained by TypedCompositeActor.
  in .<Unnamed Object> and .<Unnamed Object>}}


test TypedIORelation-3.0 {Test _checkPort} {
    set p3 [java::new ptolemy.actor.IOPort]
    set r0 [java::new ptolemy.actor.test.TestTypedIORelation]
    catch {$r0 checkPort $p3 } msg

    list [$r0 getFullName] $msg
} {. {ptolemy.kernel.util.IllegalActionException: TypedIORelation can only link to a TypedIOPort.
  in .<Unnamed Object> and .<Unnamed Object>}}
