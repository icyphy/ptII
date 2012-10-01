# Test PtExecuteApplication
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

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

java::call System setProperty ptolemy.ptII.doNotExit true

java::call ptolemy.moml.MoMLParser purgeModelRecord test.xml
set parser [java::new ptolemy.moml.MoMLParser]
$parser resetAll

######################################################################
####
#
test PtExecuteApplication-1.0 {test reading MoML file} {
    set cmdArgs [java::new {java.lang.String[]} 1 {test.xml}]
    set app [java::new ptolemy.actor.gui.PtExecuteApplication $cmdArgs]
    list {}
    # success is just not throwing an exception.
} {{}}

test PtExecuteApplication-1.1 {check result of running the model} {
    $app runModels
    $app waitForFinish
    set models [listToObjects [$app models]]
    set result {}
    foreach model $models {
        set modelc [java::cast ptolemy.kernel.CompositeEntity $model]
	if [java::instanceof $modelc ptolemy.actor.gui.Configuration] {
	    continue
	}
        set rec [java::cast ptolemy.actor.lib.Recorder \
                [$modelc getEntity "rec"]]
	if [java::isnull $rec] {
	    lappend result "rec for [$modelc getFullName] was null!"
	} else {
	    lappend result [listToStrings [$rec getHistory 0]]
	    $modelc setContainer [java::null]
	}
    }
    list $result
} {{{0 1 2}}}

java::call ptolemy.moml.MoMLParser purgeModelRecord test.xml
set parser [java::new ptolemy.moml.MoMLParser]
$parser resetAll

######################################################################
####
#
sleep 2 0
test PtExecuteApplication-1.2 {check parameter handling} {
    set cmdArgs [java::new {java.lang.String[]} 3 \
            {{-step} {4} {test.xml}}]
    set app [java::new ptolemy.actor.gui.PtExecuteApplication $cmdArgs]
    set models [listToObjects [$app models]]
    set result {}
    $app runModels
    $app waitForFinish
    foreach model $models {
        set modelc [java::cast ptolemy.kernel.CompositeEntity $model]
	if [java::instanceof $modelc ptolemy.actor.gui.Configuration] {
	    continue
	}
	puts [$modelc getFullName]
        set rec [java::cast ptolemy.actor.lib.Recorder \
                [$modelc getEntity "rec"]]
	lappend result [listToStrings [$rec getHistory 0]]
	$modelc setContainer [java::null]
    }
    java::call ptolemy.moml.MoMLParser purgeModelRecord test.xml
    list $result
} {{{0 4 8}}}

set parser [java::new ptolemy.moml.MoMLParser]
$parser resetAll

sleep 2 0
test PtExecuteApplication-1.3 {check parameter handling} {
    set cmdArgs [java::new {java.lang.String[]} 3            {{-director.iterations} {5} {test.xml}}]
    set app [java::new ptolemy.actor.gui.PtExecuteApplication $cmdArgs]
    set models [listToObjects [$app models]]
    set result {}
    $app runModels
    $app waitForFinish
    foreach model $models {
        set modelc [java::cast ptolemy.kernel.CompositeEntity $model]
	if [java::instanceof $modelc ptolemy.actor.gui.Configuration] {
	    continue
	}
        set rec [java::cast ptolemy.actor.lib.Recorder \
                [$modelc getEntity "rec"]]
	lappend result [listToStrings [$rec getHistory 0]]
	puts [$modelc exportMoML]
	$modelc setContainer [java::null]
    }
    java::call ptolemy.moml.MoMLParser purgeModelRecord test.xml
    list $result
} {{{0 1 2 3 4}}}
