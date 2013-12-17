package IDF;

import java.io.File;

public class Run_IDFGenerator_Threaded
{

    public static void main(String[] args)
    {
        File options = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric\\Parametric_UNT_PI_12_16_2013_Options.xml");
        File base = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric\\UNT_PI_12_16_2013_Options.idf");
        File baseDir = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric\\Output");
        File pppDir = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric");
        File batchLoc = new File("C:\\EnergyPlusV7-2-0");
        File weather = new File("C:\\EnergyPlusV7-2-0\\USA_TX_Fort.Worth-Alliance.AP.722594_TMY3.epw");
        IDFGenerator.buildAndRunIDFs(options, base, baseDir, pppDir, batchLoc, weather);
    }
}
