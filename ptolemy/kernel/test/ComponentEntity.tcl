# Tests for the ComponentEntity class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997 The Regents of the University of California.
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

# Load up Tcl procs to print out enums
if {[info procs _testEntityGetPorts] == "" } then { 
    source testEnums.tcl
}

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
test ComponentEntity-1.1 {Get information about an instance of ComponentEntity} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.ComponentEntity]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.ComponentEntity
  fields:        
  methods:       {addPort pt.kernel.Port} connectedPorts {description in
    t} {equals java.lang.Object} getClass getContainer getF
    ullName getName {getPort java.lang.String} getPorts has
    hCode isAtomic linkedRelations {newPort java.lang.Strin
    g} notify notifyAll removeAllPorts {removePort pt.kerne
    l.Port} {setContainer pt.kernel.CompositeEntity} {setNa
    me java.lang.String} toString wait {wait long} {wait lo
    ng int} workspace
    
  constructors:  pt.kernel.ComponentEntity {pt.kernel.ComponentEntity pt
    .kernel.CompositeEntity java.lang.String} {pt.kernel.Co
    mponentEntity pt.kernel.Workspace}
    
  properties:    atomic class container fullName name ports
    
  superclass:    pt.kernel.Entity
    
}}

######################################################################
####
# 
test ComponentEntity-2.1 {Construct entities} {
    set e1 [java::new pt.kernel.ComponentEntity]
    set e2 [java::new pt.kernel.ComponentEntity]
    $e2 setName A
    set w [java::new pt.kernel.Workspace]
    set e3 [java::new pt.kernel.CompositeEntity $w]
    $e3 setName B
    set e4 [java::new pt.kernel.ComponentEntity $e3 B]
    list [$e1 getFullName] [$e2 getFullName] [$e3 getFullName] [$e4 getFullName]
} {. .A .B .B.B}

######################################################################
####
# 
test ComponentEntity-3.1 {add ports} {
    set e1 [java::new pt.kernel.ComponentEntity]
    $e1 setName X
    set p1 [java::new pt.kernel.ComponentPort $e1 A]
    set p2 [java::new pt.kernel.ComponentPort $e1 B]
    list [$p1 getFullName] [$p2 getFullName] [_testEntityGetPorts $e1]
} {.X.A .X.B {{A B}}}

######################################################################
####
# 
test ComponentEntity-4.1 {is atomic test} {
    set e1 [java::new pt.kernel.ComponentEntity]
    list [$e1 isAtomic]
} {1}

######################################################################
####
# 
test ComponentEntity-5.1 {Create new ports} {
    set w [java::new pt.kernel.Workspace X]
    set e1 [java::new pt.kernel.ComponentEntity $w]
    $e1 setName Y
    set p1 [$e1 newPort A]
    set p2 [$e1 newPort B]
    list [$p1 getFullName] [$p2 getFullName] [_testEntityGetPorts $e1]
} {X.Y.A X.Y.B {{A B}}}

######################################################################
####
# 
test ComponentEntity-6.1 {Reparent entities} {
    set e1 [java::new pt.kernel.Workspace A]
    set e2 [java::new pt.kernel.CompositeEntity $e1]
    $e2 setName B
    set e3 [java::new pt.kernel.CompositeEntity $e2 C]
    set e4 [java::new pt.kernel.ComponentEntity $e3 D]
    set result1 [list [$e1 getFullName] [$e2 getFullName] \
            [$e3 getFullName] [$e4 getFullName]]
    $e4 setContainer $e2
    set result2 [list [$e1 getFullName] [$e2 getFullName] \
            [$e3 getFullName] [$e4 getFullName]]
    $e3 setContainer [java::null]
    set result3 [list [$e1 getFullName] [$e2 getFullName] \
            [$e3 getFullName] [$e4 getFullName]]
    list $result1 $result2 $result3
} {{A A.B A.B.C A.B.C.D} {A A.B A.B.C A.B.D} {A A.B A.C A.B.D}}

######################################################################
####
# 
test ComponentEntity-7.1 {Reparent entities, attempting a circular structure} {
    set e1 [java::new pt.kernel.Workspace A]
    set e2 [java::new pt.kernel.CompositeEntity $e1]
    $e2 setName B
    set e3 [java::new pt.kernel.CompositeEntity $e2 C]
    catch {$e2 setContainer $e3} msg
    list $msg
} {{pt.kernel.IllegalActionException: A.B and A.B.C: Attempt to construct recursive containment.}}
