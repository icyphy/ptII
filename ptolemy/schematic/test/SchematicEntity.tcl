# Tests for the AtomicActor class
#
# @Author: Edward A. Lee
#
# @Version: @(#)AtomicActor.tcl	1.6   10/20/98
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
test XMLElement-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.XMLElement element]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.XMLElement element $attributes]
    list [$e0 toString] [$e1 toString]
} {{<element>
</element>
} {<element name1="value1" name2="value2">
</element>
}}

######################################################################
####
#
test XMLElement-3.1 {addChildElement tests} {
    set e0 [java::new ptolemy.schematic.XMLElement element0]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.XMLElement element1 $attributes]
    $e0 addChildElement $e1
    list [$e0 toString] [$e1 toString]
} {{<element0>
<element1 name1="value1" name2="value2">
</element1>
</element0>
} {<element1 name1="value1" name2="value2">
</element1>
}}


######################################################################
####
#
test XMLElement-3.2 {removeChildElement tests} {
    # NOTE: Uses the setup above
    $e0 removeChildElement $e1
    list [$e0 toString] [$e1 toString]
} {{<element0>
</element0>
} {<element1 name1="value1" name2="value2">
</element1>
}}

######################################################################
####
#
test XMLElement-4.1 {childElements tests} {
    set e0 [java::new ptolemy.schematic.XMLElement element0]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.XMLElement element1 $attributes]
    set e2 [java::new ptolemy.schematic.XMLElement element2]
    set e3 [java::new ptolemy.schematic.XMLElement element3]    
    $e0 addChildElement $e1
    $e0 addChildElement $e2
    $e2 addChildElement $e3
    set e0children [$e0 childElements]
    set e0child1 [$e0children nextElement] 
    set e0child2 [$e0children nextElement] 
    list [$e0child1 toString] [$e0child2 toString] \
[$e0children hasMoreElements]
} {{<element1 name1="value1" name2="value2">
</element1>
} {<element2>
<element3>
</element3>
</element2>
} 0}


######################################################################
####
#
test XMLElement-4.2 {hasChildElement tests} {
    # NOTE: Uses the setup above
    list [$e0 hasChildElement $e0] [$e0 hasChildElement $e1] \
[$e0 hasChildElement $e2] [$e0 hasChildElement $e3]
} {0 1 1 0}

######################################################################
####
#
test XMLElement-4.3 {getParent tests} {
    # NOTE: Uses the setup above
    list [[$e0 getParent] equals java::null] [[$e1 getParent] equals $e0] \
[[$e2 getParent] equals $e0] [[$e3 getParent] equals $e2] 
} {1 1 1 1}

######################################################################
####
#
test XMLElement-5.1 {setAttribute tests} {
    set e0 [java::new ptolemy.schematic.XMLElement element0]
    $e0 setAttribute name1 value1
    $e0 setAttribute name2 value2
    $e0 toString
} {<element0 name1="value1" name2="value2">
</element0>
}


######################################################################
####
#
test XMLElement-6.2 {removeAttribute tests} {
    # NOTE: Uses the setup above
    $e0 removeAttribute name1
    $e0 toString
} {<element0 name2="value2">
</element0>
}

######################################################################
####
#
test XMLElement-7.1 {attributes tests} {
    set e0 [java::new ptolemy.schematic.XMLElement element0]
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
test XMLElement-7.2 {hasChildElement tests} {
    # NOTE: Uses the setup above
    list [$e0 hasAttribute name1] [$e0 hasAttribute name2] \
[$e0 hasAttribute name3]
} {1 1 0}

######################################################################
####
#
test XMLElement-7.3 {getParent tests} {
    # NOTE: Uses the setup above
    list [[$e0 getParent] equals java::null] [[$e1 getParent] equals $e0] \
[[$e2 getParent] equals $e0] [[$e3 getParent] equals $e2] 
} {1 1 0}

######################################################################
####
#
test XMLElement-8.1 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.XMLElement element0]
    $e0 setPCData "hello this is a test\n"
    $e0 toString
} {<element0>
hello this is a test
</element0>
}

######################################################################
####
#
test XMLElement-8.2 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.XMLElement element0]
    $e0 setPCData "hello this is a test"
    $e0 appendPCData " of appending\n"
    $e0 toString
} {<element0>
hello this is a test of appending
</element0>
}

######################################################################
####
#
test XMLElement-8.3 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.XMLElement element0]
    $e0 setPCData "hello this is a test"
    $e0 appendPCData " of appending\n"
    $e0 setPCData "and resetting PCData\n"    
    $e0 toString
} {<element0>
and resetting PCData
</element0>
}

 
