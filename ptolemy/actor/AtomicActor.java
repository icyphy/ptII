/* One line description of file.

 Copyright (c) 1998 The Regents of the University of California.
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

package pt.actors;
import pt.kernel.*;
import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// AtomicActor
/** 
Description of the class
@author Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class AtomicActor extends Actor{
    /** Constructor
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public AtomicActor(CompositeActor container, String name) 
           throws NameDuplicationException {
        super(container, name);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    
//     public NamedList deepSuccessorList() {
//         synchronized(workspace()) {
//             if(_deepSuccVersion == workspace().getVersion()) {
//                 return _deepSuccessors;
//             }
//             NamedList successorList = new NamedList();
//             Enumeration outputs = outputPorts();
//             while(outputs.hasMoreElements()) {
//                 Enumeration deepConnectedInports = 
//                     ((AtomicIOPort)outputs.nextElement()).deepConnectedInputPorts();
//                 while(deepConnectedInports.hasMoreElements()) {
//                     AtomicIOPort p = (AtomicIOPort)deepConnectedInports.nextElement();
//                     Actor succ = (Actor) p.getContainer();
//                     if(!successorList.includes(succ)) {
//                         try {
//                             successorList.append(succ);
//                         } catch (KernelException e) {}
//                     }
//                 }
//             }
//             _deepSuccessors = successorList;
//             _deepSuccVersion = workspace().getVersion();
//             return _deepSuccessors;
//         }
//     }
//     /** get an list of successor (ComponentEntity)
//      * keep uniqueness. identity entity appears only once in the 
//      * enumeration. The reason of returning a NamedList is that 
//      * both the enumeration and the size can be get easily.
//      */
//     public Enumeration deepSuccessors() {
//             return deepSuccessorList().getElements();
//     }
// 
// 
//     /** get an list of predecessors (ComponentEntity)
//      * keep uniqueness. identity entity appears only once in the 
//      * enumeration. The reason of returning a NamedList is that 
//      * both the enumeration and the size can be get easily.
//      */
//     public NamedList deepPredecessorList() {
//         synchronized(workspace()) {
//             if(_deepPredVersion == workspace().getVersion()) {
//                 return _deepPredecessors;
//             }
//             NamedList predecessorList = new NamedList();
//             Enumeration inputs = inputPorts();
//             while(inputs.hasMoreElements()) {
//                 Enumeration deepConnectedOutports = 
//                     ((AtomicIOPort)inputs.nextElement()).deepConnectedOutputPorts();
//                 while(deepConnectedOutports.hasMoreElements()) {
//                     AtomicIOPort p = (AtomicIOPort)deepConnectedOutports.nextElement();
//                     Actor pred =
//                         (Actor)p.getContainer();
//                     if(!predecessorList.includes(pred)) {
//                         try {
//                             predecessorList.append(pred);
//                         } catch (KernelException e) {}
//                     }
//                 }
//             }
//             _deepPredecessors = predecessorList;
//             _deepPredVersion = workspace().getVersion();
//             return _deepPredecessors;
//         }
//     }
//     
//     public Enumeration deepPredecessors() {
//         return deepPredecessorList().getElements();
//     }

    /** get an enumeration of the input ports
     */ 
    public Enumeration inputPorts() {
        synchronized(workspace()) {
            if(_inputPortsVersion == workspace().getVersion()) {
                return _cachedInputPorts.getElements();
            }
            NamedList inports = new NamedList();
            Enumeration ports = getPorts();
            while(ports.hasMoreElements()) {
                AtomicIOPort p = (AtomicIOPort)ports.nextElement();
                if( p.isInput()) {
                    try {
                        inports.append(p);
                    }catch(KernelException e) {}
                }
            }
            _cachedInputPorts = inports;
            _inputPortsVersion = workspace().getVersion();
            return _cachedInputPorts.getElements();
        }
    }

    /** get an enumeration of the output ports
     */
    public Enumeration outputPorts() {
        synchronized(workspace()) {
            if(_outputPortsVersion == workspace().getVersion()) {
                return _cachedOutputPorts.getElements();
            }
            NamedList outports = new NamedList();
            Enumeration ports = getPorts();
            while(ports.hasMoreElements()) {
                AtomicIOPort p = (AtomicIOPort)ports.nextElement();
                if( p.isOutput()) { 
                    try {
                        outports.append(p);
                    }catch (KernelException e) {}
                }
            }
            _cachedOutputPorts = outports;
            _outputPortsVersion = workspace().getVersion();
            return _cachedOutputPorts.getElements();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Description
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    protected int _AProtectedMethod() {
        return 1;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** Description */
    protected int _aprotectedvariable;

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    // Private methods should not have doc comments, they should
    // have regular C++ comments.
    private int _APrivateMethod() {
        return 1;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private long _inputPortsVersion = -1;
    private transient NamedList _cachedInputPorts;
    private long _outputPortsVersion = -1;
    private transient NamedList _cachedOutputPorts;
    private long _deepSuccVersion = -1;
    private transient NamedList _deepSuccessors;
    private long _deepPredVersion = -1;
    private transient NamedList _deepPredecessors;
}
