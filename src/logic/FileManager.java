/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.log4j.Logger;
import sipmovilrtc.connection.SipmovilrtcConnection;

/**
 *
 * @author Usuario
 */
public class FileManager {
    
    private static final Logger LOGGER = SipmovilrtcConnection.logger;
    
    public static JsonObject getCompanyInfo(String companySlug, String companyDirectory,
            String recordsFolder) throws IOException{
        JsonObject response = inspectCompany(companySlug, companyDirectory, recordsFolder, false, true);
        return response;
    }
    
    public static JsonObject deleteCompanyFiles(String companySlug, String companyDirectory,
            String recordsFolder, long limitStorage) throws IOException{
        System.out.println("From FileManager.deleteCompanyFiles");
        LOGGER.info("From FileManager.deleteCompanyFiles");
        
        JsonObject response = new JsonObject();
        
        JsonObject resp = inspectCompany(companySlug, companyDirectory, recordsFolder, true, true);
        
        // espacio de ocupado por las grabaciones
        long occupiedSpace = resp.get("size").getAsLong();
        
        // listado de los archivos de las grabaciones
        JsonArray beforeOrder = resp.getAsJsonArray("records");
        
        // ordenar las grabaciones desde la fecha mas antigua
        JsonArray afterOrder = orderRecords(beforeOrder, "date", false);
      
        
        if (occupiedSpace > limitStorage){
            LOGGER.info("La compañia "+companySlug+" superó la cuota de almacenamiento");
            
            JsonArray deletedFiles = new JsonArray();            
            deletedFiles = getCandidateFiles(afterOrder, limitStorage, occupiedSpace);

            for (int i = 0; i < deletedFiles.size(); i++) {
                JsonObject record = deletedFiles.get(i).getAsJsonObject();
                String recordpath = record.get("path").getAsString();
                LOGGER.info("archivo a eliminar "+recordpath);
                System.out.println("archivo a eliminar "+recordpath);
                Runtime.getRuntime().exec("rm -f " + recordpath);
                LOGGER.info("Se elimino el archivo "+recordpath);
                System.out.println("Se elimino el archivo "+recordpath);
                occupiedSpace -= record.get("size").getAsLong();                
            }
            response.add("deleted_files", deletedFiles);
        }else{
            LOGGER.info("La compañia "+companySlug+" no ha superado la cuota de almacenamiento");
        }
        
        
        response.addProperty("storage", occupiedSpace);
        return response;
    }
    
    
    
    private static JsonObject inspectCompany( String companyName, String companyDirectory,
            String recordsFolder, Boolean appendRecords, Boolean accountData) throws IOException{
        System.out.println("From FileManager.inspectCompany: "+companyName +" in "+companyDirectory);
        LOGGER.info("From FileManager.inspectCompany: "+companyName +" in "+companyDirectory);
        JsonObject response = new JsonObject();
        Path companyPath  = Paths.get(companyDirectory,companyName);
        System.out.println(companyPath);
        File file = new File(companyPath.toString());
        String[] accountFolders = file.list();
        Integer recordCount = 0;
        long accountSize = 0;
        
        // stoare record data for each account
        JsonArray companyRecordsArray = new JsonArray();
        JsonArray accountsArray = new JsonArray();
        
        for(String name : accountFolders){
           Path accountPath  = Paths.get(companyDirectory,companyName,name,recordsFolder); //recordsFolder
           System.out.println("inpect folder "+accountPath);
           if (new File(accountPath.toString()).isDirectory()){
               JsonObject resp = inspectAcccount(accountPath.toString(), name, 
                       appendRecords);
               accountSize += resp.get("size").getAsLong();
               recordCount += resp.get("count").getAsInt();
               
               if (appendRecords){
                   JsonArray recordsArray = resp.getAsJsonArray("records");                 
                   for (int i = 0; i < recordsArray.size(); i++) {
                       JsonElement record = recordsArray.get(i);
                       JsonObject rr = recordsArray.get(i).getAsJsonObject();
                       companyRecordsArray.add(record);
                   }                   
               }
               
               if (accountData) {
                   accountsArray.add(resp);
               }
               
           }
        }
        
        response.addProperty("size", accountSize);
        response.addProperty("count", recordCount);
        if (appendRecords) {
            response.add("records", companyRecordsArray);
        }
        if (accountData) {
            response.add("accounts", accountsArray);
        }
        
        return response;
    }
    
