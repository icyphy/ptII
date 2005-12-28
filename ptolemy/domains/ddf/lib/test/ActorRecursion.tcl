# Test ActorRecursion
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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
test ActorRecursion-1.1 {cover _exportMoML} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set actorRecursion [java::new ptolemy.domains.ddf.lib.ActorRecursion \
	$e0 actorRecursion]
    $actorRecursion exportMoML 
} {<entity name="actorRecursion" class="ptolemy.actor.TypedCompositeActor">
    <property name="DDFDirector" class="ptolemy.domains.ddf.kernel.DDFDirector">
        <property name="timeResolution" class="ptolemy.moml.SharedParameter" value="1E-10">
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="maximumReceiverCapacity" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="runUntilDeadlockInOneIteration" class="ptolemy.data.expr.Parameter" value="false">
        </property>
    </property>
    <property name="recursionActor" class="ptolemy.data.expr.StringParameter">
    </property>
</entity>}
