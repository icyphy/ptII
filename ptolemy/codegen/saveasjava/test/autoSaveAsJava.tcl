# Tests for the MoMLToJava class that use the actor/lib/test/auto tests
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

if {[info procs saveAsJava] == "" } then {
    source [file join $PTII util testsuite saveAsJava.tcl]
}
# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#


test autoSaveAsJava-1.0 {} {
    set results [saveAsJava rampFileWriter.xml]
    list [lrange $results 1 4]
} {{1 2 3 4}}

autoSaveAsJava [file join $relativePathToPTII ptolemy actor lib test auto]

autoSaveAsJava [file join $relativePathToPTII ptolemy actor lib conversions test auto]
#autoSaveAsJava [file join $relativePathToPTII ptolemy actor lib javasound test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains ct lib test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains de lib test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains dt kernel test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains fsm kernel test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains fsm test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains hdf kernel test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains sdf kernel test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains sdf lib test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains sdf lib vq test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains sr kernel test auto]
autoSaveAsJava [file join $relativePathToPTII ptolemy domains sr lib test auto]

# Print out stats
#doneTests
