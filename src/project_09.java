import java.io.*;
import java.util.Locale;
import java.util.Scanner;

import static java.lang.System.exit;


public class project_09 {

    static Scanner read = new Scanner(System.in);
    public static final String GNUPLOT_LOCATION = "C:\\Program Files\\gnuplot\\bin\\gnuplot.exe";

    //After checking for command arguments we set this boolean flag so that on any method we can differentiate
    //between interactive mode (with user input) or without any user input
    private static boolean interactiveMode = false;
    //The path for the file that includes data input
    private static String filePath = "exemplo_parametros_modelo.csv";
    //Data from arguments for non-interactive mode
    //Lets use Double so that we can initialize them as null for future validations
    private static Double mArg = null;
    private static Double pArg = null;
    private static Double tArg = null;
    private static Double dArg = null;

    //For the data file lines and columns amount
    //For the lines (amount of entries) we will count them
    private static int dataLines = 0;
    //For the columns we know it needs to always be 5
    //Columns are name;beta;gama;ro;alfa
    private static final int dataColumns = 5;
    public static void main(String[] args) throws Exception {

        //Check if arguments have been inserted
        if (args != null && args.length > 0){

            //Check if we have all required arguments plus a filename
            if (args.length == 9 && args[0].contains(".csv")) {

                //Get file path argument
                filePath = args[0];
                //Get all data type
                setArg(args[1],args[2]);
                setArg(args[3],args[4]);
                setArg(args[5],args[6]);
                setArg(args[7],args[8]);
            }

            //Check if all arguments have been parsed
            if (filePath == null || mArg == null || pArg == null || tArg == null || dArg == null) {
                //If there was a problem with the arguments we show the error message and exit
                System.out.println("Inserted commands are not valid!");
                System.out.println("Example format: filename.csv -m X -p Y -t Z -d K");
                exit(0);
            }
        }
        else{
            //No arguments have been inserted, we will use interactive mode
            interactiveMode = true;
        }


        //Check if files exists, if not we exit
        File dataFile = new File(filePath);
        if (!dataFile.exists()){
            System.out.println("Data file does not exist, exiting...");
            exit(0);
        }

        //If file exists we can continue
        //Lets get line count and validate column amount
        if (!validateDataFile(dataFile)){
            System.out.println("Not a valid data file, please check the file contents.");
            exit(0);
        }

        //Now we fetch the values
        String[] names = new String[dataLines];
        //Data does not includes the name column so its the dataColumns - 1
        double[][] data = new double[dataLines][dataColumns-1];
        //Loads the data to the arrays and verifies each column data value
        if (!loadDataFromFile(dataFile, names, data)){
            System.out.println("There was an error in parsing the data from the file, please check file contents.");
            exit(0);
        }


        //In case we are in interactive mode we will ask the user
        if (interactiveMode){

            //Show the data collected from the csv file
            showEntryData(names, data);

            //Select the name from the data provided
            int selectedLine = findName(names, false);

            //Collect additional information data
            double[] additionalInfo = additionalInformation();

            //Print all the collected data and additional information
            printCollectedInformation(additionalInfo, data[selectedLine], names[selectedLine]);

            //Confirm all data and ask if user wants to insert it again
            confirmInitialInput(additionalInfo, data[selectedLine], names[selectedLine]);

            //Move to the menu and show available options for the user to select
            menuOptions(additionalInfo, data[selectedLine], names[selectedLine]);
        }
        //In case we are not in interactive mode we proceed without user input
        else{

            //Get additional information
            double[] additionalInfo = additionalInformation();

            for (int line = 0 ; line == dataLines ; line++){
                //TODO REVIEW FILE NAME
                if (mArg == 1) {
                    //Compute values
                    double[][] resultEuler_2 = methodEuler(data[line], additionalInfo);
                    //Save to file
                    createFileCSV(resultEuler_2, names[line]+  "_EULER.csv");
                    //Generate Graphics
                    //TODO Generate Graphics to File
                    graphicModuleEuler((int)additionalInfo[0],(int) additionalInfo[1]);
                    GraficoPngEuler(data[line], additionalInfo);
                    System.out.println(names[line]+ args[1]+ args[2]+ args[3]+ args[4]
                            + args[5]+ args[6]+ args[7]+ args[8]+"_RK4.csv");
                }
                else {
                    //Compute values
                    double[][] resultRk4_2 = methodRk4(data[line], additionalInfo);
                    //Save to file
                    createFileCSV(resultRk4_2, names[line]+ args[1]+ args[2]+ args[3]+ args[4]
                            + args[5]+ args[6]+ args[7]+ args[8]+"_RK4.csv");
                    //Generate Graphics
                    //TODO Generate Graphics to File
                    graphicModuleRk4((int)additionalInfo[0],(int) additionalInfo[1]);
                    GraficoPngrk4(data[line], additionalInfo);
                }
            }


        }

    }

