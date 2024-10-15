/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package sed.home.linkedfield880analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

/**
 *
 * @author Sed
 */
public class LinkedField880Analysis {

    public static void main(String[] args) throws IOException {
        Path currentRelativePath = Paths.get(".");
        String s = currentRelativePath.toRealPath().toString();
        //System.out.println("Current absolute path is: " + s);        
        
        File[] files = currentRelativePath.toFile().listFiles((dir, name) -> {return name.toLowerCase().endsWith(".mrc") || name.toLowerCase().endsWith(".marc");});    
        if(files.length == 0)
        {
            System.out.println("Add one or more .mrc/.marc file and rerun the app!");
            System.exit(0);
        }
        
        for(File currentFile : files)
        {
            System.out.println("Processing: " + currentFile.getCanonicalPath());
            
            StringBuilder stringBuilder = new StringBuilder();
            
            try(InputStream inputStream = new FileInputStream(currentFile))
            {
                MarcReader marcReader = new MarcStreamReader(inputStream);
                while(marcReader.hasNext())
                {
                    org.marc4j.marc.Record record = marcReader.next();
                    ControlField controlField001 = (ControlField)record.getVariableField("001");
                    List<DataField> dataFields035 = Marc4jHelper.getDataFields("035", record);

                    List<DataField> dataFields880 = Marc4jHelper.getDataFields("880", record);
                    if(!dataFields880.isEmpty())
                    {
                       // System.out.println("001 " + controlField001.getData() + "\t" + (!dataFields035.isEmpty() ? dataFields035.get(0).toString() : ""));
                        stringBuilder.append("001 ").append(controlField001.getData()).append("\t").append(!dataFields035.isEmpty() ? dataFields035.get(0).toString() : "").append(System.lineSeparator());
                    

                        dataFields880.forEach((dataField880) -> {
                            List<Subfield> subfields6 = dataField880.getSubfields('6');
                            subfields6.forEach(subfield6 ->{                        
                                String searchForField =  subfield6.getData().substring(0, 3);
                                //System.out.println("\t$6 " + subfield6.getData() + " - Does field " + searchForField + " exist? " + !record.getVariableFields(searchForField).isEmpty());
                                stringBuilder.append("\t$6 ").append(subfield6.getData()).append(" - Does field ").append(searchForField).append(" exist? ").append(!record.getVariableFields(searchForField).isEmpty()).append(System.lineSeparator());
                            });   

                        });
                        stringBuilder.append(System.lineSeparator());
                        //System.out.println();
                    }
                }
                
                String outputFile = currentFile.getCanonicalPath().replace(".mrc", ".txt").replace(".marc", ".txt");
                System.out.println("Output Text File: " + outputFile);
                System.out.println();
                Files.write(new File(outputFile).toPath(), stringBuilder.toString().getBytes());
            }
            catch (FileNotFoundException ex) 
            {
                Logger.getLogger(LinkedField880Analysis.class.getName()).log(Level.SEVERE, null, ex);
            }  
        }
    }
}
