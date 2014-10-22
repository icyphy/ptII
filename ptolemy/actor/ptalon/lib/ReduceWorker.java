/* A ReduceWorker actor, as a subsystem of the MapReduce system.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.ptalon.lib;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////ReduceWorker

/**
 A ReduceWorker actor, as a subsystem of the MapReduce system.

 <p> This actor has a parameter <i>classNameForReduce</i> which is the
 qualified name for a Java class that extends
 ptolemy.actor.ptalon.lib.MapReduceAlgorithm.  It must also have a no
 argument constructor.  By extending this abstract class, it will
 implement a method named <i>reduce</i> with type signature:

 <p> <code>public List&lt;String&gt; reduce(String key,
 BlockingQueue&lt;String&gt; value)</code>

 <p> This method defines the Reduce algorithm for the MapReduce
 system.  At each call, it should return a list of Strings, which is a
 reduction of the list of input values.  At each firing, this actor
 inputs all available input keys and values.  It outputs the value
 tokens when its <i>doneReading</i> port receives a true value.  This
 should only happen after all inputs have been sent to the system.

 <p> When implementing a custom reduce method in a subclass of
 MapReduceAlgorithm, note to use the take method to get values from
 the queue.  Call the <i>isQueueEmpty</i> of MapReduceAlgorithm to
 test if this actor has stopped putting values on the queue and that
 all values have been taken from the queue.  The last element of the
 queue will always be the empty string.  Ignore this value.

 @author Adam Cataldo
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.actor.ptalon.lib.KeyValuePair
 */

