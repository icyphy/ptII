/* 

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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.od.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// ODDirector 
/** 

@author John S. Davis II
@version @(#)ODDirector.java	1.3	11/16/98
*/
public class ODDirector extends ProcessDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ODDirector() {
        super();
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public ODDirector(String name) {
        super(name);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public ODDirector(Workspace workspace, String name) {
        super(workspace, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
    }
    
    /** 
     */
    public synchronized void addReadBlock() {
        _readBlocks++;
    }
    
    /** 
     */
    public synchronized void addWriteBlock() {
        _writeBlocks++;
    }

    /** Execute all deeply contained actors of the container of this
     *  ODDirector and resolve deadlocked actors. 
     *
     * @exception IllegalActionException If any called method of the
     *  container or one of the deeply contained actors throws it.
     */
    public void fire() throws IllegalActionException {
        while( !isDeadlocked() && !hasMutation() ) {
            workspace().wait(this);
            if( isDeadlocked() ) {
                resolveDeadlock();
                if( isDeadlocked() ) {
                    return;
                }
            }
            if( hasMutation() ) {
                // try {
                    _performMutations();
                    if( hasMutation() ) {
                        return;
                    }
                    /* 
                } catch( NameDuplicationException e ) {
                    System.err.println("NameDuplicationException occurring "
                    + "because of mutation.");
                }
                */
            }
        }
    }
    
    /** FIXME
     */
    public boolean hasMutation() {
        boolean dummy = false;
        return dummy;
    }
    
    /** FIXME
     */
    public boolean isDeadlocked() {
        if( _actorsActive == _readBlocks + _writeBlocks ) {
            return true;
        }
        return false;
    }
    
    /** Return a new receiver of a type compatible with this director.
     *  @return A new ODReceiver.
     */
    public Receiver newReceiver() {
        return new ODReceiver();
    }

    /** 
     */
    public synchronized void removeReadBlock() {
        if( _readBlocks > 0 ) {
            _readBlocks--;
        }
    }
    
    /** 
     */
    public synchronized void removeWriteBlock() {
        if( _writeBlocks > 0 ) {
            _writeBlocks--;
        }
    }
    
    /** FIXME
     */
    public void resolveDeadlock() {
        ;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////
    
    protected void _performMutations() {
        ;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    private int _readBlocks = 0;
    private int _writeBlocks = 0;

}















