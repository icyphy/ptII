
/* An FIR filter example for Java to C translation.
   @author Shuvra S. Bhattacharyya
   @version $Id$
   @since Ptolemy II 2.0

   This example is adapted from the FIR actor implementation in the
   Ptolemy II SDF domain. It is convenient for translation to C because
   it does not reference any other classes. Thus, it is suitable for
   compilation using the 'singleclass' mode of JavaToC.
*/
public class FIRSingle {
    /** Perform an invocation of the FIR filter that operates on given input
     *  and output arrays.
     *  @param input The input array.
     *  @param output The output array.
     */
    public void fire(float input[], float output[]) {
        // Pointers into the input and output buffers
        int inputIndex = 0;
        int outputIndex = 0;

        // Phase keeps track of which phase of the filter coefficients
        // are used. Starting phase depends on the _decimationPhase value.
        int phase = _decimation - _decimationPhase - 1;

        // Transfer _decimation inputs to _data[]
        for (int inC = 1; inC <= _decimation; inC++) {
            if (--_mostRecent < 0) {
                _mostRecent = _dataLength - 1;
            }
            _data[_mostRecent] = input[inputIndex++];
        }

        // Interpolate once for each input consumed
        for (int inC = 1; inC <= _decimation; inC++) {
            // Produce however many outputs are required
            // for each input consumed
            while (phase < _interpolation) {
                float outToken = 0;

                // Compute the inner product.
                for (int i = 0; i < _phaseLength; i++) {
                    int tapsIndex = i * _interpolation+ phase;

                    int dataIndex = (_mostRecent + _decimation -
                            inC + i)%(_dataLength);

                    if (tapsIndex < _numberOfTaps) {
                        float _tapItem = _taps[tapsIndex];
                        float _dataItem = _data[dataIndex];
                        _dataItem = _tapItem  * _dataItem;
                        outToken = outToken += _dataItem;
                    }
                    // else assume tap is zero, so do nothing.
                }
                output[outputIndex++] = outToken;
                phase += _decimation;
            }
            phase -= _interpolation;
        }
    }

    /** Configure the FIR filter with a specified number of taps, tap
     *  values, delay line storage, interpolation value, decimation value,
     *  and decimation phase.
     *  @param numberOfTaps The number of taps.
     *  @param taps The tap values.
     *  @param data The storage to use for the delay line (this is needed
     *  due to the limited support for arrays in the current version of
     *  the C code generator).
     *  @param interpolation The interpolation value.
     *  @param decimation The decimation value.
     *  @param decimationPhase The decimation phase.
     *
     */
    public void initialize(float taps[], int numberOfTaps, float data[],
            int interpolation, int decimation, int decimationPhase) {
        /* Copy the arguments */
        _taps = taps;
        _numberOfTaps = numberOfTaps;
        _data = data;
        _interpolation = interpolation;
        _decimation = decimation;
        _decimationPhase = decimationPhase;
        _mostRecent = 0;

        _phaseLength = (int)(numberOfTaps / _interpolation);
        if ((numberOfTaps % _interpolation) != 0) {
            _phaseLength++;
        }
        for (int i = 0; i < _phaseLength; i++ ) {
            _data[i] = 0;
        }
        _dataLength = _phaseLength;
    }

    // Protected variables.

    /** Length of the delay line. */
    protected int _dataLength;

    /** Decimation phase. */
    protected int _decimationPhase;

    /** Decimation value. */
    protected int _decimation;

    /** Interpolation value. */
    protected int _interpolation;

    /** The phaseLength is ceiling(length/interpolation), where
     *  length is the number of taps.
     */
    protected int _phaseLength;

    /** The delay line. */
    protected float _data[];

    /** The index into the delay line of the most recent input. */
    protected int _mostRecent;

    /** The number of filter coefficients */
    protected int _numberOfTaps;

    /** The filter coefficients */
    protected float _taps[];
}
