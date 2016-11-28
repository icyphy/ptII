# Test running WebSocketClient.xml and WebSocketClient2.xml
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2015-2016 The Regents of the University of California.
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

######################################################################
####
#
# test WebSocketClientTest-1.1 {Run WebSocketClient.xml and then WebSocketClient2.xml} {
#     java::new ptolemy.moml.MoMLSimpleApplication $PTII/org/terraswarm/accessor/test/auto/WebSocketClient.xml
#     java::new ptolemy.moml.MoMLSimpleApplication $PTII/org/terraswarm/accessor/test/auto/WebSocketClient.xml
#     java::new ptolemy.moml.MoMLSimpleApplication $PTII/org/terraswarm/accessor/test/auto/WebSocketClient2.xml
#     java::new ptolemy.moml.MoMLSimpleApplication $PTII/org/terraswarm/accessor/test/auto/WebSocketClient2.xml
#     # Success is not throwing an exception.
#     list true
# } {true}

#test WebSocketClientTest-2.1 {Run WebSocketClient.xml and then WebSocketClient2.xml with rerun} {
#    set application [java::new ptolemy.moml.MoMLSimpleApplication $PTII/org/terraswarm/accessor/test/auto/WebSocketClient.xml]
#    $application rerun
#    set application2 [java::new ptolemy.moml.MoMLSimpleApplication $PTII/org/terraswarm/accessor/test/auto/WeSocketClient2.xml]
#    $application2 rerun
#    # Success is not throwing an exception.
#    list true
#} {true}


test WebSocketClientTest-3.1 {Run WebSocketClientJS.xml and then WebSocketClient2JS.xml with reloading of the accessor} {
    set application [java::new ptolemy.moml.MoMLSimpleApplication $PTII/org/terraswarm/accessor/test/auto/WebSocketClientJS.xml]
    $application rerun
    set toplevel [$application toplevel]
    java::call org.terraswarm.accessor.JSAccessor reloadAllAccessors $toplevel
    $application rerun

    set $application [java::null]
    java::call System gc

    set application2 [java::new ptolemy.moml.MoMLSimpleApplication $PTII/org/terraswarm/accessor/test/auto/WebSocketClient2JS.xml]
    $application2 rerun
    set toplevel2 [$application2 toplevel]
    java::call org.terraswarm.accessor.JSAccessor reloadAllAccessors $toplevel2
    $application2 rerun

    set $application2 [java::null]
    java::call System gc

    # Success is not throwing an exception.
    list true
} {true}

