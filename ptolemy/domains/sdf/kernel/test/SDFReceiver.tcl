# Tests for the SDFReceiver class
#
# @Author: Brian K. Vogel
#
# @Version: : NamedObj.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 1999-2000 The Regents of the University of California.
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
test SDFReceiver-1.1 {Check put and hasToken} {
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]
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
} {0 1 0}

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

    list $result1 $result2 $result3 $result4 [$receivedToken toString] [$receivedToken2 toString] [$receivedToken3 toString] [$receivedToken4 toString]
} {0 1 1 0 {"foo"} {"bar"} {"foo"} {"bar"}}

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

    list $result1 $result2 $result3
} {1 1 1}

######################################################################
####
#
test SDFReceiver-5.1 {Check setCapacity} {
    set receiver [java::new ptolemy.domains.sdf.kernel.SDFReceiver]
    # Check that hasToken returns false when the receiver is empty
    # and returns true after a token has been put in the
    # receiver.

    # Should return true, since there should be room.
    set result1 [$receiver hasRoom]

    # Should return true, since there should be room.
    set result2 [$receiver hasRoom 1]

    # Should return true, since there should be room.
    set result3 [$receiver hasRoom 2]

    $receiver setCapacity 1

    # token to put
    set token [java::new ptolemy.data.StringToken foo]

    # Now put a token.
    $receiver {put ptolemy.data.Token} $token

    # Should return false, since there should not be room.
    set result4 [$receiver hasRoom]

    # Should return false, since there should not be room.
    set result5 [$receiver hasRoom 1]

    # Should return false, since there should not be room.
    set result6 [$receiver hasRoom 2]

    list $result1 $result2 $result3 $result4 $result5 $result6
} {1 1 1 0 0 0}