# Tests for the abstract Entity class
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
test Entity-1.1 {Get information about an instance of Entity} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.Entity]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.Entity
  fields:        
  methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait getName {setName java.lang.String} getParams enumEntities {enumEntities java.lang.String} enumRelations {enumRelations java.lang.String} getPortList numberOfConnectedEntities {numberOfConnectedEntities java.lang.String}
  constructors:  pt.kernel.Entity {pt.kernel.Entity java.lang.String}
  properties:    portList class params name
  superclass:    pt.kernel.NamedObj
}}


######################################################################
####
# 
test Entity-2.1 {Construct Entities} {
    set e1 [java::new pt.kernel.Entity]
    set e2 [java::new pt.kernel.Entity "My Entity"]
    list [$e1 getName] [$e2 getName] 
} {{} {My Entity}}

######################################################################
####
# 
test Entity-2.2 {Construct Entities, call getPortList} {
    set e1 [java::new pt.kernel.Entity]
    set e2 [java::new pt.kernel.Entity "My Entity"]
    list [java::instanceof [$e1 getPortList] pt.kernel.PortList]
} {1}

######################################################################
####
# 
test Entity-2.3 {Construct Entities, call numberOfConnectedEntities} {
    set e1 [java::new pt.kernel.Entity]
    set e2 [java::new pt.kernel.Entity "My Entity"]
    list [$e1 numberOfConnectedEntities] [$e2 numberOfConnectedEntities] 
} {0 0}

######################################################################
####
# 
test Entity-2.4 {Test numberOfConnectedEntities(portname)} {
    set e1 [java::new pt.kernel.Entity]
    set e2 [java::new pt.kernel.Entity "My Entity"]
    list [$e1 numberOfConnectedEntities "Not a PortName"] \
	    [$e2 numberOfConnectedEntities "Not a PortName"] 
} {0 0}
	 

######################################################################
####
# 
test Entity-3.1 {Test enumEntities. Note that since Entity is abstract,
	the Entity enum code is better covered in the derived classes.} {
    set e1 [java::new pt.kernel.Entity "My Entity"]
    set enum [$e1 enumEntities]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Entity-3.2 {Test enumEntities(portName)} {
    set e1 [java::new pt.kernel.Entity "My Entity"]
    set enum [$e1 enumEntities "Not a Port"]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Entity-3.3 {Test enumRelations()} {
    set e1 [java::new pt.kernel.Entity "My Entity"]
    set enum [$e1 enumRelations]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Entity-3.4 {Test enumRelations()} {
    set e1 [java::new pt.kernel.Entity "My Entity"]
    set enum [$e1 enumRelations "Not a PortName"]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Entity-4.0 {Connect Entities} {
    # Create objects
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set print [java::new pt.kernel.Entity "Print"]
    set out [java::new pt.kernel.Port "Ramp out"]
    set in [java::new pt.kernel.Port "Print in"]
    set arc [java::new pt.kernel.test.RelationTest "Arc"]

    # Connect
    $out setEntity $ramp
    $in setEntity $print
    $out connectToRelation $arc
    $in connectToRelation $arc

    for {set enum [$ramp enumEntities]} \
	    {[$enum hasMoreElements] == 1} \
	    {} {
	set enumelement [$enum nextElement]
	lappend lresults [$enumelement getName]
    }
    list $lresults
} {}

