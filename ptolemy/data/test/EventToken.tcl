# Tests for the EventToken class
#
# @Author: Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# 
#

######################################################################
####
# 
test EventToken-1.0 {Create an instance} {
    set p [java::new ptolemy.data.EventToken]
    list [$p toString] [[$p getType] toString]
} {present event}

######################################################################
####
# 
test EventToken-2.0 {Test add} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 add $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: add operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}}

######################################################################
####
# 
test EventToken-2.1 {Test addReverse} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 addReverse $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: addReverse operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}}

######################################################################
####
# 
test EventToken-2.2 {Test divide} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 divide $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}}

######################################################################
####
# 
test EventToken-2.3 {Test divideReverse} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 divideReverse $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: divideReverse operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}}

######################################################################
####
# 
test EventToken-2.4 {Test equals} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    set p3 [java::new ptolemy.data.IntEventToken]
    list [$p1 equals $p1] [$p1 equals $p2] [$p1 equals $p3]
} {1 1 0} {Known failure}

######################################################################
####
# 
test EventToken-2.5 {Test hashCode} {
    set p1 [java::new ptolemy.data.EventToken]
    $p1 hashCode
} {0}

######################################################################
####
# 
test EventToken-2.6 {Test isEqualTo} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 {isEqualTo ptolemy.data.EventToken} $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: isEqualTo operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}} {known failure}

######################################################################
####
# 
test EventToken-2.6.1 {Test isCloseTo} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 {isCloseTo ptolemy.data.EventToken} $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: isCloseTo operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}} {Known failure}

######################################################################
####
# 
test EventToken-2.7 {Test modulo} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 modulo $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}}

######################################################################
####
# 
test EventToken-2.8 {Test moduloReverse} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 moduloReverse $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: moduloReverse operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}}

######################################################################
####
# 
test EventToken-2.9 {Test multiply} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 multiply $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}}

######################################################################
####
# 
test EventToken-2.10 {Test multiplyReverse} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 multiplyReverse $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: multiplyReverse operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}}

######################################################################
####
# 
test EventToken-2.11 {Test subtract} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 subtract $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: subtract operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}}

######################################################################
####
# 
test EventToken-2.12 {Test subtractReverse} {
    set p1 [java::new ptolemy.data.EventToken]
    set p2 [java::new ptolemy.data.EventToken]
    catch {$p1 subtractReverse $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: subtractReverse operation not supported between ptolemy.data.EventToken 'present' and ptolemy.data.EventToken 'present'}}

######################################################################
####
# 
test EventToken-3.0 {Test one} {
    set p [java::new ptolemy.data.EventToken]
    catch {$p1 one} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Multiplicative identity not supported on ptolemy.data.EventToken.}}

######################################################################
####
# 
test EventToken-3.1 {Test zero} {
    set p [java::new ptolemy.data.EventToken]
    catch {$p1 zero} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Additive identity not supported on ptolemy.data.EventToken.}}

######################################################################
####
# 
test EventToken-4.0 {Test toString} {
    set p [java::new ptolemy.data.EventToken]
    list [$p toString]
} {present}
