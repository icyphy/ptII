package ptolemy.lang.java.test;

public class ArrayLength {

   public ArrayLength() {
       double[] h = new double[5];

       int len = h.length;

       Object o = h;
       Cloneable c = h;

       ugh(h);

       ack(c);

       o = h.clone();
       h.finalize();

       oof(h.length);
   }

   public int yo(double[] h) { return h.length; }
   public double ugh(Object g) { return 0.0; }

   public int ack(Cloneable c) { return 10; }

   public String oof(int len) { return len + "oof"; }

   public String oof(double r) { return r + "wrong"; }
