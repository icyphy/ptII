# Tests for the PNActor class
#
# @Author: Mudit Goel
#
# @Version: $Id$
#
# @Copyright (c) 1997-.  The Regents of the University of California.
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
if {[info procs _testEntityLinkedRelations] == "" } then { 
    source testEnums.tcl
}

# Load up Tcl procs to print out enums
if {[info procs enumToNames] == "" } then { 
    source enums.tcl
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
test PNActor-1.1 {Get information about an instance of Entity} {
    # If anything changes, we want to know about it so we can write tests.
    set parent [java::new pt.domains.pn.kernel.PNCompositeActor]
    set n [java::new pt.domains.pn.kernel.PNActor $parent actor]
    list [getJavaInfo $n]
} {{
  class:         pt.domains.pn.kernel.PNActor
  fields:        
  methods:       {addParam pt.data.Param} {addPort pt.kernel.Port} conne
    ctedPorts {description int} {equals java.lang.Object} f
    ire getClass getContainer getDirector getFullName getNa
    me {getParam java.lang.String} getParams {getPort java.
    lang.String} getPorts hashCode initialize isAtomic link
    edRelations {newInPort pt.domains.pn.kernel.PNActor jav
    a.lang.String} {newOutPort pt.domains.pn.kernel.PNActor
     java.lang.String} {newPort java.lang.String} {newPort 
    pt.domains.pn.kernel.PNActor java.lang.String} notify n
    otifyAll postfire prefire {readFrom pt.domains.pn.kerne
    l.PNInPort pt.actors.IORelation} removeAllPorts {remove
    Param java.lang.String} {removePort pt.kernel.Port} run
     {setContainer pt.kernel.CompositeEntity} {setCycles in
    t} {setName java.lang.String} {setParam java.lang.Strin
    g double} {setThread java.lang.Thread} stop toString wa
    it {wait long} {wait long int} workspace wrapup {writeT
    o pt.domains.pn.kernel.PNPort pt.data.Token}
    
  constructors:  {pt.domains.pn.kernel.PNActor pt.actors.CompositeActor 
    java.lang.String}
    
  properties:    atomic class container cycles director fullName name pa
    rams ports thread
    
  superclass:    pt.actors.Actor
    
}}


######################################################################
####
# 
test PNActor-2.1 {Construct Actors and test their names} {
    set parent [java::new pt.domains.pn.kernel.PNCompositeActor]
    set n [java::new pt.domains.pn.kernel.PNActor $parent actor]
    list [$n getName] [$n getFullName] 
} {actor ..actor}

######################################################################
####
# 
test PNActor-2.2 {Construct Entities, call newInPort and getPorts} {
    set parent [java::new pt.domains.pn.kernel.PNCompositeActor]
    set n [java::new pt.domains.pn.kernel.PNActor $parent actor]
    set p1 [$n newInPort $n inport1]
    set p2 [$n newInPort $n inport2]
    list [enumToNames [$n getPorts]]
} {{inport1 inport2}}

######################################################################
####
#

test PNActor-2.3 {Construct Entities, call newOutPort and getPorts} {
    set parent [java::new pt.domains.pn.kernel.PNCompositeActor]
    set n [java::new pt.domains.pn.kernel.PNActor $parent actor]
    set p1 [$n newOutPort $n outport1]
    set p2 [$n newOutPort $n outport2]
    set p3 [$n newInPort $n inport1]
    list [enumToNames [$n getPorts]]
} {{outport1 outport2 inport1}}

######################################################################
####
#
 
test PNActor-3.0 {Connect Ports} {
    # Create objects
    set parent [java::new pt.domains.pn.kernel.PNCompositeActor]
    set ramp [java::new pt.domains.pn.kernel.PNActor $parent "Ramp"]
    set sink [java::new pt.domains.pn.kernel.PNActor $parent "Sink"]
    set out [$ramp newOutPort $ramp outport1]
    set in [$sink newInPort $sink inport1]

    # Connect
    set arc [$parent connect $in $out arc]

    list [enumToNames [$ramp connectedPorts]] \
     [enumToNames [$ramp linkedRelations]]
} {inport1 arc}

