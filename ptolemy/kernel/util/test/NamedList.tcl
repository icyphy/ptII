# Tests for the NamedList class
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

# Load up Tcl procs to print out enums
if {[info procs _testEnums] == "" } then { 
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
test NamedList-1.1 {Get information about an instance of NamedObj} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.NamedList]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.NamedList
  fields:        
  methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait {append pt.kernel.Nameable} first {get java.lang.String} {get int} getElements {getIndexOf pt.kernel.Nameable} {getIndexOf java.lang.String} {insertAfter java.lang.String pt.kernel.Nameable} {insertAt int pt.kernel.Nameable} {insertBefore java.lang.String pt.kernel.Nameable} last {prepend pt.kernel.Nameable} {remove pt.kernel.Nameable} {remove java.lang.String} removeAll size
  constructors:  pt.kernel.NamedList {pt.kernel.NamedList pt.kernel.Nameable} {pt.kernel.NamedList pt.kernel.NamedList}
  properties:    elements class {{}}
  superclass:    java.lang.Object
}}

######################################################################
####
# 
test NamedList-2.1 {Construct a list, call getIndexOf and get} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n2"]
    set n3 [java::new pt.kernel.NamedObj "n3"]
    $dir prepend $n1
    set result1 [list [$dir {getIndexOf String} n1] \
	    [expr {$n1 == [$dir {get String} "n1"]}]]
    $dir prepend $n2
    $dir prepend $n3
    set result2 [list [$dir {getIndexOf String} n1] \
	    [expr {$n1 == [$dir {get String} n1]}]]
    set result3 [list [$dir {getIndexOf String} n3] \
	    [expr {$n3 == [$dir {get String} n3]}]]
    list $result1 $result2 $result3
} {{0 1} {2 1} {0 1}}

######################################################################
####
# 
test NamedList-3.1 {Test getIndexOf} {
    set dir [java::new pt.kernel.NamedList]
    catch {$dir {getIndexOf String} ""} errMsg1
    catch {$dir {getIndexOf String} {}} errMsg2
    catch {$dir {getIndexOf String} foo} errMsg3
    list $errMsg1 $errMsg2 $errMsg3
} {-1 -1 -1}

######################################################################
####
# 
test NamedList-4.1 {Test insertAt and last} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n2"]
    set n3 [java::new pt.kernel.NamedObj "n3"]
    $dir insertAt 0 $n1
    $dir insertAt 0 $n2 
    $dir insertAt 0 $n3 
    list [$dir {getIndexOf String} n1] \
	    [$dir {getIndexOf String} n2] \
	    [$dir {getIndexOf String} n3] \
            [[$dir last] getName]
} [list 2 1 0 n1]

######################################################################
####
# 
test NamedList-4.2 {insertAt with duplicate names} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n1"]
    $dir insertAt 0 $n1
    catch {$dir insertAt 0 $n2} errMsg4
    list $errMsg4
} {{pt.kernel.NameDuplicationException: Attempt to insert object named "n1" into a container that already contains an object with that name.}}

######################################################################
####
# 
test NamedList-4.3 {insertAt with a node with a null name and test first} {
    set dir [java::new pt.kernel.NamedList]
    set n0 [java::new pt.kernel.NamedObj]
    $dir insertAt 0 $n0
    list [expr {$n0 == [$dir first]}] [expr {$n0 == [$dir {get String} {}]}]
} [list 1 1]

######################################################################
####
# 
test NamedList-5.1 {Test prepend} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n2"]
    set n3 [java::new pt.kernel.NamedObj "n3"]
    $dir prepend $n1
    $dir prepend $n2 
    $dir prepend $n3 
    list [$dir {getIndexOf String} n1] \
	    [$dir {getIndexOf String} n2] \
	    [$dir {getIndexOf String} n3]
} {2 1 0}

######################################################################
####
# 
test NamedList-5.2 {prepend with duplicate names} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n1"]
    $dir prepend $n1
    catch {$dir prepend $n2} errMsg4
    list $errMsg4
} {{pt.kernel.NameDuplicationException: Attempt to insert object named "n1" into a container that already contains an object with that name.}}

######################################################################
####
# 
test NamedList-5.3 {prepend with a node with a null name} {
    set dir [java::new pt.kernel.NamedList]
    set n0 [java::new pt.kernel.NamedObj]
    $dir prepend $n0
    list [expr {$n0 == [$dir {get String} {}]}]
} {1}

######################################################################
####
# 
test NamedList-5.4 {prepend two nodes with null names} {
    set dir [java::new pt.kernel.NamedList]
    set n0 [java::new pt.kernel.NamedObj]
    set n00 [java::new pt.kernel.NamedObj]
    $dir prepend $n0
    catch {$dir prepend $n00} errMsg1
    list [expr {$n0 == [$dir {get String} ""]}] $errMsg1
} {1 {pt.kernel.NameDuplicationException: Attempt to insert object named "<Unnamed Object>" into a container that already contains an object with that name.}}

