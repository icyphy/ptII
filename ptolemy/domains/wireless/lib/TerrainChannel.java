/* A channel deals with terrain models.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Red (pjb2e@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.lib;

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.data.RecordToken;
import ptolemy.domains.wireless.kernel.PropertyTransformer;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.domains.wireless.kernel.WirelessReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TerrainChannel

/**
This is a model of a wireless channel that support terrain models
in a sensor fields and uses the terrain models to calculate the
propagation effects from the sender to the receiver more accurately.

FIXME: we can add this feature to the AtomicWirelessChannel so that
all extended channels can support terrain modeling. The reason I don't
do it this way is to try to keep this channel specific...
<p> 
@author Yang Zhao
@version $ $
*/
public class TerrainChannel extends PowerLossChannel {

    /** Construct a channel with the given name and container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown. If the name argument
     *  is null, then the name is set to the empty string.
     *  @param container The container.
     *  @param name The name of the channel.
     *  @exception IllegalActionException If the container is incompatible.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public TerrainChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Initialize the _portPropertyTransformer set.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _terrainProperty = new LinkedList();
    }

    /** Register a PropertyTransformer for a channel.
     */
    public void registerPropertyTransformer(
            PropertyTransformer transformer) {
        _terrainProperty.add(transformer);
    }
    /** Transform the properties to take into account terrain effects
     *  for transmission between the specified sender
     *  and the specified receiver. 
     *  @param properties The transmit properties.
     *  @param sender The sending port.
     *  @param receiver The receiving port.
     *  @return The transformed properties.
     *  @exception IllegalActionException If the properties cannot
     *   be transformed. Not thrown in this base class.
     */
    protected RecordToken _transformProperties(
            RecordToken properties,
            WirelessIOPort sender, 
            WirelessReceiver receiver)
            throws IllegalActionException {
        RecordToken property = properties;
        if (_terrainProperty != null) {
            Iterator terrainProperties = _terrainProperty.iterator();
            
            WirelessIOPort destination = (WirelessIOPort)receiver.getContainer();
            while (terrainProperties.hasNext()) {
                PropertyTransformer terrain = (PropertyTransformer) terrainProperties.next();
                property = terrain.getProperty(property, sender, destination);
            }
        }
        // Use the superclass to merge the record argument with the
        // properties.
        return super._transformProperties(
                property, sender, receiver);

    }
    
    private LinkedList _terrainProperty;
}
