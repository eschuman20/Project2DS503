import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

public class CreateDatasets {
    public static void main(String[] args) throws IOException {
//        CSVWriter csvWriter = new CSVWriter(new FileWriter("P.csv"));
//        LinkedList<String[]> listOfPoints = generateP();
//        csvWriter.writeAll(listOfPoints);
//        csvWriter.close();
//
//        CSVWriter csvWriter2 = new CSVWriter(new FileWriter("R.csv"));
//        LinkedList<String[]> listOfRectangles = generateR();
//        csvWriter2.writeAll(listOfRectangles);
//        csvWriter2.close();

        CSVWriter csvWriter3 = new CSVWriter(new FileWriter("K.csv"));
        LinkedList<String[]> listOfCentroid = generateK();
        csvWriter3.writeAll(listOfCentroid);
        csvWriter3.close();
    }

    public static LinkedList<String[]> generateP(){
        LinkedList<String[]> listOfPoints = new LinkedList<String[]>();
        Random random = new Random();
        for (int i = 1; i <= 10000000; i++){
            String[] aPoint = new String[2];
            aPoint[0] = Integer.toString(1 + random.nextInt(10000));
            aPoint[1] = Integer.toString(1 + random.nextInt(10000));
            listOfPoints.add(aPoint);
        } return listOfPoints;
    }

    public static LinkedList<String[]> generateR(){
        LinkedList<String[]> listOfRectangles = new LinkedList<String[]>();
        Random random = new Random();
        for (int i = 1; i <= 5000000; i++){
            String[] aRectangle = new String[4];
            //bottomLeftX
            aRectangle[0] = Integer.toString(1 + random.nextInt(10000));
            //bottomLeftY
            aRectangle[1] = Integer.toString(1 + random.nextInt(10000));
            //height
            aRectangle[2] = Integer.toString(1 + random.nextInt(20));
            //width
            aRectangle[3] = Integer.toString(1 + random.nextInt(5));
            listOfRectangles.add(aRectangle);
        } return listOfRectangles;
    }

    public static LinkedList<String[]> generateK(){
        LinkedList<String[]> listOfPoints = new LinkedList<String[]>();
        Random random = new Random();
        for (int i = 1; i <= 30; i++){
            String[] aPoint = new String[2];
            aPoint[0] = Integer.toString(1 + random.nextInt(10000));
            aPoint[1] = Integer.toString(1 + random.nextInt(10000));
            listOfPoints.add(aPoint);
        } return listOfPoints;
    }

}
