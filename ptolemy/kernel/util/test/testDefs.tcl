# Load test bed definitions
#
# @Author: Christopher Hylands
#
# @Version: @(#)testDefs.tcl	1.9 01/07/98
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

if [info exist env(PTOLEMY)] {
    set TYCHO $env(PTOLEMY)/tycho
}

if [info exist env(TYCHO)] {
    set TYCHO $env(TYCHO)
}

if {![info exist TYCHO]} {
    # If we are here, then we are probably running jacl and we can't
    # read environment variables
    set TYCHO [file join [pwd] .. .. .. ..]
}

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source [file join $TYCHO kernel test testDefs.tcl]
} {}

######################################################################
#### 
# Split a string into shorter lines separated by newlines.
#
proc _splitline {str} {
    set results ""
    for {set i 0} {$i < [string length $str] } {incr i 55} {
	append results "[string range $str $i [expr {$i + 54}]]\n    "
    }
    return $results
}

######################################################################
####
# Return a string that contains all of the information for an object
# that we can retrieve with java::info
#
proc getJavaInfo {obj} {
    return "\n \
    class:         [java::info class $obj]\n \
    fields:        [_splitline [lsort [java::info fields $obj]]]\n \
    methods:       [_splitline [lsort [java::info methods $obj]]]\n \
    constructors:  [_splitline [lsort [java::info constructors $obj]]]\n \
    properties:    [_splitline [lsort [java::info properties $obj]]]\n \
    superclass:    [_splitline [java::info superclass $obj]]\n"
}


######################################################################
####
# Given a string description returned by the pt.kernel.Nameable description()
# method, return a Tcl Blend script that will regenerate the description
#
# An example use would be:
# set desc [_description2TclBlend [$e0 description \
#	[java::field pt.kernel.Nameable PRETTYPRINT]]]
# eval $desc
#
proc _description2TclBlend {descriptionString} {
    set descList [split $descriptionString "\n"]
    set results {}
    set relationsSeen {}
    foreach line $descList {
	# Name of the class we are building.
	set className [lindex $line 0]

	# Use this list to get the name and the parentName
	set nameList [split [lindex $line 1] .]

	# FIXME: Here, we assume that each object has a unique
	# short name.  That is, if the fullname of an object
	# is e0.e1.e2, then there is no e0.e3.e2
	# To fix this we could use the fullname everywhere, but
	# that would be sort of ugly.

	# Name of the object
	set name [lindex $nameList [expr {[llength $nameList] - 1}]]

	# Name of the parent
	set parentName [lindex $nameList [expr {[llength $nameList] - 2}]]

	switch -regexp $line {
	    ^pt.kernel.CompositeEntity {
		if { "$parentName" == "" } {
		    # Handle the case where the entity has no parent
		    append results "set $name\
			    \[java::new $className\]\n\
			    \$$name setName $name\n"
		} else {   
		    append results "set $name\
			    \[java::new $className \$$parentName $name\]\n"
		}
	    }
	    ^pt.kernel.ComponentEntity {
		append results "set $name\
			\[java::new $className \$$parentName $name\]\n"
	    }
	    ^pt.kernel.ComponentRelation {
		# Relations appear more than once in the output
		# of description().  This is because the Relation
		# appears once for each port the Relation is connected to.
		#
		# If we have not yet seen this relation, then emit
		# an instruction to create it.
		if {[lsearch $relationsSeen $name] == -1} { 
		    lappend relationsSeen $name
		    append results "set $name\
			    \[java::new $className \$$parentName $name\]\n"
		}
	    }
	    ^pt.kernel.ComponentPort {
		if {[llength $line] == 2} {
		    # Construct a port
		    append results "set $name\
			    \[\$$parentName newPort $name\]\n"
		} else {
		    if {[lindex $line 2] == "link"} {
			# Connect a port to a relation
			set fullRelationList [split [lindex $line 4] .]
			set relation [lindex $fullRelationList \
				[expr {[llength $fullRelationList] - 1}]]

			# Check to see if the link is a level crossing link.
			# FIXME: Here, we are only checking the
			# lengths of fullnames, not the actual contents
			if { [expr {[llength $nameList] - \
				[llength $fullRelationList] }] > 1 } {
			    set commonParent [lindex $nameList \
				    [expr {[llength $nameList] - \
				    [llength $fullRelationList] }]]
			    append results "\$$commonParent\
				    allowLevelCrossingConnect true\n"
			    append results "\$$name liberalLink \$$relation\n"

			} else {
			    append results "\$$name link \$$relation\n"
			}
		    }
		}
	    }
	}
    }
    return $results
}


