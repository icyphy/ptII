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

# Load up Tcl Procs to print out enums
if {[info procs _testCrossRefListElements] == "" } then { 
    source testEnums.tcl
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
test CrossRefList-1.1 {Get information about an instance of CrossRefList} {
    # If anything changes, we want to know about it so we can write tests.
    set owner [java::new Object]
    set n [java::new pt.kernel.CrossRefList $owner]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.CrossRefList
  fields:        
  methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait {associate pt.kernel.CrossRefList} dissociate {dissociate java.lang.Object} {isMember java.lang.Object} isEmpty elements size {duplicate pt.kernel.CrossRefList}
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
    set enum [$crlone elements]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test CrossRefList-3.1 {associate CrossRefLists, check out isMember} {
    set ownerone [java::new pt.kernel.NamedObj "Owner One"]
    set crlone [java::new pt.kernel.CrossRefList $ownerone]
    set ownertwo [java::new pt.kernel.NamedObj "Owner Two"]
    set crltwo [java::new pt.kernel.CrossRefList $ownertwo]
    $crlone associate $crltwo
    list [_testCrossRefListElements $crlone $crltwo] \
	    [list \
	    [$crlone isMember $ownerone] [$crlone isMember $crlone] \
	    [$crlone isMember $ownertwo] [$crlone isMember $crltwo]] \
	    [list \
	    [$crltwo isMember $ownerone] [$crltwo isMember $crlone] \
	    [$crltwo isMember $ownertwo] [$crltwo isMember $crltwo]]

} {{{{Owner Two}} {{Owner One}}} {0 0 1 0} {1 0 0 0}}

######################################################################
####
# 
test CrossRefList-4.1 {associate CrossRefLists, check out Dissociate} {
    # Create Three CrossRefLists, associate the first to the other two,
    # then disassociate
    set ownerone [java::new pt.kernel.NamedObj "Owner One"]
    set crlone [java::new pt.kernel.CrossRefList $ownerone]
    set ownertwo [java::new pt.kernel.NamedObj "Owner Two"]
    set crltwo [java::new pt.kernel.CrossRefList $ownertwo]
    set ownerthree [java::new pt.kernel.NamedObj "Owner Three"]
    set crlthree [java::new pt.kernel.CrossRefList $ownerthree]
    set result0 [_testCrossRefListElements $crlone $crltwo $crlthree]

    $crlone associate $crltwo
    set result1 [_testCrossRefListElements $crlone $crltwo $crlthree]

    $crlone associate $crlthree
    set result2 [_testCrossRefListElements $crlone $crltwo $crlthree]

    $crlone dissociate $ownertwo
    set result3 [_testCrossRefListElements $crlone $crltwo $crlthree]

    $crlone dissociate $ownerthree
    set result4 [_testCrossRefListElements $crlone $crltwo $crlthree]

    list "\
result0 = $result0\n\
result1 = $result1\n\
result2 = $result2\n\
result3 = $result3\n\
result4 = $result4\n\
"
} {{ result0 = {} {} {}
 result1 = {{Owner Two}} {{Owner One}} {}
 result2 = {{Owner Three} {Owner Two}} {{Owner One}} {{Owner One}}
 result3 = {{Owner Three}} {} {{Owner One}}
 result4 = {} {} {}
 }}

######################################################################
####
# 
test CrossRefList-4.2 {associate CrossRefLists, check out Dissociate} {
    set ownerone [java::new pt.kernel.NamedObj "Owner One"]
    set crlone [java::new pt.kernel.CrossRefList $ownerone]
    set ownertwo [java::new pt.kernel.NamedObj "Owner Two"]
    set crltwo [java::new pt.kernel.CrossRefList $ownertwo]
    set ownerthree [java::new pt.kernel.NamedObj "Owner Three"]
    set crlthree [java::new pt.kernel.CrossRefList $ownerthree]
    set result0 [_testCrossRefListElements $crlone $crltwo $crlthree]

    # 1->2 2->3 3->2
    $crlone associate $crltwo
    $crlone associate $crlthree
    $crltwo associate $crlthree
    $crlthree associate $crltwo
    set result1 [_testCrossRefListElements $crlone $crltwo $crlthree]

    $crltwo dissociate 
    set result2 [_testCrossRefListElements $crlone $crltwo $crlthree]

    $crlone associate $crltwo
    set result3 [_testCrossRefListElements $crlone $crltwo $crlthree]

    $crlone dissociate
    set result4 [_testCrossRefListElements $crlone $crltwo $crlthree]

    list "\
result0 = $result0\n\
1->2 1->3 2-3>= $result1\n\
dissociate 2 = $result2\n\
1->2 = $result3\n\
dissociate 1 = $result4\n\
"
} {{ result0 = {} {} {}
 1->2 1->3 2-3>= {{Owner Three} {Owner Two}} {{Owner Three} {Owner Three} {Owner One}} {{Owner Two} {Owner Two} {Owner One}}
 dissociate 2 = {{Owner Three}} {} {{Owner One}}
 1->2 = {{Owner Two} {Owner Three}} {{Owner One}} {{Owner One}}
 dissociate 1 = {} {} {}
 }}

######################################################################
####
# 
test CrossRefList-4.3 {associate two CrossLists many times, then dissociate} {
    set ownerone [java::new pt.kernel.NamedObj "Owner One"]
    set crlone [java::new pt.kernel.CrossRefList $ownerone]
    set ownertwo [java::new pt.kernel.NamedObj "Owner Two"]
    set crltwo [java::new pt.kernel.CrossRefList $ownertwo]

    set result0 [_testCrossRefListElements $crlone $crltwo $crlthree]

    $crlone associate $crltwo
    $crlone associate $crltwo
    $crlone associate $crltwo
    $crlone associate $crltwo
    set result1 [_testCrossRefListElements $crlone $crltwo $crlthree]
 
    $crlone dissociate $ownertwo
    $crlone dissociate $ownertwo
    set result2 [_testCrossRefListElements $crlone $crltwo $crlthree]

    $crlone dissociate $ownertwo
    $crlone dissociate $ownertwo
    $crlone dissociate $ownertwo
    $crlone dissociate $ownertwo
    set result3 [_testCrossRefListElements $crlone $crltwo $crlthree]

   list "\
result0 = $result0\n\
1->2 4 times = $result1\n\
dissociate 1->2 twice = $result2\n\
dissociate 1->2 4 times = $result3\n\
"
} {{ result0 = {} {} {}
 1->2 4 times = {{Owner Two} {Owner Two} {Owner Two} {Owner Two}} {{Owner One} {Owner One} {Owner One} {Owner One}} {}
 dissociate 1->2 twice = {{Owner Two} {Owner Two}} {{Owner One} {Owner One}} {}
 dissociate 1->2 4 times = {} {} {}
 }}

######################################################################
####
# 
test CrossRefList-5.1 {associate CrossRefLists, then call duplicate} {
    set ownerone [java::new pt.kernel.NamedObj "Owner One"]
    set crlone [java::new pt.kernel.CrossRefList $ownerone]
    set ownertwo [java::new pt.kernel.NamedObj "Owner Two"]
    set crltwo [java::new pt.kernel.CrossRefList $ownertwo]
    set ownerthree [java::new pt.kernel.NamedObj "Owner Three"]
    set crlthree [java::new pt.kernel.CrossRefList $ownerthree]
    set ownerfour [java::new pt.kernel.NamedObj "Owner Four"]
    set crlfour [java::new pt.kernel.CrossRefList $ownerfour]

    set result0 [_testCrossRefListElements $crlone $crltwo $crlthree $crlfour]

    $crlone associate $crltwo
    $crlone associate $crlthree
    set result1 [_testCrossRefListElements $crlone $crltwo $crlthree $crlfour]
 
    $crlfour duplicate $crlone
    set result2 [_testCrossRefListElements $crlone $crltwo $crlthree $crlfour]

    $crlone dissociate
    set result3 [_testCrossRefListElements $crlone $crltwo $crlthree $crlfour]

    $crlfour dissociate $ownerthree
    set result4 [_testCrossRefListElements $crlone $crltwo $crlthree $crlfour]

   list "\
result0 = $result0\n\
1->2 1->3 = $result1\n\
copy 1 to 4 = $result2\n\
dissociate 1 = $result3\n\
dissociate 4->3 = $result4\n\
"
} {{ result0 = {} {} {} {}
 1->2 1->3 = {{Owner Three} {Owner Two}} {{Owner One}} {{Owner One}} {}
 copy 1 to 4 = {{Owner Three} {Owner Two}} {{Owner Four} {Owner One}} {{Owner Four} {Owner One}} {{Owner Two} {Owner Three}}
 dissociate 1 = {} {{Owner Four}} {{Owner Four}} {{Owner Two} {Owner Three}}
 dissociate 4->3 = {} {{Owner Four}} {} {{Owner Two}}
 }}
