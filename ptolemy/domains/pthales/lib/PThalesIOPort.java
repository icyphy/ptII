package ptolemy.domains.pthales.lib;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.IORelation;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypeEvent;
import ptolemy.actor.TypeListener;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.StructuredType;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class PThalesIOPort extends TypedIOPort {

    /** Construct a PThalesIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public PThalesIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, isInput, isOutput);

        //FIXME : adapt to correct type        
        setTypeEquals(BaseType.FLOAT);
        
        // Add parameters for PThales Domain
        initialize();
    }

    /** Construct a PThalesIOPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the TypedActor interface, or an exception will be
     *  thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public PThalesIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        //FIXME : adapt to correct type        
        setTypeEquals(BaseType.FLOAT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the port is an output, return the remote receivers that can
     *  receive from the port.  For an output
     *  port, the returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  The length
     *  of the array is the width of the port (the number of channels).
     *  It is an array of arrays, each of which represents a group of
     *  receivers that receive data from the same channel.
     *  <p>
     *  This method may have the effect of creating new receivers in the
     *  remote input ports, if they do not already have the right number of
     *  receivers.  In this case, previous receivers are lost, together
     *  with any data they may contain.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *  @return The receivers for output data, or an empty array if there
     *   are none.
     * @exception IllegalActionException
     */
    public Receiver[][] getRemoteReceivers() throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            if (!isOutput()) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            int width = getWidth();

            if (width <= 0) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            // For opaque port, try the cached _farReceivers
            // Check validity of cached version
            if (isOpaque() && (_farReceiversVersion == _workspace.getVersion())) {
                return _farReceivers;
            }

            // If not an opaque port or Cache is not valid.  Reconstruct it.
            Receiver[][] farReceivers = new Receiver[width][0];
            Iterator<?> relations = linkedRelationList().iterator();
            int index = 0;
            // Hypothese : 1 input per relation

            while (relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();

                // A null link (supported since indexed links) might
                // yield a null relation here. EAL 7/19/00.
                if (relation != null) {
                    Receiver[][] deepReceivers = relation.deepReceivers(this);

                    if (deepReceivers != null) {
                        for (int i = 0; i < deepReceivers.length; i++) {
                            farReceivers[index] = deepReceivers[i];
                            index++;
                        }
                    } else {
                        // create a number of null entries in farReceivers
                        // corresponding to the width of relation r
                        index += relation.getWidth();
                    }
                }
            }

            // For an opaque port, cache the result.
            if (isOpaque()) {
                _farReceiversVersion = _workspace.getVersion();
                _farReceivers = farReceivers;
            }

            return farReceivers;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Clone this port into the specified workspace. The new port is
     *  <i>not</i> added to the directory of that workspace (you must
     *  do this yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *  The new port will have the same type as this one, but will not
     *  have any type listeners and type constraints attached to it.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the
     *   attributes cannot be cloned.
     *  @return A new TypedIOPort.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PThalesIOPort newObject = (PThalesIOPort) super.clone(workspace);

        // set _declaredType and _resolvedType
        if (_declaredType instanceof StructuredType
                && !_declaredType.isConstant()) {
            newObject._declaredType = (Type) ((StructuredType) _declaredType)
                    .clone();
            newObject._resolvedType = newObject._declaredType;
        }

        newObject._typeTerm = null;
        newObject._typeListeners = new LinkedList<TypeListener>();
        newObject._constraints = new HashSet<Inequality>();
        return newObject;
    }
    
    /** Computes pattern size in byte, not the space in memory
    *  @return Pattern size in byte.
    */
   public int getPattern() {
       int result = 0;
       
       pattern = (Parameter)getAttribute("pattern");
       
       if (pattern != null)
       {
           int value = 1;
           String [] dims = pattern.toString().split(",");
           for (String dim : dims)
           {
               value *= Integer.parseInt(dim.split("=")[1].split("\\.")[0].trim());
           }
           result = value;
       }
       
       return result;
   }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
  
    
    /** Reset the variable part of this type to the specified type.
     *  @param type A Type.
     *  @exception IllegalActionException If the type is not settable,
     *   or the argument is not a Type.
     */
    private void initialize() throws IllegalActionException,
            NameDuplicationException {

        base = new Parameter(this, "base");
        base.setExpression("");
        base.setTypeEquals(BaseType.STRING);

        pattern = new Parameter(this, "pattern");
        pattern.setExpression("");
        pattern.setTypeEquals(BaseType.STRING);

        tiling = new Parameter(this, "tiling");
        tiling.setExpression("");
        tiling.setTypeEquals(BaseType.STRING);

        size = new Parameter(this, "size");
        size.setExpression("");
        size.setTypeEquals(BaseType.STRING);

        dataType = new Parameter(this, "dataType");
        dataType.setExpression("");
        dataType.setTypeEquals(BaseType.STRING);

        dataTypeSize = new Parameter(this, "dataTypeSize");
        dataTypeSize.setExpression("");
        dataTypeSize.setTypeEquals(BaseType.INT);

        dimensionNames = new Parameter(this, "dimensionNames");
        dimensionNames.setExpression("");
        dimensionNames.setTypeEquals(BaseType.STRING);

    }

    /** Array base
     */
    public Parameter base;

    /** Array pattern
     */
    public Parameter pattern;

    /** Array tiling 
     */
    public Parameter tiling;

    /** Array size
     */
    public Parameter size;

    /** data type (for code generation only) 
     */
    public Parameter dataType;

    /** data type size(for code generation only) 
     */
    public Parameter dataTypeSize;

    /** data type size(for code generation only) 
     */
    public Parameter dimensionNames;

    /** Initialize the iteration counter.  A derived class must call
     *  this method in its initialize() method or the <i>firingCountLimit</i>
     *  feature will not work.
     *  @exception IllegalActionException If the parent class throws it,
     *   which could occur if, for example, the director will not accept
     *   sequence actors.
     */

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Notify the type listener about type change.
    private void _notifyTypeListener(Type oldType, Type newType) {
        if (_typeListeners.size() > 0) {
            TypeEvent event = new TypeEvent(this, oldType, newType);
            Iterator<TypeListener> listeners = _typeListeners.iterator();

            while (listeners.hasNext()) {
                (listeners.next()).typeChanged(event);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Type _declaredType = BaseType.UNKNOWN;

    private Type _resolvedType = BaseType.UNKNOWN;

    private TypeTerm _typeTerm = null;

    // Listeners for type change.
    private List<TypeListener> _typeListeners = new LinkedList<TypeListener>();

    // type constraints
    private Set<Inequality> _constraints = new HashSet<Inequality>();

    /** To avoid creating this repeatedly, we use a single version. */
    private static final Receiver[][] _EMPTY_RECEIVER_ARRAY = new Receiver[0][0];

    // A cache of the deeply connected Receivers, and the versions.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _farReceivers;

    private transient long _farReceiversVersion = -1;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    private class TypeTerm implements InequalityTerm {
        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return this TypedIOPort.
         *  @return A TypedIOPort.
         */
        public Object getAssociatedObject() {
            return PThalesIOPort.this;
        }

        /** Return the type of this TypedIOPort.
         */
        public Object getValue() {
            return getType();
        }

        /** Return this TypeTerm in an array if this term represent
         *  a type variable. This term represents a type variable
         *  if the type of this port is not set through setTypeEquals().
         *  If the type of this port is set, return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = this;
                return variable;
            }

            return (new InequalityTerm[0]);
        }

        /** Reset the variable part of this type to the specified type.
         *  @param type A Type.
         *  @exception IllegalActionException If the type is not settable,
         *   or the argument is not a Type.
         */
        public void initialize(Object type) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException("TypeTerm.initialize: "
                        + "Cannot initialize a constant type.");
            }

            if (!(type instanceof Type)) {
                throw new IllegalActionException("TypeTerm.initialize: "
                        + "The argument is not a Type.");
            }

            Type oldType = _resolvedType;

            if (_declaredType == BaseType.UNKNOWN) {
                _resolvedType = (Type) type;
            } else {
                // _declaredType is a StructuredType
                ((StructuredType) _resolvedType).initialize((Type) type);
            }

            if (!oldType.equals(_resolvedType)) {
                _notifyTypeListener(oldType, _resolvedType);
            }
        }

        /** Test if the type of this TypedIOPort can be changed.
         *  The type can be changed if setTypeEquals() is not called,
         *  or called with a BaseType.UNKNOWN argument.
         *  @return True if the type of this TypedIOPort can be changed;
         *   false otherwise.
         */
        public boolean isSettable() {
            return !_declaredType.isConstant();
        }

        /** Check whether the current value of this term is acceptable.
         *  This method delegates the check to the isTypeAcceptable()
         *  method of the outer class.
         *  @return True if the current value is acceptable.
         */
        public boolean isValueAcceptable() {
            return isTypeAcceptable();
        }

        /** Set the type of this port.
         *  @param type A Type.
         *  @exception IllegalActionException If the new type violates
         *   the declared type of this port.
         */
        public void setValue(Object type) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "TypedIOPort$TypeTerm.setValue: The type is not "
                                + "settable.");
            }

            if (!_declaredType.isSubstitutionInstance((Type) type)) {
                throw new IllegalActionException("Type conflict on port "
                        + PThalesIOPort.this.getFullName() + ".\n"
                        + "Declared type is " + _declaredType.toString()
                        + ".\n"
                        + "The connection or type constraints, however, "
                        + "require type " + type.toString());
            }

            Type oldType = _resolvedType;

            if (_declaredType == BaseType.UNKNOWN) {
                _resolvedType = (Type) type;
            } else {
                // _declaredType is a StructuredType
                ((StructuredType) _resolvedType)
                        .updateType((StructuredType) type);
            }

            if (!oldType.equals(type)) {
                _notifyTypeListener(oldType, _resolvedType);
            }
        }

        /** Override the base class to give a description of the port
         *  and its type.
         *  @return A description of the port and its type.
         */
        public String toString() {
            return "(" + PThalesIOPort.this.toString() + ", " + getType() + ")";
        }
    }
}
