package IDF;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class SumMonthCalculatorKWH implements EnergyCalculator
{

    @Override
    public double CalculateFacilityElectricity(File energyusage)
    {
        double electricity = 0.0;
        try
        {
            Scanner scan = new Scanner(energyusage);
            scan.nextLine(); //discard header line
            while (scan.hasNextLine())
            {
                String[] lineValues = scan.nextLine().split(",");
                electricity += Double.parseDouble(lineValues[1]); //second line is monthly facility usage
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return -1;
        }

        return electricity;
    }
}
