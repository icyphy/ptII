/* An attribute that creates an editor pane to configure an entity
   contained by its container.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Ptolemy imports.
import java.awt.Component;
import java.util.Iterator;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// InsideEntityEditorPaneFactory
/**
This is an attribute that can create a pane (called a "configuration
widget") for interactively configuring an entity contained by its container.
That is, it adds a level of indirection, making it appear as if you
were configuring the container, when in fact you are configuring an
entity contained by the container.  If the container is not an instance
of CompositeEntity, or it does not contain any entities, then this behaves
just like the base class.  If the container contains more than one entity,
then only the first one encountered is configured.  To use this,
place an instance of this class (or a derived class) inside a Ptolemy II
object.  When the user double clicks on the icon for that object,
or selects Configure from the context menu, then a dialog is opened
containing the pane returned by createEditorPane().

@author Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
*/

public class InsideEntityEditorPaneFactory extends EditorPaneFactory {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public InsideEntityEditorPaneFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to look for an entity contained by
     *  the container and return a configurer for that.
     *  @return A new widget for configuring the container.
     */
    public Component createEditorPane() {
        NamedObj object = (NamedObj)getContainer();

        if (object instanceof CompositeEntity) {
            Iterator entities = ((CompositeEntity)object).entityList().iterator();
            if (entities.hasNext()) {
                object = (NamedObj)entities.next();
            }
        }
        return super.createEditorPane(object);
    }
}
