package ptolemy.lang.java.test;

public class UseFields {

  int ack() { return Fields.b; }

  int ugh(Fields f) { return f.a; }

  int ni() { return Double.NEGATIVE_INFINITY; }
}
