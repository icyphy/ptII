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

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// IconScript
/**
 * A parameter associating a JavaScript with an icon in model.
 * This class assumes that the icon becomes an area in an image map
 * on an HTML page. This parameter provides a way to specify a
 * script to execute when that area in the image map is the target
 * of some UI event, such as mouse movement or clicking or keyboard
 * action. A typical use of this would be to set its string value
 * to something like "foo(args)" where foo is a JavaScript function
 * defined in the <i>script</i> parameter.
 * You can also provide HTML text to insert into the start or
 * end sections of the container's container's web page.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class IconScript extends Script implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public IconScript(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        startText = new StringParameter(this, "startText");
        TextStyle style = new TextStyle(startText, "style");
        style.height.setExpression("5");

        endText = new StringParameter(this, "endText");
        style = new TextStyle(endText, "style");
        style.height.setExpression("5");

        jQueryLibraries = new StringParameter(this, "jQueryLibraries");
        style = new TextStyle(jQueryLibraries, "style");
        style.height.setExpression("5");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Text to insert in the end section of the
     *  web page. This text will be inserted exactly once.
     */
    public StringParameter endText;

    /** Text to insert in the start section of the
     *  web page. This text will be inserted exactly once.
     */
    public StringParameter startText;

    /** jQuery libraries to be included in the HEAD section of the html file
     * The path to the libraries will be copied in the same order as given.
     */
    public StringParameter jQueryLibraries;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Provide content to the specified web exporter to be
     *  included in a web page.
     *  This class provides an area attribute, and also
     *  the value of <i>script</i>, <i>startText</i>,
     *  and <i>endText</i>, if any has been provided.
     *  These value get inserted into the container's container's
     *  corresponding HTML sections, where the <i>script</i>
     *  is inserted inside a JavaScript HTML element.
     *
     *  @param exporter The web exporter to add content to
     *  @exception IllegalActionException If evaluating the value
     *   of this parameter fails.
     */

    //FIXME:  From DefaultIconScript - what did previous file do?
    //*  If the <i>eventType</i> parameter is "default", then
    //*  remove all previously defined defaults and use the global
    //*  defaults.
    @Override
    protected void _provideElements(WebExporter exporter)
            throws IllegalActionException {

        // All content covered?
        // 1) the script itself, and
        // 2) the method call to invoke the script
        // 3) data, and
        // 4) <divs> that the script will change the content of -> target <div>

        WebElement webElement;
        String jQueryImports = jQueryLibraries.stringValue();
        if (!jQueryImports.trim().equals("")) {
            //Create WebElement for jQueryLibraries and add the exporter.
            //content should only be added once(<onceOnly->true)
            webElement = WebElement.createWebElement(getContainer(),
                    "jQueryLibraries", "jQueryLibraries");
            webElement.setParent(WebElement.HEAD);
            webElement.setExpression(jQueryImports);
            exporter.defineElement(webElement, true);
        }

        String scriptValue;
        // Check whether the user wants to insert the evaluated expression
        // or the exact text for the script
        if (evaluateScript.getToken().isEqualTo(BooleanToken.TRUE)
                .booleanValue()) {
            scriptValue = script.stringValue();
        } else {
            scriptValue = script.getExpression();
        }

        if (!scriptValue.trim().equals("")) {
            // Create WebElement for script and add to exporter.
            // Content should only be added once (onceOnly -> true).
            webElement = WebElement.createWebElement(getContainer(), "script",
                    "script");
            webElement.setParent(WebElement.HEAD);
            webElement.setExpression("<script type=\"" + getMimeType()
                    + "\">\n" + scriptValue + "\n</script>\n");
            exporter.defineElement(webElement, true);
        }

        String startTextValue = startText.stringValue();
        if (!startTextValue.trim().equals("")) {
            // Create WebElement for start text and add to exporter.
            // Content should only be added once (onceOnly -> true).
            webElement = WebElement.createWebElement(getContainer(),
                    "startText", "startText");
            webElement.setParent(WebElement.START);
            webElement.setExpression(startTextValue);
            exporter.defineElement(webElement, true);
        }

        String endTextValue = endText.stringValue();
        if (!endTextValue.trim().equals("")) {
            // Create WebElement for end text and add to exporter.
            // Content should only be added once (onceOnly -> true).
            webElement = WebElement.createWebElement(getContainer(), "endText",
                    "endText");
            webElement.setParent(WebElement.END);
            webElement.setExpression(endTextValue);
            exporter.defineElement(webElement, true);
        }
    }

    /** Provide method call to invoke script that can be included as an
     *  attribute of an HTML tag, e.g. onclick="runFunction()" in
     *  &lt;button onclick="runFunction()"/&gt;
     *
     *  @param exporter  The web exporter to which to write content.
     *  @exception IllegalActionException If the eventType cannot be obtained,
     *  the web attribute cannot be created or set.
     */
    @Override
    protected void _provideAttributes(WebExporter exporter)
            throws IllegalActionException {

        // FIXME:  Support multiple events in the future.  E.g. onclick() and
        // ontap() might call the same Javascript method.
        WebAttribute webAttribute;

        NamedObj container = getContainer();
        if (container != null) {
            String eventTypeValue = eventType.stringValue();
            if (!eventTypeValue.trim().equals("")) {
                // Create WebAttribute for event and add to exporter.
                // Content should only be added once (onceOnly -> true).
                webAttribute = WebAttribute.createWebAttribute(getContainer(),
                        eventTypeValue + "WebAttribute", eventTypeValue);
                webAttribute.setExpression(stringValue());
                exporter.defineAttribute(webAttribute, true);
            }
        }
    }
}
