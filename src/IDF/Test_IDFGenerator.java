package IDF;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Test_IDFGenerator
{

    enum ProgramStyle
    {

        GUI,
        CMD
    }
    public static final ProgramStyle pstyle = ProgramStyle.GUI;

    public static void main(String[] args)
    {
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
                chooser.setDialogTitle("Select ParametricPreProcessor Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.showOpenDialog(null);
                File pppDir = chooser.getSelectedFile();
                chooser.setDialogTitle("Select Base Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.showOpenDialog(null);
                File baseDir = chooser.getSelectedFile();

                if (options == null || baseDir == null || base == null)
                {
                    JOptionPane.showMessageDialog(null, "Missing input! Exiting...", "Missing Input", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                IDFGenerator.GenerateFiles(options, base, baseDir, pppDir);
                JOptionPane.showMessageDialog(null, "IDFGenerator finished Running!!!", "Finished", JOptionPane.INFORMATION_MESSAGE);
                break;
            }
            case CMD:
            {
                if (args.length < 2)
                {
                    System.err.println("Usage: IDFGenerator.jar <OptionsFile> "
                            + "<BaseIDF> <BaseDirectory> "
                            + "<ParametricPreProcessorDirectory>");
                    System.exit(-1);
                }

                File options = new File(args[0]);
                File base = new File(args[1]);
                File baseDir = new File(args[2]);
                File pppDir = new File(args[2]);

                IDFGenerator.GenerateFiles(options, base, baseDir, pppDir);
                break;
            }
        }
    }
}
