# Tests for the SRReceiver class
#
# @Author: Christopher Hylands, Based on SDFReceiver by Brian K. Vogel
#
# @Version: : NamedObj.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 1999-2002 The Regents of the University of California.
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

test SRReceiver-1.1 {Check put and hasToken} {
    set director [java::new ptolemy.domains.sr.kernel.SRDirector]
    set r1 [java::new ptolemy.domains.sr.kernel.SRReceiver $director]
    list [_testReceiver $r1]
} {{0 1 0}}

######################################################################
####
#
test SRReceiver-2.1 {Check put and get and hasToken} {
    set director [java::new ptolemy.domains.sr.kernel.SRDirector]
    set receiver [java::new ptolemy.domains.sr.kernel.SRReceiver $director]
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
    set receivedToken [$receiver get]

    # Should return true (1), since there is still a token in
    # the receiver.
    set result3 [$receiver hasToken 1]

    # Now get the token, and remove it from the receiver.
    set receivedToken2 [$receiver get]

    # SDFReceiver returns false (0), since there is no longer a token in
    # the receiver.
    # However, SRReceiver returns true (1) here.
    set result4 [$receiver hasToken 1]

    list $result1 $result2 $result3 $result4 [$receivedToken toString] [$receivedToken2 toString]
} {0 1 1 1 {"foo"} {"foo"}}


test SRReceiver-2.2 {Check put and get and hasToken with more than 1 token in the queue} {

    set director [java::new ptolemy.domains.sr.kernel.SRDirector]
    set receiver [java::new ptolemy.domains.sr.kernel.SRReceiver $director]
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

    # Now put another token, which will fail
    catch {$receiver {put ptolemy.data.Token} $token2} result1
    list $result1
} {{ptolemy.domains.sr.kernel.IllegalOutputException: SRReceiver cannot receive two tokens that differ.}}


test SRReceiver-2.3 {check hasToken in an unknown state} {

    set director [java::new ptolemy.domains.sr.kernel.SRDirector]
    set receiver [java::new ptolemy.domains.sr.kernel.SRReceiver $director]
    catch {$receiver hasToken} result1
    list $result1
} {{ptolemy.domains.sr.kernel.UnknownTokenException: hasToken() called on SRReceiver with unknown state.}}


######################################################################
####
#
test SRReceiver-3.1 {Try to put a null} {

    set director [java::new ptolemy.domains.sr.kernel.SRDirector]
    set receiver [java::new ptolemy.domains.sr.kernel.SRReceiver $director]
    catch {$receiver {put ptolemy.data.Token} [java::null]} result1
    list $result1
} {{ptolemy.kernel.util.InternalErrorException: SRReceiver.put(null) is invalid.}}

######################################################################
####
#
test SRReceiver-4.1 {put after setAbsent} {

    set director [java::new ptolemy.domains.sr.kernel.SRDirector]
    set receiver [java::new ptolemy.domains.sr.kernel.SRReceiver $director]
    $receiver setAbsent
    set token [java::new ptolemy.data.StringToken foo]
    catch {$receiver {put ptolemy.data.Token} $token} result1

    list $result1
} {{ptolemy.domains.sr.kernel.IllegalOutputException: SRReceiver cannot transition from an absent state to a present state.}}

test SRReceiver-4.2 {setAbsent after put} {

    set director [java::new ptolemy.domains.sr.kernel.SRDirector]
    set receiver [java::new ptolemy.domains.sr.kernel.SRReceiver $director]
    set token [java::new ptolemy.data.StringToken foo]
    $receiver {put ptolemy.data.Token} $token
    catch {$receiver setAbsent} result1
    list $result1
} {{ptolemy.domains.sr.kernel.IllegalOutputException: SRReceiver cannot transition from a present state to an absent state.}}


test SRReceiver-4.2 {setAbsent, then get} {

    set director [java::new ptolemy.domains.sr.kernel.SRDirector]
    set receiver [java::new ptolemy.domains.sr.kernel.SRReceiver $director]
    $receiver setAbsent
    catch {$receiver get} result1
    list $result1
} {{ptolemy.actor.NoTokenException: Attempt to get data from an empty mailbox.}}

######################################################################
####
#
test SRReceiver-5.1 {Check hasRoom} {
    set director [java::new ptolemy.domains.sr.kernel.SRDirector]
    set receiver [java::new ptolemy.domains.sr.kernel.SRReceiver $director]

    # Should return true, since there should be room.
    set result1 [$receiver hasRoom]

    # Should return true, since there should be room.
    set result2 [$receiver hasRoom 1]

    # SDFReceiver returns true here, but SRReceiver returns false
    # because the capacity of a SRReceiver is 1
    set result3 [$receiver hasRoom 2]

    catch {$receiver hasRoom 0} result4
    
    list $result1 $result2 $result3 $result4
} {1 1 0 {java.lang.IllegalArgumentException: hasRoom() requires a positive argument.}}
