/*

 Copyright (c) 2008-2009 The Regents of the University of California.
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

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// Clone

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Clone extends GTEvent {

    /**
     *  @param container
     *  @param name
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public Clone(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        useMoml = new Parameter(this, "useMoml");
        useMoml.setTypeEquals(BaseType.BOOLEAN);
        useMoml.setToken(BooleanToken.FALSE);
    }

    public RefiringData fire(Token arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        ModelParameter modelParameter = getModelParameter();
        CompositeEntity oldModel = modelParameter.getModel();
        boolean useMoml = ((BooleanToken) this.useMoml.getToken())
                .booleanValue();
        CompositeEntity model;
        if (useMoml) {
            String moml = oldModel.exportMoML();
            MoMLParser parser = new MoMLParser();
            try {
                model = (CompositeEntity) parser.parse(moml);
            } catch (Exception e) {
                throw new IllegalActionException(this, e,
                        "Unable to parse moml.");
            }
        } else {
            try {
                model = (CompositeEntity) oldModel.clone(oldModel.workspace());
            } catch (CloneNotSupportedException e) {
                throw new IllegalActionException(this, e,
                        "Unable to clone the model.");
            }
        }
        modelParameter.setModel(model);

        return data;
    }

    public Parameter useMoml;
}
