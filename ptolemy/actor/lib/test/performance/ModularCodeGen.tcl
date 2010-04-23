# Common Tcl Procs to Test ModularCodeGen
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
#if {[string compare test [info procs modularCodeGenModel]] == 1} then {
    source PublisherCommon.tcl
#} {}

set composite ptolemy.cg.lib.ModularCodeGenTypedCompositeActor
#set composite ptolemy.actor.TypedCompositeActor

#test ModularCodeGen-1.2.1 {$composite} {
#    modularCodeGenModel 2 1 $composite
#} {{0.0 3.5 7.0 10.5 14.0}}

test ModularCodeGen-1.2.2 {Use $composite} {
    modularCodeGenModel 2 2 $composite
} {{0.0 7.0 14.0 21.0 28.0}}

#test ModularCodeGen-1.3.2 {Use $composite} {
#    modularCodeGenModel 3 2  $composite
#} {{0.0 5.0 10.0 15.0 20.0}}

# test ModularCodeGen-1.2.7 {Use $composite} {
#     modularCodeGenModel 2 7 $composite
# } {{0.0 3.5 7.0 10.5 14.0}}

# test ModularCodeGen-1.2.8 {Use $composite} {
#     modularCodeGenModel 2 8 $composite
# } {{0.0 3.5 7.0 10.5 14.0}}

# test ModularCodeGen-1.2.10 {Use $composite} {
#     modularCodeGenModel 2 10 $composite
# } {{0.0 3.5 7.0 10.5 14.0}}

# test ModularCodeGen-1.2.11 {Use $composite} {
#     modularCodeGenModel 2 11 $composite
# } {{0.0 3.5 7.0 10.5 14.0}}

# test ModularCodeGen-1.3.1 {Use $composite} {
#     modularCodeGenModel 3 1 $composite
# } {{0.0 5.0 10.0 15.0 20.0}}

# test ModularCodeGen-1.3.2 {Use $composite} {
#     modularCodeGenModel 3 2 $composite
# } {{0.0 5.0 10.0 15.0 20.0}}

#test ModularCodeGen-1.3.3 {Use $composite} {
#     modularCodeGenModel 3 3  $composite
#} {{0.0 45.0 90.0 135.0 180.0}}

# test ModularCodeGen-1.3.4 {Use $composite} {
#     modularCodeGenModel 3 4 $composite
# } {{0.0 5.0 10.0 15.0 20.0}}

# test ModularCodeGen-1.3.5 {Use $composite} {
#     modularCodeGenModel 3 5 $composite
# } {{0.0 5.0 10.0 15.0 20.0}}

#test ModularCodeGen-1.3.6 {Use $composite} {
#    modularCodeGenModel 3 6 $composite
#} {{0.0 5.0 10.0 15.0 20.0}}

# test ModularCodeGen-1.3.7 {Use $composite} {
#     modularCodeGenModel 3 7 $composite
# } {{0.0 5.0 10.0 15.0 20.0}}


#test ModularCodeGen-1.4.4 {Use $composite} {
#    modularCodeGenModel 4 4 $composite
#} {{0.0 416.0 832.0 1248.0 1664.0}}

#test ModularCodeGen-1.4.5 {Use $composite} {
#     modularCodeGenModel 4 5 $composite
# } {{0.0 6.5 13.0 19.5 26.0}}

# test ModularCodeGen-1.4.6 {Use $composite} {
#     modularCodeGenModel 4 6 $composite
# } {{0.0 5.0 10.0 15.0 20.0}}

# test ModularCodeGen-1.5.1 {Use $composite} {
#     modularCodeGenModel 5 1 $composite
# } {{0.0 5.0 10.0 15.0 20.0}}

# test ModularCodeGen-1.5.2 {Use $composite} {
#     modularCodeGenModel 5 2 $composite
# } {{0.0 8.0 16.0 24.0 32.0}}

# test ModularCodeGen-1.5.3 {Use $composite} {
#     modularCodeGenModel 5 3 $composite
# } {{0.0 8.0 16.0 24.0 32.0}}

# test ModularCodeGen-1.5.4 {Use $composite} {
#     modularCodeGenModel 5 4 $composite
# } {{0.0 8.0 16.0 24.0 32.0}}

# test ModularCodeGen-1.6.3 {Use $composite} {
#     modularCodeGenModel 6 3 $composite
# } {{0.0 9.5 19.0 28.5 38.0}}

# test ModularCodeGen-1.6.5 {Use $composite} {
#     modularCodeGenModel 6 4 $composite
# } {{0.0 9.5 19.0 28.5 38.0}}

#test ModularCodeGen-1.6.5 {Use $composite} {
#    modularCodeGenModel 6 5 $composite
#} {{0.0 9.5 19.0 28.5 38.0}}

