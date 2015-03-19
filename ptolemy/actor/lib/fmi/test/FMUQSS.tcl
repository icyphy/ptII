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

# Load a FMU into a sdf model as a model exchange FMU and return the model
proc importFMU {fmu} {
    set e1 [sdfModel 5]
    set fmuFile [java::call ptolemy.util.FileUtilities nameToFile $fmu [java::null]]
    set fmuFileParameter [java::new ptolemy.data.expr.FileParameter $e1 fmuFileParameter]
    $fmuFileParameter setExpression [$fmuFile getCanonicalPath]
    # Last argument is true if we should import as a model exchange FMU
    java::call ptolemy.actor.lib.fmi.FMUQSS importFMU $e1 $fmuFileParameter $e1 100.0 100.0 true
    return $e1
}

# Load a FMU as QSS into a sdf model and return the model
proc importQSSFMU {fmu} {
    set e1 [sdfModel 5]
    set fmuFile [java::call ptolemy.util.FileUtilities nameToFile $fmu [java::null]]
    set fmuFileParameter [java::new ptolemy.data.expr.FileParameter $e1 fmuFileParameter]
    $fmuFileParameter setExpression [$fmuFile getCanonicalPath]
    java::call ptolemy.actor.lib.fmi.FMUQSS importFMU $e1 $fmuFileParameter $e1 100.0 100.0
    return $e1
}

# Try to load a non-qss FMU and return the error in a format
# that will be the same on all platforms
proc tryToLoadNonQSSFMU {fmu} {
    set e1 [sdfModel 5]
    set fmuFile [java::call ptolemy.util.FileUtilities nameToFile $fmu [java::null]]
    set fmuFileParameter [java::new ptolemy.data.expr.FileParameter $e1 fmuFileParameter]
    $fmuFileParameter setExpression [$fmuFile getCanonicalPath]
    catch {java::call ptolemy.actor.lib.fmi.FMUQSS importFMU $e1 $fmuFileParameter $e1 100.0 100.0} err
    regsub {The fmu ".*ptII/ptolemy} $err {The fmu "xxx/ptII/ptolemy} err2

    # Windows... why?
    regsub {The fmu ".*ptII\\ptolemy} $err2 {The fmu "xxx/ptII/ptolemy} err3
    regsub {\\} $err3 {/} err4

    return $err4
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


######################################################################
####
#
test FMUQSS-1.3 {Test out importFMU on an Co-Simulation FMU that should be rejected} {
    set err [tryToLoadNonQSSFMU {$CLASSPATH/ptolemy/actor/lib/fmi/test/auto/bouncingBall20.fmu}]
    list $err
} {{ptolemy.kernel.util.IllegalActionException: The fmu "xxx/ptII/ptolemy/actor/lib/fmi/test/auto/bouncingBall20.fmu" is not acceptable.
  in .top
Because:
There is no ModelExchange attribute in the model description file of This FMU to indicate whether it is for model exchange or not.  QSS currently only supports FMU for model exchange.}}


######################################################################
####
#

# Two tests, one that imports as a QSS FMU, one that imports as a regular, non-QSS ME FMU.

test FMUQSS-2.1.1 {Test out importFMU on a FMI-2.0 Model Exchange FMU as a QSS and be sure that the state variables are output ports} {
    set bouncingBallME20Model [importQSSFMU {$CLASSPATH/ptolemy/actor/lib/fmi/test/auto/bouncingBallME20.fmu}]
    set bouncingBallME [$bouncingBallME20Model getEntity bouncingBallME]
    set v [java::cast ptolemy.actor.IOPort [$bouncingBallME getPort {v}]]
    set h [java::cast ptolemy.actor.IOPort [$bouncingBallME getPort {h}]]

    list [$v isOutput] [$h isOutput] [enumToNames [$bouncingBallME getPorts]]
} {1 1 {h v}}

test FMUQSS-2.1.2 {Test out importFMU on a FMI-2.0 Model Exchange FMU as a non-QSS and be sure that the state variables are input ports} {
    set bouncingBallME20Model2_1_2 [importFMU {$CLASSPATH/ptolemy/actor/lib/fmi/test/auto/bouncingBallME20.fmu}]
    set bouncingBallME2_1_2 [$bouncingBallME20Model2_1_2 getEntity bouncingBallME]
    set v [java::cast ptolemy.actor.IOPort [$bouncingBallME2_1_2 getPort {v}]]
    set h [java::cast ptolemy.actor.IOPort [$bouncingBallME2_1_2 getPort {h}]]
    #set numberOfPorts [[$bouncingBallME2_1_2 portList] size]
    #puts [$bouncingBallME2_1_2 exportMoML]

    #list [$v getName] [$h getName] $numberOfPorts
    list [$v isInput] [$h isInput] [enumToNames [$bouncingBallME2_1_2 getPorts]]
} {1 1 {h der_h_ v der_v_}}

######################################################################
####
#
test FMUQSS-2.2 {Test out importFMU on a FMI-2.0 Model Exchange FMU as a QSS and be sure that the state variables are not ports but an output is} {
    # From qss_0first_0order.fmu, but with the binary directory
    # removed because we are just testing the modelDescription.xml
    # file.
    set model [importQSSFMU {$CLASSPATH/ptolemy/actor/lib/fmi/test/auto/qss1State1OutputNoBinaries.fmu}]
    # qss_0first_0order is the ModelExchange modelIdentifier in modelDescription.xml.
    # However, the name of the actor is taken from the name of the .fmu file.
    set qss [$model getEntity qss1State1OutputNoBinaries]
    set u [java::cast ptolemy.actor.IOPort [$qss getPort {u}]]
    set x [java::cast ptolemy.actor.IOPort [$qss getPort {x}]]
    set y [java::cast ptolemy.actor.IOPort [$qss getPort {y}]]

    set numberOfPorts [[$qss portList] size]
    # The state variable x *should not* be included as a port when we import a FMU for QSS.
    # We could list all the ports and parameters here, but we would get failures as we add functionality
    list [$u isInput] [$x isOutput] [$y isOutput] $numberOfPorts
} {1 1 1 3}
