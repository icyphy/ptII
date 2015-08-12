/* A directory of open models.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// ModelDirectory

/**
 A directory of open models. An instance of this class is contained
 by a Configuration. Each open model is represented by an instance of
 Effigy.  An effigy represents the model data.
 It contains a string attribute named "identifier"
 with a string value that uniquely identifies the model.
 A typical choice (which depend on the configuration)
 is the canonical URL for a MoML file that describes the model.
 An effigy also contains all open instances of Tableau associated
 with the model.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see Configuration
 @see Effigy
 @see Tableau
 */
public class ModelDirectory extends CompositeEntity {
    /** Construct a model directory with the specified container and name.
     *  @param container The configuration that contains this directory.
     *  @param name The name of the directory.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.  This should not be thrown.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ModelDirectory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the effigy of the model that corresponds to the specified
     *  identifier.
     *  @param identifier The identifier for the model, such as a URL.
     *  @return The effigy for the model, or null if the model is not
     *   in the directory.
     */
    public Effigy getEffigy(String identifier) {
        Iterator entities = entityList(Effigy.class).iterator();

        while (entities.hasNext()) {
            Effigy entity = (Effigy) entities.next();
            StringAttribute id = (StringAttribute) entity
                    .getAttribute("identifier");

            if (id != null) {
                String idString = id.getExpression();

                if (idString.equals(identifier)) {
                    return entity;
                }
            }
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Remove the specified entity, and if there are no more models
     *  in the directory, except possibly the configuration, then
     *  remove this directory from its container.
     *  This method should not be used directly.  Call the setContainer()
     *  method of the entity instead with a null argument.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the entity in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  This class overrides the superclass to check if this composite is
     *  empty, and if so, calls system.exit
     *  @param entity The entity to remove.
     */
    @Override
    protected void _removeEntity(ComponentEntity entity) {
        super._removeEntity(entity);

        List remainingEntities = entityList(Effigy.class);

        if (remainingEntities.size() == 0) {
            try {
                // This clause gets called when File->Exit is invoked.
                _purgeConfigurationURL();
                setContainer(null);
            } catch (KernelException ex) {
                throw new InternalErrorException("Cannot remove directory!");
            }
        } else {
            if (remainingEntities.size() == 1) {
                // Check to see whether what remains is only the configuration.
                Object remaining = remainingEntities.get(0);

                if (remaining instanceof PtolemyEffigy) {
                    if (((PtolemyEffigy) remaining).getModel() instanceof Configuration) {
                        try {
                            // This clause gets called when the window is closed.
                            _purgeConfigurationURL();
                            setContainer(null);
                        } catch (KernelException ex) {
                            throw new InternalErrorException(
                                    "Cannot remove directory!");
                        }
                    }
                }
            }

            // Finally, we might have a case where none of the effigies in
            // the application have a tableau, in which case the application
            // no longer has a UI.  If this happens, then we want to remove
            // the directory, triggering the application to exit.
            boolean anyTableau = false;

            // Check to see if the remaining effigies have any tableaux.
            for (Iterator effigies = remainingEntities.iterator(); effigies
                    .hasNext() && !anyTableau;) {
                Effigy effigy = (Effigy) effigies.next();

                if (effigy.numberOfOpenTableaux() > 0) {
                    anyTableau = true;
                }
            }

            // If we can't find any tableau for any of the effigies, then exi
            if (!anyTableau) {
                try {
                    // This gets reentrant...  Ugh..
                    for (Iterator effigies = remainingEntities.iterator(); effigies
                            .hasNext();) {
                        Effigy effigy = (Effigy) effigies.next();
                        effigy.setContainer(null);
                    }
                } catch (KernelException ex) {
                    throw new InternalErrorException("Cannot remove directory!");
                }
            }
        }
    }

    /** If the configuration is present, then purge the model record
     * of the configuration.  This is done so that if we re-read the configuration.xml
     * file, then we get the ModelDirectory.
     * This came up with applets that bring up a separate window from
     * the browser.  To reproduce:
     * <ol>
     * <li> point your browser at a Ptolemy applet demo.</li>
     * <li> The toplevel applet window containing the demo comes up.</li>
     * <li> Close the toplevel applet window.</li>
     * <li> Reload the browser page.</li>
     * </ol>
     * Formerly, we were seeing exceptions about missing directory.
     */
    private void _purgeConfigurationURL() {
        if (getContainer() != null) {
            List attributes = getContainer().attributeList(URIAttribute.class);
            if (attributes.size() > 0) {
                // The entity has a URI, which was probably
                // inserted by MoMLParser.
                URL url = null;
                try {
                    url = ((URIAttribute) attributes.get(0)).getURL();
                    MoMLParser.purgeModelRecord(url);
                } catch (Exception ex) {
                    throw new InternalErrorException("Cannot purge " + url);
                }
            }
        }
    }
}
