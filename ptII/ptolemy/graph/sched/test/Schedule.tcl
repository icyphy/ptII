# Tests for the Schedule class.
#
# @Author: Shahrooz Shahparnia, Mingyung Ko
#
# $Id$
#
# @Copyright (c) 2001-2005 The Regents of the University of Maryland.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
#                                           PT_COPYRIGHT_VERSION_2
#                                           COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
#

######################################################################
####
#
test Schedule-1.1 {building a schedule that does not check for the type} {
    set s [java::new ptolemy.graph.sched.Schedule]
    set n1 [java::new ptolemy.graph.Node "n1"]
    set n2 [java::new ptolemy.graph.Node "n2"]
    set e1 [java::new ptolemy.graph.Edge $n1 $n2 "e1"]
    set s1 [java::new ptolemy.graph.sched.Schedule]
    set f1 [java::new ptolemy.graph.sched.Firing $n1]
    set f2 [java::new ptolemy.graph.sched.Firing $n2]
    $f2 setIterationCount 2
    set f3 [java::new ptolemy.graph.sched.Firing $e1]
    $s add $f1
    $s add $f2
    $s add $f3
    $s setIterationCount 3
    $s1 add $f1
    $s1 add $s
    $s1 add $f3
    list [$s1 toString]
} {{Execute Schedule{
Fire firing element n1
Execute Schedule{
Fire firing element n1
Fire firing element n2 2 times
Fire firing element (n1, n2, e1)
} 3 times
Fire firing element (n1, n2, e1)
}}}

######################################################################
####
#
test Schedule-1.2 {creating a graph that check for the element type} {
    catch { set c [java::call Class forName "ptolemy.graph.Node"] } msg
    set s2 [java::new ptolemy.graph.sched.Schedule $c]
    catch {$s2 add $s1} msg
    list $msg
} {{java.lang.RuntimeException: Attempt to add a non authorized firing element}}

######################################################################
####
#
test Schedule-1.3 {adding an authorized firing element to a checking graph} {
    catch { set c [java::call Class forName "ptolemy.graph.Node"] } msg
    set s3 [java::new ptolemy.graph.sched.Schedule $c]
    $s3 add $f1
    $s3 add $f2
    $s2 add $s3
    $s2 add $f1
    $s2 add 0 $f2
    list [$s2 toString]
} {{Execute Schedule{
Fire firing element n2 2 times
Execute Schedule{
Fire firing element n1
Fire firing element n2 2 times
}
Fire firing element n1
}}}


######################################################################
####
#
test Schedule-1.5 {test the size and get method} {
    list [$s2 size] [[$s2 get 1] toString]
} {3 {Execute Schedule{
Fire firing element n1
Fire firing element n2 2 times
}}}

######################################################################
####
#
test Schedule-1.6 {testing the iterator} {
    set i [$s2 iterator]
    set b1 [$i hasNext]
    set se1 [$i next]
    set b2 [$i hasNext]
    set se2 [$i next]
    set b3 [$i hasNext]
    set se3 [$i next]
    catch {set se4 [$i next]} msg
    list $b1 $b2 $b3 [$se1 toString] [$se2 toString] [$se3 toString] $msg
} {1 1 1 {Fire firing element n2 2 times} {Execute Schedule{
Fire firing element n1
Fire firing element n2 2 times
}} {Fire firing element n1} java.util.NoSuchElementException}

######################################################################
####
#
test Schedule-1.7 {testing the firing iterator} {
    set f1 [java::new ptolemy.graph.sched.Firing "n1"]
    set f2 [java::new ptolemy.graph.sched.Firing "n2"]
    set f3 [java::new ptolemy.graph.sched.Firing "n3"]
    set f4 [java::new ptolemy.graph.sched.Firing "n4"]
    set s4 [java::new ptolemy.graph.sched.Schedule]
    set s5 [java::new ptolemy.graph.sched.Schedule]
    $f1 setIterationCount 3
    $s4 add $f1
    $s4 add $f4
    $s4 setIterationCount 2
    $s5 add $f2
    $s5 add $f3
    $s5 add $s4
    set i [$s5 firingIterator]
    set b1 [$i hasNext]
    set se1 [$i next]
    set b2 [$i hasNext]
    set se2 [$i next]
    set b3 [$i hasNext]
    set se3 [$i next]
    set se4 [$i next]
    set se5 [$i next]
    set se6 [$i next]
    list [$se1 toString] [$se2 toString] [$se3 toString] [$se4 toString]\
    [$se5 toString] [$se6 toString]
} {{Fire firing element n2} {Fire firing element n3}\
{Fire firing element n1 3 times} {Fire firing element n4}\
{Fire firing element n1 3 times} {Fire firing element n4}}

