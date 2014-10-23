/* Attribute containing information and methods for properties for web content,
 * for example, an HTML attribute that is part of an HTML element, as in the
 * "href" attribute of <a href="http://www.w3schools.com">This is a link</a>

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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// WebAttribute
/**
 * Class containing information and methods for properties for web content,
 * for example, an HTML attribute that is part of an HTML element, as in the
 * "href" attribute of &lt;a href="http://www.w3schools.com"&gt;This is a link&lt;/a&gt;
 *
 * The _elementName is used as the name of the attribute, e.g. "href" in
 * &lt;a href="http://www.w3schools.com"&gt;This is a link&lt;/a&gt;.  An object is not
 * allowed to have two attributes with the same _elementName, since it is
 * assumed that these attributes will belong to the same web element.  For
 * example, an HTML tag with two href elements should not be allowed, since
 * it is unclear what behavior occurs (first takes precedence?  second?  both?)
 * &lt;a href="http://site1.com" href="http://site2.com"&gt;This is a link&lt;/a&gt;
 *
 * @author Elizabeth Latronico
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ltrnc)
 * @Pt.AcceptedRating Red (ltrnc)
 */

public class WebAttribute extends StringAttribute {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public WebAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setWebName("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Factory method for creating WebAttributes that appends to the
     *  attribute if it already exists and does not have the specified
     *  content.  Return the modified or new WebAttribute.
     *  Set persistent to false.  This is static so that any
     *  WebExportable may call it.
     *
     * @param container  The container object for the WebAttribute
     * @param id The Ptolemy name for the WebAttribute (needed to ensure a
     * unique Ptolemy name)
     * @param webName The web name of this WebAttribute.
     * @param content The value of this WebAttribute.
     * @return The WebAttribute that was created (or that previously existed)
     * with persistent set to false
     * @exception IllegalActionException if the WebAttribute cannot be created
     * (perhaps another Attribute exists with the requested name)
     */
    public static WebAttribute appendToWebAttribute(NamedObj container,
            String id, String webName, String content)
            throws IllegalActionException {
        WebAttribute webAttribute = createWebAttribute(container, id, webName);

        String previousValue = webAttribute.getExpression();
        if (previousValue == null || previousValue.trim().length() == 0) {
            // No previous value.
            webAttribute.setExpression(content);
            return webAttribute;
        }

        // Assume values are space-separated, as they are with the class attribute.
        String[] previousValues = previousValue.split(" ");
        for (String value : previousValues) {
            if (value.equals(content)) {
                // Already present.
                return webAttribute;
            }
        }
        // Append to the previous value.
        webAttribute.setExpression(previousValue + " " + content);

        return webAttribute;
    }

    /** Return the web name of this element; for example, "myElement" in
     * &lt;div name="myElement"&gt; &lt;/div&gt; in HTML.
     *
     * @return The web name of this element; for example, "myElement" in
     * &lt;div name="myElement"&gt; &lt;/div&gt; in HTML.
     * @see #setWebName(String)
     */
    public String getWebName() {
        return _webName;
    }

    /** Set the web name of this element; for example, "myElement" in
     * &lt;div name="myElement"&gt; &lt;/div&gt; in HTML.
     *
     * @param webName The web name of this element; for example, "myElement" in
     * &lt;div name="myElement"&gt; &lt;/div&gt; in HTML.
     * @see #getWebName()
     */
    public void setWebName(String webName) {
        _webName = webName;
    }

    /** Factory method for creating WebAttributes.  Checks if there is an
     * existing WebAttribute with the same name.  If so, return it; if not
     * create a new one.  Sets persistent to false.  Static so that any
     * WebExportable may call it.
     *
     * @param container  The container object for the WebAttribute
     * @param id The Ptolemy name for the WebAttribute (needed to ensure a
     * unique Ptolemy name)
     * @param webName The web name of this WebAttribute
     * @return The WebAttribute that was created (or that previously existed)
     * with persistent set to false
     * @exception IllegalActionException if the WebAttribute cannot be created
     * (perhaps another Attribute exists with the requested name)
     */
    public static WebAttribute createWebAttribute(NamedObj container,
            String id, String webName) throws IllegalActionException {
        WebAttribute webAttribute;

        try {
            if (id != null && container.getAttribute(id) == null) {
                webAttribute = new WebAttribute(container, id);
                webAttribute.setPersistent(false);
            }
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(container,
                    "Cannot create web content.  Duplicate name for "
                            + "WebAttribute: " + id);
        }

        webAttribute = (WebAttribute) container.getAttribute(id,
                WebAttribute.class);
        webAttribute.setWebName(webName);
        webAttribute.setPersistent(false);
        webAttribute.setVisibility(NONE);
        return webAttribute;
    }

    /** The desired name of this attribute in the web file, for example,
     * "href" in &lt;a href="http://ptolemy.org"&gt;&lt;/div&gt;.
     */
    private String _webName;
}
