# Tests for the Relation class
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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
if {[info procs _testRelationEnumPorts] == "" } then { 
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
test Relation-1.1 {Get information about an instance of Relation} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.Relation]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.Relation
  fields:        
  methods:       {description int} {equals java.lang.Object} getClass ge
    tContainer getFullName getName hashCode linkedEntities 
    linkedPorts {linkedPortsExcept pt.kernel.Port} notify n
    otifyAll numLinks {setName java.lang.String} toString {
    unlink pt.kernel.Port} unlinkAll wait {wait long} {wait
     long int} workspace
    
  constructors:  pt.kernel.Relation {pt.kernel.Relation java.lang.String
    } {pt.kernel.Relation pt.kernel.Workspace java.lang.Str
    ing}
    
  properties:    class container fullName name
    
  superclass:    pt.kernel.NamedObj
    
}}

######################################################################
####
# 
test Relation-2.1 {Construct Relations, checks numLinks on empty Relations} {
    set r1 [java::new pt.kernel.Relation]
    set r2 [java::new pt.kernel.Relation "My Relation"]
    list [$r1 numLinks] [$r2 numLinks]
} {0 0}

######################################################################
####
# 
test Relation-3.1 {Test linkedPorts on a Relation that has no ports} {
    set r1 [java::new pt.kernel.Relation]
    set enum  [$r1 linkedPorts]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Relation-4.1 {Test linkedPortsExcept on a Relation that has no ports} {
    set r1 [java::new pt.kernel.Relation]
    set p1 [java::new pt.kernel.Port]
    set enum  [$r1 linkedPortsExcept $p1]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Relation-5.1 {Test linkedEntities on a Relation that has no ports} {
    set r1 [java::new pt.kernel.Relation]
    set enum  [$r1 linkedEntities]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Relation-6.1 {Test a Relation with three ports} {
    set r1 [java::new pt.kernel.Relation]
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port $e1 P1]
    set p2 [java::new pt.kernel.Port $e1 P2]
    set p3 [java::new pt.kernel.Port $e1 P3]
    $p3 setContainer $e1
    $p1 link $r1
    $p2 link $r1
    $p3 link $r1
    list [$r1 numLinks] [_testRelationLinkedPorts $r1]
} {3 {{P1 P2 P3}}}

######################################################################
####
# 
test Relation-7.1 {Test a Relation with one named port} {
    set r1 [java::new pt.kernel.Relation "my relation"]
    set e1 [java::new pt.kernel.Entity "my entity"]
    set p1 [java::new pt.kernel.Port $e1 "my port"]
    $p1 link $r1
    list [$r1 numLinks] [_testRelationLinkedPorts $r1]
} {1 {{{my port}}}}

######################################################################
####
# 
test Relation-8.1 {Test a Relation with two named ports} {
    set r1 [java::new pt.kernel.Relation "my relation"]
    set e1 [java::new pt.kernel.Entity "my entity"]
    set p1 [java::new pt.kernel.Port $e1 "my port"]
    set p2 [java::new pt.kernel.Port $e1 "my other port"]
    $p1 link $r1
    $p2 link $r1
    list [$r1 numLinks] [_testRelationLinkedPorts $r1]
} {2 {{{my port} {my other port}}}}

######################################################################
####
# 
test Relation-9.1 {Test a linkedEntities with two named ports, same entity} {
    set r1 [java::new pt.kernel.Relation "my relation"]
    set e1 [java::new pt.kernel.Entity "my entity"]
    set p1 [java::new pt.kernel.Port $e1 "my port"]
    set p2 [java::new pt.kernel.Port $e1 "my other port"]
    $p1 link $r1
    $p2 link $r1
    list [$r1 numLinks] [_testRelationLinkedEntities $r1]
} {2 {{{my entity} {my entity}}}}

######################################################################
####
# 
test Relation-10.1 {linkedEntities with two named ports, distinct entities} {
    set r1 [java::new pt.kernel.Relation "my relation"]
    set e1 [java::new pt.kernel.Entity "my entity"]
    set e2 [java::new pt.kernel.Entity "other entity"]
    set p1 [java::new pt.kernel.Port $e1 "my port"]
    set p2 [java::new pt.kernel.Port $e2 "my other port"]
    $p1 link $r1
    $p2 link $r1
    list [$r1 numLinks] [_testRelationLinkedEntities $r1]
} {2 {{{my entity} {other entity}}}}

######################################################################
####
# 
test Relation-11.1 {unlink a port} {
    set r1 [java::new pt.kernel.Relation "my relation"]
    set e1 [java::new pt.kernel.Entity "my entity"]
    set e2 [java::new pt.kernel.Entity "other entity"]
    set p1 [java::new pt.kernel.Port $e1 "my port"]
    set p2 [java::new pt.kernel.Port $e2 "my other port"]
    $p1 link $r1
    $p2 link $r1
    $r1 unlink $p1
    list [$r1 numLinks] [_testRelationLinkedEntities $r1]
} {1 {{{other entity}}}}

######################################################################
####
# 
test Relation-12.1 {unlinkALL ports} {
    set r1 [java::new pt.kernel.Relation "my relation"]
    set e1 [java::new pt.kernel.Entity "my entity"]
    set e2 [java::new pt.kernel.Entity "other entity"]
    set p1 [java::new pt.kernel.Port $e1 "my port"]
    set p2 [java::new pt.kernel.Port $e2 "my other port"]
    $p1 link $r1
    $p2 link $r1
    $r1 unlinkAll 
    list [$r1 numLinks] [_testRelationLinkedEntities $r1]
} {0 {{}}}
