package ptolemy.math;

public interface SampleGenerator {

  /** Generate a sample of the waveform at a specified time.
   *  @param time The time at which to sample the waveform.
   *  @return A sample of the waveform.
   */
  public double sampleAt(double time);
}