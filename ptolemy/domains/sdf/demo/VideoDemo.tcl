# SDF example using TclBlend
#
# @Author: Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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
#  Ramp->Delay->Print
#

# Create the top level Composite Actor
set sys [java::new ptolemy.actor.CompositeActor]
$sys setName SDFSystem

# Create directors and associate them with the top level composite actor.
set dir [java::new ptolemy.domains.sdf.kernel.SDFDirector SDFLocalDirector]
$sys setDirector $dir
set manager [java::new ptolemy.actor.Manager]
$sys setManager $manager
set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler]
$dir setScheduler $scheduler
$dir setScheduleValid false

set listener [java::new ptolemy.domains.sdf.lib.testlistener]
$manager addExecutionListener $listener

# Build the system
set source [java::new ptolemy.domains.sdf.lib.vq.ImageSequence $sys Source]
set part [java::new ptolemy.domains.sdf.lib.vq.ImagePartition $sys Part]
set encode [java::new ptolemy.domains.sdf.lib.vq.HTVQEncode $sys encode]
set decode [java::new ptolemy.domains.sdf.lib.vq.VQDecode $sys decode]
set unpart [java::new ptolemy.domains.sdf.lib.vq.ImageUnpartition $sys Unpart]
set display [java::new ptolemy.domains.sdf.lib.vq.ImageDisplay $sys Display]

# Identify the ports
set source_image [$source getPort image]
set part_image [$part getPort image]
set part_partition [$part getPort partition]
set encode_imagepart [$encode getPort imagepart]
set encode_index [$encode getPort index]
set decode_index [$decode getPort index]
set decode_imagepart [$decode getPort imagepart]
set unpart_image [$unpart getPort image]
set unpart_partition [$unpart getPort partition]
set display_image [$display getPort image]

# Connect the ports
set r1 [$sys connect $source_image $part_image R1]
set r2 [$sys connect $part_partition $encode_imagepart R2]
set r3 [$sys connect $encode_index $decode_index R3]
set r4 [$sys connect $decode_imagepart $unpart_partition R4]
set r5 [$sys connect $unpart_image $display_image R5]

#set debug ptolemy.domains.sdf.kernel.Debug
#set debugger [java::new ptolemy.domains.sdf.kernel.DebugListener]
#java::call $debug register $debugger

# Run it
set param [$dir getAttribute Iterations]
$param setToken [java::new {ptolemy.data.IntToken int} 60]
$manager run









