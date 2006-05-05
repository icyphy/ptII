# Test StaticSchedulingCodeGenerator
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005-2006 The Regents of the University of California.
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

if {[info procs sdfModel] == "" } then {
    source [file join $PTII util testsuite models.tcl]
}

#####
test StaticSchedulingCodeGenerator-1.1 {constructor} {
    set model [sdfModel]
    set sscc [java::new ptolemy.codegen.kernel.StaticSchedulingCodeGenerator \
		  $model "myStaticSchedulingCodeGenerator"]
    # For code coverage
    $sscc generateModeTransitionCode [java::new StringBuffer]

    # Call createOffsetVariablesIfNeeded for codeCoverage
    list [$sscc toString] \
	[$sscc createOffsetVariablesIfNeeded] \
	[[$sscc getHeaderFiles] size] \
	[[$sscc getSharedCode] size] \
	[[$sscc getModifiedVariables] size]
} {{ptolemy.codegen.kernel.StaticSchedulingCodeGenerator {.top.myStaticSchedulingCodeGenerator}} {} 0 0 0}

#####
test StaticSchedulingCodeGenerator-1.2 {no director} {
    set compositeEntity [java::new ptolemy.kernel.CompositeEntity] 
    set sscc [java::new ptolemy.codegen.kernel.StaticSchedulingCodeGenerator \
		  $compositeEntity "sscc1_2"]
    catch {$sscc generateBodyCode} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot find helper class ptolemy.codegen.c.kernel.CompositeEntity
  in .<Unnamed Object>.sscc1_2
Because:
ptolemy.codegen.c.kernel.CompositeEntity}}


#####
test StaticSchedulingCodeGenerator-2.1 {generateMainEntryCode, generateMainExitCode} {
    set compositeEntity [java::new ptolemy.kernel.CompositeEntity] 
    set sscc [java::new ptolemy.codegen.kernel.StaticSchedulingCodeGenerator \
		  $compositeEntity "sscc2_1"]
    list [$sscc generateMainEntryCode] \
	[$sscc generateMainExitCode] 
} {{    /* main entry code */
} {    /* main exit code */
}}
