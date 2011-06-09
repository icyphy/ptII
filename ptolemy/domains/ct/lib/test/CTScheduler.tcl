# Test CTScheduler
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2008 The Regents of the University of California.
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

######################################################################
#### 
#
test CTScheduler {A model that has top level ports that are not connected} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser addMoMLFilter [java::new ptolemy.moml.filter.RemoveGraphicalClasses]
    set topLevel [java::cast ptolemy.actor.CompositeActor \
		[$parser {parse java.net.URL java.net.URL} \
		[java::cast {java.net.URL} [java::null]] \
		[[java::new java.io.File CTTopLevelUnconnectedPorts.xml] toURL]]]
    set manager [java::new ptolemy.actor.Manager [$topLevel workspace] "foo"]
    $topLevel setManager $manager
    catch {$manager execute} errMsg 	
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: The container ".CTTopLevelUnconnectedPorts" is a "ptolemy.actor.TypedCompositeActor", which is not a CT Composite actor, yet the port ".CTTopLevelUnconnectedPorts.level1" appears to have no receivers?  Perhaps the port is not connected?
  in .CTTopLevelUnconnectedPorts.Continuous Time (CT) Solver.CTScheduler
Because:
0}}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]

