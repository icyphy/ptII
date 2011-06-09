# Tests for the SingletonConfigurableAttribute class
#
# @Author: Steve Neuendorffer and Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

######################################################################
####
#
test SingletonConfigurableAttribute-1.1 {test export moml.} {
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    $n0 setName N0
    set p1 [java::new ptolemy.kernel.util.SingletonConfigurableAttribute $n0 P1]
    $p1 configure [java::null] [java::null] {My Test String}
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="N0" class="ptolemy.kernel.util.NamedObj">
    <property name="P1" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
        <configure>My Test String</configure>
    </property>
</entity>
}

test SingletonConfigurableAttribute-1.2 {test replacement of previous via constructor.} {
    set p2 [java::new ptolemy.kernel.util.SingletonConfigurableAttribute $n0 P1]
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="N0" class="ptolemy.kernel.util.NamedObj">
    <property name="P1" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
    </property>
</entity>
}

test SingletonConfigurableAttribute-1.2 {test replacement of setContainer()} {
    set p3 [java::new ptolemy.kernel.util.SingletonConfigurableAttribute]
    $p3 setName P1
    $p3 configure [java::null] [java::null] {yyy}
    $p3 setContainer $n0
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="N0" class="ptolemy.kernel.util.NamedObj">
    <property name="P1" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
        <configure>yyy</configure>
    </property>
</entity>
}


test SingletonConfigurableAttribute1.1 {Create SingletonConfigurableAttributes} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n1 [java::new ptolemy.kernel.util.NamedObj] 
    set sa1 [java::new ptolemy.kernel.util.SingletonConfigurableAttribute] 
    set sa2 [java::new ptolemy.kernel.util.SingletonConfigurableAttribute $w] 
    set sa3 [java::new ptolemy.kernel.util.SingletonConfigurableAttribute $n1 "foo"] 
    list [$sa1 toString] [$sa2 toString ] [$sa3 toString]
} {{ptolemy.kernel.util.SingletonConfigurableAttribute {.}} {ptolemy.kernel.util.SingletonConfigurableAttribute {.}} {ptolemy.kernel.util.SingletonConfigurableAttribute {..foo}}}


test SingletonConfigurableAttribute2.1 {setContainer with the same name} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "N1"] 
    set n2 [java::new ptolemy.kernel.util.NamedObj "N2"] 
    set sa3 [java::new ptolemy.kernel.util.SingletonConfigurableAttribute $n1 "foo"] 
    set sa4 [java::new ptolemy.kernel.util.SingletonConfigurableAttribute $n2 "foo"] 
    $sa4 setContainer $n1
    list [$sa3 toString] [$sa4 toString]
} {{ptolemy.kernel.util.SingletonConfigurableAttribute {.foo}} {ptolemy.kernel.util.SingletonConfigurableAttribute {.N1.foo}}}


test SingletonConfigurableAttribute-2.1 {setContainer with different workspaces} {
    set w1 [java::new ptolemy.kernel.util.Workspace W1]
    set w2 [java::new ptolemy.kernel.util.Workspace W2]
    set n1 [java::new ptolemy.kernel.util.NamedObj $w1 N1] 
    set n2 [java::new ptolemy.kernel.util.NamedObj $w2 N2] 
    set sa5 [java::new ptolemy.kernel.util.SingletonConfigurableAttribute $n1 foo] 
    set sa6 [java::new ptolemy.kernel.util.SingletonConfigurableAttribute $n2 foo] 
    # Cover the catch block in setContainer
    catch {$sa5 setContainer $n2} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot set container because workspaces are different.
  in .N1.foo and .N2}}
