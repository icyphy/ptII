# Tests for the Event class
#
# @Author: John S. Davis II
#
# @Version: @(#)Event.tcl	1.1	11/15/98
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#
test Event-2.1 {Check for correct event time} {
    set t [java::new ptolemy.data.Token]
    set e1 [java::new ptolemy.domains.od.kernel.Event $t 5.0]
    list [$e1 getTime] 
} {5.0}

######################################################################
####
#
test Event-2.2 {Check for correct event token} {
    set t1 [java::new ptolemy.data.Token]
    set e1 [java::new ptolemy.domains.od.kernel.Event $t1 5.0]
    set t2 [$e1 getToken]
    list [expr { $t1 == [$e1 getToken] } ]
} {1}

######################################################################
####
#
test Event-2.3 {Check for correct event time, token and receiver} {
    set t1 [java::new ptolemy.data.Token]
    set r1 [java::new ptolemy.domains.od.kernel.TimedQueueReceiver]
    set e1 [java::new ptolemy.domains.od.kernel.Event $t1 7.5 $r1]
    list [expr { $t1 == [$e1 getToken] } ] [$e1 getTime] [expr { $r1 == [$e1 getReceiver] } ]
} {1 7.5 1}
