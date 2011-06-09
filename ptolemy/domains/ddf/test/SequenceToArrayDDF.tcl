# Tests for SequenceToArray
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

######################################################################
####
#
test SequenceToArray-1.0 {Read SequenceToArrayDDF.xml, run it, change the const} {
    # DDF SequenceToArray bug submitted by Tomasz Zok
    # The problem is that if the length of the Const actor changes
    # between runs from three elements to two elements, then the output of 
    # the second run does not occur.

    set parser [java::new ptolemy.moml.MoMLParser]
    set model "SequenceToArrayDDF.xml"
    $parser purgeAllModelRecords
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile $model]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] myManager]
    $toplevel setManager $manager
    $manager execute
    set recorder [java::cast ptolemy.actor.lib.Recorder [$toplevel getEntity Recorder]]
    set r1 [listToStrings [$recorder getHistory 0]]

    set const [java::cast ptolemy.actor.lib.Const [$toplevel getEntity Const]]
    set value [getParameter $const value]
    $value setExpression "{\"aaa\", \"bbb\"}"
    $manager execute
    set r2 [listToStrings [$recorder getHistory 0]]
    list $r1 $r2
} {{{{"aaa", "bbb", "ccc"}}} {{{"aaa", "bbb"}}}} {Known Failure, see https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=326}

