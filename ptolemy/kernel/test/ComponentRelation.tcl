# Tests for the ComponentRelation class
#
# @Author: Jie Liu, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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

#
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
test ComponentRelation-5.5 {move* methods with no container} {
    set n [java::new ptolemy.kernel.ComponentRelation]
    catch {$n moveDown} msg1
    catch {$n moveToFirst} msg2
    catch {$n moveToIndex 0} msg3
    catch {$n moveToLast} msg4
    catch {$n moveUp} msg5
    list $msg1 $msg2 $msg3 $msg4 $msg5
} {{ptolemy.kernel.util.IllegalActionException: Has no container.
  in .<Unnamed Object>} {ptolemy.kernel.util.IllegalActionException: Has no container.
  in .<Unnamed Object>} {ptolemy.kernel.util.IllegalActionException: Has no container.
  in .<Unnamed Object>} {ptolemy.kernel.util.IllegalActionException: Has no container.
  in .<Unnamed Object>} {ptolemy.kernel.util.IllegalActionException: Has no container.
  in .<Unnamed Object>}}

test ComponentRelation-5.5.1 {moveDown} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set a1  [java::new ptolemy.kernel.ComponentRelation $top a1]
    set a2  [java::new ptolemy.kernel.ComponentRelation $top a2]
    set a3  [java::new ptolemy.kernel.ComponentRelation $top a3]
    set result1 [listToNames [$top relationList]]
    $a1 moveDown
    set result2 [listToNames [$top relationList]]
    $a1 moveDown
    # Can't go past the bottom
    $a1 moveDown
    set result3 [listToNames [$top relationList]]
    list $result1 $result2 $result3
} {{a1 a2 a3} {a2 a1 a3} {a2 a3 a1}}

test ComponentRelation-5.5.2 {moveToFirst} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set a1  [java::new ptolemy.kernel.ComponentRelation $top a1]
    set a2  [java::new ptolemy.kernel.ComponentRelation $top a2]
    set a3  [java::new ptolemy.kernel.ComponentRelation $top a3]
    set result1 [listToNames [$top relationList]]
    $a1 moveToFirst
    set result2 [listToNames [$top relationList]]
    $a2 moveToFirst
    set result3 [listToNames [$top relationList]]
    $a3 moveToFirst
    set result4 [listToNames [$top relationList]]
    $a3 moveToFirst
    set result5 [listToNames [$top relationList]]	
    list $result1 $result2 $result3 $result4 $result5
} {{a1 a2 a3} {a1 a2 a3} {a2 a1 a3} {a3 a2 a1} {a3 a2 a1}}

test ComponentRelation-5.5.3 {moveToIndex} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set a1  [java::new ptolemy.kernel.ComponentRelation $top a1]
    set a2  [java::new ptolemy.kernel.ComponentRelation $top a2]
    set a3  [java::new ptolemy.kernel.ComponentRelation $top a3]
    set result1 [listToNames [$top relationList]]
    catch {$a1 moveToIndex -1} msg
    set result2 $msg
    $a2 moveToIndex 0
    set result3 [listToNames [$top relationList]]
    $a3 moveToIndex 1
    set result4 [listToNames [$top relationList]]
    $a3 moveToIndex 2
    set result5 [listToNames [$top relationList]]	
    catch {$a3 moveToIndex 3} result6
    list $result1 $result2 $result3 $result4 $result5 $result6
} {{a1 a2 a3} {ptolemy.kernel.util.IllegalActionException: Index out of range.
  in .<Unnamed Object>.a1} {a2 a1 a3} {a2 a3 a1} {a2 a1 a3} {ptolemy.kernel.util.IllegalActionException: Index out of range.
  in .<Unnamed Object>.a3}}

test ComponentRelation-5.5.4 {moveToLast} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set a1  [java::new ptolemy.kernel.ComponentRelation $top a1]
    set a2  [java::new ptolemy.kernel.ComponentRelation $top a2]
    set a3  [java::new ptolemy.kernel.ComponentRelation $top a3]
    set result1 [listToNames [$top relationList]]
    $a1 moveToLast
    set result2 [listToNames [$top relationList]]
    $a2 moveToLast
    set result3 [listToNames [$top relationList]]
    $a3 moveToLast
    set result4 [listToNames [$top relationList]]
    $a3 moveToLast
    set result5 [listToNames [$top relationList]]	
    list $result1 $result2 $result3 $result4 $result5
} {{a1 a2 a3} {a2 a3 a1} {a3 a1 a2} {a1 a2 a3} {a1 a2 a3}}

test ComponentRelation-5.5.5 {moveUp} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set a1  [java::new ptolemy.kernel.ComponentRelation $top a1]
    set a2  [java::new ptolemy.kernel.ComponentRelation $top a2]
    set a3  [java::new ptolemy.kernel.ComponentRelation $top a3]
    set result1 [listToNames [$top relationList]]
    $a3 moveUp
    set result2 [listToNames [$top relationList]]
    $a1 moveUp
    # Can't go past the top
    $a1 moveUp
    set result3 [listToNames [$top relationList]]
    list $result1 $result2 $result3
} {{a1 a2 a3} {a1 a3 a2} {a1 a3 a2}}

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
} {{ptolemy.kernel.util.NameDuplicationException: Name duplication: B1
  in .A}}

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
