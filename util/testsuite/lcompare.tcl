# Utilities for doing a deep comparison of lists
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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

# This file contains procs for comparing unordered lists,
# where the order of elements at a particular level does not matter.
#
# Note that these procs are very slow because jacl has poor performance
# when using recursion.


# Return the depth of a list.  Note that tcl has some strange
# ideas about lists with single elements:
# list a returns a
# list [list a] returns a
# so, the depth of a list like {a {b}} is 1
proc ldepth {list {count 0}} {
    incr count
    set greatestDepth $count
    foreach element $list {
	set subelement $element
	if {$subelement != {} && $subelement != [lindex $subelement 0]} {
	    set temp [ldepth $subelement $count]
	    if {$greatestDepth < $temp} {
		set greatestDepth $temp
	    }
	}
    }
    return $greatestDepth
}

# Routine to test ldepth proc
proc test_ldepth {} {
    # Test case and expected results
    set cases {
	{ {} 1}
	{ {a} 1}
	{ {{a2}} 1}
	{ {{{a3}}} 2}
	{ {{{{a3}}}} 3}
	{ {a4} 1}
	{ {a b} 1}
	{ {a {b}} 1}
	{ {a {{b}}} 2}
	{ {a {{{b}}}} 3}
	{ {a b c} 1}
	{ {a {b} c} 1}
	{ {a {b c} d} 2}
	{ {a {b {c}} d} 2}
	{{a {b c} d {e {f g {h i}}}} 4}
    }
    foreach case $cases {
	if {[ldepth [lindex $case 0]] != [lindex $case 1]} {
	    puts "'ldepth [lindex $case 0]' is [ldepth [lindex $case 0]] != [lindex $case 1]"
	}
    }
}

# Uncomment these lines to run the test for ldepth
#test_ldepth
#exit

# Sort a list by sorting each sublist as well.
# Given a nested list like
#                    {a c b {{f {n m} p} {d e} g} z}
# return             {a b c z {{d e} {f {m n} p} g}}
# lsort would return {a b c z {{f {n m} p} {d e} g}}
proc deeplsort {list} {
    # This is _really_ slow under jacl because it uses recursion,
    # so we print dots to let the user know what is up
    puts -nonewline "."
    # If all the elements in a list are leaves [llength <= 1], then
    # call lsort on the list
    set allLeaves 1
    foreach element $list {
	if {[ldepth $element] > 1} {
	    set allLeaves 0
	    break
	}
    }
    if {$allLeaves == 1} {
	return [lsort $list]
    }

    # If any elements have a length greather than 1, then call deepsort on
    # each element that has a length greater than 1 and create a new list
    # and then call lsort on the new list
    foreach element $list {
	if {[ldepth $element] > 1} {
	    lappend newlist [deeplsort $element]
	} else {
	    lappend newlist $element
	}
    }
    return [lsort $newlist]
}



# Compare two deeply sorted lists
# If the lists are the same return 1
# If the lists are not the same, try returning a useful message
proc deeplcompare {lista listb {indent ""}} {
    if {[llength $lista] != [llength $listb]} {
	return "The two lists are not the same length '[llength $lista]' != '[llength $listb]'\n$lista\n$listb"} {
    }
    for {set i 0} {$i< [llength $lista]} {incr i} {
	if { [lindex $lista $i ] != [lindex $listb $i ]} {
	    set a [lindex $lista $i]
	    set b [lindex $listb $i]
	    append indent " "
	    if {[ldepth $a] > 1 ||  [ldepth $b] > 1} {
		return "$indent checking '$a' '$b' \n [deeplcompare $a $b $indent]"
	    } else {
		return "$indent '$a' is not equal to '$b'"
	    }
	}
    }
    return 1
}

# Compare two lists.  If == says that they are not equal, then
# deeply sort the lists and then deeply compare then
proc lcompare {lista listb} {
    if {$lista == $listb } {
	return 1
    }
    puts -nonewline "lcompare: Doing deeplcompare "
    set deepsortedlista [deeplsort $lista]
    set deepsortedlistb [deeplsort $listb]
    puts ""
    return [deeplcompare $deepsortedlista $deepsortedlistb]
}

#set a {a c b {{f {n m} p} {d e} g} z}
#set b {a c b {{f {n m} q} {d e} g} z}
#
#puts [lcompare $a $b]
