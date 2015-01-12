# Tests for the SmoothToken class
#
# @Author: Edward A. Lee, contributor: Christopher Brooks
#
# @Version $Id$
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
test SmoothToken-1.0 {Create an empty instance} {
    set s0 [java::new ptolemy.data.SmoothToken 0.0]
    $s0 toString
} {0.0}

######################################################################
####
# 
test SmoothToken-1.1 {Create a single derivative} {
    set d [java::new {double[]} {1} {1.0}]
    set s1 [java::new ptolemy.data.SmoothToken 1.0 $d]
    $s1 toString
} {smoothToken(1.0, {1.0})}

######################################################################
####
# 
test SmoothToken-1.2 {Test adding a double} {
    set d1 [java::new ptolemy.data.DoubleToken 1.0]
    set result [$s1 add $d1]
    $result toString
} {smoothToken(2.0, {1.0})}

######################################################################
####
# 
test SmoothToken-1.3 {Test adding in reverse} {
    set result [$d1 add $s1]
    $result toString
} {smoothToken(2.0, {1.0})}

######################################################################
####
# 
test SmoothToken-1.4 {Test subtracting a double} {
    set result [$s1 subtract $d1]
    $result toString
} {smoothToken(0.0, {1.0})}

######################################################################
####
# 
test SmoothToken-1.5 {Test subtracting in reverse} {
    set result [$d1 subtract $s1]
    $result toString
} {smoothToken(0.0, {-1.0})}

######################################################################
####
# 
test SmoothToken-1.6 {Test adding two SmoothTokens of different lengths} {
    set derivatives [java::new {double[]} {2} {2.0 3.0}]
    set s2 [java::new ptolemy.data.SmoothToken 2.0 $derivatives]
    set result [$s1 add $s2]
    $result toString
} {smoothToken(3.0, {3.0,3.0})}

######################################################################
####
# 
test SmoothToken-1.7 {Test adding in reverse} {
    set result [$s2 add $s1]
    $result toString
} {smoothToken(3.0, {3.0,3.0})}

######################################################################
####
# 
test SmoothToken-1.8 {Test subtracting two SmoothTokens of different lengths} {
    set result [$s1 subtract $s2]
    $result toString
} {smoothToken(-1.0, {-1.0,-3.0})}

######################################################################
####
# 
test SmoothToken-1.9 {Test subtracting in reverse} {
    set result [$s2 subtract $s1]
    $result toString
} {smoothToken(1.0, {1.0,3.0})}

######################################################################
####
# 
test SmoothToken-1.10 {Test multiplying a double} {
    set d2 [java::new ptolemy.data.DoubleToken 2.0]
    set result [$s1 multiply $d2]
    $result toString
} {smoothToken(2.0, {2.0})}

######################################################################
####
# 
test SmoothToken-1.11 {Test multiplying in reverse} {
    set result [$d2 multiply $s1]
    $result toString
} {smoothToken(2.0, {2.0})}

######################################################################
####
# 
test SmoothToken-1.8 {Test multiplying two SmoothTokens of different lengths} {
    # s1 is smoothToken(1.0, {1.0})
    # s2 is smoothToken(2.0, {2.0, 3.0})
    set result [$s1 multiply $s2]
    $result toString
} {smoothToken(2.0, {4.0,7.0})}
