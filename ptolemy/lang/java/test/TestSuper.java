package ptolemy.lang.java.test;

public class TestSuper {

  public TestSuper() { i = 3; }
  
  public TestSuper(int _i) { i = _i; }

  public int i;
  
  public String stupor(int j) { return "stupor"; }
 
  public static double myIQ(String name) { return 4.0; }
    
}
  
public class TestChild extends TestSuper {

  public TestChild() {}
  
  public TestChild(int _i) {
      super(_i);
  }
  
  public String stupor(int j) { return "stuporkid"; }
 
  public void test() {      
      stupor(8);      
  }
 
}

public class TestAll {
  public static void test() {
      TestChild tc = new TestChild(7);      
      tc.stupor(5);
      
      tc.i = 6;
      TestChild.myIQ("jeff");
  }     
}