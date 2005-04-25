/* Filter for Property class changes

Copyright (c) 2002-2005 The Regents of the University of California.
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

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

import java.util.HashMap;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// PropertyClassChanges

/** When this class is registered with the MoMLParser.setMoMLFilter()
    method, it will cause MoMLParser to filter so that models from
    earlier releases will run in the current release.

    <p>This class will filter for classes with properties where the class
    name has changed.

    <p>For example, after Ptolemy II 2.0.1, the Expression actor
    changed in such a way that the expression property changed from
    being a Parameter to being a StringAttribute.  To add this
    change to this filter, we add a code to the static section at
    the bottom of the file.
    <pre>
    // Expression: After 2.0.1, expression
    // property is now a StringAttribute
    HashMap expressionClassChanges = new HashMap();
    // Key = property name, Value = new class name
    expressionClassChanges.put("expression",
    "ptolemy.kernel.util.StringAttribute");
    </pre>
    The expressionClassChange HashMap maps property names to the new
    classname

    <pre>

    _actorsWithPropertyClassChanges
    .put("ptolemy.actor.lib.Expression",
    expressionClassChanges);
    </pre>
    The _actorsWithPropertyClassChanges HashMap contains all the classes
    such as Expression that have changes and each class has a map
    of the property changes that are to be made.

    <p> Conceptually, how the code works is that when we see a class while
    parsing, we check to see if the class is in _actorsWithPropertyClassChanges.
    If the class was present in the HashMap, then as we go through the
    code, we look for property names that need to have their classes changed.


    @author Christopher Hylands, Edward A. Lee
    @version $Id$
    @since Ptolemy II 2.0
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class PropertyClassChanges implements MoMLFilter {
    /** Return new class names for properties that have been
     *  registered as having changed classes. This filter
     *  may also return null to remove the element.
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @return The value of the attributeValue argument or
     *   a new value if the value has changed.
     */
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue) {
        // If you change this class, you should run before and after
        // timing tests on large moml files, a good command to run
        // is:
        // $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
        // which will open up a large xml file and then close after 2 seconds.
        //System.out.println("filterAttributeValue: " + container + "\t"
        //  +  attributeName + "\t" + attributeValue);
        // This method gets called many times by the MoMLParser,
        // so we try to be smart about the number of comparisons
        // and we try to group comparisons together so that we
        // are not making the same comparison more than once.
        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        if (attributeName.equals("name")) {
            // Save the name of the attribute for later use if we see a "class"
            _lastNameSeen = attributeValue;

            if (_currentlyProcessingActorWithPropertyClassChanges
                    && (element != null) && element.equals("property")) {
                if (_propertyMap.containsKey(attributeValue)) {
                    // We will do the above checks only if we found a
                    // class that had property class changes.
                    _newClass = (String) _propertyMap.get(attributeValue);
                    _foundChange = true;
                } else {
                    // Saw a name that did not match.
                    // However, we might have other names that
                    // did match, so keep looking
                    //_currentlyProcessingActorWithPropertyClassChanges = false;
                    _newClass = null;
                    _foundChange = false;
                }
            }
        }

        if (attributeName.equals("class")) {
            if (_actorsWithPropertyClassChanges.containsKey(attributeValue)) {
                // We found a class with a property class change.
                _currentlyProcessingActorWithPropertyClassChanges = true;

                if (container != null) {
                    _currentActorFullName = container.getFullName() + "."
                        + _lastNameSeen;
                } else {
                    _currentActorFullName = "." + _lastNameSeen;
                }

                _propertyMap = (HashMap) _actorsWithPropertyClassChanges.get(attributeValue);
            } else if (_currentlyProcessingActorWithPropertyClassChanges
                    && _foundChange) {
                String temporaryNewClass = _newClass;

                if (!attributeValue.equals(_newClass)) {
                    MoMLParser.setModified(true);
                }

                _newClass = null;
                _foundChange = false;
                return temporaryNewClass;
            } else if (_currentlyProcessingActorWithPropertyClassChanges
                    && (container != null)
                    && !container.getFullName().equals(_currentActorFullName)
                    && !container.getFullName().startsWith(_currentActorFullName)) {
                // We found another class in a different container
                // while handling a class with port name changes
                _currentlyProcessingActorWithPropertyClassChanges = false;
            }
        }

        return attributeValue;
    }

    /** Reset private variables.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     */
    public void filterEndElement(NamedObj container, String elementName)
            throws Exception {
        _foundChange = false;
    }

