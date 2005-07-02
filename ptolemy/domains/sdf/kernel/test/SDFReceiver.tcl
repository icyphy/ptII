# Tests for the SDFReceiver class
#
# @Author: Brian K. Vogel
#
# @Version: $Id$
#
# @Copyright (c) 1999-2005 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# FIXME: Add constructor tests.

######################################################################
####
#
proc _testReceiver {receiver} {
   
    # Check that hasToken returns false when the receiver is empty
    # and returns true after a token has been put in the
    # receiver.

    # token to put
    set token [java::new ptolemy.data.StringToken foo]

    # Should return false (0), since no token has yet been put.
    set result1 [$receiver hasToken 1]

    # Now put a token.
    $receiver {put ptolemy.data.Token} $token

    # Should return true (1), since there is now a token in
    # the receiver.
    set result2 [$receiver hasToken 1]
    # Should return false (0), since there is only a single token
    # in the receiver.
    set result3 [$receiver hasToken 2]

    list $result1 $result2 $result3
} 

test SDFReceiver-1.1 {Check put and hasToken} {
    set r1 [java::new ptolemy.domains.sdf.kernel.SDFReceiver]
    set r2 [java::new {ptolemy.domains.sdf.kernel.SDFReceiver int} 2]
    set p3 [java::new ptolemy.actor.IOPort]
    set r3 [java::new {ptolemy.domains.sdf.kernel.SDFReceiver ptolemy.actor.IOPort} $p3]
    set p4 [java::new ptolemy.actor.IOPort]
    set r4 [java::new ptolemy.domains.sdf.kernel.SDFReceiver $p4 1]
    list [_testReceiver $r1] [_testReceiver $r2] [_testReceiver $r3] [_testReceiver $r4]
} {{0 1 0} {0 1 0} {0 1 0} {0 1 0}}

######################################################################
####
#
test SDFReceiver-2.1 {Check put and get and hasToken} {
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]
    # Check that hasToken returns false when the receiver is empty
    # and returns true after a token has been put in the
    # receiver and returns false after the token has been
    # removed from the receiver.

    # token to put
    set token [java::new ptolemy.data.StringToken foo]

    # Should return false (0), since no token has yet been put.
    set result1 [$receiver hasToken 1]

    # Now put a token.
    $receiver {put ptolemy.data.Token} $token

    # Should return true (1), since there is now a token in
    # the receiver.
    set result2 [$receiver hasToken 1]
    
    # Now get the token, but don't remove it from the receiver.
    set receivedToken [$receiver get 0]

    # Should return true (1), since there is still a token in
    # the receiver.
    set result3 [$receiver hasToken 1]

    # Now get the token, and remove it from the receiver.
    set receivedToken2 [$receiver get]

    # Should return false (0), since there is no longer a token in
    # the receiver.
    set result4 [$receiver hasToken 1]

    list $result1 $result2 $result3 $result4 [$receivedToken toString] [$receivedToken2 toString]
} {0 1 1 0 {"foo"} {"foo"}}

test SDFReceiver-2.1a {Check clear} {
    $receiver {put ptolemy.data.Token} $token
    $receiver {put ptolemy.data.Token} $token

    set result1 [$receiver size]
    
    $receiver clear
    set result2 [$receiver size]
    
    list $result1 $result2
} {2 0}

test SDFReceiver-2.2 {Check put and get and hasToken with more than 1 token in the queue} {
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]
    # Check that hasToken returns false when the receiver is empty
    # and returns true after a token has been put in the
    # receiver and returns false after the token has been
    # removed from the receiver.

    # tokens to put
    set token [java::new ptolemy.data.StringToken foo]
    set token2 [java::new ptolemy.data.StringToken bar]

    # Should return false (0), since no token has yet been put.
    set result1 [$receiver hasToken 1]

    # Now put a token.
    $receiver {put ptolemy.data.Token} $token

    # Now put another token.
    $receiver {put ptolemy.data.Token} $token2

    # Should return true (1), since there is now a token in
    # the receiver.
    set result2 [$receiver hasToken 1]
    
    # Now get the token, but don't remove it from the receiver.
    set receivedToken [$receiver get 0]

    # Now get the token, but don't remove it from the receiver.
    set receivedToken2 [$receiver get 1]
    
    # Throws an exception because the offset is out of range.
    catch {$receiver get 2} result6

    # Should return true (1), since there is still a token in
    # the receiver.
    set result3 [$receiver hasToken 1]

    # Now get the token, and remove it from the receiver.
    set receivedToken3 [$receiver get]

    # Now get the token, and remove it from the receiver.
    set receivedToken4 [$receiver get]

    # Should return false (0), since there is no longer a token in
    # the receiver.
    set result4 [$receiver hasToken 1]

    catch {$receiver hasToken 0} result5
    catch {$receiver hasToken -1} result7

    list $result1 $result2 $result3 $result4 [$receivedToken toString] [$receivedToken2 toString] [$receivedToken3 toString] [$receivedToken4 toString] $result5 $result6 $result7
} {0 1 1 0 {"foo"} {"bar"} {"foo"} {"bar"} 1 {ptolemy.actor.NoTokenException: Offset 2 out of range with 2 tokens in the receiver and 0 in history.} {java.lang.IllegalArgumentException: The argument must not be negative. It was: -1}}

