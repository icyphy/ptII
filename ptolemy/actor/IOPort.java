/* A port supporting message passing.

 Copyright (c) 1997- The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)

*/

package pt.actors;
import pt.kernel.*;
import pt.data.*;
import java.util.Enumeration;
import java.util.Hashtable;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// IOPort
/** 
This class supports exchanging data between
entities via message passing. It can serve as an input port,
an output port, or both. If it is an input port, then it
contains one or more receivers, which are responsible for receiving
data from remote entities. If it is an output port, then it
can send data to remote receivers.

The port has a <i>width</i>, which by default can be no greater
than one.  This width is the sum of the widths of the linked relations.
A port with a width greater than one behaves as a bus interface,
so if the width is <i>w</i>, then the port can simultaneously
handle <i>w</i> distinct input or output channels of data.

In general, an input port might have more than one receiver for
each channel.  This occurs particularly for transparent ports,
but might also occur for atomic ports in some derived classes.
Each receiver in the group is sent a clone of the same data.
Thus, an input port in general will have <i>w</i> distinct groups
of receivers, and can receive <i>w</i> distinct channels.

By default, the maximum width of the port is one, so only one
channel is handled. A port with a width greater than one is called a
<i>multiport</i>. Calling <code>makeMultiport</code> with a
<code>true</code> argument allows the port to have width greater
than one.

The width of the port is not set directly. It is the sum of the
widths of the relations that the port is linked to. If it is a
transparent port, then this sum for the inside links must equal the
sum for the outside links, or an exception will be thrown.

@authors Edward A. Lee, Jie Liu
@version $Id$
*/
public class IOPort extends ComponentPort {

    /** Construct an IOPort with no container and no name that is
     *  neither an input nor an output.
     */	
    public IOPort() {
        super();
    }

    /** Construct an IOPort with a containing entity and a name 
     *  that is neither an input nor an output.
     *  @param container The container entity.
     *  @param name The name of the port.
     *  @exception NameDuplicationException if the name coincides with
     *   a port already on the port list of the parent.
     */	
    public IOPort(ComponentEntity container, String name) 
             throws NameDuplicationException {
	super(container,name);
    }

