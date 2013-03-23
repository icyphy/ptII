# Tests for the AttributeValueAttribute class
#
# @Author: Based on Attribute.tcl Edward A. Lee, Jie Liu, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2013 The Regents of the University of California.
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

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
#

######################################################################
####
#
test AttributeValueAttribute-2.2 {Create a AttributeValueAttribute with a container} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    $n setName N
    set p [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n P]
    $p getFullName
} {.N.P}

######################################################################
####
#
test AttributeValueAttribute-3.1 {Test for NameDuplicationException on constructor} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    $n setName N
    set p1 [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n P]
    catch {[java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n P]} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "P" into a container that already contains an object with that name.}}
######################################################################
####
#
test AttributeValueAttribute-3.2 {Test for NameDuplicationException on setName} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    $n setName N
    set p1 [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n P1]
    set p2 [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n P2]
    catch {$p2 setName P1} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Name duplication: P1
  in .N}}

######################################################################
####
#
test AttributeValueAttribute-3.3 {set an AttributeValueAttribute to its own name} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    $n setName N
    set p1 [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n P1]
    $p1 setName P1
    $p1 getFullName
} {.N.P1}

test AttributeValueAttribute-3.4 {setName null} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    $n setName N
    set c [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n P2]
    $c setName [java::null]
    $c getFullName	
} {.N.}

######################################################################
####
#
test AttributeValueAttribute-6.2 {Test description} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set n [java::new ptolemy.kernel.util.NamedObj N]

    set c [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n C]
    # Test with DEEP bit not set
    set detail [expr "[java::field ptolemy.kernel.util.NamedObj COMPLETE] & \
            ~[java::field ptolemy.kernel.util.NamedObj DEEP]"]
    list    [$c description $detail] \
	    [$n description $detail]
} {{ptolemy.vergil.kernel.attributes.AttributeValueAttribute {.N.C} attributes {
    {ptolemy.data.expr.SingletonParameter {.N.C._hideName} true}
    {ptolemy.data.expr.Parameter {.N.C._hideAllParameters} true}
    {ptolemy.vergil.icon.TextIcon {.N.C._icon}}
    {ptolemy.data.expr.Parameter {.N.C.textSize} 14}
    {ptolemy.actor.gui.ColorAttribute {.N.C.textColor} {0.0, 0.0, 1.0, 1.0}}
    {ptolemy.data.expr.StringParameter {.N.C.fontFamily} "SansSerif"}
    {ptolemy.data.expr.Parameter {.N.C.bold} false}
    {ptolemy.data.expr.Parameter {.N.C.italic} false}
    {ptolemy.data.expr.StringParameter {.N.C.anchor} "northwest"}
    {ptolemy.kernel.util.StringAttribute {.N.C.attributeName}}
    {ptolemy.data.expr.Parameter {.N.C.displayWidth} 6}
    {ptolemy.data.expr.Parameter {.N.C.useExpression} false}
}} {ptolemy.kernel.util.NamedObj {.N} attributes {
    {ptolemy.vergil.kernel.attributes.AttributeValueAttribute {.N.C}}
}}}

######################################################################
####
#
test AttributeValueAttribute-7.1 {Test clone into a new workspace} {
    # NOTE: Builds on previous test.
    set x [java::new ptolemy.kernel.util.Workspace X]
    set cx [java::cast ptolemy.vergil.kernel.attributes.AttributeValueAttribute [$c clone $x]]
    set cw [java::cast ptolemy.vergil.kernel.attributes.AttributeValueAttribute [$c clone]]
    list    [$cx getFullName] \
            [$cw getFullName]
} {.C .C}

test AttributeValueAttribute-7.2 {Test cloning of NamedObj with attributes} {
    # NOTE: Builds on previous test.
    set nx [java::cast ptolemy.kernel.util.NamedObj [$n clone $x]]
    set nw [java::cast ptolemy.kernel.util.NamedObj [$n clone]]
    list [$nx description $detail] [$nw description $detail]
} {{ptolemy.kernel.util.NamedObj {.N} attributes {
    {ptolemy.vergil.kernel.attributes.AttributeValueAttribute {.N.C}}
}} {ptolemy.kernel.util.NamedObj {.N} attributes {
    {ptolemy.vergil.kernel.attributes.AttributeValueAttribute {.N.C}}
}}}