public class ReduceWorker extends TypedAtomicActor {

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
    public ReduceWorker(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        classNameForReduce = new StringParameter(this, "classNameForReduce");
        classNameForReduce.setExpression("ptolemy.actor.ptalon.lib.WordCount");

        inputKey = new TypedIOPort(this, "inputKey");
        inputKey.setInput(true);
        inputKey.setTypeEquals(BaseType.STRING);
        inputKey.setMultiport(true);

        inputValue = new TypedIOPort(this, "inputValue");
        inputValue.setInput(true);
        inputValue.setTypeEquals(BaseType.STRING);
        inputValue.setMultiport(true);

        outputKey = new TypedIOPort(this, "outputKey");
        outputKey.setOutput(true);
        outputKey.setTypeEquals(BaseType.STRING);

        outputValue = new TypedIOPort(this, "outputValue");
        outputValue.setOutput(true);
        outputValue.setTypeEquals(BaseType.STRING);

        doneReading = new TypedIOPort(this, "doneReading");
        doneReading.setInput(true);
        doneReading.setTypeEquals(BaseType.BOOLEAN);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The qualified class name for a Java class containing a method
     *  with signature:
     *  <p>
     *  <code>public static List&lt;String[]&gt; map(String key, String value)</code>
     *  <p>
     *  Each element of each returned list should be a length two array of
     *  Strings.
     */
    public StringParameter classNameForReduce;

    /**
     * A boolean input.  When this input is true, the
     * actor is done reading values, and it may output
     * tokens for each key it received.
     */
    public TypedIOPort doneReading;

    /** A String input key.
     */
    public TypedIOPort inputKey;

    /** A String input value.
     */
    public TypedIOPort inputValue;

    /** A String output key.
     */
    public TypedIOPort outputKey;

    /** A String output value.
     */
    public TypedIOPort outputValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this base class,
     *  the method does nothing.  In derived classes, this method may
     *  throw an exception, indicating that the new attribute value
     *  is invalid.  It is up to the caller to restore the attribute
     *  to a valid value if an exception is thrown.  If the attribute
     *  changed is <i>classNameForReduce</i>, update this actor accordingly.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.  If the class set in <i>classNameForReduce<i/>
     *   does not exist, or if the class exists but does not contain a map
     *   method with an appropriate signature, this exception will be thrown.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == classNameForReduce) {
            _setReduceMethod();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read in a token on the <i>inputKey</i> and <i>inputValue</i>
     *  ports and output pairs of tokens on the <i>outputKey</i>, <i>outputValue</i>
     *  ports.
     *
     *  @exception IllegalActionException If there is any trouble calling
     *  the map method.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_readMode) {
            int numberInputs = inputKey.getWidth();
            for (int i = 0; i < numberInputs; i++) {
                while (inputKey.hasToken(i) && inputValue.hasToken(i)) {
                    String key = ((StringToken) inputKey.get(i)).stringValue();
                    String value = ((StringToken) inputValue.get(i))
                            .stringValue();
                    if (_runningAlgorithms.containsKey(key)) {
                        MapReduceAlgorithm algorithm = _runningAlgorithms
                                .get(key);
                        try {
                            algorithm.reduceValues.put(value);
                        } catch (InterruptedException e) {
                            throw new IllegalActionException(
                                    "Interrupted while trying to put value for key "
                                            + key);
                        }
                    } else {
                        MapReduceAlgorithm newAlgorithm = null;
                        try {
                            newAlgorithm = (MapReduceAlgorithm) _reduceClass
                                    .newInstance();
                        } catch (IllegalAccessException e) {
                            throw new IllegalActionException(
                                    classNameForReduce.stringValue()
                                            + " does not have a no argument constructor");
                        } catch (InstantiationException e) {
                            throw new IllegalActionException(
                                    classNameForReduce.stringValue()
                                            + " is abstract.");
                        } catch (ClassCastException e) {
                            throw new IllegalActionException(
                                    "Unable to cast instance of "
                                            + classNameForReduce.stringValue()
                                            + " to ptolemy.actor.ptalon.lib.MapReduceAlgorithm.");
                        }
                        newAlgorithm.reduceKey = key;
                        newAlgorithm.reduceValues = new LinkedBlockingQueue<String>();
                        try {
                            newAlgorithm.reduceValues.put(value);
                        } catch (InterruptedException e) {
                            throw new IllegalActionException(
                                    "Interrupted while trying to put value for key "
                                            + key);
                        }
                        newAlgorithm.start();
                        _runningAlgorithms.put(key, newAlgorithm);
                    }
                }
            }
        }
        if (doneReading.hasToken(0)) {
            boolean done = ((BooleanToken) doneReading.get(0)).booleanValue();
            if (done && !_doneReading) {
                _doneReading = true;
                for (String key : _runningAlgorithms.keySet()) {
                    MapReduceAlgorithm algorithm = _runningAlgorithms.get(key);
                    algorithm.setNoMoreInputs();
                    try {
                        algorithm.reduceValues.put("");
                    } catch (InterruptedException e) {
                        throw new IllegalActionException(
                                "Interrupted while trying to put value for key "
                                        + key);
                    }
                    while (!algorithm.isReduceFinished()) {
                        //Wait for the algorithm to finish.
                    }
                    List<String> outputs = algorithm.reduceOutput;
                    for (String value : outputs) {
                        outputKey.send(0, new StringToken(key));
                        outputValue.send(0, new StringToken(value));
                    }
                }
            }
        }
    }

    /** Return true if there is an available key token and value token
     *  on the <i>inputKey</i> and <i>inputValue</i> ports.
     *
     *  @return True if this actor is ready for firing, false otherwise.
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called prefire(), which returns true");
        }
        if (inputKey.hasToken(0) && inputValue.hasToken(0)) {
            _readMode = true;
        } else {
            _readMode = false;
        }
        return super.prefire();
    }

    /**
     * @return The base class return value.
     * @exception IllegalActionException If thrown in the base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _readMode = false;
        return super.postfire();
    }

    /**
     * Clean up memory.
     * @exception IllegalActionException If thrown in the base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        _runningAlgorithms = new Hashtable<String, MapReduceAlgorithm>();
        _readMode = false;
        _doneReading = false;
        super.wrapup();
    }

    /** Extract the map method from the <i>classNameForMap</i> parameter.
     *
     *  @exception IllegalActionException If unable to extract an appropriate
     *  map method.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _setReduceMethod();
        _runningAlgorithms = new Hashtable<String, MapReduceAlgorithm>();
        _readMode = false;
        _doneReading = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Set the map method using the class specified in <i>classNameForMap<i>.
     * @exception IllegalActionException If the map method does not
     * exist, has the wrong type signature, or has the wrong
     * access modifiers.
     */
    private void _setReduceMethod() throws IllegalActionException {
        String className = classNameForReduce.stringValue();
        Class<?> reduceClass = null;
        Class<?> algorithmClass = null;
        Class<?> objectClass = null;
        try {
            reduceClass = Class.forName(className);
            algorithmClass = Class
                    .forName("ptolemy.actor.ptalon.lib.MapReduceAlgorithm");
            objectClass = Class.forName("java.lang.Object");

        } catch (ClassNotFoundException e) {
            throw new IllegalActionException("No class named " + className
                    + " could be found.");
        }
        Class<?> superClass = reduceClass;
        while (!superClass.equals(objectClass)) {
            superClass = superClass.getSuperclass();
            if (superClass.equals(algorithmClass)) {
                break;
            }
            if (superClass.equals(objectClass)) {
                throw new IllegalActionException(className
                        + " is not a subclass of "
                        + "ptolemy.actor.ptalon.lib.MapReduceAlgorithm.");
            }
        }
        try {
            /*MapReduceAlgorithm algorithm = (MapReduceAlgorithm) */reduceClass
                    .newInstance();
        } catch (IllegalAccessException e) {
            throw new IllegalActionException(className
                    + " does not have a no argument constructor");
        } catch (InstantiationException e) {
            throw new IllegalActionException(className + " is abstract.");
        } catch (ClassCastException e) {
            throw new IllegalActionException("Unable to cast instance of "
                    + className
                    + " to ptolemy.actor.ptalon.lib.MapReduceAlgorithm.");
        }
        _reduceClass = reduceClass;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////

    private boolean _doneReading = false;

    private boolean _readMode = false;

    private Class<?> _reduceClass;

    /**
     * Each key is a distinct key for a reduce call, and each value
     * is a MapReduceAlgorithm to reduce a set of values.
     */
    private Map<String, MapReduceAlgorithm> _runningAlgorithms;

    ///////////////////////////////////////////////////////////////////
    ////                        private classes                    ////

    //    private class ReduceIterator implements Iterator<String> {
    //
    //        public ReduceIterator(MapReduceAlgorithm algorithm) {
    //            //_algorithm = algorithm;
    //        }
    //
    //        /**
    //         * Return true if the iterator has another token.
    //         * This may be unknown, in which case wait is called
    //         * on the MapReduceAlgorithm associated with this
    //         * iterator.
    //         * @return true if this has another token.
    //         */
    //        public boolean hasNext() {
    //
    //            return false;
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see java.util.Iterator#next()
    //         */
    //        public String next() {
    //            // TODO Auto-generated method stub
    //            return null;
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see java.util.Iterator#remove()
    //         */
    //        public void remove() {
    //            // TODO Auto-generated method stub
    //
    //        }
    //
    //        //private MapReduceAlgorithm _algorithm;
    //
    //    }
}
