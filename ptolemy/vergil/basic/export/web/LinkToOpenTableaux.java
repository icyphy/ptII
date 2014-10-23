/* Interface for parameters that provide web export content.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.web;

import java.awt.Frame;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.TypedActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.gui.ImageExportable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.ExportParameters;
import ptolemy.vergil.basic.HTMLExportable;

///////////////////////////////////////////////////////////////////
//// LinkToOpenTableaux
/**
 * A parameter specifying default hyperlink to associate
 * with icons in model. Putting this into a model causes a hyperlink
 * to be associated with each icon (as specified by the <i>include</i>
 * and <i>instancesOf</i> parameters) that is associated to an
 * open Tableau.
 * If the the frame associated with the tableau implements
 * HTMLExportable, then this is an ordinary link to
 * the HTML exported by the frame. If it instead
 * implements ImageExportable, then this a link that
 * brings up the image in a lightbox.
 * <p>
 * This parameter is designed to be included in a Configuration file
 * to specify global default behavior for export to Web. Just put
 * it in the top level of the Configuration, and this hyperlink
 * will be provided by default.
 * <p>
 * Note that this class works closely with
 * {@link ptolemy.vergil.basic.export.html.ExportHTMLAction}.
 * It will not work if the {@link WebExporter} provided to its
 * methods is not an instance of ExportHTMLAction.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class LinkToOpenTableaux extends DefaultIconLink {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public LinkToOpenTableaux(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _exportedClassDefinitions = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This overrides the base class to ensure that each class
     *  definition is exported only once.
     *  @exception IllegalActionException If a subclass throws it.
     */
    @Override
    public void provideContent(WebExporter exporter)
            throws IllegalActionException {
        try {
            _exportedClassDefinitions = new HashSet<NamedObj>();
            super.provideContent(exporter);
        } finally {
            _exportedClassDefinitions = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to generate a web page or an image
     *  file for the specified object, if appropriate, and to provide
     *  the href, target, and class attributes to the area attribute
     *  associated with the object.
     *  @param exporter The exporter.
     *  @param object The Ptolemy II object.
     *  @exception IllegalActionException If evaluating parameters fails.
     */
    @Override
    protected void _provideEachAttribute(WebExporter exporter, NamedObj object)
            throws IllegalActionException {
        WebAttribute webAttribute;

        // Create a table of effigies associated with any
        // open submodel or plot.
        Map<NamedObj, PtolemyEffigy> openEffigies = new HashMap<NamedObj, PtolemyEffigy>();
        Tableau myTableau = exporter.getFrame().getTableau();
        Effigy myEffigy = (Effigy) myTableau.getContainer();
        List<PtolemyEffigy> effigies = myEffigy.entityList(PtolemyEffigy.class);
        for (PtolemyEffigy effigy : effigies) {
            // The following will, for example, associate a plotter with effigy
            // for the open plot.
            openEffigies.put(effigy.getModel(), effigy);
        }
        // In order to ensure that all open windows have hyperlinks
        // to them, fix up the openEffies data structure so that if
        // the container of a plotter (for example) is not associated
        // with an open effigy, then we attempt to associate it with
        // the container instead, or the container's container, until
        // we get to the top level.
        Map<NamedObj, PtolemyEffigy> containerAssociations = new HashMap<NamedObj, PtolemyEffigy>();
        if (openEffigies.size() > 0) {
            for (NamedObj component : openEffigies.keySet()) {
                if (component == null) {
                    // This is caused by $PTII/bin/ptinvoke
                    // ptolemy.vergil.basic.export.ExportModel -force
                    // htm -run -openComposites -whiteBackground
                    // ptolemy/actor/gt/demo/ModelExecution/ModelExecution.xml
                    // $PTII/ptolemy/actor/gt/demo/ModelExecution/ModelExecution
                    System.out
                    .println("Warning: LinkToOpenTableaux._provideEachAttribute() "
                            + object.getFullName()
                            + ", an open effigy was null?");
                } else {
                    NamedObj container = component.getContainer();
                    while (container != null) {
                        if (openEffigies.get(container) != null) {
                            // The container of a plotter (say) has
                            // an open effigy, so there will be a link
                            // to the plot.
                            // Exporting
                            // ptolemy/data/ontologies/demo/CarTracking/CarTracking.xml
                            // was hanging in a tight loop here.
                            container = container.getContainer();
                            continue;
                        }
                        // The container of the plotter (say) does
                        // not have an open effigy.  Associate this
                        // container and then try the container's container.
                        containerAssociations.put(container,
                                openEffigies.get(component));
                        container = container.getContainer();
                    }
                }
            }
        }
        openEffigies.putAll(containerAssociations);

        PtolemyEffigy effigy = openEffigies.get(object);
        // The hierarchy of effigies does not always follow the model hierarchy
        // (e.g., a PlotEffigy will be contained by the top-level effigy for the
        // model for some reason), so if the effigy is null, we search
        // nonetheless for an effigy.
        if (effigy == null) {
            Effigy candidate = Configuration.findEffigy(object);
            if (candidate instanceof PtolemyEffigy) {
                effigy = (PtolemyEffigy) candidate;
            }
        }
        try {
            if (effigy != null) {
                // _linkTo() recursively calls writeHTML();
                _linkTo(exporter, effigy, object, object,
                        exporter.getExportParameters());
            } else {
                // If the object is a State, we still have work to do.
                if (object instanceof State) {
                    // In a ModalModel, object is a State
                    // inside the _Controller.  But the effigy is stored
                    // under the refinements of that state, which have the
                    // same container as the _Controller.
                    try {
                        TypedActor[] refinements = ((State) object)
                                .getRefinement();
                        // FIXME: There may be more
                        // than one refinement. How to open all of them?
                        // We have only one link. For now, just open the first one.
                        if (refinements != null && refinements.length > 0) {
                            effigy = openEffigies.get(refinements[0]);
                            if (effigy != null) {
                                // _linkTo() recursively calls writeHTML();
                                _linkTo(exporter, effigy, object,
                                        (NamedObj) refinements[0],
                                        exporter.getExportParameters());
                            }
                        }
                    } catch (IllegalActionException e) {
                        // Ignore errors here. Just don't export this refinement.
                    }
                } else if (object instanceof Instantiable) {
                    // There is no open effigy, but the object might
                    // be an instance of a class where the class definition
                    // is open. Look for that.
                    Instantiable parent = ((Instantiable) object).getParent();
                    if (parent instanceof NamedObj) {
                        // Avoid doing the export of a class definition
                        // multiple times.
                        if (_exportedClassDefinitions.contains(parent)) {
                            // Have already exported the class definition. Just
                            // need to add the hyperlink.
                            webAttribute = WebAttribute.createWebAttribute(
                                    object, "hrefWebAttribute", "href");
                            webAttribute.setExpression(parent.getName()
                                    + "/index.html");
                            exporter.defineAttribute(webAttribute, true);
                        } else {
                            // Have not exported the class definition. Do so now.
                            _exportedClassDefinitions.add((NamedObj) parent);
                            Effigy classEffigy = Configuration
                                    .findEffigy((NamedObj) parent);
                            if (classEffigy instanceof PtolemyEffigy) {
                                // _linkTo() recursively calls writeHTML();
                                _linkTo(exporter, (PtolemyEffigy) classEffigy,
                                        object, (NamedObj) parent,
                                        exporter.getExportParameters());
                            }
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to generate sub-web-page. ");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the title of the specified object. If it contains a parameter
     *  of class {@link Title}, then return the title specified by that class.
     *  Otherwise, if the object is an instance of FSMActor contained by
     *  a ModalModel, then return the
     *  name of its container, not the name of the FSMActor.
     *  Otherwise, return the name of the object.
     *  @param object The object.
     *  @return A title for the object.
     *  @exception IllegalActionException If accessing the title attribute fails..
     */
    private static String _getTitleText(NamedObj object)
            throws IllegalActionException {
        // If the object contains an IconLink parameter, then use that instead
        // of the default. If it has more than one, then just use the first one.
        List<Title> links = object.attributeList(Title.class);
        if (links != null && links.size() > 0) {
            return links.get(0).stringValue();
        }
        if (object instanceof FSMActor) {
            NamedObj container = object.getContainer();
            if (container instanceof ModalModel) {
                return container.getName();
            }
        }
        return object.getName();
    }

    /** For the specified effigy, define the relevant href, target,
     *  and class area attributes
     *  if the effigy has any open tableaux and those have frames
     *  that implement either HTMLExportable or ImageExportable.
     *  As a side effect, this may generate HTML or image
     *  files or subdirectories in the directory given in the specified
     *  parameters.
     *  @param exporter The exporter.
     *  @param effigy The effigy.
     *  @param sourceObject The source Ptolemy II object (link from).
     *  @param destinationObject The destination object (link to, same as sourceObject,
     *   or alternatively, a class definition for sourceObject).
     *  @param parameters The parameters of the web export that requires this link.
     *  @exception IOException If unable to create required HTML files.
     *  @exception PrinterException If unable to create required HTML files.
     *  @exception IllegalActionException If something goes wrong.
     */
    private void _linkTo(WebExporter exporter, PtolemyEffigy effigy,
            NamedObj sourceObject, NamedObj destinationObject,
            ExportParameters parameters) throws IOException, PrinterException,
            IllegalActionException {
        File gifFile;
        WebAttribute webAttribute;
        WebElement webElement;
        // Look for any open tableaux for the object.
        List<Tableau> tableaux = effigy.entityList(Tableau.class);

        // ThreadedComposite extends MirrorComposite.
        // ThreadedComposites do not have a top level tableau, they
        // contain an effigy that contains a tableau.

        // To replicate:
        // $PTII/bin/ptinvoke ptolemy.vergil.basic.export.ExportModel -force htm -run -openComposites -timeOut 30000 -whiteBackground ptolemy/actor/lib/hoc/demo/ThreadedComposite/MulticoreExecution.xml $PTII/ptolemy/actor/lib/hoc/demo/ThreadedComposite/MulticoreExecution

        if (tableaux.size() == 0) {
            List<PtolemyEffigy> effigies = effigy
                    .entityList(PtolemyEffigy.class);
            if (effigies != null && effigies.size() > 0) {
                tableaux = effigies.get(0).entityList(Tableau.class);
            }
        }
        // If there are multiple tableaux open, use only the first one.
        if (tableaux.size() > 0) {
            // The ddf IfThenElse model has a composite called +1/-1 Gain,
            // which is not a legal file name, so we sanitize it.
            String name = StringUtilities.sanitizeName(destinationObject
                    .getName());
            Frame frame = tableaux.get(0).getFrame();
            // If it's a composite actor, export HTML.
            if (frame instanceof HTMLExportable) {
                File directory = parameters.directoryToExportTo;
                File subDirectory = new File(directory, name);
                if (subDirectory.exists()) {
                    if (!subDirectory.isDirectory()) {
                        // Move file out of the way.
                        File backupFile = new File(directory, name + ".bak");
                        if (!subDirectory.renameTo(backupFile)) {
                            throw new IOException("Failed to rename \""
                                    + subDirectory + "\" to \"" + backupFile
                                    + "\"");
                        }
                    }
                } else if (!subDirectory.mkdir()) {
                    throw new IOException("Unable to create directory "
                            + subDirectory);
                }
                ExportParameters newParameters = new ExportParameters(
                        subDirectory, parameters);
                // The null argument causes the write to occur to an index.html
                // file.
                ((HTMLExportable) frame).writeHTML(newParameters, null);
                webAttribute = WebAttribute.createWebAttribute(sourceObject,
                        "hrefWebAttribute", "href");
                webAttribute.setExpression(name + "/index.html");
                exporter.defineAttribute(webAttribute, true);

                // Add to table of contents file if we are using the Ptolemy
                // website infrastructure.
                boolean usePtWebsite = Boolean.valueOf(StringUtilities
                        .getProperty("ptolemy.ptII.exportHTML.usePtWebsite"));
                if (usePtWebsite) {
                    String destinationTitle = LinkToOpenTableaux
                            ._getTitleText(destinationObject);
                    if (destinationTitle.length() > 16) {
                        //Truncate the text so that it does not overflow the toc.
                        destinationTitle = destinationTitle.substring(0, 16)
                                + ".";
                    }
                    webElement = WebElement.createWebElement(destinationObject,
                            "tocContents", "tocContents");
                    webElement.setExpression(" <li><a href=\"" + name
                            + "/index.html" + "\">" + destinationTitle
                            + "</a></li>");
                    exporter.defineElement(webElement, false);
                }
            } else if (frame instanceof ImageExportable) {
                gifFile = new File(parameters.directoryToExportTo, name
                        + ".gif");
                if (parameters.deleteFilesOnExit) {
                    gifFile.deleteOnExit();
                }
                OutputStream gifOut = new FileOutputStream(gifFile);
                try {
                    ((ImageExportable) frame).writeImage(gifOut, "gif");
                } finally {
                    gifOut.close();
                }
                // Strangely, the class has to be "iframe".
                // I don't understand why it can't be "lightbox".
                webAttribute = WebAttribute.createWebAttribute(sourceObject,
                        "hrefWebAttribute", "href");
                webAttribute.setExpression(name + ".gif");
                exporter.defineAttribute(webAttribute, true);

                webAttribute = WebAttribute.appendToWebAttribute(sourceObject,
                        "classWebAttribute", "class", "iframe");

                exporter.defineAttribute(webAttribute, true);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** A set of class definitions for which an export has already occurred. */
    private Set<NamedObj> _exportedClassDefinitions;
}
