package IDF;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;

public class Run_IDFGenerator_Threaded
{

    enum ProgramStyle
    {

        GUI,
        CMD
    }
    public static final Run_IDFGenerator.ProgramStyle pstyle = Run_IDFGenerator.ProgramStyle.GUI;

    public static void main(String[] args) throws InterruptedException, IOException
    {
        //Set number of threads to run at once
        IDFGenerator.THREAD_COUNT = 15;
        switch (pstyle)
        {
            case GUI:
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select Base IDF File");
                chooser.showOpenDialog(null);
                File base = chooser.getSelectedFile();
                chooser.setSelectedFile(null);
                chooser.setDialogTitle("Select Options File");
                chooser.showOpenDialog(null);
                File options = chooser.getSelectedFile();
                chooser.setSelectedFile(null);
                chooser.setDialogTitle("Select Weather File");
                chooser.showOpenDialog(null);
                File weather = chooser.getSelectedFile();
                chooser.setSelectedFile(null);
                chooser.setDialogTitle("Select Directory Containing Epl-run.bat");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.showOpenDialog(null);
                File eplRunDir = chooser.getSelectedFile();
                chooser.setDialogTitle("Select Output Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.showOpenDialog(null);
                File baseDir = chooser.getSelectedFile();

                if (options == null || baseDir == null || base == null)
                {
                    JOptionPane.showMessageDialog(null, "Missing input! Exiting...", "Missing Input", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                IDFGenerator.pstyle = pstyle;
                File dir = new File(baseDir.getPath() + "\\output.txt");
                if (dir.exists())
                {
                    dir.delete();
                }
                double startTime = System.currentTimeMillis();
                IDFGenerator.buildAndRunIDFs(options, base, baseDir, eplRunDir, weather);
                double endTime = System.currentTimeMillis();
                double time = (endTime - startTime);
                int seconds = (int)time / 1000 % 60;
                int minutes = (int)((time / (1000 * 60)) % 60);
                int hours = (int)((time / (1000 * 60 * 60)) % 60);
                JOptionPane.showMessageDialog(null, String.format("IDFGenerator finished Running!!!\n"
                        + "Estimated duration of run: %2d:%2d:%2d", hours, minutes, seconds),
                        "Finished", JOptionPane.INFORMATION_MESSAGE);
                break;
            }
            case CMD:
            {
                if (args.length < 2)
                {
                    System.err.println("Usage: java -jar "
                            + "IDFGenerator.jar <OptionsFile> "
                            + "<BaseIDF> <WeatherFile> <OutputDirectory> "
                            + "<BatchFileDirectory>");
                    System.exit(-1);
                }

                File options = new File(args[0]);
                File base = new File(args[1]);
                File baseDir = new File(args[2]);
                File weather = new File(args[3]);
                File eplRunDir = new File(args[4]);

                IDFGenerator.buildAndRunIDFs(options, base, baseDir, eplRunDir, weather);
                break;
            }
        }
    }
//    // TEST MAIN
//    public static void main(String[] args)
//    {
//        File options = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric\\Parametric_UNT_PI_12_16_2013_Options.xml");
//        File base = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric\\UNT_PI_12_16_2013_Options.idf");
//        File baseDir = new File("C:\\Documents and Settings\\fdot\\Desktop\\Parametric\\Output");
//        File batchLoc = new File("C:\\EnergyPlusV7-2-0");
//        File weather = new File("C:\\EnergyPlusV7-2-0\\USA_TX_Fort.Worth-Alliance.AP.722594_TMY3.epw");
//        IDFGenerator.buildAndRunIDFs(options, base, baseDir, batchLoc, weather);
//    }
}
