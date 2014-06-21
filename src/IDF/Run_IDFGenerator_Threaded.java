package IDF;

import java.io.File;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Run_IDFGenerator_Threaded
{
    enum ProgramStyle
    {
        GUI,
        CMD
    }
    static final IDFGenerator.ProgramStyle pstyle = IDFGenerator.ProgramStyle.CMD;

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
                File outputDir = chooser.getSelectedFile();

                if(options == null || outputDir == null || base == null)
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
                            + "of simulations to run (2-16 recommended):");
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
                    JOptionPane.showMessageDialog(null, "Too many simulations may "
                            + "slow down your machine while running!",
                            "WARNING!!!", JOptionPane.WARNING_MESSAGE);

                IDFGenerator.THREAD_COUNT = maxThreads;

                IDFGenerator.keepErr = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
                        "Do you want to keep error files?", "Keep Error Files?",
                        JOptionPane.YES_NO_OPTION)
                        ? IDFGenerator.KeepFiles.YES
                        : IDFGenerator.KeepFiles.NO;

                IDFGenerator.keepIdf = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
                        "Do you want to keep generated IDFs?",
                        "Keep IDFs?", JOptionPane.YES_NO_OPTION)
                        ? IDFGenerator.KeepFiles.YES : IDFGenerator.KeepFiles.NO;

                if(IDFGenerator.keepIdf == IDFGenerator.KeepFiles.YES)
                {
                    JOptionPane.showMessageDialog(null, "WARNING!!! Saving "
                            + "IDF files may result in the use of large amounts "
                            + "of storage space!", "WARNING: Saving IDFs",
                            JOptionPane.WARNING_MESSAGE);
                }

                IDFGenerator.pstyle = pstyle;
                File dir = new File(outputDir.getPath() + "\\output.txt");
                if(dir.exists())
                {
                    dir.delete();
                }
                double startTime = System.currentTimeMillis();
                IDFGenerator.buildAndRunIDFs(options, base, outputDir,
                        eplRunDir, weather);
                double endTime = System.currentTimeMillis();
                double time = (endTime - startTime);
                int seconds = (int) time / 1000 % 60;
                int minutes = (int) ((time / (1000 * 60)) % 60);
                int hours = (int) ((time / (1000 * 60 * 60)) % 60);
                JOptionPane.showMessageDialog(null,
                        String.format("IDFGenerator finished Running!!!\n"
                        + "Estimated duration of run: %02d:%02d:%02d", hours,
                        minutes, seconds),
                        "Finished", JOptionPane.INFORMATION_MESSAGE);
                break;
            }
            case CMD:
            {
                if(args.length < 5)
                {
                    System.err.println("Usage: java -jar "
                            + "IDFGenerator.jar <BaseIDF>"
                            + "<OptionsFile> <WeatherFile> "
                            + "<BatchFileDirectory> <OutputDirectory> "
                            + "[KeepErrs] [KeepIDFs]");
                    System.exit(-1);
                }

                File base = new File(args[0]);
                File options = new File(args[1]);
                File weather = new File(args[2]);
                File eplRunDir = new File(args[3]);
                File outputDir = new File(args[4]);

                int maxThreads = IDFGenerator.THREAD_COUNT;
                String kerrors = "N";
                String kidfs = "N";

                if(args.length >= 8)
                {
                    try
                    {
                        maxThreads = Integer.parseInt(args[5]);
                    }
                    catch(NumberFormatException e)
                    {
                        maxThreads = IDFGenerator.THREAD_COUNT;
                    }
                    IDFGenerator.THREAD_COUNT = maxThreads;
                    kerrors = args[6];
                    IDFGenerator.keepErr = kerrors.toLowerCase().charAt(0) == 'y'
                            ? IDFGenerator.KeepFiles.YES
                            : IDFGenerator.KeepFiles.NO;

                    kidfs = args[7];
                    IDFGenerator.keepIdf = kidfs.toLowerCase().charAt(0) == 'y'
                            ? IDFGenerator.KeepFiles.YES
                            : IDFGenerator.KeepFiles.NO;
                }

                System.out.printf("Base IDF: %s\n", base.getName());
                System.out.printf("Parametric Options File: %s\n", options.getName());
                System.out.printf("Weather File: %s\n", weather.getName());
                System.out.printf("Epl-Run.bat Directory: %s\\\n", eplRunDir.getPath());
                System.out.printf("Output Directory: %s\\\n", outputDir.getPath());
                System.out.printf("# of simulations run in parallel: %d\n", IDFGenerator.THREAD_COUNT);
                System.out.printf("Keep Error Files: %s\n", kerrors.toUpperCase());
                System.out.printf("Keep IDFs: %s\n", kidfs.toUpperCase());
                System.out.println();


                IDFGenerator.buildAndRunIDFs(options, base, outputDir,
                        eplRunDir, weather);
                break;
            }
        }
    }
}
