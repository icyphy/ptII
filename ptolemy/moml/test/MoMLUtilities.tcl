# Tests for the MoMLUtilities class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2007 The Regents of the University of California.
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

set paramCopy {
<entity name="paramCopy" class="ptolemy.actor.TypedCompositeActor">
    <property name="myParam" class="ptolemy.data.expr.Parameter" value="1">
    </property>
}

set paramCopyConst {
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="myParam">
        </property>
    </entity>
 <entity name="Recorder" class="ptolemy.actor.lib.Recorder">
        <property name="_location" class="ptolemy.kernel.util.Location" value="[240.0, 255.0]">
        </property>
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="Const.output" relation="relation"/>
    <link port="Recorder.input" relation="relation"/>
}

set baseModel2 {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="2"/>
    </property>
</entity>
}

proc parseMoML {moml workspaceName } {
    set w [java::new ptolemy.kernel.util.Workspace $workspaceName]
    set parser [java::new ptolemy.moml.MoMLParser $w]

    set toplevel [java::cast ptolemy.actor.CompositeActor \
		      [$parser parse $moml]]
    return $toplevel
}

# Invoke a change request and get a token.
# This proc is used to test cut and paste.
proc changeAndGetToken {toplevel changeRequestString} {
    set changeRequest [java::new ptolemy.moml.MoMLChangeRequest \
			   $toplevel $toplevel $changeRequestString]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] \
		     "myManager"]
    $toplevel setManager $manager
    $manager requestChange $changeRequest
    set const [$toplevel getEntity Const]
    set value [java::cast ptolemy.data.expr.Variable \
		   [$const getAttribute value]]
    return [$value getToken]
}

######################################################################
####
#
test MoMLUtilties-1.1 {copy a const that refers another parameter } {
    set toplevel1 [parseMoML $baseModel2 w1_1]

    # Create a change request: a const that refers to missing param 

    catch {changeAndGetToken $toplevel1 \
	       "<group name=\"auto\">$paramCopyConst</group>"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Error evaluating expression: myParam
  in .top.Const.value
Because:
The ID myParam is undefined.}}

######################################################################
####
#
test MoMLUtilties-1.2.1 {copy a const that refers another parameter } {
    set moml1_2 "$header $paramCopy $paramCopyConst </entity>"
    set toplevel1_2 [parseMoML $moml1_2 w1_2]

    set copyMoML [java::call ptolemy.moml.MoMLUtilities \
		      checkCopy $paramCopyConst $toplevel1_2]
} {<property name="myParam" class="ptolemy.data.expr.Parameter" value="1">
</property>
}

######################################################################
####
#
test MoMLUtilties-1.2.2 {Simulate paste with the myParam variable defined} {
    # Uses 1.2.1 above
    set value [changeAndGetToken $toplevel1_2 "<group name=\"auto\">$copyMoML $paramCopyConst\n</group>"]
    list [$value toString]
} {1}
