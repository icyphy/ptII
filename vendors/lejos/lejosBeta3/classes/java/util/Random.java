package java.util;

/**
 * Pseudo-random number generation.
 */
public class Random
{
  private int iPrevSeed, iSeed;
  
  public Random (long seed)
  {
    iPrevSeed = 1;
    iSeed = (int) seed;
  }
  
  /**
   * @return A random positive or negative integer.
   */
  public int nextInt()
  {
    int pNewSeed = (iSeed * 48271) ^ iPrevSeed;
    iPrevSeed = iSeed;
    iSeed = pNewSeed;
    return pNewSeed;
  }
}
