/* An applet that shows resolved types in a vergil graph.

Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.vergil;

import ptolemy.actor.Manager;
import ptolemy.actor.TypeEvent;
import ptolemy.actor.TypeListener;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;

import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// TypeAnimatorApplet

/**
   An applet that demonstrates the Ptolemy II type system.
   This applet identifies attributes whose names match entity names,
   followed by an underscore, followed by a port name.
   If that attribute has an icon (i.e. it is a visible attribute),
   then the icon is set to a text string that gives the type of
   the port.
   <p>
   To use this applet, create an MoML file with the model that you
   want to animate, and insert visible attributes in it.  Rename
   these so that the name has the form "entityname_portname",
   to illustrate the port of the specified entity.

   @author Edward A. Lee and Yuhong Xiong
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class TypeAnimatorApplet extends MoMLViewerApplet {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the fact that the manager state has changed.  This is
     *  called by the manager.  In this class, we detect the start of
     *  execution of the model to initialize the type displays with
     *  the initial types.
     *  @param manager The manager that changed.
     */
    public void managerStateChanged(Manager manager) {
        super.managerStateChanged(manager);

        // FIXME this is a workaround for a bug in the type system where
        // type listeners are not properly notified when the type is
        // a structured type.
        if (manager.getState() == Manager.INITIALIZING) {
            _updateAllTypeDisplays();
        }
    }

    /** Override the base class to avoid executing the model automatically
     *  when the applet starts, and instead to update type displays
     *  to the initial types of the ports.
     */
    public void start() {
        _updateAllTypeDisplays();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the model.  This overrides the base class to attach
     *  type listeners to all the ports contained by top-level entities.
     *  @param workspace The workspace in which to create the model.
     *  @exception Exception If there was a problem creating the model.
     *  @return  The model that was created
     */
    protected NamedObj _createModel(Workspace workspace)
            throws Exception {
        _toplevel = super._createModel(workspace);

        if (_toplevel instanceof CompositeEntity) {
            CompositeEntity toplevel = (CompositeEntity) _toplevel;
            TypeListener typeListener = new PortTypeListener();
            Iterator entities = toplevel.entityList().iterator();

            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();
                Iterator ports = entity.portList().iterator();

                while (ports.hasNext()) {
                    TypedIOPort port = (TypedIOPort) ports.next();
                    port.addTypeListener(typeListener);
                }
            }
        }

        return _toplevel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Update the type designator for all ports contained by
    // entities contained by the toplevel.
    private void _updateAllTypeDisplays() {
        if (_toplevel instanceof CompositeEntity) {
            CompositeEntity toplevel = (CompositeEntity) _toplevel;
            Iterator entities = toplevel.entityList().iterator();

            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();
                Iterator ports = entity.portList().iterator();

                while (ports.hasNext()) {
                    TypedIOPort port = (TypedIOPort) ports.next();
                    _updateTypeDisplay(port);
                }
            }
        }
    }

    // Update the type designator for the specified port.
    private void _updateTypeDisplay(TypedIOPort port) {
        // Construct the name of the type label from the name
        // of the port relative to the top level.
        String portName = port.getName(_toplevel);
        String labelName = portName.replace('.', '_');
        Attribute label = _toplevel.getAttribute(labelName);

        if (label != null) {
            Configurable config = (Configurable) label.getAttribute(
                    "_iconDescription");

            if (config != null) {
                String moml = "<property name=" + "\"_iconDescription\" "
                    + "class=\"ptolemy.kernel.util"
                    + ".SingletonConfigurableAttribute\">"
                    + "<configure><svg><text x=\"20\" "
                    + "style=\"font-size:14; font-family:sanserif; "
                    + "fill:red\" y=\"20\">" + port.getType()
                    + "</text></svg></configure></property>";
                label.requestChange(new MoMLChangeRequest(this, label, moml));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // The local listener class
    private class PortTypeListener implements TypeListener {
        // Called to indicate that a type has changed.
        public void typeChanged(final TypeEvent event) {
            TypedIOPort port = (TypedIOPort) event.getSource();
            _updateTypeDisplay(port);
        }
    }
}
