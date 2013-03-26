/* This attribute is used to specify that a given Port is mediated by
 *  a QuantityManager.

@Copyright (c) 2010-2013 The Regents of the University of California.
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


package ptolemy.actor.lib.qm;

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.QuantityManager;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;


/** This attribute is used to specify that a given Port is mediated by
 *  a {@link QuantityManager}. QuantityManager specific attributes are
 *  stored in the QuantityManager but configured through this attribute.  
 *  @author Patricia Derler, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class QuantityManagerAttribute extends Parameter {
    
    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public QuantityManagerAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        setPersistent(false);
    }

    /** Construct a Parameter with the given container, name, and Token.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  @param container The container.
     *  @param name The name.
     *  @param token The Token contained by this Parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an parameter already in the container.
     */
    public QuantityManagerAttribute(NamedObj container, String name, ptolemy.data.Token token)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, token);
        setPersistent(false);
        //setTypeEquals(new ArrayType(BaseType.DOUBLE));
    }
    
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (getContainer() instanceof Port) {
            if (_quantityMananger == null) {
                _quantityMananger = (QuantityManager) ((ObjectToken)getToken()).getValue();
            }
            _quantityMananger.setPortAttribute((Port) getContainer(), attribute);
            }
        super.attributeChanged(attribute);
    } 
    
    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        QuantityManagerAttribute newObject = (QuantityManagerAttribute) super.clone(workspace); 
        newObject._quantityMananger = null; 
        return newObject;
        
    }
    
    
    
    /** Update attributeList.
     *  @exception IllegalActionException Thrown by super class.
     */
    public void setToken(Token token) throws IllegalActionException {
        // TODO Auto-generated method stub
        super.setToken(token);
        attributeList();
    }
    
    /** Retrieve attributes specific for the QuantityManager and return.
     *  @return The list of QM specific attributes. 
     *  @exception IllegalActionException Thrown by super class.
     */
    public List attributeList() {
        try { 
            if (getToken() != null) {
                _quantityMananger = (QuantityManager)((ObjectToken)getToken()).getValue();
                if (getContainer() instanceof IOPort) {
                    Port port = (IOPort) getContainer();
                    return _quantityMananger.getPortAttributeList(this, port);
                }
            }
        } catch (IllegalActionException e) {
            // FIXME when does this happen?
            e.printStackTrace();
        } 
        return super.attributeList();
    }   
    
    /** Update attributeList.
     *  @exception IllegalActionException Thrown by super class.
     */
    protected void _evaluate() throws IllegalActionException {
        // TODO Auto-generated method stub
        super._evaluate();
        attributeList();
    }

    private QuantityManager _quantityMananger;
    
}
