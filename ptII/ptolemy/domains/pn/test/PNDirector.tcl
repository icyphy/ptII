# Tests for the PNDirector class that requires actors from pn/lib
#
# @Author: Mudit Goel
#
# @Version: $Id$
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

test PNDirector-6.1 {Test an application} {

    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set manager [java::new ptolemy.actor.Manager]
    set director [java::new ptolemy.domains.pn.kernel.PNDirector]
    $e0 setDirector $director
    $e0 setName top
    $e0 setManager $manager

    # FIXME: why doesn't the getParameter option work.
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$ramp getAttribute  firingCountLimit]]
    $param setExpression 20

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    #error "Running this test sometimes causes an endless loop" 
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19}

#############################################
####
#
test PNDirector-2.0 {Try a PN inside DE model} {
    # PN Inside DE fails
    # See ptolemy hackers log for 12/15/2004 and 12/16/2004"
    catch {createAndExecute "PNInsideDE.xml"} errMsg

    # Truncate the string to handle differences between IBM and Sun JVMs
    list [string range $errMsg 0 204]
} {{ptolemy.kernel.util.IllegalActionException: At the current time, process-oriented domains (PN and CSP) cannot be nested inside firing-based domains (SDF, DE, CT, etc.).
  in .PNInsideDE.CompositeActor.port}}

