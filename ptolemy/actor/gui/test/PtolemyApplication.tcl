# Test PtolemyApplication
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2006 The Regents of the University of California.
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
#set VERBOSE 1

set testCase {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
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
test PtolemyApplication-1.0 {test constructor with no arguments} {
    set empty [java::new {java.lang.String[]} 0]
    # If we are running without a display then creating a PtolemyApplication
    # will fail.  We set app here to null so that we can detect the problem
    set app [java::null]
    set app [java::new ptolemy.actor.gui.PtolemyApplication $empty]
    list {}
    # success is just not throwing an exception.
} {{}}

test PtolemyApplication-1.1 {test constructor with one file argument} {
    set empty [java::new {java.lang.String[]} 1 {test.xml}]
    # If we are running without a display then creating a PtolemyApplication
    # will fail.  We set app here to null so that we can detect the problem
    set app [java::null]
    set app [java::new ptolemy.actor.gui.PtolemyApplication $empty]
    list {}
    # success is just not throwing an exception.
} {{}}

#########################################################################


# NOTE: we can't test anything that calls System.exit(0) in
# PtolemyApplication or MoMLApplication, so we can't test -help or -version
# I don't think that we should be calling System.exit(0), but 
# fixing this so that we shutdown gracefully would be tricky

#test PtolemyApplication-2.0 {test command line options} {
#    set cmdArgs [java::new {java.lang.String[]} 2 {{-version} {-test}}]
#    set app [java::new ptolemy.actor.gui.PtolemyApplication $cmdArgs]
#    list {}
#    # success is just not throwing an exception.
#} {{}}

test PtolemyApplication-2.1 {test invalid command line options} {
    set cmdArgs [java::new {java.lang.String[]} 2 {{-foo} {-test}}]
    catch {set app [java::new ptolemy.actor.gui.PtolemyApplication $cmdArgs]} \
            msg
    list $msg
} {{java.lang.Exception: Failed to parse "-foo -test"}}
