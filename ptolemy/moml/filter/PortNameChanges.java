/* Filter actors for port name changes.

 Copyright (c) 2002-2013 The Regents of the University of California.
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
package ptolemy.moml.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// FilterBackwardCompatibility

/** When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause MoMLParser to filter so that models from
 earlier releases will run in the current release.

 <p>This class will filter for actors that have had port name changes.

 <p>NOTE: This class and ParameterNameChange might conflict if
 a port and parameter have the same name.

 @author Christopher Hylands, Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PortNameChanges extends MoMLFilterSimple {
    /** Clear the map of actors with port name changes.
     */
    public static void clear() {
        _actorsWithPortNameChanges = new HashMap();
    }

    /**  If the attributeName is "class" and attributeValue names a
     *   class that has had its port names changed between releases,
     *   then substitute in the new port names.
     *
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return the value of the attributeValue argument.
     */
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        // This method gets called many times by the MoMLParser,
        // so we try to be smart about the number of comparisons
        // and we try to group comparisons together so that we
        // are not making the same comparison more than once.
        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        if (attributeName.equals("name")) {
            // Save the name of the for later use if we see a "class"
            _lastNameSeen = attributeValue;

            if (_currentlyProcessingActorWithPortNameChanges
                    && _portMap != null && _portMap.containsKey(attributeValue)) {
                // We will do the above checks only if we found a
                // class that had port name changes, but have not
                // yet found the next class.
                // Here, we add the port name and the new port name
                // to a map for later use.
                String containerName = container.getFullName();

                String newPort = (String) _portMap.get(attributeValue);

                // Save the container.newPort name for later use.
                _containerPortMap.put(containerName + "." + attributeValue,
                        containerName + "." + newPort);
                MoMLParser.setModified(true);
                return newPort;
            }
        }

        if (attributeName.equals("class")) {
            // Look for lines like:
            // <entity name="ComplexToCartesian1"
            //   class="ptolemy.actor.lib.conversions.ComplexToCartesian">
            if (_actorsWithPortNameChanges.containsKey(attributeValue)) {
                // We found a class with a port name change.
                _currentlyProcessingActorWithPortNameChanges = true;
                _doneProcessingActorWithPortNameChanges = false;
                _currentActorFullName = container.getFullName() + "."
                        + _lastNameSeen;
                _portMap = (HashMap) _actorsWithPortNameChanges
                        .get(attributeValue);
            } else if (_currentlyProcessingActorWithPortNameChanges
                    && container != null
                    && !container.getFullName().equals(_currentActorFullName)
                    && !container.getFullName().startsWith(
                            _currentActorFullName)) {
                // We found another class in a different container
                // while handling a class with port name changes, so
                // set _doneProcessingActorWithPortNameChanges so we
                // can handle any port changes later.
                _currentlyProcessingActorWithPortNameChanges = false;
                _doneProcessingActorWithPortNameChanges = true;
            }
        } else if (_doneProcessingActorWithPortNameChanges
                && attributeName.equals("port")
                && _containerPortMap.containsKey(container.getFullName() + "."
                        + attributeValue)) {
            // We are processing actors that have port names.
            // Now map the old port to the new port.
            String newPort = (String) _containerPortMap.get(container
                    .getFullName() + "." + attributeValue);

            // Extreme chaos here because sometimes
            // container.getFullName() will be ".transform_2.transform" and
            // attributeValue will be "ComplexToCartesian.real"
            // and sometimes container.getFullName() will be
            // ".transform_2.transform.ComplexToCartesian"
            // and attributeValue will be "real"
            newPort = newPort.substring(container.getFullName().length() + 1);

            MoMLParser.setModified(true);
            return newPort;
            // The following else if () can be deleted if it causes trouble.
            // The reason to add the code is that a port may not be contained in
            // _containerPortMap if it is encountered the first time here --- Gang
        } else if (_doneProcessingActorWithPortNameChanges
                && attributeName.equals("port")
                // If we don't have the following case, sometimes opening a
                // model with a TransformationController inside may cause an
                // exception because the container may be a Ptera event, not a
                // CompositeEntity. (tfeng 08/03/2008)
                // <<<
                && container instanceof CompositeEntity) {
            // >>>
            int lastIndex = attributeValue.lastIndexOf(".");
            NamedObj portContainer = null;
            String portContainerName = null;
            String portName = null;
            if (lastIndex > 0) {
                portContainerName = attributeValue.substring(0, lastIndex);
                portContainer = ((CompositeEntity) container)
                        .getEntity(portContainerName);
                portName = attributeValue.substring(lastIndex + 1);
            } else {
                portContainer = container;
                portName = attributeValue;
            }
            if (portContainer != null) {
                String className = portContainer.getClassName();
                if (_actorsWithPortNameChanges.containsKey(className)) {
                    HashMap portMap = (HashMap) _actorsWithPortNameChanges
                            .get(className);
                    if (portMap.containsKey(portName)) {
                        String newPort = (String) portMap.get(portName);
                        if (lastIndex > 0) {
                            newPort = portContainerName + "." + newPort;
                        }
                        return newPort;
                    }
                }
            }
        }

        return attributeValue;
    }

    /** Clear recorded state for this element so that it does not
     *  interfere with the next element.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *  only in the doc and configure elements
     *  @param xmlFile The file currently being parsed, ignored
     *  by this method.
     *  @exception Exception Not thrown in this base class.
     */
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
        if (elementName.equals("entity") || elementName.equals("class")) {
            _currentActorFullName = null;
            _currentlyProcessingActorWithPortNameChanges = false;
            _doneProcessingActorWithPortNameChanges = false;
            _lastNameSeen = null;
        }
    }

    /** Add a class to be filtered and the old and new port names.
     *  Note that if you add a class with this method, then you must
     *  remove it with {@link #remove(String)}, calling
     *  "new PortNameChanges()" will not remove a class that was
     *  added with this method.
     *  @param className The name of the class to be filtered
     *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
     *  @param portNameMap The HashMap that contains the old port names
     *  as keys and the new port names as a values. If the value of the
     *  HashMap is null then the rest of the attribute is skipped.
     *  @see #remove(String)
     */
    public void put(String className, HashMap portNameMap) {
        _actorsWithPortNameChanges.put(className, portNameMap);
    }

    /** Remove a class to be filtered.
     *  @param className The name of the class to be filtered
     *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
     *  @see #put(String, HashMap)
     */
    public void remove(String className) {
        _actorsWithPortNameChanges.remove(className);
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Update any actor port names that have been\n"
                + "renamed.\n"
                + "Below are the actors that are affected, along\n"
                + "with the old port name and the new port name:\n");
        Iterator actors = _actorsWithPortNameChanges.keySet().iterator();

        while (actors.hasNext()) {
            String actor = (String) actors.next();
            results.append("\t" + actor + "\n");

            HashMap portMap = (HashMap) _actorsWithPortNameChanges.get(actor);
            Iterator portMapEntries = portMap.entrySet().iterator();

            while (portMapEntries.hasNext()) {
                Map.Entry ports = (Map.Entry) portMapEntries.next();
                String oldPort = (String) ports.getKey();
                String newPort = (String) ports.getValue();
                results.append("\t\t" + oldPort + "\t -> " + newPort + "\n");
            }
        }

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Map of actor names a HashMap of old ports to new ports
    private static HashMap _actorsWithPortNameChanges;

    // Map of old container.port to new container.newPort;
    private static HashMap _containerPortMap;

    // The the full name of the actor we are currently processing
    private String _currentActorFullName;

    // Set to true if we are currently processing an actor with port name
    // changes, set to false when we are done.
    private boolean _currentlyProcessingActorWithPortNameChanges = false;

    // Set to true if we are done processing an actor.
    private boolean _doneProcessingActorWithPortNameChanges = false;

    // Last "name" value seen, for use if we see a "class".
    private String _lastNameSeen;

    // Cache of map from old port names to new port names for
    // the actor we are working on.
    private static HashMap _portMap;

    static {
        ///////////////////////////////////////////////////////////
        // Actors with port name changes.
        _actorsWithPortNameChanges = new HashMap();
        _containerPortMap = new HashMap();

        // ComplexToCartesian: real is now x, imag is now y.
        HashMap cartesianPorts = new HashMap();
        cartesianPorts.put("real", "x");
        cartesianPorts.put("imag", "y");
        _actorsWithPortNameChanges.put(
                "ptolemy.actor.lib.conversions.ComplexToCartesian",
                cartesianPorts);

        // CartesianToComplex has the same ports as ComplexToCartesian.
        _actorsWithPortNameChanges.put(
                "ptolemy.actor.lib.conversions.CartesianToComplex",
                cartesianPorts);

        // Sleep
        HashMap sleepPorts = new HashMap();
        sleepPorts.put("delay", "sleepTime");
        _actorsWithPortNameChanges.put("ptolemy.actor.lib.Sleep", sleepPorts);

        // Scrambler changed between 3.0.2 and 4.0
        // Port name change from initial to initialState.
        HashMap scramblerPorts = new HashMap();
        scramblerPorts.put("initial", "initialState");
        _actorsWithPortNameChanges.put("ptolemy.actor.lib.comm.Scrambler",
                scramblerPorts);

        // ConvolutionalCoder changed between 3.0.2 and 4.0
        HashMap convolutionalCoderPorts = new HashMap();
        convolutionalCoderPorts.put("initial", "initialState");
        convolutionalCoderPorts.put("uncodeBlockSize", "uncodedRate");

        _actorsWithPortNameChanges.put(
                "ptolemy.actor.lib.comm.ConvolutionalCoder",
                convolutionalCoderPorts);

        // ViterbiDecoder changed between 3.0.2 and 4.0
        HashMap viterbiDecoderPorts = new HashMap();

        //viterbiDecoderPorts.put("initial", "initialState");
        viterbiDecoderPorts.put("uncodeBlockSize", "uncodedRate");
        viterbiDecoderPorts.put("amplitude", "constellation");

        _actorsWithPortNameChanges.put("ptolemy.actor.lib.comm.ViterbiDecoder",
                viterbiDecoderPorts);

        // Server: after 4.1, the newServiceTime is renamed to serviceTime.
        // Regrettably, after 5.1, it reverted.
        HashMap serverPorts = new HashMap();
        serverPorts.put("newServiceTime", "serviceTime");
        _actorsWithPortNameChanges.put("ptolemy.domains.de.lib.Server",
                serverPorts);
    }
}
