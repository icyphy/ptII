/* A interface with meta information for modular generated code. 

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.cg.lib;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
////Profile

/** This class contains meta information about 
 *  modularly generated code such as port information.
 *  In this way we have a way to interface with the
 *  generated code.
 *  The actual profile instances derive from this class.
 *  @author Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (rodiers)
 *  @Pt.AcceptedRating Red (rodiers)
 */
abstract public class Profile {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the port information.
     *  @return A list with for each port the information
     *  necessary to interface the generated code.
     */
    abstract public List<Port> ports();

    /** A class contains the port information to
     * interface with modular code.
     */
    static public class Port {
        
        /** Create the port.
         * @param name The name of the port.
         * @param publisher A flag that specifies whether it is a subscriber.
         * @param subscriber A flag that specifies whether it is a publisher.
         * @param width The width of the port. 
         * @param input A flag that specifies whether the port is an input port.
         * @param output A flag that specifies whether the port is an output port.
         * @param pubSubChannelName The name
         */
        public Port(String name, boolean publisher, boolean subscriber, int width, int rate,
                boolean input, boolean output, String pubSubChannelName) {
            _name = name;
            _publisher = publisher;
            _subscriber = subscriber;
            _width = width;
            _rate = rate;
            _input = input;
            _output = output;
            _pubSubChannelName = pubSubChannelName;
        }
        
        /** Get the channel name for the publisher/subscriber pattern.
         *  @return The channel name for the publisher/subscriber.
         */
        public String getPubSubChannelName() { return _pubSubChannelName; }

        /** Return whether the port is an input.
         *  @return True when the port is an input port.
         */
        public boolean input() { return _input; }
        
        /** Return the name of the port.
         *  @return the port name.
         */
        public String name() { return _name; }
        
        /** Return whether the port is an output.
         *  @return True when the port is an output port.
         */
        public boolean output() { return _output; }

        /** Return whether the port is an publisher port.
         *  @return True when the port is an publisher port.
         */
        public boolean publisher() { return _publisher; }
        
        /** Return whether the port is an subscriber port.
         *  @return True when the port is an subscriber port.
         */
        public boolean subscriber() { return _subscriber; }

        /** Return whether the width of the port.
         *  @return the width of the port.
         */     
        public int width() { return _width; }
        
        /** Return whether the rate of the port.
         *  @return the rate of the port.
         */     
        public int rate() { return _rate;}

        /** A flag that specifies whether the port in an input port.*/
        private boolean _input;

        /** The name of the port.*/
        private String _name;
        
        /** A flag that specifies whether the port in an output port.*/
        private boolean _output;
        
        /** A flag that specifies whether the port in an publisher port.*/
        private boolean _publisher;
        
        /** The name of the channel for the publisher port/subscriber port.*/
        private String _pubSubChannelName;
        
        /** A flag that specifies whether the port in an subscriber port.*/
        private boolean _subscriber;

        /** The width of the port.*/
        private int _width;
        
        /** The rate of the port */
        private int _rate;
    }    
}
