# Tests for the ArrayFIFOQueue class
#
# @Author: Edward A. Lee, Xiaojun Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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
####
#
# test ArrayFIFOQueue-1.1 {Get class information} {
#     # If anything changes, we want to know about it so we can write tests.
#     set n [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
#     list [getJavaInfo $n]
# } {{
#   class:         ptolemy.domains.sdf.kernel.ArrayFIFOQueue
#   fields:
#   methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait capacity elements full {get int} getContainer history historyCapacity historySize {previous int} {put java.lang.Object} {setCapacity int} {setHistoryCapacity int} size take
#   constructors:  ptolemy.domains.sdf.kernel.ArrayFIFOQueue {ptolemy.domains.sdf.kernel.ArrayFIFOQueue ptolemy.actor.Nameable} {ptolemy.domains.sdf.kernel.ArrayFIFOQueue ptolemy.domains.sdf.kernel.ArrayFIFOQueue}
#   properties:    class historyCapacity capacity container {{}}
#   superclass:    java.lang.Object
# }}
#
######################################################################
####
#
test ArrayFIFOQueue-2.1 {Construct an empty queue and check defaults} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    list [$queue getCapacity] \
            [_testEnums elements $queue] \
            [$queue isFull] \
            [_testEnums historyElements $queue] \
            [$queue getHistoryCapacity] \
            [$queue historySize] \
            [$queue size]
} {-1 {{}} 0 {{}} 0 0 0}

######################################################################
####
#
test ArrayFIFOQueue-2.2 {Construct an empty queue and attempt two gets and a take} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    catch { [$queue get 0] } msg1
    catch { [$queue get -1] } msg2
    catch { [$queue take] } msg3
    list $msg1 $msg2 $msg3
} {{java.util.NoSuchElementException: No object at offset 0 in the FIFOQueue.} {java.util.NoSuchElementException: No object at offset -1 in the FIFOQueue.} {java.util.NoSuchElementException: The FIFOQueue is empty!}}

######################################################################
####
#
test ArrayFIFOQueue-2.3 {Construct an empty queue with a container} {
    set container [java::new ptolemy.kernel.util.NamedObj "parent"]
    set queue [java::new {ptolemy.domains.sdf.kernel.ArrayFIFOQueue ptolemy.kernel.util.Nameable} $container]
    [$queue getContainer] getName
} {parent}

######################################################################
####
#
test ArrayFIFOQueue-2.4 {Set container} {
    set container [java::new ptolemy.kernel.util.NamedObj "parent"]
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    $queue setContainer $container
    [$queue getContainer] getName
} {parent}

######################################################################
####
#
test ArrayFIFOQueue-2.4 {Set container constructor with size} {
    set container [java::new ptolemy.kernel.util.NamedObj "parent"]
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue \
	    $container 3]
    [$queue getContainer] getName
} {parent}

######################################################################
####
#
test ArrayFIFOQueue-2.5 {INFINITE_CAPACITY} {
    java::field ptolemy.domains.sdf.kernel.ArrayFIFOQueue INFINITE_CAPACITY
} {-1}


    ######################################################################
    ######################################################################
    # The following named objects are used throughout the rest of the tests.
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
    set n4 [java::new ptolemy.kernel.util.NamedObj "n4"]
    set n5 [java::new ptolemy.kernel.util.NamedObj "n5"]

######################################################################
####
#
test ArrayFIFOQueue-3.1 {Put data on a queue} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    $queue {put java.lang.Object} $n1
    $queue {put java.lang.Object} $n2
    $queue {put java.lang.Object} $n3
    $queue {put java.lang.Object} $n4
    $queue {put java.lang.Object} $n5
    _testEnums elements $queue
} {{n1 n2 n3 n4 n5}}

