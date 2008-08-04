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
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;

import ptolemy.actor.TypedActor;
import ptolemy.actor.gt.GraphMatcher;
import ptolemy.actor.gt.GraphTransformer;
import ptolemy.actor.gt.MatchCallback;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.TransformationException;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.data.ActorToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.ParserScope;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// GTTask

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Transform extends GTEvent implements Configurable, MatchCallback {

    public Transform(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _transformation = new TransformationRule(workspace());
        _transformation.setName("Transformation");
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Transform newObject = (Transform) super.clone(workspace);
        newObject._transformation = (TransformationRule) _transformation.clone(
                workspace);
        newObject._matchResult = null;
        return newObject;
    }

    public void configure(URL base, String source, String text)
            throws Exception {
        _configureSource = source;
        text = text.trim();
        if (!text.equals("")) {
            workspace().remove(_transformation);
            MoMLParser parser = new MoMLParser(workspace());
            _transformation = (TransformationRule) parser.parse(base,
                    new StringReader(text));
        }
    }

    public void fire(ArrayToken arguments) throws IllegalActionException {
        ParserScope scope = _getParserScope(arguments);
        actions.execute(scope);

        CompositeEntity model = (CompositeEntity) _getModelArgument(arguments)
                .getEntity(new Workspace());
        model.setDeferringChangeRequests(false);

        Pattern pattern = _transformation.getPattern();
        GraphMatcher matcher = new GraphMatcher();
        matcher.setMatchCallback(this);
        _matchResult = null;
        matcher.match(pattern, model);
        if (_matchResult != null) {
            try {
                GraphTransformer.transform(_transformation, _matchResult);
            } catch (TransformationException e) {
                throw new IllegalActionException("Unable to transform model.");
            }
        }

        _scheduleEvents(scope, new ActorToken(model), BooleanToken.getInstance(
        		_matchResult != null));
    }

    public boolean foundMatch(GraphMatcher matcher) {
        _matchResult = (MatchResult) matcher.getMatchResult().clone();
        return false;
    }

    public String getConfigureSource() {
        return _configureSource;
    }

    public String getConfigureText() {
        return null;
    }

    public TypedActor[] getRefinement() {
        return new TypedActor[] {_transformation};
    }

    protected void _exportMoMLContents(Writer output, int depth)
    throws IOException {
        super._exportMoMLContents(output, depth);

        String sourceSpec = "";

        if ((_configureSource != null) && !_configureSource.trim().equals("")) {
            sourceSpec = " source=\"" + _configureSource + "\"";
        }

        output.write(_getIndentPrefix(depth) + "<configure" + sourceSpec +
                ">\n");
        _transformation.exportMoML(output, depth + 1);
        output.write("</configure>\n");
    }

    protected MatchResult _matchResult;

    protected TransformationRule _transformation;

    private String _configureSource;
}