######################################################################
####
#
test AttributeValueAttribute-7.5 {move* methods with no container} {
    set container [java::new ptolemy.kernel.util.NamedObj Container]
    set n [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $container AVA]
    catch {$n moveDown} msg1
    catch {$n moveToFirst} msg2
    catch {$n moveToIndex} msg3
    catch {$n moveToLast} msg4
    catch {$n moveUp} msg5
    list $msg1 $msg2 $msg3 $msg4 $msg5
} {-1 -1 {can't find method "moveToIndex" with 0 argument(s) for class "ptolemy.vergil.kernel.attributes.AttributeValueAttribute"} -1 -1}


test AttributeValueAttribute-7.5.1 {moveDown} {
    set top [java::new ptolemy.kernel.util.NamedObj]
    set a1  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a1]
    set a2  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a2]
    set a3  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a3]
    set result1 [listToNames [$top attributeList]]
    $a1 moveDown
    set result2 [listToNames [$top attributeList]]
    $a1 moveDown
    # Can't go past the bottom
    $a1 moveDown
    set result3 [listToNames [$top attributeList]]
    list $result1 $result2 $result3
} {{a1 a2 a3} {a2 a1 a3} {a2 a3 a1}}

test AttributeValueAttribute-7.5.2 {moveToFirst} {
    set top [java::new ptolemy.kernel.util.NamedObj]
    set a1  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a1]
    set a2  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a2]
    set a3  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a3]
    set result1 [listToNames [$top attributeList]]
    $a1 moveToFirst
    set result2 [listToNames [$top attributeList]]
    $a2 moveToFirst
    set result3 [listToNames [$top attributeList]]
    $a3 moveToFirst
    set result4 [listToNames [$top attributeList]]
    $a3 moveToFirst
    set result5 [listToNames [$top attributeList]]	
    list $result1 $result2 $result3 $result4 $result5
} {{a1 a2 a3} {a1 a2 a3} {a2 a1 a3} {a3 a2 a1} {a3 a2 a1}}

test AttributeValueAttribute-7.5.3 {moveToIndex} {
    set top [java::new ptolemy.kernel.util.NamedObj]
    set a1  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a1]
    set a2  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a2]
    set a3  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a3]
    set result1 [listToNames [$top attributeList]]
    catch {$a1 moveToIndex -1} msg
    set result2 $msg
    $a2 moveToIndex 0
    set result3 [listToNames [$top attributeList]]
    $a3 moveToIndex 1
    set result4 [listToNames [$top attributeList]]
    $a3 moveToIndex 2
    set result5 [listToNames [$top attributeList]]	
    catch {$a3 moveToIndex 3} result6
    list $result1 $result2 $result3 $result4 $result5 $result6
} {{a1 a2 a3} {ptolemy.kernel.util.IllegalActionException: Index out of range.
  in .<Unnamed Object>.a1} {a2 a1 a3} {a2 a3 a1} {a2 a1 a3} {ptolemy.kernel.util.IllegalActionException: Index out of range.
  in .<Unnamed Object>.a3}}

test AttributeValueAttribute-7.5.4 {moveToLast} {
    set top [java::new ptolemy.kernel.util.NamedObj]
    set a1  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a1]
    set a2  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a2]
    set a3  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a3]
    set result1 [listToNames [$top attributeList]]
    $a1 moveToLast
    set result2 [listToNames [$top attributeList]]
    $a2 moveToLast
    set result3 [listToNames [$top attributeList]]
    $a3 moveToLast
    set result4 [listToNames [$top attributeList]]
    $a3 moveToLast
    set result5 [listToNames [$top attributeList]]	
    list $result1 $result2 $result3 $result4 $result5
} {{a1 a2 a3} {a2 a3 a1} {a3 a1 a2} {a1 a2 a3} {a1 a2 a3}}

test AttributeValueAttribute-7.5.5 {moveUp} {
    set top [java::new ptolemy.kernel.util.NamedObj]
    set a1  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a1]
    set a2  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a2]
    set a3  [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $top a3]
    set result1 [listToNames [$top attributeList]]
    $a3 moveUp
    set result2 [listToNames [$top attributeList]]
    $a1 moveUp
    # Can't go past the top
    $a1 moveUp
    set result3 [listToNames [$top attributeList]]
    list $result1 $result2 $result3
} {{a1 a2 a3} {a1 a3 a2} {a1 a3 a2}}

