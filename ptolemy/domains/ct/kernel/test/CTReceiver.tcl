# Tests for the CTReceiver
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-2009 The Regents of the University of California.
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

######################################################################
####  Generally used director.
#

######################################################################
####  Test constructors.
#
test CTReceiver-1.1 {Construct a CTReceiver, set and get signal type} {
    set re1 [java::new ptolemy.domains.ct.kernel.CTReceiver]
    $re1 setSignalType [java::field ptolemy.domains.ct.kernel.CTReceiver \
	CONTINUOUS]
    set compareResult [[$re1 getSignalType] equals [java::field \
	ptolemy.domains.ct.kernel.CTReceiver CONTINUOUS]]
    list $compareResult
} {1}


test CTReceiver-1.2 {Construct a CTReceiver, put and get Token} {
    set re1 [java::new ptolemy.domains.ct.kernel.CTReceiver]
    $re1 setSignalType [java::field ptolemy.domains.ct.kernel.CTReceiver \
	CONTINUOUS]
    set one [java::new {ptolemy.data.DoubleToken double} 1.0]
    $re1 put $one
    [java::cast ptolemy.data.DoubleToken [$re1 get]] doubleValue
} {1.0}


test CTReceiver-1.3 {Construct a CTReceiver with container} {
    set p1 [java::new ptolemy.actor.TypedIOPort]
    set re1 [java::new ptolemy.domains.ct.kernel.CTReceiver $p1]
    $re1 setSignalType [java::field ptolemy.domains.ct.kernel.CTReceiver \
	DISCRETE]
    set one [java::new {ptolemy.data.DoubleToken double} 1.0]
    $re1 put $one
    [java::cast ptolemy.data.DoubleToken [$re1 get]] doubleValue
} {1.0}

######################################################################
####  check has room
#
test CTReceiver-2.1 {check has room} {
    set zero [java::new {ptolemy.data.DoubleToken double} 0.0]
    set hr1 [$re1 hasRoom]
    $re1 put $zero
    set hr2 [$re1 hasRoom]
    list $hr1 $hr2
} {1 1}


######################################################################
####  Overwrite tokens.
#
test CTReceiver-2.2 {put two tokens} {
    set zero [java::new {ptolemy.data.DoubleToken double} 0.0]
    $re1 put $one
    $re1 put $zero
    #list [[$re1 get] doubleValue]
    [java::cast ptolemy.data.DoubleToken [$re1 get]] doubleValue
} {0.0}

######################################################################
####  Retake tokens from a continuous receiver
#
test CTReceiver-2.3 {take a token twice} {
    $re1 setSignalType  [java::field ptolemy.domains.ct.kernel.CTReceiver \
	CONTINUOUS]
    set zero [java::new {ptolemy.data.DoubleToken double} 0.0]
    $re1 put $zero
    $re1 get
    [java::cast ptolemy.data.DoubleToken [$re1 get]] doubleValue
} {0.0}

######################################################################
####  Retake tokens from a discrete receiver should throw an exception.
#
test CTReceiver-2.4 {take a token twice} {
    $re1 setSignalType  [java::field ptolemy.domains.ct.kernel.CTReceiver \
	DISCRETE]
    $re1 put $one
    $re1 get
    catch {$re1 get} errMsg
    list $errMsg
} {{ptolemy.actor.NoTokenException: Attempt to get data from an empty CTReceiver.
Are you trying to use a discrete signal to drive a continuous port?
  in .<Unnamed Object>}}

######################################################################
####
#
test CTReceiver-3.1 {passing tokens} {
    set ca [java::new ptolemy.actor.TypedCompositeActor]
    $ca setName CA
    set manager [java::new ptolemy.actor.Manager]
    $ca setManager $manager    
            
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $ca Dir]
    set a1 [java::new ptolemy.domains.ct.kernel.test.CTDummySource $ca A1]
    set a2 [java::new ptolemy.domains.ct.kernel.test.CTDummySink $ca A2]
    set p1o [java::cast ptolemy.actor.IOPort [$a1 getPort output]]
    set p2i [java::cast ptolemy.actor.IOPort [$a2 getPort input]]
    set r1 [$ca connect $p1o $p2i]
    $manager initialize
    $p1o broadcast $zero
    [java::cast ptolemy.data.DoubleToken [$p2i get 0]] doubleValue
} {0.0}

test CTReceiver-3.2 {overwriting tokens} {
    #Note: use above setup.
    $p1o broadcast $zero
    $p1o broadcast $one
    [java::cast ptolemy.data.DoubleToken [$p2i get 0]] doubleValue
} {1.0}

