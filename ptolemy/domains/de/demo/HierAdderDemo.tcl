# Type system demo.
#
# @Author: Yuhong Xiong
#
# $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

#######################################################################
# This demo builds the following DE model:
#
#  |-------------------|
#  |(transparent)      |
#  |                   |
#  | Clock1--->Ramp1---+----> Adder----------------------->Plot
#  |                   |     ^                            
#  |-------------------|     |
#                            |                            
#  |-------------------|     |
#  |(opaque)           |     |
#  |                   |     |
#  | Clock2--->Ramp2---O-----|
#  |                   |
#  |-------------------|
#
# Ramp1 has initial value 0(int), step size 0.5(double), so its output
# type is Double; Ramp2 has value 2(int), step size 0(int) so its output
# type is Int. Ramp2 is used as a Const.
# The upper Clock1+Ramp1 block is transparent, so the port on its
# boundary is not involved in type checking. The lower block is a
# wormhole, its port will be resolved to Int.
#
# The demo shows the following:
# (1) Type resolution of polymorphic actor (Adder)
# (2) Ports with different types can be connected together:
#     The two ports connected with the Adder input have different types.
# (3) Run-time type conversion. The IntToken from Ramp2 is converted
#     to DoubleToken before sent to the Adder. (This is not shown by
#     a print message in the demo.)
# (4) Type resolution works across transparent and opaque composite
#     actors.
# (5) The adder in the polymorphic actor library works in the DE domain.
# (6) The use of the "pure signal" type Token. The output of the clock and
#     the trigger input of Ramp have type General (Token). 

package require java

proc printPortType {port} {
    set info "[$port getFullName]: ";

    set t [$port getResolvedType];
    if {$t == [java::null]} {
	lappend info "undeclared";
    } else {
	lappend info [$t getName];
    }

    puts stdout $info;
}

# Create the top level Composite Actor
set sys [java::new ptolemy.actor.TypedCompositeActor]
$sys setName DESystem

# Create directors and associate them with the top level composite actor.
set dir [java::new ptolemy.domains.de.kernel.DECQDirector DELocalDirector]
$sys setDirector $dir
set exec [java::new ptolemy.actor.Manager]
$sys setManager $exec

# Build Clock+Ramp block1
set block1 [java::new ptolemy.actor.TypedCompositeActor $sys Block1];
set clock1 [java::new ptolemy.domains.de.lib.DEClock $block1 Clock 1.0 1.0] 
set ramp1 [java::new {ptolemy.domains.de.lib.DERamp \
    ptolemy.actor.TypedCompositeActor String String String} $block1 Ramp 0 0.5]
set block1Out [java::new ptolemy.actor.TypedIOPort $block1 Output false true];

set clock1Out [java::field $clock1 output]
set ramp1In [java::field $ramp1 input]
set ramp1Out [java::field $ramp1 output]

set r1 [$block1 connect $clock1Out $ramp1In R1]
set r2 [$block1 connect $ramp1Out $block1Out R2]

# Build Clock+Ramp block2, not cloning from block1 since the actors
# don't have a clone method yet.
set block2 [java::new ptolemy.actor.TypedCompositeActor $sys Block2];
set clock2 [java::new ptolemy.domains.de.lib.DEClock $block2 Clock 1.0 1.0] 
set ramp2 [java::new {ptolemy.domains.de.lib.DERamp \
    ptolemy.actor.TypedCompositeActor String String String} $block2 Ramp 2 0]
set block2Out [java::new ptolemy.actor.TypedIOPort $block2 Output false true];

set clock2Out [java::field $clock2 output]
set ramp2In [java::field $ramp2 input]
set ramp2Out [java::field $ramp2 output]

set r3 [$block2 connect $clock2Out $ramp2In R3]
set r4 [$block2 connect $ramp2Out $block2Out R4]

# Build the system
set blockDir [java::new ptolemy.domains.de.kernel.DECQDirector BlockDir]
$block2 setDirector $blockDir

set add [java::new ptolemy.actor.lib.Add $sys Add]
set plot [java::new ptolemy.domains.de.lib.DEPlot $sys Plot]

# Identify the ports
set addIn [java::field $add input]
set addOut [java::field $add output]
set plotIn [java::field $plot input]

# Connect the ports
set r5 [$sys connect $block1Out $addIn R5]
set r6 [$sys connect $block2Out $addIn R6]
set r7 [$sys connect $addOut $plotIn R7]

# Set the stop time
$dir setStopTime 8.0

# before the Ramp actors set their declared type
puts stdout {-----------------------------------------}
puts stdout {Before the Ramp actors set declared type.}
printPortType $clock1Out;
printPortType $ramp1In;
printPortType $ramp1Out;
printPortType $block1Out;

printPortType $clock2Out;
printPortType $ramp2In;
printPortType $ramp2Out;
printPortType $block2Out;

printPortType $addIn;;
printPortType $addOut;;

printPortType $plotIn;;

# let Ramp actors evaluate parameter and set declared types.
$dir initialize
puts stdout {-----------------------------------------}
puts stdout {After the Ramp actors set declared type.}
printPortType $clock1Out;
printPortType $ramp1In;
printPortType $ramp1Out;
printPortType $block1Out;

printPortType $clock2Out;
printPortType $ramp2In;
printPortType $ramp2Out;
printPortType $block2Out;

printPortType $addIn;;
printPortType $addOut;;

printPortType $plotIn;;

# resolve types
$exec resolveTypes;
puts stdout {-----------------------------------------}
puts stdout {After type resolution.}
printPortType $clock1Out;
printPortType $ramp1In;
printPortType $ramp1Out;
printPortType $block1Out;

printPortType $clock2Out;
printPortType $ramp2In;
printPortType $ramp2Out;
printPortType $block2Out;

printPortType $addIn;;
printPortType $addOut;;

printPortType $plotIn;;

# Run it
puts stdout {-----------------------------------------}
puts stdout {Running...}
$exec startRun

