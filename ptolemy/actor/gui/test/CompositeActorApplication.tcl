# Test CompositeActorApplication
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

set testCase {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/archive/moml.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <director name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="2"/>
    </director>
    <entity name="ramp" class="ptolemy.actor.lib.Ramp"></entity>
    <entity name="rec" class="ptolemy.actor.lib.Recorder"></entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation"/>
    <link port="ramp.output" relation="r"/>
    <link port="rec.input" relation="r"/>
</model>
}

######################################################################
####
#
test CompositeActorApplication-1.0 {test constructor} {
    set empty [java::new {java.lang.String[]} 0]
    set app [java::new ptolemy.actor.gui.CompositeActorApplication $empty]
    list {}
    # success is just not throwing an exception.
} {{}}

test CompositeActorApplication-1.1 {test add} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set top [java::cast ptolemy.actor.CompositeActor [$parser parse $testCase]]
    $app add $top
    list {}
    # success is just not throwing an exception.
} {{}}

test CompositeActorApplication-1.2 {execute it} {
    $app startRun $top
    list {}
    # success is just not throwing an exception.
} {{}}

test CompositeActorApplication-1.3 {wait for execution to finish} {
    $app waitForFinish
    set rec [java::cast ptolemy.actor.lib.Recorder [$top getEntity rec]]
    listToStrings [$rec getHistory 0]
} {0 1}

test CompositeActorApplication-1.4 {test stopRun} {
    set iter [java::cast ptolemy.data.expr.Parameter \
            [[$top getDirector] getAttribute iterations]]
    $iter setExpression {-1}
    $app startRun $top
    $app stopRun $top
    $app waitForFinish
    # success here is returning (not hanging).
} {}

#########################################################################

test CompositeActorApplication-2.0 {test command line options} {
    set cmdArgs [java::new {java.lang.String[]} 2 {{-version} {-help}}]
    set app [java::new ptolemy.actor.gui.CompositeActorApplication $cmdArgs]
    list {}
    # success is just not throwing an exception.
} {{}}

test CompositeActorApplication-2.1 {test invalid command line options} {
    set cmdArgs [java::new {java.lang.String[]} 2 {{-foo} {-help}}]
    catch {set app [java::new ptolemy.actor.gui.CompositeActorApplication $cmdArgs]} \
            msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Unrecognized option: -foo}}

test CompositeActorApplication-2.2 {test invalid class name} {
    set cmdArgs [java::new {java.lang.String[]} 2 \
            {{-class} {ptolemy.actor.gui.test.bogon}}]
    catch {set app [java::new ptolemy.actor.gui.CompositeActorApplication $cmdArgs]} \
            msg
    list $msg
} {{java.lang.ClassNotFoundException: ptolemy.actor.gui.test.bogon}}

test CompositeActorApplication-2.3 {test valid class name} {
    set cmdArgs [java::new {java.lang.String[]} 4 \
            {{-class} {ptolemy.actor.gui.test.TestModel} \
            {-class} {ptolemy.actor.gui.test.TestModel}}]
    # The model execution is started in the constructor below...
    set app [java::new ptolemy.actor.gui.CompositeActorApplication $cmdArgs]
    set models [listToObjects [$app models]]
    set result {}
    $app waitForFinish
    foreach model $models {
        set modelc [java::cast ptolemy.actor.gui.test.TestModel $model]
        lappend result [listToStrings [$modelc getResults]]
    }
    list $result
} {{{0 1 2} {0 1 2}}}
