# Tests models that combine domains
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2004-2005 The Regents of the University of California.
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

if {[info procs jdkCaptureErr] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}


# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


#############################################
####
#
test domains.1.1 {} {
    catch {createAndExecute "auto/knownFailedTests/PNSDFtest1.xml"} errMsg
    # Sometimes it is port, sometimes port2
    regsub -all {port[^.]*.} $errMsg {portXXX.} r1
    list $r1
} {{ptolemy.actor.NoRoomException: Queue is at capacity of 1. Cannot put a token.
  in .PNSDFtest1.Topologia SDF.AddSubtract.plus}}

#############################################
####
#
test domains-2.1 {} {
    jdkCaptureErr {
	catch {createAndExecute "auto/knownFailedTests/PNSRTimedtest.xml"} errMsg
    } out
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Queue size 131072 exceeds the maximum capacity in port .PNSRTimedtest.Topologia SR.port. Perhaps you have an unbounded queue?
  in .PNSRTimedtest.Topologia SR.port}}

#############################################
####
#
test domains-3.1 {} {
     catch {createAndExecute "auto/knownFailedTests/PNSRtest3.xml"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Queue size 131072 exceeds the maximum capacity in port .PNSRtest3.AddSubtract.plus. Perhaps you have an unbounded queue?
  in .PNSRtest3.AddSubtract.plus}}

#############################################
####
#
test domains-4.1 {} {
     catch {createAndExecute "auto/knownFailedTests/SDFSRtest2.xml"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Actor is not ready to fire.  Perhaps SampleDelay.prefire() returned false? Try debugging the actor by selecting "Listen to Actor".  Also, for SDF check moml for tokenConsumptionRate on input.
  in .SDFSRtest2.SDF Director and .SDFSRtest2.SampleDelay}}


#############################################
####
#
test domains-5.1 {} {
     catch {createAndExecute "auto/knownFailedTests/SRSDFtest1.xml"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Width of input is 0 but NonStrictTest only supports a width of 1.
  in .SRSDFtest1.NonStrictTest2}}

#############################################
####
#
test domains-6.1 {} {
     catch {createAndExecute "auto/knownFailedTests/SRSDFtest2.xml"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: The test produced only 0 tokens, yet the correctValues parameter was expecting 1 tokens.
  in .SRSDFtest2.NonStrictTest}}

