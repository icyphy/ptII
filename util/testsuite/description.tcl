# Tcl Procs that for use with the Java ptolemy.kernel.Nameable description() method
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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


# The methods in this file should be written in Tcl, not Itcl
# so that this file can be used with both Itkwish and Tcl8.0.
#
# Any methods that call Tcl Blend commands should check to
# see if the java package is loaded first with:
# if {[lsearch [package names] java] != -1 } { ...}

######################################################################
####
# Given a string description returned by the ptolemy.kernel.Nameable description()
# method, return a Tcl Blend script that will regenerate the description
#
# An example use would be:
# set filename [tmpFileName ptII .dag]
# source ExampleSystem.tcl
# set desc [$e0 description [java::field ptolemy.kernel.Nameable LIST_PRETTYPRINT]]]
# description2DAG "Ptolemy II Entities" $filename $desc
# 
proc description2DAG {title filename desc} {
    set fd [open $filename w]
    puts $fd "{configure -canvasheight 600} {configure -canvaswidth 800}"
    puts $fd "{titleSet title {$title}}"
    puts $fd "\{titleSet subtitle \{created:\
            [clock format [clock seconds]]\}\}"

    # We have list of colors and an array that maps the colors to
    # classes.  We can't just use an array, since [array names] returns
    # the arrays unordered.
    global _descriptionColorList  _descriptionColorArray
    set _descriptionColorList \
	    [list red darkGreen blue yellow darkViolet darkPink ]
    foreach color $_descriptionColorList {
	set _descriptionColorArray($color) {}
    }
    _description2DAGInternal $fd $desc
    close $fd
}

