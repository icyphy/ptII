package ptolemy.actor.lib.resourceScheduler;

import ptolemy.actor.lib.ResourceAttributes;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class ExecutionTimeResourceAttributes extends ResourceAttributes {

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public ExecutionTimeResourceAttributes(NamedObj target, Decorator decorator)
            throws IllegalActionException, NameDuplicationException {
        super(target, decorator);
        _init();
    }

    /** Constructor to use when parsing a MoML file.
     *  @param target The object being decorated.
     *  @param name The name of this attribute.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public ExecutionTimeResourceAttributes(NamedObj target, String name)
            throws IllegalActionException, NameDuplicationException {
        super(target, name);
        _init();
    }
    
    /** The executionTime parameter specifies the execution time of the
     *  decorated object. This means the time that the decorated actor occupies
     *  the decorator resource when it fires.
     *  This is a double that defaults to 0.0.
     */
    public Parameter executionTime;
    

    ///////////////////////////////////////////////////////////////////
    ////                        public methods                     ////
    
    /** React to a change in an attribute.  If the attribute is
     *  <i>executionTime</i>, check that it is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == executionTime) {
            double value = ((DoubleToken)executionTime.getToken()).doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(getContainer(),
                        "Cannot specify a negative number for executionTime.");
            }
        }
        super.attributeChanged(attribute);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    /** Create the parameters.
     */
    private void _init() {
        try {
            executionTime = new Parameter(this, "executionTime");
            executionTime.setExpression("0.0");
            executionTime.setTypeEquals(BaseType.DOUBLE);
        } catch (KernelException ex) {
            // This should not occur.
            throw new InternalErrorException(ex);
        }
    }
    
}
