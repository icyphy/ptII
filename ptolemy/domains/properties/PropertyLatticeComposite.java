/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

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
*/
package ptolemy.domains.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.LatticeProperty;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

import ptolemy.graph.GraphStateException;

public class PropertyLatticeComposite extends FSMActor {

    public PropertyLatticeComposite(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public PropertyLatticeComposite(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return true if the contained elements form a lattice;
     * false, otherwise.
     */
    public boolean isLattice() {
        List<LatticeElement> elements = (List<LatticeElement>) deepEntityList();

        _clearHighlightColor(elements);

        PropertyLattice lattice = new Lattice(elements);

        // 06/03/2009 Charles Shelton - Bug fixes for the isLattice() function:
        // - Catch the exception from the directed acyclic graph _validate() method that checks for a cycle in the graph
        // - LatticeProperty class is derived from the Property class and not the NamedObj class and cannot be cast to NamedObj
        //   so replace all getName() calls with toString()
        // - The elements list contains LatticeElement objects, not LatticeProperty objects, so the leastUpeperBound() method does not work.
        //   use the list of LatticeProperty objects from the newly created Lattice object instead.
        // - Added additional debug messages to provide both positive and negative feedback about the lattice.
        
        try {
            if ((lattice.top() == null)) {
                _debug("This is not a lattice.");
                MessageHandler.error("Cannot find an unique top element.");
                return false;
            } else {
                LatticeProperty top = (LatticeProperty) lattice.top();
                _debug("Top is: " + top.toString());
            }
        } catch (GraphStateException e) {
            _debug("This is not a lattice.");
            MessageHandler.error("Proposed lattice has a cycle and is not a true lattice.");
            return false;
        }

        if ((lattice.bottom() == null)) {
            _debug("This is not a lattice.");
            MessageHandler.error("Cannot find an unique bottom element.");
            return false;
        } else {
            LatticeProperty bottom = (LatticeProperty) lattice.bottom();
            _debug("Bottom is: " + bottom.toString());
        }
        
        
        List<LatticeProperty> latticeProperties = ((Lattice) lattice).getLatticeProperties();

        // This is the same check done in ptolemy.graph.DirectedAcyclicGraph.
        for (int i = 0; i < (latticeProperties.size() - 1); i++) {
            for (int j = i + 1; j < latticeProperties.size(); j++) {
                LatticeProperty lub = (LatticeProperty) lattice.leastUpperBound(latticeProperties.get(i), latticeProperties.get(j));

                if (lub == null) {
                    // FIXME: add highlight color?

                    // The offending nodes.
                    _debug("This is not a lattice.");
                    MessageHandler.error("\"" + elements.get(i).getName()
                            + "\" and \"" + elements.get(j).getName() + "\""
                            + " does not have an unique least upper bound (LUB).");

                    return false;
                } else {
                    _debug("LUB(" + elements.get(i).getName()
                            + ", " + elements.get(j).getName() + "): "
                            + lub.toString());
                }
            }
        }
        
        _debug("This is a correctly formed lattice.");
        return true;
    }

    private void _clearHighlightColor(List<LatticeElement> elements) {
        // TODO Auto-generated method stub

    }

    public static class Lattice extends PropertyLattice {
        private List<LatticeProperty> _properties;

        public Lattice(List<LatticeElement> elements) {
            _properties = new ArrayList();

            HashMap map = new HashMap();

            // First add the property nodes to the lattice.
            for (LatticeElement element : elements) {
                LatticeProperty property =
                    new LatticeProperty(this, element.getName());

                property.setColor(element.solutionColor.getExpression());

                _properties.add(property);

                addNodeWeight(property);
                map.put(element, property);
            }

            // Create edges to connect the nodes.
            for (LatticeElement element : elements) {
                // for each outgoing edge.
                for (Port port : (List<Port>) element.outgoingPort.connectedPortList()) {
                    addEdge(map.get(element), map.get(port.getContainer()));
                }
            }
        }
        
        
        // 06/03/2009 Charles Shelton - Public method to access the list of LatticeProperty objects.
        public List<LatticeProperty> getLatticeProperties() {
            return _properties;
        }

        
        public Property getElement(String name)
        throws IllegalActionException {
            for (LatticeProperty property : _properties) {
                if (name.equalsIgnoreCase(property.toString())) {
                    return property;
                }
            }
            return null;
        }


    }
}
