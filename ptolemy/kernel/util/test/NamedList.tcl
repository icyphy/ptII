# Tests for the NamedList class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    set result1 [$n1 equals [$dir get "n1"]]
    $dir prepend $n2
    $dir prepend $n3
    set result2 [[$dir first] getName]
    set result3 [$n1 equals [$dir get "n1"]]
    set result4 [$n3 equals [$dir get "n3"]]
    list $result1 $result2 $result3 $result4
} {1 n3 1 1}

######################################################################
####
#
test NamedList-2.2 {Construct a list with a container} {
    set c1 [java::new ptolemy.kernel.util.NamedObj "c1"]
    set dir [java::new {ptolemy.kernel.util.NamedList \
	    ptolemy.kernel.util.Nameable} \
	    $c1]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    # Note that n3 has the same name as n1
    set n3 [java::new ptolemy.kernel.util.NamedObj "n1"]
    $dir prepend $n1
    $dir prepend $n2
    catch {$dir prepend $n3} errMsg
    list [_testEnums elements $dir] $errMsg
} {{{n2 n1}} {ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "n1" into container named ".c1", which already contains an object with that name.}}

######################################################################
####
#
test NamedList-3.1 {Test append by using a class that can take null names} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.test.TestNullNamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.test.TestNullNamedObj]
    set n3 [java::new ptolemy.kernel.util.test.TestNullNamedObj [java::null]]
    $dir append $n1
    $dir append $n2
    catch {$dir append $n3} errMsg
    list [_testEnums elements $dir] \
	    $errMsg
} {{{n1 {}}} {ptolemy.kernel.util.IllegalActionException: Attempt to add an object with a null name to a NamedList.}}

######################################################################
####
#
test NamedList-3.2 {Test clone} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj]
    $dir append $n1
    $dir append $n2
    $dir append $n3
    set clonedDir [java::cast ptolemy.kernel.util.NamedList [$dir clone]]
    _testEnums elements $clonedDir
} {{n1 n2 {}}}

test NamedList-3.2.1 {Test elementList} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj]
    $dir append $n1
    $dir append $n2
    $dir append $n3
    set clonedDir [java::cast ptolemy.kernel.util.NamedList [$dir clone]]
    listToFullNames [$clonedDir elementList]
} {.n1 .n2 .}

######################################################################
####
#
test NamedList-3.3 {Test includes} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj]
    set n4 [java::new ptolemy.kernel.util.NamedObj "n4"]
    $dir append $n1
    $dir append $n2
    $dir {remove ptolemy.kernel.util.Nameable} $n2
    $dir append $n3
    list [$dir includes $n1] \
	    [$dir includes $n2] \
	    [$dir includes $n3] \
	    [$dir includes $n4]

} {1 0 1 0}


######################################################################
####
#
test NamedList-4.1 {Test prepend, first last} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir prepend $n2
    $dir prepend $n3
    list [_testEnums elements $dir] \
            [[$dir first] getName] \
            [[$dir last] getName]
} {{{n3 n2 n1}} n3 n1}

######################################################################
####
#
test NamedList-4.1 {Test prepend with Nameables that can have null names } {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.test.TestNullNamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.test.TestNullNamedObj]
    set n3 [java::new ptolemy.kernel.util.test.TestNullNamedObj [java::null]]
    $dir prepend $n1
    $dir prepend $n2
    catch {$dir prepend $n3} errMsg
    list [_testEnums elements $dir] \
	    $errMsg
} {{{{} n1}} {ptolemy.kernel.util.IllegalActionException: Attempt to add an object with a null name to a NamedList.}}

######################################################################
####
#
test NamedList-4.3 {Test insertBefore} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj ""]
    set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
    $dir insertBefore "foo" $n1
    $dir insertBefore "n1" $n2
    $dir insertBefore "" $n3
    catch {$dir insertBefore "" $n3} errMsg
    list [_testEnums elements $dir] \
	    $errMsg
} {{{n3 {} n1}} {ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "n3" into a container that already contains an object with that name.}}

