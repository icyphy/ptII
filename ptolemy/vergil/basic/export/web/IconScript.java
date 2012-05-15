/* Interface for parameters that provide web export content.

 Copyright (c) 2011 The Regents of the University of California.
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
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class IconScript extends WebContent implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public IconScript(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _icon.setIconText("S");
        displayText.setExpression("Web page script to run on containers icon.");

        eventType = new AreaEventType(this, "eventType");
        
        script = new StringParameter(this, "script");
        TextStyle style = new TextStyle(script, "style");
        style.height.setExpression("5");
        
        startText = new StringParameter(this, "startText");
        style = new TextStyle(startText, "style");
        style.height.setExpression("5");

        endText = new StringParameter(this, "endText");
        style = new TextStyle(endText, "style");
        style.height.setExpression("5");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** Text to insert in the end section of the
     *  web page. This text will be inserted exactly once.
     */
    public StringParameter endText;
    
    /** Event type to respond to by executing the command given by
     *  the value of this IconScript parameter.
     *  The script will be run when the icon corresponding to the
     *  container of this parameter gets one of the following events:
     *  <ul>
     *  <li><b>onblur</b>: Command to be run when an element loses focus.
     *  <li><b>onclick</b>: Command to be run on a mouse click.
     *  <li><b>ondblclick</b>: Command to be run on a mouse double-click.
     *  <li><b>onfocus</b>: Command to be run when an element gets focus.
     *  <li><b>onmousedown</b>: Command to be run when mouse button is pressed.
     *  <li><b>onmousemove</b>: Command to be run when mouse pointer moves.
     *  <li><b>onmouseout</b>: Command to be run when mouse pointer moves out of an element.
     *  <li><b>onmouseover</b>: Command to be run when mouse pointer moves over an element.
     *  <li><b>onmouseup</b>: Command to be run when mouse button is released.
     *  <li><b>onkeydown</b>: Command to be run when a key is pressed.
     *  <li><b>onkeypress</b>: Command to be run when a key is pressed and released.
     *  <li><b>onkeyup</b>: Command to be run when a key is released.
     *  </ul>
     *  These are the events supported by the HTML area tag.
     *  The default is "onmouseover".
     */
    public AreaEventType eventType;
    
    /** JavaScript to insert in the head section of the
     *  web page. This will normally define a JavaScript function that
     *  will be invoked when the UI event specified by <i>eventType</i>
     *  occurs. By default, this is blank. For example, if the value
     *  of this parameter is the string
<pre>
function writeText(text) {
    document.getElementById("xyz").innerHTML = text;
};
</pre>
     * and the value of this parameter is "writeText('hello world')",
     * then the HTML element with ID xyz will be populated with the
     * string 'hello world' when the UI action <i>eventType</i> occurs.
     */
    public StringParameter script;
    
    /** Text to insert in the start section of the
     *  web page. This text will be inserted exactly once.
     */
    public StringParameter startText;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class provides only outside content, so this method
     *  does nothing.
     *  @throws IllegalActionException If a subclass throws it.
     */
    public void provideContent(WebExporter exporter) throws IllegalActionException {
    }

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class provides an area attribute, and also
     *  the value of <i>script</i>, <i>startText</i>,
     *  and <i>endText</i>, if any has been provided.
     *  These value get inserted into the container's container's
     *  corresponding HTML sections, where the <i>script</i>
     *  is inserted inside a JavaScript HTML element.

     *  @throws IllegalActionException If evaluating the value
     *   of this parameter fails.
     */
    public void provideOutsideContent(WebExporter exporter) throws IllegalActionException {
        NamedObj container = getContainer();
        if (container != null) {
            String eventTypeValue = eventType.stringValue();
            if (!eventTypeValue.trim().equals("")) {
                // Last argument specifies to overwrite any previous value defined.
                exporter.defineAreaAttribute(container, eventTypeValue, stringValue(), true);
            }
        }
        String scriptValue = script.stringValue();
        if (!scriptValue.trim().equals("")) {
            exporter.addContent("head", true, "<script type=\"text/javascript\">\n"
                    + scriptValue
                    + "\n</script>\n");
        }
        
        String startTextValue = startText.stringValue();
        if (!startTextValue.trim().equals("")) {
            exporter.addContent("start", true, startTextValue);
        }

        String endTextValue = endText.stringValue();
        if (!endTextValue.trim().equals("")) {
            exporter.addContent("end", true, endTextValue);
        }
    }
}
