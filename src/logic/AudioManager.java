/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
//import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.io.InputStreamReader;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.logging.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.log4j.Logger;
import sipmovilrtc.connection.SipmovilrtcConnection;

/**
 *
 * @author Usuario
 */
public class AudioManager {
    
    private static final String COMPANY_DIRECTORY = SipmovilrtcConnection.RECORDS_DIRECTORY;
    private static final Logger LOGGER = SipmovilrtcConnection.logger;
    private static final String RECORDS_FOLDER = "records/";
    
    public static JsonArray getCompaniesStorage() throws IOException, UnsupportedAudioFileException{
        System.out.println("From AudioManager.getCompaniesStotage");
        LOGGER.info("From AudioManager.getCompaniesStotage");
        
        JsonArray companies = new JsonArray();
        
        String path  = COMPANY_DIRECTORY;
        File file = new File(path);
        String[] names = file.list();

        // Recorre la carpeta /etc/asterisk/companies/
        for(String name : names){
            if (new File(path + name).isDirectory() && name.startsWith("company_")){
                LOGGER.info("compañia: " + name);
                JsonObject company = new JsonObject();
                company.addProperty("company", name);
                long acum_company = 0;
                JsonArray accounts = new JsonArray();
                //company.addProperty("accounts", name);
                File sub_file = new File(path + name);
                String[] sub_names = sub_file.list();

                // Recorre la carpeta .../companies/company-xxx
                for (String sub_name : sub_names){
                    if (new File(path + name+"/"+sub_name).isDirectory() && sub_name.equals("records")){                        
                        String record_path = path + name+"/"+sub_name+"/";
                                               
                        JsonObject accounts_objects = new JsonObject();
                        accounts_objects = getCompanytInfo(record_path);
                        accounts = accounts_objects.get("accounts").getAsJsonArray();//getCompanytInfo(record_path);
                        acum_company = accounts_objects.get("size").getAsLong();
                    }                    
                }// fin recorrido cuentas de empresa
                company.add("accounts", accounts);
                company.addProperty("size", acum_company);
                LOGGER.info(" company size:" + acum_company);
                companies.add(company);
            }     

        }
        return companies;
    }
    
    public static JsonObject getCompanytInfo(String record_path) throws UnsupportedAudioFileException, IOException{
        LOGGER.info("From AudioManager.getCompanytInfo");
        LOGGER.info("record_path: " + record_path);
        JsonObject response = new JsonObject();
        JsonArray accounts = new JsonArray();
        File record_file = new File(record_path);
        String[] account_names = record_file.list(); 
        long acum_company = 0;
        for(String account_name : account_names){
            JsonObject account = new JsonObject();
            LOGGER.info("cuenta: " + account_name);
            account = getAccountInfo(record_path, account_name);
            acum_company = acum_company + account.get("size").getAsLong();
            accounts.add(account);
        } // fin recorrido grabaciones
        response.add("accounts", accounts);
        response.addProperty("size", acum_company);
        return response;
    }
    
    public static JsonObject getCompanyRecords(String record_path) throws UnsupportedAudioFileException, IOException{
        LOGGER.info("From AudioManager.getCompanyRecords");
        LOGGER.info("record_path: " + record_path);
        JsonObject response = new JsonObject();
        JsonArray companyRecords = new JsonArray();
        File record_file = new File(record_path);
        String[] account_names = record_file.list(); 
        long acum_company = 0;
        for(String account_name : account_names){
            JsonObject account = new JsonObject();
            LOGGER.info("cuenta: " + account_name);
            account = getAccountInfo(record_path, account_name);
            JsonArray accountArray = new JsonArray();
            accountArray = account.get("records").getAsJsonArray();
            acum_company = acum_company + account.get("size").getAsLong();
            for (JsonElement pa : accountArray) {
                companyRecords.add(pa);
            }
        }
        response.add("records", companyRecords);
        response.addProperty("size", acum_company);
        return response;
    }
    
    public static JsonObject getAccountInfo(String record_path, String account_name) throws UnsupportedAudioFileException, IOException{
        LOGGER.info("From AudioManager.getAccountInfo");
        LOGGER.info("record_path: " + record_path + " account_name: " + account_name);
        JsonObject account = new JsonObject();
        String account_path = record_path+"/"+account_name+"/";
        File account_file = new File(account_path);
        account.addProperty("account", account_name);
        JsonArray records = new JsonArray();
        String[] account_records = account_file.list();
        long acum_records = 0;
        for(String account_record : account_records){
            JsonObject record = new JsonObject();
            record.addProperty("name", account_record);
            File audio_file = new File(account_path+"/"+account_record);
            String extension = fileExtension(account_record);
            long audioFileLength = audio_file.length();
            record.addProperty("length", audioFileLength);
            long audioFileDate = audio_file.lastModified();
            record.addProperty("date", audioFileDate);
            record.addProperty("date_format", dateFormat(audioFileDate));
            acum_records = acum_records + audioFileLength;
            if (extension.equals("wav")) {            
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audio_file);
                AudioFormat format = audioInputStream.getFormat();            
                int frameSize = format.getFrameSize();
                float frameRate = format.getFrameRate();
                record.addProperty("rate", frameRate);
                float durationInSeconds = (audioFileLength / (frameSize * frameRate));
                Integer durationSeconds = Math.round(durationInSeconds);
                record.addProperty("duration", durationSeconds);  
            }
            record.addProperty("account", account_name);
            records.add(record);
        }
        
        account.add("records", records);
        account.addProperty("size", acum_records);
        LOGGER.info(" account size:" + acum_records);
        return account;
    }
    
    private static String dateFormat(long long_date){
        Date date=new Date(long_date);
        SimpleDateFormat df2 = new SimpleDateFormat("yy/MM/dd HH:mm:ss.SSS");
        return df2.format(date);
    }
    
    public static String fileExtension(String filename){
        Integer lastIndex = filename.lastIndexOf(".") + 1;
        return filename.substring(lastIndex);
    }
    
}
