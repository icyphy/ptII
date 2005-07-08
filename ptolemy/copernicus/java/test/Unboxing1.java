package ptolemy.copernicus.java.test;

import ptolemy.data.IntToken;

public class Unboxing1 {
    static IntToken intToken = new IntToken(1);

    public static void main(String[] strings) {
        IntToken inttoken = intToken;
        System.out.println("token = " + inttoken.toString());
    }
}
