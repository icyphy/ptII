# Tests for AbstractReceiver.  See also QueueReceiver.tcl

# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005-2007 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test AbstractReceiver-1.1 {Check clear and getCurrentTime} {
    set w [java::new ptolemy.kernel.util.Workspace w]
    set toplevel [java::new ptolemy.actor.CompositeActor $w]
    #set manager [java::new ptolemy.actor.Manager $w myManager]

    set actor [java::new ptolemy.actor.TypeOpaqueCompositeActor]
    set director [java::new ptolemy.actor.Director $actor myDirector]

    set port [java::new ptolemy.actor.TypedIOPort $actor p1]
    set receiver [java::new ptolemy.actor.test.TestAbstractReceiver $port]
    catch {$receiver clear} msg
    list $msg [$receiver getCurrentTime] "\n" [$receiver toString]
} {{ptolemy.kernel.util.IllegalActionException: Receiver class ptolemy.actor.test.TestAbstractReceiver does not support clear().
  in .<Unnamed Object>.p1} -Infinity {
} {ptolemy.actor.test.TestAbstractReceiver {..p1.receiver }}}