//     /** Remove a class to be filtered.
//      *  @param className The name of the class to be filtered
//      *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
//      *  @see #put(String, String)
//      */
//     public void remove(String className) {
//         // ptolemy.copernicus.kernel.MakefileGenerator
//         // so as to filter out the GeneratorAttribute
//         _actorsWithPropertyClassChanges.remove(className);
//     }

//     /** Add a new property class filter.
//      *  Note that if you add a class with
//      *  this method, then you must remove it with {@link #remove(String)},
//      *  calling "new PropertyClassChanges()" will not remove a class
//      *  that was added with this method.
//      *  @param className The name of the class to be filtered
//      *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
//      *  @param propertyClassMap The HashMap that has the property
//      *  name as a Key and the new class name as a value.  
//      *  If the value of the HashMap is null then the
//      *  rest of the attribute is skipped.
//      *  @see #remove(String)
//      */
//     public void put(String className, HashMap propertyClassMap) {
//         // Copernicus uses this to skip SharedParameters.
//         _actorsWithPropertyClassChanges.put(className, propertyClassMap);
//     }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Update any actor port class names\n"
                + "that have been renamed.\n"
                + "Below are the actors that are affected, along "
                + "with the port name\nand the new classname:");
        Iterator actors = _actorsWithPropertyClassChanges.keySet().iterator();

        while (actors.hasNext()) {
            String actor = (String) actors.next();
            results.append("\t" + actor + "\n");

            HashMap propertyMap = (HashMap) _actorsWithPropertyClassChanges.get(actor);
            Iterator properties = propertyMap.keySet().iterator();

            while (properties.hasNext()) {
                String oldProperty = (String) properties.next();
                String newProperty = (String) propertyMap.get(oldProperty);
                results.append("\t\t" + oldProperty + "\t -> " + newProperty
                        + "\n");
            }
        }

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Map of actor names a HashMap of property names to new classes.
    private static HashMap _actorsWithPropertyClassChanges;

    // The the full name of the actor we are currently processing
    private static String _currentActorFullName;

    // Set to true if we are currently processing an actor with parameter
    // class changes, set to false when we are done.
    private static boolean _currentlyProcessingActorWithPropertyClassChanges = false;

    // Last "name" value seen, for use if we see a "class".
    private static String _lastNameSeen;

    // The new class name for the property we are working on.
    private static String _newClass;

    // Keep track of whether a change was found.
    private static boolean _foundChange;

    // Cache of map from old property names to new class names for
    // the actor we are working on.
    private static HashMap _propertyMap;

    static {
        ///////////////////////////////////////////////////////////
        // Actors that have properties that have changed class.
        _actorsWithPropertyClassChanges = new HashMap();

        // Expression
        // This is a second generation change.
        // Used to change it to a StringAttribute
        HashMap expressionClassChanges = new HashMap();

        // Key = property name, Value = new class name
        expressionClassChanges.put("expression",
                "ptolemy.kernel.util.StringAttribute");

        _actorsWithPropertyClassChanges.put("ptolemy.actor.lib.Expression",
                expressionClassChanges);

        // _hideName in visible attributes.
        HashMap hideNameClassChanges = new HashMap();

        // Key = property name, Value = new class name
        // NOTE: Ideally, we would create a
        // ptolemy.data.expr.SingletonParameter with value
        // true, but we have no mechanism to set the value,
        // so we use an attribute.
        hideNameClassChanges.put("_hideName",
                "ptolemy.kernel.util.SingletonAttribute");

        _actorsWithPropertyClassChanges.put("ptolemy.kernel.util.Attribute",
                hideNameClassChanges);

        // MathFunction
        HashMap mathFunctionClassChanges = new HashMap();

        // Key = property name, Value = new class name
        mathFunctionClassChanges.put("function",
                "ptolemy.data.expr.StringParameter");

        _actorsWithPropertyClassChanges.put("ptolemy.actor.lib.MathFunction",
                mathFunctionClassChanges);

        // TrigFunction
        HashMap trigFunctionClassChanges = new HashMap();

        // Key = property name, Value = new class name
        trigFunctionClassChanges.put("function",
                "ptolemy.data.expr.StringParameter");

        _actorsWithPropertyClassChanges.put("ptolemy.actor.lib.TrigFunction",
                trigFunctionClassChanges);

        // MatlabExpression
        HashMap matlabClassChanges = new HashMap();

        // Key = property name, Value = new class name
        matlabClassChanges.put("expression", "ptolemy.data.expr.StringParameter");

        _actorsWithPropertyClassChanges.put("ptolemy.matlab.Expression",
                matlabClassChanges);

        // DirectoryListing
        HashMap directoryListingClassChanges = new HashMap();

        // Key = property name, Value = new class name
        directoryListingClassChanges.put("directoryOrURL",
                "ptolemy.actor.parameters.FilePortParameter");

        _actorsWithPropertyClassChanges.put("ptolemy.actor.lib.io.DirectoryListing",
                directoryListingClassChanges);

        // ModelReference
        HashMap modelReferenceClassChanges = new HashMap();

        // Key = property name, Value = new class name
        modelReferenceClassChanges.put("modelFileOrURL",
                "ptolemy.actor.parameters.FilePortParameter");

        _actorsWithPropertyClassChanges.put(
                "ptolemy.actor.lib.hoc.ModelReference",
                modelReferenceClassChanges);
        _actorsWithPropertyClassChanges.put(
                "ptolemy.vergil.actor.lib.VisualModelReference",
                modelReferenceClassChanges);

        // SRDirector
        HashMap srDirectorClassChanges = new HashMap();

        // Key = property name, Value = new class name
        srDirectorClassChanges.put("scheduler",
                "ptolemy.data.expr.StringParameter");

        _actorsWithPropertyClassChanges.put("ptolemy.domains.sr.kernel.Director",
                srDirectorClassChanges);

        // There is only one BreakpointODESolver. Some old models have the
        // wrong choices of break point ODE solvers. The following filters 
        // remove them.
        // CTEmbeddedDirector
        HashMap CTEmbeddedDirectorClassChanges = new HashMap();

        // Key = property name, Value = new class name
        CTEmbeddedDirectorClassChanges.put("breakpointODESolver", null);

        _actorsWithPropertyClassChanges.put(
                "ptolemy.domains.ct.kernel.CTEmbeddedDirector",
                CTEmbeddedDirectorClassChanges);

        // CTMixedSignalDirector
        HashMap ctMixedSignalDirectorClassChanges = new HashMap();

        // Key = property name, Value = new class name
        ctMixedSignalDirectorClassChanges.put("breakpointODESolver", null);

        _actorsWithPropertyClassChanges.put(
                "ptolemy.domains.ct.kernel.CTMixedSignalDirector",
                ctMixedSignalDirectorClassChanges);

        // CTMultiSolverDirector
        HashMap CTMultiSolverDirectorClassChanges = new HashMap();

        // Key = property name, Value = new class name
        CTMultiSolverDirectorClassChanges.put("breakpointODESolver", null);

        _actorsWithPropertyClassChanges.put(
                "ptolemy.domains.ct.kernel.CTMultiSolverDirector",
                CTMultiSolverDirectorClassChanges);

        // ModalModel
        HashMap modalModelClassChanges = new HashMap();

        // Key = property name, Value = new class name
        modalModelClassChanges.put("directorClass",
                "ptolemy.data.expr.StringParameter");

        // Remove the _Director attribute, which does not help the modal model
        // to decide which director to choose. This attribugte will be
        // automatically created. This attribute will not appear in the MoML
        // output any more.
        // NOTE: Remove a property by setting the new class to null.
        modalModelClassChanges.put("_Director", null);

        _actorsWithPropertyClassChanges.put("ptolemy.domains.fsm.modal.ModalModel",
                modalModelClassChanges);

        // LevelCrossingDetector
        HashMap levelCrossingDetectorClassChanges = new HashMap();
        levelCrossingDetectorClassChanges.put("useEventValue", null);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.ct.lib.LevelCrossingDetector",
                levelCrossingDetectorClassChanges);

        // ZeroCrossingDetector
        HashMap zeroCrossingDetectorClassChanges = new HashMap();
        zeroCrossingDetectorClassChanges.put("useEventValue", null);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.ct.lib.ZeroCrossingDetector",
                zeroCrossingDetectorClassChanges);

        // SDF actors don't record rates.
        HashMap rateParameterChanges = new HashMap();
        rateParameterChanges.put("tokenProductionRate", null);
        rateParameterChanges.put("tokenConsumptionRate", null);
        rateParameterChanges.put("tokenInitProduction", null);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.Autocorrelation",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.ArrayToSequence",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.BitsToInt",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.Chop",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.CountTrues",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.DownSample",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.DoubleToMatrix",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.FIR",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.FFT",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.IFFT",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.IntToBits",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.LineCoder",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.MatrixToDouble",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.MatrixToSequence",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.Repeat",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.SampleDelay",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.SequenceToArray",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.SequenceToMatrix",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.UpSample",
                rateParameterChanges);
        _actorsWithPropertyClassChanges.put("ptolemy.domains.sdf.lib.VariableFIR",
                rateParameterChanges);
    }
}
