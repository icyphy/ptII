# Tests for the QueueReceiver class. Note that the non-abstract methods
# of Abstract receiver, such as the vectorized putArray and getArray
# are also tested here.
#
# @Author: Brian K. Vogel
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
test QueueReceiver-1.1 {Check put and hasToken} {
    set qreceiver1 [java::new ptolemy.actor.QueueReceiver]
    # Check that hasToken returns false when the receiver is empty
    # and returns true after a token has been put in the
    # receiver.

    # token to put
    set token [java::new ptolemy.data.StringToken foo]

    # Should return false (0), since no token has yet been put.
    set result1 [$qreceiver1 hasToken 1]

    # Now put a token.
    $qreceiver1 {put ptolemy.data.Token} $token

    # Should return true (1), since there is now a token in
    # the receiver.
    set result2 [$qreceiver1 hasToken 1]
    # Should return false (0), since there is only a single token
    # in the receiver.
    set result3 [$qreceiver1 hasToken 2]

    list $result1 $result2 $result3
} {0 1 0}

######################################################################
####
#
test QueueReceiver-2.1 {Check put and get and hasToken} {
    set qreceiver1 [java::new ptolemy.actor.QueueReceiver]
    # Check that hasToken returns false when the receiver is empty
    # and returns true after a token has been put in the
    # receiver and returns false after the token has been
    # removed from the receiver.

    # token to put
    set token [java::new ptolemy.data.StringToken foo]

    # Should return false (0), since no token has yet been put.
    set result1 [$qreceiver1 hasToken 1]

    # Now put a token.
    $qreceiver1 {put ptolemy.data.Token} $token

    # Should return true (1), since there is now a token in
    # the receiver.
    set result2 [$qreceiver1 hasToken 1]
    
    # Now get the token, but don't remove it from the receiver.
    set receivedToken [$qreceiver1 get 0]

    # Should return true (1), since there is still a token in
    # the receiver.
    set result3 [$qreceiver1 hasToken 1]

    # Now get the token, and remove it from the receiver.
    set receivedToken2 [$qreceiver1 get]

    # Should return false (0), since there is no longer a token in
    # the receiver.
    set result4 [$qreceiver1 hasToken 1]

    list $result1 $result2 $result3 $result4 [$receivedToken toString] [$receivedToken2 toString]
} {0 1 1 0 {"foo"} {"foo"}}

test QueueReceiver-2.2 {Check put and get and hasToken with more than 1 token in the queue} {
    set qreceiver1 [java::new ptolemy.actor.QueueReceiver]
    # Check that hasToken returns false when the receiver is empty
    # and returns true after a token has been put in the
    # receiver and returns false after the token has been
    # removed from the receiver.

    # tokens to put
    set token [java::new ptolemy.data.StringToken foo]
    set token2 [java::new ptolemy.data.StringToken bar]

    # Should return false (0), since no token has yet been put.
    set result1 [$qreceiver1 hasToken 1]

    # Now put a token.
    $qreceiver1 {put ptolemy.data.Token} $token

    # Now put another token.
    $qreceiver1 {put ptolemy.data.Token} $token2

    # Should return true (1), since there is now a token in
    # the receiver.
    set result2 [$qreceiver1 hasToken 1]
    
    # Now get the token, but don't remove it from the receiver.
    set receivedToken [$qreceiver1 get 0]

    # Now get the token, but don't remove it from the receiver.
    set receivedToken2 [$qreceiver1 get 1]

    # Should return true (1), since there is still a token in
    # the receiver.
    set result3 [$qreceiver1 hasToken 1]

    # Now get the token, and remove it from the receiver.
    set receivedToken3 [$qreceiver1 get]

    # Now get the token, and remove it from the receiver.
    set receivedToken4 [$qreceiver1 get]

    # Should return false (0), since there is no longer a token in
    # the receiver.
    set result4 [$qreceiver1 hasToken 1]

    list $result1 $result2 $result3 $result4 [$receivedToken toString] [$receivedToken2 toString] [$receivedToken3 toString] [$receivedToken4 toString]
} {0 1 1 0 {"foo"} {"bar"} {"foo"} {"bar"}}

######################################################################
####
#
test QueueReceiver-3.1 {Check putArray} {
    # Do a vectorized put of two tokens, then do two scalar
    # get()s.
    set qreceiver1 [java::new ptolemy.actor.QueueReceiver]

    # tokens to put
    set token [java::new ptolemy.data.StringToken foo]
    set token2 [java::new ptolemy.data.StringToken bar]

    set tokenArray [java::new {ptolemy.data.Token[]} {2}]
    $tokenArray set 0 $token
    $tokenArray set 1 $token2

    # Should return false (0), since no token has yet been put.
    set result1 [$qreceiver1 hasToken 1]

    # Now put the token array.
    $qreceiver1 {putArray ptolemy.data.Token[] int} $tokenArray 2

    # Should return true (1), since there are now 2 tokens in
    # the receiver.
    set result2 [$qreceiver1 hasToken 2]

    # Now do two scalar get()s.
    set receivedToken1 [$qreceiver1 get]
    set receivedToken2 [$qreceiver1 get]

    # Should return false (0), since there is no longer a token in
    # the receiver.
    set result3 [$qreceiver1 hasToken 1]

    list $result1 $result2 $result3 [$receivedToken1 toString] [$receivedToken2 toString]
} {0 1 0 {"foo"} {"bar"}}

######################################################################
####
#
test QueueReceiver-4.1 {Check getArray} {
    # Do two scalar put()s of two tokens, then do a vectorized
    # get by using getArray().
    set qreceiver1 [java::new ptolemy.actor.QueueReceiver]

    # tokens to put
    set token [java::new ptolemy.data.StringToken foo]
    set token2 [java::new ptolemy.data.StringToken bar]

    # Should return false (0), since no token has yet been put.
    set result1 [$qreceiver1 hasToken 1]

    # Scalar put.
    $qreceiver1 put $token

    # Another scalr put.
    $qreceiver1 put $token2

    # Should return true (1), since there are now 2 tokens in
    # the receiver.
    set result2 [$qreceiver1 hasToken 2]

    # Now do a vectorized get.
    set receivedTokenArray [$qreceiver1 getArray 2]

    # Should return false (0), since there is no longer a token in
    # the receiver.
    set result3 [$qreceiver1 hasToken 1]

    list $result1 $result2 $result3 [jdkPrintArray $receivedTokenArray]
} {0 1 0 {{"foo"} {"bar"}}}
