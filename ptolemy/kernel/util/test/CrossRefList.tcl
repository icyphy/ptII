# Tests for the NamedObj class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997 The Regents of the University of California.
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
#### _testGetCrossRefList
# Given a CrossRefList, return a Tcl List containing its contents
#
proc _testGetCrossRefList {crossreflist} {
    set results {}
    if {$crossreflist == [java::null]} {
	return $results
    } 
    for {set crossrefenum [$crossreflist enumerate]} \
	    {$crossrefenum != [java::null] && \
	    [$crossrefenum hasMoreElements] == 1} \
	    {} {
	set enumelement [$crossrefenum nextElement]
	if [ java::instanceof $enumelement pt.kernel.NamedObj] {
	    lappend results [$enumelement getName]
	} else {
	    lappend results $enumElement
	}
    }
    return $results
}

######################################################################
####
# 
test CrossRefList-1.1 {Get information about an instance of CrossRefList} {
    # If anything changes, we want to know about it so we can write tests.
    set owner [java::new Object]
    set n [java::new pt.kernel.CrossRefList $owner]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.CrossRefList
  fields:        
  methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait {associate pt.kernel.CrossRefList} dissociate {dissociate java.lang.Object} {isMember java.lang.Object} isEmpty enumerate size {copyList pt.kernel.CrossRefList}
  constructors:  {pt.kernel.CrossRefList java.lang.Object} {pt.kernel.CrossRefList java.lang.Object pt.kernel.CrossRefList}
  properties:    empty class
  superclass:    java.lang.Object
}}


######################################################################
####
# 
test CrossRefList-2.1 {Create a CrossRefList, copy it} {
    set owner [java::new Object]
    set crlone [java::new pt.kernel.CrossRefList $owner]
    set crltwo [java::new pt.kernel.CrossRefList $owner $crlone]
    list [$crlone isEmpty] [$crltwo isEmpty] [$crlone size] [$crlone size]
} {1 1 0 0}

######################################################################
####
# 
test CrossRefList-2.2 {Create a CrossRefList, try to enumerate it} {
    set owner [java::new Object]
    set crlone [java::new pt.kernel.CrossRefList $owner]
    set enum [$crlone enumerate]
    list [$enum hasMoreElements] [expr {[$enum nextElement]== [java::null]}]
} {0 1}


######################################################################
####
# 
test CrossRefList-3.1 {associate CrossRefLists} {
    set ownerone [java::new pt.kernel.NamedObj "Owner One"]
    set crlone [java::new pt.kernel.CrossRefList $ownerone]
    set ownertwo [java::new pt.kernel.NamedObj "Owner Two"]
    set crltwo [java::new pt.kernel.CrossRefList $ownertwo]
    $crlone associate $crltwo
    list [_testGetCrossRefList $crlone] [_testGetCrossRefList $crltwo]
} {{{Owner Two}} {{Owner One}}}
