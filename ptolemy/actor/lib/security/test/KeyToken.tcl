# Tests for the KeyToken class
#
# @Author: Christopher Hylands Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2004 The Regents of the University of California.
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

######################################################################
####
# 
test KeyToken-1.1 {Create DES token with keysize of 56} {
    set keyGenerator [java::call javax.crypto.KeyGenerator getInstance "DES"]
    set secureRandom [java::new java.security.SecureRandom]
    $keyGenerator {init int java.security.SecureRandom} 56 $secureRandom
    set secretKey [$keyGenerator generateKey]
    set secretKeyToken \
	    [java::new ptolemy.actor.lib.security.KeyToken $secretKey]
    # The encoded portion may change each time we run
    regexp {algorithm = DES, format = RAW, encoded = } [$secretKeyToken toString]
} 1

######################################################################
####
# 
test KeyToken-2.1 {getType} {
    # uses 1.1 above
    [$secretKeyToken getType] toString
} {Key}


######################################################################
####
# 
test KeyToken-3.1 {getValue} {
    # uses 1.1 above
    set value [$secretKeyToken getValue]
    list \
	    [$value getAlgorithm] [$secretKey getAlgorithm] \
	    [$value getFormat] [$secretKey getFormat] \
	    [expr {[[$value getEncoded] getrange] == [[$secretKey getEncoded] getrange]}]
} {DES DES RAW RAW 1}


######################################################################
####
# 
test KeyToken-4.1 {equals} {
    # uses 1.1 above
    set value [$secretKeyToken getValue]
    set secretKeyToken2 \
	    [java::new ptolemy.actor.lib.security.KeyToken $value]

    set  boolean [$secretKeyToken isEqualTo $secretKeyToken2]
    $boolean toString
} {true}


######################################################################
####
# 
test KeyToken-4.2 {equals(): Two Keys are not likely to have the same encoding} {
    # uses 1.1 above (secretKeyToken)

    set keyGenerator3 [java::call javax.crypto.KeyGenerator getInstance "DES"]
    set secureRandom3 [java::new java.security.SecureRandom]
    $keyGenerator3 {init int java.security.SecureRandom} 56 $secureRandom3

    set secretKey3 [$keyGenerator3 generateKey]
    set secretKeyToken3 \
	    [java::new ptolemy.actor.lib.security.KeyToken $secretKey3]

    set  boolean [$secretKeyToken isEqualTo $secretKeyToken3]
    $boolean toString
} {false}

######################################################################
####
# 
test KeyToken-4.3 {equals(): Different Algorithms} {
    # uses 1.1 above

    source $PTII/util/testsuite/enums.tcl
    set s [java::call java.security.Security getAlgorithms "Cipher"]
    puts "Available Ciphers: [listToStrings $s]"

    set keyGenerator4 [java::call javax.crypto.KeyGenerator getInstance "AES"]
    set secureRandom4 [java::new java.security.SecureRandom]
    $keyGenerator4 {init int java.security.SecureRandom} 128 $secureRandom4
    set secretKey4 [$keyGenerator4 generateKey]
    set secretKeyToken4 \
	    [java::new ptolemy.actor.lib.security.KeyToken $secretKey4]

    set  boolean [$secretKeyToken isEqualTo $secretKeyToken4]
    $boolean toString
} {false}

######################################################################
####
# 
test KeyToken-10.1 {KeyType.convert} {
    # uses 1.1 above
    set KEY  [java::field ptolemy.actor.lib.security.KeyToken KEY]
    set secretKeyToken3 [$KEY convert $secretKeyToken]
    set  boolean [$secretKeyToken isEqualTo $secretKeyToken2]
    $boolean toString
} {true}

######################################################################
####
# 
test KeyToken-10.2 {KeyType.convert with a StringToken} {
    # uses 1.1 and 10.1 above
    set stringToken [java::new ptolemy.data.StringToken "MyStringToken"]
    catch {set secretKeyToken3 [$KEY convert $stringToken]} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Attempt to convert token "MyStringToken" into a Key token, which is not possible.}}

######################################################################
####
# 
test KeyToken-11.1 {KeyType.getTokenClass} {
    # uses 1.1 and 10.1 above
    [$KEY getTokenClass] toString
} {class ptolemy.actor.lib.security.KeyToken}

######################################################################
####
# 
test KeyToken-12.1 {KeyType.isCompatible} {
    # uses 1.1 and 10.1 above
    list \
	    [$KEY isCompatible $KEY] \
	    [$KEY isCompatible \
	    [java::field ptolemy.data.type.BaseType DOUBLE]]
} {1 0}

######################################################################
####
# 
test KeyToken-13.1 {KeyType.isSubstitutionInstance} {
    # uses 1.1 and 10.1 above
    list \
	    [$KEY isSubstitutionInstance $KEY] \
	    [$KEY isSubstitutionInstance \
	    [java::field ptolemy.data.type.BaseType DOUBLE]]
} {1 0}
