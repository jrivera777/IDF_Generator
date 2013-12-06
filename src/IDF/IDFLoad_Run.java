package IDF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class IDFLoad_Run implements Runnable
{

    private File baseIdf;
    private File baseOutputPath;
    private String permutation;
    private Map<String, List<POption>> parametrics;
    private Run_IDFGenerator.ProgramStyle pstyle;

    public IDFLoad_Run(File bidf, File bopp, String perm, Map<String, List<POption>> params, Run_IDFGenerator.ProgramStyle style)
    {
        baseIdf = bidf;
        baseOutputPath = bopp;
        permutation = perm;
        parametrics = params;
        pstyle = style;
    }

    @Override
    public void run()
    {
        try
        {
            FileUtils.copyFile(baseIdf, new File(baseOutputPath.getPath() + "\\" + permutation + ".idf"));
        }
        catch (IOException e)
        {
            if (pstyle == Run_IDFGenerator.ProgramStyle.CMD)
                System.err.println("Failed to copy base IDF file!!");
            else
                JOptionPane.showMessageDialog(null, "Failed to copy base IDF file!!", "Copy Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        String[] opts = permutation.split("-");
        int i = 0;
        for (Map.Entry<String, List<POption>> entry : parametrics.entrySet())
        {
            String val = opts[i];
            String replacement = "";
            for (POption op : entry.getValue())
            {
                if (val.equals(op.getValue()))
                {
                    replacement = op.getName();
                    break;
                }
            }

            replaceUnusedParameter(baseOutputPath.getPath() + "\\" + permutation + ".idf", entry.getKey(), replacement);

            i++;
        }
    }

    private void replaceUnusedParameter(String fileName, String param, String rplc)
    {
        FileInputStream input = null;
        FileOutputStream output = null;
        try
        {
            input = new FileInputStream(fileName);
            String content = IOUtils.toString(input);
            Pattern p = Pattern.compile("\\$" + param + ",\\s*!-\\s*ignore");
            content = p.matcher(content).replaceAll(
                    Matcher.quoteReplacement(rplc + ", !- Current Value"));

            output = new FileOutputStream(fileName);
            IOUtils.write(content, output);
        }
        catch (IOException e)
        {
            if (pstyle == Run_IDFGenerator.ProgramStyle.CMD)
                System.err.println("Something went wrong replacing unused parameter!!!");
            else
                JOptionPane.showMessageDialog(null, "Something went wrong replacing unused parameter!!!",
                        "Parameter Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        finally
        {
            try
            {
                if (input != null)
                    input.close();
                if (output != null)
                    output.close();
            }
            catch (IOException ex)
            {
            }
        }
    }
}