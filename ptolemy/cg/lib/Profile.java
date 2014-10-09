/* A interface with meta information for modular generated code.

 Copyright (c) 2009-2014 The Regents of the University of California.
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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Profile

/**
 * Meta information about
 * modularly generated code such as port information.
 * In this way we have a way to interface with the
 * generated code.
 * The actual profile instances derive from this class.
 * @author Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (rodiers)
 * @Pt.AcceptedRating Red (rodiers)
 */
abstract public class Profile {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the port information.
     *  @return A list with for each port the information
     *  necessary to interface the generated code.
     */
    abstract public List<Port> ports();

    /** Return the list of firings in the graph.
     *  @return A list with for each actor the information
     *  necessary to interface the generated code.
     *  @exception IllegalActionException If there is a problem
     *  accessing components in the model.
     */
    abstract public List<FiringFunction> firings()
            throws IllegalActionException;

    /** Return firing per iteration of the actor
     * @return value of number of firings per iteration of the actor
     */

    /**
     * A named connection between an actor and a junction.
     */
    static public class Connection {
        /** The name of the actor to be connected. */
        public String actorName;

        /** The name of the junction. */
        public String junctionName;
    }

    /**
     * A profiled Junction.
     * @author dai
     *
     */
    static public class Junction {
        /** Construct a Junction.
         *  @param putActor The actor to add to the junction.
         *  @param putActorPort The port of the put actor.
         *  @param getActor The get actor that is to be added to the junction.
         *  @param getActorPort The port of the get actor.
         *  @param numTokens The number of tokens.
         */
        public Junction(String putActor, String putActorPort, String getActor,
                String getActorPort, int numTokens) {
            _putActor = putActor;
            numInitialTokens = numTokens;
        }

        /** Get the put actor name.
         *  @return the actor name.
         */
        public String getPutActorName() {
            return _putActor;
        }

        /** The number of initial tokens. */
        public int numInitialTokens = 0;

        private String _putActor = null;
    }

    /** The Firing Function. */
    static public class FiringFunction {
        /** Create a firing function.
         *  @param index  The firing index.
         */
        public FiringFunction(int index) {
            firingIndex = index;
            ports = new LinkedList();
            nextFiringFunctions = new LinkedList();
            previousFiringFunctions = new LinkedList();

            nextIterationFirings = new LinkedList();
            previousIterationFirings = new LinkedList();
        }

        /** The ports. */
        public List<FiringFunctionPort> ports;

        /** The next firing functions. */
        public List<Integer> nextFiringFunctions;

        /** The previous firing functions. */
        public List<Integer> previousFiringFunctions;

        /** The next iteration firings. */
        public List<Integer> nextIterationFirings;

        /** The previous iteration firings. */
        public List<Integer> previousIterationFirings;

        /** The firing index. */
        public int firingIndex;
    }

    /**
     * A class for actors in a graph information.
     */
    static public class ProfileActor {
        /** Create a Profile Actor.
         *  @param name The name of the actor.
         *  @param original True if this actor is original?
         *  @exception IllegalActionException Not thrown in this base clasee.
         */   
        public ProfileActor(String name, boolean original)
                throws IllegalActionException {
            _name = name;
            _isOriginal = original;
        }

        /** Return if an actor is an original ptolemy actor or not.
         * @return true is the actor is an original ptolemy actor like Ramp,
         * false if the actor is generated from some composite actor, thus it has profile
         */
        public boolean isOriginal() {
            return _isOriginal;
        }

        /** Return the name of this ProfileActor.
         *  @return the name of this ProfileActor
         */
        public String getName() {
            return _name;
        }

