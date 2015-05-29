/* Embedding of a Vert.x core.

   Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;

import ptolemy.actor.lib.jjs.HelperBase;

///////////////////////////////////////////////////////////////////
//// VertxHelperBase

/**
   A base class for helper classes that use an embedded Vert.x core.
   Creates only one static Vert.x core that is accessible to its subclasses. 
   
   @author Hokeun Kim
   @version $Id: VertxHelperBase.java 72160 2015-04-29 09:58:26Z hokeunkim $
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class VertxHelperBase extends HelperBase {

    /** Construct a helper for the specified JavaScript object.
     *  @param currentObj The JavaScript object that this is helping.
     */
    public VertxHelperBase(ScriptObjectMirror currentObj) {
	super(currentObj);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected fields                        ////
    
    /** Global instance of Vert.x core. */
    protected  static Vertx _vertx = VertxFactory.newVertx();

}
