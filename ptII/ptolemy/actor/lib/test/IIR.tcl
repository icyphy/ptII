# Test IIR
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 1997-2007 The Regents of the University of California.
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

######################################################################
####
#
test IIR-1.1 {test constructor} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set iir [java::new ptolemy.actor.lib.IIR $e0 g]
    set numerator [getParameter $iir numerator]
    set denominator [getParameter $iir denominator]

    set numeratorVal [[$numerator getToken] toString]
    set denominatorVal [[$denominator getToken] toString]

    list $numeratorVal $denominatorVal
} {{{1.0}} {{1.0}}}

test IIR-1.2 {test clone} {
    set iir2 [java::cast ptolemy.actor.lib.IIR [$iir clone [$e0 workspace]]]
    $numerator setExpression {2.0}
    set numerator [getParameter $iir2 numerator]
    [$numerator getToken] toString
} {{1.0}}

set model "<?xml version=\"1.0\" standalone=\"no\"?>
<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"
    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">
<entity name=\"cannotInstantiate\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"SDF Director\" class=\"ptolemy.domains.sdf.kernel.SDFDirector\"/>
    <class name=\"CompositeClassDefinition\" extends=\"ptolemy.actor.TypedCompositeActor\">
        <property name=\"m\" class=\"ptolemy.data.expr.Parameter\" value=\"1.0\"/>
        <entity name=\"IIR\" class=\"ptolemy.actor.lib.IIR\">
            <property name=\"numerator\" class=\"ptolemy.data.expr.Parameter\" value=\"{m}\">
            </property>
        </entity>
    </class>
</entity>"

test IIR-1.3 {test clone with numerator set to a parameter} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set top [java::cast ptolemy.actor.TypedCompositeActor [$parser parse $model]]
    set base [$top getEntity "CompositeClassDefinition"]
    set clone [java::cast ptolemy.kernel.util.NamedObj [$base instantiate $top "myClone"]]
    list [$clone exportMoML]
} {{<entity name="myClone" class=".cannotInstantiate.CompositeClassDefinition">
</entity>
}}


