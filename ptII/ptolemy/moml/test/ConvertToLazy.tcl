# Tests for the ConvertToLazy Class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2008-2009 The Regents of the University of California.
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

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}


# Note that there are a number of tests for ConvertToLazy
# in $PTII/ptolemy/actor/lib/test/performance/PubSubLazy.tcl

######################################################################
####
#
test ConvertToLazy-1.1 {Convert a test model} {
    jdkCapture {
	java::new ptolemy.moml.ConvertToLazy ConvertToLazyTest.xml 10 
    } moml_1
    set w1 [java::new ptolemy.kernel.util.Workspace w1]
    set parser1 [java::new ptolemy.moml.MoMLParser $w1]
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
		      [$parser1 parse $moml_1]]
    set manager [java::new ptolemy.actor.Manager \
            [$toplevel workspace] "manager"]
    $toplevel setManager $manager
    $manager execute

    list [regexp LazyTypedCompositeActor $moml_1] \
	[regexp configure $moml_1]
} {1 1}

test ConvertToLazy-2.1 {Only convert composites with 1000 deep entities} {
    $parser1 reset
    $parser1 purgeAllModelRecords

    jdkCapture {
	java::new ptolemy.moml.ConvertToLazy ConvertToLazyTest.xml 10000
    } moml_2
    # Nothing should have been converted
    regexp LazyTypedCompositeActor $moml_2
} {0}
