# Test PubSub models with LazyComposite
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2008-2009 The Regents of the University of California.
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
if {[string compare test [info procs pubSubAggModel]] == 1} then {
    source PublisherCommon.tcl
} {}



test PubSubAgg-1.2.1 {Use PubSub} {
    pubSubAggLazyModel 2 1
} {{0.0 3.5 7.0 10.5 14.0}}

test PubSubAgg-1.2.2 {Use PubSub} {
    pubSubAggLazyModel 2 2
} {{0.0 3.5 7.0 10.5 14.0}}

test PubSubAgg-1.2.7 {Use PubSub} {
    pubSubAggLazyModel 2 7
} {{0.0 3.5 7.0 10.5 14.0}}

test PubSubAgg-1.2.8 {Use PubSub} {
    pubSubAggLazyModel 2 8
} {{0.0 3.5 7.0 10.5 14.0}}

test PubSubAgg-1.2.10 {Use PubSub} {
    pubSubAggLazyModel 2 10
} {{0.0 3.5 7.0 10.5 14.0}}

test PubSubAgg-1.2.11 {Use PubSub} {
    pubSubAggLazyModel 2 11
} {{0.0 3.5 7.0 10.5 14.0}}

test PubSubAgg-1.3.1 {Use PubSub} {
    pubSubAggLazyModel 3 1
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSubAgg-1.3.2 {Use PubSub} {
    pubSubAggLazyModel 3 2
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSubAgg-1.3.3 {Use PubSub} {
    pubSubAggLazyModel 3 3
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSubAgg-1.3.4 {Use PubSub} {
    pubSubAggLazyModel 3 4
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSubAgg-1.3.5 {Use PubSub} {
    pubSubAggLazyModel 3 5
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSubAgg-1.3.6 {Use PubSub} {
    pubSubAggLazyModel 3 6
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSubAgg-1.3.7 {Use PubSub} {
    pubSubAggLazyModel 3 7
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSubAgg-1.4.4 {Use PubSub} {
    pubSubAggLazyModel 4 4
} {{0.0 6.5 13.0 19.5 26.0}}

test PubSubAgg-1.4.5 {Use PubSub} {
    pubSubAggLazyModel 4 5
} {{0.0 6.5 13.0 19.5 26.0}}

test PubSubAgg-1.4.6 {Use PubSub} {
    pubSubAggLazyModel 4 6
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSubAgg-1.5.1 {Use PubSub} {
    pubSubAggLazyModel 5 1
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSubAgg-1.5.2 {Use PubSub} {
    pubSubAggLazyModel 5 2
} {{0.0 8.0 16.0 24.0 32.0}}

test PubSubAgg-1.5.3 {Use PubSub} {
    pubSubAggLazyModel 5 3
} {{0.0 8.0 16.0 24.0 32.0}}

test PubSubAgg-1.5.4 {Use PubSub} {
    pubSubAggLazyModel 5 4
} {{0.0 8.0 16.0 24.0 32.0}}

test PubSubAgg-1.6.3 {Use PubSub} {
    pubSubAggLazyModel 6 3
} {{0.0 9.5 19.0 28.5 38.0}}

test PubSubAgg-1.6.5 {Use PubSub} {
    pubSubAggLazyModel 6 4
} {{0.0 9.5 19.0 28.5 38.0}}

#test PubSubAgg-1.6.5 {Use PubSub} {
#    pubSubAggLazyModel 6 5
#} {{0.0 9.5 19.0 28.5 38.0}}

