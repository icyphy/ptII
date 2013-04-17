package ptolemy.domains.metroII.kernel;

import java.util.Hashtable;


public class EventDictionary {

    public EventDictionary() {
        // TODO Auto-generated constructor stub
    }

    public int getID(String name) {
        if (_eventName2ID.containsKey(name)) {
            return _eventName2ID.get(name);
        } else {
            return -1; 
        }
    }

    public void add(String name) {
        if (!_eventName2ID.containsKey(name)) {
            _eventName2ID.put(name, _nextAvailableID);
            _nextAvailableID++;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /** The next available event ID. If an new event is proposed, the
     *  _nextAvailableID is assigned to the new event and
     *  _nextAvailableID is increased by one.
     */
    private int _nextAvailableID = 0;

    /** The dictionary of event name and ID pair.
     *
     */
    private Hashtable<String, Integer> _eventName2ID = new Hashtable<String, Integer>();
}
