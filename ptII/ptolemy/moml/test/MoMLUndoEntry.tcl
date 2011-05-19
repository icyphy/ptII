# Tests for the MoMLUndoEntry class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006-2007 The Regents of the University of California.
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
test MoMLUndoEntry-1.0 {Constructor} {
    set workspace [java::new ptolemy.kernel.util.Workspace workspace]
    set toplevel [java::new ptolemy.kernel.CompositeEntity $workspace] 
    $toplevel setName top

    set a [java::new ptolemy.kernel.CompositeEntity $toplevel a] 

    set undoEntry [java::new ptolemy.moml.MoMLUndoEntry $a \
		       {<group> <property name="test" class="ptolemy.data.expr.Parameter" value="3"> </property> </group>}]
    list [$undoEntry toString]
} {{<group> <property name="test" class="ptolemy.data.expr.Parameter" value="3"> </property> </group>
...in context: .top.a}}

######################################################################
####
#
test MoMLUndoEntry-2.0 {execute} {
    $undoEntry execute
    list [$toplevel exportMoML]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="6.1.devel">
    </property>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <property name="test" class="ptolemy.data.expr.Parameter" value="3">
        </property>
    </entity>
</entity>
}}


######################################################################
####
#
test MoMLUndoEntry-3.0 {changeFailed} {
    set workspace3 [java::new ptolemy.kernel.util.Workspace workspace3]
    set toplevel3 [java::new ptolemy.kernel.CompositeEntity $workspace3] 
    $toplevel3 setName top3

    set a3 [java::new ptolemy.kernel.CompositeEntity $toplevel3 a3] 

    set undoEntry3 [java::new ptolemy.moml.MoMLUndoEntry $a3 \
			{xxx}]
    catch {$undoEntry3 execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.InternalErrorException: ChangeRequest failed (NOTE: there is no ChangeListener):
xxx
Because:
expected character (found "x") (expected "<") in [external stream] at line 1 and column 5}}
