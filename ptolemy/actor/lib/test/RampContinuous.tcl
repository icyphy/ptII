# Test Ramp.
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2017 The Regents of the University of California.
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

# RampContinuous.tcl is a separate file so that we can exclude
# if from distributions like Cape Code that do not include the Continuous actor.

######################################################################
####
#
test Ramp-3.1 {Run a Continous model which will detect errors in scheduling} {
    # ct used to fail with "ptolemy.actor.sched.NotSchedulableException: ramp is a SequenceActor, which cannot be a source actor in the CT domain.   in .top.ramp", but continuous passes

    set e0 [continuousModel 5]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    catch {[$e0 getManager] execute} msg
    list $msg
} {{}}
