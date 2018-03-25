
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Map;

public class Memory
{
  private String _directory;
  private HashMap<String, Integer> mem = new HashMap<String, Integer>();
  private int mem_size = 0;
  private PrintWriter output;

  // Find all values from the text file and store it into the hash
  // only lines that are 10 characters long should be accepted logically
  public Memory(String directory)
  {
    _directory = directory;
    String[] lines = null;
    try
    {
      lines = loadStrings(directory);
    }
    finally
    {
      if (lines == null)
        return;

      for (int i = 0; i < lines.length; i++)
      {
        if (lines[i].length() != 10)
          continue;

        String hash = lines[i].substring(0, 9);
        int data = int(lines[i].substring(9, 10));
        Add(hash, data);
      }
    }
  }

  // Add value to hashmap
  private void Add(String hash, int data)
  {
    mem.put(hash, data);
    mem_size++;
  }
  // Return value of hasmap with error check
  private int Get(String hash)
  {
    int value = -1;
    try
    {
      value = mem.get(hash);
    }
    finally
    {
      return value;
    }
  }

  // Saves all data of the hashmap into the same text file it was reading from (overwrite)
  private void SaveData()
  {
    if (USE_MEMORY)
    {
      String[] _Results = new String[mem_size];
      int index = 0;

      for (Map.Entry me : mem.entrySet())
      {
        _Results[index] = me.getKey().toString() + me.getValue().toString();
        index++;
      }
      println("Total Strings Saved: " + mem_size);
      _Results = sort(_Results);

      output = createWriter(_directory);
      for (int i = 0; i < _Results.length; i++)
      {
        output.println(_Results[i]);
      }
      output.flush();
      output.close();
    }
  }
}