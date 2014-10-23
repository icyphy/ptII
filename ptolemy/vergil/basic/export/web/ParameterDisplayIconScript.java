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

import java.util.List;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// ParameterDisplayIconScript
/**
 * A parameter specifying default JavaScript actions to associate
 * with icons in model. Putting this into a model causes an action
 * to be associated with each icon (as specified by the <i>include</i>
 * and <i>instancesOf</i> parameters) that, on moving the mouse over
 * the icon, displays in a table the parameters of the corresponding
 * Ptolemy II object. The table is displayed in a tooltip.
 * <p>
 * This parameter is designed to be included in a Configuration file
 * to specify global default behavior for export to Web. Just put
 * it in the top level of the Configuration, and this behavior
 * will be provided by default.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ParameterDisplayIconScript extends DefaultIconScript {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public ParameterDisplayIconScript(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override base class since content here should only be added if
     * it does not exist already.
     *
     * @return False, since default content should only be added if no content
     * already exists
     */
    @Override
    public boolean isOverwriteable() {
        return false;
    }

    /** Get an HTML table describing the parameters of the object.
     *  @param object The Ptolemy object to return a table for.
     *  @return An HTML table displaying the parameter values for the
     *   specified object, or the string "Has no parameters" if the
     *   object has no parameters.
     */
    public static String getParameterTable(NamedObj object) {
        StringBuffer table = new StringBuffer();
        List<Settable> parameters = object.attributeList(Settable.class);
        boolean hasParameter = false;
        for (Settable parameter : parameters) {
            if (parameter.getVisibility().equals(Settable.FULL)) {
                hasParameter = true;
                table.append("<tr><td>");
                table.append(parameter.getName());
                table.append("</td><td>");
                String expression = parameter.getExpression();
                expression = StringUtilities.escapeForXML(expression);
                expression = expression.replaceAll("'", "\\\\'");
                // Bizarrely, escaping all characters except newlines work.
                // Newlines need to be converted to \n.
                // No idea why so many backslashes are required below.
                expression = expression.replaceAll("&#10;", "\\\\\\n");

                if (expression.length() == 0) {
                    expression = "&nbsp;";
                }
                table.append(expression);
                table.append("</td><td>");
                String value = parameter.getValueAsString();
                value = StringUtilities.escapeForXML(value);
                value = value.replaceAll("'", "\\\\'");
                // Bizarrely, escaping all characters except newlines work.
                // Newlines need to be converted to \n.
                // No idea why so many backslashes are required below.
                value = value.replaceAll("&#10;", "\\\\\\n");

                if (value.length() == 0) {
                    value = "&nbsp;";
                }
                table.append(value);
                table.append("</td></tr>");
            }
        }
        if (hasParameter) {
            table.insert(0, "<table border=\"1\">"
                    + "<tr><td><b>Parameter</b></td>"
                    + "<td><b>Expression</b></td>"
                    + "<td><b>Value</b></td></tr>");
            table.append("</table>");
        } else {
            table.append("Has no parameters.");
        }
        return table.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to provide the parameter table
     *  for the specified object.
     *  Provide default content to the specified web exporter to be
     *  included in a web page for the container of this object for
     *  objects that do not override onmouseover.
     *  This class provides an area attribute of type
     *  "onmouseover" that displays the parameter values
     *  of the object and one of type "onmouseout" that
     *  clears that display.
     *
     *  @param exporter The exporter to which to provide the content.
     *  @param object The object which provides the content.
     *  @exception IllegalActionException If there is a problem creating the content
     * or if there is a name duplication with the created attributes
     */
    @Override
    protected void _provideDefaultAttributes(NamedObj object,
            WebExporter exporter) throws IllegalActionException {

        WebAttribute webAttribute;

        String command = "<h2>" + object.getName() + "</h2>"
                + getParameterTable(object);

        // Create WebAttribute for the class "tooltip".
        webAttribute = WebAttribute.appendToWebAttribute(object,
                "classWebAttribute", "class", "tooltip");
        exporter.defineAttribute(webAttribute, true);

        // Content of the tooltip.
        webAttribute = WebAttribute.createWebAttribute(object,
                "titleWebAttribute", "title");
        webAttribute.setExpression(command);
        exporter.defineAttribute(webAttribute, true);
    }

    /** Provide default content to the specified web exporter to be
     *  included in a web page for the container of this object for
     *  objects that do not override onmouseover.  This class provides
     *  an HTML table containing parameter names and values, plus a
     *  placeholder &lt;div&gt; tag which the script writes
     *  information in upon onmouseover().
     *
     *  @param exporter The exporter to which to provide the content.
     *  @exception IllegalActionException If evaluating the value
     *   of this parameter fails.
     */
    @Override
    protected void _provideElements(WebExporter exporter)
            throws IllegalActionException {
        // FIXME:  How to do this, from old comments?
        //*  If the <i>eventType</i> parameter is "default", then
        //*  remove all previously defined defaults and use the global
        //*  defaults.

        WebElement webElement;

        // Add content from IconScript (DefaultIconScript does not override
        // _provideElements)
        super._provideElements(exporter);

        // Define the JavaScript command writeText.  Script with this name
        // should only be included once (onceOnly -> true)
        /* Replaced by tooltipster
        webElement = WebElement.createWebElement(getContainer(),
                "writeTextScriptWebElement", "writeTextScript");
        webElement.setParent(WebElement.HEAD);
        webElement
                .setExpression("<script type=\"text/javascript\">\n"
                        + "function writeText(text) {\n"
                        + "   document.getElementById(\"afterImage\").innerHTML = text;\n"
                        + "};\n" + "</script>");
        exporter.defineElement(webElement, true);
         */

        // Define the JavaScript command to initialize tooltipster,
        // the JQuery library being used to display parameters.  Script with this name
        // should only be included once (onceOnly -> true)
        webElement = WebElement.createWebElement(getContainer(),
                "tooltipsterScriptWebElement", "tooltipster");
        webElement.setParent(WebElement.HEAD);
        webElement.setExpression("<script type=\"text/javascript\">\n"
                + "$(document).ready(function() {\n"
                + "  $('.tooltip').tooltipster({\n"
                + "    contentAsHTML: true\n" + "  });\n" + "});\n"
                + "</script>");
        exporter.defineElement(webElement, true);

        // Put a destination paragraph in the end section of the HTML.
        webElement = WebElement.createWebElement(getContainer(),
                "afterImageWebElement", "afterImage");
        webElement.setParent(WebElement.END);
        webElement
        .setExpression("<div id=\"afterImage\">\n"
                + "  <script type=\"text/javascript\">\n"
                + "     writeText('Mouse over the icons to see their parameters. "
                + "Click on composites and plotters to reveal their contents (if provided).');\n"
                + "  </script>\n"
                + "  <noscript>\n"
                + "     Your browser does not support JavaScript so moving the mouse\n"
                + "     over the actors will not display their parameters. To enable\n"
                + "     JavaScript, consult the security preferences of your browser.\n"
                + "     <br/>See <a href=\"http://support.microsoft.com/gp/howtoscript\"><code>http://support.microsoft.com/gp/howtoscript</code></a> for details.\n"
                + "  </noscript>\n" + "</div>");
        exporter.defineElement(webElement, true);
    }
}
