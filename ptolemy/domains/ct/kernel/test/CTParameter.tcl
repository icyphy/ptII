# Tests for the TotallyOrderedSet class
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999 The Regents of the University of California.
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

######################################################################
####  Generally used director.
#

######################################################################
####  Test constructors.
#
test CTParameter-1.1 {Construct a CTParameter and get name} {
    set a1 [java::new ptolemy.domains.ct.kernel.CTActor]
    set p1 [java::new ptolemy.domains.ct.kernel.CTParameter $a1 P1]
    list [[$a1 getAttribute P1] getName] 
} {P1}

test CTParameter-1.2 {Construct a CTParameter with a Init Token} {
    # Note: Use the above setup
    set t1 [java::new {ptolemy.data.DoubleToken double} 1.0]
    set p2 [java::new ptolemy.domains.ct.kernel.CTParameter $a1 P2 $t1]
    list [[$a1 getAttribute P2] getName] \
	    [[[$a1 getAttribute P2] getToken] doubleValue]
} {P2 1.0}

######################################################################
####  Test Exceptions
#  
test CTParameter-2.1 {NameDuplicationException} {
    # Note: Use the above setup
    catch {[java::new ptolemy.domains.ct.kernel.CTParameter $a1 P1 $t1]} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "P1" into a container that already contains an object with that name.}}

test CTParameter-2.2 {IllegalActionException} {
    # new set up.
    set a1 [java::new ptolemy.actor.TypedAtomicActor]
    catch {[java::new ptolemy.domains.ct.kernel.CTParameter $a1 P1]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .P1 and .: CTParameter can only be attached to CT actors.}}

test CTParameter-2.3 {IllegalActionException 2} {
    # Note: Use the above setup
    catch {[java::new ptolemy.domains.ct.kernel.CTParameter $a1 P2 $t1]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .P2 and .: CTParameter can only be attached to CT actors.}}



