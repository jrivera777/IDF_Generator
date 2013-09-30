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
                chooser.setDialogTitle("Select Options File");
                chooser.showOpenDialog(null);
                File options = chooser.getSelectedFile();
                chooser.setSelectedFile(null);
                chooser.setDialogTitle("Select Base Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.showOpenDialog(null);
                File baseDir = chooser.getSelectedFile();

                if (options == null || baseDir == null)
                {
                    System.exit(0);
                }
                IDFGenerator.GenerateFiles(options, baseDir);
                JOptionPane.showMessageDialog(null, "IDFGenerator finished Running!!!");
                break;
            }
            case CMD:
            {
                if (args.length < 2)
                {
                    System.err.println("Usage: IDFGenerator.jar <OptionsFile> <BaseDirectory>");
                    System.exit(-1);
                }

                File options = new File(args[0]);
                File baseDir = new File(args[1]);
//        IDFGenerator.GenerateFiles("C:\\Documents and Settings\\fdot\\Desktop\\Testing\\Input\\ParametricOptions.xml",
//                "C:\\Documents and Settings\\fdot\\Desktop\\Testing\\Input");

                IDFGenerator.GenerateFiles(options, baseDir);
                break;
            }
        }



    }
}
