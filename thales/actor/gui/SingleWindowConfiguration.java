/*
Copyright (c) 2003 THALES.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THALES BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THALES HAS BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THALES SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
BASIS, AND THALES HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Yellow (jerome.blanc@thalesgroup.com)
@AcceptedRating
 */
package thales.actor.gui;

import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyTableauFactory;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Prototype;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import thales.vergil.SingleWindowApplication;

//////////////////////////////////////////////////////////////////////////
//// SingleWindowConfiguration
/**
A Configuration that intercept created Tableau, and the content of its
Frame into a TabbnedPane.

@author Jérôme Blanc & Benoit Masson, Thales Research and technology, 01 sept. 2003
@version $Id$
@since Ptolemy II 3.1
*/
public class SingleWindowConfiguration extends Configuration {

    /**
     * @param workspace
     */
    public SingleWindowConfiguration(Workspace workspace) {
        super(workspace);
    }

    /** Create the first tableau for the given effigy, using the
     *  tableau factory.  This is called after an effigy is first opened,
     *  or when a new effigy is created.  If the method fails
     *  to create a tableau, then it removes the effigy from the directory.
     *  This prevents us from having lingering effigies that have no
     *  user interface.
     *  @param effigy The effigy for which to create a tableau.
     */
    public Tableau createPrimaryTableau(final Effigy effigy) {
        // NOTE: It used to be that the body of this method was
        // actually executed later, in the event thread, so that it can
        // safely interact with the user interface.
        // However, this does not appear to be necessary, and it
        // makes it impossible to return the tableau.
        // So we no longer do this.

        // If the object referenced by the effigy contains
        // an attribute that is an instance of TableauFactory,
        // then use that factory to create the tableau.
        // Otherwise, use the first factory encountered in the
        // configuration that agrees to represent this effigy.
        TableauFactory factory = null;
        if (effigy instanceof PtolemyEffigy) {
            NamedObj model = ((PtolemyEffigy) effigy).getModel();
            if (model != null) {
                Iterator factories =
                    model.attributeList(TableauFactory.class).iterator();
                                // If there are more than one of these, use the first
                                // one that agrees to open the model.
                while (factories.hasNext() && factory == null) {
                    factory = (TableauFactory) factories.next();
                    try {
                        Tableau tableau = factory.createTableau(effigy);
                        if (tableau != null) {
                            // The first tableau is a master if the container
                            // of the containing effigy is the model directory.
                            if (effigy.getContainer() instanceof ModelDirectory) {
                                tableau.setMaster(true);
                            }
                            tableau.setEditable(effigy.isModifiable());
                            //THALES MODIF
                            catchTableau(tableau);
                            tableau.show();
                            return tableau;
                        }
                    } catch (Exception ex) {
                        // Ignore so we keep trying.
                        factory = null;
                    }
                }
            }
        }
        // Defer to the configuration.
        // Create a tableau if there is a tableau factory.
        factory = (TableauFactory) getAttribute("tableauFactory");
        if (factory != null) {
            // If this fails, we do not want the effigy to linger
            try {
                Tableau tableau = factory.createTableau(effigy);
                if (tableau == null) {
                    throw new Exception("Tableau factory returns null.");
                }
                // The first tableau is a master if the container
                // of the containing effigy is the model directory.
                if (effigy.getContainer() instanceof ModelDirectory) {
                    tableau.setMaster(true);
                }
                tableau.setEditable(effigy.isModifiable());
                                //THALES MODIF
                catchTableau(tableau);
                tableau.show();
                return tableau;
            } catch (Exception ex) {
                                // Note that we can't rethrow the exception here
                                // because removing the effigy may result in
                                // the application exiting.
                MessageHandler.error(
                        "Failed to open tableau for "
                        + effigy.identifier.getExpression(),
                        ex);
                try {
                    effigy.setContainer(null);
                } catch (KernelException kernelException) {
                    throw new InternalErrorException(
                            this,
                            kernelException,
                            null);
                }
            }
        }
        return null;
    }

    /**
     * Catches the tableau, gets the corresponding JFrame and builds a new JTabbedPane
     * into the SingleWindowHTMLViewer.
     *
     * @param tableau tableau to catch
     */
    //THALES MODIF
    private void catchTableau(Tableau tableau) {
        if (SingleWindowApplication._mainFrame == null) {
            SingleWindowHTMLViewer mainView = new SingleWindowHTMLViewer();
            mainView.setConfiguration(this);
            mainView.show();
        }
        SingleWindowApplication._mainFrame.newTabbedPanel(tableau);
    }

