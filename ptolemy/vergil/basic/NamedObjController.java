/* The node controller for Ptolemy objects.

 Copyright (c) 1998-2013 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import diva.graph.GraphController;
import diva.graph.JGraph;
import ptolemy.actor.gui.Configuration;

///////////////////////////////////////////////////////////////////
//// NamedObjController

/**
 This class extends LocatableNodeController with an association
 with a configuration. The configuration is central to a Ptolemy GUI,
 and is used by derived classes to perform various functions such as
 opening models or their documentation. The class also contains an
 inner class the specifically supports accessing the documentation for
 a Ptolemy II object.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class NamedObjController extends LocatableNodeController {
    /** Create a node controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public NamedObjController(GraphController controller) {
        super(controller);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add hot keys to the actions in the given JGraph.
     *   It would be better that this method was added higher in the hierarchy. Now
     *   most controllers
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    public void addHotKeys(JGraph jgraph) {
    }

    /** Set the configuration.  This is used in derived classes to
     *  to open files (such as documentation).  The configuration is
     *  is important because it keeps track of which files are already
     *  open and ensures that there is only one editor operating on the
     *  file at any one time.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The configuration. */
    protected Configuration _configuration;
}
