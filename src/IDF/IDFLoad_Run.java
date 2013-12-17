package IDF;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.directory.DirContext;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class IDFLoad_Run implements Runnable
{

    private final String[] extensions =
    {
        ".audit", ".bnd", ".mdd", ".eio", ".err", ".eso",
        "Table.html", "Meter.csv", ".csv", ".mtd", ".mtr",
        ".rdd", ".rvaudit", ".shd", ".svg", ".idf"
    };
    private File baseIdf;
    private File baseOutputPath;
    private File batchLocation;
    private String permutation;
    private File weather_epw;
    private Map<String, List<POption>> parametrics;
    private Run_IDFGenerator.ProgramStyle pstyle;

    public IDFLoad_Run(File bidf, File bopp, File bLoc, File wthr,
            String perm, Map<String, List<POption>> params,
            Run_IDFGenerator.ProgramStyle style)
    {
        baseIdf = bidf;
        baseOutputPath = bopp;
        batchLocation = bLoc;
        permutation = perm;
        parametrics = params;
        pstyle = style;
        weather_epw = wthr;
    }

    @Override
    public void run()
    {
        String permIdf = baseOutputPath.getPath() + "\\" + permutation + ".idf";
        System.out.println("Running permutation: " + permutation + "...");
        try
        {
            FileUtils.copyFile(baseIdf, new File(permIdf));
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
            replaceUnusedParameter(permIdf, entry.getKey(), replacement);
            i++;
        }
        try
        {
            final String batch = batchLocation.getPath() + "\\Epl-run.bat";
            final String in = permIdf.substring(0, permIdf.lastIndexOf("."));
            final String out = baseOutputPath.getPath() + "\\" + permutation;

            //REFER TO Epl-run.bat comments for details
            //=========================================
            //Epl-run.bat "IDF file no extension" "Output file name no extension" 
            //"exention(idf)" "Weather File with extension" "EP or NONE" "Pausing?(N)" 
            //"Col limit?(nolimit)" "Convert ESO?(Y) "Process CSV?(Y)"  "active count? ("")" "Multi-threaded?(Y)" 

            ArrayList<String> commands = new ArrayList<String>()
            {

                
                {
                    add("cmd");
                    add("/c");
                    add(batch);
                    add(in);
                    add(out);
                    add("idf");
                    add(weather_epw.getPath());
                    add("EP");
                    add("N");
                    add("nolimit");
                    add("Y");
                    add("Y");
                    add("\"\"");
                    add("Y");
                }
            };
            ProcessBuilder pb = new ProcessBuilder(commands);
            File dir = new File(permutation);
            if (dir.mkdir())
            {
                pb.directory(dir);
            }
            double time = System.currentTimeMillis();
            double diff = 0;
            Process p = pb.start();
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader buffer = new BufferedReader(isr);
            String line = "";
            while ((line = buffer.readLine()) != null)
            {
                diff = System.currentTimeMillis() - time;
                if (diff > 30000)
                {
                    time = System.currentTimeMillis();
                    System.out.println("Simulation " + permutation + " still working...");
                }
            }
            System.out.println("Simulation " + permutation + " finished!");
            if (p.exitValue() == 0) //normal exit
                FileUtils.deleteDirectory(dir);
        }
        catch (IOException e)
        {
            e.printStackTrace();

        }
        EnergyCalculator calculator = new SumMonthCalculatorKWH();
        double totalElectricity = calculator.CalculateFacilityElectricity(new File(baseOutputPath.getPath() + "\\" + permutation + "Meter.csv"));

        OutputWriter.getInstance().writeLine(baseOutputPath.getPath() + "\\output.txt", permutation + " : " + totalElectricity);
        System.out.printf("%s wrote to to output.txt\n", permutation);
        for (String ext : extensions)
        {
            File f = new File(baseOutputPath.getPath() + "\\" + permutation + ext);
            f.delete();
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
            Pattern p = Pattern.compile("\\$" + param + ",\\s*(!-\\s*ignore)*");
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
            e.printStackTrace();
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