# Internal proc that traverses a description and generates the DAG
# This is called by description2DAG
proc _description2DAGInternal {fd contents {parent {}} {oldparent {}}
        {depth 1}} {
    global _descriptionColorList  _descriptionColorArray
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
	    set className [lindex $element 0]

	    # Pick a color, default to black if we have more classes
	    # than colors.  Ideally, we would try to get a notion of
	    # the type of object and use the right color
	    set colorName ""
	    foreach colorElement $_descriptionColorList {
		if {$_descriptionColorArray($colorElement) == "$className"} {
		    set colorName $colorElement
		    break
		}
		if {$_descriptionColorArray($colorElement) == {}} {
		    set _descriptionColorArray($colorElement) $className
		    set colorName $colorElement
		    break
		}
	    }
	    if {$colorName == ""} {
		# default to black, we have more classes than colors
		set colorName black
	    }


	    # Substitute / for . in the classname
	    regsub -all {\.} $className {/} fileName

	    # Get the name of the Entity or Relation
	    set fullName [lindex $element 1]

	    #puts "element = '$element' fullName = '$fullName'"
	    if { $parent == {} } {
		set oldparent {}
		set parent $element
		puts $fd "\{add $fullName \{label \{$fullName\}\
			color \{$colorName\}\
			tcl \{fullName2HTML $className $fullName \}\
			altlink \{\$TYCHO/java/$fileName.java\}\}\
			\{\}\}"
	    } else {
		if  {[lindex $parent 1] == $fullName} {
		    puts $fd "\{add $fullName \{label \{$fullName\}\
			color \{$colorName\}\
			tcl \{ fullName2HTML $className $fullName \}\
			altlink \{\$TYCHO/java/$fileName.java\}\}\
			\{[lindex $oldparent 1]\}\}"

		} else {
		    puts $fd "\{add $fullName \{label \{$fullName\}\
			    color \{$colorName\}\
			    tcl \{ fullName2HTML $className $fullName \}\
			    altlink \{\$TYCHO/java/$fileName.java\}\}\
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
# Given a string description returned by the ptolemy.kernel.Nameable description()
# method, return a Tcl Blend script that will regenerate the description
#
# An example use would be:
# set desc [description2TclBlend [$e0 description \
#	[java::field ptolemy.kernel.Nameable PRETTYPRINT]]]
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
	    ^ptolemy.kernel.CompositeEntity {
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
	    ^ptolemy.kernel.ComponentEntity {
		append results "set $name\
			\[java::new $className \$$parentName $name\]\n"
	    }
	    ^ptolemy.kernel.ComponentRelation {
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
	    ^ptolemy.kernel.ComponentPort {
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

######################################################################
#### getCurrentUniverse
# Get the java handle for the Current Ptolemy II Universe.
# This method is used to inspect Ptolemy II objects in a DAG.
#
proc getCurrentUniverse {} {
    global _currentPtolemyIIUniverse
    if ![info exists _currentPtolemyIIUniverse] {
	return {}
    } else {
	return $_currentPtolemyIIUniverse
    }
}


######################################################################
#### getEntityByName
# Search the current universe for an entity with a particular name 
# setCurrentUniverse $e0
# set e [getEntityByName .E0.E10.E9]
# $e description 3
#
proc getEntityByName {name} {
    if {[lsearch [package names] java] == -1 } {
	error "Can't call getEntityByName without doing 'package require\
		java' first."
    }
    set universe [getCurrentUniverse]
    if {$universe == {}} {
	error "getCurrentUniverse returned {}, so there is no where to look"
    }

    # Do a little error checking.
    set description [$universe description \
	    [java::field ptolemy.kernel.Nameable CONTENTS]]
    set index [lsearch $description $name]
    if {$index == -1} {
	# Not found
	return {}
    }

    #set type [lindex $description [incr index -1]]

    set entity $universe
    set previousEntity $entity
    # FIXME: we are depending on the name being separated by .
    set splitList [split $name .]
    foreach splitElement [lrange $splitList 2 end] {
	if {$splitElement != {} } {
	    set previousEntity $entity
	    set entity [ $entity getEntity $splitElement]
	    puts "splitElement: $splitElement, $entity"
	    if {$entity == [java::null]} {
		break
	    }
	}
    }
    if {$entity == [java::null]} {
	set entity $previousEntity
    }
    return $entity
}

######################################################################
#### fullName2HTML
# Given a fullName of a Entity or Relation, display an HTML
# description.
#
proc fullName2HTML {className fullName} {
    global TYCHO jrpc
    startJRPCIfNecessary

    regsub -all {\.} $className {/} fileName
    set fullFileName $TYCHO/java/$fileName.java
    set fullDocFileName \
	    [file dirname $fullFileName]/doc/codeDoc/$className.html

    set entity [$jrpc send "getEntityByName $fullName"] 
    set description [split [$jrpc send "$entity description 2"] "\n"]
    foreach linkLine $description {
	set elements [split $linkLine]
	if {[llength $elements] > 0} {
	    append formattedDescription \
		    "<li>[lindex $elements 0],\ 
		    named `[lindex $elements 1]'\n\
		    is linked to a [lindex $elements 3],\
		    named `[lindex $elements 4]'\n"
	}
    }

    set m [::tycho::autoName .fullName2HTML]
    ::tycho::HTMLMessage $m
    $m insertData "<h1>Description of <code>$fullName</code></h1> \
	    `$fullName' is a `$className'
	    <menu>\
	    <li>Source code:\
	    <a href=\"$fullFileName\"><code>$fullFileName</code></a>\
	    <li>JavaDoc:\
	    <a href=\"$fullDocFileName\"><code>$fullDocFileName</code></a>\
	    <li>Ports:
	    <menu>$formattedDescription</menu>\
	    "
    $m centerOnScreen
}

######################################################################
#### setCurrentUniverse
# Set the java handle for the Current Universe
#
proc setCurrentUniverse { universe} {
    if {[lsearch [package names] java] != -1 } {
	if ![java::instanceof $universe ptolemy.kernel.CompositeEntity] {
	    error "$universe is not a CompositeEntity.\nIt is a:\n\
		    [java::info class $universe]"
	}
    }
    global _currentPtolemyIIUniverse
    set _currentPtolemyIIUniverse $universe
}
