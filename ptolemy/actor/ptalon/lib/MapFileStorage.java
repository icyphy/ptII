/* An actor that stores data from a MapWorker and distributes it to a ReduceWorker,

 @Copyright (c) 2006-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.ptalon.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MapFileStorage

/**
 A DE actor that stores data from a MapWorker and distributes it to a ReduceWorker,
 upon request from the reduce worker.

 @author Adam Cataldo
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class MapFileStorage extends DEActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MapFileStorage(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        numberOfOutputs = new Parameter(this, "numberOfOutputs");
        numberOfOutputs.setExpression("1");
        numberOfOutputs.setTypeEquals(BaseType.INT);

        inputKey = new TypedIOPort(this, "inputKey", true, false);
        inputKey.setTypeEquals(BaseType.STRING);

        inputValue = new TypedIOPort(this, "inputValue", true, false);
        inputValue.setTypeEquals(BaseType.STRING);

        outputKey = new TypedIOPort(this, "outputKey", false, true);
        outputKey.setTypeEquals(BaseType.STRING);
        outputKey.setMultiport(true);

        outputKey.setWidthEquals(numberOfOutputs);

        outputValue = new TypedIOPort(this, "outputValue", false, true);
        outputValue.setTypeEquals(BaseType.STRING);
        outputValue.setMultiport(true);

        outputValue.setWidthEquals(numberOfOutputs);

        doneReceiving = new TypedIOPort(this, "doneReceiving", true, false);
        doneReceiving.setTypeEquals(BaseType.BOOLEAN);

        doneEmitting = new TypedIOPort(this, "doneEmitting", false, true);
        doneEmitting.setTypeEquals(BaseType.BOOLEAN);

        _keyBuffers = new LinkedList<LinkedList<String>>();
        _valueBuffers = new LinkedList<LinkedList<String>>();

        _readMode = false;

        _doneReceiving = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The inputTrigger port.  The type of this port is undeclared, meaning
     *  that it will resolve to any data type.
     */
    public TypedIOPort doneReceiving;

    /** The inputTrigger port.  The type of this port is undeclared, meaning
     *  that it will resolve to any data type.
     */
    public TypedIOPort doneEmitting;

    /** The inputTrigger port.  The type of this port is undeclared, meaning
     *  that it will resolve to any data type.
     */
    public TypedIOPort inputKey;

    /** The inputTrigger port.  The type of this port is undeclared, meaning
     *  that it will resolve to any data type.
     */
    public TypedIOPort inputValue;

    /** The number of output actors to write to.
     *  This parameter contains an IntToken, initially with value 1.
     *  The value must be greater than zero.
     */
    public Parameter numberOfOutputs;

    /** The output port.  The type of this port is unspecified.
     *  Derived classes may set it.
     */
    public TypedIOPort outputKey;

    /** The output port.  The type of this port is unspecified.
     *  Derived classes may set it.
     */
    public TypedIOPort outputValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>fileOrURLPrefix</i> and there is an
     *  open file being read, then close that file and open the new one;
     *  if the attribute is <i>numberOfLinesToSkip</i> and its value is
     *  negative, then throw an exception.  In the case of <i>fileOrURLPrefix</i>,
     *  do nothing if the file name is the same as the previous value of
     *  this attribute.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *  is <i>fileOrURLPrefix</i> and the file cannot be opened, or the previously
     *  opened file cannot be closed; or if the attribute is
     *  <i>numberOfLinesToSkip</i> and its value is negative.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numberOfOutputs) {
            if (!(_keyBuffers.isEmpty() && _valueBuffers.isEmpty())) {
                throw new IllegalActionException(this,
                        "Cannot change numberOfOutputs dynamically.");
            }
            int size = ((IntToken) numberOfOutputs.getToken()).intValue();

            if (size < 1) {
                throw new IllegalActionException(this, "The bock size "
                        + "must be greater than zero.");
            }

        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MapFileStorage newObject = (MapFileStorage) super.clone(workspace);
        newObject.outputKey.setWidthEquals(newObject.numberOfOutputs);
        newObject.outputValue.setWidthEquals(newObject.numberOfOutputs);
        return newObject;
    }

    /** Output the data read in the preinitialize() or in the previous
     *  invocation of postfire(), if there is any.
     *  @exception IllegalActionException If there's no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int outputs = ((IntToken) numberOfOutputs.getToken()).intValue();

        if (_readMode) {
            //Read inputs.
            while (inputKey.hasToken(0) && inputValue.hasToken(0)) {
                String key = ((StringToken) inputKey.get(0)).stringValue();
                String value = ((StringToken) inputValue.get(0)).stringValue();
                int hashCode = key.hashCode();
                int position = hashCode >= 0 ? hashCode % outputs : -hashCode
                        % outputs;
                _keyBuffers.get(position).add(key);
                _valueBuffers.get(position).add(value);
            }
        }
        //Write outputs.
        for (int i = 0; i < outputs; i++) {
            while (!_keyBuffers.get(i).isEmpty()
                    && !_valueBuffers.get(i).isEmpty()) {
                StringToken key = new StringToken(_keyBuffers.get(i).remove());
                StringToken value = new StringToken(_valueBuffers.get(i)
                        .remove());
                outputKey.send(i, key);
                outputValue.send(i, value);
            }
        }
        if (doneReceiving.hasToken(0)) {
            if (((BooleanToken) doneReceiving.get(0)).booleanValue()) {
                _doneReceiving = true;
            }
        }
        if (_doneReceiving) {
            boolean emptyBuffers = true;
            for (int i = 0; i < _keyBuffers.size(); i++) {
                if (!_keyBuffers.get(i).isEmpty()
                        || !_valueBuffers.get(i).isEmpty()) {
                    emptyBuffers = false;
                    break;
                }
            }
            if (emptyBuffers) {
                doneEmitting.send(0, new BooleanToken(true));
            } else {
                doneEmitting.send(0, new BooleanToken(false));
            }
        } else {
            doneEmitting.send(0, new BooleanToken(false));
        }
    }

    /** If this is called after prefire() has been called but before
     *  wrapup() has been called, then close any
     *  open file re-open it, skip the number of lines given by the
     *  <i>numberOfLinesToSkip</i> parameter, and read the first line to
     *  be produced in the next invocation of prefire(). This occurs if
     *  this actor is re-initialized during a run of the model.
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened, or if the lines to be skipped and the first line to be
     *   sent out in the fire() method cannot be read.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        int outputs = ((IntToken) numberOfOutputs.getToken()).intValue();
        for (int i = 0; i < outputs; i++) {
            _keyBuffers.add(new LinkedList<String>());
            _valueBuffers.add(new LinkedList<String>());
        }
        _readMode = false;
        _doneReceiving = false;
    }

    /** Return false if there is no more data available in the file.
     *  Otherwise, return whatever the superclass returns.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (inputKey.hasToken(0) && inputValue.hasToken(0)) {
            _readMode = true;
        } else {
            _readMode = false;
        }
        return super.prefire();
    }

    /** Return true, unless stop() has been called, in which case,
     *  return false.  Derived classes override this method to define
     *  operations to be performed at the end of every iteration of
     *  its execution, after one invocation of the prefire() method
     *  and any number of invocations of the fire() method.
     *  This method typically wraps up an iteration, which may
     *  involve updating local state.  In derived classes,
     *  this method returns false to indicate that this actor should not
     *  be fired again.
     *
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_readMode) {
            _readMode = false;
            getDirector().fireAtCurrentTime(this);
        }
        return super.postfire();
    }

    /** Close the reader if there is one.
     *  @exception IllegalActionException If an IO error occurs.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        _keyBuffers = new LinkedList<LinkedList<String>>();
        _valueBuffers = new LinkedList<LinkedList<String>>();
        _readMode = false;
        _doneReceiving = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private boolean _doneReceiving;

    private List<LinkedList<String>> _keyBuffers;

    private boolean _readMode;

    private List<LinkedList<String>> _valueBuffers;
}
