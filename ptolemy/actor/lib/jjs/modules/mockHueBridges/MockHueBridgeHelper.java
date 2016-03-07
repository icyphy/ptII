/* Helper for the mockHueBridges JavaScript module.

   Copyright (c) 2014-2016 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.mockHueBridges;

import java.util.HashMap;
import java.util.HashSet;

/** Helper for the mockHueBridges JavaScript module. 
 * 
 *  This helper is a singleton class that holds state information for the mock
 *  bridge.  Using a singleton allows multiple MockHueBridge accessors to 
 *  interact with the same mock bridge.  Note that implementing a singleton 
 *  module in Javascript instead of Java is not possible with the current 
 *  implementation of the Ptolemy JavaScript actor, which creates a separate 
 *  script engine for each JavaScript actor for security purposes.  This helper
 *  supports multiple bridges, if desired.
 *
 *  @author Elizabeth Osyk
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *
 */
public class MockHueBridgeHelper {
    
    /** Construct a new MockHueBridgeHelper.  The constructor is private so that
     * this class can ensure that only one instance is created.
     */
    private MockHueBridgeHelper() {
        _bridges = new HashMap();
    }
     
     ///////////////////////////////////////////////////////////////////
     ////                     public methods                       ////
     
     /** Add a new entry to the table of available bridges.  If the bridgeID
      * already exists, do nothing.
      * @param bridgeID  The ID of the new bridge.
      */
     public void addBridge(String bridgeID) {
         if (! _bridges.containsKey(bridgeID)) {
             _bridges.put(bridgeID, new BridgeInfo());
         }
     }
     
     /** Add a new username to the set of authorized usernames for the
      *  specified bridge.  If there is no entry for this bridgeID, create one.
      *  Any username is accepted.
      *  @param bridgeID  The bridge identifier.
      *  @param username  The username to add to the list of authorized users.
      */
     public void addUsername(String bridgeID, String username) {
         if (! _bridges.containsKey(bridgeID)) {
             _bridges.put(bridgeID, new BridgeInfo());
         }
         
         _bridges.get(bridgeID).addUsername(username);
     }
     
     
     /** Clear the list of authorized users for the specified bridge.
      * @param bridgeID  The bridge identifier.
      */
     public void clearUsernames(String bridgeID) {
         if (_bridges.containsKey(bridgeID)) {
             _bridges.get(bridgeID).clearUsernames();
         }
     }
     
     /** Get the list of all bridge identifiers.
      * @return  A list of all bridge identifiers.
      */
     public String[] getBridgeIDs() {
         return _bridges.keySet().toArray(new String[_bridges.keySet().size()]);
     }
     
     /** Get the MockHueBridge instance.  Returns the existing instance if 
      * present; otherwise, creates a new instance.
      * @return  The MockHueBridge instance.
      */
      public static MockHueBridgeHelper getInstance() {
          if(_instance == null) {
             _instance = new MockHueBridgeHelper();
          }
          return _instance;
       }
     
     /** Return the state of the specified bridge, or an empty string if the 
      * bridge is not present.  This helper class serves as a data store for the 
      * state, but does not modify the state.  The state is stored as a string 
      * for easy passing between Java and Javascript. 
      * Please see the Hue API for an example of bridge state:
      * http://www.developers.meethue.com/philips-hue-api
      * @param bridgeID  The bridge identifier.
      * @return  The state of the specified bridge, as a string, or an empty 
      * string if the bridge is not present.
      * @see #setState(String, String)
      */
     public String getState(String bridgeID) {
         if (_bridges.containsKey(bridgeID)) {
             return _bridges.get(bridgeID).getState();
     
         } else {
             return "";
         }
     }
     
     /** Get the transition time for the specified bridge.  In a real Hue bridge,
      * the transition time is the delay between receipt of a change request and 
      * the time at which execution of the request is complete.  
      * Note that transition time is not currently implemented in the mock 
      * bridge.  All requests take effect immediately.
      * @param bridgeID  The bridge identifier.
      * @return  The transition time.
      * @see @setTransitionTime(String, int)
      */
     public int getTransitionTime(String bridgeID) {
         if (_bridges.containsKey(bridgeID)) {
             return _bridges.get(bridgeID).getTransitionTime();
         } else {
             return 0;
         }
     }

     /** Get the authorized usernames for the given bridgeID.  If there is no
      * entry for this bridgeID, return an empty array.  Return a string array
      * for easier handling in Javascript.
      * @param bridgeID  The bridge identifier.
      * @return  An array of authorized usernames for this bridge, or an empty
      * array if there is no bridge matching the bridgeID.
      */
     public String[] getUsernames(String bridgeID) {
         if (_bridges.containsKey(bridgeID)) {
             HashSet names = _bridges.get(bridgeID).getUsernames();
             return (String[]) names.toArray(new String[names.size()]);
         } else {
             return new String[0];
         }
     }    
     
