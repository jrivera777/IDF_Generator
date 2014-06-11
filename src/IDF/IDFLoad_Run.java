package IDF;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * *
 * Thread to run EnergyPlus simulations using Epl-run.bat provided in EnergyPlus
 * install.
 *
 * Work Flow: 1. Copy Base IDF file. 2. Change parametric variables to selected
 * options for this permutation. 3. Run Epl-run.bat with created IDF and given
 * Weather file. 4. Write results into global output file (output.txt) 5. Clean
 * up all temporary files.
 *
 * @author Joseph Rivera
 */
public class IDFLoad_Run implements Runnable
{
    // Extensions for file created by simulation and thread
    // Mostly used to delete them after successful finish
    private final String[] extensions =
    {
        ".audit", ".bnd", ".mdd", ".eio", ".err", ".eso",
        "Table.html", "Meter.csv", ".csv", ".mtd", ".mtr",
        ".rdd", ".rvaudit", ".shd", ".svg", ".idf", "-PROC.csv"
    };
    private File baseIdf;
    private File baseOutputPath;
    private File batchLocation;
    private String permutation;
    private File weather_epw;
    private Map<String, List<POption>> parametrics;
    private IDFGenerator.ProgramStyle pstyle;
    private JTextArea area;

    public IDFLoad_Run(File bidf, File bopp, File bLoc, File wthr,
            String perm, Map<String, List<POption>> params,
            IDFGenerator.ProgramStyle style)
    {
        baseIdf = bidf;
        baseOutputPath = bopp;
        batchLocation = bLoc;
        permutation = perm;
        parametrics = params;
        pstyle = style;
        weather_epw = wthr;
    }

    public IDFLoad_Run(File bidf, File bopp, File bLoc, File wthr,
            String perm, Map<String, List<POption>> params,
            IDFGenerator.ProgramStyle style, JTextArea ar)
    {
        this(bidf, bopp, bLoc, wthr, perm, params, style);
        area = ar;
    }

    @Override
    public void run()
    {
        String permIdf = baseOutputPath.getPath() + "\\" + permutation + ".idf";

        writeOutMessage("Running permutation: " + permutation + "...");
        try
        {
            FileUtils.copyFile(baseIdf, new File(permIdf));
        }
        catch(IOException e)
        {
            if(pstyle == IDFGenerator.ProgramStyle.CMD)
                System.err.println("Failed to copy base IDF file!!");
            else
                JOptionPane.showMessageDialog(null, "Failed to copy base IDF file!!", "Copy Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        // Replace every parametric variable in the IDF with the chosen option
        // for this permutation
        String[] opts = permutation.split("-");
        int i = 0;
        for(Map.Entry<String, List<POption>> entry : parametrics.entrySet())
        {
            String val = opts[i];
            String replacement = "";
            for(POption op : entry.getValue())
            {
                if(val.equals(op.getValue()))
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
            // Run Epl-run.bat program (which calls Energyplus.exe) to
            // start a simulation with this IDF file
            final String batch = batchLocation.getPath() + "\\Epl-run.bat";
            final String in = permIdf.substring(0, permIdf.lastIndexOf("."));
            final String out = baseOutputPath.getPath() + "\\" + permutation;

            // REFER TO Epl-run.bat comments for details about input parameters
            // =========================================
            // Epl-run.bat "IDF file no extension" "Output file name no extension" 
            // "exention(idf)" "Weather File with extension" "EP or NONE" "Pausing?(N)" 
            // "Col limit?(nolimit)" "Convert ESO?(Y) "Process CSV?(Y)"  "active count? ("")" "Multi-threaded?(Y)" 

            //NOTE: Path to Epl-run.bat file should probably NOT have spaces in it.
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

            // Create new directory for this Simulation so as not to conflict
            // with any other simulations already running
            File dir = new File(permutation);
            if(dir.mkdir())
            {
                pb.directory(dir);
            }

            double time = System.currentTimeMillis();
            double diff = 0;
            Process p = pb.start();

            // Handle output from simulation
            // Currently just thrown away. Must be done though, otherwise
            // we have a deadlock between this thread and the process running
            // the simulation
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader buffer = new BufferedReader(isr);
            String line = "";
            while((line = buffer.readLine()) != null)
            {
                diff = System.currentTimeMillis() - time;
                if(diff > 15000)
                {
                    time = System.currentTimeMillis();
                    writeOutMessage("Simulation " + permutation + " still working...");
                }
            }
            writeOutMessage("Simulation " + permutation + " finished!");
            if(p.exitValue() != 0) //normal exit
            {
                writeOutMessage("Something went wrong with permutation " + permutation + ".");
            }
            FileUtils.deleteDirectory(dir);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        // Read file to calculate electricity usage for time duration (and any 
        // other data values that might be pulled in the future.
        // Implement EnergyCalculator Interface to handle new file formats
        EnergyCalculator calculator = new SumMonthCalculatorKWH();
        boolean writeOutput = true;
        String res = "";
        double totalElectricity = -1;
        try
        {
            totalElectricity = calculator.CalculateFacilityElectricity(new File(baseOutputPath.getPath() + "\\" + permutation + "Meter.csv"));
            res = permutation + " : " + totalElectricity;
        }
        catch(FileNotFoundException e)
        {
            res = "Failed to find file: " + baseOutputPath.getPath() + "\\" + permutation + "Meter.csv..."
                    + "Check .ERR files if you chose to save them!";
            writeOutput = false;
        }
        catch(IOException e)
        {
            res = e.getMessage();
            e.printStackTrace();
        }

        OutputWriter.getInstance().writeLine(baseOutputPath.getPath() + "\\output.txt", res);
        writeOutMessage(permutation + " wrote to to output.txt");

        for(String ext : extensions)
        {
            if(ext.equals(".err") && IDFGenerator.keepErr == IDFGenerator.KeepFiles.YES)
                continue;
            if(ext.equals(".idf") && IDFGenerator.keepIdf == IDFGenerator.KeepFiles.YES)
                continue;
            File f = new File(baseOutputPath.getPath() + "\\" + permutation + ext);
            f.delete();
        }
    }

    /**
     * *
     * Searches given file for one value and replaces it with the other.
     *
     * @param fileName File to search in.
     * @param param Value to to replace in file.
     * @param rplc Replacement value.
     */
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
                    Matcher.quoteReplacement(rplc + ", !- Current Value\n"));

            output = new FileOutputStream(fileName);
            IOUtils.write(content, output);
        }
        catch(IOException e)
        {
            if(pstyle == IDFGenerator.ProgramStyle.CMD)
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
                if(input != null)
                    input.close();
                if(output != null)
                    output.close();
            }
            catch(IOException ex)
            {
            }
        }
    }

    public void writeOutMessage(String message)
    {
        System.out.println(message);
        if(area != null)
        {
            area.append(message + "\n");
            area.setCaretPosition(area.getDocument().getLength());
        }
    }
}