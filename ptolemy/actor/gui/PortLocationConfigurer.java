/* A GUI widget for configuring port locations.

 Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.moml.MoMLChangeRequest;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;

//////////////////////////////////////////////////////////////////////////
//// PortLocationConfigurer
/**
This class is an editor to configure the port locations of an object.
It supports setting their cardinal positions

Only ports that extend the TypedIOPort class are listed, since more
primitive ports cannot be configured in this way.

@see Configurer
@author Mason Holding, Steve Neuendorffer and Edward A. Lee, Contributor: Christopher Hylands
@version $Id$
@since Ptolemy 2.1
*/
public class PortLocationConfigurer extends Query implements QueryListener {

    /** Construct a port configurer for the specified entity.
     *  @param object The entity to configure.
     */
    public PortLocationConfigurer(Entity object) {
      super();
        this.addQueryListener(this);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setTextWidth(2);

      // The second column is for type designators.
      setColumns(2);

      _object = object;
      //      StringAttribute ordinal = null;
      StringAttribute cardinal = null;
      //String ordinalValue = null;
      String cardinalValue = null;
      int i = 0;
      Iterator ports = _object.portList().iterator();
      // Figure out how many ports on each side.
      while (ports.hasNext()) {
         Object candidate = ports.next();
         if (candidate instanceof TypedIOPort) {
            TypedIOPort port = (TypedIOPort)candidate;
            cardinal = (StringAttribute)port.getAttribute("_cardinal");
//             if (cardinal != null) {
//               cardinalValue = cardinal.getExpression().toUpperCase();
//               if (cardinalValue.equals("WEST")){
//                  _portsWest.add(port.getName());
//                  _numWest++;
//               } else if (cardinalValue.equals("EAST")){
//                  _portsEast.add(port.getName());
//                  _numEast++;
//               } else if (cardinalValue.equals("NORTH")){
//                  _portsNorth.add(port.getName());
//                  _numNorth++;
//               } else {
//                  _portsSouth.add(port.getName());
//                  _numSouth++;
//               }
//             } else if (port.isInput() && !port.isOutput()) {
//                _portsWest.add(port.getName());
//                _numWest++;
//             } else if (port.isOutput() && !port.isInput()) {
//                _portsEast.add(port.getName());
//                _numEast++;
//             } else {
//                _portsSouth.add(port.getName());
//                _numSouth++;
//             }

            cardinalValue = null;
         }
      }

//       if (_numWest > 0) {
//          _ordinalsWest = new String[_numWest];
//          for (i = 0; i < _numWest; i++) {
//             _ordinalsWest[i] = Integer.toString(i);
//          }
//       }

//       if (_numEast > 0) {
//          _ordinalsEast = new String[_numEast];
//          for (i = 0; i < _numEast; i++) {
//             _ordinalsEast[i] = Integer.toString(i);
//          }
//       }

//       if (_numNorth > 0) {
//          _ordinalsNorth = new String[_numNorth];
//          for (i = 0; i < _numNorth; i++) {
//             _ordinalsNorth[i] = Integer.toString(i);
//          }
//       }

//       if (_numSouth > 0) {
//          _ordinalsSouth = new String[_numSouth];
//          for (i = 0; i < _numSouth; i++) {
//             _ordinalsSouth[i] = Integer.toString(i);
//          }
//       }
      // Add the drop-down boxes.
      ports = _object.portList().iterator();
      i = 0;
      while (ports.hasNext()) {
            Object candidate = ports.next();
            if (candidate instanceof TypedIOPort) {
                TypedIOPort port = (TypedIOPort)candidate;
//                 ordinal = (StringAttribute)port.getAttribute("_ordinal");

//                 if (ordinal != null) {
//                     ordinalValue = ordinal.getExpression();
//                 } else {
//                     ordinalValue = Integer.toString(i);
//                 }
                cardinal = (StringAttribute)port.getAttribute("_cardinal");

                if (cardinal != null) {
                   cardinalValue = cardinal.getExpression().toUpperCase();
//                    if (cardinalValue.equals("WEST")) {
//                        addChoice("ordinal" + port.getName(),
//                                port.getName() + ": ordinal",
//                                _ordinalsWest, ordinalValue);
//                    } else if (cardinalValue.equals("EAST")) {
//                        addChoice("ordinal" + port.getName(),
//                                port.getName() + ": ordinal",
//                                _ordinalsEast, ordinalValue);
//                    } else if (cardinalValue.equals("NORTH")) {
//                       addChoice("ordinal" + port.getName(),
//                               port.getName() + ": ordinal",
//                               _ordinalsNorth, ordinalValue);
//                    } else {
//                       addChoice("ordinal" + port.getName(),
//                               port.getName() + ": ordinal",
//                               _ordinalsSouth, ordinalValue);
//                    }
                } else if (port.isInput() && !port.isOutput()) {
                   cardinalValue = "WEST";
//                    addChoice("ordinal" + port.getName(),
//                            port.getName() + ": ordinal",
//                            _ordinalsWest, ordinalValue);
                } else if (port.isOutput() && !port.isInput()) {
                   cardinalValue = "EAST";
//                    addChoice("ordinal" + port.getName(),
//                            port.getName() + ": ordinal",
//                            _ordinalsEast, ordinalValue);
                } else {
                   cardinalValue = "SOUTH";
//                    addChoice("ordinal" + port.getName(),
//                            port.getName() + ": ordinal",
//                            _ordinalsSouth, ordinalValue);
                }

                addChoice("cardinal" + port.getName(),
                        port.getName() + ": cardinal direction",
                        _cardinals, cardinalValue);

//                 ordinal = null;
//                 ordinalValue = null;
                cardinalValue = null;
                cardinal = null;
            }
            i++;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply the changes by configuring the ports that have changed.
     */
    public void apply() {
        StringBuffer moml = new StringBuffer("<group>");
        boolean foundOne = false;
        Iterator ports = _object.portList().iterator();
        NamedObj parent = null;
        while (ports.hasNext()) {
            Object candidate = ports.next();
            if (candidate instanceof TypedIOPort) {
                TypedIOPort port = (TypedIOPort)candidate;
                String name = port.getName();
                String nameOrdinal = "ordinal" + name;
                // If port has not changed, skip it.
                String nameCardinal = "cardinal" + name;
                if (!_changed.contains(nameOrdinal)
                        && !_changed.contains(nameCardinal)) continue;

                // The context for the MoML should be the first container
                // above this port in the hierarchy that defers its
                // MoML definition, or the immediate parent
                // if there is none.
                parent = MoMLChangeRequest.getDeferredToParent(port);
                if (parent == null) {
                    parent = (NamedObj)port.getContainer();
                }
                foundOne = true;
                moml.append("<port name=\"");
                moml.append(port.getName(parent));
                moml.append("\">");

                if (_changed.contains(nameCardinal)) {
                    // Type designation has changed.
                    String cardinalVal = stringValue(nameCardinal);
                    moml.append(
                            "<property name=\"_cardinal\" "
                            + "class = \"ptolemy.kernel.util.StringAttribute\" "
                            + "value = \""
                            + cardinalVal
                            + "\"/>");
                }
//                 if (_changed.contains(nameOrdinal)) {
//                     // Type designation has changed.
//                     String ordinalVal = stringValue(nameOrdinal);
//                     moml.append(
//                             "<property name=\"_ordinal\" "
//                             + "class = \"ptolemy.kernel.util.StringAttribute\" "
//                             + "value = \""
//                             + ordinalVal
//                             + "\"/>");
//                 }
                moml.append("</port>");
            }
        }

        if (foundOne) {
            moml.append("</group>");
            ChangeRequest request = new MoMLChangeRequest(
                    this,            // originator
                    parent,          // context
                    moml.toString(), // MoML code
                    null);           // base

            // NOTE: There is no need to listen for completion
            // or errors in this change request, since, in theory,
            // it will just work.  Will someone report the error
            // if one occurs?  I hope so...
            parent.requestChange(request);
        }
    }

    /** Called to notify that one of the entries has changed.
     *  This simply sets a flag that enables application of the change
     *  when the apply() method is called.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
//         String portName;
//         if (name.startsWith("cardinal")){
//            portName = name.substring(8);
//            try {
//               String oldCardinal = previousCachedStringValue(name);
//               String newCardinal = stringValue(name);
//               System.out.println("PortLocationConfigurer: changed("
//                       + name + ") oldCardinal: " + oldCardinal
//                       + " newCardinal: " + newCardinal);

//               //_updatePrevious(name, newCardinal);
//               if (oldCardinal.equals("WEST")) {
//                     _numWest--;
//                     _portsWest.remove(portName);
//                     _adjustOrdinals(_portsWest.iterator(), _numWest);
//               } else if (oldCardinal.equals("EAST")){
//                     _numEast--;
//                     _portsEast.remove(portName);
//                     _adjustOrdinals(_portsEast.iterator(), _numEast);
//               } else if (oldCardinal.equals("NORTH")){
//                     _numNorth--;
//                     _portsNorth.remove(portName);
//                     _adjustOrdinals(_portsNorth.iterator(), _numNorth);
//               } else {
//                     _numSouth--;
//                     _portsSouth.remove(portName);
//                     _adjustOrdinals(_portsSouth.iterator(), _numSouth);

//               }
//               if (newCardinal.equals("WEST")) {
//                     _numWest++;
//                     _portsWest.add(portName);
//                     _adjustOrdinals(_portsWest.iterator(), _numWest);
//               } else if (newCardinal.equals("EAST")) {
//                     _numEast++;
//                     _portsEast.add(portName);
//                     _adjustOrdinals(_portsEast.iterator(), _numEast);
//               } else if (newCardinal.equals("NORTH")) {
//                     _numNorth++;
//                     _portsNorth.add(portName);
//                     _adjustOrdinals(_portsNorth.iterator(), _numNorth);
//               } else {
//                     _numSouth++;
//                     _portsSouth.add(portName);
//                     _adjustOrdinals(_portsSouth.iterator(), _numSouth);
//              }
//            } catch (Exception e) {
//            }
//         } else if (name.startsWith("ordinal")){
//            portName = name.substring(7);
//            String oldOrdinal = previousCachedStringValue(name);
//            String newOrdinal = stringValue(name);
//            String cardinal = stringValue("cardinal" + portName);
//            //_updatePrevious(name , newOrdinal);
//         }

        _changed.add(name);
        apply();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*
     * Go through all of the ports on a side and adjust the choices in
     * the JComboBox.  If the count is greater than the number of
     * items, add items to the JComboBox.  If the count is less
     * than the number of items, then remove items from the JComboBox.
     *
     * @param ports An iterator over ports on a side.
     * @param count The number of ports on the side.
     */
//     private
//     void _adjustOrdinals(Iterator ports, int count) {
//        if (ports != null) {
//           while (ports.hasNext()) {
//              String name = (String)ports.next();
//              JComboBox combo = (JComboBox)
//                                getComponent("ordinal" + name);
//              int itemCount = combo.getItemCount();
//              int ordinal = Integer.parseInt((String)combo.getSelectedItem());
//              if (count > itemCount) {
//                 for (; itemCount < count; itemCount++) {
//                    combo.addItem(Integer.toString(itemCount));
//                 }
//              } else if (count < itemCount) {
//                 for (; count < itemCount && itemCount > 1; itemCount--) {
//                    combo.removeItemAt(itemCount-1);
//                 }
//              }
//              // If the selected item was the last item before we
//              // removed an item then make the new selected item
//              // be the last item
//              itemCount = combo.getItemCount();
//              if (ordinal >= (itemCount-1)) {
//                 combo.setSelectedItem(Integer.toString(itemCount-2));
//                 _changed.add("ordinal" + name);
//              }
//           }
//        }
//     }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The set of names of ports that have changed.
    private Set _changed = new HashSet();
    // Sets of ports, grouped by cardinal location
    private Set _portsWest = new HashSet();
    private Set _portsEast = new HashSet();
    private Set _portsNorth = new HashSet();
    private Set _portsSouth = new HashSet();

    // The object that this configurer configures.
    private Entity _object;

//     // The number of ports on the WEST
//     private int _numWest = 0;
//     // The number of ports on the EAST
//     private int _numEast = 0;
//     // The number of ports on the NORTH
//     private int _numNorth = 0;
//     // The number of ports on the SOUTH
//     private int _numSouth = 0;

    // The possible configurations for a port.
    private String[] _cardinals = {"NORTH", "SOUTH", "EAST", "WEST" };
//     private String[] _ordinalsWest;
//     private String[] _ordinalsEast;
//     private String[] _ordinalsNorth;
//     private String[] _ordinalsSouth;
}
