/* An actor that updates fields in a RecordToken.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.kernel.Port;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// RecordUpdater
/**
On each firing, read one token from each input port and assemble them
into a RecordToken that contains the union of the original input record
and each of the update ports.  To use this class, instantiate it, and
then add input ports (instances of TypedIOPort).  This actor is polymorphic.
The type constraint is that the type of each record field is no less than
the type of the corresponding input port.

@author Michael Shilman
@version $Id$
@see RecordAssembler
*/

public class RecordUpdater extends TypedAtomicActor {

    /** Construct a RecordUpdater with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RecordUpdater(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        input = new TypedIOPort(this, "input", true, false);

        _addIcon();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. Its type is constrained to be a RecordType. */
    public TypedIOPort output;

    /** The input port. Its type is constrained to be a RecordType. */
    public TypedIOPort input;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input port, assemble them into a
     * RecordToken that contains the union of the original input record
     * and each of the update ports.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        Director director = getDirector();
        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        // Pack a HashMap with all of the record entries from
        // the original record and all of the updating ports.
        HashMap outputMap = new HashMap();
        
        RecordToken record = (RecordToken)input.get(0);
        Set recordLabels = record.labelSet();
        for(Iterator i = recordLabels.iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            Token value = record.get(name);
            outputMap.put(name,value);
        }
        
        List inputPorts = inputPortList();
        for(int i = 1; i < inputPorts.size(); i++) {
            TypedIOPort inputPort = (TypedIOPort)inputPorts.get(i);
            outputMap.put(inputPort.getName(), inputPort.get(0));
        }
        
 	// Construct a RecordToken and fill it with the values
        // in the HashMap.
	String[] labels = new String[outputMap.size()];
	Token[] values = new Token[outputMap.size()];

        int j = 0;
	for (Iterator i = outputMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry)i.next();
	    labels[j] = (String)entry.getKey();
	    values[j] = (Token)entry.getValue();
            j++;
	}

	RecordToken result = new RecordToken(labels, values);
        output.send(0, result);
    }

    /** Return true if all input ports have tokens, false if some input
     *  ports do not have a token.
     *  @return True if all input ports have tokens.
     *  @exception IllegalActionException If the hasToken() call to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int)
     */
    public boolean prefire() throws IllegalActionException {
        Iterator ports = inputPortList().iterator();
	while (ports.hasNext()) {
	    IOPort port = (IOPort)ports.next();
	    if ( !port.hasToken(0)) {
	        return false;
	    }
        }
	return true;
    }

    /** Return the type constraints of this actor. The type constraint is
     *  that the type of the fields of the output RecordToken is no less
     *  than the type of the corresponding input ports.
     *  @return a list of Inequality.
     */
    public List typeConstraintList() {
        // Pack a HashMap with all of the record entries from
        // the original record and all of the updating ports.
        HashMap typeMap = new HashMap();
        
        RecordType inputType = (RecordType)input.getType();
        Set inputLabels = inputType.labelSet();
        for(Iterator i = inputLabels.iterator(); i.hasNext(); ) {
            String label = (String)i.next();
            typeMap.put(label, inputType.getTypeTerm(label));
        }
        List inputPorts = inputPortList();
        for(int i = 1; i < inputPorts.size(); i++) {
            TypedIOPort inputPort = (TypedIOPort)inputPorts.get(i);
            typeMap.put(inputPort.getName(), inputPort.getTypeTerm());
        }
        
	// form the declared type for the output port
        int j = 0;
        String[] labels = new String[typeMap.size()];
	Type[] types = new Type[typeMap.size()];
	for (Iterator i = typeMap.keySet().iterator(); i.hasNext(); ) {
	    labels[j] = (String)i.next();
            types[j] = BaseType.UNKNOWN;
            j++;
        }
        RecordType declaredType = new RecordType(labels, types);
	output.setTypeEquals(declaredType);

	// set the constraints between record fields and input ports
	List constraints = new LinkedList();
        
	// since the output port has a clone of the above RecordType, need to
	// get the type from the output port.
	RecordType outputType = (RecordType)output.getType();

        //set up the constraints between the original record and
        //the output record.
	for (Iterator i = typeMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry)i.next();
	    String label = (String)entry.getKey();
	    InequalityTerm inputTerm = (InequalityTerm)entry.getValue();
	    Inequality inequality =
                new Inequality(inputTerm, outputType.getTypeTerm(label));
	    constraints.add(inequality);
	}
	return constraints;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addIcon() {
	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"0\" y=\"0\" width=\"10\" " +
                "height=\"60\" style=\"fill:red\"/>\n" +
                "</svg>\n");
    }
}

