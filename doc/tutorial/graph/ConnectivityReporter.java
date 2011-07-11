package doc.tutorial.graph;

import java.awt.Frame;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

/** This class demonstrates how to create an attribute
 *  that appears visually in a model, and when the user
 *  double clicks on it, examines the structure of the model
 *  that contains it and reports connectivity information.
 *  <p>
 *  To place this attribute in model, in Vergil, select
 *  Graph->Instantiate Attribute and specify for the class
 *  name: doc.tutorial.graph.ConnectivityReporter. Alternatively,
 *  you can paste the following MoML code into the model:
 *  <pre>
   <property name="HelloWorld" class="doc.tutorial.graph.ConnectivityReporter">
     <property name="_location" class="ptolemy.kernel.util.Location" value="[100, 100]"/>
   </property>
 *  </pre>
 *  In the above, the _location property is necessary to ensure
 *  that Vergil shows an icon for the ConnectivityReporter attribute.
 *  It specifies the location on the canvas where that icon
 *  should be placed.
 *
 * @author Edward A. Lee
 * @version $Id$
 */
public class ConnectivityReporter extends Attribute {

    /** Need this two-argument constructor to be able to instantiate
     *  the attribute in a model using MoML.
     *  @param container The containing model.
     *  @param name The name to give this attribute.
     */
    public ConnectivityReporter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create an EditorFactory to handle double clicks.
        new DoubleClickHandler(this, "Calculate");
    }

    /** Inner class that is itself an Attribute which, when
     *  contained by a Ptolemy object, changes the default
     *  behavior of a double click on the attribute.
     *  A double click will cause the createEditor()
     *  method of this inner class to be invoked.
     */
    public class DoubleClickHandler extends EditorFactory {

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
            List<Entity> entities = ((CompositeEntity) ConnectivityReporter.this
                    .getContainer()).entityList();
            for (Entity entity : entities) {
                List<IOPort> ports = entity.portList();
                for (IOPort port : ports) {
                    if (port.isOutput()) {
                        List<IOPort> remotePorts = port.connectedPortList();
                        for (IOPort remotePort : remotePorts) {
                            MessageHandler.message(entity.getName()
                                    + " is connected to "
                                    + remotePort.getContainer().getName());
                        }
                    }
                }
            }
        }
    }
}
