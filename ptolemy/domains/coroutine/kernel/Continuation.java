



package ptolemy.domains.coroutine.kernel;

import java.util.List;

import ptolemy.kernel.util.IllegalActionException;

public interface Continuation {

    public ControlExitToken controlEnter(ControlEntryToken entry) throws IllegalActionException;
    public ControlExitToken controlInit() throws IllegalActionException;
    public ControlExitToken controlResume() throws IllegalActionException;
    public ControlExitToken controlEnterAt(ControlEntryToken.EntryLocation location) throws IllegalActionException;

    /** Returns the set of exposed Entry Locations.
     *  Some internal entry locations may exist that are not exposed
     *  to the interface.
     */
    public List<ControlEntryToken.EntryLocation> entryLocations();

    /** Returns the set of exposed Exit Locations.
     *  Some internal exit locations may exist that are not exposed
     *  to the interface.
     *
     */
    public List<ControlExitToken.ExitLocation>   exitLocations();

}
