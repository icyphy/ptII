package ptdb.test;
// FIXME: clean up style

public class ClassB {

    public static boolean isValidBDate(int date, String monthS) {
        if(date > 0 && date < 32)
            if("Jan".equalsIgnoreCase(monthS) || "Feb".equalsIgnoreCase(monthS))
                return true;
        return false;
    }

    public int getMonth(String monthS) {
        if("Jan".equalsIgnoreCase(monthS))
            return 3;
        if("Feb".equalsIgnoreCase(monthS))
            return 2;
        return 0;
    }
}
