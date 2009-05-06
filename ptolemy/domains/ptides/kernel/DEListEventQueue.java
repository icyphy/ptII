package ptolemy.domains.ptides.kernel;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

public class DEListEventQueue implements DEEventQueue {
    
    /** Construct an empty event queue.
     */
    public DEListEventQueue () {
        // Construct a calendar queue _cQueue with its default parameters:
        // minBinCount is 2, binCountFactor is 2, and isAdaptive is true.
        _listQueue = new LinkedList();
    }
    
    public void clear() {
        _listQueue.clear();
    }

    public DEEvent get() throws InvalidStateException {
        DEEvent result = (DEEvent)_listQueue.getFirst();
        if (_debugging) {
            _debug("--- getting from queue: " + result);
        }
        return result;
    }
    
    public DEEvent get(int index) throws InvalidStateException {
        DEEvent result = (DEEvent)_listQueue.get(index);
        if (_debugging) {
            _debug("--- getting " + index + "th element from queue: " + result);
        }
        return result;
    }

    public boolean isEmpty() {
        return _listQueue.isEmpty();
    }

    public void put(DEEvent event) throws IllegalActionException {
        if (_debugging) {
            _debug("+++ putting in queue: " + event);
        }
        _listQueue.addFirst(event);
        Collections.sort(_listQueue);
    }

    public int size() {
        return _listQueue.size();
    }

    public DEEvent take() throws InvalidStateException {
        DEEvent result = (DEEvent)_listQueue.remove();
        if (_debugging) {
            _debug("--- taking from queue: " + result);
        }
        return result;
    }
    
    public DEEvent take(int index) throws InvalidStateException {
        DEEvent result = (DEEvent)_listQueue.remove(index);
        if (_debugging) {
            _debug("--- taking " + index + "th element from queue: " + result);
        }
        return result;
    }

    public Object[] toArray() {
        return _listQueue.toArray();
    }

    public void addDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            _debugListeners = new LinkedList();
        } else {
            if (_debugListeners.contains(listener)) {
                return;
            }
        }

        _debugListeners.add(listener);
        _debugging = true;
    }

    public void removeDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            return;
        }

        _debugListeners.remove(listener);

        if (_debugListeners.size() == 0) {
            _debugListeners = null;
            _debugging = false;
        }

        return;
    }
    
    
    /** Send a debug message to all debug listeners that have registered.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param message The message.
     */
    private final void _debug(String message) {
        if ((_debugListeners == null) || !_debugging) {
            return;
        } else {
            Iterator listeners = _debugListeners.iterator();

            while (listeners.hasNext()) {
                ((DebugListener) listeners.next()).message(message);
            }
        }
    }

    /** @serial The list of DebugListeners registered with this object. */
    private LinkedList _debugListeners = null;

    /** @serial A flag indicating whether there are debug listeners. */
    private boolean _debugging;
    
    private LinkedList _listQueue;
}
