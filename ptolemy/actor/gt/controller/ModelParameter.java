/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import java.io.IOException;
import java.io.Writer;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gt.controller.ModelAttribute.ModelListener;
import ptolemy.data.ActorToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// RefinementModelParameter

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ModelParameter extends Parameter implements ModelListener{

    public ModelParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setTypeEquals(ActorToken.TYPE);
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModelParameter newObject = (ModelParameter) super
                .clone(workspace);
        newObject._modelAttribute = null;
        return newObject;
    }

    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (_isMoMLSuppressed(depth)) {
            return;
        }

        output.write(_getIndentPrefix(depth) + "<" + _elementName + " name=\""
                + name + "\" class=\"" + getClassName() + "\">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</" + _elementName + ">\n");
    }

    public Token getToken() throws IllegalActionException {
        CompositeEntity model = null;
        if (_modelAttribute != null) {
            model = _modelAttribute.getModel();
        }
        if (model == null) {
            model = new TypedCompositeActor(new Workspace());
        }
        return new ActorToken(model);
    }

    public void modelChanged() {
        invalidate();
    }

    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        if (!(container instanceof GTEvent)) {
            throw new IllegalActionException("ModelParameter can be " +
                    "associated with GTEvent only.");
        }

        super.setContainer(container);

        ModelAttribute newModelAttribute = ((GTEvent) container)
                .getModelAttribute();
        if (_modelAttribute != newModelAttribute) {
            if (_modelAttribute != null) {
                _modelAttribute.removeModelListener(this);
            }
            _modelAttribute = newModelAttribute;
            if (_modelAttribute != null) {
                _modelAttribute.addModelListener(this);
            }
        }
    }

    public void setToken(Token token) throws IllegalActionException {
        if (_modelAttribute != null) {
            ActorToken actorToken = (ActorToken) ActorToken.TYPE.convert(token);
            _modelAttribute.setModel((CompositeEntity) actorToken.getEntity());
        }
    }

    private ModelAttribute _modelAttribute;
}
