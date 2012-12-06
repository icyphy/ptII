package ptolemy.domains.metroII.kernel;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;

public interface MetroIIEventHandler {
    public YieldAdapterIterable<Iterable<Event.Builder>> adapter();

    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException;

}
