/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
/* Demonstration of how to put an object in Ptolemy model that executes something. */
package doc.tutorial.graph;

import java.awt.Frame;

import ptolemy.actor.gui.EditorFactory;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

/** This class demonstrates how to create an attribute
 *  that appears visually in a model, and when the user
 *  double clicks on it, does something. In this simple
 *  demonstration, the class simply opens a dialog that
 *  says "Hello World".
 *  <p>
 *  To place this attribute in model, in Vergil, select
 *  Graph->Instantiate Attribute and specify for the class
 *  name: doc.tutorial.graph.HelloWorld. Alternatively,
 *  you can paste the following MoML code into the model:
 *  <pre>
 *  &lt;property name="HelloWorld" class="doc.tutorial.graph.HelloWorld"&gt;
 *    &lt;property name="_location" class="ptolemy.kernel.util.Location" value="[100, 100]"/&gt;
 *  &lt;/property&gt;
 *  </pre>
 *  In the above, the _location property is necessary to ensure
 *  that Vergil shows an icon for the HelloWorld attribute.
 *  It specifies the location on the canvas where that icon
 *  should be placed.
 *
 * @author Edward A. Lee
 * @version $Id$
 */
public class HelloWorld extends Attribute {

    /** Need this two-argument constructor to be able to instantiate
     *  the attribute in a model using MoML.
     *  @param container The containing model.
     *  @param name The name to give this attribute.
     *  @exception IllegalActionException If the attribute cannot be contained by
     *   the specified container.
     *  @exception NameDuplicationException If the container already contains an
     *   attribute with the proposed name.
     */
    public HelloWorld(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create an EditorFactory to handle double clicks.
        new DoubleClickHandler(this, "Calculate");
    }

    /** Inner class that is itself an Attribute which, when
     *  contained by a Ptolemy object, changes the default
     *  behavior of a double click on the attribute.
     *  A double click will cause the createEditor()
     *  method to be invoked.
     */
    public class DoubleClickHandler extends EditorFactory {

        /** Need this two-argument constructor to be able to instantiate
         *  the attribute in a model using MoML.
         *  @param container The containing model.
         *  @param name The name to give this attribute.
         *  @exception IllegalActionException If the attribute cannot be contained by
         *   the specified container.
         *  @exception NameDuplicationException If the container already contains an
         *   attribute with the proposed name.
         */
        public DoubleClickHandler(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** React to a double click on the specified object, which
         *  is displayed within the specified Frame.
         *  @param object The object on which the double click occurred.
         *  @param parent The parent Frame (top-level window).
         */
        @Override
        public void createEditor(NamedObj object, Frame parent) {
            MessageHandler.message("Hello World");
        }
    }
}
