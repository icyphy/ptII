# Tcl Procs that for use with the Java pt.kernel.Nameable description() method
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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


######################################################################
####
# Given a string description returned by the pt.kernel.Nameable description()
# method, return a Tcl Blend script that will regenerate the description
#
# An example use would be:
# set filename [tmpFileName ptII .dag]
# source ExampleSystem.tcl
# set desc [$e0 description [java::field pt.kernel.Nameable LIST_PRETTYPRINT]]]
# description2DAG "Ptolemy II Entities" $filename $desc
# 
proc description2DAG {title filename desc} {
    set fd [open $filename w]
    puts $fd "{configure -canvasheight 600} {configure -canvaswidth 800}"
    puts $fd "{titleSet title {$title}}"
    puts $fd "\{titleSet subtitle \{created:\
            [clock format [clock seconds]]\}\}"
    _description2DAGInternal $fd $desc
    close $fd
}

# Internal proc that traverses a description and generates the DAG
# This is called by description2DAG
proc _description2DAGInternal {fd contents {parent {}} {oldparent {}}
        {depth 1}} {
    #puts "dbg: top: $depth parent='$parent' oldparent='$oldparent' \
    #	    [llength $contents] --$contents--"

    foreach element $contents {
	#puts "dbg: $depth parent='$parent' oldparent='$oldparent'\
	#	[llength $element] [llength [lindex $element 0]] '$element'"

	# Checking that the list has two elements is not enough, since
	# we could have the case where we have a graph that consists
	# of a parent Entity with two child entities that are
	# connected to each other.  
	if {[llength $element] == 2 && [llength [lindex $element 0]] == 1} {

	    # Substitute / for . in the classname
	    regsub -all {\.} [lindex $element 0] {/} fileName

	    # Get the name of the Entity or Relation
	    set fullName [lindex $element 1]

	    #puts "element = '$element' fullName = '$fullName'"
	    if { $parent == {} } {
		set oldparent {}
		set parent $element
		puts $fd "\{add $fullName \{label \{$fullName\}\
			link \{\$TYCHO/java/$fileName.java\}\}\
			\{\}\}"
	    } else {
		if  {[lindex $parent 1] == $fullName} {
		    puts $fd "\{add $fullName \{label \{$fullName\}\
			link \{\$TYCHO/java/$fileName.java\}\}\
			\{[lindex $oldparent 1]\}\}"

		} else {
		    puts $fd "\{add $fullName \{label \{$fullName\}\
			    link \{\$TYCHO/java/$fileName.java\}\}\
			    \{[lindex $parent 1]\}\}"
		}
	    }
	}

	if {[llength $element] > 2  } {
	    _description2DAGInternal $fd $element [lindex $element 0]  \
		    $parent [expr {$depth + 1}]
	} else {
	    if {[llength [lindex $element 0]] > 1  } {
		_description2DAGInternal $fd $element [lindex $element 0]  \
		    $parent [expr {$depth + 1}]
	    }
	}
    }
}



######################################################################
####
# Given a string description returned by the pt.kernel.Nameable description()
# method, return a Tcl Blend script that will regenerate the description
#
# An example use would be:
# set desc [description2TclBlend [$e0 description \
#	[java::field pt.kernel.Nameable PRETTYPRINT]]]
# eval $desc
#
proc description2TclBlend {descriptionString} {
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

