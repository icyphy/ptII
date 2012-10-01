# Test ActorRecursion
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005-2012 The Regents of the University of California.
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
        <property name="localClock" class="ptolemy.actor.LocalClock">
            <property name="globalTimeResolution" class="ptolemy.actor.parameters.SharedParameter" value="1E-10">
            </property>
            <property name="clockRate" class="ptolemy.data.expr.Parameter" value="1.0">
            </property>
        </property>
        <property name="startTime" class="ptolemy.data.expr.Parameter">
        </property>
        <property name="stopTime" class="ptolemy.data.expr.Parameter">
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="maximumReceiverCapacity" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="runUntilDeadlockInOneIteration" class="ptolemy.data.expr.Parameter" value="false">
        </property>
    </property>
    <property name="recursionActor" class="ptolemy.data.expr.StringParameter" value="">
    </property>
</entity>
}

test ActorRecursion-1.2 {cover _exportMoML in a model} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser addMoMLFilter [java::new ptolemy.moml.filter.RemoveGraphicalClasses]
    set topLevel [java::cast ptolemy.actor.CompositeActor \
		[$parser {parse java.net.URL java.net.URL} \
		[java::cast {java.net.URL} [java::null]] \
		[[java::new java.io.File auto/Eratosthenes.xml] toURL]]]
    set manager [java::new ptolemy.actor.Manager [$topLevel workspace] "foo"]
    $topLevel setManager $manager
    $manager execute	

    set actorRecursion [$topLevel getEntity Prime_Number_Filter.ActorRecursion]
    $actorRecursion exportMoML 
} {<entity name="ActorRecursion" class="ptolemy.domains.ddf.lib.ActorRecursion">
    <property name="recursionActor" class="ptolemy.data.expr.StringParameter" value="Prime_Number_Filter">
    </property>
    <property name="_location" class="ptolemy.kernel.util.Location" value="[430.0, 225.0]">
    </property>
    <port name="in" class="ptolemy.actor.TypedIOPort">
        <property name="input"/>
        <property name="_showName" class="ptolemy.kernel.util.SingletonAttribute">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{0.0, 0.0}">
        </property>
    </port>
    <port name="out" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="_showName" class="ptolemy.kernel.util.SingletonAttribute">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{0.0, 0.0}">
        </property>
        <property name="_type" class="ptolemy.actor.TypeAttribute" value="int">
        </property>
        <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="{1}">
        </property>
    </port>
</entity>
}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
