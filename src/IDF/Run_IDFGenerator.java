package IDF;

import IDF.IDFGenerator.ProgramStyle;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Class runs IDF_Generator  program that creates all possible combinations
 * of IDF files from some base IDF file and a set of options.
 * 
 * @author Joseph Rivera
 * @deprecated
 */
public class Run_IDFGenerator
{
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
                chooser.setDialogTitle("Select Output Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.showOpenDialog(null);
                File baseDir = chooser.getSelectedFile();

                IDFGenerator.pstyle = pstyle;
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
                File pppDir = new File(args[3]);

                IDFGenerator.GenerateFiles(options, base, baseDir, pppDir);
                break;
            }
        }
    }
}
