# Tests for the SchematicEntity class
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
test SchematicEntity-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.SchematicEntity $attributes]
    set entitytype [java::new ptolemy.schematic.EntityType]
    $entitytype setName testentitytype
    set e2 [java::new ptolemy.schematic.SchematicEntity $attributes $entitytype]
    list [$e0 toString] [$e1 toString] [$e2 toString]
} {{<entity name="" icon="default"></entity>
} {<entity name1="value1" name2="value2" name="" icon="default"></entity>
} {<entity name1="value1" name2="value2" name="" icon="default"></entity>
}}

######################################################################
####
#
test SchematicEntity-3.1 {addChildElement tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.SchematicEntity $attributes]
    $e0 addChildElement $e1
    list [$e0 toString] [$e1 toString]
} {{<entity name="" icon="default">
<entity name1="value1" name2="value2" name="" icon="default"></entity>
</entity>
} {<entity name1="value1" name2="value2" name="" icon="default"></entity>
}}


######################################################################
####
#
test SchematicEntity-3.2 {removeChildElement tests} {
    # NOTE: Uses the setup above
    $e0 removeChildElement $e1
    list [$e0 toString] [$e1 toString]
} {{<entity name="" icon="default"></entity>
} {<entity name1="value1" name2="value2" name="" icon="default"></entity>
}}

######################################################################
####
#
test SchematicEntity-4.1 {childElements tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity]
    set attributes [java::new collections.HashedMap]
    $attributes putAt name1 value1
    $attributes putAt name2 value2
    set e1 [java::new ptolemy.schematic.SchematicEntity $attributes]
    set e2 [java::new ptolemy.schematic.SchematicEntity]
    set e3 [java::new ptolemy.schematic.SchematicEntity]    
    $e0 addChildElement $e1
    $e0 addChildElement $e2
    $e2 addChildElement $e3
    set e0children [$e0 childElements]
    set e0child1 [$e0children nextElement] 
    set c1left [$e0children hasMoreElements]
    set e0child2 [$e0children nextElement] 
    set c2left [$e0children hasMoreElements]
    list $c1left $c2left
} {1 0}


######################################################################
####
#
test SchematicEntity-4.2 {hasChildElement tests} {
    # NOTE: Uses the setup above
    list [$e0 hasChildElement $e0] [$e0 hasChildElement $e1] \
[$e0 hasChildElement $e2] [$e0 hasChildElement $e3]
} {0 1 1 0}

######################################################################
####
#
test SchematicEntity-4.3 {getParent tests} {
    # NOTE: Uses the setup above
    list [[$e1 getParent] equals $e0] \
[[$e2 getParent] equals $e0] [[$e3 getParent] equals $e2] 
} {1 1 1}

######################################################################
####
#
test SchematicEntity-5.1 {setAttribute tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity]
    $e0 setAttribute name1 value1
    $e0 setAttribute name2 value2
    $e0 toString
} {<entity name1="value1" name2="value2" name="" icon="default"></entity>
}


######################################################################
####
#
test SchematicEntity-6.2 {removeAttribute tests} {
    # NOTE: Uses the setup above
    $e0 removeAttribute name1
    $e0 toString
} {<entity name2="value2" name="" icon="default"></entity>
}

######################################################################
####
#
test SchematicEntity-7.1 {attributes tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity]
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
} {name1 name2 name icon 0}

######################################################################
####
#
test SchematicEntity-7.2 {hasChildElement tests} {
    # NOTE: Uses the setup above
    list [$e0 hasAttribute name1] [$e0 hasAttribute name2] \
[$e0 hasAttribute name3]
} {1 1 0}

######################################################################
####
#
test SchematicEntity-8.1 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity ]
    $e0 setPCData "hello this is a test\n"
    $e0 toString
} {<entity name="" icon="default">hello this is a test
</entity>
}

