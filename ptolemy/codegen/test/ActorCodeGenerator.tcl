# Tests for the ActorCodeGenerator class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2001 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test ActorCodeGenerator-1.1 {} {
    set codeGeneratorClassFactory \
	    [java::call ptolemy.codegen.CodeGeneratorClassFactory getInstance]
    set actorCodeGeneratorInfo \
	    [java::new ptolemy.codegen.ActorCodeGeneratorInfo]
    set outputDirectoryName "."
    set outputPackageName "acg"
    file mkdir $outputPackageName

    set actorCodeGenerator \
	    [java::new ptolemy.codegen.ActorCodeGenerator \
	    $codeGeneratorClassFactory \
	    $outputDirectoryName \
	    $outputPackageName]

    # Create a Ramp
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    java::field $actorCodeGeneratorInfo actor $ramp

    # Set up some types
    set output [java::field [java::cast ptolemy.actor.lib.Source $ramp] output]
    
    $output setTypeEquals [java::field ptolemy.data.type.BaseType INT]

    # Generate Code
    $actorCodeGenerator pass1 $actorCodeGeneratorInfo
} {acg.CG_Ramp_Ramp}

