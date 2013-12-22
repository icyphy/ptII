/* An attribute that when double clicked brings up a mapping editor.

 Copyright (c) 2013 The Regents of the University of California.
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
package ptolemy.domains.metroII.kernel;

import java.util.List;

import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.domains.metroII.gui.MappingEditorGUIFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


/** An attribute that when double clicked brings up a mapping editor.
 
 @author Liangpeng Guo
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (eal)
 */
public class MappingEditor extends Attribute {

    /** Construct a MappingEditor attribute.
     *  @param container the container of the attribute
     *  @param name the name of the attribute.
     *  @exception IllegalActionException If the attribute cannot be constructed.
     *  @exception NameDuplicationException If there is a name collision.
     */
    public MappingEditor(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:11; font-family:SansSerif; fill:white\">"
                + "Double click to\nedit mapping.</text></svg>");

        new MappingEditorGUIFactory(this, "_mappingEditorGUIFactory");
    }

}