     /** Return true if the given bridge exists; false otherwise.
      * @param bridgeID The bridge identifier.
      * @return  True if the given bridge exists; false otherwise.
      */
     public boolean hasBridge(String bridgeID) {
         if (_bridges.containsKey(bridgeID)) {
             return true;
         }      
         return false;
     }
     
     /** Remove the given username from the list of authorized users for the 
      * given bridge.
      * @param bridgeID  The bridge identifier.
      * @param username  The username to remove.
      */
     public void removeUsername(String bridgeID, String username) {
         if (_bridges.containsKey(bridgeID)) {
             _bridges.get(bridgeID).removeUsername(username);
         }
     }
     
     /** Set the state of the specified bridge.  If the bridge does not exist,
      * create it, then set the state.  This helper class serves as a data store 
      * for the state, but does not modify the state.  The state is stored as a 
      * string for easy passing between Java and Javascript. 
      * Please see the Hue API for an example of bridge state:
      * http://www.developers.meethue.com/philips-hue-api
      * @param bridgeID  The bridge identifier.
      * @param state  The new state of the bridge.
      * @see #getState(String)
      */
     public void setState(String bridgeID, String state) {
         if (!_bridges.containsKey(bridgeID)){
             addBridge(bridgeID);
         }
         
         _bridges.get(bridgeID).setState(state);
     }
     
     /** Set the transition time for the given bridgeID.  Do nothing if this
      * bridge does not exist. 
      * Note that transition time is not currently implemented in the mock 
      * bridge.  All requests take effect immediately.
      * @param bridgeID  The bridge identifier.
      * @param transitionTime  The transition time between receipt of a request 
      * and completion of request execution.
      * @see #getTransitionTime(String)
      */
     public void setTransitionTime(String bridgeID, int transitionTime) {
         if (_bridges.containsKey(bridgeID)) {
             _bridges.get(bridgeID).setTransitionTime(transitionTime);
         }
     }
     
    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
     
     /** A map of bridge IDs to usernames that have access to this bridge.
      */
     private static HashMap<String, BridgeInfo> _bridges;
     
    /** The helper instance.  All mockHueBridge accessors share this
     * instance.   
     */
    private static MockHueBridgeHelper _instance = null;
    
    ///////////////////////////////////////////////////////////////////
    ////                     inner classes                         ////
    
    /** An inner class to hold information for a bridge. */
    private static class BridgeInfo {
        // FindBugs says this can be static.
        
        /** Construct a new BridgeInfo object, holding the bridge state and
         * set of authorized usernames.
         */
        public BridgeInfo() {
            _state = "";
            _usernames = new HashSet();
        }
        
        ///////////////////////////////////////////////////////////////////
        ////                     public methods                        ////
        
        /** Add a username to the set of authorized users. Any name is accepted.
         * @param username  The username to add to the list of authorized users.
         */
        public void addUsername(String username) {
            _usernames.add(username);  // Duplicates ignored by Sets
        }
        
        /** Clear the set of authorized users.
         */
        public void clearUsernames() {
            _usernames.clear();
        }
        
        /** Get the state of the bridge, as a string. 
         * Please see the Hue API for an example of bridge state:
         * http://www.developers.meethue.com/philips-hue-api 
         * @return  The state of the bridge.
         */
        public String getState() {
            return _state;
        }
        
        /** Get the transition time, which is the time between receipt of a 
         * request and completed execution of that request.  Transition time is
         * not currently implemented.
         * @return  The transition time.
         */
        public int getTransitionTime() {
            return _transitionTime;
        }
        
        /** Get the set of authorized usernames.
         * @return  The set of authorized userames.
         */
        public HashSet<String> getUsernames() {
            return _usernames;
        }
        
        /** Remove the given username from the set of authorized users.
         * @param username  The username to remove from the set of authorized
         * users.
         */
        public void removeUsername(String username) {
            _usernames.remove(username);
        }

        /** Set the state of the bridge.
         * Please see the Hue API for an example of bridge state:
         * http://www.developers.meethue.com/philips-hue-api 
         * @param state  The new bridge state.
         */
        public void setState(String state) {
            _state = state;
        }
        
        /** Set the transition time, which is the time between receipt of a 
         * request and completed execution of that request.  Transition time is
         * not currently implemented.
         * @param transitionTime  The transition time.
         */
        public void setTransitionTime(int transitionTime) {
            _transitionTime = transitionTime;
        }

        ///////////////////////////////////////////////////////////////////
        ////                     private variables                     ////

        /** The state of the bridge.  Please see the Hue API for an example:
         * http://www.developers.meethue.com/philips-hue-api 
         */
        private String _state;
        
        /** The transition time, which is the time between receipt of a 
         * request and completed execution of that request.  Not currently used.
         */
        private int _transitionTime;
        
        /** The set of authorized usernames for the bridge.  
         */
        private HashSet<String> _usernames;
    }
}
