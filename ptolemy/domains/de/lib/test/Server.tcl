# Tests for the Server class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1999-2003 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test Server-1.1 {test constructor and clone} {
    set e0 [deModel 3.0]
    set serverbase [java::new ptolemy.domains.de.lib.Server $e0 server]
    set server [java::cast ptolemy.domains.de.lib.Server \
		    [$serverbase clone [$e0 workspace]]]
    $serverbase {setContainer ptolemy.kernel.CompositeEntity} [java::null]
    $server {setContainer ptolemy.kernel.CompositeEntity} $e0
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test Server in a DE model
#
test Server-2.1 {test with the default service time value} {
    set clock [java::new ptolemy.actor.lib.Clock $e0 clock]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $clock] output] \
       [java::field [java::cast ptolemy.domains.de.lib.DETransformer $server] \
       input]
    $e0 connect \
       [java::field \
       [java::cast ptolemy.domains.de.lib.DETransformer $server] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToObjects [$rec getTimeRecord]
} {1.0 2.0 3.0}

test Server-3.1 {test with zero service time} {
    set serviceTime [java::field $server serviceTime]
    $serviceTime setExpression "0.0"
    [$e0 getManager] execute
    enumToObjects [$rec getTimeRecord]
} {0.0 1.0 2.0 3.0}

test Server-3.2 {test with negative service time} {
    $serviceTime setExpression "-1.0"
    catch {[$e0 getManager] execute} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Error evaluating expression: -1.0
  in .top.server.serviceTime
Because:
Cannot have negative service time: -1.0
  in .top.server}}

test Server-4.0 {Test with service time input} {
    set clock2 [java::new ptolemy.actor.lib.Clock $e0 clock2]
    set period [java::field $clock2 period]
    # set period larger than execution time so we get only one cycle
    $period setExpression {4.0}
    # Have to reset service time to a legit value because it will be
    # examined in preinitialize.
    $serviceTime setExpression "1.0"
    set values [java::field $clock2 values]
    $values setExpression {{1.5, 0.5}}
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $clock2] output] \
       [java::field $server newServiceTime]
    [$e0 getManager] execute
    enumToObjects [$rec getTimeRecord]
} {1.5 2.0 2.5}
