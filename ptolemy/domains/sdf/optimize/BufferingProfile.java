package ptolemy.domains.sdf.optimize;

import ptolemy.kernel.util.IllegalActionException;

public interface BufferingProfile {
    /****
     * @return
     * returns the number of buffers required upon calling shared fire in excess of
     * the input and output buffer.
     */
    int sharedBuffers();
    
    /****
     * @return
     * returns the number of buffers required upon calling exclusive fire in excess of
     * the input and output buffer.
     */
    int exclusiveBuffers();

    /****
     * @return
     * returns the number of new buffers instantiated temporarily during copying fire.
     */
    int sharedExecutionTime();
    
    /****
     * @return
     * returns the net number of new buffers instantiated temporarily during exclusive fire.
     */
    int exclusiveExecutionTime();

    int iterate(int iterationCount, boolean fireExclusive) throws IllegalActionException;

}
