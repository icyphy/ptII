# Tests for the IntegerList class
#
# @Author: 
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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
test IntegerList-1.1 {Construct an IntegerList} {
    set context [[java::new ptolemy.caltrop.PtolemyPlatform] context]
    set list [java::new {ptolemy.caltrop.util.IntegerList caltrop.interpreter.Context int int} $context 2 8]
    $list toString
} {[2, 3, 4, 5, 6, 7, 8]}

test IntegerList-1.2 {Construct a zero-length IntegerList} {
    set context [[java::new ptolemy.caltrop.PtolemyPlatform] context]
    set list [java::new {ptolemy.caltrop.util.IntegerList \
	    caltrop.interpreter.Context int int} $context 2 1]
    $list toString
} {[]}

test IntegerList-1.3 {Length of an IntegerList} {
    set context [[java::new ptolemy.caltrop.PtolemyPlatform] context]
    set list [java::new {ptolemy.caltrop.util.IntegerList \
	    caltrop.interpreter.Context int int} $context 2 8]
    $list size
} {7}

test IntegerList-1.4 {Length of a zero-length IntegerList} {
    set context [[java::new ptolemy.caltrop.PtolemyPlatform] context]
    set list [java::new {ptolemy.caltrop.util.IntegerList \
	    caltrop.interpreter.Context int int} $context 2 1]
    $list size
} {0}

test IntegerList-1.5 {Access an IntegerList} {
    set context [[java::new ptolemy.caltrop.PtolemyPlatform] context]
    set list [java::new {ptolemy.caltrop.util.IntegerList \
	    caltrop.interpreter.Context int int} $context 2 8]
    set n [$list {get int} 3]
    $n toString
} {5}

test IntegerList-1.6 {Access an IntegerList with an out of bounds} {
    set context [[java::new ptolemy.caltrop.PtolemyPlatform] context]
    set list [java::new {ptolemy.caltrop.util.IntegerList \
	    caltrop.interpreter.Context int int} $context 2 8]
    catch {$list {get int} 7} errMsg
    list $errMsg
} {{java.lang.IndexOutOfBoundsException: 2 + 7 is greater than 8}}

test IntegerList-1.7 {Length of a singleton IntegerList} {
    set context [[java::new ptolemy.caltrop.PtolemyPlatform] context]
    set list [java::new {ptolemy.caltrop.util.IntegerList \
	    caltrop.interpreter.Context int int} $context 2 2]
    $list size
} {1}

test IntegerList-1.8 {Access element of a singleton IntegerList} {
    set context [[java::new ptolemy.caltrop.PtolemyPlatform] context]
    set list [java::new {ptolemy.caltrop.util.IntegerList \
	    caltrop.interpreter.Context int int} $context 2 2]
    set n [$list {get int} 0]
    $n toString
} {2}


