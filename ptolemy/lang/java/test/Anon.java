package ptolemy.lang.java.test;

class Anon {
   public Anon() {
      Anon a = new Anon() {

         public int ack() { return b; }

         public int ugh() { return c + 3; }

         protected int bar(int g) { return g / foo(); }

         protected int b = 2;
      };
      a.ugh();
   }

   public int ugh() { return 3; }
   public int foo() { return 9; }
   protected int c = 7;
}
