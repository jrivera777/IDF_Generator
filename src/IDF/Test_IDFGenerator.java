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
//        String name = "base-temp-1-1-2-3.idf";
//        Pattern p = Pattern.compile("base-temp-(.*)");
//        Matcher m = p.matcher(name);
//        System.out.println(m.groupCount());
//        while (m.find())
//        {
//            System.out.println(m.group(1));
//        }

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
                chooser.setDialogTitle("Select Base Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.showOpenDialog(null);
                File baseDir = chooser.getSelectedFile();

                if (options == null || baseDir == null || base == null)
                {
                    JOptionPane.showMessageDialog(null, "Missing input! Exiting...", "Missing Input", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                IDFGenerator.GenerateFiles(options, baseDir, base);
                JOptionPane.showMessageDialog(null, "IDFGenerator finished Running!!!", "Finished", JOptionPane.INFORMATION_MESSAGE);
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
                File base = new File(args[2]);
                IDFGenerator.GenerateFiles(options, baseDir, base);
                break;
            }
        }
    }
}
