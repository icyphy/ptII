/* Utilities that manipulate a model.

 Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.actor.lib.hoc;


import java.net.URL;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// ModelUtilities
/**
A collection of utilities for manipulating a Ptolemy model.
FIXME: Currently there is only one major operation for invoke
the execution of a model. We may add more later.


@author Yang Zhao
@version $ $
@since Ptolemy II 3.0
*/
public class ModelUtilities {

    /** Instances of this class cannot be created.
     */
    private ModelUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** 
     * This method takes a url specifying the model to be execute. The
     * <i>args<i> argument is a record token that will be used to set
     * corresponding attributes of the spedified model by
     * naming match, (see _setAttribute() method). The results of 
     * executing the model is returned back by setting the value of some
     * Attributes. In particular, only Attributes 
     * that have name matches the <i>resultLabels<i> are returned. 
     * The return result is a RecordToken which has the resultLabels as 
     * its feild.
     * @param url The Model url.
     * @param args A set of attributes of the specified model.
     * @param resultLabels Labels of the returned result.
     * @return The execution result.
     * @exception IllegalActionException If can not parse the url
     * or failed to execute the model.
     */
    public static synchronized RecordToken executeModel
            (URL url, RecordToken args, String[] resultLabels) 
             throws IllegalActionException {
        if (url != null) {
            MoMLParser parser = new MoMLParser();
            NamedObj model;
            try {
                model = parser.parse(null, url);
            } catch (Exception ex) {
                throw new IllegalActionException(
                    ex +
                    "Failed to pass the model URL." + 
                    url.toString());
            }
            if (model instanceof CompositeActor) {
                return executeModel((CompositeActor)model, args, resultLabels);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    /** 
     * This method takes model argument which is type of CompositeActor. 
     * The <i>args<i> argument is a record token that will be used to
     * set corresponding attributes of the spedified model by
     * naming match, (see _setAttribute() method). The results of 
     * executing the model is returned back by setting the value of some
     * Attributes. In particular, only Attributes 
     * that have name matches the <i>resultLabels<i> are returned. 
     * The return result is a RecordToken which has the resultLabels as 
     * its feild.
     * @param model The Model.
     * @param args A set of attributes of the specified model.
     * @param resultLabels Labels of the returned result.
     * @return The execution result.
     * @exception IllegalActionException If failed to execute the model.
     */
    public static synchronized RecordToken executeModel
            (CompositeActor model, RecordToken args, 
            String[] resultLabels) throws IllegalActionException {
        Manager manager = model.getManager();
        if(manager == null) {
            //System.out.println("create manager for the model");
            manager = new Manager(model.workspace(), "Manager");
            model.setManager(manager);
        }
        _setAttribute(model, args);

        try {
            manager.execute();
        } catch (KernelException ex) {
            throw new IllegalActionException(ex+
            "Execution failed.");
        }
        return _getResult(model, resultLabels);
    }
///////////////////////////////////////////////////////////////////
////                        private methods                    ////

/** Iterate over the labelSet of the <i>args<i> argument and 
 *  check whether the specified model has Attribute with the
 *  same name of a label. If so, set the value of the attribute
 *  to be the value of that record feild.
 *  @exception IllegalActionException If reading the ports or
 *   setting the parameters causes it.
 */
    private static void _setAttribute(CompositeActor model, 
        RecordToken args) throws IllegalActionException {
        Object[] labels = args.labelSet().toArray();
        int length = args.length();
        //String[] labels = new String[length];
        //Token[] values = new Token[length];
        for (int i = 0; i < length; i++) {
            String label = (String)labels[i];
            Attribute attribute = model.getAttribute(label);
            // Use the token directly rather than a string if possible.
            if(attribute != null) {
                Token token = args.get(label);
                if (attribute instanceof Variable) {
                    ((Variable) attribute).setToken(token);
                } else if (attribute instanceof Settable) {
                    ((Settable) attribute).
                        setExpression(token.toString());
                }
            }
        }
    }    
    
/** Iterate over the resultLabels and 
 *  check whether the specified model has Attribute with the
 *  same name of a label. If so, get the value of the attribute
 *  and return a record token with labels equal to resultLabels
 *  and values equal to the corresponding attribute value.
 *  @param model The model executed.
 *  @param resultLabels Labels of the returned result.
 *  @return The execution result.
 *  @exception IllegalActionException If reading the ports or
 *   setting the parameters causes it.
 */
private static RecordToken _getResult(CompositeActor model,
        String[] resultLabels) throws IllegalActionException {
    Token[] value = new Token[resultLabels.length];
    for (int i = 0; i < resultLabels.length; i++) {
        String label = resultLabels[i];
        Attribute attribute = model.getAttribute(label);
        if (attribute instanceof Variable) {
            value[i] =((Variable) attribute).getToken();
        } 
    }    
    return new RecordToken(resultLabels, value);
}

}