        /** Get the profile.
         *  @return the profile.   
         *  @exception IllegalActionException If there is a problem
         *  loading the class from the $HOME/cg directory.
         */
        public Profile getProfile() throws IllegalActionException {
            try {
                if (_profile != null) {
                    return _profile;
                } else {
                    String className = _name + "_profile";
                    Class<?> classInstance = null;

                    String home = System.getenv("HOME");
                    URL url = new URL("file:" + home + "/cg/");

                    ClassLoader classLoader = new URLClassLoader(
                            new URL[] { url });

                    classInstance = classLoader.loadClass(className);
                    _profile = (Profile) classInstance.newInstance();
                }
            } catch (Throwable throwable) {
                _profile = null;
                throw new IllegalActionException(
                        "Cannot locate the profile of the actor: " + _name);
            }
            return _profile;
        }

        /** The profile. */
        Profile _profile = null;

        private String _name;
        private boolean _isOriginal = true;
    }

    /** A firing function port. */
    static public class FiringFunctionPort {
        /** Create a firing function port.
         *  @param portName The name of the port.
         *  @param externalPort The external port.
         *  @param portRate The rate of the port.
         *  @param isInputPort True if this is an input port.
         */   
        public FiringFunctionPort(String portName, String externalPort,
                int portRate, boolean isInputPort) {
            name = portName;
            externalPortName = externalPort;
            rate = portRate;
            isInput = isInputPort;
        }

        /** The name of the external port. */
        public String name;

        /** The name of the external port? */
        public String externalPortName;

        /** The rate. */
        public int rate;

        /** True if this is an input port. */
        public boolean isInput;
    }

    /** The port information to interface with modular code.
     */
    static public class Port {

        /** Create the port.
         * @param name The name of the port.
         * @param publisher A flag that specifies whether it is a subscriber.
         * @param subscriber A flag that specifies whether it is a publisher.
         * @param width The width of the port.
         * @param rate The rate of the port
         * @param type The code type of the port that can be mapped back to ptolemy type
         * @param input A flag that specifies whether the port is an input port.
         * @param output A flag that specifies whether the port is an output port.
         * @param multiport A flag that specifies whether the port is multiport
         * @param pubSubChannelName The name
         */
        public Port(String name, boolean publisher, boolean subscriber,
                int width, int rate, int type, boolean input, boolean output,
                boolean multiport, String pubSubChannelName) {
            _name = name;
            _publisher = publisher;
            _subscriber = subscriber;
            _width = width;
            _rate = rate;
            _type = type;
            _input = input;
            _output = output;
            _multiport = multiport;
            _pubSubChannelName = pubSubChannelName;
        }

        /** Get the channel name for the publisher/subscriber pattern.
         *  @return The channel name for the publisher/subscriber.
         */
        public String getPubSubChannelName() {
            return _pubSubChannelName;
        }

        /** Return whether the port is an input.
         *  @return True when the port is an input port.
         */
        public boolean input() {
            return _input;
        }

        /**
         * Return whether the port is a multiport.
         * @return True if the port is a multiport.
         */
        public boolean multiport() {
            return _multiport;
        }

        /** Return the name of the port.
         *  @return the port name.
         */
        public String name() {
            return _name;
        }

        /** Return whether the port is an output.
         *  @return True when the port is an output port.
         */
        public boolean output() {
            return _output;
        }

        /** Return whether the port is an publisher port.
         *  @return True when the port is an publisher port.
         */
        public boolean publisher() {
            return _publisher;
        }

        /** Return whether the port is an subscriber port.
         *  @return True when the port is an subscriber port.
         */
        public boolean subscriber() {
            return _subscriber;
        }

        /** Return whether the width of the port.
         *  @return the width of the port.
         */
        public int width() {
            return _width;
        }

        /** Return whether the rate of the port.
         *  @return the rate of the port.
         */
        public int rate() {
            return _rate;
        }

        /** Return whether the rate of the port.
         *  @return the rate of the port.
         */
        public int type() {
            return _type;
        }

        /** A flag that specifies whether the port in an input port.*/
        private boolean _input;

        /** A flag that specifies whether the port in an input port */
        private boolean _multiport;

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

        /** The codegen type of the port */
        private int _type;
    }

}
