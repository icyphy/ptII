/*

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.actor.gt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.rules.PortRule;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// AtomicActorMatcher

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AtomicActorMatcher extends TypedAtomicActor {

    public AtomicActorMatcher(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        ruleList = new RuleListAttribute(this, "ruleList");

        ruleList.setExpression("");
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"60\" height=\"30\" "
                + "style=\"fill:#C0C0C0\"/>\n" + "<text x=\"-18\" y=\"5\""
                + "style=\"font-size:14; fill:blue; font-family:SansSerif\">"
                + "Match</text>\n" + "</svg>\n");
    }

    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        super.attributeChanged(attribute);
        
        if (attribute == ruleList) {
            try {
                _workspace.getWriteAccess();
                
                Set<String> preservedPortNames = new HashSet<String>();
                for (Rule rule : ruleList.getRuleList()) {
                    if (rule instanceof PortRule) {
                        PortRule portRule = (PortRule) rule;
                        String portName = portRule.getPortName();
                        preservedPortNames.add(portName);

                        TypedIOPort port = (TypedIOPort) getPort(portName);
                        if (port != null) {
                            // Change the port instead of deleting it later.
                            port.setInput(portRule.isInput());
                            port.setOutput(portRule.isOutput());
                            port.setMultiport(portRule.isMultiport());
                            port.setPersistent(false);
                        } else {
                            port = new TypedIOPort(this, portRule.getPortName(),
                                    portRule.isInput(), portRule.isOutput());
                            port.setMultiport(portRule.isMultiport());
                            port.setPersistent(false);
                        }
                    }
                }
                List<?> portList = portList();
                for (int i = 0; i < portList.size();) {
                    Port port = (Port) portList.get(i);
                    if (!preservedPortNames.contains(port.getName())) {
                        port.setContainer(null);
                    } else {
                        i++;
                    }
                }
                for (Object portObject : portList()) {
                    Port port = (Port) portObject;
                    if (!preservedPortNames.contains(port.getName())) {
                        port.setContainer(null);
                    }
                }
            } catch (MalformedStringException e) {
                throw new IllegalActionException(null, e,
                        "ruleList attribute is malformed.");
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(null, e,
                        "Name duplicated.");
            } finally {
                _workspace.doneWriting();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    public RuleListAttribute ruleList;

    private static final long serialVersionUID = 8775143612221394893L;
}
