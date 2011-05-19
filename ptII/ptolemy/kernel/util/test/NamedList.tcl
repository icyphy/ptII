# Tests for the NamedList class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2008 The Regents of the University of California.
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

# Load up Tcl procs to print out enums
if {[info procs _testEnums] == "" } then {
    source testEnums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
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
test NamedList-3.1.5 {Test append with duplicate names} {
    set dir [java::new ptolemy.kernel.util.NamedList]

    # Two NamedObjs in two different workspaces with the same name
    set W1a [java::new ptolemy.kernel.util.Workspace "W1a"]
    set W1b [java::new ptolemy.kernel.util.Workspace "W1b"]
    set n1a [java::new ptolemy.kernel.util.NamedObj $W1a "n1"]
    set n1b [java::new ptolemy.kernel.util.NamedObj $W1b "n1"]
    $dir append $n1a

    catch {$dir append $n1a} errMsg
    catch {$dir append $n1b} errMsg2

    # NamedLists are not Sets
    set namedSet [java::new java.util.HashSet]
    $namedSet add $n1a
    # In a NamedList, this would fail, but not in a Set
    $namedSet add $n1b
    $namedSet add $n1a

    # NamedLists are not Maps
    set namedMap [java::new java.util.HashMap]
    $namedMap put $n1a [$n1a getFullName]
    # In a NamedList, this would fail, but not in a Map
    $namedMap put $n1b [$n1b getFullName]
    $namedMap put $n1a [$n1a getFullName]

    list "$errMsg \n $errMsg2"
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "n1" into a container that already contains an object with that name. 
 ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "n1" into a container that already contains an object with that name.}}

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

######################################################################
#### Test methods for moving elements in a list
#
test NamedList-10.1 {Construct a list, call get} {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
    $dir append $n1
    $dir append $n2
    $dir append $n3
    _testEnums elements $dir
} {{n1 n2 n3}}

test NamedList-10.2 {Move an element to the top} {
    $dir moveToFirst $n3
    _testEnums elements $dir
} {{n3 n1 n2}}

test NamedList-10.3 {Move an element to the end} {
    $dir moveToLast $n1
    _testEnums elements $dir
} {{n3 n2 n1}}

test NamedList-10.4 {Move the last element down} {
    $dir moveDown $n1
    _testEnums elements $dir
} {{n3 n2 n1}}

test NamedList-10.5 {Move an element down} {
    $dir moveDown $n2
    _testEnums elements $dir
} {{n3 n1 n2}}

test NamedList-10.6 {Move an element down} {
    $dir moveDown $n3
    _testEnums elements $dir
} {{n1 n3 n2}}

test NamedList-10.8 {Move the first element up} {
    $dir moveUp $n1
    _testEnums elements $dir
} {{n1 n3 n2}}

test NamedList-10.9 {Move an element up} {
    $dir moveUp $n3
    _testEnums elements $dir
} {{n3 n1 n2}}

test NamedList-10.10 {Move an element up} {
    $dir moveUp $n2
    _testEnums elements $dir
} {{n3 n2 n1}}

test NamedList-10.11 {Move to specified index} {
    $dir moveToIndex $n2 0
    _testEnums elements $dir
} {{n2 n3 n1}}

# Test NamedLists that have 100 elements
proc createNamedList {size} {
    set workspace [java::new ptolemy.kernel.util.Workspace "w$size"]
    set namedList [java::new ptolemy.kernel.util.NamedList]
    for {set i 0} {$i < $size} {incr i} {
	$namedList append [java::new ptolemy.kernel.util.NamedObj $workspace "n$i"]
    }
    return $namedList
}

set namedList99 [createNamedList 99]
set namedList100 [createNamedList 100]
set namedList101 [createNamedList 101]
set namedList102 [createNamedList 102]
set namedList202 [createNamedList 202]

test NamedList-100.1 {get} {
    set r1_99 [[$namedList99 get n98] getName]
    set r1_100 [[$namedList100 get n99] getName]
    set r1_101 [[$namedList101 get n100] getName]
    set r1_102 [[$namedList102 get n101] getName]
    set r1_202 [[$namedList202 get n201] getName]
    list $r1_99 $r1_100 $r1_101 $r1_102 $r1_202
} {n98 n99 n100 n101 n201}

test NamedList-100.2 {includes} {
    set r1_99 [$namedList99 includes [$namedList99 get n98]]
    set r1_100 [$namedList100 includes [$namedList100 get n99]]
    set r1_101 [$namedList101 includes [$namedList101 get n100]]
    set r1_102 [$namedList102 includes [$namedList102 get n101]]
    set r1_202 [$namedList202 includes [$namedList202 get n201]]

    set r2_99 [$namedList99 includes [$namedList100 get n99]]
    set r2_100 [$namedList100 includes [$namedList101 get n100]]
    set r2_101 [$namedList100 includes [$namedList101 get n101]]
    set r2_102 [$namedList100 includes [$namedList101 get n102]]
    set r2_202 [$namedList100 includes [$namedList101 get n202]]

    list [list $r1_99 $r1_100 $r1_101 $r1_102 $r1_202] \
	[list $r2_99 $r2_100 $r2_101 $r2_102 $r2_202]
} {{1 1 1 1 1} {0 0 0 0 0}}

