# Tests for the FIFOQueue class
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
# test FIFOQueue-1.1 {Get class information} {
#     # If anything changes, we want to know about it so we can write tests.
#     set n [java::new ptolemy.actor.util.FIFOQueue]
#     list [getJavaInfo $n]
# } {{
#   class:         ptolemy.actor.util.FIFOQueue
#   fields:
#   methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait capacity elements full {get int} getContainer history historyCapacity historySize {previous int} {put java.lang.Object} {setCapacity int} {setHistoryCapacity int} size take
#   constructors:  ptolemy.actor.util.FIFOQueue {ptolemy.actor.util.FIFOQueue ptolemy.actor.Nameable} {ptolemy.actor.util.FIFOQueue ptolemy.actor.util.FIFOQueue}
#   properties:    class historyCapacity capacity container {{}}
#   superclass:    java.lang.Object
# }}
#
######################################################################
####
#
test FIFOQueue-2.1 {Construct an empty queue and check defaults} {
    set queue [java::new ptolemy.actor.util.FIFOQueue]
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
test FIFOQueue-2.2 {Construct an empty queue and attempt two gets and a take} {
    set queue [java::new ptolemy.actor.util.FIFOQueue]
    catch { [$queue get 0] } msg1
    catch { [$queue get -1] } msg2
    catch { [$queue take] } msg3
    list $msg1 $msg2 $msg3
} {{java.util.NoSuchElementException: No object at offset 0 in the FIFOQueue.} {java.util.NoSuchElementException: No object at offset -1 in the FIFOQueue.} {java.util.NoSuchElementException: The FIFOQueue is empty!}}

######################################################################
####
#
test FIFOQueue-2.3 {Construct an empty queue with a container} {
    set container [java::new ptolemy.kernel.util.NamedObj "parent"]
    set queue [java::new {ptolemy.actor.util.FIFOQueue ptolemy.kernel.util.Nameable} $container]
    [$queue getContainer] getName
} {parent}

######################################################################
####
#
test FIFOQueue-2.4 {Set container} {
    set container [java::new ptolemy.kernel.util.NamedObj "parent"]
    set queue [java::new ptolemy.actor.util.FIFOQueue]
    $queue setContainer $container
    [$queue getContainer] getName
} {parent}


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
test FIFOQueue-3.1 {Put data on a queue} {
    set queue [java::new ptolemy.actor.util.FIFOQueue]
    $queue put $n1
    $queue put $n2
    $queue put $n3
    $queue put $n4
    $queue put $n5
    _testEnums elements $queue
} {{n1 n2 n3 n4 n5}}

######################################################################
####
#
test FIFOQueue-3.2 {Get individual items} {
    list \
	    [[java::cast ptolemy.kernel.util.NamedObj [$queue get 1]] \
            getName] \
            [$queue size] \
            [$queue isFull]
} {n2 5 0}

######################################################################
####
#
test FIFOQueue-3.3 {Take items} {
    list \
	    [[java::cast ptolemy.kernel.util.NamedObj [$queue take]] \
            getName] \
            [_testEnums elements $queue]
} {n1 {{n2 n3 n4 n5}}}

######################################################################
####
#
test FIFOQueue-4.1 {Put data on a queue with bounded capacity} {
    set queue [java::new ptolemy.actor.util.FIFOQueue]
    $queue setCapacity 3
    list \
            [$queue put $n1] \
            [$queue put $n2] \
            [$queue put $n3] \
            [$queue put $n4] \
            [_testEnums elements $queue]
} {1 1 1 0 {{n1 n2 n3}}}

######################################################################
####
#
test FIFOQueue-4.2 {Take data off a queue with bounded capacity} {
    $queue take
    list \
            [$queue put $n5] \
            [_testEnums elements $queue]
} {1 {{n2 n3 n5}}}

######################################################################
####
#
test FIFOQueue-5.1 {Test history} {
    set queue [java::new ptolemy.actor.util.FIFOQueue]
    $queue setHistoryCapacity -1
    $queue put $n1
    $queue put $n2
    $queue put $n3
    $queue put $n4
    $queue put $n5
    $queue take
    $queue take
    $queue take
    list [$queue historySize] [_testEnums elements $queue] [_testEnums historyElements $queue]
} {3 {{n4 n5}} {{n1 n2 n3}}}

######################################################################
####
#
test FIFOQueue-5.2 {Get elements from history queue} {
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
test FIFOQueue-5.3 {Test get elements from history queue with error} {
    catch {[$queue get -4]} msg
    list $msg
} {{java.util.NoSuchElementException: No object at offset -4 in the FIFOQueue.}}

######################################################################
####
#
test FIFOQueue-6.1 {Test history with bounded capacity} {
    set queue [java::new ptolemy.actor.util.FIFOQueue]
    $queue setHistoryCapacity 2
    $queue put $n1
    $queue put $n2
    $queue put $n3
    $queue put $n4
    $queue put $n5
    $queue take
    $queue take
    $queue take
    list [_testEnums elements $queue] [_testEnums historyElements $queue]
} {{{n4 n5}} {{n2 n3}}}

######################################################################
####
#
test FIFOQueue-6.2 {Test clear history queue} {
    set queue [java::new ptolemy.actor.util.FIFOQueue]
    set container [java::new ptolemy.kernel.util.NamedObj QueueContainer]
    $queue setContainer $container
    $queue put $n1
    $queue put $n2
    $queue setHistoryCapacity 2
    $queue take
    $queue take
    set newqueue [java::cast ptolemy.actor.util.FIFOQueue [$queue clone]]
    $queue setHistoryCapacity 0
    catch {[$queue get 0]} msg1
    catch {[$queue get -1]} msg2
    list [_testEnums historyElements $newqueue] $msg1 $msg2
} {{{n1 n2}} {java.util.NoSuchElementException: No object at offset 0 in the FIFOQueue contained by .QueueContainer} {java.util.NoSuchElementException: No object at offset -1 in the FIFOQueue contained by .QueueContainer}}
