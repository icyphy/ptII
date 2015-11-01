/* An object that can create a tableau for a model.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

import java.util.Iterator;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import diva.graph.GraphController;
import diva.gui.toolbox.MenuFactory;

/**
 * An object that can create a tableau for a model.
 *
 * <p>This class allows the configuration option "contextMenuFactory" to
 * be passed via the moml configuration.
 * A suitable MenuFactory is instantiated and returned by a call to the
 * createContextMenuFactory() method.
 *
 * @author Matthew Brooke
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 * @version $Id$
 */
public class ContextMenuFactoryCreator extends Attribute {

    /** Create a context menu factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ContextMenuFactoryCreator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create the KeplerContextMenuFactory class and return it.
     *
     * @param controller The GraphController in which to search for
     * a ContextMenuFactoryCreate attribute.
     * @return a KeplerContextMenuFactory
     */
    public MenuFactory createContextMenuFactory(GraphController controller) {
        MenuFactory kcmFactory = null;

        Iterator factories = attributeList(ContextMenuFactoryCreator.class)
                .iterator();

        while (factories.hasNext() && kcmFactory == null) {
            ContextMenuFactoryCreator factory = (ContextMenuFactoryCreator) factories
                    .next();
            kcmFactory = factory.createContextMenuFactory(controller);
        }
        return kcmFactory;
    }
}
