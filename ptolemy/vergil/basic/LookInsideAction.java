/*  The action to open a composite actor model, an ontology, or a
 *  MoMLModelAttribute.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;

import javax.swing.KeyStroke;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLModelAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorInteractionAddon;
import ptolemy.vergil.toolbox.FigureAction;
import diva.gui.GUIUtilities;

/** <p>The action to open a composite actor model, an ontology, or a
 *  MoMLModelAttribute. This class must remain named LookInsideAction for
 *  backward compatibility.</p>
 *  <p>This used to be a private class contained in
 *  {@link ptolemy.vergil.actor.ActorController ActorController}, but it is
 *  now relevant for ptolemy.vergil.ontologies.OntologyEntityController
 *  OntologyEntityController and
 *  {@link ptolemy.vergil.basic.MoMLModelAttributeController MoMLModelAttributeController},
 *  so it has been pulled out into a public class. Additionally MoMLModelAttributeController
 *  requires a different implementation to open its contained model since it is
 *  an Attribute and not a CompositeEntity, and thus its contained model does
 *  not fit in the traditional Ptolemy containment hierarchy.</p>
 *  <p>Previously
 *  {@link MoMLModelAttribute} contained its own private LookInsideAction class, but the
 *  shortcut key binding did not work correctly. This was because in a Ptolemy
 *  model, a shortcut key can only bind one action for every node in the entire model
 *  graphical space to any given key. If a model contains both normal
 *  {@link ptolemy.kernel.CompositeEntity CompositeEntity}
 *  (including ptolemy.data.ontologies.Ontology entities)
 *  elements and MoMLModelAttributes, then only one look inside action will be bound
 *  to the shortcut L key even though each action will be accessible from their
 *  individual context menus (See {@link GUIUtilities#addHotKey(javax.swing.JComponent,
 *  javax.swing.Action, javax.swing.KeyStroke) GUIUtilities.addHotKey()}).</p>
 *  <p>The solution here
 *  is that there is a single class to implement the look inside action that has
 *  two private methods to implement look inside for the normal Ptolemy CompositeEntity
 *  case and the special MoMLModelAttribute case.  The controller for each
 *  respective Ptolemy element can customize its instance of the LookInsideAction with its
 *  own menu label for its context menu, but the actual {@link #actionPerformed} method that
 *  executes the action is the same for all instances. So regardless of which
 *  LookInsideAction gets bound to the L key, it will work for all Ptolemy elements.</p>
 *  <p>If the element is not a CompositeEntity or a MoMLModelAttribute, it will
 *  just open the java text file of the element's class definition.</p>
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
@SuppressWarnings("serial")
public class LookInsideAction extends FigureAction {

    /** Create a new LookInsideAction object with the given
     *  string as its menu action label.
     *  @param menuActionLabel The label of the menu action to be displayed in
     *   the GUI context menus.
     */
    public LookInsideAction(String menuActionLabel) {
        super(menuActionLabel);

        // Attach a key binding for look inside (also called
        // open actor).
        // If we are in an applet, so Control-L or Command-L will
        // be caught by the browser as "Open Location", so we don't
        // supply Control-L or Command-L as a shortcut under applets.
        if (!StringUtilities.inApplet()) {
            // For some inexplicable reason, the I key doesn't work here.
            // Use L, which used to be used for layout.
            // Avoid Control_O, which is open file.
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_L, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the look inside action command received from the event sent from
     *  the user interface.
     *  @param event The event received to execute the look inside action.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (_configuration == null) {
            MessageHandler.error("Cannot open a model element "
                    + "without a configuration.");
            return;
        }

        // Determine which entity was selected for the open actor action.
        super.actionPerformed(event);
        NamedObj object = getTarget();

        if (object instanceof MoMLModelAttribute) {
            _openContainedModel((MoMLModelAttribute) object);
        } else {
            _openModel(object);
        }
    }

    /** Set the configuration to be used by the LookInsideAction object.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Open the model contained by the given MoMLModelAttribute. This is
     *  significantly different from opening a normal CompositeEntity.
     *  @param momlModelAttribute The MoMLModelAttribute to be opened.
     */
    private void _openContainedModel(MoMLModelAttribute momlModelAttribute) {
        try {
            NamedObj model = momlModelAttribute.getContainedModel();
            Tableau tableau = _configuration.openInstance(model);
            Effigy effigy = (Effigy) tableau.getContainer();

            // If the model is contained in a separate file, then we
            // need to set its uri parameter.
            // If it is in the same file, we have more work to do.
            File modelFile = momlModelAttribute.modelURL.asFile();
            if (modelFile != null) {
                // Model is in a separate file.
                effigy.uri.setURL(momlModelAttribute.modelURL.asURL());
            } else {
                // Model is in the same file.
                tableau.setMaster(false);

                // The effigy returned above has three problems. First,
                // it's container is the directory in the
                // configuration. We want it to be contained by the following
                // containerEffigy.
                final Effigy containerEffigy = _configuration
                        .getEffigy(momlModelAttribute.getContainer());

                if (containerEffigy == null) {
                    throw new InternalErrorException(momlModelAttribute, null,
                            "Could not get the effigy for "
                                    + momlModelAttribute);
                } else {
                    // Second, the effigy returned above returns the wrong value in its
                    // masterEffigy() method. That method returns the effigy associated
                    // with the toplevel, which is the same as effigy. We want it to
                    // return whatever the masterEffigy of containerEffigy is.
                    // We accomplish this by substituting a new effigy.
                    // This technique is borrowed from what is done in
                    // PtolemyFrame.getEffigy().
                    PtolemyEffigy newEffigy = new PtolemyEffigy(
                            containerEffigy, containerEffigy.uniqueName(model
                                    .getName())) {
                        @Override
                        public Effigy masterEffigy() {
                            return containerEffigy.masterEffigy();
                        }
                    };

                    // Third, the uri attribute and file properties
                    // of the effigy are not set to
                    // refer to the file that will actually save the model.
                    // This could be an external file if the modelURL parameter
                    // of the MoMLModelAttribute is set.
                    newEffigy.setModified(effigy.isModified());
                    URI uri = containerEffigy.uri.getURI();
                    newEffigy.uri.setURI(uri);
                    tableau.setContainer(newEffigy);
                    effigy.setContainer(null);

                    newEffigy.setModel(model);
                }
            }
        } catch (Exception ex) {
            MessageHandler.error("Unable to open the model contained by "
                    + momlModelAttribute.getName(), ex);
        }
    }

    /** Open the model contained by the given NamedObj. If it is a CompositeEntity,
     *  the submodel will be opened in a separate window. Otherwise the java
     *  source code text file for the element's class will be displayed.
     *  @param modelObject The NamedObj element to be opened.
     */
    private void _openModel(NamedObj modelObject) {
        try {
            StringParameter actorInteractionAddonParameter;
            actorInteractionAddonParameter = (StringParameter) _configuration
                    .getAttribute("_actorInteractionAddon", Parameter.class);

            if (actorInteractionAddonParameter != null) {
                String actorInteractionAddonClassName = actorInteractionAddonParameter
                        .stringValue();

                Class actorInteractionAddonClass = Class
                        .forName(actorInteractionAddonClassName);

                ActorInteractionAddon actorInteractionAddon = (ActorInteractionAddon) actorInteractionAddonClass
                        .newInstance();

                if (actorInteractionAddon
                        .isActorOfInterestForAddonController(modelObject)) {
                    actorInteractionAddon.lookInsideAction(this, modelObject);
                }

            }

        } catch (Exception e) {
            MessageHandler.error("Open model element failed.", e);
            // e.printStackTrace();
        }

        // NOTE: Used to open source code here if the object
        // was not a CompositeEntity. But this made it impossible
        // to associate a custom tableau with an atomic entity.
        // So now, the Configuration opens the source code as a
        // last resort.
        try {
            _configuration.openModel(modelObject);
        } catch (Exception ex) {
            MessageHandler.error("Open model element failed.", ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The configuration for the controller that contains this LookInsideAction. */
    private Configuration _configuration;
}
