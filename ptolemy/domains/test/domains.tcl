# Tests models that combine domains
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2004 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.htm for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


#############################################
####
#
test PNDirector-1.1 {} {
     catch {createAndExecute "auto/knownFailedTests/PNSDFtest1.xml"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Queue size exceeds the maximum capacity in port .PNSDFtest1.Topologia SDF.port. Perhaps you have an unbounded queue?
  in .PNSDFtest1.PN Director}}

#############################################
####
#
test PNDirector-2.1 {} {
     catch {createAndExecute "auto/knownFailedTests/PNSRTimedtest.xml"} errMsg
    list $errMsg
} {}

#############################################
####
#
test PNDirector-3.1 {} {
     catch {createAndExecute "auto/knownFailedTests/PNSRtest3.xml"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Queue size exceeds the maximum capacity in port .PNSRtest3.AddSubtract.plus. Perhaps you have an unbounded queue?
  in .PNSRtest3.PN Director}}

#############################################
####
#
test PNDirector-4.1 {} {
     catch {createAndExecute "auto/knownFailedTests/SDFSRtest2.xml"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Actor is not ready to fire.
  in .SDFSRtest2.SDF Director and .SDFSRtest2.SampleDelay}}

#############################################
####
#
test PNDirector-5.1 {} {
     catch {createAndExecute "auto/knownFailedTests/SRSDFtest1.xml"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Width of input is 0 but NonStrictTest only supports a width of 1.
  in .SRSDFtest1.NonStrictTest2}}

#############################################
####
#
test PNDirector-6.1 {} {
     catch {createAndExecute "auto/knownFailedTests/SRSDFtest2.xml"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: The fire() method of this actor was never called. Usually, this is an error indicating that starvation is occurring.
  in .SRSDFtest2.NonStrictTest}}