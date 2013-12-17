package IDF;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/***
 * Singleton class to write to a file in a thread-safe way.
 * 
 * @author Joseph Rivera
 */
public class OutputWriter
{

    private static OutputWriter out = null;

    public static OutputWriter getInstance()
    {
        if (out == null)
        {
            synchronized (OutputWriter.class)
            {
                if (out == null)
                    out = new OutputWriter();
            }
        }
        return out;
    }

    public synchronized void writeLine(String file, String output)
    {
        File f = new File(file);
        FileWriter writer = null;
        try
        {
            writer = new FileWriter(f, true);
            writer.write(output + "\n");

        }
        catch (IOException e)
        {
        }
        finally
        {
            try
            {
                if (writer != null)
                    writer.close();
            }
            catch (IOException iOException)
            {
            }
        }
    }
}
