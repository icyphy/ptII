# Test ChangeRequest
#
# @Author: Edward A. Lee
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
#### ChangeRequest
#

test ChangeRequest-1.0 {test simple run with only parameter changes} {
    set t [java::new ptolemy.kernel.event.test.ChangeRequestTest]
    $t start
    $t mutate
    enumToTokenValues [$t finish]
} {1 2.0 2.0 2.0 2.0}

test ChangeRequest-2.0 {test elaborate run with graph rewiring} {
    $t start
    $t insertFeedback
    enumToTokenValues [$t finish]
} {2.0 6.0 7.0 8.0 9.0}

test ChangeRequest-3.0 {test DE example with no mutations} {
    set t [java::new ptolemy.kernel.event.test.TestDE]
    $t start
    # $t insertFeedback
    enumToObjects [$t finish]
} {0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0}

test ChangeRequest-3.1 {test DE example with period change} {
    $t start
    $t doublePeriod
    enumToObjects [$t finish]
} {0.0 1.0 2.0 3.0 5.0 7.0 9.0 11.0}

test ChangeRequest-3.2 {test DE example with inserted actor} {
    set t [java::new ptolemy.kernel.event.test.TestDE]
    $t start
    $t insertClock
    enumToObjects [$t finish]
} {0.0 1.0 2.0 2.5 3.0 4.0 4.5 5.0 6.0 6.5 7.0 8.0 8.5 9.0 10.0 10.5}
