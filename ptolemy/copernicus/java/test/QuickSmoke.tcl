# Tests for deep codegen
#
# @Author: Steve Neuendorffer, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2003 The Regents of the University of California.
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

if {[info procs jdkClassPathSeparator] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

if {[info procs sootCodeGeneration] == "" } then { 
    source [file join $PTII util testsuite codegen.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#

# First, do an SDF test just to be sure things are working
test QuickSmoke-1.1 {Compile and run the SDF IIR test} {
    global PTII
    set result [sootCodeGeneration $PTII \
		[file join $relativePathToPTII ptolemy actor lib test auto \
		     IIR.xml] "Deep" "" 0 0 smokeTest]
    list {}
} {{}}


test QuickSmoke-1.2 {Compile and run the SDF OrthogonalCom test} {
    global PTII
    set result [sootCodeGeneration $PTII \
		    [file join $relativePathToPTII ptolemy domains sdf demo OrthogonalCom \
			 OrthogonalCom.xml] "Deep" 1000 0 0 smokeTest]
    list {}
} {{}}

test QuickSmoke-1.3 {Compile and run the ComplexDivide test} {
    global PTII
    set result [sootCodeGeneration $PTII \
		    [file join $relativePathToPTII ptolemy actor lib test auto \
			 ComplexDivide.xml] "Deep" 1000 0 0 smokeTest]
    list {}
} {{}}
