package IDF;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Run_IDFGenerator_Threaded
{
    enum ProgramStyle
    {
        GUI,
        CMD
    }
    
    public static final IDFGenerator.ProgramStyle pstyle = IDFGenerator.ProgramStyle.GUI;

    public static void main(String[] args) throws InterruptedException, IOException
    {
        switch(pstyle)
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

                if(options == null || baseDir == null || base == null)
                {
                    JOptionPane.showMessageDialog(null, "Missing input! "
                            + "Exiting...", "Missing Input",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }

                int maxThreads = -1;
                String tCnt = "";
                while(maxThreads < 1)
                {
                    tCnt = JOptionPane.showInputDialog("Enter maximum number "
                            + "of threads to run (2-16 recommended):");
                    if(tCnt == null)
                    {
                        JOptionPane.showMessageDialog(null, "Cancelling run!!",
                                "Cancelling", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);
                    }
                    try
                    {
                        maxThreads = Integer.parseInt(tCnt);
                    }
                    catch(NumberFormatException e)
                    {
                        JOptionPane.showMessageDialog(null, "Please enter "
                                + "a positive integer value!");
                    }
                }

                if(maxThreads > 16)
                    JOptionPane.showMessageDialog(null, "Too many threads may "
                            + "slow down your machine while running!",
                            "WARNING!!!", JOptionPane.WARNING_MESSAGE);

                IDFGenerator.THREAD_COUNT = maxThreads;

                IDFGenerator.keep = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
                        "Do you want to keep error files?", "Keep Error Files?", JOptionPane.YES_NO_OPTION)
                        ? IDFGenerator.KeepFiles.YES : IDFGenerator.KeepFiles.NO;

                IDFGenerator.pstyle = pstyle;
                File dir = new File(baseDir.getPath() + "\\output.txt");
                if(dir.exists())
                {
                    dir.delete();
                }
                double startTime = System.currentTimeMillis();
                IDFGenerator.buildAndRunIDFs(options, base, baseDir, eplRunDir, weather);
                double endTime = System.currentTimeMillis();
                double time = (endTime - startTime);
                int seconds = (int) time / 1000 % 60;
                int minutes = (int) ((time / (1000 * 60)) % 60);
                int hours = (int) ((time / (1000 * 60 * 60)) % 60);
                JOptionPane.showMessageDialog(null, String.format("IDFGenerator finished Running!!!\n"
                        + "Estimated duration of run: %02d:%02d:%02d", hours, minutes, seconds),
                        "Finished", JOptionPane.INFORMATION_MESSAGE);
                break;
            }
            case CMD:
            {
                if(args.length < 5)
                {
                    System.err.println("Usage: java -jar "
                            + "IDFGenerator.jar <BaseIDF>"
                            + "<OptionsFile> <WeatherFile> <OutputDirectory> "
                            + "<BatchFileDirectory>");
                    System.exit(-1);
                }

                File base = new File(args[0]);
                File options = new File(args[1]);
                File baseDir = new File(args[2]);
                File weather = new File(args[3]);
                File eplRunDir = new File(args[4]);

                IDFGenerator.buildAndRunIDFs(options, base, baseDir, eplRunDir, weather);
                break;
            }
        }
    }
}
