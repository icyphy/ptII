# Test FMUQSS
#
# @Author: Christopher Brooks
#
# @Version: $Id: FMUImport.tcl 71326 2015-01-12 05:24:47Z cxh $
#
# @Copyright (c) 2015 The Regents of the University of California.
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

#set VERBOSE 1
if {[string compare sdfModel [info procs sdfModel]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}

# Try to load a non-qss FMU and return the error in a format
# that will be the same on all platforms
proc tryToLoadNonQSSFMU {fmu} {
    set e1 [sdfModel 5]
    set fmuFile [java::call ptolemy.util.FileUtilities nameToFile $fmu [java::null]]
    set fmuFileParameter [java::new ptolemy.data.expr.FileParameter $e1 fmuFileParameter]
    $fmuFileParameter setExpression [$fmuFile getCanonicalPath]
    catch {java::call ptolemy.actor.lib.fmi.FMUQSS importFMU $e1 $fmuFileParameter $e1 100.0 100.0} err
    regsub {The fmu ".*ptII/ptolemy} $err {The fmu "xxx/ptII/ptolemy} err2
    return $err2
}

######################################################################
####
#
test FMUQSS-1.1 {Test out importFMU on an fmu that is FMI-1.0, not FMI-2.0 and should be rejected} {
    set err [tryToLoadNonQSSFMU {$CLASSPATH/ptolemy/actor/lib/fmi/test/auto/helloWorld.fmu}]
    list $err
} {{ptolemy.kernel.util.IllegalActionException: The fmu "xxx/ptII/ptolemy/actor/lib/fmi/test/auto/helloWorld.fmu" is not acceptable.
  in .top
Because:
The FMI version of this FMU is: 1.0 which is not supported.  QSS currently only supports FMI version 2.0.}}

######################################################################
####
#
test FMUQSS-1.2 {Test out importFMU on an FMU that has no state and should be rejected} {
    set err [tryToLoadNonQSSFMU {$CLASSPATH/ptolemy/actor/lib/fmi/test/auto/helloWorldME2.fmu}]
    list $err
} {{ptolemy.kernel.util.IllegalActionException: The fmu "xxx/ptII/ptolemy/actor/lib/fmi/test/auto/helloWorldME2.fmu" is not acceptable.
  in .top
Because:
The number of continuous states of this FMU is: 0.  The FMU does not have any state variables.  The FMU needs to have at least one state variable. Please check the FMU.}}
