# Tests for the Token class
#
# @Author: Yuhong Xiong
#
# @Version $Id$
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
test Token-1.0 {Create an instance} {
    set p [java::new ptolemy.data.Token]
    $p toString
} {present}

######################################################################
####
# 
test Token-2.0 {Test add} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 add $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Addition not supported between ptolemy.data.Token and ptolemy.data.Token.}}

######################################################################
####
# 
test Token-2.1 {Test addReverse} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 addReverse $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Addition not supported between ptolemy.data.Token and ptolemy.data.Token.}}

######################################################################
####
# 
test Token-2.2 {Test divide} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 divide $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Division not supported for ptolemy.data.Token divided by ptolemy.data.Token.}}

######################################################################
####
# 
test Token-2.3 {Test divideReverse} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 divideReverse $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Division not supported for ptolemy.data.Token divided by ptolemy.data.Token.}}

######################################################################
####
# 
test Token-2.4 {Test isEqualTo} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 {isEqualTo ptolemy.data.Token} $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Equality test not supported between ptolemy.data.Token and ptolemy.data.Token.}}

######################################################################
####
# 
test Token-2.5 {Test modulo} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 modulo $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Modulo operation not supported: ptolemy.data.Token modulo ptolemy.data.Token.}}

######################################################################
####
# 
test Token-2.6 {Test moduloReverse} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 moduloReverse $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Modulo operation not supported on ptolemy.data.Token objects modulo ptolemy.data.Token objects.}}

######################################################################
####
# 
test Token-2.7 {Test multiply} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 multiply $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Multiplication not supported on ptolemy.data.Token by ptolemy.data.Token.}}

######################################################################
####
# 
test Token-2.8 {Test multiplyReverse} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 multiplyReverse $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Multiplication not supported on ptolemy.data.Token by ptolemy.data.Token.}}

######################################################################
####
# 
test Token-2.9 {Test subtract} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 subtract $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Subtraction not supported on ptolemy.data.Token minus ptolemy.data.Token.}}

######################################################################
####
# 
test Token-2.10 {Test subtractReverse} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    catch {$p1 subtractReverse $p2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Subtraction not supported on ptolemy.data.Token minus ptolemy.data.Token.}}

######################################################################
####
# 
test Token-3.0 {Test one} {
    set p [java::new ptolemy.data.Token]
    catch {$p1 one} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Multiplicative identity not supported on ptolemy.data.Token.}}

######################################################################
####
# 
test Token-3.1 {Test zero} {
    set p [java::new ptolemy.data.Token]
    catch {$p1 zero} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Token.zero: Additive identity not supported on ptolemy.data.Token.}}

######################################################################
####
# 
test Token-4.0 {Test stringValue} {
    set p [java::new ptolemy.data.Token]
    list [$p stringValue]
} {present}

######################################################################
####
# 
test Token-5.0 {Test convert} {
    set p1 [java::new ptolemy.data.Token]
    set p2 [java::new ptolemy.data.Token]
    set p3 [$p1 convert $p2]
    $p3 toString
} {present}