    /** Open the specified Ptolemy II model. If a model already has
     *  open tableaux, then put those in the foreground and
     *  return the first one.  Otherwise, create a new tableau.
     *  @param entity The model.
     *  @return The tableau that is created, or the first one found,
     *   or null if none is created or found.
     *  @exception IllegalActionException If constructing an effigy or tableau
     *   fails.
     *  @exception NameDuplicationException If a name conflict occurs (this
     *   should not be thrown).
     */
    public Tableau openModel(NamedObj entity)
            throws IllegalActionException, NameDuplicationException {

        // If the entity defers its MoML definition to another,
        // then open that other.
        Prototype deferredTo = null;
        if (entity instanceof Prototype) {
            deferredTo = ((Prototype)entity).getDeferTo();
        }
        if (deferredTo != null) {
            entity = deferredTo;
        }

        // Search the model directory for an effigy that already
        // refers to this model.
        PtolemyEffigy effigy = getEffigy(entity);
        if (effigy != null) {
            // Found one.  Display all open tableaux.
            return effigy.showTableaux();
        } else {
            // There is no pre-existing effigy.  Create one.
            //THALES MODIF
            NamedObj topLevel = entity.toplevel();

            effigy = creatreEffifgy(getEffigy(topLevel));
            if (effigy == null) {
                effigy = new PtolemyEffigy(workspace());
            }
            effigy.setModel(entity);

            // Look to see whether the model has a URIAttribute.
            List attributes = entity.attributeList(URIAttribute.class);
            if (attributes.size() > 0) {
                                // The entity has a URI, which was probably
                                // inserted by MoMLParser.

                URI uri = ((URIAttribute) attributes.get(0)).getURI();

                                // Set the URI and identifier of the effigy.
                effigy.uri.setURI(uri);
                effigy.identifier.setExpression(uri.toString());

                                // Put the effigy into the directory
                ModelDirectory directory = getDirectory();
                effigy.setName(directory.uniqueName(entity.getName()));
                effigy.setContainer(directory);

                                // Create a default tableau.
                return createPrimaryTableau(effigy);
            } else {
                                // If we get here, then we are looking inside a model
                                // that is defined within the same file as the parent,
                                // probably.  Create a new PtolemyEffigy
                                // and open a tableau for it.

                                // Put the effigy inside the effigy of the parent,
                                // rather than directly into the directory.
                NamedObj parent = (NamedObj) entity.getContainer();
                PtolemyEffigy parentEffigy = null;
                                // Find the first container above in the hierarchy that
                                // has an effigy.
                while (parent != null && parentEffigy == null) {
                    parentEffigy = getEffigy(parent);
                    parent = (NamedObj) parent.getContainer();
                }
                boolean isContainerSet = false;
                if (parentEffigy != null) {
                    // OK, we can put it into this other effigy.
                    effigy.setName(parentEffigy.uniqueName(entity.getName()));
                    effigy.setContainer(parentEffigy);

                    // Set the identifier of the effigy to be that
                    // of the parent with the model name appended.

                    // Note that we add a # the first time, and
                    // then add . after that.  So
                    // file:/c:/foo.xml#bar.bif is ok, but
                    // file:/c:/foo.xml#bar#bif is not
                    // If the title does not contain a legitimate
                    // way to reference the submodel, then the user
                    // is likely to look at the title and use the wrong
                    // value if they xml edit files by hand. (cxh-4/02)
                    String entityName = parentEffigy.identifier.getExpression();
                    String separator = "#";
                    if (entityName.indexOf("#") > 0) {
                        separator = ".";
                    }
                    effigy.identifier.setExpression(
                            entityName + separator + entity.getName());

                    // Set the uri of the effigy to that of
                    // the parent.
                    effigy.uri.setURI(parentEffigy.uri.getURI());

                    // Indicate success.
                    isContainerSet = true;
                }
                                // If the above code did not find an effigy to put
                                // the new effigy within, then put it into the
                                // directory directly.
                if (!isContainerSet) {
                    CompositeEntity directory = getDirectory();
                    effigy.setName(directory.uniqueName(entity.getName()));
                    effigy.setContainer(directory);
                    effigy.identifier.setExpression(entity.getFullName());
                }

                return createPrimaryTableau(effigy);
            }
        }
    }

    protected PtolemyEffigy creatreEffifgy(PtolemyEffigy container) {
        PtolemyEffigy answer = null;
        if (container instanceof NavigableEffigy) {
            answer = new NavigableEffigy(workspace());
        }

        return answer;
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.gui.Configuration#openModel(java.net.URL, java.net.URL, java.lang.String, ptolemy.actor.gui.EffigyFactory)
     */
    public Tableau openModel(
            URL base,
            URL in,
            String identifier,
            EffigyFactory factory)
            throws Exception {

        //ModelValidator validator = new ModelValidator();
        //validator.filter(base);

        return super.openModel(base, in, identifier, factory);
    }

}
