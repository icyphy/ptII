# Tests for the SchematicRelation class
#
# @Author: Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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
# we have to change _testEnums here, because the normal _testEnums only handles
# NamedObj's

proc _testEnums {enummethod args} {
    set results {}
    foreach objecttoenum $args {
        if {$objecttoenum == [java::null]} {
            lappend results [java::null]
        } else {
            set lresults {}
            for {set enum [$objecttoenum $enummethod]} \
                    {$enum != [java::null] && \
                    [$enum hasMoreElements] == 1} \
                    {} {
                set enumelement [$enum nextElement]
                if [ java::instanceof $enumelement ptolemy.schematic.util.PTMLObject] {
                         set enumelement \
                                 [java::cast ptolemy.schematic.util.PTMLObject \
                                 $enumelement]
                    lappend lresults [$enumelement getName]
                } else {
                    lappend lresults $enumElement
                }
            }
            lappend results $lresults
        }
    }
    return $results
}

######################################################################
####
#
test SchematicRelation-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.util.SchematicRelation]
    set e1 [java::new ptolemy.schematic.util.SchematicRelation "TestSchematicRelation"]
    list [$e0 description] [$e1 description]
} {{ptolemy.schematic.util.SchematicRelation {relation} parameters {
} terminals {
} links {
}} {ptolemy.schematic.util.SchematicRelation {TestSchematicRelation} parameters {
} terminals {
} links {
}}}

test SchematicRelation-2.2 {setDocumentation, isDocumentation tests} {
    # NOTE: Uses the setup above
    set r0 [$e0 getDocumentation]
    $e0 setDocumentation {Oh what a tangled web we weave,}
    set r1 [$e0 getDocumentation]
    $e0 setDocumentation {when we practice to deceive.}
    set r2 [$e0 getDocumentation]
    list $r0 $r1 $r2
} {{} {Oh what a tangled web we weave,} {when we practice to deceive.}}

######################################################################
####
#
#set l1 [java::new ptolemy.schematic.util.SchematicLink link]

######################################################################
####
#
test SchematicRelation-3.1 {addTerminal} {
    set tt1 [java::new ptolemy.schematic.util.SchematicTerminal ToTemplate]
    set tt2 [java::new ptolemy.schematic.util.SchematicTerminal FromTemplate]
    set t1 [java::new ptolemy.schematic.util.SchematicTerminal Terminal1 $tt1]
    set t2 [java::new ptolemy.schematic.util.SchematicTerminal Terminal2 $tt2]
    $e0 addTerminal $t1
    $e0 description
} {ptolemy.schematic.util.SchematicRelation {relation} parameters {
} terminals {
    {ptolemy.schematic.util.SchematicTerminal {relation.Terminal1} parameters {
    } template {
        ptolemy.schematic.util.SchematicTerminal {ToTemplate} parameters {
        } template {
            null
        } X {0.0} Y {0.0}
    } X {0.0} Y {0.0}}
} links {
}}

test SchematicRelation-3.2 {containsTerminal} {
    list [$e0 containsTerminal $t1] [$e0 containsTerminal $t2]
} {1 0}

test SchematicRelation-3.3 {terminals} {
    $e0 addTerminal $t2
    _testEnums terminals $e0
} {{Terminal1 Terminal2}}

test SchematicRelation-3.4 {removeTerminal} {
    $e0 removeTerminal $t1
    $e0 description
} {ptolemy.schematic.util.SchematicRelation {relation} parameters {
} terminals {
    {ptolemy.schematic.util.SchematicTerminal {relation.Terminal2} parameters {
    } template {
        ptolemy.schematic.util.SchematicTerminal {FromTemplate} parameters {
        } template {
            null
        } X {0.0} Y {0.0}
    } X {0.0} Y {0.0}}
} links {
}}

test SchematicRelation-3.5 {addLink} {
    set l1 [java::new ptolemy.schematic.util.SchematicLink $t1 $t2]
    set l2 [java::new ptolemy.schematic.util.SchematicLink $t2 $t1]
    $e0 addLink $l1
    $e0 description
} {ptolemy.schematic.util.SchematicRelation {relation} parameters {
} terminals {
    {ptolemy.schematic.util.SchematicTerminal {relation.Terminal2} parameters {
    } template {
        ptolemy.schematic.util.SchematicTerminal {FromTemplate} parameters {
        } template {
            null
        } X {0.0} Y {0.0}
    } X {0.0} Y {0.0}}
} links {
    {ptolemy.schematic.util.SchematicLink
     to {
        ptolemy.schematic.util.SchematicTerminal {Terminal1} parameters {
        } template {
            ptolemy.schematic.util.SchematicTerminal {ToTemplate} parameters {
            } template {
                null
            } X {0.0} Y {0.0}
        } X {0.0} Y {0.0}
    } from {
        ptolemy.schematic.util.SchematicTerminal {relation.Terminal2} parameters {
        } template {
            ptolemy.schematic.util.SchematicTerminal {FromTemplate} parameters {
            } template {
                null
            } X {0.0} Y {0.0}
        } X {0.0} Y {0.0}
    }}
}}

test SchematicRelation-3.6 {containsLink} {
    list [$e0 containsLink $l1] [$e0 containsLink $l2]
} {1 0}

# Links are not nameable
#test SchematicRelation-3.7 {Links} {
#    $e0 addLink $l2
#    _testEnums links $e0
#} {{Link1 Link2}}

test SchematicRelation-3.8 {removeLink} {
    $e0 removeLink $l1
    $e0 description
} {ptolemy.schematic.util.SchematicRelation {relation} parameters {
} terminals {
    {ptolemy.schematic.util.SchematicTerminal {relation.Terminal2} parameters {
    } template {
        ptolemy.schematic.util.SchematicTerminal {FromTemplate} parameters {
        } template {
            null
        } X {0.0} Y {0.0}
    } X {0.0} Y {0.0}}
} links {
}}

test SchematicRelation-3.9 {setWidth, getWidth tests} {
    # NOTE: Uses the setup above
    set r0 [$e0 getWidth]
    $e0 setWidth 4
    set r1 [$e0 getWidth]
    $e0 setWidth 2
    set r2 [$e0 getWidth]
    list $r0 $r1 $r2
} {1 4 2}

