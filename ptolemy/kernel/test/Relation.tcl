# Tests for the Relation class
#
# @Author: 
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
test Relation-1.1 {Get information about an instance of Relation} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.Relation]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.Relation
  fields:        
  methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait getName {setName java.lang.String} getParams enumPorts {enumPortsExcept pt.kernel.Port} enumEntities {isPortConnected java.lang.String} numberOfConnections
  constructors:  pt.kernel.Relation {pt.kernel.Relation java.lang.String}
  properties:    class params name
  superclass:    pt.kernel.NamedObj
}}

######################################################################
####
# 
test Relation-2.1 {Construct Relations, call some methods on empty Relations} {
    set r1 [java::new pt.kernel.Relation]
    set r2 [java::new pt.kernel.Relation "My Relation"]
    list [$r1 numberOfConnections] \
	    [$r2 numberOfConnections] \
	    [$r1 isPortConnected "not a port"] \
	    [$r2 isPortConnected "not a port"]
} {0 0 0 0}

######################################################################
####
# 
test Relation-3.1 {Test enumPorts on a Relation that has no ports} {
    set r1 [java::new pt.kernel.Relation]
    set enum  [$r1 enumPorts]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Relation-3.2 {Test enumPorts on a Relation that has no ports} {
    set r1 [java::new pt.kernel.Relation "my relation"]
    set enum  [$r1 enumPorts]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Relation-4.1 {Test enumPortsExcept on a Relation that has no ports} {
    set r1 [java::new pt.kernel.Relation]
    set p1 [java::new pt.kernel.Port "My Port"]
    set enum  [$r1 enumPortsExcept $p1]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Relation-4.2 {Test enumPortsExcept on a Relation that has no ports} {
    set r1 [java::new pt.kernel.Relation "my relation"]
    set p1 [java::new pt.kernel.Port "My Port"]
    set enum  [$r1 enumPortsExcept $p1]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Relation-5.1 {Test enumEntities on a Relation that has no ports} {
    set r1 [java::new pt.kernel.Relation]
    set enum  [$r1 enumEntities]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Relation-5.2 {Test enumEntities on a Relation that has no ports} {
    set r1 [java::new pt.kernel.Relation "my relation"]
    set enum  [$r1 enumEntities]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}
