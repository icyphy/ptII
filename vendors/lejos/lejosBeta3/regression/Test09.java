
// Class-dependent language features

public class Test09
{
  int x;
  static String y;

  public static void main (String[] aArg)
  {
    y = new Object() + "abc" + ((float) 4.5) + "" + ((double) 3.2) +
               56L + "" + 22 + "" + 'c' + "" + ((byte) 5) + "" +
               true + "" + ((short) 5);
    Object c = Test09.class;
  }
}

