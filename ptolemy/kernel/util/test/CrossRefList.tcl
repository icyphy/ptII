# Tests for the CrossRefList class
#
# @Author: Christopher Hylands
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

# Load up Tcl Procs to print out enums
if {[info procs _testCrossRefListGetLinks] == "" } then {
    source testEnums.tcl
}

if {[info procs enumToNames] == "" } then {
    source enums.tcl
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
test CrossRefList-2.1 {Create a CrossRefList, copy it} {
    set owner [java::new Object]
    set crlone [java::new ptolemy.kernel.util.CrossRefList $owner]
    set crltwo [java::new ptolemy.kernel.util.CrossRefList $owner $crlone]
    list [$crlone size] [$crlone size]
} {0 0}

######################################################################
####
#
test CrossRefList-2.2 {Create a CrossRefList, try to enumerate it} {
    set owner [java::new Object]
    set crlone [java::new ptolemy.kernel.util.CrossRefList $owner]
    set enum [$crlone getContainers]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
#
test CrossRefList-3.1 {link CrossRefLists, check out isLinked} {
    set ownerone [java::new ptolemy.kernel.util.NamedObj "Owner One"]
    set crlone [java::new ptolemy.kernel.util.CrossRefList $ownerone]
    set ownertwo [java::new ptolemy.kernel.util.NamedObj "Owner Two"]
    set crltwo [java::new ptolemy.kernel.util.CrossRefList $ownertwo]
    $crlone link $crltwo
    list [_testCrossRefListGetLinks $crlone $crltwo] \
	    [list \
	    [$crlone isLinked $ownerone] [$crlone isLinked $crlone] \
	    [$crlone isLinked $ownertwo] [$crlone isLinked $crltwo]] \
	    [list \
	    [$crltwo isLinked $ownerone] [$crltwo isLinked $crlone] \
	    [$crltwo isLinked $ownertwo] [$crltwo isLinked $crltwo]]

} {{{{Owner Two}} {{Owner One}}} {0 0 1 0} {1 0 0 0}}

######################################################################
####
#
test CrossRefList-4.1 {link CrossRefLists, check out unlink} {
    # Create Three CrossRefLists, link the first to the other two,
    # then unlink
    set ownerone [java::new ptolemy.kernel.util.NamedObj "Owner One"]
    set crlone [java::new ptolemy.kernel.util.CrossRefList $ownerone]
    set ownertwo [java::new ptolemy.kernel.util.NamedObj "Owner Two"]
    set crltwo [java::new ptolemy.kernel.util.CrossRefList $ownertwo]
    set ownerthree [java::new ptolemy.kernel.util.NamedObj "Owner Three"]
    set crlthree [java::new ptolemy.kernel.util.CrossRefList $ownerthree]
    set result0 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crlone link $crltwo
    set result1 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crlone link $crlthree
    set result2 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crlone unlink $ownertwo
    set result3 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crlone unlink $ownerthree
    set result4 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    list "\
result0 = $result0\n\
result1 = $result1\n\
result2 = $result2\n\
result3 = $result3\n\
result4 = $result4\n\
"
} {{ result0 = {} {} {}
 result1 = {{Owner Two}} {{Owner One}} {}
 result2 = {{Owner Two} {Owner Three}} {{Owner One}} {{Owner One}}
 result3 = {{Owner Three}} {} {{Owner One}}
 result4 = {} {} {}
 }}

######################################################################
####
#
test CrossRefList-4.2 {link CrossRefLists, check out unlink} {
    set ownerone [java::new ptolemy.kernel.util.NamedObj "Owner One"]
    set crlone [java::new ptolemy.kernel.util.CrossRefList $ownerone]
    set ownertwo [java::new ptolemy.kernel.util.NamedObj "Owner Two"]
    set crltwo [java::new ptolemy.kernel.util.CrossRefList $ownertwo]
    set ownerthree [java::new ptolemy.kernel.util.NamedObj "Owner Three"]
    set crlthree [java::new ptolemy.kernel.util.CrossRefList $ownerthree]
    set result0 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    # 1->2 2->3 3->2
    $crlone link $crltwo
    $crlone link $crlthree
    $crltwo link $crlthree
    $crlthree link $crltwo
    set result1 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crltwo unlinkAll
    set result2 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crlone link $crltwo
    set result3 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crlone unlinkAll
    set result4 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    list "\
result0 = $result0\n\
1->2 1->3 2-3>= $result1\n\
unlink 2 = $result2\n\
1->2 = $result3\n\
unlink 1 = $result4\n\
"
} {{ result0 = {} {} {}
 1->2 1->3 2-3>= {{Owner Two} {Owner Three}} {{Owner One} {Owner Three} {Owner Three}} {{Owner One} {Owner Two} {Owner Two}}
 unlink 2 = {{Owner Three}} {} {{Owner One}}
 1->2 = {{Owner Three} {Owner Two}} {{Owner One}} {{Owner One}}
 unlink 1 = {} {} {}
 }}

######################################################################
####
#
test CrossRefList-4.3 {link two CrossLists many times, then unlink} {
    set ownerone [java::new ptolemy.kernel.util.NamedObj "Owner One"]
    set crlone [java::new ptolemy.kernel.util.CrossRefList $ownerone]
    set ownertwo [java::new ptolemy.kernel.util.NamedObj "Owner Two"]
    set crltwo [java::new ptolemy.kernel.util.CrossRefList $ownertwo]

    set result0 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crlone link $crltwo
    $crlone link $crltwo
    $crlone link $crltwo
    $crlone link $crltwo
    set result1 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crlone unlink $ownertwo
    $crlone unlink $ownertwo
    set result2 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crlone unlink $ownertwo
    $crlone unlink $ownertwo
    $crlone unlink $ownertwo
    $crlone unlink $ownertwo
    set result3 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

   list "\
result0 = $result0\n\
1->2 4 times = $result1\n\
unlink 1->2 twice = $result2\n\
unlink 1->2 4 times = $result3\n\
"
} {{ result0 = {} {} {}
 1->2 4 times = {{Owner Two} {Owner Two} {Owner Two} {Owner Two}} {{Owner One} {Owner One} {Owner One} {Owner One}} {}
 unlink 1->2 twice = {{Owner Two} {Owner Two}} {{Owner One} {Owner One}} {}
 unlink 1->2 4 times = {} {} {}
 }}

######################################################################
####
#
test CrossRefList-5.1 {link CrossRefLists, then use the copy constructor} {
    set ownerone [java::new ptolemy.kernel.util.NamedObj "Owner One"]
    set crlone [java::new ptolemy.kernel.util.CrossRefList $ownerone]
    set ownertwo [java::new ptolemy.kernel.util.NamedObj "Owner Two"]
    set crltwo [java::new ptolemy.kernel.util.CrossRefList $ownertwo]
    set ownerthree [java::new ptolemy.kernel.util.NamedObj "Owner Three"]
    set crlthree [java::new ptolemy.kernel.util.CrossRefList $ownerthree]

    set result0 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    $crlone link $crltwo
    $crlone link $crlthree
    set result1 [_testCrossRefListGetLinks $crlone $crltwo $crlthree]

    set ownerfour [java::new ptolemy.kernel.util.NamedObj "Owner Four"]
    set crlfour [java::new ptolemy.kernel.util.CrossRefList $ownerfour $crlone]
    set result2 [_testCrossRefListGetLinks $crlone $crltwo $crlthree $crlfour]

    $crlone unlinkAll
    set result3 [_testCrossRefListGetLinks $crlone $crltwo $crlthree $crlfour]

    $crlfour unlink $ownerthree
    set result4 [_testCrossRefListGetLinks $crlone $crltwo $crlthree $crlfour]

   list "\
result0 = $result0\n\
1->2 1->3 = $result1\n\
copy 1 to 4 = $result2\n\
unlink 1 = $result3\n\
unlink 4->3 = $result4\n\
"
} {{ result0 = {} {} {}
 1->2 1->3 = {{Owner Two} {Owner Three}} {{Owner One}} {{Owner One}}
 copy 1 to 4 = {{Owner Two} {Owner Three}} {{Owner One} {Owner Four}} {{Owner One} {Owner Four}} {{Owner Two} {Owner Three}}
 unlink 1 = {} {{Owner Four}} {{Owner Four}} {{Owner Two} {Owner Three}}
 unlink 4->3 = {} {{Owner Four}} {} {{Owner Two}}
 }}

######################################################################
####
#
test CrossRefList-5.2 {link CrossRefLists, then check ordering} {
    set a1 [java::new ptolemy.kernel.util.NamedObj A1]
    set c1 [java::new ptolemy.kernel.util.CrossRefList $a1]
    set a2 [java::new ptolemy.kernel.util.NamedObj A2]
    set c2 [java::new ptolemy.kernel.util.CrossRefList $a2]
    set a3 [java::new ptolemy.kernel.util.NamedObj A3]
    set c3 [java::new ptolemy.kernel.util.CrossRefList $a3]

    $c1 link $c2
    $c1 link $c3
    list [enumToNames [$c1 getContainers]] \
	   [[java::cast ptolemy.kernel.util.NamedObj [$c1 first]] getName] \
	   [[java::cast ptolemy.kernel.util.NamedObj [$c2 first]] getName] \
	   [[java::cast ptolemy.kernel.util.NamedObj [$c3 first]] getName]
    
} {{A2 A3} A2 A1 A1}

######################################################################
####
#
test CrossRefList-5.3 {link CrossRefList to itself} {
    set a1 [java::new ptolemy.kernel.util.NamedObj A1]
    set c1 [java::new ptolemy.kernel.util.CrossRefList $a1]

    catch {$c1 link $c1} errmsg
    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: CrossRefLink.link: Illegal self-link.}}
