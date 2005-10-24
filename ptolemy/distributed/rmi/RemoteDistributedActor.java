/* A DistributedActor is a distributed executable entity that is accessed
 via RMI.

 @Copyright (c) 2005 The Regents of Aalborg University.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
 HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.distributed.rmi;

import java.rmi.Remote;

import ptolemy.distributed.common.DistributedActor;

//////////////////////////////////////////////////////////////////////////
//// RemoteDistributedActor

/**
 A DistributedActor is a distributed executable entity that is accessed
 via RMI. This interface basically extends DistributedActor and
 java.rmi.Remote.
 The Remote interface serves to identify interfaces whose methods may be
 invoked from a non-local virtual machine.

 @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.distributed.rmi.DistributedActorWrapper
 @see ptolemy.actor.Actor
 @see ptolemy.actor.Executable
 */
public interface RemoteDistributedActor extends DistributedActor, Remote {
}
