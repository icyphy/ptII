# Tests for the IconLibrary class
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
test IconLibrary-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.IconLibrary $attributes]
    list [$e0 toString] [$e1 toString]
} {{<iconlibrary version="" name="">
<description>
</description>
</iconlibrary>
} {<iconlibrary name1="value1" name2="value2" version="" name="">
<description>
</description>
</iconlibrary>
}}

######################################################################
####
#
test IconLibrary-3.1 {addChildElement tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.XMLElement element1 $attributes]
    $e0 addChildElement $e1
    list [$e0 toString] [$e1 toString]
} {{<iconlibrary version="" name="">
<description>
</description>
<element1 name1="value1" name2="value2">
</element1>
</iconlibrary>
} {<element1 name1="value1" name2="value2">
</element1>
}}


######################################################################
####
#
test IconLibrary-3.2 {removeChildElement tests} {
    # NOTE: Uses the setup above
    $e0 removeChildElement $e1
    list [$e0 toString] [$e1 toString]
} {{<iconlibrary version="" name="">
<description>
</description>
</iconlibrary>
} {<element1 name1="value1" name2="value2">
</element1>
}}

######################################################################
####
#
test IconLibrary-4.1 {childElements tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.IconLibrary $attributes]
    set e2 [java::new ptolemy.schematic.IconLibrary]
    set e3 [java::new ptolemy.schematic.IconLibrary]    
    $e0 addChildElement $e1
    $e0 addChildElement $e2
    $e2 addChildElement $e3
    set e0children [$e0 childElements]
    set e0child1 [$e0children nextElement] 
    set c1left [$e0children hasMoreElements]
    set e0child2 [$e0children nextElement] 
    set c2left [$e0children hasMoreElements]
    set e0child3 [$e0children nextElement] 
    set c3left [$e0children hasMoreElements]
    list $c1left $c2left $c3left
} {1 1 0}

######################################################################
####
#
test IconLibrary-4.2 {hasChildElement tests} {
    # NOTE: Uses the setup above
    list [$e0 hasChildElement $e0] [$e0 hasChildElement $e1] \
[$e0 hasChildElement $e2] [$e0 hasChildElement $e3]
} {0 1 1 0}

######################################################################
####
#
test IconLibrary-4.3 {getParent tests} {
    # NOTE: Uses the setup above
    list [[$e1 getParent] equals $e0] \
[[$e2 getParent] equals $e0] [[$e3 getParent] equals $e2] 
} {1 1 1}

######################################################################
####
#
test IconLibrary-5.1 {setAttribute tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    $e0 setAttribute name1 value1
    $e0 setAttribute name2 value2
    $e0 toString
} {<iconlibrary name1="value1" name2="value2" version="" name="">
<description>
</description>
</iconlibrary>
}


######################################################################
####
#
test IconLibrary-6.2 {removeAttribute tests} {
    # NOTE: Uses the setup above
    $e0 removeAttribute name1
    $e0 toString
} {<iconlibrary name2="value2" version="" name="">
<description>
</description>
</iconlibrary>
}

######################################################################
####
#
test IconLibrary-7.1 {attributes tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    $e0 setAttribute name1 value1
    $e0 setAttribute name2 value2
    $e0 toString
    set e0attributes [$e0 attributeNames]
    set e0attrib1 [$e0attributes nextElement] 
    set e0attrib2 [$e0attributes nextElement] 
    set e0attrib3 [$e0attributes nextElement] 
    set e0attrib4 [$e0attributes nextElement] 
    list $e0attrib1 $e0attrib2 $e0attrib3 $e0attrib4\
[$e0attributes hasMoreElements]
} {name1 name2 version name 0}

######################################################################
####
#
test IconLibrary-7.2 {hasAttribute tests} {
    # NOTE: Uses the setup above
    list [$e0 hasAttribute name1] [$e0 hasAttribute name2] \
[$e0 hasAttribute name3]
} {1 1 0}

######################################################################
####
#
test IconLibrary-8.1 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    $e0 setPCData "hello this is a test\n"
    $e0 toString
} {<iconlibrary version="" name="">
<description>
</description>
hello this is a test
</iconlibrary>
}

