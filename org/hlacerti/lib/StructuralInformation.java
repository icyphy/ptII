/* This actor implements a subscriber in a HLA/CERTI federation.

@Copyright (c) 2013-2015 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package org.hlacerti.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;


/**
 * Utility class for storing all we need to know about a given class
 * Is used in the HlaManager in a HashMap
 * @author David Come
 */
public class StructuralInformation {
    ///////////////////////////////////////////////////////////////////
    ////                         Constructor                       ////
    public StructuralInformation() {
        freeActors = new LinkedList<ComponentEntity>();
        _relations = new HashMap<String,HashSet<IOPort>>();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         Public variables                  ////
    /*
    * current free actors for that class (ie instance of that class
    that have not been binded to an object instance from the federation).
    */
    public LinkedList<ComponentEntity> freeActors;

    /*
    The class to instanticate
    */
    public ComponentEntity classToInstantiate;

    ///////////////////////////////////////////////////////////////////
    ////                         Public functions                  ////

    /**
     * Retrieve all receiving port for the output port whose name is given
    */
    public HashSet<IOPort> getPortReceiver(String name) {
        return _relations.get(name);
    }

    /**
     * Add for the given all its receiving ports
    */
    public void addPortSinks(IOPort port) {
        if (!_relations.containsKey(port.getName())) {
            _relations.put(port.getName(), new HashSet<IOPort>());
        }
        _relations.get(port.getName()).addAll(port.sinkPortList());
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /*
    * For a given output port, all the inputs ports
    * that will receive a token from it. The key is the
    * output port's name.
    */
    private HashMap<String,HashSet<IOPort>> _relations;
}
