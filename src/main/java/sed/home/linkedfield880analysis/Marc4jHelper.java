/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sed.home.linkedfield880analysis;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

/**
 *
 * @author blj0011
 */
public class Marc4jHelper {       
    static public List<Record> getMarcRecordsFromFile(String fileName)
    {
        List<Record> records = new ArrayList();
        
        try 
        {
            InputStream in = new FileInputStream(fileName);
            MarcReader reader = new MarcStreamReader(in);
            while(reader.hasNext())
            {
                records.add(reader.next());
            }
            
            System.out.println("Number of records read: " + records.size());
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(Marc4jHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return records;
    }
    
    /**
     * StandardCharsets.UTF_8 Only!
     * 
     * @param marcString
     * @return 
     */
    static public List<Record> getMarcRecordsFromString(String marcString)
    {
        List<Record> records = new ArrayList();
        
        InputStream in = new ByteArrayInputStream(marcString.getBytes(StandardCharsets.UTF_8));
        MarcReader reader = new MarcStreamReader(in);
        while(reader.hasNext())
        {
            records.add(reader.next());
        }
        
        return records;
    }    
    
    /**
     * Only removes DataFields that start with the given character.
     * @param digit
     * @param record 
     */
    static public void removeDataFieldsThatStartWith(char digit, Record record)
    {
        List<VariableField> fieldsToRemove = new ArrayList();
        
        
        if (!Character.isDigit(digit)) {
            System.err.println("RemoveVariableFieldsThatStartWith Method Error: Character input must be a digit");
        }
        else {    
            
            int i = 0;
            if(digit == '0')
            {
                i = 10;
            }            
            while (i < 100) 
            {                
                String paddedDigit = String.format("%02d", i);
                final String tag = digit + paddedDigit;
                fieldsToRemove.addAll(record.getVariableFields(tag));
                
                i++;
            }
            
        }
        
        fieldsToRemove.stream().forEach((field) -> {
            record.removeVariableField(field);
        });
    }
    
    /**
  * Copies the leader and the variablefields of record record and returns a
  * duplicate record.This copy is not a 100% copy of the original Record. It
 does not copy the id, maxSeverity, or type. It does copy the leader, the
 controlfields, the datafields, and the errors.
  *
     * @param record
  * @return a new Record Identical to record Record
  */
    static public Record cloneRecord(Record record)
    {
        Record outputRecord = MarcFactory.newInstance().newRecord();
        outputRecord.setLeader(record.getLeader());

        for (VariableField variableField : record.getControlFields()) {
            outputRecord.addVariableField(variableField);
        }

        for (VariableField variableField : record.getDataFields()) {
            outputRecord.addVariableField(variableField);
        }

        outputRecord.addErrors(record.getErrors());

        return outputRecord;
    }
    
    /**
   * Sorts the variablefields by tag.
   *
     * @param record
   */
    static public void sortVariableFields(Record record)
    {
        Record tempRecord = cloneRecord(record);
        List<ControlField> controlFields = tempRecord.getControlFields();
        List<DataField> dataFields = tempRecord.getDataFields();
      
        Collections.sort(controlFields);
        Collections.sort(dataFields);
      
        record.getVariableFields().forEach((t) -> {
            record.removeVariableField(t);
        });
        
        controlFields.forEach((t) -> {
            record.addVariableField(t);
        });
        dataFields.forEach((t) -> {
            record.addVariableField(t);
        });
    }
    
    static public void removeAllXXXFields(String field, Record record)
    {
        List<VariableField> fieldsToRemove = record.getVariableFields(field);
                fieldsToRemove.forEach((item) -> {
                    record.removeVariableField(item);
                });
    }  
    
    static public void writeMarcRecordToFile(String fileName, Record record)
    {
        try 
        {
            OutputStream bibOutputStream = new FileOutputStream(fileName);
            MarcWriter bibWriter = new MarcStreamWriter(bibOutputStream);
            bibWriter.write(record);
            bibWriter.close();
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(Marc4jHelper.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    static public void writeMarcRecordsToFile(String fileName, List<Record> records)
    {
        try 
        {
            OutputStream bibOutputStream = new FileOutputStream(fileName);
            MarcWriter bibWriter = new MarcStreamWriter(bibOutputStream);
            records.forEach((t) -> {bibWriter.write(t);});
            bibWriter.close();
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(Marc4jHelper.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    static public void removeAllSubfields(VariableField field)
    {
        DataField tempDataField = (DataField)field;
        List<Subfield> subfieldsToRemove = new ArrayList(tempDataField.getSubfields());
        for(int i = 0; i < subfieldsToRemove.size(); i++)
        {
            tempDataField.removeSubfield(subfieldsToRemove.get(i));
        }
    }
    
    static public List<DataField> getDataFields(String tag, Record record)            
    {
        List<DataField> dataFields = new ArrayList();
        
        record.getVariableFields(tag).forEach((variableField) -> {
            dataFields.add((DataField)variableField);
        });
        
        return dataFields;
    }
    
    static public List<ControlField> getControlFields(String tag, Record record)            
    {
        List<ControlField> controlFields = new ArrayList();
        
        record.getVariableFields(tag).forEach((controlField) -> {
            controlFields.add((ControlField)controlField);
        });
        
        return controlFields;
    }
    
    static public boolean has035(Record record)
    {
        return record.getVariableField("035") != null;
    }
    
    static public boolean has035a(Record record)
    {
        if(!has035(record))
            return false;
        
        DataField dataField035 = (DataField)record.getVariableField("035");
        
        return dataField035.getSubfield('a') != null;
    }
    
    static public boolean findLinkedFieldSubfield6(Record record)
    {
        //List<DataField> dataFields773 = cloneBib.getVariableFields("773").stream().map(t -> (DataField) t).collect(Collectors.toList());
    }
}