######################################################################
####
#
test IconLibrary-8.2 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    $e0 setPCData "hello this is a test"
    $e0 appendPCData " of appending\n"
    $e0 toString
} {<iconlibrary version="" name="">
<description>
</description>
hello this is a test of appending
</iconlibrary>
}

######################################################################
####
#
test IconLibrary-8.3 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    $e0 setPCData "hello this is a test"
    $e0 appendPCData " of appending\n"
    $e0 setPCData "and resetting PCData\n"    
    $e0 toString
} {<iconlibrary version="" name="">
<description>
</description>
and resetting PCData
</iconlibrary>
}
 
######################################################################
####
#
test IconLibrary-9.1 {set/getDescription tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    $e0 setDescription "Test Description\n"
    list [$e0 toString] [$e0 getDescription]
} {{<iconlibrary version="" name="">
<description>
Test Description
</description>
</iconlibrary>
} {Test Description
}}

######################################################################
####
#
test IconLibrary-9.2 {set/getName tests} {
    # uses configuration above
    $e0 setName "IconLibrary Name"
    list [$e0 toString] [$e0 getName]
} {{<iconlibrary version="" name="IconLibrary Name">
<description>
Test Description
</description>
</iconlibrary>
} {IconLibrary Name}}

######################################################################
####
#
test IconLibrary-9.3 {set/getVersion tests} {
    # uses configuration above
    $e0 setVersion "IconLibrary Version"
    list [$e0 toString] [$e0 getVersion]
} {{<iconlibrary version="IconLibrary Version" name="IconLibrary Name">
<description>
Test Description
</description>
</iconlibrary>
} {IconLibrary Version}}

######################################################################
####
#
test IconLibrary-10.1 {sublibrary tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    $e0 addSubLibrary testlibrary
    list [$e0 toString] [$e0 containsSubLibrary testlibrary]
} {{<iconlibrary version="" name="">
<description>
</description>
<sublibrary>
testlibrary</sublibrary>
</iconlibrary>
} 1}

######################################################################
####
#
test IconLibrary-10.2 {remove sublibrary tests} {
    # uses configuration above
    set enumlib [$e0 subLibraries]
    set onelib [$enumlib hasMoreElements]
    $enumlib nextElement
    set zerolib [$enumlib hasMoreElements]
    list $onelib $zerolib
} {1 0}

######################################################################
####
#
test IconLibrary-10.3 {remove sublibrary tests} {
    # uses configuration above
    $e0 removeSubLibrary testlibrary
    set enumlib [$e0 subLibraries]
    list [$e0 toString] [$e0 containsSubLibrary testLibrary]\
[$enumlib hasMoreElements]
} {{<iconlibrary version="" name="">
<description>
</description>
</iconlibrary>
} 0 0}

######################################################################
####
#
test IconLibrary-11.1 {icon tests} {
    set e0 [java::new ptolemy.schematic.IconLibrary]
    set i0 [java::new ptolemy.schematic.Icon]
    $i0 setName testicon
    $e0 addIcon $i0
    list [$e0 toString] [$e0 containsIcon testicon]
} {{<iconlibrary version="" name="">
<description>
</description>
<icon name="testicon">
<entitytype>
</entitytype>
</icon>
</iconlibrary>
} 1}

######################################################################
####
#
test IconLibrary-11.2 {remove icon tests} {
    # uses configuration above
    set enumlib [$e0 icons]
    set onelib [$enumlib hasMoreElements]
    $enumlib nextElement
    set zerolib [$enumlib hasMoreElements]
    list $onelib $zerolib
} {1 0}

######################################################################
####
#
test IconLibrary-11.3 {remove icon tests} {
    # uses configuration above
    $e0 removeIcon testicon
    set enumlib [$e0 icons]
    list [$e0 toString] [$e0 containsIcon testicon]\
[$enumlib hasMoreElements]
} {{<iconlibrary version="" name="">
<description>
</description>
</iconlibrary>
} 1 1}