######################################################################
####
#
test Schedule-1.8 {testing the firing element iterator} {
    set i [$s5 firingElementIterator]
    set se1 [$i next]
    set se2 [$i next]
    set se3 [$i next]
    set se4 [$i next]
    set se5 [$i next]
    set se6 [$i next]
    set se7 [$i next]
    set se8 [$i next]
    set se9 [$i next]
    set se10 [$i next]
    catch {set se11 [$i next]} msg
    list $se1 $se2 $se3 $se4 $se5 $se6 $se7 $se8 $se9 $se10 $msg
} {n2 n3 n1 n1 n1 n4 n1 n1 n1 n4\
{java.util.NoSuchElementException: No element to return.}}

######################################################################
####
#
test Schedule-1.9 {test removing a ScheduleElement} {
    $s2 remove 0
    list [$s2 toString]
} {{Execute Schedule{
Execute Schedule{
Fire firing element n1
Fire firing element n2 2 times
}
Fire firing element n1
}}}

######################################################################
####
#
test Schedule-2.1 {lexicalOrder() of (3 (2 B (3 A)) C)} {
  set ndA [java::new ptolemy.graph.Node ]
  set ndB [java::new ptolemy.graph.Node ]
  set ndC [java::new ptolemy.graph.Node ]
  set fr1 [java::new ptolemy.graph.sched.Firing $ndB]
  set fr2 [java::new ptolemy.graph.sched.Firing $ndA]
  set fr3 [java::new ptolemy.graph.sched.Firing $ndC]
  $fr2 setIterationCount 3
  set sc  [java::new ptolemy.graph.sched.Schedule]
  set sc1 [java::new ptolemy.graph.sched.Schedule]
  $sc  setIterationCount 3
  $sc  add $sc1
  $sc  add $fr3
  $sc1 setIterationCount 2
  $sc1 add $fr1
  $sc1 add $fr2
  set order [$sc lexicalOrder]
  set num_lex [$order size]
  set chk_lx1 [[$order get 0] equals $ndB]
  set chk_lx2 [[$order get 1] equals $ndA]
  set chk_lx3 [[$order get 2] equals $ndC]
  list $num_lex $chk_lx1 $chk_lx2 $chk_lx3
} {3 1 1 1}

######################################################################
####
#
test Schedule-2.2 {appearanceCount() and maxAppearanceCount() of (3 (2 B (3 A)) C B B A)} {
  # A new schedule is necessary since lexicalOrder() has been called. The result of
  # lexicalOrder() is cached and never changes after the first call. Therefore, a second
  # schedule is required.
  set sc  [java::new ptolemy.graph.sched.Schedule]
  $sc setIterationCount 3
  $sc add $sc1
  $sc add $fr3
  set fr4 [java::new ptolemy.graph.sched.Firing $ndB]
  set fr5 [java::new ptolemy.graph.sched.Firing $ndB]
  set fr6 [java::new ptolemy.graph.sched.Firing $ndA]
  $sc add $fr4
  $sc add $fr5
  $sc add $fr6
  set apA [$sc appearanceCount $ndA]
  set apB [$sc appearanceCount $ndB]
  set apC [$sc appearanceCount $ndC]
  set maxap [$sc maxAppearanceCount]
  list $apA $apB $apC $maxap
} {2 3 1 3}

######################################################################
####
#
test Schedule-2.3 {firings() of $ndB from the previous example } {
  set frsB [$sc firings $ndB]
  set frB1rtn [$frsB get 0]
  set frB2rtn [$frsB get 1]
  set frB3rtn [$frsB get 2]
  set frsA [$sc firings $ndA]
  set frA1rtn [$frsA get 0]
  set frA2rtn [$frsA get 1]
  list [$frB1rtn equals $fr1] [$frB2rtn equals $fr4] [$frB3rtn equals $fr5] [$frA1rtn equals $fr2] [$frA2rtn equals $fr6]
} {1 1 1 1 1}

######################################################################
####
#
test Schedule-2.4 {toParenthesisString()} {
  set nameA [java::new {java.lang.String String} A]
  set nameB [java::new {java.lang.String String} B]
  set nameC [java::new {java.lang.String String} C]
  set map [java::new java.util.HashMap]
  $map put $ndA $nameA
  $map put $ndB $nameB
  $map put $ndC $nameC
  set str1 [$sc toParenthesisString $map]
  set str2 [$sc toParenthesisString $map "."]
  list $str1 $str2
} {{(3 (2 B (3 A)) C B B A)} (3.(2.B.(3.A)).C.B.B.A)}
