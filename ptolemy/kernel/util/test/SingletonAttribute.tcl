# Tests for the SingletonAttribute class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2005 The Regents of the University of California.
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

if {[string compare test [info procs test]] == 1} then {
    source [file join $PTII util testsuite testDefs.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test SingletonAttribute1.1 {Create SingletonAttributes} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n1 [java::new ptolemy.kernel.util.NamedObj] 
    set sa1 [java::new ptolemy.kernel.util.SingletonAttribute] 
    set sa2 [java::new ptolemy.kernel.util.SingletonAttribute $w] 
    set sa3 [java::new ptolemy.kernel.util.SingletonAttribute $n1 "foo"] 
    list [$sa1 toString] [$sa2 toString ] [$sa3 toString]
} {{ptolemy.kernel.util.SingletonAttribute {.}} {ptolemy.kernel.util.SingletonAttribute {.}} {ptolemy.kernel.util.SingletonAttribute {..foo}}}

test SingletonAttribute2.1 {setContainer with the same name} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "N1"] 
    set n2 [java::new ptolemy.kernel.util.NamedObj "N2"] 
    set sa3 [java::new ptolemy.kernel.util.SingletonAttribute $n1 "foo"] 
    set sa4 [java::new ptolemy.kernel.util.SingletonAttribute $n2 "foo"] 
    $sa4 setContainer $n1
    list [$sa3 toString] [$sa4 toString]
} {{ptolemy.kernel.util.SingletonAttribute {.foo}} {ptolemy.kernel.util.SingletonAttribute {.N1.foo}}}


test SingletonAttribute-2.1 {setContainer with different workspaces} {
    set w1 [java::new ptolemy.kernel.util.Workspace W1]
    set w2 [java::new ptolemy.kernel.util.Workspace W2]
    set n1 [java::new ptolemy.kernel.util.NamedObj $w1 N1] 
    set n2 [java::new ptolemy.kernel.util.NamedObj $w2 N2] 
    set sa5 [java::new ptolemy.kernel.util.SingletonAttribute $n1 foo] 
    set sa6 [java::new ptolemy.kernel.util.SingletonAttribute $n2 foo] 
    # Cover the catch block in setContainer
    catch {$sa5 setContainer $n2} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot set container because workspaces are different.
  in .N1.foo and .N2}}