######################################################################
####
#
test ArrayFIFOQueue-3.2 {Get individual items} {

    catch {[$queue get -1]} s0
    set a0 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 0]] \
            getName]
    set a1 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 1]] \
            getName]
    set a2 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 2]] \
            getName]
    set a3 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 3]] \
            getName]
    set a4 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 4]] \
            getName]
    catch {[$queue get 5]} s1
    list $s0 $a0 $a1 $a2 $a3 $a4 $s1 [$queue size] [$queue isFull]
} {{java.util.NoSuchElementException: No object at offset -1 in the FIFOQueue.} n1 n2 n3 n4 n5 {java.util.NoSuchElementException: No object at offset 5 in the FIFOQueue.} 5 0}

######################################################################
####
#
test ArrayFIFOQueue-3.3 {Take items} {
    list \
	    [[java::cast ptolemy.kernel.util.NamedObj [$queue take]] \
            getName] \
            [_testEnums elements $queue]
} {n1 {{n2 n3 n4 n5}}}

######################################################################
####
#
test ArrayFIFOQueue-3.4.1 {Put array of data on a queue} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    set array [java::new {Object[]} {5} {}]
    $array set 0 $n1
    $array set 1 $n2
    $array set 2 $n3
    $array set 3 $n4
    $array set 4 $n5
    $queue putArray $array
    _testEnums elements $queue
} {{n1 n2 n3 n4 n5}}

######################################################################
####
#
test ArrayFIFOQueue-3.4.2 {Put array of data on a queue} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    set array [java::new {Object[]} {5} {}]
    $array set 0 $n1
    $array set 1 $n2
    $array set 2 $n3
    $array set 3 $n4
    $array set 4 $n5
    $queue putArray $array 5
    _testEnums elements $queue
} {{n1 n2 n3 n4 n5}}

######################################################################
####
#
test ArrayFIFOQueue-3.5 {Get individual items} {

    catch {[$queue get -1]} s0
    set a0 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 0]] \
            getName]
    set a1 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 1]] \
            getName]
    set a2 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 2]] \
            getName]
    set a3 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 3]] \
            getName]
    set a4 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 4]] \
            getName]
    catch {[$queue get 5]} s1
    list $s0 $a0 $a1 $a2 $a3 $a4 $s1 [$queue size] [$queue isFull]
} {{java.util.NoSuchElementException: No object at offset -1 in the FIFOQueue.} n1 n2 n3 n4 n5 {java.util.NoSuchElementException: No object at offset 5 in the FIFOQueue.} 5 0}

######################################################################
####
#
test ArrayFIFOQueue-3.6.1 {Takearray items} {
    set array [java::new {Object[]} {5} {}]
    $queue takeArray $array
    list [jdkPrintArray $array] [_testEnums elements $queue]
} {{{ptolemy.kernel.util.NamedObj {.n1}} {ptolemy.kernel.util.NamedObj {.n2}} {ptolemy.kernel.util.NamedObj {.n3}} {ptolemy.kernel.util.NamedObj {.n4}} {ptolemy.kernel.util.NamedObj {.n5}}} {{}}}

######################################################################
####
#
test ArrayFIFOQueue-3.6.2 {Takearray items} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    set array [java::new {Object[]} {5} {}]
    $array set 0 $n1
    $array set 1 $n2
    $array set 2 $n3
    $array set 3 $n4
    $array set 4 $n5
    $queue putArray $array 5
    set array [java::new {Object[]} {5} {}]
    $queue takeArray $array 5
    list [jdkPrintArray $array] [_testEnums elements $queue]
} {{{ptolemy.kernel.util.NamedObj {.n1}} {ptolemy.kernel.util.NamedObj {.n2}} {ptolemy.kernel.util.NamedObj {.n3}} {ptolemy.kernel.util.NamedObj {.n4}} {ptolemy.kernel.util.NamedObj {.n5}}} {{}}}

######################################################################
####
#
test ArrayFIFOQueue-3.7.1 {Test getting the data in the queue when the data wraps around the circular array.} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    set array [java::new {Object[]} {5} {}]
    $array set 0 $n1
    $array set 1 $n2
    $array set 2 $n3
    $array set 3 $n4
    $array set 4 $n5
    $queue putArray $array
    $queue take
    $queue take
    $queue put $n1
    $queue put $n2
    _testEnums elements $queue
} {{n3 n4 n5 n1 n2}}

