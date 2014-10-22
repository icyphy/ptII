/* An actor to combine the input model tokens into one.

@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.actor.gt;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ActorToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

/**
 An actor to combine the input model tokens into one.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ModelCombine extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModelCombine(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(ActorToken.TYPE);
        input.setMultiport(true);

        output.setTypeEquals(ActorToken.TYPE);
    }

    /** Read all available input tokens as models, and combine those models into
     *  a larger model. Send the result model to the output.
     *
     *  @exception IllegalActionException If the input tokens cannot be read, or
     *   the models cannot be combined.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Entity entity = ((ActorToken) input.get(0)).getEntity(new Workspace());
        for (int i = 1; i < input.getWidth(); i++) {
            _merge(entity,
                    ((ActorToken) input.get(i)).getEntity(new Workspace()));
        }
        output.send(0, new ActorToken(entity));
    }

    /** Return true if there is any input token available.
     *
     *  @return true if there is any input token available.
     *  @exception IllegalActionException If availability of the tokens cannot
     *   be tested.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();
        if (result) {
            int width = input.getWidth();
            if (width == 0) {
                result = false;
            } else {
                for (int i = 0; i < width; i++) {
                    if (!input.hasToken(i)) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /** Merge the two entities with the first one being the primary and modify
     *  the first entity to be the result.
     *
     *  @param entity1 The first entity.
     *  @param entity2 The second entity.
     */
    protected void _merge(Entity entity1, Entity entity2) {
        StringBuffer moml = new StringBuffer(entity2.exportMoMLPlain().trim());
        int eol = moml.indexOf("\n");
        if (eol >= 0) {
            moml.delete(0, eol + 1);
            eol = moml.lastIndexOf("\n");
            if (eol >= 0) {
                moml.delete(eol, moml.length());
                moml.insert(0, "<group name=\"auto\">\n");
                moml.insert(0, StringUtilities.getIndentPrefix(1));
                moml.append("\n");
                moml.append(StringUtilities.getIndentPrefix(1));
                moml.append("</group>");
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        entity1, moml.toString());
                request.execute();
            }
        }
    }
}
