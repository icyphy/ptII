# Tests for the NameDuplicationException class
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
# test NameDuplicationException-2.1 {One named objects} {
#     set containee [java::new ptolemy.kernel.Port]
#     $containee setName "wouldBeContainee"
#     set pe [java::new {ptolemy.kernel.util.NameDuplicationException \
#             ptolemy.kernel.util.Nameable} $containee]
#     list [$pe getMessage] [$pe getLocalizedMessage]
# } {{Attempt to insert object named "wouldBeContainee" into a container that already contains an object with that name.} {Attempt to insert object named "wouldBeContainee" into a container that already contains an object with that name.}}

######################################################################
####
#
# test NameDuplicationException-2.2 {One named object and one string} {
#     set containee [java::new ptolemy.kernel.Port]
#     $containee setName "wouldBeContainee"
#     set pe [java::new {ptolemy.kernel.util.NameDuplicationException \
#             ptolemy.kernel.util.Nameable String} $containee {more info}]
#     list [$pe getMessage] [$pe getLocalizedMessage]
# } {{Attempt to insert object named "wouldBeContainee" into a container that already contains an object with that name. more info} {Attempt to insert object named "wouldBeContainee" into a container that already contains an object with that name. more info}}

######################################################################
####
#
test NameDuplicationException-2.3 {Two named objects arguments} {
    set container [java::new ptolemy.kernel.Entity "container"]
    set containee [java::new ptolemy.kernel.Port]
    $containee setName "wouldBeContainee"
    set pe [java::new {ptolemy.kernel.util.NameDuplicationException \
            ptolemy.kernel.util.Nameable ptolemy.kernel.util.Nameable} $container $containee]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{Attempt to insert object named "wouldBeContainee" into container named ".container", which already contains an object with that name.} {Attempt to insert object named "wouldBeContainee" into container named ".container", which already contains an object with that name.}}

######################################################################
####
#
test NameDuplicationException-2.4 {two objects and a string} {
    set container [java::new ptolemy.kernel.Entity "container"]
    set containee [java::new ptolemy.kernel.Port]
    $containee setName "wouldBeContainee"
    set pe [java::new ptolemy.kernel.util.NameDuplicationException \
            $container $containee "more info" ]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{Attempt to insert object named "wouldBeContainee" into container named ".container", which already contains an object with that name. more info} {Attempt to insert object named "wouldBeContainee" into container named ".container", which already contains an object with that name. more info}}

######################################################################
####
#
test NameDuplicationException-3.1 {Two null objects} {
    set container [java::null]
    set containee [java::null]
    set pe [java::new ptolemy.kernel.util.NameDuplicationException \
            $container $containee "more info" ]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{Attempt to insert object named "" into a container that already contains an object with that name. more info} {Attempt to insert object named "" into a container that already contains an object with that name. more info}}
