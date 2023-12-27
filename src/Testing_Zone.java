import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
/*
public class Testing_Zone {
    static final int MAX_NAMES=10;
    static final int MAX_CASES=4;
    static final String FILE_IN= "exemplo_resultado_modelo.txt";

    public static void main(String[] args)throws FileNotFoundException {
        String[] names =new String[MAX_NAMES];
        int[][] cases =new int[MAX_NAMES][MAX_CASES];
    }

    public static int readFile (String [] Names , int[][] cases)throws FileNotFoundException{

        Scanner in = new Scanner(new File(FILE_IN));

        int names = 0;
        

        return 0;
    }

    public static double[][] metodoEulerSamuel(double beta, double gama, double ro, double alpha, int population, int days, double h) {
        double[][] resultado = new double[days + 1][4];
        resultado[0][0] = population - 1;
        resultado[0][1] = 1;
        resultado[0][2] = 0;
        resultado[0][3] = population;
        double S0 = population - 1;
        double I0 = 1;
        double R0 = 0;
        double S1 = 0;
        double I1 = 0;
        double R1 = 0;
        for (int day = 1; day <= days; day++) {
            for (int contador = 1; contador <= 1 / h; contador++) {
                S1 = S0 + h * (-beta * S0 * I0);
                I1 = I0 + h * (ro * beta * S0 * I0 - gama * I0 + alpha * R0);
                R1 = R0 + h * (gama * I0 - alpha * R0 + (1 - ro) * beta * S0 * I0);
                S0 = S1;
                I0 = I1;
                R0 = R1;
            }
            resultado[day][0] = S0;
            resultado[day][1] = I0;
            resultado[day][2] = R0;
            resultado[day][3] = S0 + I0 + R0;

        }
        return resultado;
    }
}

 */