    /** Construct an IOPort with a container and a name that is
     *  either an input or an output or both, depending on the third
     *  and fourth arguments.
     *  @param container The container entity.
     *  @param name The name of the port.
     *  @param isinput True if this is to be an input port.
     *  @param isoutput True if this is to be an output port.
     *  @exception NameDuplicationException if the name coincides with
     *   an element already on the port list of the parent.
     */	
    public IOPort(ComponentEntity container, String name,
             boolean isinput, boolean isoutput) 
             throws NameDuplicationException {
	this(container,name);
        makeInput(isinput);
        makeOutput(isoutput);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Send a token to all connected Receivers.  The first receiver gets
     *  the actual token, and subsequent receivers get clones.
     *  The transfer is accomplished by calling the put()
     *  method of the destination Receiver.
     *  If there are no destination receivers, then do nothing.
     *  This method is synchronized on the workspace.
     *  @param token The token to send
     *  @exception CloneNotSupportedException If the token cannot be cloned
     *   and there is more than one destination.
     *  @exception IllegalActionException If the port is not an output.
     */	
    public void broadcast(Token token) 
	    throws CloneNotSupportedException, IllegalActionException {
        synchronized(workspace()) {
            if (!isOutput()) {
                throw new IllegalActionException(this,
                "broadcast: Tokens can only be sent from an output port.");
            }
            Receiver fr[][] = getRemoteReceivers();
            if(fr == null) {
                return;
            }
            boolean first = true;
                
            for (int j = 0; j < fr.length; j++) {
                if (first) {
                    send(j,token);
                    first = false;
                } else {
                    send(j,((Token)(token.clone())));
                }
            }
        }
    }

    /** Return a description of the object specified by verbosity.
     *  If verbosity = RECEIVERS(7), then returns a String containing
     *  the receivers of this port. The returned String has the same 
     *  format as the Receivers array, ie. each row corresponding to 
     *  a channel, and all the elements in a row are receiving the 
     *  clone of the same token. Each element in the receivers array 
     *  has the form: container's_full_name.receiver_type, 
     *  e.g. e0.p0.pt.actors.Mailbox. Every null element in the array is 
     *  is a string "null" in the returned String. 
     *
     *  If verbosity = REMOTE_RECEIVERS(8), then returns a String 
     *  containing the remote receivers of this port.
     *  The returned String has the same 
     *  format as the remoteReceivers array, ie. each row corresponding to 
     *  a channel, and all the elements in a row are the destination 
     *  receiving the 
     *  clone of the same token. Each element in the receivers array 
     *  has the form: container's_full_name.receiver_type, 
     *  e.g. e0.p0.pt.acotrs.Mailbox. 
     * 
     *  NOTE: The information of getReceivers(IORelation) and 
     *  getRemoteReceivers(IORelation) is not included.
     *  @param verbosity The level of verbosity.
     */
    public String description(int verbosity){
        String results = new String();
        Receiver[][] recvrs;
        switch (verbosity) {
            case RECEIVERS:
                 recvrs= getReceivers();
                break;
            case REMOTE_RECEIVERS:
                recvrs = getRemoteReceivers();
                break;
            default :
                return super.description(verbosity);
        }
        if (recvrs == null) {
            return "null\n";
        }
        for (int i = 0; i<recvrs.length; i++) {
            if(recvrs[i] == null) {
                results += "null";
            } else {
                for (int j = 0; j< recvrs[i].length; j++) {
                    results = results.concat(
                        (recvrs[i][j].getContainer()).getFullName() +
                        "." + (recvrs[i][j].getClass()).getName() +
                        " ");
                }
            }
            results += "\n";
        }
        return results; 
    }

    /** Get a token from the specified channel.
     *  If there are multiple copies of the specified channel (something
     *  that is possible if this is a transparent port), then this method
     *  retrieves a token from all copies, but returns only the last.
     *  Normally this method is not used on transparent ports.
     *  This method is synchronized on the workspace.
     *  @exception NoSuchItemException If there is no token, or the
     *   port is not an input port, or the channel index is out of range.
     */	
    public Token get(int channelindex) throws NoSuchItemException {
        synchronized(workspace()) {
            if (!isInput()) {
                throw new NoSuchItemException(this,
                "get: Tokens can only be retreived from an input port.");
            }
            if (channelindex >= getWidth()) {
                throw new NoSuchItemException(this,
                "get: channel index is out of range.");
            }
            Receiver[][] fr = getReceivers();
            Token tt = null;
            for (int j = 0; j < fr[channelindex].length; j++) {
                tt = fr[channelindex][j].get();
            }
            if (tt == null) {
                throw new NoSuchItemException(this,
                "get: No receivers for the channel.");
            }
            return tt;
        }
    }

    /** If the port is an input, return the receivers that handle channels
     *  from all relations, otherwise return null.  For an input
     *  port, the returned value is an array of arrays.  The first index
     *  (the row) specifies the channel number.  The second index (the
     *  column) specifies the receiver number within the group of
     *  receivers that get copies of the same channel.  In this base class,
     *  the port is atomic, so the group size is always one (the second
     *  index is always zero).  In derived classes, the group size is
     *  arbitrary, and can be different for each channel.  The number of
     *  channels (rows) is the width of the port.  If the width is zero,
     *  this method will return null, as if the port were not an input
     *  port.
     *
     *  For an atomic port,
     *  this method creates receivers by calling <code>newReceiver</code>
     *  if there no receivers or the number of receivers does not match
     *  the width of the port.  Previous receivers are lost, together
     *  with any data they may contain.
     *
     *  For a transparent port (a port of a composite entity), this method
     *  returns the Receivers in ports connected to this port on the inside.
     *
     *  This method is synchronized on the workspace.
     *  @return The local receivers.
     */
    public Receiver[][] getReceivers() {
        synchronized(workspace()) {
            if (!isInput()) return null;

            int width = getWidth();
            if (width <= 0) return null;

            if(isAtomic()) {
                // Check to see whether cache is valid.
                if (_localReceiversVersion == workspace().getVersion()) {
                    return _localReceivers;
                }
            
                // Cache not valid.  Reconstruct it.
                _localReceivers = new Receiver[width][];
                int index = 0;
                Enumeration relations = linkedRelations();
                while (relations.hasMoreElements()) {
                    IORelation r = (IORelation) relations.nextElement();
                    try {
                        Receiver[][] rr = getReceivers(r);
                        if (rr != null) {
                            for (int i=0; i < rr.length; i++) {
                                _localReceivers[index++] = rr[i];
                            }
                        }
                    } catch (IllegalActionException ex) {
                        // Ignore exception that can't occur
                    }
                }
                _localReceiversVersion = workspace().getVersion();
                return _localReceivers;
            } else {
                // Transparent port. Does not use cache.
                Receiver[][] result = new Receiver[width][];
                int index = 0;
                Enumeration insideRels = insideRelations();
                while (insideRels.hasMoreElements()) {
                    IORelation r = (IORelation) insideRels.nextElement();
                    Receiver[][] rr = r.deepReceivers(this);
                    if (rr != null) {
                        int size = java.lang.Math.min(rr.length, width-index);
                        for (int i=0; i < size; i++) {
                            if (rr[i] != null) {
                                result[index++] = rr[i];
                            }
                        }
                    }
                }
                return result;
            }
        }
    }
       
    /** If the port is an input, return the receivers that handle incoming
     *  channels from the specified relation, otherwise return null.  For an 
     *  input port, the returned value is an array of arrays.  The first index
     *  (the row) specifies the channel number.  The second index (the
     *  column) specifies the receiver number within the group of
     *  receivers that get copies of the same channel.  If the port has no 
     *  inside links (is not transparent), then the group size is one (the 
     *  second index is always zero).  For transparent ports, the group size is
     *  arbitrary, and can be different for each channel.  The number of
     *  channels (rows) is the width of the relation.  If the width is zero,
     *  this method will return null, as if the port were not an input
     *  port.
     *
     *  For an atomic port,
     *  this method creates receivers by calling <code>newReceiver</code>
     *  if there no receivers or the number of receivers does not match
     *  the width of the port.  Previous receivers are lost, together
     *  with any data they may contain.
     * 
     *  For a transparent port, this method returns the receivers
     *  corresponding to the given IORelation contained by ports connected
     *  to this port from inside. If there are fewer inside Receivers
     *  than the width of the specified IORelation, then some of the
     *  returned array elements may be null.
     *
     *  This method is synchronized on the workspace.
     *  @param A linked relation.
     *  @return The local receivers.
     *  @exception IllegalActionException If the relation is not linked
     *   from the outside.
     */
    public Receiver[][] getReceivers(IORelation relation)
           throws IllegalActionException {
        synchronized(workspace()) {
            if (!isLinked(relation)) {
                throw new IllegalActionException(this, 
                "getReceivers: Relation argument is not linked to me.");
            }
            if (!isInput()) return null;

            int width = relation.getWidth();
            if (width <= 0) return null;

            // If the port is atomic, return the local Receivers for the
            // relation.
            if(isAtomic()) {
                // Do this here so that derived classes do not need to have the
                // hash table if they are not using it.
                if (_localReceiversTable == null) {
                    _localReceiversTable = new Hashtable();
                }

                if(_localReceiversTable.containsKey(relation) ) {
                    // There is a list of receivers for this relation.
                    // Check to see that the width is valid, since it's
                    // possible that the width of the port has changed since
                    // this list was constructed.
                    Receiver[][] result =
                            (Receiver[][])_localReceiversTable.get(relation);
                    if (result.length == width) return result;
                }
                // Create a new list of receivers.
                Receiver[][] result = new Receiver[width][1];

                // Populate it.
                for (int i = 0; i< width; i++) {
                    result[i][0] = newReceiver();
                }
                // Save it, possibly replacing a previous version.
                // NOTE: if a previous version is replaced, then any data
                // in the receivers is lost.
                _localReceiversTable.put(relation, result);
                return result;
            } else {
                // If not an atomic port, ask its all inside receivers,
                // and trim the returned Receivers array to get the 
                // part corresponding to the IORelation
                Receiver[][] insideRecvrs = getReceivers();
                if(insideRecvrs == null) {
                    return null;
                }
                int insideWidth = insideRecvrs.length;
                int index = 0;
                Receiver[][] result = new Receiver[width][];  
                Enumeration outsideRels = linkedRelations();
                while(outsideRels.hasMoreElements()) {
                    IORelation r = (IORelation) outsideRels.nextElement();
                    if(r == relation) {
                        result = new Receiver[width][];    
                        int rstSize = 
                            java.lang.Math.min(width, insideWidth-index);
                        for (int i = 0; i< rstSize; i++) {
                            result[i] = insideRecvrs[index++];
                        }
                        break;
                    } else {
                        index += r.getWidth();                        
                        if(index > insideWidth) break;
                    }
                }
                return result;
            }
        }
    }

    /** If the port is an output, return the remote receivers that can
     *  receive from the port, otherwise return null.  For an output
     *  port, the returned value is an array of arrays.  The first index
     *  (the row) specifies the channel number.  The second index (the
     *  column) specifies the receiver number within the group of
     *  receivers that get copies of the same channel.  The number of
     *  channels (rows) is the width of the port.  If the width is zero,
     *  or if the port is not connected to any input ports,
     *  this method will return null, as if the port were not an output
     *  port.
     *
     *  This method may have the effect of creating new receivers in the
     *  remote input ports, if they do not already have the right number of
     *  receivers.  In this case, previous receivers are lost, together
     *  with any data they may contain.
     *
     *  This method is synchronized on the workspace.
     *  @return The receivers for output data.
     */
    public Receiver[][] getRemoteReceivers() {
        synchronized(workspace()) {
            if (!isOutput()) return null;

            int width = getWidth();
            if (width <= 0) return null;
            
            // For atomic port, try the cached _farReceivers
            // Check validity of cached version
            if(isAtomic() && _farReceiversVersion == workspace().getVersion()) {
                return _farReceivers;
            }
            // If not an atomic port or Cache is not valid.  Reconstruct it.
            Receiver[][] farReceivers = new Receiver[width][];
            Enumeration relations = linkedRelations();
            int index = 0;
            boolean foundremoteinput = false;
            while(relations.hasMoreElements()) {
                IORelation r = (IORelation) relations.nextElement();
                Receiver[][] rr;
                rr = r.deepReceivers(this);
                if (rr != null) {
                    for(int i = 0; i < rr.length; i++) {
                        farReceivers[index] = rr[i];
                        index++;
                        foundremoteinput = true;
                    }
                } else {
                    // create a number of null entries in farReceivers
                    // corresponding to the width of relation r
                    index += r.getWidth();
                }
            }
            if (!foundremoteinput) {
                // No remote receivers
                farReceivers = null;
            }
            // For an atomic port, cache the result.
            if(isAtomic()) {
                _farReceiversVersion = workspace().getVersion();
                _farReceivers = farReceivers;
            }
            return farReceivers;
        }
    }   

    /** If this port is an output, return the remote receivers that can
     *  receive data from this port through the specified relation, 
     *  otherwise return null.  The relation should linked to the port
     *  from the inside, otherwise an exception is thrown. For an output
     *  port, the returned value is an array of arrays.  The first index
     *  (the row) specifies the stream number.  The second index (the
     *  column) specifies the receiver number within the group of
     *  receivers that get copies of the same channel.  The number of
     *  channels (rows) is the width of the specified relation.  
     *  If this width is zero, or if the port is not connected to
     *  any outside input ports through the relation,
     *  this method will return null, as if the port were not an output
     *  port.
     *
     *  This method may have the effect of creating new receivers in the
     *  remote input ports, if they do not already have the right number of
     *  receivers.  In this case, previous receivers are lost, together
     *  with any data they may contain.
     *
     *  This method is synchronized on the workspace.
     *  @return The receivers for output data.
     *  @exception IllegalActionException If the IORelation is not linked
     *       to the port from the inside.
     */
    public Receiver[][] getRemoteReceivers(IORelation relation)
            throws IllegalActionException {
        synchronized(workspace()) {
            if (!isInsideLinked(relation)) {
                throw new IllegalActionException(this, relation,
                    "not linked from the inside.");
            }
                
            if (!isOutput()) return null;

            int width = relation.getWidth();
            if (width <= 0) return null;
            
            // no cache used.
            Receiver[][] outsideRecvrs = getRemoteReceivers();
            if(outsideRecvrs == null) {
                return null;
            }
            Receiver[][] result = new Receiver[width][];
            Enumeration insideRels = insideRelations();
            int index = 0;
            while(insideRels.hasMoreElements()) {
                IORelation r = (IORelation) insideRels.nextElement();
                if(r == relation) {
                    int size = java.lang.Math.min
                            (width, outsideRecvrs.length-index);
                    //NOTE: if size = 0, the for loop is skipped.
                    //      result in returning null.
                    for(int i = 0; i<size; i++) {
                        result[i] = outsideRecvrs[i+index];
                    }
                    break;
                }
                index += r.getWidth();
            }
            return result;
        }
    }

    /** Return the width of the port.  The width is the sum of the
     *  widths of the relations that the port is linked to (on the outside).
     *  This method is synchronized on the workspace.
     *  @return The width of the port.
     */
    public int getWidth() {
        synchronized(workspace()) {
            if(_widthversion != workspace().getVersion()) {
                _widthversion = workspace().getVersion();
                int sum = 0;
                Enumeration relations = linkedRelations();
                while(relations.hasMoreElements()) {
                    IORelation r = (IORelation) relations.nextElement();
                    sum +=r.getWidth();
                }
                _width = sum;
            }
        }
        return _width;
    }

    /** Return true if the port is atomic.  The port is atomic
     *  if its container is atomic, or if it has no container.
     *  @see pt.kernel.ComponentEntity#isAtomic
     */
    public boolean isAtomic() {
        ComponentEntity container = (ComponentEntity) getContainer();
        if(container != null) {
            return container.isAtomic();
        } else {
            return true;
        }
    }

    /** Return true if the port is an input.  The port is an input
     *  if either makeInput() has been called with a true argument, or
     *  it is connected on the inside to an input port, or if it is
     *  connected on the inside to the inside of an output port.
     */	
    public boolean isInput() {
        if (_insideinputversion != workspace().getVersion()) {
            // Check to see whether any port linked on the inside is an input.
            Enumeration ports = deepInsidePorts();
            while(ports.hasMoreElements()) {
                IOPort p = (IOPort) ports.nextElement();
                // Rule out case where this port itself is listed...
                if (p != this && p.isInput()) _isinput = true;
            }
            _insideinputversion = workspace().getVersion();
        }
        return _isinput;
    }

    /** Return true if the port is a multiport.  The port is a multiport
     *  if makeMultiport() has been called with a true argument.
     */
    public boolean isMultiport() {
        return _ismultiport;
    }

    /** Return true if the port is an output. The port is an output
     *  if either makeOutput() has been called with a true argument, or
     *  it is connected on the inside to an output port, or it is
     *  connected on the inside to the inside of an input port.
     */	
    public boolean isOutput() {
        if (_insideoutputversion != workspace().getVersion()) {
            // Check to see whether any port linked on the inside is an output.
            Enumeration ports = deepInsidePorts();
            while(ports.hasMoreElements()) {
                IOPort p = (IOPort) ports.nextElement();
                // Rule out case where this port itself is listed...
                if (p != this && p.isOutput()) _isoutput = true;
            }
            _insideoutputversion = workspace().getVersion();
        }
        return _isoutput;
    }

    /** Override parent method to ensure validity of the width of the port.
     *  If the given relation is already linked to this port from the 
     *  inside or the outside, or the
     *  argument is null, do nothing.
     *  Otherwise, create a new link or throw an exception if the link is
     *  invalid.  If this port is not a multiport, then the width of the
     *  relation is required to be specified at exactly one.
     *  To prohibit links across levels of the hierarchy, use link().
     *  This method is synchronized on the workspace and increments
     *  its version.
     *  @exception IllegalActionException If the port already linked to a
     *   relation and is not a multiport; or if the relation has width
     *   not exactly one and the port is not a multiport; or if the 
     *   relation is incompatible with this port; or if the port has
     *   no container; or the port is not in the same workspace as
     *   the relation.
     */
    public void liberalLink(Relation relation) throws IllegalActionException {
        synchronized(workspace()) {
            _checkRelation(relation);
            IORelation rel = (IORelation) relation;
            if(!isLinked(rel) && !isInsideLinked(rel)) {
                // Check for existing inside or outside links
                boolean insidelink = _outside(relation.getContainer());
                if(!isMultiport()) {
                    if ((insidelink && numInsideLinks() >= 1)
                    || (!insidelink && numLinks() >= 1)) {
                        throw new IllegalActionException(this, 
                        "Attempt to link more than one relation " +
                        "to a single port.");
                    }
                }
                if ((rel.getWidth() != 1) || !rel.widthFixed()) {
                    // Relation is a bus.
                    if(!isMultiport()) {
                        throw new IllegalActionException(this,  rel,
                        "Attempt to link a bus relation to a single port.");
                    }
                    if(insidelink) {
                        try {
                            _getInsideWidth(null);
                        } catch (InvalidStateException ex) {
                            throw new IllegalActionException(this, rel,
                            "Attempt to link a second bus relation with " +
                            "unspecified width to the inside of a port.");
                        }
                    } else {
                        Enumeration relations = linkedRelations();
                        while (relations.hasMoreElements()) {
                            IORelation r = (IORelation)relations.nextElement();
                            if (!r.widthFixed()) {
                                throw new IllegalActionException(this, rel,
                                "Attempt to link a second bus relation with " +
                                "unspecified width to the outside of a port.");
                            }
                        }
                    }
                }
                super.liberalLink(rel);
            }
        }
    }

    /** If the argument is true, make the port an input port.
     *  If the argument is false, make the port not an input port.
     *  This has no effect if the port is a transparent port that is
     *  linked on the inside to input ports.  In that case, the port
     *  is an input port regardless of whether and how this method is called.
     *  This method is synchronized on the workspace and increments
     *  its version.
     */	
    public void makeInput(boolean isinput) {
        synchronized(workspace()) {
            _isinput = isinput;
            workspace().incrVersion();
        }
    }

    /** If the argument is true, make the port a multiport.
     *  That is, make it capable of linking with multiple IORelations, 
     *  or with IORelations that have width greater than one. 
     *  If the argument is false, allow only links with a single
     *  IORelation of width one.
     *  This has no effect if the port is a transparent port that is
     *  linked on the inside to a multiport.  In that case, the port
     *  is a multiport regardless of whether and how this method is called.
     *  This method is synchronized on the workspace and increments
     *  its version.
     */	
    public void makeMultiport(boolean ismultiport) {
        synchronized(workspace()) {
            _ismultiport = ismultiport;
            workspace().incrVersion();
        }
    }

    /** If the argument is true, make the port an output port.
     *  If the argument is false,
     *  make the port not an output port.
     *  This has no effect if the port is a transparent port that is
     *  linked on the inside to output ports.  In that case, the port
     *  is an output port regardless of whether and how this method is called.
     *  This method is synchronized on the workspace and increments
     *  its version.
     */	
    public void makeOutput(boolean isoutput) {
        synchronized(workspace()) {
            _isoutput = isoutput;
            workspace().incrVersion();
        }
    }
    
    /** Create a new local Receiver.  In this base class, the default
     *  receiver is a Mailbox.  Derived classes should override this to
     *  create receivers appropriate to their model of computation.
     */
    public Receiver newReceiver() {
        return new Mailbox(this);
    }

    /** Send the specified token to all receivers connected to the
     *  specified channel.  The first receiver gets the actual token,
     *  while subsequent ones get a clone.  If there are no receivers,
     *  then do nothing. The transfer is accomplished by calling the put()
     *  method of the destination receivers.
     *  @param channelindex The index of the channel, from 0 to width-1
     *  @param token The token to send
     *  @exception CloneNotSupportedException If the token cannot be cloned
     *   and there is more than one destination.
     *  @exception IllegalActionException If the port is not an output,
     *   or if the index is out of range.
     */	
    public void send(int channelindex, Token token)
           throws CloneNotSupportedException, IllegalActionException {
        synchronized(workspace()) {
            if (!isOutput()) {
                throw new IllegalActionException(this,
                "send: Tokens can only be sent from an output port.");
            }
            if (channelindex >= getWidth()) {
                throw new IllegalActionException(this,
                "send: channel index is out of range.");
            }
            Receiver[][] fr = getRemoteReceivers();
            if (fr == null || fr[channelindex] == null) return;
            boolean first = true;
            for (int j = 0; j < fr[channelindex].length; j++) {
                if (first) {
                    fr[channelindex][j].put(token);
                    first = false;
                } else {
                    fr[channelindex][j].put((Token)(token.clone()));
                }
            }
        }
    }

    /** Unlink the specified Relation. If the Relation
     *  is not linked to this port, do nothing.
     *  This method is synchronized on the workspace
     *  and increments its version number.
     *  @param relation The relation to unlink.
     */
    public void unlink(Relation relation) {
        synchronized(workspace()) {
            super.unlink(relation);
            if (_localReceiversTable != null) {
                _localReceiversTable.remove(relation);
            }
        }
    } 

    /** Unlink all relations.
     *  This method is synchronized on the
     *  workspace and increments its version number.
     */	
    public void unlinkAll() {
        synchronized(workspace()) {
            super.unlinkAll();
            if (_localReceiversTable != null) {
                _localReceiversTable.clear();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Do nothing if the specified relation is compatible with this port.
     *  Otherwise, throw an exception.
     *  @param relation
     *  @exception IllegalActionException Incompatible relation.
     */	
    protected void _checkRelation(Relation relation) 
            throws IllegalActionException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this,
                   "Attempt to link to an incompatible relation." +
                   " IOPort requires IORelation.");
        }
    }

    /** Return the sums of the widths of the relations linked on the inside,
     *  except the specified port.  If any of these relations has not had
     *  its width specified, throw an exception.  This is used by IORelation
     *  to infer the width of a bus with unspecified width and to determine
     *  whether more than one relation with unspecified width is linked on the
     *  inside, and by the liberalLink() method to check validity of the link.
     *  If the argument is null, all relations linked on the inside are checked.
     */
    protected int _getInsideWidth(IORelation except) {
        int result = 0;
        Enumeration relations = insideRelations();
        while (relations.hasMoreElements()) {
            IORelation rel = (IORelation)relations.nextElement();
            if (rel != except) {
                if (!rel.widthFixed()) {
                    throw new InvalidStateException(this,
                    "Width of inside relations cannot be determined.");
                }
                result += rel.getWidth();
            }
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Indicate whether the port is an input, an output, or both.
    // The value may be overriden in transparent ports, in that if
    // a transparent port is inside linked to an input or output port,
    // then it will be considered an inside or output port respectively.
    // This determination is cached, so we need variables to track the
    // validity of the cache.
    // 'transient' means that the variable will not be serialized.
    private boolean _isinput, _isoutput;
    private transient boolean _insideinput, _insideoutput;
    private transient long _insideinputversion = -1;
    private transient long _insideoutputversion = -1;

    // Indicate whether the port is a multiport. Default false.
    private boolean _ismultiport = false;

    // The cached width of the port, which is the sum of the widths of the
    // linked relations.  The default 0 because initially there are no
    // linked relations.  It is set or updated when getWidth() is called.
    // 'transient' means that the variable will not be serialized.
    private transient int _width = 0;
    // The workspace version number on the last update of the _width.
    // 'transient' means that the variable will not be serialized.
    private transient long _widthversion = -1;

    // A cache of the deeply connected Receivers, and the versions.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _farReceivers;
    private transient long _farReceiversVersion = -1;

    // A cache of the local Receivers, and the version.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _localReceivers;
    private transient long _localReceiversVersion = -1;

    // The local receivers, indexed by relation.
    private Hashtable _localReceiversTable;

    // description variables.
    // FIXME: consider move them into pt.kernel.Nameable.java

    /**
     * The description() method returns the receivers of this port.
     */ 
    public static final int RECEIVERS = 7;

    /**
     * The description() method returns the remote receivers of this port.
     */ 
    public static final int REMOTE_RECEIVERS = 8;
    
}





