# Tests for PetriNetDirector
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

proc listenToDirector {model} {
    set parser [java::new ptolemy.moml.MoMLParser]

    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    # Change PetriNetDisplayer to PetriNetRecorder
    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]

    $parser purgeAllModelRecords
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile $model]]

    # Add a listener to the director
    #set director [$toplevel getAttribute {PetriNet Director}]
    #set listener [java::new ptolemy.kernel.util.RecorderListener]    
    #$director addDebugListener $listener

    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] myManager]
    $toplevel setManager $manager

    $manager execute
    # The RemoveGraphicalClasses filter changes PetriNetDisplay into PetriNet
    set recorder [java::cast ptolemy.domains.petrinet.lib.PetriNetRecorder \
		      [$toplevel getEntity PetriNetDisplay]]
    return [$recorder getMessages]
}

######################################################################
####
#
test PetriNetDirector-1.0 {Read PetriNetSimpleTest.xml, listen to the director and run it} {
    listenToDirector "../demo/PetriNetSimple/PetriNetSimple.xml"
} {Place Place2 
6     0      
5     1      
4     2      
3     3      
2     4      
1     5      
0     6      
}


######################################################################
####
#
test PetriNetDirector-2.0 {Read PetriNetDiningPhilosophers, listen to the director and run it} {
    listenToDirector "../demo/PetriNetDiningPhilosophers/PetriNetDiningPhilosophers.xml"
} {p0  p1  p2  p3  p4  p5  p6  p7  p8  p9  
1   1   1   1   1   0   0   0   0   0   
0   0   1   0   0   1   0   0   1   0   
1   1   1   1   1   0   0   0   0   0   
0   0   0   0   1   1   0   1   0   0   
1   1   1   1   1   0   0   0   0   0   
0   0   1   0   0   1   0   0   1   0   
1   1   1   1   1   0   0   0   0   0   
0   0   0   0   1   1   0   1   0   0   
1   1   1   1   1   0   0   0   0   0   
0   1   0   0   0   0   0   1   0   1   
1   1   1   1   1   0   0   0   0   0   
0   1   0   0   0   0   0   1   0   1   
1   1   1   1   1   0   0   0   0   0   
1   0   0   0   0   0   1   0   1   0   
1   1   1   1   1   0   0   0   0   0   
0   0   1   0   0   1   0   0   1   0   
1   1   1   1   1   0   0   0   0   0   
0   0   1   0   0   1   0   0   1   0   
1   1   1   1   1   0   0   0   0   0   
0   0   0   1   0   0   1   0   0   1   
}