######################################################################
####
#
test ArrayFIFOQueue-3.7.2 {Test getting the data in the queue when the data wraps around the circular array.} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    set array [java::new {Object[]} {5} {}]
    $array set 0 $n1
    $array set 1 $n2
    $array set 2 $n3
    $array set 3 $n4
    $array set 4 $n5
    $queue putArray $array 5
    $queue take
    $queue take
    $queue put $n1
    $queue put $n2
    _testEnums elements $queue
} {{n3 n4 n5 n1 n2}}

######################################################################
####
#
test ArrayFIFOQueue-4.1 {Inserting elements into a queue of bounded size} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    $queue setCapacity 3
    list \
            [$queue {put java.lang.Object} $n1] \
            [$queue {put java.lang.Object} $n2] \
            [$queue {put java.lang.Object} $n3] \
            [$queue {put java.lang.Object} $n4] \
            [_testEnums elements $queue]
} {1 1 1 0 {{n1 n2 n3}}}

######################################################################
####
#
test ArrayFIFOQueue-4.1 {Inserting elements into a queue of bounded size \
    using size constructor} {
set queue [java::new {ptolemy.domains.sdf.kernel.ArrayFIFOQueue int} 3]
    list \
            [$queue {put java.lang.Object} $n1] \
            [$queue {put java.lang.Object} $n2] \
            [$queue {put java.lang.Object} $n3] \
            [$queue {put java.lang.Object} $n4] \
            [_testEnums elements $queue]
} {1 1 1 0 {{n1 n2 n3}}}

######################################################################
####
#
test ArrayFIFOQueue-4.2 {Take data off a queue with bounded capacity} {
    $queue take
    list \
            [$queue {put java.lang.Object} $n5] \
            [_testEnums elements $queue]
} {1 {{n2 n3 n5}}}

######################################################################
####
#
test ArrayFIFOQueue-4.3 {resize a bounded capacity queue} {
    catch {[$queue setCapacity 2]} msg1
    $queue setCapacity 4
    set r1 [$queue put $n4]
    set r2 [$queue put $n4]
    list $msg1 $r1 $r2
} {{ptolemy.kernel.util.IllegalActionException: Queue contains more elements than the proposed capacity.} 1 0}

######################################################################
####
#
test ArrayFIFOQueue-4.4.1 {Put array of data on a queue of bounded capacity} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    $queue setCapacity 4
    set array0 [java::new {Object[]} {4} {}]
    set array1 [java::new {Object[]} {5} {}]
    $array0 set 0 $n1
    $array0 set 1 $n2
    $array0 set 2 $n3
    $array0 set 3 $n4
    $array1 set 0 $n1
    $array1 set 1 $n2
    $array1 set 2 $n3
    $array1 set 3 $n4
    $array1 set 4 $n5
    set r1 [$queue putArray $array1]
    set r2 [$queue putArray $array0]
    set r3 [$queue putArray $array0]
    set r4 [$queue put $n4]
    list $r1 $r2 $r3 $r4 [_testEnums elements $queue]
} {0 1 0 0 {{n1 n2 n3 n4}}}

######################################################################
####
#
test ArrayFIFOQueue-4.4.2 {Put array of data on a queue of bounded capacity} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    $queue setCapacity 4
    set array0 [java::new {Object[]} {4} {}]
    set array1 [java::new {Object[]} {5} {}]
    $array0 set 0 $n1
    $array0 set 1 $n2
    $array0 set 2 $n3
    $array0 set 3 $n4
    $array1 set 0 $n1
    $array1 set 1 $n2
    $array1 set 2 $n3
    $array1 set 3 $n4
    $array1 set 4 $n5
    set r1 [$queue putArray $array1 5]
    set r2 [$queue putArray $array0 4]
    set r3 [$queue putArray $array0 4]
    set r4 [$queue put $n4]
    list $r1 $r2 $r3 $r4 [_testEnums elements $queue]
} {0 1 0 0 {{n1 n2 n3 n4}}}

