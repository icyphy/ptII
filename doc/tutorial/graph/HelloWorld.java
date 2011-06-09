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
   <property name="HelloWorld" class="doc.tutorial.graph.HelloWorld">
     <property name="_location" class="ptolemy.kernel.util.Location" value="[100, 100]"/>
   </property>
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
     *  @throws IllegalActionException If the attribute cannot be contained by
     *   the specified container.
     *  @throws NameDuplicationException If the container already contains an
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
         *  @throws IllegalActionException If the attribute cannot be contained by
         *   the specified container.
         *  @throws NameDuplicationException If the container already contains an
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
        public void createEditor(NamedObj object, Frame parent) {
            MessageHandler.message("Hello World");
        }
    }
}
