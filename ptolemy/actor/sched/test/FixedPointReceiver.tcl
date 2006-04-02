# Tests for the FixedPointReceiver class
#
# @Author: Christopher Hylands, Based on SDFReceiver.tcl by Brian K. Vogel
#
# @Version: $Id$
#
# @Copyright (c) 1999-2006 The Regents of the University of California.
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

    # Unlike SDF, which would return false (0), fp should throw an error
    catch {$receiver hasToken 1} result1

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

test FixedPointReceiver-1.1 {Check put and hasToken} {
    set director [java::new ptolemy.actor.sched.FixedPointDirector]
    set r1 [java::new ptolemy.actor.sched.FixedPointReceiver $director]
    list [_testReceiver $r1]
} {{{ptolemy.kernel.util.InvalidStateException: hasToken(int) called on FixedPointReceiver with unknown status.} 1 0}}

######################################################################
####
#
test FixedPointReceiver-2.1 {Check put and get and hasToken} {
    set director [java::new ptolemy.actor.sched.FixedPointDirector]
    set receiver [java::new ptolemy.actor.sched.FixedPointReceiver $director]
    # Check that hasToken returns false when the receiver is empty
    # and returns true after a token has been put in the
    # receiver and returns false after the token has been
    # removed from the receiver.

    # token to put
    set token [java::new ptolemy.data.StringToken foo]

    # Unlike SDF, which will return false (0), fp will throw an exception
    catch {$receiver hasToken} result1
    catch {$receiver hasToken 1} result1_1

    list $result1 $result1_1
} {{ptolemy.kernel.util.InvalidStateException: hasToken() called on FixedPointReceiver with unknown status.} {ptolemy.kernel.util.InvalidStateException: hasToken(int) called on FixedPointReceiver with unknown status.}}

# NOTE: Continues from previous test.
test FixedPointReceiver-2.1.1 {Check put and get and hasToken} {

    # Now put a token.
    $receiver {put ptolemy.data.Token} $token

    # Should return true (1), since there is now a token in
    # the receiver.
    set result2 [$receiver hasToken]
    set result2_1 [$receiver hasToken 1]
    
    # Now get the token, but don't remove it from the receiver.
    set receivedToken [$receiver get]

    # Should return true (1), since there is still a token in
    # the receiver.
    set result3 [$receiver hasToken]
    set result3_1 [$receiver hasToken 1]

    # Now get the token, and remove it from the receiver.
    set receivedToken2 [$receiver get]

    # SDFReceiver returns false (0), since there is no longer a token in
    # the receiver.
    # However, FixedPointReceiver returns true (1) here.
    set result4 [$receiver hasToken]
    set result4_1 [$receiver hasToken 1]

    list $result2 $result2_1 \
	    $result3 $result3_1 $result4 $result4_1 \
	    [$receivedToken toString] [$receivedToken2 toString]
} {1 1 1 1 1 1 {"foo"} {"foo"}}

test FixedPointReceiver-2.2 {Check put and get and hasToken with more than 1 token in the queue} {

    set director [java::new ptolemy.actor.sched.FixedPointDirector]
    set receiver [java::new ptolemy.actor.sched.FixedPointReceiver $director]
    # Check that hasToken returns false when the receiver is empty
    # and returns true after a token has been put in the
    # receiver and returns false after the token has been
    # removed from the receiver.

    # tokens to put
    set token [java::new ptolemy.data.StringToken foo]
    set token2 [java::new ptolemy.data.StringToken bar]

    # Unlike SDF, which will return false (0), fp will throw an exception
    catch {$receiver hasToken 1} result1

    # Now put a token.
    $receiver {put ptolemy.data.Token} $token

    # Now put another token, which will fail
    catch {$receiver {put ptolemy.data.Token} $token2} result2
    list $result1 $result2
} {{ptolemy.kernel.util.InvalidStateException: hasToken(int) called on FixedPointReceiver with unknown status.} {ptolemy.kernel.util.IllegalActionException: Cannot put a token with a different value into a receiver with present status.}}