test SDFReceiver-2.3 {Check noTokenException} {
    # uses previous setup.
    catch {$receiver get} result1
    catch {$receiver {getArray int} 2} result2
    list $result1 $result2
} {{ptolemy.actor.NoTokenException: Attempt to get token from an empty QueueReceiver.} {java.util.NoSuchElementException: The FIFOQueue does not contain enough elements!}}

######################################################################
####
#
test SDFReceiver-3.1 {Check putArray} {
    # Do a vectorized put of two tokens, then do two scalar
    # get()s.
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]

    # tokens to put
    set token [java::new ptolemy.data.StringToken foo]
    set token2 [java::new ptolemy.data.StringToken bar]

    set tokenArray [java::new {ptolemy.data.Token[]} {2}]
    $tokenArray set 0 $token
    $tokenArray set 1 $token2

    # Should return false (0), since no token has yet been put.
    set result1 [$receiver hasToken 1]

    # Now put the token array.
    $receiver {putArray ptolemy.data.Token[] int} $tokenArray 2

    # Should return true (1), since there are now 2 tokens in
    # the receiver.
    set result2 [$receiver hasToken 2]

    # Now do two scalar get()s.
    set receivedToken1 [$receiver get]
    set receivedToken2 [$receiver get]

    # Should return false (0), since there is no longer a token in
    # the receiver.
    set result3 [$receiver hasToken 1]

    list $result1 $result2 $result3 [$receivedToken1 toString] [$receivedToken2 toString]
} {0 1 0 {"foo"} {"bar"}}

######################################################################
####
#
test SDFReceiver-4.1 {Check getArray} {
    # Do two scalar put()s of two tokens, then do a vectorized
    # get by using getArray().
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]

    # tokens to put
    set token [java::new ptolemy.data.StringToken foo]
    set token2 [java::new ptolemy.data.StringToken bar]

    # Should return false (0), since no token has yet been put.
    set result1 [$receiver hasToken 1]

    # Scalar put.
    $receiver put $token

    # Another scalr put.
    $receiver put $token2

    # Should return true (1), since there are now 2 tokens in
    # the receiver.
    set result2 [$receiver hasToken 2]

    # Now do a vectorized get.
    set receivedTokenArray [$receiver {getArray int} 2]

    # Should return false (0), since there is no longer a token in
    # the receiver.
    set result3 [$receiver hasToken 1]

    list $result1 $result2 $result3 [jdkPrintArray $receivedTokenArray]
} {0 1 0 {{"foo"} {"bar"}}}

######################################################################
####
#
test SDFReceiver-5.1 {Check hasRoom} {
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]

    # Should return true, since there should be room.
    set result1 [$receiver hasRoom]

    # Should return true, since there should be room.
    set result2 [$receiver hasRoom 1]

    # Should return true, since there should be room.
    set result3 [$receiver hasRoom 2]

    catch {$receiver hasRoom 0} result4
    
    list $result1 $result2 $result3 $result4
} {1 1 1 {java.lang.IllegalArgumentException: The argument must not be negative. It was: 0}}

