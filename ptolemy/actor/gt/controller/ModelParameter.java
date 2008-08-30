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
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Initializable;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.ActorToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ModelParameter

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ModelParameter extends Parameter implements Initializable {

    public ModelParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setTypeEquals(ActorToken.TYPE);
    }

    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedList<Initializable>();
        }
        _initializables.add(initializable);
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

    public CompositeEntity getModel() {
        return _model;
    }

    public Token getToken() throws IllegalActionException {
    CompositeEntity model = getModel();
    if (model == null) {
        model = new TypedCompositeActor(new Workspace());
    }
    return new ActorToken(model);
   }

    public void initialize() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }
    }

    public void modelChanged() {
        invalidate();
    }

    public void preinitialize() throws IllegalActionException {
        setModel(null);
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }
    }

    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        NamedObj oldContainer = getContainer();
        if (oldContainer instanceof Initializable) {
            ((Initializable) oldContainer).removeInitializable(this);
        }
        super.setContainer(container);
        if (container instanceof Initializable) {
            ((Initializable) container).addInitializable(this);
        }
    }

    public void setModel(CompositeEntity model) {
        _model = model;
        invalidate();
    }

    public void setToken(Token token) throws IllegalActionException {
        ActorToken actorToken = (ActorToken) ActorToken.TYPE.convert(token);
        setModel((CompositeEntity) actorToken.getEntity());
    }

    public void wrapup() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }
        setModel(null);
    }

    /** List of objects whose (pre)initialize() and wrapup() methods should be
     *  slaved to these.
     */
    private transient List<Initializable> _initializables;

    private CompositeEntity _model;
}
