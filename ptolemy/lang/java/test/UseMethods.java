package ptolemy.lang.java.test;

public class UseMethods {

  public UseMethods(float f) {}

  public Object use(int dummy) {

     int r;
     Methods met;
     Object o;

     met = new Methods();

     met.m(dummy);

     met.m(dummy + 3, 'g');

     r = met.m("argh");

     r = met.m(this);

     o = Methods.yo();

     Methods.syo("ack" + "thp");

     String s[] = {"yuck"};

     r = met.h(s);

     return o;
  }


