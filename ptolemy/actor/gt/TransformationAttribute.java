/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

package ptolemy.actor.gt;

import java.io.IOException;
import java.io.Writer;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.gt.TransformationAttributeEditorFactory;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationAttribute extends GTAttribute {

    /**
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public TransformationAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _init();
    }

    /**
     * @param workspace
     */
    public TransformationAttribute(Workspace workspace) {
        super(workspace);
    }

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == transformer) {
            String moml = transformer.getExpression();
            if (moml.equals("")) {
                _transformer = new ToplevelTransformer(workspace());
            } else {
                if (_parser == null) {
                    _parser = new MoMLParser();
                } else {
                    _parser.reset();
                }
                try {
                    _transformer = (ToplevelTransformer) _parser.parse(moml);
                } catch (Exception e) {
                    throw new IllegalActionException(this, e,
                            "Unable to parse transformer.");
                }
            }
        }
    }

    public ToplevelTransformer getTransformer() {
        return _transformer;
    }

    /** The editor factory for the transformer in this attribute.
     */
    public TransformationAttributeEditorFactory editorFactory;

    public StringAttribute transformer;
    
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        String moml = _transformer.exportMoML();
        try {
            transformer.setExpression(moml);
        } catch (IllegalActionException e) {
            throw new IOException("Cannot update the textual description of " +
                    "the transformer.", e);
        }
        super._exportMoMLContents(output, depth);
    }

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        transformer = new StringAttribute(this, "transformer");
        transformer.setExpression("");
        transformer.setPersistent(true);
        editorFactory = new TransformationAttributeEditorFactory(this,
                "editorFactory");
    }

    private MoMLParser _parser;

    private ToplevelTransformer _transformer;
}
