# Tests for the ToIntMapMapping class.
#
# @Author: Shahrooz Shahparnia
#
# $Id$
#
# @Copyright (c) 2001-2002 The Regents of the University of Maryland.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
#                                           PT_COPYRIGHT_VERSION_2
#                                           COPYRIGHTENDKEY
#######################################################################

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
}

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
test ToDoubleMapMapping-1.1 {building an example} {
    set map [java::new java.util.HashMap]
    set mapping [java::new ptolemy.graph.mapping.ToIntMapMapping $map]
    set dummyObject1 [java::new java.lang.Object]
    set dummyObject2 [java::new java.lang.Object]
    $map put $dummyObject1 [java::new Integer 3]
    $map put $dummyObject2 [java::new Integer -4]
    set value1 [$mapping toInt $dummyObject1]
    set value2 [$mapping toInt $dummyObject2]
    list $value1 $value2
} {3 -4}

######################################################################
####
#
test ToDoubleMapMapping-1.2 {exceptions} {
    set dummyObject3 [java::new java.lang.Object]
    catch {$mapping toInt $dummyObject3} msg1
    list $msg1
} {java.lang.NullPointerException}


