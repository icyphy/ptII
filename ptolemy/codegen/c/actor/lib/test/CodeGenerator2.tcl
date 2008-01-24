# Test CodeGenerator
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs sdfModel] == "" } then {
    source [file join $PTII util testsuite models.tcl]
}

# main(String[]) is tested in
# $PTII/ptolemy/codegen/c/actor/lib/test/CodeGenerator2.tcl
# because the test relies on SDFDirector being built and actors being present.
#####
test CodeGenerator-2.1 {Call main} {
    set args [java::new {String[]} 1  auto/Ramp.xml]
    java::call ptolemy.codegen.kernel.CodeGenerator main $args
} {}

#####
test CodeGenerator-3.1 {Call main and generate code in the current directory } {
    file delete -force Ramp.c Ramp.mk 
    set args [java::new {String[]} 3 [list {-codeDirectory} {$CWD} {auto/Ramp.xml}]]
    java::call ptolemy.codegen.kernel.CodeGenerator main $args
    list [file exists Ramp.c] [file exists Ramp.mk]
} {1 1}

#####
test CodeGenerator-4.1 {Call main and copy a file to the codeDirectory} {
    set codeDirectory [java::call ptolemy.util.StringUtilities getProperty user.home]
    set necessaryFile1 [file join $codeDirectory codegen necessaryFile1]
    set necessaryFile2 [file join $codeDirectory codegen necessaryFile2]
    file delete -force $necessaryFile1
    file delete -force $necessaryFile2

    set args [java::new {String[]} 1 [list {auto/RampNecessaryFiles.xml}]]
    java::call ptolemy.codegen.kernel.CodeGenerator main $args

    list \
	[file exists $necessaryFile1] \
	[file exists $necessaryFile2]
} {1 1}
