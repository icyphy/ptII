# Tests for the NamedList class
#
# @Author: Christopher Hylands
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
test NamedList-2.1 {Construct a list, call get} {
    set dir [java::new pt.kernel.util.NamedList]
    set n1 [java::new pt.kernel.util.NamedObj "n1"]
    set n2 [java::new pt.kernel.util.NamedObj "n2"]
    set n3 [java::new pt.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    set result1 [expr {$n1 == [$dir get "n1"]}]
    $dir prepend $n2
    $dir prepend $n3
    set result2 [expr {$n1 == [$dir get n1]}]
    set result3 [expr {$n3 == [$dir get n3]}]
    list $result1 $result2 $result3
} {1 1 1}

######################################################################
####
# 
test NamedList-4.1 {Test insertAt and last} {
    set dir [java::new pt.kernel.util.NamedList]
    set n1 [java::new pt.kernel.util.NamedObj "n1"]
    set n2 [java::new pt.kernel.util.NamedObj "n2"]
    set n3 [java::new pt.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir prepend $n2 
    $dir prepend $n3 
    list [[$dir get n1] getName]\
	    [[$dir get n2] getName] \
	    [[$dir get n3] getName] \
            [[$dir last] getName]
} [list n1 n2 n3 n1]

######################################################################
####
# 
test NamedList-5.1 {Test prepend} {
    set dir [java::new pt.kernel.util.NamedList]
    set n1 [java::new pt.kernel.util.NamedObj "n1"]
    set n2 [java::new pt.kernel.util.NamedObj "n2"]
    set n3 [java::new pt.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir prepend $n2 
    $dir prepend $n3 
    _testEnums getElements $dir
} {{n3 n2 n1}}

######################################################################
####
# 
test NamedList-5.2 {prepend with duplicate names} {
    set dir [java::new pt.kernel.util.NamedList]
    set n1 [java::new pt.kernel.util.NamedObj "n1"]
    set n2 [java::new pt.kernel.util.NamedObj "n1"]
    $dir prepend $n1
    catch {$dir prepend $n2} errMsg4
    list $errMsg4
} {{pt.kernel.util.NameDuplicationException: Attempt to insert object named "n1" into a container that already contains an object with that name.}}

######################################################################
####
# 
test NamedList-5.3 {prepend with a node with a null name} {
    set dir [java::new pt.kernel.util.NamedList]
    set n0 [java::new pt.kernel.util.NamedObj]
    $dir prepend $n0
    list [expr {$n0 == [$dir {get String} {}]}]
} {1}

######################################################################
####
# 
test NamedList-5.4 {prepend two nodes with null names} {
    set dir [java::new pt.kernel.util.NamedList]
    set n0 [java::new pt.kernel.util.NamedObj]
    set n00 [java::new pt.kernel.util.NamedObj]
    $dir prepend $n0
    catch {$dir prepend $n00} errMsg1
    list [expr {$n0 == [$dir {get String} ""]}] $errMsg1
} {1 {pt.kernel.util.NameDuplicationException: Attempt to insert object named "<Unnamed Object>" into a container that already contains an object with that name.}}

######################################################################
####
# 
test NamedList-6.1 {Test insertAfter} {
    set dir [java::new pt.kernel.util.NamedList]
    set n1 [java::new pt.kernel.util.NamedObj "n1"]
    set n2 [java::new pt.kernel.util.NamedObj "n2"]
    set n3 [java::new pt.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir insertAfter [$n1 getName] $n2
    $dir insertAfter [$n2 getName] $n3
    _testEnums getElements $dir
} {{n1 n2 n3}}

######################################################################
####
# 
test NamedList-6.2 {insertAfter with duplicate names} {
    set dir [java::new pt.kernel.util.NamedList]
    set n1 [java::new pt.kernel.util.NamedObj "n1"]
    set n2 [java::new pt.kernel.util.NamedObj "n1"]
    $dir prepend $n1
    catch {$dir insertAfter [$n1 getName] $n2} errMsg4
    list $errMsg4
} {{pt.kernel.util.NameDuplicationException: Attempt to insert object named "n1" into a container that already contains an object with that name.}}

######################################################################
####
# 
test NamedList-6.3 {insertAfter with a node with a null name} {
    set dir [java::new pt.kernel.util.NamedList]
    set n1 [java::new pt.kernel.util.NamedObj "n1"]
    set n0 [java::new pt.kernel.util.NamedObj]
    $dir prepend $n1
    $dir insertAfter [$n1 getName] $n0
} {}

######################################################################
####
# 
test NamedList-6.4 {insertAfter with two nodes with a null names} {
    set dir [java::new pt.kernel.util.NamedList]
    set n0 [java::new pt.kernel.util.NamedObj]
    set n00 [java::new pt.kernel.util.NamedObj]
    $dir prepend $n0
    catch {$dir insertAfter [$n1 getName] $n00} errMsg1
    list $errMsg1
} {{pt.kernel.util.NameDuplicationException: Attempt to insert object named "<Unnamed Object>" into a container that already contains an object with that name.}}

######################################################################
####
# 
test NamedList-8.1 {Test remove} {
    set dir [java::new pt.kernel.util.NamedList]
    set n1 [java::new pt.kernel.util.NamedObj "n1"]
    set n2 [java::new pt.kernel.util.NamedObj "n2"]
    set n3 [java::new pt.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir insertAfter [$n1 getName] $n2
    $dir insertAfter [$n2 getName] $n3
    set result1 [_testEnums getElements $dir]
    $dir {remove String} n2
    set result2 [_testEnums getElements $dir]
    $dir {remove pt.kernel.util.Nameable} $n3
    set result3 [_testEnums getElements $dir]
    $dir {remove String} n1
    set result4 [_testEnums getElements $dir]
    list $result1 $result2 $result3 $result4
} {{{n1 n2 n3}} {{n1 n3}} n1 {{}}}

######################################################################
####
# 
test NamedList-8.2 {Test remove} {
    set dir [java::new pt.kernel.util.NamedList]
    # Remove something that does not exist from an empty NamedList
    set result1 [expr {[java::null] == [$dir {remove String} n1]}]
    set n1 [java::new pt.kernel.util.NamedObj "n1"]
    set n2 [java::new pt.kernel.util.NamedObj "n2"]
    $dir append $n1
    $dir insertAfter [$n1 getName] $n2
    $dir {remove pt.kernel.util.Nameable} $n1
    # Remove something that has already been removed. 
    set result2 [expr {[java::null] == [$dir {remove String} n1]}]
    list $result1 $result2
} {1 1}

######################################################################
####
# 
test NamedList-9.1 {Test copy constructor} {
    set dir [java::new pt.kernel.util.NamedList]
    set n1 [java::new pt.kernel.util.NamedObj "n1"]
    set n2 [java::new pt.kernel.util.NamedObj "n2"]
    set n3 [java::new pt.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir prepend $n2 
    $dir prepend $n3 
    set result1 [_testEnums getElements $dir]
    set clone [java::new {pt.kernel.util.NamedList pt.kernel.util.NamedList} $dir]
    set result2 [_testEnums getElements $clone]
    $dir {remove pt.kernel.util.Nameable} $n2
    set result3 [_testEnums getElements $dir]
    list $result1 $result2 $result3
} {{{n3 n2 n1}} {{n3 n2 n1}} {{n3 n1}}}