######################################################################
####
# 
test NamedList-6.1 {Test insertAfter} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n2"]
    set n3 [java::new pt.kernel.NamedObj "n3"]
    $dir prepend $n1
    $dir insertAfter [$n1 getName] $n2
    $dir insertAfter [$n2 getName] $n3
    list [$dir {getIndexOf String} n1] \
	    [$dir {getIndexOf String} n2] \
	    [$dir {getIndexOf String} n3]
} {0 1 2}

######################################################################
####
# 
test NamedList-6.2 {insertAfter with duplicate names} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n1"]
    $dir prepend $n1
    catch {$dir insertAfter [$n1 getName] $n2} errMsg4
    list $errMsg4
} {{pt.kernel.NameDuplicationException: Attempt to insert object named "n1" into a container that already contains an object with that name.}}

######################################################################
####
# 
test NamedList-6.3 {insertAfter with a node with a null name} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n0 [java::new pt.kernel.NamedObj]
    $dir prepend $n1
    $dir insertAfter [$n1 getName] $n0
} {}

######################################################################
####
# 
test NamedList-6.4 {insertAfter with two nodes with a null names} {
    set dir [java::new pt.kernel.NamedList]
    set n0 [java::new pt.kernel.NamedObj]
    set n00 [java::new pt.kernel.NamedObj]
    $dir prepend $n0
    catch {$dir insertAfter [$n1 getName] $n00} errMsg1
    list $errMsg1
} {{pt.kernel.NameDuplicationException: Attempt to insert object named "<Unnamed Object>" into a container that already contains an object with that name.}}

######################################################################
####
# 
test NamedList-7.1 {Test getIndexOf} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n2"]
    set n3 [java::new pt.kernel.NamedObj "n3"]
    $dir prepend $n1
    $dir insertAfter [$n1 getName] $n2
    $dir insertAfter [$n2 getName] $n3
    catch {$dir {getIndexOf String} "not a node"} errMsg1
    list [$dir {getIndexOf String} n1] \
	    [$dir {getIndexOf String} n2] \
	    [$dir {getIndexOf String} n3] \
	    $errMsg1
} {0 1 2 -1}

######################################################################
####
# 
test NamedList-7.2 {getIndexOf with a node with a null name} {
    set dir [java::new pt.kernel.NamedList]
    set n0 [java::new pt.kernel.NamedObj]
    $dir insertAt 0 $n0
    list [$dir {getIndexOf String} {}]
} {0}

######################################################################
####
# 
test NamedList-8.1 {Test remove} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n2"]
    set n3 [java::new pt.kernel.NamedObj "n3"]
    $dir prepend $n1
    $dir insertAfter [$n1 getName] $n2
    $dir insertAfter [$n2 getName] $n3
    set result1 [list [$dir {getIndexOf String} n1] \
	    [$dir {getIndexOf String} n2] \
	    [$dir {getIndexOf String} n3]]
    $dir {remove String} n2
    set result2 [list [$dir {getIndexOf String} n1] \
	    [catch {$dir {getIndexOf String} n2}] \
	    [$dir {getIndexOf String} n3]]
    $dir {remove pt.kernel.Nameable} $n3
    set result3 [list [$dir {getIndexOf String} n1] \
	    [catch {$dir {getIndexOf String} n2}] \
	    [catch {$dir {getIndexOf String} n3}]]
    $dir {remove String} n1
    set result4 [list [catch {$dir {getIndexOf String} n1}] \
	    [catch {$dir {getIndexOf String} n2}] \
	    [catch {$dir {getIndexOf String} n3}]]

    list $result1 $result2 $result3 $result4
} {{0 1 2} {0 0 1} {0 0 0} {0 0 0}}

######################################################################
####
# 
test NamedList-8.2 {Test remove} {
    set dir [java::new pt.kernel.NamedList]
    # Remove something that does not exist from an empty NamedList
    set result1 [expr {[java::null] == [$dir {remove String} n1]}]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n2"]
    $dir append $n1
    $dir insertAfter [$n1 getName] $n2
    $dir {remove pt.kernel.Nameable} $n1
    # Remove something that has already been removed. 
    set result2 [expr {[java::null] == [$dir {remove String} n1]}]
    list $result1 $result2
} {1 1}

######################################################################
####
# 
test NamedList-9.1 {Test copy constructor} {
    set dir [java::new pt.kernel.NamedList]
    set n1 [java::new pt.kernel.NamedObj "n1"]
    set n2 [java::new pt.kernel.NamedObj "n2"]
    set n3 [java::new pt.kernel.NamedObj "n3"]
    $dir prepend $n1
    $dir prepend $n2 
    $dir prepend $n3 
    set result1 [_testEnums getElements $dir]
    set clone [java::new {pt.kernel.NamedList pt.kernel.NamedList} $dir]
    set result2 [_testEnums getElements $clone]
    $dir {remove pt.kernel.Nameable} $n2
    set result3 [_testEnums getElements $dir]
    list $result1 $result2 $result3
} {{{n3 n2 n1}} {{n3 n2 n1}} {{n3 n1}}}
