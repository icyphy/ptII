# Tests for the ComponentEntity class
#
# @Author: Edward A. Lee, Jie Liu, Christopher Hylands
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
test ComponentEntity-2.1 {Construct entities} {
    set e1 [java::new ptolemy.kernel.ComponentEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity]
    $e2 setName A
    set w [java::new ptolemy.kernel.util.Workspace]
    set e3 [java::new ptolemy.kernel.CompositeEntity $w]
    $e3 setName B
    set e4 [java::new ptolemy.kernel.ComponentEntity $e3 B]
    list [$e1 getFullName] [$e2 getFullName] [$e3 getFullName] [$e4 getFullName]
} {. .A .B .B.B}

######################################################################
####
#
test ComponentEntity-3.1 {add ports} {
    set e1 [java::new ptolemy.kernel.ComponentEntity]
    $e1 setName X
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 A]
    set p2 [java::new ptolemy.kernel.ComponentPort $e1 B]
    list [$p1 getFullName] [$p2 getFullName] [_testEntityGetPorts $e1]
} {.X.A .X.B {{A B}}}

######################################################################
####
#
test ComponentEntity-4.1 {is atomic test} {
    set e1 [java::new ptolemy.kernel.ComponentEntity]
    list [$e1 isAtomic]
} {1}

######################################################################
####
#
test ComponentEntity-5.1 {Create new ports} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.ComponentEntity $w]
    $e1 setName Y
    set p1 [$e1 newPort A]
    set p2 [$e1 newPort B]
    list [$p1 getFullName] [$p2 getFullName] [_testEntityGetPorts $e1]
} {.Y.A .Y.B {{A B}}}

test ComponentEntity-5.2 {Test clone} {
    set e2 [java::cast ptolemy.kernel.ComponentEntity [$e1 clone]]
    $e2 description 31
} {ptolemy.kernel.ComponentEntity {.Y} ports {
    {ptolemy.kernel.ComponentPort {.Y.A} links {
    } insidelinks {
    }}
    {ptolemy.kernel.ComponentPort {.Y.B} links {
    } insidelinks {
    }}
}}

######################################################################
####
#
test ComponentEntity-5.5 {move* methods with no container} {
    set n [java::new ptolemy.kernel.ComponentEntity]
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

test ComponentEntity-5.5.1 {moveDown} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set a1  [java::new ptolemy.kernel.ComponentEntity $top a1]
    set a2  [java::new ptolemy.kernel.ComponentEntity $top a2]
    set a3  [java::new ptolemy.kernel.ComponentEntity $top a3]
    set result1 [listToNames [$top deepEntityList]]
    $a1 moveDown
    set result2 [listToNames [$top deepEntityList]]
    $a1 moveDown
    # Can't go past the bottom
    $a1 moveDown
    set result3 [listToNames [$top deepEntityList]]
    list $result1 $result2 $result3
} {{a1 a2 a3} {a2 a1 a3} {a2 a3 a1}}

test ComponentEntity-5.5.2 {moveToFirst} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set a1  [java::new ptolemy.kernel.ComponentEntity $top a1]
    set a2  [java::new ptolemy.kernel.ComponentEntity $top a2]
    set a3  [java::new ptolemy.kernel.ComponentEntity $top a3]
    set result1 [listToNames [$top deepEntityList]]
    $a1 moveToFirst
    set result2 [listToNames [$top deepEntityList]]
    $a2 moveToFirst
    set result3 [listToNames [$top deepEntityList]]
    $a3 moveToFirst
    set result4 [listToNames [$top deepEntityList]]
    $a3 moveToFirst
    set result5 [listToNames [$top deepEntityList]]	
    list $result1 $result2 $result3 $result4 $result5
} {{a1 a2 a3} {a1 a2 a3} {a2 a1 a3} {a3 a2 a1} {a3 a2 a1}}

test ComponentEntity-5.5.3 {moveToIndex} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set a1  [java::new ptolemy.kernel.ComponentEntity $top a1]
    set a2  [java::new ptolemy.kernel.ComponentEntity $top a2]
    set a3  [java::new ptolemy.kernel.ComponentEntity $top a3]
    set result1 [listToNames [$top deepEntityList]]
    catch {$a1 moveToIndex -1} msg
    set result2 $msg
    $a2 moveToIndex 0
    set result3 [listToNames [$top deepEntityList]]
    $a3 moveToIndex 1
    set result4 [listToNames [$top deepEntityList]]
    $a3 moveToIndex 2
    set result5 [listToNames [$top deepEntityList]]	
    catch {$a3 moveToIndex 3} result6
    list $result1 $result2 $result3 $result4 $result5 $result6
} {{a1 a2 a3} {ptolemy.kernel.util.IllegalActionException: Index out of range.
  in .<Unnamed Object>.a1} {a2 a1 a3} {a2 a3 a1} {a2 a1 a3} {ptolemy.kernel.util.IllegalActionException: Index out of range.
  in .<Unnamed Object>.a3}}

