package ptolemy.domains.metroII.kernel;

import java.util.Iterator;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;

public class MetroIICompositeActor extends TypedCompositeActor implements
        MetroIIEventHandler {

    public MetroIICompositeActor() {
        // TODO Auto-generated constructor stub
    }

    public MetroIICompositeActor(Workspace workspace) {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    public MetroIICompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    public YieldAdapterIterable<Iterable<Event.Builder>> adapter() {
        return new ThreadedYieldAdapter<Iterable<Event.Builder>>()
                .adapt(new Collector<Iterable<Event.Builder>>() {
                    public void collect(
                            ResultHandler<Iterable<Event.Builder>> resultHandler)
                            throws CollectionAbortedException {
                        getfire(resultHandler);
                    }
                });
    }

    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {
        try {

            if (_debugging) {
                _debug("Calling fire()");
            }

            try {
                _workspace.getReadAccess();

                // First invoke piggybacked methods.
                //                    if (_piggybacks != null) {
                //                        // Invoke the fire() method of each piggyback.
                //                        for (Executable piggyback : _piggybacks) {
                //                            piggyback.fire();
                //                        }
                //                    }
                //                    if (_derivedPiggybacks != null) {
                //                        // Invoke the fire() method of each piggyback.
                //                        for (Executable piggyback : _derivedPiggybacks) {
                //                            piggyback.fire();
                //                        }
                //                    }

                if (!isOpaque()) {
                    throw new IllegalActionException(this,
                            "Cannot fire a non-opaque actor.");
                }

                // Need to read from port parameters
                // first because in some domains (e.g. SDF)
                // the behavior of the schedule might depend on rate variables
                // set from ParameterPorts.
                for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                        .hasNext() && !_stopRequested;) {
                    IOPort p = (IOPort) inputPorts.next();

                    if (p instanceof ParameterPort) {
                        ((ParameterPort) p).getParameter().update();
                    }
                }

                Director _director = getDirector();
                // Use the local director to transfer inputs from
                // everything that is not a port parameter.
                // The director will also update the schedule in
                // the process, if necessary.
                for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                        .hasNext() && !_stopRequested;) {
                    IOPort p = (IOPort) inputPorts.next();

                    if (!(p instanceof ParameterPort)) {
                        _director.transferInputs(p);
                    }
                }

                if (_stopRequested) {
                    return;
                }

                // _director.fire();
                if (_director instanceof MetroIIEventHandler) {
                    ((MetroIIEventHandler) _director).getfire(resultHandler);
                    //                                  final Iterable<Hashtable> results = ((M2EventHandler) _director).adapter();
                    //                                  for (Hashtable result : results) {
                    //                                      // assertFalse(resultsCheck.contains(result)); // no duplicate results
                    //                                      // assertEquals(SOURCE_WORD.length(), result.length());
                    //                                      
                    //                                      resultHandler.handleResult(result); 
                    //                                  }
                } else {
                    _director.fire();
                }

                if (_stopRequested) {
                    return;
                }

                // Use the local director to transfer outputs.
                Iterator<?> outports = outputPortList().iterator();

                while (outports.hasNext() && !_stopRequested) {
                    IOPort p = (IOPort) outports.next();
                    _director.transferOutputs(p);
                }
            } finally {
                _workspace.doneReading();
            }

            if (_debugging) {
                _debug("Called fire()");
            }
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
