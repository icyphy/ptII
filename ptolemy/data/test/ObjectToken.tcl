# Tests for the ObjectToken class
#
# @Author: Edward A. Lee, Neil Smyth, Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2008 The Regents of the University of California.
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
test ObjectToken-2.1 {Create an empty instance} {
    set p [java::new ptolemy.data.ObjectToken]
    list [$p toString] [$p isNil]
} {object(null) 0}

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
test ObjectToken-3.0 {Test equals} {
    set i1 [java::new {java.lang.Integer int} 1]
    set t1 [java::new {ptolemy.data.ObjectToken Object} $i1]
    set i1a [java::new {java.lang.Integer int} 1]
    set t2 [java::new {ptolemy.data.ObjectToken Object} $i1a]
    set i3 [java::new {java.lang.Integer int} 3]
    set t3 [java::new {ptolemy.data.ObjectToken Object} $i3]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3]
} {1 1 0}

######################################################################
####
# 
test ObjectToken-4.0 {Test hashCode} {
    set i1 [java::new {java.lang.Integer int} 1]
    set t1 [java::new {ptolemy.data.ObjectToken Object} $i1]
    set i1a [java::new {java.lang.Integer int} 1]
    set t2 [java::new {ptolemy.data.ObjectToken Object} $i1a]
    set i3 [java::new {java.lang.Integer int} 3]
    set t3 [java::new {ptolemy.data.ObjectToken Object} $i3]

    list [$t1 hashCode] [$t2 hashCode] [$t3 hashCode]
} {1 1 3}

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

######################################################################
####
# 
test ObjectToken-13.0 {Test convert from ObjectToken} {
    set i1 [java::new {java.lang.Integer int} 1]
    set t1 [java::new {ptolemy.data.ObjectToken Object} $i1]

    set i2 [java::new {java.lang.Integer int} 2]
    set t2 [java::new {ptolemy.data.ObjectToken Object} $i2]
    
    set r1 [java::call ptolemy.data.ObjectToken convert $t2] 
    list [$r1 toString]
} {object(2)}

######################################################################
####
# 
test ObjectToken-13.0 {Test convert from IntToken} {
    set t1 [java::new ptolemy.data.IntToken 1]

    set i2 [java::new {java.lang.Integer int} 2]
    set t2 [java::new {ptolemy.data.ObjectToken Object} $i2]
    
    catch {java::call ptolemy.data.ObjectToken convert $t1} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.IntToken '1' to the type object.}}
