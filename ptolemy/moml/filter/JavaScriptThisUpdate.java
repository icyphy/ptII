/* Add "this." to certain places in the script parameter of a JavaScript actor.

 Copyright (c) 2016 The Regents of the University of California.
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

import java.lang.reflect.Method;

import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// JavaScriptThisUpdate

/**
 * Add "this." to certain places in the script parameter of a JavaScript actor.
 *
 * <p>This class will eventually be removed after we have updated the files.</p>
 *
 * <p>To update a model, try:</p>
 * <pre>
 * $PTII/bin/ptinvoke ptolemy.vergil.basic.imprt.accessor.ReloadAccessors model.xml
 * </pre>
 *
 * To update all the models that contain JavaScript actors:
 * <pre>
 * # Generate a list of all the text files in the ptII tree, excluding certain directories like vendors.
 * $PTII/adm/bin/ptIItxtfiles &lt;&amp; /tmp/f
 *
 * # Generate a list of all of the .xml files.
 * cat /tmp/f | egrep '.xml$' &lt; /tmp/x
 *
 * # Generate a list of all the .xml files that contain the JavaScript actor.
 * cat /tmp/x | xargs egrep 'ptolemy.actor.lib.jjs.JavaScript' | awk -F ':' '{print $1}' | sort | uniq &lt; /tmp/javascriptx
 *
 * # Reload the accessors on all the .xml files that contain the JavaScript actor,
 * # which as a side effect runs the Backward Compatibility script
 * cat /tmp/jsaccessorx | xargs $PTII/bin/ptinvoke ptolemy.vergil.basic.imprt.accessor.ReloadAccessors
 * </pre>
 *
 * <p>We use <code>ptinvoke</code> to set the classpath.</p>
 *
 * <p>ReloadAccessors, opens a model, reloads all the accessors (if any) and
 * saves the model.  While opening the model, the BackwardCompatibility MoML
 * filters are run.  This class can be one of those filters.</p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class JavaScriptThisUpdate extends MoMLFilterSimple {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle parameter name changes.
     *  @param container  The container for XML element.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return A new value for the attribute, or the same value
     *   to leave it unchanged, or null to cause the current element
     *   to be ignored (unless the attributeValue argument is null).
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        return attributeValue;
    }

    /** If the container is a property named "script" contained
     *  by the JavaScript actor, then add "this." to certain
     *  function calls.
     *  @param container The object defined by the element that this
     *   is the end of.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @exception Exception if there is a problem substituting
     *  in the new value.
     */
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {

        // Fix the background color of the ViewScreen actor.
        // Note that the ViewScreen actor also has a name change.
        if (container != null && container.getName().equals("script")) {
            NamedObj actor = container.getContainer();

            if (actor != null
                    && actor.getClass().getName()
                    .startsWith("ptolemy.actor.lib.jjs.JavaScript")) {
                String value = ((Settable) container).getExpression().trim();

                // Prepend "this." to keywords that have leading whitespace.
                for (int i = 0; i < _keywords.length; i++) {
                    value = value.replaceAll(" " + _keywords[i] + "\\(", " this." + _keywords[i] +"(");
                    value = value.replaceAll("\t" + _keywords[i] + "\\(", "\tthis." + _keywords[i] +"(");
                }

                // If the old and new values are different, then print them out
                // and mark the container as modified.
                String previousValue = ((Settable) container).getExpression().trim();
                if (!value.equals(previousValue)) {
                    System.out.println("JavaScriptThisUpdate: " + actor.getFullName() + " has a script:\n" + previousValue);
                    System.out.println("That has been updated to:\n" + value);
                    // $PTII/ptolemy/util/test/Diff.java is not necessarily present.
                    if (_diff != null) {
                        System.out.println("The diff is:\n");
                        System.out.println(_diff.invoke(null, ((Settable) container).getExpression().trim(),
                                    value));
                    }
                    ((Settable) container).setExpression(value);
                    MoMLParser.setModified(true);
                }
            }
        }
    }

    /** Return a string that describes what the filter does.
     *  @return A description of the filter (ending with a newline).
     */
    @Override
    public String toString() {
        return getClass().getName()
            + ": Update script parameter of the JavaScript actor "
            + "by adding \"this.\" to certain locations";
    }

    // Keywords that get "this." prepended if the have leading
    // whitespace.
    private static String [] _keywords = {
        "addInputHandler",
        "connect",
        "extend",
        "get",
        "getParameter",
        "getResource",
        "implement",
        "input",
        "instantiate",
        "output",
        "parameter",
        "removeInputHandler",
        "send",
        "setDefault",
        "setParameter"};

    static {
        try {
            Class diffClass = Class.forName("ptolemy.util.test.Diff");
            _diff = diffClass.getDeclaredMethod("diff", new Class [] {String.class, String.class});
        } catch (ClassNotFoundException ex) {
            System.err.println("JavaScriptThisUpdated could not find ptolemy.util.test.Diff, so diffs will not be printed.");
        } catch (NoSuchMethodException ex2) {
            System.err.println("JavaScriptThisUpdated could not find ptolemy.util.test.Diff.diff(String, String), so diffs will not be printed.");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The ptolemy.util.test.Diff.diff(String, String) method. */
    protected static Method _diff = null;
}