test NamedList-100.3 {insertAfter} {
    set namedList99 [createNamedList 99]
    set namedList100 [createNamedList 100]
    set namedList101 [createNamedList 101]
    set namedList102 [createNamedList 102]
    set namedList202 [createNamedList 202]

    $namedList99 insertAfter n98 [java::new ptolemy.kernel.util.NamedObj n99]
    $namedList100 insertAfter n99 [java::new ptolemy.kernel.util.NamedObj n100]
    $namedList101 insertAfter n100 [java::new ptolemy.kernel.util.NamedObj n101]
    $namedList102 insertAfter n101 [java::new ptolemy.kernel.util.NamedObj n102]
    $namedList202 insertAfter n201 [java::new ptolemy.kernel.util.NamedObj n202]

    set r1_99 [$namedList99 size]
    set r1_100 [$namedList100 size]
    set r1_101 [$namedList101 size]
    set r1_102 [$namedList102 size]
    set r1_202 [$namedList202 size]

    set r2_99 [[$namedList99 get n99] getName]
    set r2_100 [[$namedList100 get n100] getName]
    set r2_101 [[$namedList101 get n101] getName]
    set r2_102 [[$namedList102 get n102] getName]
    set r2_202 [[$namedList202 get n202] getName]

    list [list $r1_99 $r1_100 $r1_101 $r1_102 $r1_202] \
	[list $r2_99 $r2_100 $r2_101 $r2_102 $r2_202]
} {{100 101 102 103 203} {n99 n100 n101 n102 n202}}

test NamedList-100.4 {insertBefore} {
    # Reset
    set namedList99 [createNamedList 99]
    set namedList100 [createNamedList 100]
    set namedList101 [createNamedList 101]
    set namedList102 [createNamedList 102]
    set namedList202 [createNamedList 202]

    $namedList99 insertBefore n98 [java::new ptolemy.kernel.util.NamedObj n97b]
    $namedList100 insertBefore n99 [java::new ptolemy.kernel.util.NamedObj n98b]
    $namedList101 insertBefore n100 [java::new ptolemy.kernel.util.NamedObj n99b]
    $namedList102 insertBefore n101 [java::new ptolemy.kernel.util.NamedObj n100b]
    $namedList202 insertBefore n201 [java::new ptolemy.kernel.util.NamedObj n200b]

    set r1_99 [$namedList99 size]
    set r1_100 [$namedList100 size]
    set r1_101 [$namedList101 size]
    set r1_102 [$namedList102 size]
    set r1_202 [$namedList202 size]

    set r2_99 [[$namedList99 get n97b] getName]
    set r2_100 [[$namedList100 get n98b] getName]
    set r2_101 [[$namedList101 get n99b] getName]
    set r2_102 [[$namedList102 get n100b] getName]
    set r2_202 [[$namedList202 get n200b] getName]

    list [list $r1_99 $r1_100 $r1_101 $r1_102 $r1_202] \
	[list $r2_99 $r2_100 $r2_101 $r2_102 $r2_202]
} {{100 101 102 103 203} {n97b n98b n99b n100b n200b}}

test NamedList-100.5 {prepend} {
    # Reset
    set namedList99 [createNamedList 99]
    set namedList100 [createNamedList 100]
    set namedList101 [createNamedList 101]
    set namedList102 [createNamedList 102]
    set namedList202 [createNamedList 202]

    $namedList99 prepend [java::new ptolemy.kernel.util.NamedObj nA]
    $namedList100 prepend [java::new ptolemy.kernel.util.NamedObj nB]
    $namedList101 prepend [java::new ptolemy.kernel.util.NamedObj nC]    
    $namedList102 prepend [java::new ptolemy.kernel.util.NamedObj nD]
    $namedList202 prepend [java::new ptolemy.kernel.util.NamedObj nE]

    set r1_99 [$namedList99 size]
    set r1_100 [$namedList100 size]
    set r1_101 [$namedList101 size]
    set r1_102 [$namedList102 size]
    set r1_202 [$namedList202 size]

    set r2_99 [[$namedList99 get nA] getName]
    set r2_100 [[$namedList100 get nB] getName]
    set r2_101 [[$namedList101 get nC] getName]
    set r2_102 [[$namedList102 get nD] getName]
    set r2_202 [[$namedList202 get nE] getName]

    list [list $r1_99 $r1_100 $r1_101 $r1_102 $r1_202] \
	[list $r2_99 $r2_100 $r2_101 $r2_102 $r2_202]
} {{100 101 102 103 203} {nA nB nC nD nE}}