test FixedPointReceiver-2.3 {check hasToken in an unknown status} {

    set director [java::new ptolemy.actor.sched.FixedPointDirector]
    set receiver [java::new ptolemy.actor.sched.FixedPointReceiver $director]
    catch {$receiver hasToken} result1
    catch {$receiver hasToken 1} result2
    list $result1 $result2
} {{ptolemy.kernel.util.InvalidStateException: hasToken() called on FixedPointReceiver with unknown status.} {ptolemy.kernel.util.InvalidStateException: hasToken(int) called on FixedPointReceiver with unknown status.}}

test FixedPointReceiver-2.4 {check hasToken with a non-positive} {

    set director [java::new ptolemy.actor.sched.FixedPointDirector]
    set receiver [java::new ptolemy.actor.sched.FixedPointReceiver $director]
    # tokens to put
    set token [java::new ptolemy.data.StringToken foo]

    $receiver {put ptolemy.data.Token} $token
    set result1 [$receiver hasToken]
    set result1_1 [$receiver hasToken 1]

    catch {$receiver hasToken -1} result2
    catch {$receiver hasToken 0} result3
    set result4 [$receiver hasToken 1]
    # Should return false 
    set result5 [$receiver hasToken 2]

    list $result1 $result1_1 $result2 $result3 $result4 $result5
} {1 1 {java.lang.IllegalArgumentException: FixedPointReceiver: hasToken(int) requires a positive argument.} {java.lang.IllegalArgumentException: FixedPointReceiver: hasToken(int) requires a positive argument.} 1 0}

######################################################################
####
#
test FixedPointReceiver-3.1 {Try to put a null} {

    set director [java::new ptolemy.actor.sched.FixedPointDirector]
    set receiver [java::new ptolemy.actor.sched.FixedPointReceiver $director]
    catch {$receiver {put ptolemy.data.Token} [java::null]} result1
    list $result1
} {{java.lang.IllegalArgumentException: FixedPointReceiver.put(null) is invalid. To set the status to absent, use the clear() method.}}

######################################################################
####
#
test FixedPointReceiver-4.1 {put after clear} {

    set director [java::new ptolemy.actor.sched.FixedPointDirector]
    set receiver [java::new ptolemy.actor.sched.FixedPointReceiver $director]
    $receiver clear
    set token [java::new ptolemy.data.StringToken foo]
    catch {$receiver {put ptolemy.data.Token} $token} result1

    list $result1
} {{ptolemy.kernel.util.IllegalActionException: Cannot change from an absent status to a present status.  Call reset() first.}}

test FixedPointReceiver-4.2 {clear after put} {

    set director [java::new ptolemy.actor.sched.FixedPointDirector]
    set receiver [java::new ptolemy.actor.sched.FixedPointReceiver $director]
    set token [java::new ptolemy.data.StringToken foo]
    $receiver {put ptolemy.data.Token} $token
    catch {$receiver clear} result1
    list $result1
} {{ptolemy.kernel.util.IllegalActionException: Cannot change the status from present to absent.}}

test FixedPointReceiver-4.2.1 {clear, then get} {
    set director [java::new ptolemy.actor.sched.FixedPointDirector]
    set receiver [java::new ptolemy.actor.sched.FixedPointReceiver $director]
    $receiver reset
    catch {$receiver get} result1
    list $result1
} {{ptolemy.kernel.util.InvalidStateException: FixedPointReceiver: get() called on an FixedPointReceiver with status unknown.}}

######################################################################
####
#
test FixedPointReceiver-5.1 {Check hasRoom} {
    set director [java::new ptolemy.actor.sched.FixedPointDirector]
    set receiver [java::new ptolemy.actor.sched.FixedPointReceiver $director]

    # Should return true, since there should be room.
    set result1 [$receiver hasRoom]

    # Should return true, since there should be room.
    set result2 [$receiver hasRoom 1]

    # SDFReceiver returns true here, but FixedPointReceiver returns false
    # because the capacity of a FixedPointReceiver is 1
    set result3 [$receiver hasRoom 2]

    catch {$receiver hasRoom 0} result4
    
    list $result1 $result2 $result3 $result4
} {1 1 0 {java.lang.IllegalArgumentException: FixedPointReceiver: hasRoom() requires a positive argument.}}
