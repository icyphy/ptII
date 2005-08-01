# Test Director
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

if {[info procs sdfModel] == "" } then {
    source [file join $PTII util testsuite models.tcl]
}

#####
test Director-1.1 {Instantiate a Director, call a few methods} {
    set model [sdfModel]
    set SDFDirector [$model getDirector]
    set cgDirector [java::new ptolemy.codegen.kernel.Director \
			$SDFDirector]

    set codeGenerator \
	    [java::new ptolemy.codegen.kernel.CodeGenerator \
	    $model "myCodeGenerator"]

    $cgDirector setCodeGenerator $codeGenerator

    list \
	[[$cgDirector getComponent] toString]
} {{ptolemy.domains.sdf.kernel.SDFDirector {.top.}}}


#####
test Director-2.1 {generateFireCode(StringBuffer)} {
    set results [java::new StringBuffer]
    $cgDirector generateFireCode $results
    list [$results toString]
} {{/* The firing of the director. */
}}

#####
test Director-3.1 {generateInitializeCode(StringBuffer)} {
    list [$cgDirector generateInitializeCode]
} {{/* The initialization of the director. */
}}

#####
test Director-4.1 {generatePreinitializeCode(StringBuffer)} {
    list [$cgDirector generatePreinitializeCode]
} {{/* The preinitialization of the director. */
}}

#####
test Director-5.1 {generateWrapupCode(StringBuffer)} {
    set results [java::new StringBuffer]
    $cgDirector generateWrapupCode $results
    list [$results toString]
} {{/* The wrapup of the director. */
}}

#####
test Director-6.1 {getBufferSize} {
    # Always return 1 in this base class.
    list [$cgDirector getBufferSize [java::null] -1]
} {1}

#####
test Director-7.1 {getComponent()} {
    [$cgDirector getComponent] toString
} {ptolemy.domains.sdf.kernel.SDFDirector {.top.}}