    private static JsonObject inspectAcccount( String accountFolder, 
        String accountNumber, Boolean appendRecords) throws IOException{
        
        System.out.println("inspectAcccount: "+accountNumber+" in folder: "+accountFolder);
        
        JsonObject response = new JsonObject();
        File file = new File(accountFolder);
        String[] recordfiles = file.list();
        Integer recordCount = 0;
        long accountSize = 0;        
        JsonArray recordsArray = new JsonArray();
        
        for(String name : recordfiles){
            System.out.println("record nbame: "+name);    
            Path recordPath = Paths.get(accountFolder,name);
            if (new File(recordPath.toString()).isFile()){
                File record = new File(recordPath.toString());
                if (record.length() == 0){
                    // delete filete if size is equal to zero bytes
                    Runtime.getRuntime().exec("rm " + recordPath.toString());
                }else{
                    accountSize += record.length();
                    recordCount += 1;
                    if (appendRecords){
                        JsonObject recordObject = new JsonObject();
                        recordObject.addProperty("file", name);
                        recordObject.addProperty("path", recordPath.toString());
                        recordObject.addProperty("size", record.length());
                        recordObject.addProperty("date", record.lastModified());
                        recordObject.addProperty("account", accountNumber);
                        recordsArray.add(recordObject);
                    }
                }
                                
            }
        }
        response.addProperty("account", accountNumber);
        response.addProperty("size", accountSize);
        response.addProperty("count", recordCount);
        if (appendRecords){
            response.add("records", recordsArray);
        }
        return response;
    }
    
    private static JsonArray orderRecords(JsonArray recordsArray, String keyName
            , Boolean descOrder){
        
        List<JsonObject> recordList = new ArrayList<JsonObject>();
        
        // store all jsonelements in arraylist
        for (int i = 0; i < recordsArray.size(); i++)
            recordList.add(recordsArray.get(i).getAsJsonObject());  
        
        SortBasedOnMessageId ordenator = new SortBasedOnMessageId();
        ordenator.setKeyName(keyName);
        Collections.sort(recordList, ordenator);       
        
        JsonArray orderArray = new JsonArray();       
        
        if (descOrder){
            for (int i = recordList.size()-1; i >= 0; i--) {
                orderArray.add(recordList.get(i));
            }            
        }else{
            for (int i = 0; i < recordList.size(); i++) {
                orderArray.add(recordList.get(i));
            }
        }
        
        return orderArray;
    }
    
    private static JsonArray getCandidateFiles(JsonArray audioFiles, 
            long companyStorage, long occupiedSpace){
        System.out.println("entro funcion getCandidateFiles");
        System.out.println(audioFiles.toString());
        long size = 0;
        
        JsonArray deletedFiles = new JsonArray();
        for (int i = 0; i < audioFiles.size(); i++) {
            JsonObject record = audioFiles.get(i).getAsJsonObject();
            occupiedSpace -= record.get("size").getAsLong();
            deletedFiles.add(record);
            size += record.get("size").getAsLong();
            if(occupiedSpace < companyStorage){
                System.out.println("borrado alcanzado: "+String.valueOf(size));    
                break;
            }
        }
        System.out.println("paso for archivos a eliminar "+String.valueOf(deletedFiles.size()));
        return deletedFiles;
    }
    
    private static class SortBasedOnMessageId implements Comparator<JsonObject> {
        
        private String keyName;
        
        private void setKeyName(String name){
            keyName = name; 
        }
        
        @Override
        public int compare(JsonObject elementA, JsonObject elementB) {
            long compare = 0;
            long keyA = elementA.get(keyName).getAsLong();
            long keyB = elementB.get(keyName).getAsLong();
            compare = Long.compare(keyA, keyB);
            return (int)compare;
        }
    }
    
}
