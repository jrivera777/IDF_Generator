package IDF;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.commons.io.IOUtils;

public class IDFGenerator
{

    /**
     * Generates all possible IDF files by running energyplus auxilary program
     * parametricpreprocessor.exe on file within a directory.
     *
     * @param optionsPath
     * @param basePath
     */
    public static void GenerateFiles(File optionsPath, File baseIdfPath)
    {
        FilenameFilter fnf =
                new FilenameFilter()
                {

                    @Override
                    public boolean accept(File dir, String name)
                    {
                        return !name.equalsIgnoreCase("base.idf")
                                && !name.equalsIgnoreCase("base-temp.idf")
                                && name.toLowerCase().endsWith(".idf");
                    }
                };
        validatePaths(baseIdfPath, optionsPath);

        //load parametric options
        Map<String, List<POption>> parametrics = readParametricOptions(optionsPath.getPath());
        int dashCount = 1;
        int paramMax = parametrics.size();
        /// Run PPP on base file first
        String baseFileName = baseIdfPath + "\\base.idf";
        String baseTempName = baseIdfPath + "\\base-temp.idf";
        try
        {
            FileUtils.copyFile(new File(baseFileName), new File(baseTempName));
        }
        catch (IOException e)
        {
            System.err.println("Failed to copy base.idf file!!");
            System.exit(-1);
        }

        String param = parametrics.keySet().toArray()[0].toString();
        List<POption> pOpts = parametrics.remove(param);
        //replace single $parameter with =$parameter so the 
        //parametetricpreprocessor.exe works properly
        replaceUnusedParameter(baseTempName, param);
        //add parametric section to file.
        addParametricObjects(baseTempName, param, pOpts);
        dashCount++;
        try
        {
            runPPP(baseIdfPath.getPath(), baseTempName);
        }
        catch (InterruptedException ex)
        {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }
        catch (IOException ex)
        {
            System.err.println("Something went wrong running the parametric"
                    + "preprocessor program!!! Check <idfFile>.err for details.");
            System.exit(-1);
        }

        //apply same process for every other parameter on all files
        //in the directory (which should be growing)
        while (!parametrics.isEmpty())
        {
            param = parametrics.keySet().toArray()[0].toString();
            pOpts = parametrics.remove(param);

            File[] idfFiles = baseIdfPath.listFiles(fnf);
            for (File idf : idfFiles)
            {
                if (countDashes(idf.getName()) == dashCount)
                {
                    replaceUnusedParameter(idf.getPath(), param);
                    addParametricObjects(idf.getPath(), param, pOpts);
                    try
                    {
                        runPPP(baseIdfPath.getPath(), idf.getPath());
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
            dashCount++;
        }

        /*
         * Remove 'intermediate' files, i.e. files that don't have all
         * parameters filled out with an option (not including the base file).
         * The files needed should have -# additions equal to the number of
         * parameters there were. e.g. 3 parameters might look like
         * base-1-2-2.idf.
         */

        try
        {
            FilenameFilter filter = new FilenameFilter()
            {

                @Override
                public boolean accept(File dir, String name)
                {
                    return !name.equalsIgnoreCase("base.idf")
                            && (name.toLowerCase().endsWith(".idf")
                            || name.toLowerCase().endsWith(".int"));
                }
            };
            CopyResults(baseIdfPath, fnf, paramMax);
            cleanUpFiles(baseIdfPath, filter);
        }
        catch (Exception e)
        {
        }

    }

    private static void validatePaths(File dir, File options)
    {
        if (!dir.isDirectory())
        {
            System.err.printf("%s is not a directory!! Quiting...\n", dir.getName());
            System.exit(-1);
        }
        if (!options.isFile() || !options.getName().toLowerCase().endsWith(".xml"))
        {
            System.err.printf("%s is not an XML file!! Quiting...\n");
            System.exit(-1);
        }
    }

    private static void replaceUnusedParameter(String fileName, String param)
    {
        FileInputStream input = null;
        FileOutputStream output = null;
        try
        {
            input = new FileInputStream(fileName);
            String content = IOUtils.toString(input);
            Pattern p = Pattern.compile("\\$" + param + ",\\s*!-ignore");
            content = p.matcher(content).replaceAll(
                    Matcher.quoteReplacement("=$" + param + ", !- Current "
                    + param + " Value"));

            output = new FileOutputStream(fileName);
            IOUtils.write(content, output);
        }
        catch (IOException e)
        {
            System.err.println("Something went wrong replacing unused parameter!!!");
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

    private static void addParametricObjects(String fileName, String param, List<POption> pOpts)
    {
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
            out.println("\n\n!-*****Parametrics start here!*****\n");
            out.println("  Parametric:SetValueForRun,"); //parametric object title

            out.printf("\t$%s, !-Parameter Name\n", param);
            //write out parameter options.
            for (int i = 0; i < pOpts.size(); i++)
            {
                if (i == pOpts.size() - 1)
                    out.printf("\t%s; !-Value %d\n", pOpts.get(i).getName(), i + 1);
                else
                    out.printf("\t%s, !-Value %d\n", pOpts.get(i).getName(), i + 1);
            }

            //write out file suffixes (which seem to be necessary)
            out.println();
            out.println("  Parametric:FileNameSuffix,");
            out.println("\tNames,");
            for (int i = 0; i < pOpts.size(); i++)
            {
                if (i == pOpts.size() - 1)
                    out.printf("\t%s;\n", pOpts.get(i).getValue());
                else
                    out.printf("\t%s,\n", pOpts.get(i).getValue());
            }
        }
        catch (IOException e)
        {
            if (out != null)
                out.close();
            System.err.println("Somethin went wrong adding parametric objects!!!");
            System.exit(-1);
        }
        finally
        {
            if (out != null)
                out.close();
        }
    }

    private static void runPPP(String basePath, String fileName) throws IOException, InterruptedException
    {

        String[] proc =
        {
            basePath + "\\parametricpreprocessor.exe",
            fileName
        };
        //run ParametricPreprocess.exe on idf file with 
        Process p = Runtime.getRuntime().exec(proc);
        p.waitFor();
    }

    private static Map<String, List<POption>> readParametricOptions(String paraOptions)
    {
        Map<String, List<POption>> params = new HashMap<String, List<POption>>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(paraOptions);

            Element docEle = dom.getDocumentElement();
            docEle.normalize();

            NodeList list = docEle.getElementsByTagName("ParametricOption");
            if (list != null && list.getLength() > 0)
            {
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node pOpt = list.item(i);
                    String paramName = pOpt.getAttributes().item(0).getNodeValue().trim();

                    if (pOpt.getNodeType() == Node.ELEMENT_NODE)
                    {
                        Element elem = (Element) pOpt;
                        NodeList options = elem.getElementsByTagName("Option");
                        if (options != null && options.getLength() > 0)
                        {
                            for (int j = 0; j < options.getLength(); j++)
                            {
                                Node opt = options.item(j);
                                String optionName = opt.getChildNodes().item(0).getNodeValue().trim();
                                String value = opt.getAttributes().item(0).getNodeValue().trim();

                                List<POption> opts = params.get(paramName);
                                if (opts == null)
                                {
                                    opts = new ArrayList<POption>();
                                    opts.add(new POption(optionName, value));
                                    params.put(paramName, opts);
                                }
                                else
                                {
                                    opts.add(new POption(optionName, value));
                                    params.put(paramName, opts);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (ParserConfigurationException pce)
        {
            pce.printStackTrace();
        }
        catch (SAXException se)
        {
            se.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        return params;
    }

    private static int countDashes(String fName)
    {
        char[] letters = fName.toCharArray();
        int count = 0;
        for (int i = 0; i < letters.length; i++)
        {
            if (letters[i] == '-')
                count++;
        }
        return count;
    }

    private static void cleanUpFiles(File dir, FilenameFilter filter) throws IOException
    {
        File[] files = dir.listFiles(filter);
        for (int i = 0; i < files.length; i++)
            files[i].deleteOnExit();
    }

    public static void CopyResults(File fromDir, FilenameFilter filter, int paramCount) throws IOException
    {
        File[] files = fromDir.listFiles(filter);
        File toDir = new File(fromDir.getPath() + "\\OutputIDFs");
        boolean madeDir = toDir.mkdir();
        if (!madeDir)
            throw new IOException("Failed to create Output Directory.  "
                    + "Stopped copying results over");
        for (int i = 0; i < files.length; i++)
        {
            if (countDashes(files[i].getName()) - 1 == paramCount)
                FileUtils.copyFileToDirectory(files[i], toDir);
        }
    }
}
