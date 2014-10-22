/* Remove graphical classes

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
package ptolemy.moml.filter;

import java.util.HashMap;
import java.util.Iterator;

import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// RemoveGraphicalClasses

/** When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause MoMLParser to filter out graphical classes.

 <p>This is very useful for running applets with out requiring files
 like diva.jar to be downloaded.  It is also used by the nightly build to
 run tests when there is no graphical display present.

 @author  Edward A. Lee, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class RemoveGraphicalClasses extends MoMLFilterSimple {
    /** Construct a filter that removes graphical classes.
     */
    public RemoveGraphicalClasses() {
        if (_graphicalClasses == null) {
            initialize();
        }
    }

    /** Clear the map of graphical classes to be removed.
     */
    public static void clear() {
        _graphicalClasses = new HashMap();
    }

    /** Filter for graphical classes and return new values if
     *  a graphical class is found.
     *  An internal HashMap maps names of graphical entities to
     *  new names.  The HashMap can also map a graphical entity
     *  to null, which means the entity is removed from the model.
     *  All class attributeValues that start with "ptolemy.domains.gr"
     *  are deemed to be graphical elements and null is always returned.
     *  For example, if the attributeValue is "ptolemy.vergil.icon.ValueIcon",
     *  or "ptolemy.vergil.basic.NodeControllerFactory"
     *  then return "ptolemy.kernel.util.Attribute"; if the attributeValue
     *  is "ptolemy.vergil.icon.AttributeValueIcon" or
     *  "ptolemy.vergil.icon.BoxedValueIcon" then return null, which
     *  will cause the MoMLParser to skip the rest of the element;
     *  otherwise return the original value of the attributeValue.
     *
     *  @param container  The container for this attribute, ignored
     *  in this method.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute, ignored
     *   in this method.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return the filtered attributeValue.
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        if (_graphicalClasses == null) {
            initialize();
        }
        // If the nightly build is failing with messages like:
        // " X connection to foo:0 broken (explicit kill or server shutdown)."
        // Try uncommenting the next lines to see what is being
        // expanding before the error:
        //System.out.println("filterAttributeValue: " + container + "\t"
        //       +  attributeName + "\t" + attributeValue);
        if (attributeValue == null) {
            return null;
        } else if (_graphicalClasses.containsKey(attributeValue)) {
            MoMLParser.setModified(true);
            return (String) _graphicalClasses.get(attributeValue);
        } else if (_removeGR && attributeValue.startsWith("ptolemy.domains.gr")) {
            MoMLParser.setModified(true);
            return null;
        }

        return attributeValue;
    }

    /** In this class, do nothing.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @exception Exception Not thrown in this base class.
     */
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
    }

    /** Initialize the set of classes to remove. */
    public static void initialize() {
        _graphicalClasses = new HashMap();

        // Alphabetical by key class
        // We can convert any graphical classes that have a port named "input" to
        // a Discard actor.  However, classes like XYPlot have ports named "X" and Y",
        // so XYPlot cannot be converted.
        _graphicalClasses.put("ptolemy.actor.lib.gui.ArrayPlotter",
                "ptolemy.actor.lib.Discard");
        _graphicalClasses.put("ptolemy.actor.lib.gui.BarGraph",
                "ptolemy.actor.lib.Discard");
        _graphicalClasses.put("ptolemy.actor.lib.gui.Display",
                "ptolemy.actor.lib.Discard");
        _graphicalClasses.put("ptolemy.actor.lib.gui.HistogramPlotter",
                "ptolemy.actor.lib.Discard");
        _graphicalClasses.put("ptolemy.actor.lib.gui.RealTimePlotter",
                "ptolemy.actor.lib.Discard");
        _graphicalClasses.put("ptolemy.actor.lib.gui.TimedPlotter",
                "ptolemy.actor.lib.Discard");
        _graphicalClasses.put("ptolemy.actor.lib.gui.SequencePlotter",
                "ptolemy.actor.lib.Discard");
        _graphicalClasses.put("ptolemy.vergil.actor.lib.LEDMatrix",
                "ptolemy.actor.lib.Discard");

        _graphicalClasses.put(
                "ptolemy.data.properties.gui.PropertyHighlighter", null);

        _graphicalClasses.put("ptolemy.domains.sr.lib.gui.NonStrictDisplay",
                "ptolemy.actor.lib.Discard");

        // Generated applet from moml/demo/modulation.xml
        // fails to run if substitute Attribute for NodeControllerFactory
        // so we set it to null instead.
        _graphicalClasses.put("ptolemy.vergil.toolbox.AnnotationEditorFactory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put(
                "ptolemy.vergil.toolbox.VisibleParameterEditorFactory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put(
                "ptolemy.vergil.fsm.modal.HierarchicalStateControllerFactory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses
                .put("ptolemy.vergil.modal.modal.HierarchicalStateControllerFactory",
                        "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put("ptolemy.vergil.fsm.modal.ModalTableauFactory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put("ptolemy.vergil.modal.modal.ModalTableauFactory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put("ptolemy.vergil.ptera.PteraGraphTableau$Factory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put(
                "ptolemy.vergil.gt.TransformationAttributeEditorFactory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put("ptolemy.vergil.gt.MatchResultTableau$Factory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put("ptolemy.vergil.gt.GTTableau$Factory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put(
                "ptolemy.vergil.gt.GTTableau$ModalTableauFactory",
                "ptolemy.kernel.util.Attribute");

        // 4/04 BooleanSwitch uses EditorIcon
        _graphicalClasses.put("ptolemy.vergil.icon.EditorIcon", null);

        // 11/06 FSM uses StateIcon
        _graphicalClasses.put("ptolemy.vergil.fsm.StateIcon", null);

        _graphicalClasses.put("ptolemy.vergil.modal.StateIcon", null);

        _graphicalClasses.put("ptolemy.vergil.fsm.fmv.FmvStateIcon", null);

        _graphicalClasses.put("ptolemy.vergil.modal.fmv.FmvStateIcon", null);

        _graphicalClasses.put("ptolemy.ontologies.ConceptIcon", null);

        _graphicalClasses.put("ptolemy.vergil.kernel.attributes.ArcAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.EllipseAttribute", null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.FilledShapeAttribute", null);

        _graphicalClasses.put("ptolemy.vergil.kernel.attributes.IDAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.ImageAttribute", null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.ArrowAttribute", null);

        _graphicalClasses.put("ptolemy.vergil.kernel.attributes.LineAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.ShapeAttribute", null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.ResizablePolygonAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.RectangleAttribute", null);

        _graphicalClasses.put("ptolemy.vergil.kernel.attributes.TextAttribute",
                null);

        _graphicalClasses.put("ptolemy.vergil.basic.export.web.BasicJSPlotter",
                null);
        _graphicalClasses.put(
                "ptolemy.vergil.basic.export.web.DygraphsJSPlotter", null);
        // Classes that import ptolemy.vergil.icon.ValueIcon
        _graphicalClasses.put(
                "ptolemy.vergil.basic.export.web.DefaultIconLink", null);
        _graphicalClasses.put(
                "ptolemy.vergil.basic.export.web.DefaultIconScript", null);
        _graphicalClasses.put("ptolemy.vergil.basic.export.web.DefaultTitle",
                null);
        _graphicalClasses
                .put("ptolemy.vergil.basic.export.web.HTMLImage", null);
        // HTMLText extends WebContent which imports ValueIcon
        _graphicalClasses.put("ptolemy.vergil.basic.export.web.HTMLText", null);
        _graphicalClasses.put("ptolemy.vergil.basic.export.web.IconLink", null);
        _graphicalClasses.put("ptolemy.vergil.basic.export.web.IconScript",
                null);
        _graphicalClasses.put(
                "ptolemy.vergil.basic.export.web.LinkToOpenTableaux", null);
        _graphicalClasses.put(
                "ptolemy.vergil.basic.export.web.ParameterDisplayIconScript",
                null);
        _graphicalClasses.put("ptolemy.vergil.basic.export.web.Title", null);
        _graphicalClasses.put("ptolemy.vergil.basic.export.web.WebContent",
                null);
        _graphicalClasses.put("ptolemy.vergil.basic.export.web.WebExportable",
                null);

        _graphicalClasses.put("ptolemy.vergil.basic.NodeControllerFactory",
                null);

        _graphicalClasses.put("ptolemy.vergil.ptera.EventIcon", null);
        _graphicalClasses.put("ptolemy.vergil.ptera.OctagonEventIcon", null);
        _graphicalClasses.put("ptolemy.vergil.ptera.TestIcon", null);
        _graphicalClasses
                .put("ptolemy.vergil.ptera.TimeAdvanceEventIcon", null);

        _graphicalClasses.put("ptolemy.vergil.gt.IterativeParameterIcon", null);
        _graphicalClasses.put("ptolemy.vergil.gt.StateMatcherIcon", null);
        _graphicalClasses.put("ptolemy.vergil.gt.TransformationAttributeIcon",
                null);

        _graphicalClasses
                .put("ptolemy.vergil.ptera.TimeAdvanceEventIcon", null);

        _graphicalClasses.put("ptolemy.vergil.icon.AttributeValueIcon", null);
        _graphicalClasses.put("ptolemy.vergil.icon.BoxedValueIcon", null);
        _graphicalClasses.put("ptolemy.vergil.icon.DesignPatternIcon", null);
        _graphicalClasses.put("ptolemy.vergil.icon.CopyCatIcon", null);
        _graphicalClasses.put("ptolemy.vergil.icon.EditorIcon", null);
        _graphicalClasses.put("ptolemy.vergil.icon.ShapeIcon", null);
        _graphicalClasses.put("ptolemy.vergil.icon.XMLIcon", null);

        // ptolemy/actor/lib/test/auto/StopSDF.xml has a MonitorValue actor,
        // so remove the UpdatedValueIcon.
        _graphicalClasses.put("ptolemy.vergil.icon.UpdatedValueIcon", null);
        _graphicalClasses.put("ptolemy.vergil.icon.ValueIcon",
                "ptolemy.kernel.util.Attribute");

        // Generated applet from moml/demo/modulation.xml
        // fails to run if substitute Attribute for AnnotationEditorFactory
        // so we set it to null instead.
        //_graphicalClasses.put("ptolemy.vergil.toolbox.AnnotationEditorFactory",
        //        "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put("ptolemy.vergil.toolbox.AnnotationEditorFactory",
                null);
        _graphicalClasses.put("ptolemy.vergil.toolbox"
                + ".VisibleParameterEditorFactory",
                "ptolemy.kernel.util.Attribute");

        // Shallow CG of actor/lib/test/auto/URLDirectoryReader3.xml fails
        // unless we remove CheckBoxStyle
        _graphicalClasses.put("ptolemy.actor.gui.style.CheckBoxStyle", null);
        _graphicalClasses.put("ptolemy.actor.gui.style.ChoiceStyle", null);

        _graphicalClasses.put("ptolemy.actor.gui.LocationAttribute", null);
        _graphicalClasses.put("ptolemy.actor.gui.SizeAttribute", null);
        _graphicalClasses.put("ptolemy.actor.gui.PtolemyPreferences",
                "ptolemy.data.expr.ScopeExtendingAttribute");
        _graphicalClasses.put("ptolemy.actor.gui.WindowPropertiesAttribute",
                null);

        // Sinewave has a DocViewerFactory, which we need to remove
        _graphicalClasses.put("ptolemy.vergil.basic.DocViewerFactory",
                "ptolemy.kernel.util.Attribute");
        // Sinewave has a DocAttribute, which we need to remove
        _graphicalClasses.put("ptolemy.vergil.basic.DocAttribute",
                "ptolemy.kernel.util.Attribute");

        _graphicalClasses.put("ptolemy.domains.wireless.lib.GraphicalLocator",
                "ptolemy.domains.wireless.lib.Locator");

        _graphicalClasses.put("ptolemy.domains.wireless.lib.TerrainProperty",
                null);

        _graphicalClasses.put(
                "ptolemy.domains.wireless.demo.EvaderAndPursuer.Sensor", null);

        // Remove various graphical classes from curriculum
        _graphicalClasses.put(
                "ptolemy.domains.curriculum.DependencyHighlighter", null);
        _graphicalClasses.put("ptolemy.vergil.basic.DependencyHighlighter",
                null);
        _graphicalClasses.put("ptolemy.domains.curriculum.HighlightEntities",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put("ptolemy.vergil.icon.NameIcon", null);

        // Needed modal/demo/SystemLevelTypes/*.xml
        _graphicalClasses.put("ptolemy.vergil.modal.StateIcon", null);

        // Exclude DependencyHighlighter
        _graphicalClasses.put("ptolemy.actor.gui.DependencyHighlighter", null);

        // properties classes
        _graphicalClasses.put(
                "ptolemy.vergil.properties.ModelAttributeController", null);

        _graphicalClasses.put("ptolemy.vergil.properties.LatticeElementIcon",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.actor.lib.MonitorReceiverContents", null);

        _graphicalClasses.put("ptolemy.vergil.ontologies.ConceptIcon", null);
        _graphicalClasses.put("ptolemy.vergil.ontologies.MultipleConceptIcon",
                null);

        _graphicalClasses.put(
                "ptolemy.domains.petrinet.lib.gui.PetriNetDisplay",
                "ptolemy.domains.petrinet.lib.PetriNetRecorder");

        _graphicalClasses.put(
                "ptolemy.domains.ptides.demo.PtidesAirplaneFuelControl.Tank",
                "ptolemy.domains.wireless.kernel.WirelessComposite");

        _graphicalClasses.put("ptolemy.actor.lib.image.ImageDisplay",
                "ptolemy.actor.lib.Discard");
        _graphicalClasses.put("ptolemy.actor.lib.gui.MatrixViewer",
                "ptolemy.actor.lib.Discard");


        // org/ptolemy/qss/test/auto/RLC.xml
        _graphicalClasses
            .put("ptolemy.vergil.pdfrenderer.PDFAttribute", null);

        //note: kepler display related actors should not be added here.
        //if the actor supports '-redirectgui', it should be put into file: $Kepler/common/configs/ptolemy/configs/kepler/KeplerDisplayActorWithRedirect.xml.
        //if the actor does not support '-redirectgui', it should be put into file: $Kepler/common/configs/ptolemy/configs/kepler/KeplerDisplayActorNoRedirect.xml.

    }

    /** Read in a MoML file, remove graphical classes and
     *  write the results to standard out.
     *  <p> For example, to remove the graphical classes from
     *  a file called <code>RemoveGraphicalClasses.xml</code>
     *  <pre>
     *  java -classpath "$PTII" ptolemy.moml.filter.RemoveGraphicalClasses test/RemoveGraphicalClasses.xml &gt; output.xml
     *  </pre>
     *  @param args An array of one string
     *  <br> The name of the MoML file to be cleaned.
     *  @exception Exception If there is a problem reading or writing
     *  a file.
     */
    public static void main(String[] args) throws Exception {
        try {
            // The HandSimDroid work in $PTII/ptserver uses dependency
            // injection to determine which implementation actors such as
            // Const and Display to use.  This method reads the
            // ptolemy/actor/ActorModule.properties file.</p>
            ActorModuleInitializer.initializeInjector();

            MoMLParser parser = new MoMLParser();

            // The list of filters is static, so we reset it in case there
            // filters were already added.
            MoMLParser.setMoMLFilters(null);

            // Add the backward compatibility filters.
            MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

            MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());
            MoMLParser.addMoMLFilter(new HideAnnotationNames());
            NamedObj topLevel = parser.parseFile(args[0]);
            System.out.println(topLevel.exportMoML());
        } catch (Throwable throwable) {
            System.err.println("Failed to filter \"" + args[0] + "\"");
            throwable.printStackTrace();
            StringUtilities.exit(1);
        }
    }

    /** Remove a class to be filtered.
     *  @param className The name of the class to be filtered
     *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
     *  @see #put(String, String)
     */
    public void remove(String className) {
        // ptolemy.copernicus.kernel.MakefileGenerator
        // so as to filter out the GeneratorAttribute
        _graphicalClasses.remove(className);
    }

    /** Add a class to be filtered for and its replacement if the class
     *  is found.  If the replacement is null, then the rest of the
     *  attribute is skipped.  Note that if you add a class with
     *  this method, then you must remove it with {@link #remove(String)},
     *  calling 'new RemoveGraphicalClasses' will not remove a class
     *  that was added with this method.
     *  @param className The name of the class to be filtered
     *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
     *  @param replacement The name of the class to be used if
     *  className is found.  If this argument is null then the
     *  rest of the attribute is skipped.
     *  @see #remove(String)
     */
    public void put(String className, String replacement) {
        // ptolemy.copernicus.kernel.KernelMain call this method
        // so as to filter out the GeneratorAttribute
        _graphicalClasses.put(className, replacement);
    }

    /** Set to true if we should removed classes that start with
     *  ptolemy.domains.gr.
     *  @param removeGR True if we should remove classes that start
     *  with ptolemy.domains.gr.
     */
    public void setRemoveGR(boolean removeGR) {
        _removeGR = removeGR;
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    @Override
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Remove or replace classes that are graphical.\n"
                + "This filter is used by the nightly build, and\n"
                + "can be used to run applets so that files like\n"
                + "diva.jar do not need to be downloaded.\n"
                + "The following actors are affected:\n");
        Iterator classNames = _graphicalClasses.keySet().iterator();

        while (classNames.hasNext()) {
            String oldClassName = (String) classNames.next();
            String newClassName = (String) _graphicalClasses.get(oldClassName);

            if (newClassName == null) {
                results.append(oldClassName + " will be removed\n");
            } else {
                results.append(oldClassName + " will be replaced by "
                        + newClassName + "\n");
            }
        }

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Map of actor names a HashMap of graphical classes to their
     *  non-graphical counterparts, usually either
     *  ptolemy.kernel.util.Attribute or null.
     */
    private static HashMap _graphicalClasses;

    /** True if we should remove the GR domain. */
    private boolean _removeGR = false;
}
