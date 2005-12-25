/* An attribute for instance-specific documentation.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// DocAttribute

/**
 FIXME
 
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DocAttribute extends Attribute {
    
    /** Construct a documentation attribute with the given name contained
     *  by the specified entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DocAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        description = new StringParameter(this, "Description");
        TextStyle style = new TextStyle(description, "_style");
        style.height.setExpression("10");
        style.width.setExpression("70");

        author = new StringAttribute(this, "Author");        
        version = new StringAttribute(this, "Version");
        since = new StringAttribute(this, "Since");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The author field. */
    public StringAttribute author;
    
    /** The description. */
    public StringParameter description;
    
    /** The since field. */
    public StringAttribute since;

    /** The version field. */
    public StringAttribute version;

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** The attribute name for a doc attribute, if it is present. */
    public static final String DOC_ATTRIBUTE_NAME = "_docAttribute";
}
