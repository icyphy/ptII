package java.util;

/**
 * Represents a long set of bits.
 */
public class BitSet
{
  byte[] iBytes;

  public BitSet (int nbits)
  {
    iBytes = new byte[(nbits - 1) / 8 + 1];
  }
  
  public void clear (int n)
  {
    int idx = n / 8;
    iBytes[idx] = (byte) ((iBytes[idx] & 0xFF) & (1 << (n%8)));
  }
  
  public void set (int n)
  {
    int idx = n / 8;
    iBytes[idx] = (byte) ((iBytes[idx] & 0xFF) | (1 << (n%8)));
  }
  
  public boolean get (int n)
  {
    return ((iBytes[n/8] & 0xFF) & (1 << (n%8))) != 0;
  }
}
