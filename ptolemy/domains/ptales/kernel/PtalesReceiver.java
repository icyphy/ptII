package ptolemy.domains.ptales.kernel;

import java.util.LinkedHashMap;
import java.util.Map;

import ptolemy.actor.IOPort;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;

public class PtalesReceiver extends SDFReceiver {

    public PtalesReceiver() {
        // TODO Auto-generated constructor stub
    }

    public PtalesReceiver(int size) {
        super(size);
        // TODO Auto-generated constructor stub
    }

    public PtalesReceiver(IOPort container) throws IllegalActionException {
        super(container);
        // TODO Auto-generated constructor stub
    }

    public PtalesReceiver(IOPort container, int size)
            throws IllegalActionException {
        super(container, size);
        // TODO Auto-generated constructor stub
    }

    /** Specify the pattern in which data is read from the receiver.
     *  A side effect of this method is to set the capacity of the receiver.
     *  @param readSpec Number of tokens read per firing by dimension.
     *  @param firingCounts Firing counts by dimension.
     * @throws IllegalActionException If setting the capacity fails.
     */
    public void setReadPattern(
            LinkedHashMap<String,Integer> readSpec, 
            Map<String,Integer> firingCounts) 
            throws IllegalActionException {
        // FIXME: For now, just set an overall capacity.
        int blockSize = 1;
        if (readSpec != null) {
            for (String dimension : readSpec.keySet()) {
                Integer dimensionSpec = readSpec.get(dimension);
                if (dimensionSpec != null) {
                    blockSize *= dimensionSpec.intValue();
                }
            }
        }
        int iterationCount = 1;
        for (String dimension : firingCounts.keySet()) {
            Integer count = firingCounts.get(dimension);
            // This should never be null, but just in case, check...
            if (count != null) {
                iterationCount *= count.intValue();
            }
        }
        int capacity = iterationCount * blockSize;
        setCapacity(capacity);
    }
    
    /** Specify the pattern in which data is written to the receiver.
     *  @param writeSpec Number of tokens written per firing by dimension.
     */
    public void setWritePattern(LinkedHashMap<String,Integer> writeSpec) {
    }
}
