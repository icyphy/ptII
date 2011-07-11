# Tests for the Time class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2008-2011 The Regents of the University of California.
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
test Time-1.1 {Constructors} {
    set d1 [java::new ptolemy.actor.Director]
    $d1 setName D1
    set t1 [java::new ptolemy.actor.util.Time $d1]
    set t2 [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d1 1.0]
    set t3 [java::new {ptolemy.actor.util.Time ptolemy.actor.Director long} $d1 1]
    list [$t1 toString] [$t2 toString] [$t3 toString] [$d1 getTimeResolution]
} {0.0 1.0 1.0E-10 1e-10}

######################################################################
####
#
test Time-1.2 {Constructors: coverage} {
    set d2 [java::new ptolemy.actor.Director]
    $d2 setName D2
    set t4 [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d2 -1.0]
    catch {[java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d2 [java::field java.lang.Double NaN]]} errMsg
    set tPositiveInfinity [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d2 [java::field java.lang.Double POSITIVE_INFINITY]]
    set tNegativeInfinity [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d2 [java::field java.lang.Double NEGATIVE_INFINITY]]
    list [$t4 toString] $errMsg [$tPositiveInfinity toString] [$tNegativeInfinity toString]
} {-1.0 {java.lang.ArithmeticException: Time value can not be NaN.} Infinity -Infinity}

######################################################################
####
#
test Time-2.1 {compareTo} {
    #Uses 1.1 and 1.2 above
    list \
         [list [$t1 compareTo $t1] \
	       [$t1 compareTo $t2] \
	       [$t1 compareTo $t3] \
               [$t1 compareTo $t4] \
               [$t1 compareTo $tPositiveInfinity] \
               [$t1 compareTo $tNegativeInfinity]] \
         "\n" \
         [list [$t2 compareTo $t1] \
	       [$t2 compareTo $t2] \
	       [$t2 compareTo $t3] \
               [$t2 compareTo $t4] \
               [$t2 compareTo $tPositiveInfinity] \
              [$t2 compareTo $tNegativeInfinity]] \
         "\n" \
         [list [$t3 compareTo $t1] \
	       [$t3 compareTo $t2] \
	       [$t3 compareTo $t3] \
               [$t3 compareTo $t4] \
               [$t3 compareTo $tPositiveInfinity] \
              [$t3 compareTo $tNegativeInfinity]] \
         "\n" \
         [list [$t4 compareTo $t1] \
	       [$t4 compareTo $t2] \
	       [$t4 compareTo $t3] \
               [$t4 compareTo $t4] \
               [$t4 compareTo $tPositiveInfinity] \
              [$t4 compareTo $tNegativeInfinity]] \
         "\n" \
         [list [$tPositiveInfinity compareTo $t1] \
	       [$tPositiveInfinity compareTo $t2] \
	       [$tPositiveInfinity compareTo $t3] \
               [$tPositiveInfinity compareTo $t4] \
               [$tPositiveInfinity compareTo $tPositiveInfinity] \
               [$tPositiveInfinity compareTo $tNegativeInfinity]] \
         "\n" \
         [list [$tNegativeInfinity compareTo $t1] \
	       [$tNegativeInfinity compareTo $t2] \
	       [$tNegativeInfinity compareTo $t3] \
               [$tNegativeInfinity compareTo $t4] \
               [$tNegativeInfinity compareTo $tPositiveInfinity] \
              [$tNegativeInfinity compareTo $tNegativeInfinity]]
} {{0 -1 -1 1 -1 1} {
} {1 0 1 1 -1 1} {
} {1 -1 0 1 -1 1} {
} {-1 -1 -1 0 -1 1} {
} {1 1 1 1 0 1} {
} {-1 -1 -1 -1 -1 0}}

######################################################################
####
#
test Time-2.2 {compareTo null} {
     # The Javadoc for java.lang.Comparable says: "Note that null
     # is not an instance of any class, and e.compareTo(null)
     # should throw a NullPointerException even though
     # e.equals(null) returns false."
     catch {$t1 compareTo [java::null]} errMsg
     list $errMsg
} {java.lang.NullPointerException}

######################################################################
####
#
test Time-3.1 {equals null} {
     # The Javadoc for java.lang.Comparable says: "Note that null
     # is not an instance of any class, and e.compareTo(null)
     # should throw a NullPointerException even though
     # e.equals(null) returns false."
     list [$t1 equals [java::null]]
} {0}

####
#
test Time-3.2 {equals a non-time} {
     # The Javadoc for java.lang.Comparable says: "Note that null
     # is not an instance of any class, and e.compareTo(null)
     # should throw a NullPointerException even though
     # e.equals(null) returns false."
     list [$t1 equals [java::new java.util.Date]]
} {0}