######################################################################
####
#
test ArrayFIFOQueue-4.5 {Get individual items from a queue of bounded capacity} {
    catch {[$queue get -1]} s0
    set a0 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 0]] \
            getName]
    set a1 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 1]] \
            getName]
    set a2 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 2]] \
            getName]
    set a3 [[java::cast ptolemy.kernel.util.NamedObj [$queue get 3]] \
            getName]
    catch {[$queue get 4]} s1
    list $s0 $a0 $a1 $a2 $a3 $s1 [$queue size] [$queue isFull]
} {{java.util.NoSuchElementException: No object at offset -1 in the FIFOQueue.} n1 n2 n3 n4 {java.util.NoSuchElementException: No object at offset 4 in the FIFOQueue.} 4 1}

######################################################################
####
#
test ArrayFIFOQueue-4.6.1 {Takearray items} {
    set array1 [java::new {Object[]} {5} {}]
    set array2 [java::new {Object[]} {4} {}]
    catch {[$queue takeArray $array1]} s1
    $queue takeArray $array2
    $queue put $n1
    catch {[$queue takeArray $array1]} s2
    list $s1 [jdkPrintArray $array2] $s1 [_testEnums elements $queue]
} {{java.util.NoSuchElementException: The FIFOQueue does not contain enough elements!} {{ptolemy.kernel.util.NamedObj {.n1}} {ptolemy.kernel.util.NamedObj {.n2}} {ptolemy.kernel.util.NamedObj {.n3}} {ptolemy.kernel.util.NamedObj {.n4}}} {java.util.NoSuchElementException: The FIFOQueue does not contain enough elements!} n1}

######################################################################
####
#
test ArrayFIFOQueue-4.6.2 {Takearray items} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    $queue setCapacity 4
    set array0 [java::new {Object[]} {4} {}]
    set array1 [java::new {Object[]} {5} {}]
    $array0 set 0 $n1
    $array0 set 1 $n2
    $array0 set 2 $n3
    $array0 set 3 $n4
    $array1 set 0 $n1
    $array1 set 1 $n2
    $array1 set 2 $n3
    $array1 set 3 $n4
    $array1 set 4 $n5
    set r1 [$queue putArray $array1 5]
    set r2 [$queue putArray $array0 4]
    set r3 [$queue putArray $array0 4]
    set r4 [$queue put $n4]
    set array1 [java::new {Object[]} {5} {}]
    set array2 [java::new {Object[]} {4} {}]
    catch {[$queue takeArray $array1 5]} s1
    $queue takeArray $array2 4
    $queue put $n1
    catch {[$queue takeArray $array1 5]} s2
    list $s1 [jdkPrintArray $array2] $s1 [_testEnums elements $queue]
} {{java.util.NoSuchElementException: The FIFOQueue does not contain enough elements!} {{ptolemy.kernel.util.NamedObj {.n1}} {ptolemy.kernel.util.NamedObj {.n2}} {ptolemy.kernel.util.NamedObj {.n3}} {ptolemy.kernel.util.NamedObj {.n4}}} {java.util.NoSuchElementException: The FIFOQueue does not contain enough elements!} n1}

######################################################################
####
#
test ArrayFIFOQueue-4.7.1 {Test getting the data in the queue when the data wraps around the circular array.} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    set array [java::new {Object[]} {5} {}]
    $array set 0 $n1
    $array set 1 $n2
    $array set 2 $n3
    $array set 3 $n4
    $array set 4 $n5
    $queue putArray $array
    $queue take
    $queue take
    $queue put $n1
    $queue put $n2
    _testEnums elements $queue
} {{n3 n4 n5 n1 n2}}

