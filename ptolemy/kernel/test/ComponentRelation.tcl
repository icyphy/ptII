# Tests for the ComponentRelation class
#
# @Author: Jie Liu
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
#
test ComponentRelation-2.1 {Constructor} {
    set r1 [java::new ptolemy.kernel.ComponentRelation]
    set r2 [java::new ptolemy.kernel.ComponentRelation]
    $r2 setName R2
    set w [java::new ptolemy.kernel.util.Workspace]
    set e [java::new ptolemy.kernel.CompositeEntity $w]
    $e setName E
    set r3 [java::new ptolemy.kernel.ComponentEntity $e R3]

    # Test out the constructor that takes a Workspace arg
    set r4 [java::new ptolemy.kernel.ComponentRelation $w]
    set w2 [java::new ptolemy.kernel.util.Workspace "workspace2"]
    set r5 [java::new ptolemy.kernel.ComponentRelation $w2]
    set r6 [java::new ptolemy.kernel.ComponentRelation [java::null]]

    list [$r1 getFullName] [$r2 getFullName] [$r3 getFullName] \
	    [$r4 getFullName] [$r5 getFullName] [$r6 getFullName]
} {. .R2 .E.R3 . . .}

######################################################################
####
#
test ComponentRelation-3.1 {Test for NameDuplicationException in constructor} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.ComponentRelation $a B]
    catch {[java::new ptolemy.kernel.ComponentRelation $a B]} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "B" into container named ".A", which already contains an object with that name.}}
######################################################################
####
#
test ComponentRelation-3.2 {Test for NameDuplicationException on setName} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b1 [java::new ptolemy.kernel.ComponentRelation $a B1]
    set b2 [java::new ptolemy.kernel.ComponentRelation $a B2]
    catch {$b2 setName B1} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: .A:
already contains a relation with the name B1.}}
######################################################################
####
#
test ComponentRelation-3.3 {Test for setName back} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b1 [java::new ptolemy.kernel.ComponentRelation $a B1]
    $b1 setName B1
    $b1 getFullName
} {.A.B1}
