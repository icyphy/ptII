package edu.twsu.cs.extensions.poly;

public class EPolyClassLoader extends ClassNotFoundException
{
  Exception iWrapped;

  public EPolyClassLoader (Exception aE)
  {
    iWrapped = aE;
  }

  public String toString()
  {
    return iWrapped.toString();
  }
}
      



