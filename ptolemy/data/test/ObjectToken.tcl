# Tests for the ObjectToken class
#
# @Author: Edward A. Lee, Neil Smyth
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
test ObjectToken-2.1 {Create an empty instance} {
    set p [java::new ptolemy.data.ObjectToken]
    $p toString
} {present}

######################################################################
####
# 
test ObjectToken-2.2 {Create an empty instance and query its value} {
    set p [java::new ptolemy.data.ObjectToken]
    expr { [$p getValue] == [java::null] }
} {1}

######################################################################
####
# 
# test ObjectToken-4.1 {Create an empty instance and clone} {
#     set p [java::new ptolemy.data.ObjectToken]
#     set q [$p clone]
#     expr { [$q getObject] == [java::null] }
# } {1}

######################################################################
####
# 
# test ObjectToken-4.2 {Create a non empty instance and clone} {
#     set n [java::new {java.lang.StringBuffer String} foo]
#     set p [java::new ptolemy.data.ObjectToken $n]
#     set q [$p clone]
#     list [$p toString] [$q toString]
# } {foo foo}

######################################################################
####
# 
# test ObjectToken-4.3 {Create a non empty instance, modify object, and clone} {
#     set n [java::new {java.lang.StringBuffer String} foo]
#     set p [java::new ptolemy.data.ObjectToken $n]
#     set q [$p clone]
#     $n {append String} " bar"
#     list [$p toString] [$q toString]
# } {{foo bar} {foo bar}}

