package IDF;

import java.io.File;

public class Run_IDFGenerator_Threaded
{

    public static void main(String[] args)
    {
        File options = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric\\Parametric_UNT_PI_11_24_2013_Options.xml");
        File base = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric\\UNT_PI_11_24_2013_Options.idf");
        File baseDir = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric\\Output");
        File pppDir = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric");

        IDFGenerator.buildAndRunIDFs(options, base, baseDir, pppDir);
    }
}