######################################################################
####
#
test ArrayFIFOQueue-4.7.2 {Test getting the data in the queue when the data wraps around the circular array.} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    set array [java::new {Object[]} {5} {}]
    $array set 0 $n1
    $array set 1 $n2
    $array set 2 $n3
    $array set 3 $n4
    $array set 4 $n5
    $queue putArray $array 5
    $queue take
    $queue take
    $queue put $n1
    $queue put $n2
    _testEnums elements $queue
} {{n3 n4 n5 n1 n2}}

######################################################################
####
#
test ArrayFIFOQueue-4.8 {resize a queue to less than zero size} {
    catch {[$queue setCapacity -2]} msg1
    list $msg1
} {{ptolemy.kernel.util.IllegalActionException: Queue Capacity cannot be negative}}

######################################################################
####
#
test ArrayFIFOQueue-4.9 {resize a bounded capacity queue to have infinite capacity} {
    $queue setCapacity -1
    set r1 [$queue put $n4]
    set r2 [$queue put $n4]
    list $r1 $r2 [_testEnums elements $queue]
} {1 1 {{n3 n4 n5 n1 n2 n4 n4}}}

######################################################################
####
#
test ArrayFIFOQueue-5.1 {Test history} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    $queue setHistoryCapacity -1
    $queue {put java.lang.Object} $n1
    $queue {put java.lang.Object} $n2
    $queue {put java.lang.Object} $n3
    $queue {put java.lang.Object} $n4
    $queue {put java.lang.Object} $n5
    $queue take
    $queue take
    $queue take
    list [$queue historySize] [_testEnums elements $queue] [_testEnums historyElements $queue]
} {3 {{n4 n5}} {{n1 n2 n3}}}

######################################################################
####
#
test ArrayFIFOQueue-5.2 {Get elements from history queue} {
    list \
	    [[java::cast ptolemy.kernel.util.NamedObj [$queue get -1]] \
            getName] \
	    [[java::cast ptolemy.kernel.util.NamedObj [$queue get -2]] \
            getName] \
	    [[java::cast ptolemy.kernel.util.NamedObj [$queue get -3]] \
            getName]
} {n3 n2 n1}

######################################################################
####
#
test ArrayFIFOQueue-5.3 {Test get elements from history queue with error} {
    catch {[$queue get -4]} msg
    list $msg
} {{java.util.NoSuchElementException: No object at offset -4 in the FIFOQueue.}}

######################################################################
####
#
test ArrayFIFOQueue-6.1 {Test history with bounded capacity} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    $queue setHistoryCapacity 2
    $queue {put java.lang.Object} $n1
    $queue {put java.lang.Object} $n2
    $queue {put java.lang.Object} $n3
    $queue {put java.lang.Object} $n4
    $queue {put java.lang.Object} $n5
    $queue take
    $queue take
    $queue take
    list [_testEnums elements $queue] [_testEnums historyElements $queue]
} {{{n4 n5}} {{n2 n3}}}

######################################################################
####
#
test ArrayFIFOQueue-6.2 {Test clear history queue} {
    set queue [java::new ptolemy.domains.sdf.kernel.ArrayFIFOQueue]
    set container [java::new ptolemy.kernel.util.NamedObj QueueContainer]
    $queue setContainer $container
    $queue {put java.lang.Object} $n1
    $queue {put java.lang.Object} $n2
    $queue setHistoryCapacity 2
    $queue take
    $queue take
    set newqueue [java::cast ptolemy.domains.sdf.kernel.ArrayFIFOQueue [$queue clone]]
    $queue setHistoryCapacity 0
    catch {[$queue get 0]} msg1
    catch {[$queue get -1]} msg2
    list [_testEnums historyElements $newqueue] $msg1 $msg2
} {{{n1 n2}} {java.util.NoSuchElementException: No object at offset 0 in the FIFOQueue contained by .QueueContainer} {java.util.NoSuchElementException: No object at offset -1 in the FIFOQueue contained by .QueueContainer}}
