/* A Scratchpad Memory simulator

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

@ProposedRating Red (sanjeev@eecs.berkeley.edu)
@AcceptedRating Red (sanjeev@eecs.berkeley.edu)
*/

package ptolemy.apps.cacheAwareScheduler.kernel;

// Ptolemy imports
import ptolemy.kernel.util.IllegalActionException;

// JAVA Imports
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

///////////////////////////////////////////////////////////////////////////
//// ScratchpadMemory
/**
   This class simulates a scratchpad memory of a given size.

   @author Sanjeev Kohli
   @version $Id$
   @since Ptolemy II 2.0

 */

public class ScratchpadMemory {

    /* Default constructor. Construct a scratchpad memory with zero size.
     */
    public ScratchpadMemory() {
        _totalSpace = 0;
        _usedSpace = 0;
        _scratchpadContents = new ArrayList();
    }

    /** Construct a scratchpad memory of size defined by the parameter.
     *
     *  @param size The size of scratchpad memory.
     *  @exception IllegalActionException If the passed parameter is negative
     *   or zero.
     */
    public ScratchpadMemory(int memorySize) throws IllegalActionException{
        if(memorySize < 0) throw new IllegalActionException("Can't define" 
                + " a negative scratchpad memory size.");
        else if(memorySize == 0) throw new IllegalActionException("Can't"
                + " define a scratchpad memory of zero size.");
        // Initialize _totalSpace, _usedSpace
        _totalSpace = memorySize;
        _usedSpace = 0;
        _scratchpadContents = new ArrayList(memorySize);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////
  
    /** Adds a new memory address to the scratchpad memory.
     *  
     *  @param address The address to be cached in the scratchpad memory.
     *  @exception IllegalActionException If the scratchpad memory is full.
     */
    public void add(Object address) throws IllegalActionException{
        if(address == null) {
            throw new IllegalArgumentException("Attempt to insert a null"
                    + " element");
        }
        else if((_totalSpace - _usedSpace) > 0 ) {
            _scratchpadContents.add(address);
            _usedSpace++;
        }
        else throw new IllegalActionException("Can't add another memory" 
                + " address, the scratchpad memory is full.");
    }

    /** Clear all of the contents of the scratchpad memory.
     */
    public void clear() {
        _scratchpadContents.clear();
        _usedSpace = 0;
    }

    /** Checks whether a memory address is cached or not in the scratchpad
     *  memory.
     *  
     *  @param address The address of the memory location to be searched.
     *  @return True if the address is present in the scrtachpad memory, 
     *   else False.
     */
    public boolean contains(Object address) {
        return _scratchpadContents.contains(address);
    }

    /** Evicts a memory location from the scratchpad memory.
     *
     *  @param address The memory location to be evicted.
     *  @exception IllegalActionException If the specified memory address isn't
     *   present in the scratchpad memory.
     */
    public void evict(Object address) throws IllegalActionException{
        if(_scratchpadContents.contains(address)) {
            int tempVar = _scratchpadContents.indexOf(address);
            _scratchpadContents.remove(tempVar);
            _usedSpace--;
        }
        else throw new IllegalActionException("Attempt made to evict a non"
                + " existent memory address.");
    }

    /** Evicts all memory locations from the scratchpad memory. Clears it.
     */
    public void evictAll() {
        clear();
    }

    /** Returns the size of the free space available in the scratchpad memory.
     *
     *  @return The size of the free space available in the scratchpad memory.
     */
    public int freeSpace() {
        return (_totalSpace - _usedSpace);
    }
    
    /** Checks if the scratchpad memory is full.
     *
     *  @return True if the scratchpad memory is full else False. 
     */
    public boolean isFull() throws IllegalActionException {
        if(_usedSpace < _totalSpace) return false;
        else if (_usedSpace == _totalSpace) return true;
        // The control should never go to this else loop. If it does, it means
        // there is something wrong with the add/remove code.
        else throw new IllegalActionException("The used space in scratchpad"
                + " memory has acceded the total available space");
    }

   /** Removes a memory location from the scratchpad memory.
     *
     *  @param address The memory location to be evicted.
     *  @exception IllegalActionException If the specified memory address isn't
     *   present in the scratchpad memory.
     */
    public void remove(Object address) throws IllegalActionException{
        evict(address);
    }


    /** Removes all memory locations from the scratchpad memory. Clears it.
     */
    public void removeAll() {
        clear();
    }

    /** Returns the size of the scratchpad memory.
     *
     *  @return The size of the scratchpad memory.
     */
    public int size() {
        return _totalSpace;
    }

    /** Returns the size of the used space in the scratchpad memory.
     *
     *  @return The size of the used space in the scratchpad memory.
     */
    public int usedSpace() {
        return _usedSpace;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////
    
    // The scratchpad memory space. The addresses present in scratchpad
    // are stored in this array.
    private ArrayList _scratchpadContents;
    // Total space (or size) in the scratchpad memory. 
    private int _totalSpace;
    // The Occupied size of scratchpad memory.
    private int _usedSpace;
}
