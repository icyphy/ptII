# Tests for the GiottoReceiver class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
test GiottoReceiver-1.1 {Construct a receiver} {
    set p1 [java::new ptolemy.actor.IOPort]
    set r1 [java::new ptolemy.domains.giotto.kernel.GiottoReceiver]
    set r2 [java::new ptolemy.domains.giotto.kernel.GiottoReceiver $p1]
    # Success is not throwing an exception.
    list {}
} {{}}

test GiottoReceiver-1.2 {Check initial state} {
    list [$r1 hasToken] [$r1 hasRoom]
} {0 1}

test GiottoReceiver-1.3 {Put a token} {
    set t1 [java::new ptolemy.data.Token]
    $r1 put $t1
    # Correct answer is false
    $r1 hasToken
} {0}

test GiottoReceiver-1.4 {Update the receiver} {
    $r1 update
    # Correct answer is true
    $r1 hasToken
} {1}

test GiottoReceiver-1.5 {Get the token} {
    # Result should be the same token that was put.
    $r1 get
} $t1

test GiottoReceiver-1.6 {Get the token from an empty receiver} {
    catch {$r2 get} msg
    list $msg
} {{ptolemy.actor.NoTokenException: .:
Attempt to get data from an empty receiver.}}

test GiottoReceiver-1.7 {Get the token from a receiver not updated} {
    $r2 put $t1
    catch {$r2 get} msg
    list $msg
} {{ptolemy.actor.NoTokenException: .:
Attempt to get data from an empty receiver.}}