######################################################################
####
#
test SchematicEntity-8.2 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity ]
    $e0 setPCData "hello this is a test"
    $e0 appendPCData " of appending\n"
    $e0 toString
} {<entity name="" icon="default">hello this is a test of appending
</entity>
}

######################################################################
####
#
test SchematicEntity-8.3 {setPCData tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity ]
    $e0 setPCData "hello this is a test"
    $e0 appendPCData " of appending\n"
    $e0 setPCData "and resetting PCData\n"    
    $e0 toString
} {<entity name="" icon="default">and resetting PCData
</entity>
}

######################################################################
####
#
test SchematicEntity-9.1 {set/getName tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity]
    $e0 setName "SchematicEntity Name"
    list [$e0 toString] [$e0 getName]
} {{<entity name="SchematicEntity Name" icon="default"></entity>
} {SchematicEntity Name}}
  
######################################################################
####
#
test SchematicEntity-10.1 {Parameter tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity]
    set p0 [java::new ptolemy.schematic.SchematicParameter testparameter testtype testvalue]
    $e0 addParameter $p0
    set p1 [$e0 getParameter testparameter]
    list [$e0 toString] [$e0 containsParameter testparameter] [$p1 toString]
} {{<entity name="" icon="default">
<parameter value="testvalue" name="testparameter" type="testtype"></parameter>
</entity>
} 1 {<parameter value="testvalue" name="testparameter" type="testtype"></parameter>
}}

######################################################################
####
#
test SchematicEntity-10.2 {parameters tests} {
    # uses configuration above
    set enumlib [$e0 parameters]
    set onelib [$enumlib hasMoreElements]
    set param [$enumlib nextElement]
    set zerolib [$enumlib hasMoreElements]
    list $onelib $zerolib [$param getName] [$param getType]\
[$param getValue]
} {1 0 testparameter testtype testvalue}

######################################################################
####
#
test SchematicEntity-10.3 {remove Parameter tests} {
    # uses configuration above
    $e0 removeParameter testparameter
    set enumlib [$e0 parameters]
    list [$e0 toString] [$e0 containsParameter testparameter]\
[$enumlib hasMoreElements]
} {{<entity name="" icon="default"></entity>
} 0 0}

######################################################################
####
#
test SchematicEntity-12.1 {addPort tests} {
    set e0 [java::new ptolemy.schematic.SchematicEntity]
    set g0 [java::new ptolemy.schematic.SchematicPort]
    $g0 setName testport
    $e0 addPort $g0
    $e0 toString
} {<entity name="" icon="default">
<port multiport="false" name="testport" input="false" type="undeclared" output="false"></port>
</entity>
}

######################################################################
####
#
test SchematicEntity-12.2 {containsPort tests} {
    # Uses setup from above
    list [$e0 containsPort testport] [$e0 containsPort failtest]
} {1 0}

######################################################################
####
#
test SchematicEntity-12.3 {getPort tests} {
    # Uses setup from above
    set g1 [$e0 getPort testport]
    list [$e0 toString] [$g1 toString]
} {{<entity name="" icon="default">
<port multiport="false" name="testport" input="false" type="undeclared" output="false"></port>
</entity>
} {<port multiport="false" name="testport" input="false" type="undeclared" output="false"></port>
}}


######################################################################
####
#
test SchematicEntity-12.4 {ports tests} {
    # Uses setup from above
    set genum [$e0 ports]
    set s [$genum nextElement]
    list [$s toString] [$genum hasMoreElements]
} {{<port multiport="false" name="testport" input="false" type="undeclared" output="false"></port>
} 0}
   
######################################################################
####
#
test SchematicEntity-12.5 {removePort tests} {
    # Uses setup from above
    set s [$e0 toString]
    $e0 removePort testport
    set genum [$e0 ports]
    list $s [$e0 toString] [$genum hasMoreElements]
} {{<entity name="" icon="default">
<port multiport="false" name="testport" input="false" type="undeclared" output="false"></port>
</entity>
} {<entity name="" icon="default"></entity>
} 0}
