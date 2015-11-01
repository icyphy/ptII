/*  XML handler to be used for "configure" code when parsing a MoML model
 that contains a PtalonActor.

 Copyright (c) 2006-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.actor.ptalon;

import java.util.Hashtable;

import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;

import com.microstar.xml.HandlerBase;

/**
 XML handler to be used for "configure" code when parsing a MoML model
 that contains a PtalonActor.

 @author Adam Cataldo, Elaine Cheong
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)

 */
public class PtalonMLHandler extends HandlerBase {

    /** Create a PtalonMLHandler, which will be used to recover the
     *  AST and code manager specified in the PtalonML.
     *  @param actor The actor to associate with this handler.
     */
    public PtalonMLHandler(PtalonActor actor) {
        super();
        _attributes = new Hashtable<String, String>();
        _actor = actor;
    }

    /** Process a PtalonML attribute.
     *  @param aname The name of the attribute.
     *  @param value The value of the attribute, or null if the
     *  attribute is <code>#IMPLIED</code>.
     *  @param isSpecified True if the value was specified, false if
     *  it was defaulted from the DTD.
     *  @exception java.lang.Exception If there is any trouble
     *  creating the AST or code manager,
     */
    @Override
    public void attribute(String aname, String value, boolean isSpecified)
            throws Exception {
        if (aname != null && value != null) {
            _attributes.put(aname, value);
        }
    }

    /** Process the end of a PtalonML element.
     *  @param elname The element type name.
     *  @exception java.lang.Exception If there is any trouble creating
     *  the AST or code manager.
     */
    @Override
    public void endElement(String elname) throws Exception {

    }

    /** Process the start of a PtalonML element.
     *  @param elname The element type name.
     *  @exception java.lang.Exception If there is any trouble creating
     *  the AST or code manager,
     */
    @Override
    public void startElement(String elname) throws Exception {
        if (elname.equals("ptalon")) {
            if (_attributes.containsKey("file")) {
                String name = _attributes.get("file");
                name = name.replace(".", "/");
                name = name + ".ptln";
                // Don't append ptolemy.ptII.dir, it will fail under
                // WebStart
                //File file = new File(StringUtilities
                //        .getProperty("ptolemy.ptII.dir"));
                //file = new File(file, name);
                //_actor.ptalonCodeLocation.setToken(new StringToken(file
                //        .toString()));
                if (name.startsWith("/")) {
                    name = name.substring(1);
                } else {
                    name = "$CLASSPATH/" + name;
                }
                _actor.ptalonCodeLocation.setToken(new StringToken(name));
            }
        } else if (elname.equals("ptalonParameter")) {
            if (_attributes.containsKey("name")
                    && _attributes.containsKey("value")) {
                PtalonParameter param = (PtalonParameter) _actor
                        .getAttribute(_attributes.get("name"));
                param.setToken(new StringToken(_attributes.get("value")));
            }
        } else if (elname.equals("ptalonExpressionParameter")) {
            if (_attributes.containsKey("name")
                    && _attributes.containsKey("value")) {
                PtalonExpressionParameter param = (PtalonExpressionParameter) _actor
                        .getAttribute(_attributes.get("name"));
                if (param == null) {
                    throw new IllegalActionException(_actor,
                            "Failed to get parameter \"name\" from actor. "
                                    + "\"name\" attribute was: "
                                    + _attributes.get("name"));
                }
                param.setExpression(_attributes.get("value"));
                _actor.attributeChanged(param);
            }
        }
        _attributes.clear();
    }

    /** The actor that created this handler.
     */
    PtalonActor _actor;

    /** Each element in this hashtable maps a name to a value.
     */
    Hashtable<String, String> _attributes;

}
