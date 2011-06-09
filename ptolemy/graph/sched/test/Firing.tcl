# Tests for the Firing class.
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
test Firing-1.1 {building an example} {
    set f [java::new ptolemy.graph.sched.Firing]
    list [$f toString]
} {{Fire firing element null}}

######################################################################
####
#
test Firing-1.2 {setting an element} {
    set n1 [java::new ptolemy.graph.Node "Test Node 1"]
    set n2 [java::new ptolemy.graph.Node "Test Node 2"]
    $f setFiringElement $n1
    list [$f toString]
} {{Fire firing element Test Node 1}}

######################################################################
####
#
test Firing-1.3 {setting another element} {
    set edge [java::new ptolemy.graph.Edge $n1 $n2 "Test Edge"]
    $f setFiringElement $edge
    list [$f toString]
} {{Fire firing element (Test Node 1, Test Node 2, Test Edge)}}

######################################################################
####
#
test Firing-1.4 {get element} {
    set edge [$f getFiringElement]
    list [$edge toString]
} {{(Test Node 1, Test Node 2, Test Edge)}}

######################################################################
####
#
test Firing-1.5 {building an example} {
    catch { set c [java::call Class forName "ptolemy.graph.Node"] } msg
    set f1 [java::new ptolemy.graph.sched.Firing $c]
    list [$f1 toString]
} {{Fire firing element null}}

######################################################################
####
#
test Firing-1.6 {setting an element} {
    $f1 setFiringElement $n1
    list [$f1 toString]
} {{Fire firing element Test Node 1}}

######################################################################
####
#
test Firing-1.7 {setting another element} {
    catch {$f1 setFiringElement $edge} msg
    list $msg
} {{java.lang.RuntimeException: Attempt to add a non authorized firing element}}

######################################################################
####
#
test Firing-1.8 {get element} {
    set node [$f1 getFiringElement]
    list [[$node getClass] toString]
} {{class ptolemy.graph.Node}}

######################################################################
####
#
test Firing-1.9 {test the firing iterator} {
    set i [$f1 firingIterator]
    set b1 [$i hasNext]
    catch {set e1 [$i next]} msg1
    set b2 [$i hasNext]
    catch {set e2 [$i next]} msg2
    list $b1 $b2 [$e1 toString] $msg2
} {1 0 {Fire firing element Test Node 1} java.util.NoSuchElementException}

######################################################################
####
#
test Firing-1.10 {test the firing element iterator} {
    set i [$f1 firingElementIterator]
    $f1 setIterationCount 3
    catch {set b1 [$i hasNext]} msg1

    set i [$f1 firingElementIterator]
    set b1 [$i hasNext]
    catch {set e1 [$i next]} msg2
    set b2 [$i hasNext]
    catch {set e2 [$i next]} msg3
    list $b1 $b2 [$e1 toString] $msg1 [$msg2 toString] [$msg3 toString]
} {1 1 {Test Node 1} {java.util.ConcurrentModificationException: Schedule structure\
changed while iterator is active.} {Test Node 1} {Test Node 1}}

######################################################################
####
#
test Firing-1.11 {toParenthesisString()} {
    set node [java::new ptolemy.graph.Node]
    set name [java::new {java.lang.String String} A]
    set map  [java::new java.util.HashMap]
    $map put $node $name
    set firing [java::new ptolemy.graph.sched.Firing $node]
    set str1 [$firing toParenthesisString $map]
    $firing setIterationCount 3
    set str2 [$firing toParenthesisString $map]
    set str3 [$firing toParenthesisString $map "--"]
    list $str1 $str2 $str3
} {A {(3 A)} (3--A)}