######################################################################
####
#
test NamedList-5.1 {Test prepend} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir prepend $n2
    $dir prepend $n3
    _testEnums elements $dir
} {{n3 n2 n1}}

######################################################################
####
#
test NamedList-5.2 {prepend with duplicate names} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n1"]
    $dir prepend $n1
    catch {$dir prepend $n2} errMsg4
    list $errMsg4
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "n1" into a container that already contains an object with that name.}}

######################################################################
####
#
test NamedList-5.3 {prepend with a node with a null name} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    $dir prepend $n0
    list [$n0 equals [$dir {get String} {}]]
} {1}

######################################################################
####
#
test NamedList-5.4 {prepend two nodes with null names} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    set n00 [java::new ptolemy.kernel.util.NamedObj]
    $dir prepend $n0
    catch {$dir prepend $n00} errMsg1
    list [$n0 equals [$dir {get String} ""]] $errMsg1
} {1 {ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "<Unnamed Object>" into a container that already contains an object with that name.}}

######################################################################
####
#
test NamedList-6.1 {Test insertAfter and removeAll } {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir insertAfter [$n1 getName] $n2
    $dir insertAfter [$n2 getName] $n3
    set result1 [_testEnums elements $dir]
    set size1 [$dir size]
    $dir removeAll
    set result2 [_testEnums elements $dir]
    set size2 [$dir size]
    list $result1 $size1 $result2 $size2
} {{{n1 n2 n3}} 3 {{}} 0}

######################################################################
####
#
test NamedList-6.2 {insertAfter with duplicate names} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n1"]
    $dir prepend $n1
    catch {$dir insertAfter [$n1 getName] $n2} errMsg4
    list $errMsg4
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "n1" into a container that already contains an object with that name.}}

######################################################################
####
#
test NamedList-6.3 {insertAfter with a node with a null name} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    $dir prepend $n1
    $dir insertAfter [$n1 getName] $n0
} {}

######################################################################
####
#
test NamedList-6.4 {insertAfter with two nodes with a null names} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    set n00 [java::new ptolemy.kernel.util.NamedObj]
    $dir prepend $n0
    catch {$dir insertAfter [$n1 getName] $n00} errMsg1
    list $errMsg1
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "<Unnamed Object>" into a container that already contains an object with that name.}}

######################################################################
####
#
test NamedList-8.1 {Test remove} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir insertAfter [$n1 getName] $n2
    $dir insertAfter [$n2 getName] $n3
    set result1 [_testEnums elements $dir]
    $dir {remove String} n2
    set result2 [_testEnums elements $dir]
    $dir {remove ptolemy.kernel.util.Nameable} $n3
    set result3 [_testEnums elements $dir]
    $dir {remove String} n1
    set result4 [_testEnums elements $dir]
    list $result1 $result2 $result3 $result4
} {{{n1 n2 n3}} {{n1 n3}} n1 {{}}}

######################################################################
####
#
test NamedList-8.2 {Test remove} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    # Remove something that does not exist from an empty NamedList
    set result1 [expr {[java::null] == [$dir {remove String} n1]}]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    $dir append $n1
    $dir insertAfter [$n1 getName] $n2
    $dir {remove ptolemy.kernel.util.Nameable} $n1
    # Remove something that has already been removed.
    set result2 [expr {[java::null] == [$dir {remove String} n1]}]
    list $result1 $result2
} {1 1}

######################################################################
####
#
test NamedList-9.1 {Test copy constructor} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir prepend $n2
    $dir prepend $n3
    set result1 [_testEnums elements $dir]
    set clone [java::new {ptolemy.kernel.util.NamedList ptolemy.kernel.util.NamedList} $dir]
    set result2 [_testEnums elements $clone]
    $dir {remove ptolemy.kernel.util.Nameable} $n2
    set result3 [_testEnums elements $dir]
    list $result1 $result2 $result3
} {{{n3 n2 n1}} {{n3 n2 n1}} {{n3 n1}}}