######################################################################
####
#
test AttributeValueAttribute-8.1 {setContainer} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj N]
    set w2 [java::new ptolemy.kernel.util.Workspace W2]
    set n2 [java::new ptolemy.kernel.util.NamedObj $w2 N2]
    set a [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n A]
    set b [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n2 B]
    set c [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n C]
    set d [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n D]
    $a setContainer $c
    $a description
} {ptolemy.vergil.kernel.attributes.AttributeValueAttribute {.N.C.A} attributes {
    {ptolemy.data.expr.SingletonParameter {.N.C.A._hideName} true}
    {ptolemy.data.expr.Parameter {.N.C.A._hideAllParameters} true}
    {ptolemy.vergil.icon.TextIcon {.N.C.A._icon} attributes {
        {ptolemy.vergil.icon.EditIconTableau$Factory {.N.C.A._icon._tableauFactory} attributes {
        }}
    }}
    {ptolemy.data.expr.Parameter {.N.C.A.textSize} 14}
    {ptolemy.actor.gui.ColorAttribute {.N.C.A.textColor} {0.0, 0.0, 1.0, 1.0}}
    {ptolemy.data.expr.StringParameter {.N.C.A.fontFamily} "SansSerif"}
    {ptolemy.data.expr.Parameter {.N.C.A.bold} false}
    {ptolemy.data.expr.Parameter {.N.C.A.italic} false}
    {ptolemy.data.expr.StringParameter {.N.C.A.anchor} "northwest"}
    {ptolemy.kernel.util.StringAttribute {.N.C.A.attributeName} attributes {
    }}
    {ptolemy.data.expr.Parameter {.N.C.A.displayWidth} 6}
    {ptolemy.data.expr.Parameter {.N.C.A.useExpression} false}
}}

test AttributeValueAttribute-8.2 {setContainer, different workspace} {
    # Builds on 8.1 above
    catch {$b setContainer $c} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot set container because workspaces are different.
  in .N2.B and .N.C}}

test AttributeValueAttribute-8.3 {setContainer, then setContainer again} {
    # Builds on 8.1 above
    # Note that this calls NamedObj _removeAttributeValueAttribute()
    $a setContainer $c
    $a setContainer $d
    $a description
} {ptolemy.vergil.kernel.attributes.AttributeValueAttribute {.N.D.A} attributes {
    {ptolemy.data.expr.SingletonParameter {.N.D.A._hideName} true}
    {ptolemy.data.expr.Parameter {.N.D.A._hideAllParameters} true}
    {ptolemy.vergil.icon.TextIcon {.N.D.A._icon} attributes {
        {ptolemy.vergil.icon.EditIconTableau$Factory {.N.D.A._icon._tableauFactory} attributes {
        }}
    }}
    {ptolemy.data.expr.Parameter {.N.D.A.textSize} 14}
    {ptolemy.actor.gui.ColorAttribute {.N.D.A.textColor} {0.0, 0.0, 1.0, 1.0}}
    {ptolemy.data.expr.StringParameter {.N.D.A.fontFamily} "SansSerif"}
    {ptolemy.data.expr.Parameter {.N.D.A.bold} false}
    {ptolemy.data.expr.Parameter {.N.D.A.italic} false}
    {ptolemy.data.expr.StringParameter {.N.D.A.anchor} "northwest"}
    {ptolemy.kernel.util.StringAttribute {.N.D.A.attributeName} attributes {
    }}
    {ptolemy.data.expr.Parameter {.N.D.A.displayWidth} 6}
    {ptolemy.data.expr.Parameter {.N.D.A.useExpression} false}
}}

test AttributeValueAttribute-8.4 {Construct an AttributeValueAttribute in an unnamed NamedObj} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj]
    set c [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n C]
    $c getFullName
} {..C}

test AttributeValueAttribute-8.5 {setContainer to an unnamed NamedObj} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj]
    set c [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n C]
    $c setContainer $n
    $c getFullName
} {..C}

test AttributeValueAttribute-8.6 {setContainer to an unnamed NamedObj} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj]
    set c [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n C]
    set d [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n D]
    $c setContainer $n
    $d setContainer $c
    $d getFullName
} {..C.D}

test AttributeValueAttribute-8.7 {setContainer recursively} {
    set n [java::new ptolemy.kernel.util.NamedObj top]
    set c [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n c]
    set d [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n d]
    $c setContainer $d
    catch {$d setContainer $c} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Attempt to construct recursive containment of attributes
  in .top.d and .top.d.c}}

test AttributeValueAttribute-10.1 {updateContent} {
    set n [java::new ptolemy.kernel.util.NamedObj top]
    set c [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n C]
    # AttributeValueAttribute.updateContent does nothing.
    $c updateContent
} {}

test AttributeValueAttribute-11.1 {validate} {
    set n [java::new ptolemy.kernel.util.NamedObj top]
    set c [java::new ptolemy.vergil.kernel.attributes.AttributeValueAttribute $n C]
    java::isnull [$c validate]
} {1}