test SDFReceiver-5.2 {Check setCapacity} {
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]
    # Check that hasRoom returns true when the receiver is empty
    # and returns false after a token has been put in the
    # receiver.
    $receiver setCapacity 1

    # Should return true, since there should be room.
    set result1 [$receiver hasRoom]

    # Should return true, since there should be room.
    set result2 [$receiver hasRoom 1]

    # Should return false, since there should not be room.
    set result3 [$receiver hasRoom 2]

    # token to put
    set token [java::new ptolemy.data.StringToken foo]

    # Now put a token.
    $receiver {put ptolemy.data.Token} $token

    set size [$receiver size]

    # Should return false, since there should not be room.
    set result4 [$receiver hasRoom]

    # Should return false, since there should not be room.
    set result5 [$receiver hasRoom 1]

    # Should return false, since there should not be room.
    set result6 [$receiver hasRoom 2]

    list [$receiver getCapacity] $result1 $result2 $result3 $result4 $result5 $result6 $size
} {1 1 1 0 0 0 0 1}

test SDFReceiver-5.2 {Check noRoomException} {
    # Uses previous setup
    catch {$receiver {put ptolemy.data.Token} $token} result1
    catch {$receiver {putArray ptolemy.data.Token[] int} $tokenArray 2} result2
    list $result1 $result2
} {{ptolemy.actor.NoRoomException: Queue is at capacity of 1. Cannot put a token.} {ptolemy.actor.NoRoomException: Queue is at capacity. Cannot put a token.}}

test SDFReceiver-5.3 {Check setCapacity errors} {
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]

    # token to put
    set token [java::new ptolemy.data.StringToken foo]

    # Now put a token.
    $receiver {put ptolemy.data.Token} $token
    $receiver {put ptolemy.data.Token} $token

    # Fails because receiver contains 2 tokens.
    catch {$receiver setCapacity 1} result1
    catch {$receiver setCapacity -2} result2

    list $result1 $result2
} {{ptolemy.kernel.util.IllegalActionException: Failed to set capacity to 1
Because:
Queue contains more elements than the proposed capacity.} {ptolemy.kernel.util.IllegalActionException: Failed to set capacity to -2
Because:
Queue Capacity cannot be negative}}

test SDFReceiver-6.0 {Check the *History methods} {
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]

    set results1 [$receiver getHistoryCapacity]
    set historyElements [$receiver historyElements]
    set results2 [enumToStrings $historyElements]
    set results3 [$receiver historySize]
    list $results1 $results2 $results3
} {0 {} 0}

test SDFReceiver-6.1 {setHistoryCapacity} {
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]

    $receiver setHistoryCapacity 3
    set results1 [$receiver getHistoryCapacity]
    set historyElements [$receiver historyElements]
    set results2 [enumToStrings $historyElements]
    set results3 [$receiver historySize]
    list $results1 $results2 $results3
} {3 {} 0}

test SDFReceiver-6.3 {try to get historyElements} {
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]
    $receiver setHistoryCapacity 3

    # Check that hasRoom returns true when the receiver is empty
    # and returns false after a token has been put in the
    # receiver.
    $receiver setCapacity 1

    # Tokens to put
    set token [java::new ptolemy.data.StringToken foo]
    set token2 [java::new ptolemy.data.StringToken bar]
    set token3 [java::new ptolemy.data.StringToken bif]
    set token4 [java::new ptolemy.data.StringToken baz]

    # Now put a token.
    $receiver {put ptolemy.data.Token} $token

    # Get a token
    set receivedToken1 [$receiver get]

    set results1 [$receiver getHistoryCapacity]
    set historyElements [$receiver historyElements]
    set results2 [enumToStrings $historyElements]
    set results3 [$receiver historySize]

    # Put another token.
    $receiver {put ptolemy.data.Token} $token2

    # Get another token
    set receivedToken2 [$receiver get]

    set results4 [$receiver getHistoryCapacity]
    set historyElements [$receiver historyElements]
    set results5 [enumToStrings $historyElements]
    set results6 [$receiver historySize]

    # Put another token.
    $receiver {put ptolemy.data.Token} $token3

    # Get another token
    set receivedToken3 [$receiver get]

    # Put another token.
    $receiver {put ptolemy.data.Token} $token4

    # Get another token
    set receivedToken4 [$receiver get]

    # The history capacity is 3, but we've seen 4 tokens
    set results7 [$receiver getHistoryCapacity]
    set historyElements [$receiver historyElements]
    set results8 [enumToStrings $historyElements]
    set results9 [$receiver historySize]

    list \
	    [list $results1 $results2 $results3] \
	    [list $results4 $results5 $results6] \
	    [list $results7 $results8 $results9] \
} {{3 {{"foo"}} 1} {3 {{"foo"} {"bar"}} 2} {3 {{"bar"} {"bif"} {"baz"}} 3}}
