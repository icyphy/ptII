# Tests for the Director class
#
# @Author: Mudit Goel
#
# @Version: $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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
#   The following commands are to get a reference to the director in the put
#   and get methods for testing.


set toplevel [java::new ptolemy.actor.CompositeActor]
set dir [java::new ptolemy.domains.pn.kernel.BasePNDirector]
$toplevel setDirector $dir
set sink [java::new ptolemy.domains.pn.kernel.test.TestSink $toplevel "sink"]
set port [$sink getPort "input"]

######################################################################
####
#
test PNQueueReceiver-2.1 {Check for null container in new receiver} {
    set rec1 [java::new ptolemy.domains.pn.kernel.PNQueueReceiver]
    list [expr {[$rec1 getContainer] == [java::null] } ]
} {1}

######################################################################
####
#
test PNQueueReceiver-2.2 {Check for correct IOPort container in new receiver} {
    set rec [java::new ptolemy.domains.pn.kernel.PNQueueReceiver $port]
    list [ $port equals [$rec getContainer]]
} {1}



######################################################################
####
#
test PNQueueReceiver-3.1 {Check hasToken} {
    list [expr {[$rec hasToken] == 1} ]
} {1}

######################################################################
####
#
test PNQueueReceiver-3.2 {Check hasRoom} {
    list [expr {[$rec hasToken] == 1} ]
} {1}

######################################################################
####
#
test PNQueueReceiver-3.3 {Test the setting of the blocking flags} {
    $rec setReadPending true
    list [$rec isReadPending] [$rec isWritePending]
} {1 0}

######################################################################
####
#
test PNQueueReceiver-3.4 {Test the setting of the blocking flags} {
    $rec setWritePending true
    list [$rec isReadPending] [$rec isWritePending]
} {1 1}




######################################################################
####
#

test PNQueueReceiver-4.1 {Put and get token when only one token} {
    $rec setCapacity 1
    $rec put [java::new {ptolemy.data.IntToken int} 2]
    set tok [java::cast ptolemy.data.IntToken [$rec get]]
    list [$tok intValue ]
} {2}



######################################################################
####
#
test PNQueueReceiver-4.2 {Put and get tokens when more than one token} {
    $rec setCapacity 3
    $rec put [java::new {ptolemy.data.IntToken int} 4]
    $rec put [java::new {ptolemy.data.IntToken int} 5]
    $rec put [java::new {ptolemy.data.IntToken int} 6]
    set tok1 [java::cast ptolemy.data.IntToken [$rec get]]
    set tok2 [java::cast ptolemy.data.IntToken [$rec get]]
    set tok3 [java::cast ptolemy.data.IntToken [$rec get]]
    list [$tok1 intValue] [$tok2 intValue] [$tok3 intValue]
} {4 5 6}

######################################################################
####
#FIXME: How do you check for setFinish and setPause?
test PNQueueReceiver-4.3 {Test for initialize} {
    $rec setCapacity 3
    $rec put [java::new {ptolemy.data.IntToken int} 4]
    $rec put [java::new {ptolemy.data.IntToken int} 5]
    $rec put [java::new {ptolemy.data.IntToken int} 6]
    $rec setReadPending true
    $rec setWritePending true
    $rec requestPause true
    $rec requestFinish
    $rec initialize
    set elem [$rec elements]
    set ans1 [$elem hasMoreElements]
    set ans2 [$rec isReadPending]
    set ans3 [$rec isWritePending]
    list $ans1 $ans2 $ans3
} {0 0 0}

######################################################################
####
#
test PNQueueReceiver-4.4 {Test for initialize} {
    $rec setCapacity 3
    $rec put [java::new {ptolemy.data.IntToken int} 4]
    $rec put [java::new {ptolemy.data.IntToken int} 5]
    $rec put [java::new {ptolemy.data.IntToken int} 6]
    $rec clear
    set elem [$rec elements]
    set ans1 [$elem hasMoreElements]
    list $ans1
} {0}