test NamedList-100.6 {remove Nameable} {
    # Reset
    set namedList99 [createNamedList 99]
    set namedList100 [createNamedList 100]
    set namedList101 [createNamedList 101]
    set namedList102 [createNamedList 102]
    set namedList202 [createNamedList 202]

    $namedList99 remove [$namedList99 get n98]
    $namedList100 remove [$namedList100 get n99]
    $namedList101 remove [$namedList101 get n100]
    $namedList102 remove [$namedList102 get n101]
    $namedList202 remove [$namedList202 get n201]

    set r1_99 [$namedList99 size]
    set r1_100 [$namedList100 size]
    set r1_101 [$namedList101 size]
    set r1_102 [$namedList102 size]
    set r1_202 [$namedList202 size]

    set r2_99 [java::isnull [$namedList99 get n98]]
    set r2_100 [java::isnull [$namedList100 get n99]]
    set r2_101 [java::isnull [$namedList101 get n100]]
    set r2_102 [java::isnull [$namedList102 get n101]]
    set r2_202 [java::isnull [$namedList202 get n201]]

    list [list $r1_99 $r1_100 $r1_101 $r1_102 $r1_202] \
	[list $r2_99 $r2_100 $r2_101 $r2_102 $r2_202]
} {{98 99 100 101 201} {1 1 1 1 1}}

test NamedList-100.6 {remove String} {
    # Reset
    set namedList99 [createNamedList 99]
    set namedList100 [createNamedList 100]
    set namedList101 [createNamedList 101]
    set namedList102 [createNamedList 102]
    set namedList202 [createNamedList 202]

    $namedList99 remove n98
    $namedList100 remove n99
    $namedList101 remove n100
    $namedList102 remove n101
    $namedList202 remove n201

    set r1_99 [$namedList99 size]
    set r1_100 [$namedList100 size]
    set r1_101 [$namedList101 size]
    set r1_102 [$namedList102 size]
    set r1_202 [$namedList202 size]

    set r2_99 [java::isnull [$namedList99 get n98]]
    set r2_100 [java::isnull [$namedList100 get n99]]
    set r2_101 [java::isnull [$namedList101 get n100]]
    set r2_102 [java::isnull [$namedList102 get n101]]
    set r2_202 [java::isnull [$namedList202 get n201]]

    list [list $r1_99 $r1_100 $r1_101 $r1_102 $r1_202] \
	[list $r2_99 $r2_100 $r2_101 $r2_102 $r2_202]
} {{98 99 100 101 201} {1 1 1 1 1}}

test NamedList-100.8 {change the name} {
    # Reset
    set namedList99 [createNamedList 99]
    set namedList100 [createNamedList 100]
    set namedList101 [createNamedList 101]
    set namedList102 [createNamedList 102]
    set namedList202 [createNamedList 202]

    # Thomas writes:
    # I think the newly updated NamedList has a bug that could break large
    # models. It automatically uses a HashMap to accelerate element lookup
    # by names when the number of elements contained in it exceeds 100. This
    # is due to the following change:
    #
    # 50642 9/15/08 6:23 PM 1 cxh Performance enhancements by Jason
    # E. Smith that use a HashMap if the size is over 100 elements.
    #
    # However, this does not take into account that elements' names could be
    # changed. So if a list contains more than 100 element, a HashMap is
    # created with the current elements' names as keys and the elements
    # themselves as values. But if I change an element's name, then the key
    # no longer matches its new name, and if I look up the element with
    # get(String), I wouldn't be able to find it with the new name.
    # 
    # This causes a model transformation model to fail, which generates 100+
    # entities, renames each entity after creation, and tries to use
    # getEntity(String) to get back those entities in the composite actor.
    
    set n98_99 [$namedList99 get n98]
    set n98_100 [$namedList100 get n98]
    set n98_101 [$namedList101 get n98]
    set n98_102 [$namedList102 get n98]
    set n98_202 [$namedList202 get n98]

    $n98_99 setName n98_new
    $n98_100 setName n98_new
    $n98_101 setName n98_new
    $n98_102 setName n98_new
    $n98_202 setName n98_new

    set r1_99 [[$namedList99 get n98_new] getName]
    set r1_100 [[$namedList100 get n98_new] getName]
    set r1_101 [[$namedList101 get n98_new] getName]
    set r1_102 [[$namedList102 get n98_new] getName]
    set r1_202 [[$namedList202 get n98_new] getName]

    # Make sure get shows that the name is really changed
    set r2_99 [java::isnull [$namedList99 get n98]]
    set r2_100 [java::isnull [$namedList100 get n98]]
    set r2_101 [java::isnull [$namedList101 get n98]]
    set r2_102 [java::isnull [$namedList102 get n98]]
    set r2_202 [java::isnull [$namedList202 get n98]]

    list [list $r1_99 $r1_100 $r1_101 $r1_102 $r1_202] \
	[list $r2_99 $r2_100 $r2_101 $r2_102 $r2_202]
} {{n98_new n98_new n98_new n98_new n98_new} {1 1 1 1 1}}

test NamedList-100.666 {removeAll} {
    $namedList99 removeAll
    $namedList100 removeAll
    $namedList101 removeAll
    $namedList102 removeAll
    $namedList202 removeAll

    set r1_99 [$namedList99 size]
    set r1_100 [$namedList100 size]
    set r1_101 [$namedList101 size]
    set r1_102 [$namedList102 size]
    set r1_202 [$namedList202 size]

    list [list $r1_99 $r1_100 $r1_101 $r1_102 $r1_202]
} {{0 0 0 0 0}}