    //Method to assign arguments
    private static void setArg(String argSwitch, String value){
        switch (argSwitch) {
            case "-m" -> {
                try {
                    mArg = Double.parseDouble(value);// 1-euler 2-Runge
                } catch (Exception e) {
                    System.out.println("Bad -m argument. Please insert 1 for Euler or 2 for Runge");
                }
            }
            case "-p" -> {
                try {
                    pArg = Double.parseDouble(value);// Step H
                } catch (Exception e) {
                    System.out.println("Bad -p argument. Please insert a number between 0 and 1. Needs to be bigger than 0. Example: 0.247");
                }
            }
            case "-t" -> {
                try {
                    tArg = Double.parseDouble(value);// Population size
                } catch (Exception e) {
                    System.out.println("Bad -t argument. Please insert a number bigger than 0.");
                }
            }
            case "-d" -> {
                try {
                    dArg = Double.parseDouble(value);// Number of days
                } catch (Exception e) {
                    System.out.println("Bad -d argument. Please insert a number bigger than 0.");
                }
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------
    //A Method to count the entries (lines) and validate that all lines have the correct columns amount
    private static boolean validateDataFile(File file){
        try {
            Scanner fileScanner = new Scanner(file);
            //We ignore the first line since it should always be headers
            fileScanner.nextLine();
            //Now we count and validate data
            while (fileScanner.hasNext()) {
                //Increment the number of lines
                dataLines = dataLines+1;
                //Check if we have the correct column size
                if (fileScanner.nextLine().split(";").length != dataColumns){
                    return false;
                }
            }
            return true;

        } catch (FileNotFoundException e) {
            return false;
        }
    }

    private static boolean loadDataFromFile(File file, String[] names, double[][] data){
        try {
            Scanner fileScanner = new Scanner(file);
            //We ignore the first line since it should always be headers
            fileScanner.nextLine();
            //Populate Data
            int currentLine = 0;
            while (fileScanner.hasNext()) {

                //Get data line into array
                String[] lineArray = fileScanner.nextLine().split(";");

                //Get name from position 0
                names[currentLine] = lineArray[0];

                //Get data columns after the name
                int currentColumn = 0;
                for (String column : lineArray){
                    if (currentColumn > 0){
                        //Validate if data can be parsed to double
                        try {
                            data[currentLine][currentColumn - 1] = Double.parseDouble(column.replace(",","."));
                        }catch (Exception e){
                            return false;
                        }
                    }
                    //Increment column
                    currentColumn++;
                }
                //Increment line
                currentLine++;

            }
            return true;

        } catch (FileNotFoundException e) {
            return false;
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------

    private static void showEntryData(String[] names, double[][] data) {
        System.out.println();
        System.out.println("The data present on the entry file is the following:");
        int currentLine = 0;
        for (String name : names){
            System.out.println(name + " | " + data[currentLine][0]+ " | " + data[currentLine][1] +
                    " | " + data[currentLine][2] + " | " + data[currentLine][3]
            );
            currentLine++;
        }
    }


    //------------------------------------------------------------------------------------------------------------------------------
    //Method for the user to select data according to the names
    private static int findName(String[] names, boolean isRetry) {

        if (!isRetry) {
            System.out.println("Please Input the Name of the Initial Spread of Fake News:");
        } else {
            System.out.println("Name not found! Please insert another name:");
        }

        String inputedName = read.next();

        //Check if name exists and return its position
        for (int i = 0; i < dataLines; i++) {
            if (inputedName.equalsIgnoreCase(names[i])) {
                System.out.println("Selected Spreader: " + names[i]);
                return i;
            }
        }

        //If name was not found we return to self and will show error message with the boolean isRetry
        return findName(names, true);

    }

    //------------------------------------------------------------------------------------------------------------------------------

    private static double[] additionalInformation() {

        //For number of steps
        double[] addInfo = new double[5];

        //Ask for number of steps
        if (!interactiveMode){
            addInfo[3] = dArg/pArg;
        }else {
            addInfo[3] = getNumberOfSteps(false);
        }

        //Ask for number of days
        if (!interactiveMode){
            addInfo[0] = dArg;
        }else {
            addInfo[0] = getNumberOfDays(false);
        }

        //Ask for number of population
        if (!interactiveMode){
            addInfo[1] = tArg;
        }else {
            addInfo[1] = getNumberOfPopulation(false);
        }

        //Ask for number of h
        if (!interactiveMode){
            addInfo[2] = pArg;
        }else {
            addInfo[2] = getNumberOfH(false);
        }

        return addInfo;
    }

    private static double getNumberOfSteps(boolean isRetry){

        if (!isRetry) {
            System.out.println("Define number of steps:");
        }

        String readString = read.next();
        double numberOfSteps;
        try {
            numberOfSteps = Double.parseDouble(readString.replace(",","."));
        }catch (Exception e){
            //Ignore exception
            System.out.println("Not a correct numeric format, please try again:");
            return getNumberOfSteps(true);
        }

        if (numberOfSteps <= 0){
            System.out.println("Number needs to be bigger than 0, please try again:");
            return getNumberOfSteps(true);
        }

        return numberOfSteps;
    }

    private static double getNumberOfDays(boolean isRetry){

        if (!isRetry) {
            System.out.println("Define number of days:");
        }

        String readString = read.next();
        double numberOfDays;
        try {
            numberOfDays = Double.parseDouble(readString.replace(",","."));
        }catch (Exception e){
            //Ignore exception
            System.out.println("Not a correct numeric format, please try again:");
            return getNumberOfDays(true);
        }

        if (numberOfDays <= 0){
            System.out.println("Number needs to be bigger than 0, please try again:");
            return getNumberOfDays(true);
        }

        return numberOfDays;
    }

    private static double getNumberOfPopulation(boolean isRetry){

        if (!isRetry) {
            System.out.println("Define number of population:");
        }

        String readString = read.next();
        double numberOfPopulation;
        try {
            numberOfPopulation = Double.parseDouble(readString.replace(",","."));
        }catch (Exception e){
            //Ignore exception
            System.out.println("Not a correct numeric format, please try again:");
            return getNumberOfPopulation(true);
        }

        if (numberOfPopulation <= 0){
            System.out.println("Number needs to be bigger than 0, please try again:");
            return getNumberOfPopulation(true);
        }

        return numberOfPopulation;
    }

    private static double getNumberOfH(boolean isRetry){

        if (!isRetry) {
            System.out.println("Define number of h between 0 and 1, needs to be bigger than zero, example 0.52.");
        }

        String readString = read.next();
        double numberOfH;
        try {
            numberOfH = Double.parseDouble(readString.replace(",","."));
        }catch (Exception e){
            //Ignore exception
            System.out.println("Not a correct numeric format, please try again:");
            return getNumberOfH(true);
        }

        if (numberOfH < 0 || numberOfH > 1){
            System.out.println("Number is out of range, needs to be between 0 and 1:");
            return getNumberOfH(true);
        }

        return numberOfH;
    }

    //------------------------------------------------------------------------------------------------------------------------------

    private static void printCollectedInformation(double[] additionalInfo, double[] entryContent, String name) {

        System.out.println("The Additional Information Inputted was the Following:");

        int numberOfDays = (int) additionalInfo[0];
        int numberOfPopulation = (int) additionalInfo[1];
        double numberOfH = additionalInfo[2];
        int numberOfSteps = (int) additionalInfo[3];

        System.out.println("Number of Setps:" + numberOfSteps);
        System.out.println("Number of Days:" + numberOfDays);
        System.out.println("Number of Population:" + numberOfPopulation);
        System.out.println("Number of h:" + numberOfH);

        System.out.println("Inital Spreader:" + name);
        System.out.println("Beta:" + entryContent[0]);
        System.out.println("Gama:" + entryContent[1]);
        System.out.println("Ro:" + entryContent[2]);
        System.out.println("Alfa:" + entryContent[3]);

    }

    //------------------------------------------------------------------------------------------------------------------------------

    //Check if Needed as It is! - Confirm Initial Input
    private static void confirmInitialInput(double[] additionalInfo, double[] entryContent, String name) {

        System.out.println("Do you want to re-enter the initial Parameters? (Y | N)");
        String confirmData = read.next().toUpperCase();

        switch (confirmData) {
            case "Y" -> {
                //Recollect the additional information
                additionalInfo = additionalInformation();
                //Show it to the user
                printCollectedInformation(additionalInfo, entryContent, name);
                //Confirm data again
                confirmInitialInput(additionalInfo, entryContent, name);
            }
            case "N" -> System.out.println("Initial Output Confirmed");

            default -> {
                System.out.println("Not a valid option. Do you want to re-enter the initial Parameters? (Y | N):");
                read.next();
            }
        }

    }

    //------------------------------------------------------------------------------------------------------------------------------


    private static void menuOptions(double[] additionalInfo, double[] entryContent, String name) throws IOException {

        System.out.println("\n\nChoose one of the following choices ");
        System.out.println("1.----------------------Euler Method");
        System.out.println("2.------Runge-Kutta 4th Order Method");
        System.out.println("3.------------------Graphical Output");
        System.out.println("4.----------------------Exit Program");
        System.out.println("Please enter a choice from 1 to 4");


        String readString = read.next();
        int optionSelection = 0;
        try {
            optionSelection = Integer.parseInt(readString);
        }catch (Exception e){
            //Ignore exception
            System.out.println("Not a correct numeric format, please try again:");
            menuOptions(additionalInfo, entryContent, name);
        }
        switch (optionSelection) {
            case 1:
                System.out.println("Will begin loading the Euler Method Computation");
                double [][] resultEuler = methodEuler(entryContent, additionalInfo);
                //Save to File
                createFileCSV(resultEuler, name+"_EULER.csv");
                //Generate Graphics
                graphicModuleEuler((int)additionalInfo[0],(int) additionalInfo[1]);
                GraficoPngEuler(entryContent, additionalInfo);
                //Go back to menu
                menuOptions(entryContent, additionalInfo,name);

                break;
            case 2:
                System.out.println("Will begin loading the Runge-Kutta 4th Order Method Computation");
                double [][] resultRk4 = methodRk4(entryContent,additionalInfo);
                //Save to File
                createFileCSV(resultRk4, name+"_RK4.csv");
                //Generate Graphics
                graphicModuleRk4((int)additionalInfo[0],(int) additionalInfo[1]);
                GraficoPngrk4(entryContent, additionalInfo);
                //Go back to menu
                menuOptions(entryContent, additionalInfo,name);
                break;
            case 3:
                System.out.println("Will begin loading the Graphical Output");
                double [][] resultEuler_2 = methodEuler(entryContent, additionalInfo);
                //Save to File
                createFileCSV(resultEuler_2, name+"_EULER.csv");
                double [][] resultRk4_2 = methodRk4(entryContent,additionalInfo);
                //Save to File
                createFileCSV(resultRk4_2, name+"_RK4.csv");
                //Generate Graphics
                graphicModuleEuler((int)additionalInfo[0],(int) additionalInfo[1]);
                GraficoPngEuler(entryContent, additionalInfo);
                menuOptions(entryContent, additionalInfo,name);
                break;
            case 4 :
                System.out.println("You will be now leaving the program");
                System.exit(0);
                break;
            default:
                System.out.println("You have inputted a word and/or a wrong Number");
                menuOptions(entryContent, additionalInfo,name);
                break;
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------

    public static double[][] methodEuler(double[] entryContent, double[] additionalInfo) {

        double betaEuler=entryContent[0];
        double gamaEuler = entryContent[1];
        double roEuler = entryContent[2];
        double alphaEuler=entryContent[3];

        double populationEuler = additionalInfo[1];
        int daysEuler = (int) additionalInfo[0];
        double hEuler = additionalInfo[2];
        double nrStepsEuler = (1/hEuler);


        double[][] resultadoEuler = new double[(int) (daysEuler + 1)][5];
        resultadoEuler[0][0] = 0;
        resultadoEuler[0][1] = populationEuler - 1;
        resultadoEuler[0][2] = 1;
        resultadoEuler[0][3] = 0;
        resultadoEuler[0][4] = populationEuler;
        double eulerS0 = populationEuler - 1;
        double eulerI0 = 1;
        double eulerR0 = 0;
        double eulerS1 = 0;
        double eulerI1 = 0;
        double eulerR1 = 0;

        for (int day = 1; day <= daysEuler; day++) {
            for (int contador = 0; contador < nrStepsEuler; contador++) {
                eulerS1 = eulerS0 + hEuler * (-betaEuler * eulerS0 * eulerI0);
                eulerI1 = eulerI0 + hEuler * (roEuler * betaEuler * eulerS0 * eulerI0 - gamaEuler * eulerI0 + alphaEuler * eulerR0);
                eulerR1 = eulerR0 + hEuler * (gamaEuler * eulerI0 - alphaEuler * eulerR0 + (1 - roEuler) * betaEuler * eulerS0 * eulerI0);
                eulerS0 = eulerS1;
                eulerI0 = eulerI1;
                eulerR0 = eulerR1;
            }
            resultadoEuler[day][0] = day;
            resultadoEuler[day][1] = eulerS0;
            resultadoEuler[day][2] = eulerI0;
            resultadoEuler[day][3] = eulerR0;
            resultadoEuler[day][4] = eulerS0 + eulerI0 + eulerR0;

        }

        System.out.println ("dia" + ";" + "  " + "S" + ";" + "   "+ "I" + ";" + "  " + "R" + ";" + "  " + "N");
        for (int i = 0; i <=daysEuler ; i++) {
            System.out.println(resultadoEuler[i][0] + " " + resultadoEuler[i][1]+" " + resultadoEuler[i][2] + " "+ resultadoEuler[i][3] + " " + resultadoEuler[i][4]);

        }


        return resultadoEuler;
    }
    //------------------------------------------------------------------------------------------------------------------------------
    public static double [][] methodRk4 (double[] entryContent,double[] additionalInformation) throws FileNotFoundException {

        double beta = entryContent[0];
        double gamma = entryContent[1];
        double ro = entryContent[2];
        double alfa = entryContent[3];

        double population = additionalInformation[1];
        int days = (int) additionalInformation[0];
        double h = additionalInformation[2];
        double nrSteps = (1/h);

        double S0 = population - 1;
        double I0 = 1;
        double R0 = 0;
        double S1 = 0;
        double I1 = 0;
        double R1 = 0;

        double s_k1 = 0;
        double i_K1 = 0;
        double r_K1 = 0;

        double s_k2 = 0;
        double i_K2 = 0;
        double r_K2 = 0;


        double s_k3 = 0;
        double i_K3 = 0;
        double r_K3 = 0;


        double s_k4 = 0;
        double i_K4 = 0;
        double r_K4 = 0;


        double[][] resultado = new double[(int) (days + 1)][5];
        resultado[0][0] = 0;
        resultado[0][1] = population - 1;
        resultado[0][2] = 1;
        resultado[0][3] = 0;
        resultado[0][4] = population;

        for (int day = 1; day <= days; day++) {
            for (int contador = 0; contador < nrSteps; contador++) {

                s_k1 = (-beta * S0 * I0);
                i_K1 = (ro * beta * S0 * I0 - gamma * I0 + alfa * R0);
                r_K1 = (gamma * I0 - alfa * R0 + (1 - ro) * beta * S0 * I0);

                s_k2 = (-beta * (S0 + ((h * s_k1) / 2)) * (I0 + ((h * i_K1) / 2)));
                i_K2 = (ro * beta * (S0 + ((h * s_k1) / 2)) * (I0 + ((h * i_K1) / 2)) - gamma * (I0 + ((h * i_K1) / 2)) + alfa * (R0 + ((h * r_K1 / 2))));
                r_K2 = (gamma * (I0 + ((h * i_K1) / 2)) - alfa * R0 + ((h * r_K1) / 2)) + (1 - ro) * beta * (S0 + ((h * s_k1) / 2)) * (I0 + ((h * i_K1) / 2));

                s_k3 = (-beta * (S0 + ((h * s_k2) / 2)) * (I0 + ((h * i_K2) / 2)));
                i_K3 = (ro * beta * (S0 + ((h * s_k2) / 2)) * (I0 + ((h * i_K2) / 2)) - gamma * (I0 + ((h * i_K2) / 2)) + alfa * (R0 + ((h * r_K2 / 2))));
                r_K3 = (gamma * (I0 + ((h * i_K2) / 2)) - alfa * R0 + ((h * r_K2) / 2)) + (1 - ro) * beta * (S0 + ((h * s_k2) / 2)) * (I0 + ((h * i_K2) / 2));

                s_k4 = (-beta * (S0 + (h * s_k3)) * (I0 + (h * i_K3)));
                i_K4 = (ro * beta * (S0 + (h * s_k3)) * (I0 + (h * i_K3)) - gamma * (I0 + (h * i_K3)) + alfa * (R0 + (h * r_K3 )));
                r_K4 = (gamma * (I0 + (h * i_K3)) - alfa * (R0 + ((h * r_K3) )) + (1 - ro) * beta * (S0 + (h * s_k3)) * (I0 + (h * i_K3)));


                S1 = S0 + ((h / 6)) * (s_k1 + (2 * s_k2) + (2 * s_k3) + s_k4);
                I1 = I0 + ((h / 6)) * (i_K1 + (2 * i_K2) + (2 * i_K3) + i_K4);
                R1 = R0 + ((h / 6)) * (r_K1 + (2 * r_K2) + (2 * r_K3) + r_K4);


                S0 = S1;
                I0 = I1;
                R0 = R1;

            }
            resultado[day][0] = day;
            resultado[day][1] = S0;
            resultado[day][2] = I0;
            resultado[day][3] = R0;
            resultado[day][4] = S0 + I0 + R0;


        }
        System.out.println ("dia" + ";" + "  " + "S" + ";" + "   "+ "I" + ";" + "  " + "R" + ";" + "  " + "N");
        for (int i = 0; i <=days ; i++) {
            System.out.println(resultado[i][0] + " " + resultado[i][1]+" " + resultado[i][2] + " "+ resultado[i][3] + " " + resultado[i][4]);
        }


        return resultado;
    }

    //------------------------------------------------------------------------------------------------------------------------------

    private static void graphicModuleEuler(int dias , int  populacao) throws FileNotFoundException {
        PrintWriter write = new PrintWriter("src\\grafeuler.gp");
        write.println("set title 'Propagação da noticia Falsa'");
        write.println("set term pngcairo");
        write.println("set output 'grafico.png'");
        write.println("set decimalsign ','");
        write.println("set encoding utf8");
        write.println("set datafile separator ';'");
        write.println("set ytics 100");
        write.println("set xtics 10");
        write.println("set yrange [0:" + populacao + "]");
        write.println("set xrange [0:" + dias + "]");
        write.println("plot 'grafeuler.csv' using 1:2 with line title 'S'," + " 'grafeuler.csv' using 1:3 with line title 'I', " + "'grafeuler.csv' using 1:4 with line title 'R'");
        write.close();

    }
    private static void graphicModuleRk4(int dias, int populacao) throws FileNotFoundException {
        PrintWriter write = new PrintWriter("src\\grafrk4.gp");
        write.println("set title 'Propagação da noticia Falsa'");
        write.println("set term pngcairo");
        write.println("set output 'graficork4.png'");
        write.println("set decimalsign ','");
        write.println("set encoding utf8");
        write.println("set datafile separator ';'");
        write.println("set ytics 100");
        write.println("set xtics 10");
        write.println("set ylabel 'População'");
        write.println("set xlabel 'Nº Dias'");
        write.println("set yrange [0:" + populacao + "]");
        write.println("set xrange [0:" + dias + "]");
        write.println("plot 'grafrk4.csv' using 1:2 with line title 'S'," + " 'grafrk4.csv' using 1:3 with line title 'I', " + "'grafrk4.csv' using 1:4 with line title 'R'");
        write.close();
    }



    //------------------------------------------------------------------------------------------------------------------------------

    private static void createFileCSV(double [][] resultado, String fileName) throws FileNotFoundException {

        PrintWriter write = new PrintWriter("");

        if(fileName.contains(".csv")){
            write = new PrintWriter(fileName);
        }else{
            write = new PrintWriter(fileName + ".csv");
        }

        //A função PrinterWriter em Java é uma classe de gravador usada para imprimir a representação formatada
        // de objetos em um fluxo de saída de texto. Criamos um objeto writer passando um novo arquivo denominado graph.csv como destino para o gravador.
        for (double[] doubles : resultado) {
            write.println(
                    String.format(Locale.US, "%6d", (int) doubles[0])
                            + ";" + String.format(Locale.US, "%.10f", doubles[1])
                            + ";" + String.format(Locale.US, "%.10f", doubles[2])
                            + ";" + String.format(Locale.US, "%.10f", doubles[3])
                            + ";" + String.format(Locale.US, "%.10f", doubles[4])

            );
        }
        write.close();
    }


    //------------------------------------------------------------------------------------------------------------------------------

    private static void fileOutput(double [][] additionalInfo , double [][] entryContent) {
        try {
            File myfile = new File("exemplo_resultado_teste1.csv");

            if (myfile.createNewFile()) {
                System.out.println("File created: " + myfile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }


    //------------------------------------------------------------------------------------------------------------------------------

    private static Process GraficoPngEuler(double [] entryContent , double [] additionalInfo) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process parametro  = rt.exec("gnuplot graf.gp");


        return parametro;

       /* Runtime rt = Runtime.getRuntime();
        String[] parametro = new String[1];
        parametro[0] = "gnuplot graf.gp";
        rt.exec(parametro);

        return parametro;
        */


    }

    private static Process GraficoPngrk4(double[] entryContent, double[] additionalInfo) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process parametro = rt.exec("gnuplot grafrk4.gp");
        return parametro;

    }




    //------------------------------------------------------------------------------------------------------------------------------

    /*public static void outputFile(double[][] resultado, double[] additionalInfo) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(new File("data2"+nomes[position]+".txt"));
        int days = (int) additionalInfo[0];
        System.out.println("dia " + " " + "S" + "   " + "I" + "   " + "R" + "   " + "N");
        for (int i = 0; i <=days ; i++) {
            System.out.println(resultado[i][0] + " " + resultado[i][1]+" " + resultado[i][2] + " "+ resultado[i][3] + " " + resultado[i][4]);
        }
        out.close();
    }*/


}








