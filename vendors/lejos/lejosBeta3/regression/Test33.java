
import tinyvm.rcx.*;

public class Test33
{
    static final byte B = -100;
    static byte b = B;

    public static void main (String[] args) 
    {
        byte t = -100;
	short s = (short) b;
	int i = (int) t;
        int k = (int) s;
	
        if (B == -100)
	  LCD.showNumber (10);
	else
	  LCD.showNumber (B);
	if (b == -100)
	  LCD.showNumber (11);
	else
          LCD.showNumber (b);
	if (t == -100)
          LCD.showNumber (12);
	else
	  LCD.showNumber (t);
	if (s == -100)
          LCD.showNumber (13);
	else
	  LCD.showNumber (s);
	if (i == -100)
          LCD.showNumber (14);
	else
	  LCD.showNumber (i);
	if (k == -100)
          LCD.showNumber (15);
	else
	  LCD.showNumber (k);
    }
}
