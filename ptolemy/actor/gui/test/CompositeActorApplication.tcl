# Test CompositeActorApplication
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2012 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

set testCase {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="testCase" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="2"/>
    </property>
    <entity name="ramp" class="ptolemy.actor.lib.Ramp"></entity>
    <entity name="rec" class="ptolemy.actor.lib.Recorder"></entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation"/>
    <link port="ramp.output" relation="r"/>
    <link port="rec.input" relation="r"/>
</entity>
}
######################################################################
####
#
test CompositeActorApplication-1.0 {test main with no arguments} {
    set empty [java::new {java.lang.String[]} 0 {}]
    java::call ptolemy.actor.gui.CompositeActorApplication main $empty
    list {}
    # success is just not throwing an exception.
} {{}}

test CompositeActorApplication-1.05 {test main with a file} {
    java::call System setProperty ptolemy.ptII.doNotExit true
    set topArgs [java::new {java.lang.String[]} 3 {-test -class ptolemy.actor.gui.test.TestModel} ]
    java::call ptolemy.actor.gui.CompositeActorApplication main $topArgs
    list {}
    # success is just not throwing an exception.
} {{}}

test CompositeActorApplication-1.1 {test startRun} {
    set app [java::new ptolemy.actor.gui.CompositeActorApplication]
    $app processArgs $topArgs
    set top [java::cast ptolemy.actor.CompositeActor [[$app models] get 0]]
    $app startRun $top
    $app close
    list {}
    # success is just not throwing an exception.
} {{}}

#test CompositeActorApplication-1.3 {wait for execution to finish} {
#    $app waitForFinish
#    set rec [java::cast ptolemy.actor.lib.Recorder [$testCase getEntity rec]]
#    listToStrings [$rec getHistory 0]
#} {0 1}

test CompositeActorApplication-1.4 {test stopRun} {
    set iter [java::cast ptolemy.data.expr.Parameter \
            [[$top getDirector] getAttribute iterations]]
    $iter setExpression {-1}
    $app startRun $top
    $app stopRun $top
    $app close
    $app waitForFinish
    list {}
    # success here is returning (not hanging).
} {{}}

#########################################################################

test CompositeActorApplication-2.0 {test command line options} {
    set cmdArgs [java::new {java.lang.String[]} 2 {{-version} {-help}}]
    jdkCapture {
	java::call ptolemy.actor.gui.CompositeActorApplication main $cmdArgs
    } stdout
    regsub  {.*ms. Memory:.*} $stdout {} result2
    regsub  {^Version.*$} $result2 {VersionXXX} result3
    regsub {.*Usage:} $result3 {XXXUsage:} result4

    # FIXME: sometimes there is a newline before VersionXXX orafter -version?
    list [string range $result4 [string first VersionXXX $result4] [expr {[string last -version $result4] + [string length {-version}]}]]
} {{VersionXXX
XXXUsage: ptolemy [ options ]

Options that take values:
 -class <classname>
 -<parameter name> <parameter value>

Boolean flags:
 -help -test -version
}}

test CompositeActorApplication-2.1 {test invalid command line options} {
    set cmdArgs [java::new {java.lang.String[]} 2 {{-foo} {-help}}]
    set app2_1 [java::new ptolemy.actor.gui.CompositeActorApplication]
    catch {$app2_1 processArgs $cmdArgs} \
	msg
    $app2_1 close
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Unrecognized option: -foo}}

test CompositeActorApplication-2.2 {test invalid class name} {
    set cmdArgs [java::new {java.lang.String[]} 2 \
            {{-class} {ptolemy.actor.gui.test.bogon}}]
    set app2_2 [java::new ptolemy.actor.gui.CompositeActorApplication]
    jdkCaptureErr {
	catch {$app2_2 processArgs $cmdArgs} msg
    } err
    list [string range $msg 0 92] $err
} {{ptolemy.kernel.util.IllegalActionException: Could not find class ptolemy.actor.gui.test.bogon} {}}

test CompositeActorApplication-2.3 {test valid class name} {
    set cmdArgs [java::new {java.lang.String[]} 5 \
		     {{-test} {-class} {ptolemy.actor.gui.test.TestModel} \
            {-class} {ptolemy.actor.gui.test.TestModel}}]
    set app2_3 [java::new ptolemy.actor.gui.CompositeActorApplication]
    $app2_3 processArgs $cmdArgs
    set models [$app2_3 models]
    set result {}
    # The 0 means don't print dots.
    sleep 2 0
    $app2_3 waitForFinish
    set modelc [java::cast ptolemy.actor.gui.test.TestModel [$models get 0]]
    lappend result [listToStrings [$modelc getResults]]
    set modelc [java::cast ptolemy.actor.gui.test.TestModel [$models get 1]]
    lappend result [listToStrings [$modelc getResults]]
    list $result
} {{{0 1 2} {0 1 2}}}
