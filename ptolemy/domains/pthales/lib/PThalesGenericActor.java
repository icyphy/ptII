package ptolemy.domains.pthales.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class PThalesGenericActor extends TypedAtomicActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public PThalesGenericActor() {
        super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public PThalesGenericActor(Workspace workspace) {
        super(workspace);
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public PThalesGenericActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        _initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    private void _initialize() throws IllegalActionException,
            NameDuplicationException {

        Attribute rep = super.getAttribute("repetitions");
        if (rep == null) {
            repetitions = new StringParameter(this, "repetitions");
            repetitions.setExpression("");
        }
    }

    /** If a positive integer, then the number of iterations before the
     *  actor indicates to the scheduler that it is finished by returning
     *  false in its postfire() method.
     */
    public Parameter repetitions;

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            PThalesIOPort port = new PThalesIOPort(this, name, false, false);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** If the argument is the <i>init</i> parameter, then reset the
     *  state to the specified value.
     *  @param attribute The attribute that changed.
     *  @throws IllegalActionException If <i>init<i> cannot be evaluated
     *   or cannot be converted to the output type, or if the superclass
     *   throws it.
     */
    /** Add an attribute.  This method should not be used directly.
     *  Instead, call setContainer() on the attribute.
     *  Derived classes may further constrain the class of the attribute.
     *  To do this, they should override this method to throw an exception
     *  when the argument is not an instance of the expected class.
     *  This method is write-synchronized on the workspace and increments its
     *  version number.
     *  @param p The attribute to be added.
     *  @exception NameDuplicationException If this object already
     *   has an attribute with the same name.
     *  @exception IllegalActionException If the attribute is not an
     *   an instance of the expect class (in derived classes).
     */
    protected void _addAttribute(Attribute p) throws NameDuplicationException,
            IllegalActionException {
        try {
            _workspace.getWriteAccess();

            if (p instanceof StringParameter) {
                if (p.getName().equals("repetition") && repetitions != null) {
                    repetitions.setExpression(((StringParameter) p)
                            .getExpression());

                    repetitions.propagateValue();
                    return;
                }
            }
            super._addAttribute(p);

        } finally {
            _workspace.doneWriting();
        }
    }

    /** Read all the array then (should) call JNI function 
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();
    }

    /** Attribute update
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == repetitions) {
            _repetitions = repetitions.getExpression();
        }
    }

    /** Unused
     */
    public boolean postfire() throws IllegalActionException {

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** This is the value in parameter base.
     */
    protected String _repetitions = "";

}
