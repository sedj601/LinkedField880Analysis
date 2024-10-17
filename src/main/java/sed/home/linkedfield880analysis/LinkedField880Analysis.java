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
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
            
            StringBuilder stringBuilderGoodLink = new StringBuilder();
            StringBuilder stringBuilderBadLink = new StringBuilder();
            
            try(InputStream inputStream = new FileInputStream(currentFile))
            {
                MarcReader marcReader = new MarcStreamReader(inputStream);
                while(marcReader.hasNext())
                {
                    org.marc4j.marc.Record record = marcReader.next();
                    Map.Entry<String, String> returnStrings = handleLinkingFields(record);
                    stringBuilderGoodLink.append(returnStrings.getKey());
                    stringBuilderBadLink.append(returnStrings.getValue());
                }
                
                print(currentFile, stringBuilderGoodLink, stringBuilderBadLink);
            }
            catch (FileNotFoundException ex) 
            {
                Logger.getLogger(LinkedField880Analysis.class.getName()).log(Level.SEVERE, null, ex);
            }  
        }
    }
    
    public static Map.Entry<String, String> handleLinkingFields(org.marc4j.marc.Record record)
    {        
        StringBuilder stringBuilderGoodLink = new StringBuilder();
        StringBuilder stringBuilderBadLink = new StringBuilder();
        
        for(DataField dataField : record.getDataFields())
        {
            if(!dataField.getSubfields('6').isEmpty())
            {
                ControlField controlField001 = (ControlField)record.getVariableField("001");
                List<DataField> dataFields035 = Marc4jHelper.getDataFields("035", record);
        
                for(Subfield subfield6 : dataField.getSubfields('6'))
                {                    
                    String linkingTag = subfield6.getData().split("-")[0];
                    String occurrenceNumber = subfield6.getData().split("-")[1].substring(0, 2);                
                    
                    boolean linkingTagExist = !record.getVariableFields(linkingTag).isEmpty();
                    if(linkingTagExist)
                    {
                        if(!Marc4jHelper.findLinkedFieldSubfield6(record, dataField.getTag(), linkingTag, occurrenceNumber).isBlank())
                        {
                            stringBuilderGoodLink.append("001 ").append(controlField001.getData()).append("\t").append(!dataFields035.isEmpty() ? dataFields035.get(0).toString() : "");
                            stringBuilderGoodLink.append("\t").append(dataField.getTag()).append("$6 ").append(subfield6.getData()).append(" ");
                            stringBuilderGoodLink.append("\t").append(linkingTag).append("$6 ").append(Marc4jHelper.findLinkedFieldSubfield6(record, dataField.getTag(), linkingTag, occurrenceNumber));
                            stringBuilderGoodLink.append(System.lineSeparator()); 
                        }
                        else
                        {
                            stringBuilderBadLink.append("001 ").append(controlField001.getData()).append("\t").append(!dataFields035.isEmpty() ? dataFields035.get(0).toString() : "");
                            stringBuilderBadLink.append("\t").append(dataField.getTag()).append("$6 ").append(subfield6.getData()).append(" ");
                            stringBuilderBadLink.append("COULD NOT FIND $6 LINK SUBFIELD!");
                            stringBuilderBadLink.append(System.lineSeparator()); 
                        }                    
                    }
                    else
                    {
                        stringBuilderBadLink.append("001 ").append(controlField001.getData()).append("\t").append(!dataFields035.isEmpty() ? dataFields035.get(0).toString() : "");
                        stringBuilderBadLink.append("\t").append(dataField.getTag()).append("$6 ").append(subfield6.getData()).append(" ");
                        stringBuilderBadLink.append("\tCOULD NOT FIND LINKING TAG FIELD ").append(linkingTag).append("!");
                        stringBuilderBadLink.append(System.lineSeparator()); 
                    }                     
                }
            }
        }
        
        return new AbstractMap.SimpleEntry<>(stringBuilderGoodLink.toString(), stringBuilderBadLink.toString());
    }   
    
    public static void print(File currentFile, StringBuilder stringBuilderGoodLink, StringBuilder stringBuilderBadLink)
    {
        try 
        {
            String outputFileGood = currentFile.getCanonicalPath().replace(".mrc", "_good.txt").replace(".marc", "_good.txt");
            System.out.println("Output Text File: " + outputFileGood);
            System.out.println();
            Files.write(new File(outputFileGood).toPath(), stringBuilderGoodLink.toString().getBytes());
            
            String outputFileBad = currentFile.getCanonicalPath().replace(".mrc", "_bad.txt").replace(".marc", "_bad.txt");
            System.out.println("Output Text File: " + outputFileBad);
            System.out.println();
            Files.write(new File(outputFileBad).toPath(), stringBuilderBadLink.toString().getBytes());
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(LinkedField880Analysis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
