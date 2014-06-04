package IDF;

import java.io.File;
import java.io.FileNotFoundException;

public interface EnergyCalculator
{
    public double CalculateFacilityElectricity(File energyusage) throws FileNotFoundException;
}
