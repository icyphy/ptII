/* Graphics (GR) domain director with synchronous/reactive semantics

 Copyright (c) 1998-2008 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.gro.kernel;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.domains.gr.kernel.GRReceiver;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// GRDirector

/**
 GR is a domain for displaying three-dimensional graphics in Ptolemy
 II.  GR is an untimed domain in where actors are connected in an
 acyclic directed graph.  Actors are fired according to a simple
 topological sort of the graph.  Nodes in the graph that have no
 descendants are assumed to be consumers of data produced by the rest
 of the model.

 <p>The basic idea behind the GR domain is to arrange geometry and
 transform actors in a directed acyclic graph to represent the location
 and orientation of objects in a scene. This topology of connected GR
 actors form what is commonly called a scene graph in computer graphics
 literature.  The GR director converts the GR scene graph into a Java3D
 representation for rendering on the computer screen.

 @see GRReceiver
 @see GROActor

 @author C. Fong, Steve Neuendorffer, Contributor: Christopher Hylands
 @version $Id: GRDirector.java 52048 2009-01-12 21:06:16Z eal $
 @since Ptolemy II 1.0
 @Pt.ProposedRating yellow (chf)
 @Pt.AcceptedRating yellow (vogel)
 */

public class TopDirector extends SDFDirector {

    public TopDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    } 
    /** Return a new receiver consistent with the GR domain.
    *
    *  @return A new GRReceiver.
    */
   public Receiver newReceiver() {
       return new SDFReceiver() {
           public void put(Token token) {
               try {
                   super.put(token);
               } catch (Exception e) {};
           }           
       };
   }
}
