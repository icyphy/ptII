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

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.gt.TransformationAttributeController;
import ptolemy.vergil.gt.TransformationAttributeEditorFactory;
import ptolemy.vergil.gt.TransformationAttributeIcon;

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
        if (attribute == applicability) {
            if (!(applicability.getToken() instanceof BooleanToken)) {
                throw new IllegalActionException(this, "Applicability of a " +
                        "TransformationAttribute must be evaluated to a " +
                        "Boolean token.");
            }
        }
    }

    public Parameter applicability;

    /** The editor factory for the transformer in this attribute.
     */
    public TransformationAttributeEditorFactory editorFactory;

    public TransformerAttribute transformer;

    public static class TransformerAttribute extends StringAttribute {

        public TransformerAttribute(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        public void exportMoML(Writer output, int depth, String name)
        throws IOException {
            if (_transformer != null) {
                try {
                    setExpression(_transformer.exportMoML());
                } catch (IllegalActionException ex) {
                    IOException ioException = new IOException("Unable to "
                            + "obtain MoML string from transformer.");
                    ioException.initCause(ex);
                    throw ioException;
                }
            }
            super.exportMoML(output, depth, name);
        }

        public synchronized ToplevelTransformer getTransformer() {
            return _transformer;
        }

        public synchronized void setTransformer(ToplevelTransformer transformer) {
            _transformer = transformer;
        }

        private ToplevelTransformer _transformer;
    }

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        transformer = new TransformerAttribute(this, "transformer");
        transformer.setExpression("");
        transformer.setPersistent(true);
        transformer.setVisibility(Variable.EXPERT);

        applicability = new Parameter(this, "applicability");
        applicability.setExpression("true");

        new TransformationAttributeIcon(this, "_icon");

        new TransformationAttributeController.Factory(this, "_controllerFactory");

        editorFactory = new TransformationAttributeEditorFactory(this,
                "editorFactory");
    }
}
