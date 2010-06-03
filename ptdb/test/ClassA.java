package ptdb.test;
// FIXME: clean up style

import java.io.File;

public class ClassA {
    
    public String getSunSign(int date, String monthS)
    {
        ClassB classC = new ClassB();
        boolean isValidDate = ClassB.isValidBDate(date, monthS);
        if(isValidDate)
        {
            int month = classC.getMonth(monthS);
            
            switch(month)
            {
            case 1 :  if (date < 22) 
                        return "Capricorn";
                      else
                          return "Aquarius";
            default : return "Cancer";
            }
        }
        return "Aries";
    }
    
    public boolean createDirectoryStructure(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.exists()) {
                throw new IllegalArgumentException("\"" + directoryPath + "\" already exists.");
        }

        return directory.mkdirs();
    }
}