test ComponentEntity-5.5.4 {moveToLast} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set a1  [java::new ptolemy.kernel.ComponentEntity $top a1]
    set a2  [java::new ptolemy.kernel.ComponentEntity $top a2]
    set a3  [java::new ptolemy.kernel.ComponentEntity $top a3]
    set result1 [listToNames [$top deepEntityList]]
    $a1 moveToLast
    set result2 [listToNames [$top deepEntityList]]
    $a2 moveToLast
    set result3 [listToNames [$top deepEntityList]]
    $a3 moveToLast
    set result4 [listToNames [$top deepEntityList]]
    $a3 moveToLast
    set result5 [listToNames [$top deepEntityList]]	
    list $result1 $result2 $result3 $result4 $result5
} {{a1 a2 a3} {a2 a3 a1} {a3 a1 a2} {a1 a2 a3} {a1 a2 a3}}

test ComponentEntity-5.5.5 {moveUp} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set a1  [java::new ptolemy.kernel.ComponentEntity $top a1]
    set a2  [java::new ptolemy.kernel.ComponentEntity $top a2]
    set a3  [java::new ptolemy.kernel.ComponentEntity $top a3]
    set result1 [listToNames [$top deepEntityList]]
    $a3 moveUp
    set result2 [listToNames [$top deepEntityList]]
    $a1 moveUp
    # Can't go past the top
    $a1 moveUp
    set result3 [listToNames [$top deepEntityList]]
    list $result1 $result2 $result3
} {{a1 a2 a3} {a1 a3 a2} {a1 a3 a2}}

######################################################################
####
#
test ComponentEntity-6.1 {Reparent entities} {
    set e1 [java::new ptolemy.kernel.util.Workspace A]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1]
    $e2 setName B
    set e3 [java::new ptolemy.kernel.CompositeEntity $e2 C]
    set e4 [java::new ptolemy.kernel.ComponentEntity $e3 D]
    set result1 [list [$e1 getFullName] [$e2 getFullName] \
            [$e3 getFullName] [$e4 getFullName]]
    $e4 setContainer $e2
    set result2 [list [$e1 getFullName] [$e2 getFullName] \
            [$e3 getFullName] [$e4 getFullName]]
    $e3 setContainer [java::null]
    set result3 [list [$e1 getFullName] [$e2 getFullName] \
            [$e3 getFullName] [$e4 getFullName]]
    list $result1 $result2 $result3
} {{A .B .B.C .B.C.D} {A .B .B.C .B.D} {A .B .C .B.D}}

######################################################################
####
#
test ComponentEntity-7.1 {Reparent entities, attempting a circular structure} {
    set e1 [java::new ptolemy.kernel.util.Workspace A]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1]
    $e2 setName B
    set e3 [java::new ptolemy.kernel.CompositeEntity $e2 C]
    catch {$e2 setContainer $e3} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Attempt to construct recursive containment
  in .B and .B.C}}

######################################################################
####
#
test ComponentEntity-8.1 {Test for NameDuplicationException on constructor} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.ComponentEntity $a B]
    catch {[java::new ptolemy.kernel.ComponentEntity $a B]} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "B" into container named ".A", which already contains an object with that name.}}

test ComponentEntity-8.2 {Test for NameDuplicationException on setName} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b1 [java::new ptolemy.kernel.ComponentEntity $a B1]
    set b2 [java::new ptolemy.kernel.ComponentEntity $a B2]
    catch {$b2 setName B1} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Name duplication: B1
  in .A}}

########################################################################
####
#
test ComponentEntity-9.1 {remove a port} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.ComponentEntity $a B]
    set p1 [java::new ptolemy.kernel.ComponentPort $b P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $b P2]
    $p1 setContainer [java::null]
    $b exportMoML
} {<entity name="B" class="ptolemy.kernel.ComponentEntity">
    <port name="P2" class="ptolemy.kernel.ComponentPort">
    </port>
</entity>
}
