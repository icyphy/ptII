
public class CaffeineApp {

    public static void main(String[] args) {
        float total = 1;

        BenchmarkUnit units[] = new BenchmarkUnit[6];
        units[0] = new BenchmarkUnit(new SieveAtom());
        units[1] = new BenchmarkUnit(new LoopAtom());
        units[2] = new BenchmarkUnit(new LogicAtom());
        units[3] = new BenchmarkUnit(new StringAtom());
        units[4] = new BenchmarkUnit(new FloatAtom());
        units[5] = new BenchmarkUnit(new MethodAtom());

        for (int i = 0; i < 6; i++) {
            BenchmarkUnit unit = units[i];
            int score = unit.testScore();
            System.out.print(unit.testName());
            System.out.print(" score = ");
            System.out.println(score);

            // We need the geometric mean.
            total = total * (float)Math.exp(Math.log(score)/6);
        }

        System.out.print("Overall score = ");
        System.out.println((int)total);


    }

}
