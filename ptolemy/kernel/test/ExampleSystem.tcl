# Create the example structure in figure 8 of the Ptolemy II design doc.
#
# @Author: Edward A. Lee
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


# This structure is the example in the kernel design document.

    # Create composite entities
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName E0
    set e3 [java::new ptolemy.kernel.CompositeEntity $e0 E3]
    set e4 [java::new ptolemy.kernel.CompositeEntity $e3 E4]
    set e7 [java::new ptolemy.kernel.CompositeEntity $e0 E7]
    set e10 [java::new ptolemy.kernel.CompositeEntity $e0 E10]

    # Create component entities.
    set e1 [java::new ptolemy.kernel.ComponentEntity $e4 E1]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e4 E2]
    set e5 [java::new ptolemy.kernel.ComponentEntity $e3 E5]
    set e6 [java::new ptolemy.kernel.ComponentEntity $e3 E6]
    set e8 [java::new ptolemy.kernel.ComponentEntity $e7 E8]
    set e9 [java::new ptolemy.kernel.ComponentEntity $e10 E9]

    # Create ports.
    set p0 [$e4 newPort P0]
    set p1 [$e1 newPort P1]
    set p2 [$e2 newPort P2]
    set p3 [$e2 newPort P3]
    set p4 [$e4 newPort P4]
    set p5 [$e5 newPort P5]
    set p6 [$e6 newPort P6]
    set p7 [$e3 newPort P7]
    set p8 [$e7 newPort P8]
    set p9 [$e8 newPort P9]
    set p10 [$e8 newPort P10]
    set p11 [$e7 newPort P11]
    set p12 [$e10 newPort P12]
    set p13 [$e10 newPort P13]
    set p14 [$e9 newPort P14]

    # Create links
    set r1 [$e4 connect $p1 $p0 R1]
    set r2 [$e4 connect $p1 $p4 R2]
    $p3 link $r2
    set r3 [$e4 connect $p1 $p2 R3]
    set r4 [$e3 connect $p4 $p7 R4]
    set r5 [$e3 connect $p4 $p5 R5]
    $e3 allowLevelCrossingConnect true
    set r6 [$e3 connect $p3 $p6 R6]
    set r7 [$e0 connect $p7 $p13 R7]
    set r8 [$e7 connect $p9 $p8 R8]
    set r9 [$e7 connect $p10 $p11 R9]
    set r10 [$e0 connect $p8 $p12 R10]
    set r11 [$e10 connect $p12 $p13 R11]
    set r12 [$e10 connect $p14 $p13 R12]
    $p11 link $r7
