# Tests for the CompositeEntity class
#
# @Author: Christopher Hylands
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
test CompositeEntity-1.1 {Get information about an instance \
	of CompositeEntity} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.CompositeEntity]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.CompositeEntity
  fields:        
  methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait getName {setName java.lang.String} getParams enumEntities {enumEntities java.lang.String} enumRelations {enumRelations java.lang.String} getPortList numberOfConnectedEntities {numberOfConnectedEntities java.lang.String} getAllContents getContainer getFullName isAtomic {setContainer pt.kernel.HierEntity} {setContents pt.kernel.NamedObjList}
  constructors:  pt.kernel.CompositeEntity {pt.kernel.CompositeEntity java.lang.String}
  properties:    fullName allContents portList class contents params atomic name container
  superclass:    pt.kernel.HierEntity
}}

######################################################################
####
# 
test CompositeEntity-1.1 {Construct CompositeEntities, call a few methods} {
    set e1 [java::new pt.kernel.CompositeEntity]
    set e2 [java::new pt.kernel.CompositeEntity "My CompositeEntity"]
    set e1contents [$e1 getAllContents]
    list [$e1 getName] [$e2 getName] \
	    [$e1 getFullName] [$e2 getFullName] \
	    [$e1 isAtomic] [$e2 isAtomic] \
	    [ java::instanceof $e1contents pt.kernel.NamedObjList] \
	    [expr {[java::null] == [$e1 getContainer]}]
} {{} {My CompositeEntity} {} {My CompositeEntity} 0 0 1 1}

######################################################################
####
# 
test CompositeEntity-2.1 {Create a 3 level deep tree} {
    # FIXME, not done yet
    set grandparent [java::new pt.kernel.HierEntity "grandparent"]
    set parent1 [java::new pt.kernel.HierEntity "parent1"]
    set parent2 [java::new pt.kernel.HierEntity "parent2"]
    set child [java::new pt.kernel.HierEntity "child"]
    list {}
} {{}}
