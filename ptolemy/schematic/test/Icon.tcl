# Tests for the Icon class
#
# @Author: Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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
test Icon-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.Icon]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.Icon $attributes]
    set entitytype [java::new ptolemy.schematic.EntityType
    $entitytype setName testentitytype
    set e1 [java::new ptolemy.schematic.Icon $attributes $entitytype]
    list [$e0 toString] [$e1 toString] [$e2 toString]
} {{<element>
</element>
} {<element name1="value1" name2="value2">
</element>
} {<element name1="value1" name2="value2">
</element>
}}

######################################################################
####
#
test Icon-3.1 {addChildElement tests} {
    set e0 [java::new ptolemy.schematic.Icon]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.Icon $attributes]
    $e0 addChildElement $e1
    list [$e0 toString] [$e1 toString]
} {{<icon>
<icon name1="value1" name2="value2">
</icon>
</icon>
} {<icon name1="value1" name2="value2">
</icon>
}}


######################################################################
####
#
test Icon-3.2 {removeChildElement tests} {
    # NOTE: Uses the setup above
    $e0 removeChildElement $e1
    list [$e0 toString] [$e1 toString]
} {{<icon>
</icon>
} {<icon name1="value1" name2="value2">
</icon>
}}

######################################################################
####
#
test Icon-4.1 {childElements tests} {
    set e0 [java::new ptolemy.schematic.Icon]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.Icon $attributes]
    set e2 [java::new ptolemy.schematic.Icon]
    set e3 [java::new ptolemy.schematic.Icon]    
    $e0 addChildElement $e1
    $e0 addChildElement $e2
    $e2 addChildElement $e3
    set e0children [$e0 childElements]
    set e0child1 [$e0children nextElement] 
    set e0child2 [$e0children nextElement] 
    list [$e0child1 toString] [$e0child2 toString] \
[$e0children hasMoreElements]
} {{<icon name1="value1" name2="value2">
</icon>
} {<icon>
<icon>
</icon>
</icon>
} 0}


######################################################################
####
#
test Icon-4.2 {hasChildElement tests} {
    # NOTE: Uses the setup above
    list [$e0 hasChildElement $e0] [$e0 hasChildElement $e1] \
[$e0 hasChildElement $e2] [$e0 hasChildElement $e3]
} {0 1 1 0}

######################################################################
####
#
test Icon-4.3 {getParent tests} {
    # NOTE: Uses the setup above
    list [[$e0 getParent] equals java::null] [[$e1 getParent] equals $e0] \
[[$e2 getParent] equals $e0] [[$e3 getParent] equals $e2] 
} {1 1 1 1}

######################################################################
####
#
test Icon-5.1 {setAttribute tests} {
    set e0 [java::new ptolemy.schematic.Icon ]
    $e0 setAttribute name1 value1
    $e0 setAttribute name2 value2
    $e0 toString
} {<icon name1="value1" name2="value2">
</icon>
}


######################################################################
####
#
test Icon-6.2 {removeAttribute tests} {
    # NOTE: Uses the setup above
    $e0 removeAttribute name1
    $e0 to String
} {<icon name2="value2">
</icon>
}

######################################################################
####
#
test Icon-7.1 {attributes tests} {
    set e0 [java::new ptolemy.schematic.Icon ]
    $e0 setAttribute name1 value1
    $e0 setAttribute name2 value2
    $e0 toString
    set e0attributes [$e0 attributeNames]
    set e0attrib1 [$e0attributes nextElement] 
    set e0attrib2 [$e0attributes nextElement] 
    list $e0attrib1 $e0attrib2 \
[$e0attributes hasMoreElements]
} {name1 name2 0}

######################################################################
####
#
test Icon-7.2 {hasChildElement tests} {
    # NOTE: Uses the setup above
    list [$e0 hasAttribute name1] [$e0 hasAttribute name2] \
[$e0 hasAttribute name3]
} {1 1 0}

######################################################################
####
#
test Icon-7.3 {getParent tests} {
    # NOTE: Uses the setup above
    list [[$e0 getParent] equals java::null] [[$e1 getParent] equals $e0] \
[[$e2 getParent] equals $e0] [[$e3 getParent] equals $e2] 
} {1 1 0}

######################################################################
####
#
test Icon-8.1 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.Icon ]
    $e0 setPCData "hello this is a test\n"
    $e0 toString
} {<icon>
hello this is a test
</icon>
}

######################################################################
####
#
test Icon-8.2 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.Icon ]
    $e0 setPCData "hello this is a test"
    $e0 appendPCData " of appending\n"
    $e0 toString
} {<icon>
hello this is a test of appending
</icon>
}

######################################################################
####
#
test Icon-8.3 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.Icon ]
    $e0 setPCData "hello this is a test"
    $e0 appendPCData " of appending\n"
    $e0 setPCData "and resetting PCData\n"    
    $e0 toString
} {<icon>
and resetting PCData
</icon>
}

######################################################################
####
#
test Icon-9.1 {addGraphic tests} {
    set e0 [java::new ptolemy.schematic.Icon]
    set g0 [java::new ptolemy.schematic.XMLElement graphic]
    $g0 setPCData "A test Graphic string"
    $g0 setAttribute format test
    $e0 addGraphic $g0
    $e0 toString
} {}

######################################################################
####
#
test Icon-9.2 {containsGraphic tests} {
    # Uses setup from above
    $e0 containsGraphic test
    $e0 containsGraphic failtest
} {1 0}

######################################################################
####
#
test Icon-9.3 {getGraphic tests} {
    # Uses setup from above
    set g1 [$e0 getGraphic test]
    list [$e0 toString] [$g1 toString]
} {}

######################################################################
####
#
test Icon-9.4 {graphicFormats tests} {
    # Uses setup from above
    set genum [$e0 graphicFormats]
    set s [$genum nextElement]
    list [$s] [$genum hasMoreElements]
} {0}
   
######################################################################
####
#
test Icon-9.5 {removeGraphic tests} {
    # Uses setup from above
    set s [$e0 toString]
    $e0 removeGraphic test
    set genum [$e0 graphicFormats]
    list $s [$e0 toString] [$genum hasMoreElements]
} {}
  

