package ptolemy.lang.java.test;

class ExceptionTest {
  public ExceptionTest() {
    Exception e = new RuntimeException();
    e = new RuntimeException("yo");
    
    throw new RuntimeException("argh");    
  }

}