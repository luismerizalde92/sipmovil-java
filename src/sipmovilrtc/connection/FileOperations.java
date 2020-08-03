package sipmovilrtc.connection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dsk2.json.JSONException;
import dsk2.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import logic.Accounts;
import logic.AudioManager;
import logic.FileManager;
import logic.Extensions;
import logic.Groups;
import logic.VoiceMail;
import logic.Ivrs;
import org.apache.log4j.Logger;
import static sipmovilrtc.connection.SipmovilrtcConnection.DID_INDEX;
import static sipmovilrtc.connection.SipmovilrtcConnection.logger;

public class FileOperations {
    
    private static final String path = "C:\\Users\\Luis Merizalde\\Documents\\asterisk\\sip.conf";
    
    private static final String EXTENSION_FOLDER = SipmovilrtcConnection.EXTENSIONS_FOLDER;
    
    private static final String ARI_FILE = SipmovilrtcConnection.ARI_FILE;
    private static final String COMPANY_DIRECTORY = SipmovilrtcConnection.COMPANY_DIRECTORY;
    private static final String FTP_USER = SipmovilrtcConnection.FTP_USER;
    private static final Logger logger = SipmovilrtcConnection.logger;
    private static final String PJSIP_FILE = "pjsip.conf";
    private static final String EXTENSIONS_FILE = "extensions.conf";
    private static final String IVRS_FILE = "ivrs.conf";
    private static final String VOICEMAIL_FILE = "voicemail.conf";
    private static final String QUEUES_FILE = "queues.conf";
    private static final String AUDIO_FOLDER = "sounds/";
    private static final String RECORD_FOLDER = "records/";
    private static final String CONVERTED_FOLDER = "converted/";
    private static final String DID_INDEX = SipmovilrtcConnection.DID_INDEX;
         
    public static boolean WriteNewFile(ArrayList<String> array, String path){
        try (PrintWriter pr = new PrintWriter(path)){
            for (String str: array) {
                pr.println(str);
            }
            pr.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
    public static JSONObject execute(String tx_id, JSONObject params, String operation) throws IOException {
        System.out.println(tx_id);
        System.out.println(params);
        System.out.println(operation);
        
        logger.debug("[" + tx_id + "] Call file sent.");
        
        JSONObject json = new JSONObject();        
        String[] lineArr;
        
        switch(operation) {
            case "GET_CURRENT_ACCOUNTS":
                System.out.println("FileOperation: GET_CURRENT_ACCOUNTS");
                logger.info("FileOperation: GET_CURRENT_ACCOUNTS");
                Accounts.getCurrentAccounts(path);
                break;
                
            case "DISABLE_ACCOUNT":
                System.out.println("FileOperation: DISABLE_ACCOUNT");
                logger.info("FileOperation: DISABLE_ACCOUNT");
                try {
                    System.out.println("CASE DISABLE_ACCOUNT");
                    String context_name = params.getString("context_name");
                    String account = params.getString("account");
                    String context = params.getString("context");
                    String parameter = "context";
                    String accountFile = COMPANY_DIRECTORY+context_name+"/"+PJSIP_FILE;
                    Accounts.changeParameter(account, parameter, context, accountFile);
                    break;
                } catch (Exception e) {
                }              
                break;
                
            case "CHANGE_PARAMETER":
                System.out.println("FileOperation: CHANGE_PARAMETER");
                logger.info("FileOperation: CHANGE_PARAMETER");
                try {
                    System.out.println("CASE DISABLE_ACCOUNT");
                    String context = params.getString("context");
                    String account = params.getString("account");
                    String parameter = params.getString("parameter");
                    String value = params.getString("value");
                    String accountFile = COMPANY_DIRECTORY+context+"/"+PJSIP_FILE;
                    Accounts.changeParameter(account, parameter, value, accountFile);
                    break;
                } catch (Exception e) {
                }              
                break;
                
//            case "KILL_ACCOUNT": 
//                System.out.println("FileOperation: KILL_ACCOUNT");
//                logger.info("FileOperation: KILL_ACCOUNT");
//                try {
//                    String account = params.getString("account");
//                    Accounts.killAccount(path, account);                    
//                    // guarda el archivo con las extensiones que no fueron eliminadas
//                    boolean ret = WriteNewFile(tempArray, path);                   
//                } catch (Exception e) {
//                }                
                
            case "GET_ACCOUNT_INFO":  
                System.out.println("FileOperation: GET_ACCOUNT");
                logger.info("FileOperation: GET_ACCOUNT");
                try {
                    String context = params.getString("context");
                    String account = params.getString("account");
                    String pjsipFile = COMPANY_DIRECTORY+context+"/"+PJSIP_FILE;
                    JsonArray lines = Accounts.getAccount(pjsipFile, account);
                    json.put("lines", lines); //return false;
                    
                } catch (Exception e) {
                }
                break;
//                try {
//                    json.put("response", false); //return false;
//                } catch (Exception ex) {
//                }
                
            case "CHANGE_CODECS":  
                System.out.println("FileOperation: CHANGE_CODECS");
                logger.info("FileOperation: CHANGE_CODECS");
                try {
                    String account = params.getString("account");
                    String codecs = params.getString("codecs");                    
                    ArrayList<String> tempArray = Accounts.changeCodecs(path, account, codecs);                    
                    // guarda el archivo con las extensiones que no fueron eliminadas
                    boolean ret = WriteNewFile(tempArray, path); 
                    break;
                } catch (Exception e) {
                }
                break;
                
//            case "CREATE_CONTEXT":     
//                System.out.println("FileOperation: CREATE_CONTEXT");
//                try {
//                    String context_name = params.getString("context_name"); 
//                    String context_file = EXTENSION_FOLDER + context_name +".conf";
//                    Runtime.getRuntime().exec("touch " + context_file);
//                    System.out.println("Creo archivo :"+context_file);
//                    boolean ret = Extensions.addContext(context_name, context_file); 
//                    json.put("response", ret);
//                    break;
//                } catch (Exception e) {
//                }
                
            case "VOICEMAIL_CONTEXT": 
                System.out.println("FileOperation: VOICEMAIL_CONTEXT");
                logger.info("FileOperation: VOICEMAIL_CONTEXT");
                try {
                    String context_name = params.getString("context_name");                    
//                    boolean ret = VoiceMail.addContext(context_name); 
//                    return json.put("response", ret);
                } catch (Exception e) {
                }
                break;
                
            case "ADD_VOICEMAIL": 
                System.out.println("FileOperation: ADD_VOICEMAIL");
                logger.info("FileOperation: ADD_VOICEMAIL");
                try {
                    String context_name = params.getString("context_name"); 
                    String user = params.getString("user"); 
                    String pin = params.getString("pin");
                    String name = params.getString("name"); 
                    String mail = params.getString("mail");
                    String voicemailFile = COMPANY_DIRECTORY+context_name+"/"+VOICEMAIL_FILE;
                    boolean ret = VoiceMail.addVoiceMail(context_name, user, pin, name, mail, voicemailFile); 
                    
                    json.put("response", ret);
                    break;
                } catch (Exception e) {
                }
                
            case "CHANGE_VOICEMAIL": 
                System.out.println("FileOperation: CHANGE_VOICEMAIL");
                logger.info("FileOperation: CHANGE_VOICEMAIL");
                try {
                    String context_name = params.getString("context_name"); 
                    String account = params.getString("account"); 
                    String pin = params.getString("pin");
                    String name = params.getString("name"); 
                    String mail = params.getString("mail");
                    String voicemailFile = COMPANY_DIRECTORY+context_name+"/"+VOICEMAIL_FILE;
                    boolean ret = VoiceMail.changeVoiceMail(account, pin, name, mail, voicemailFile); 
                    
                    json.put("response", ret);
                    break;
                } catch (Exception e) {
                }
                
//            case "CREATE_EXTENSION": 
//                System.out.println("FileOperation: CREATE_EXTENSION");
//                logger.info("FileOperation: CREATE_EXTENSION");
//                try {
//                    String context_name = params.getString("context_name"); 
//                    String extension = params.getString("extension");
//                    String time_ring = params.getString("time_ring");
//                    String account = params.getString("account");
//                    // creacion del directorio para almacenar las grabaciones de los usuarios
//                    String company_directory = COMPANY_DIRECTORY+context_name+"/";
//                    String records_folder = company_directory + RECORD_FOLDER + account + "/";
//                    Runtime.getRuntime().exec("mkdir " + records_folder);
//                    Runtime.getRuntime().exec("sudo chown "+FTP_USER+":"+FTP_USER+" "+ records_folder);
//                    Runtime.getRuntime().exec("sudo chmod a+rwx "+records_folder);
//                    
//                    String current_file = EXTENSION_FOLDER + context_name + ".conf";
//                    boolean ret = Extensions.createExtension(context_name, extension, time_ring, account, current_file); 
//                    
//                    json.put("response", ret);
//                    break;
//                } catch (Exception e) {
//                }
                
            case "CREATE_COMPANY": 
                System.out.println("FileOperation: CREATE_COMPANY");
                logger.info("FileOperation: CREATE_COMPANY");
                try {  
                    String context_name = params.getString("context_name");
                    String ari_password = params.getString("ari_password");
                    String sipmovil_trunk = params.getString("sipmovil_trunk");
                    // Creación del directorio para una nueva empresa
                    String company_directory = COMPANY_DIRECTORY+context_name+"/";
                    Runtime.getRuntime().exec("mkdir " + company_directory);
                    // creacion del directorio para almacenar los srchivos de audio
                    String audio_folder = company_directory + AUDIO_FOLDER;
                    Runtime.getRuntime().exec("mkdir " + audio_folder);
                    Runtime.getRuntime().exec("sudo chown "+FTP_USER+":"+FTP_USER+" "+ audio_folder);
                    Runtime.getRuntime().exec("sudo chmod a+rwx "+audio_folder);
                    // creacion del directorio para almacenar las grabaciones de los usuarios
                    String records_folder = company_directory + RECORD_FOLDER;
                    Runtime.getRuntime().exec("mkdir " + records_folder);
                    Runtime.getRuntime().exec("sudo chown "+FTP_USER+":"+FTP_USER+" "+ records_folder);
                    Runtime.getRuntime().exec("sudo chmod a+rwx "+records_folder);
                    // creacion del directorio para almacenar las conversiones
                    String converted_folder = company_directory + CONVERTED_FOLDER;
                    Runtime.getRuntime().exec("mkdir " + converted_folder);
                    Runtime.getRuntime().exec("sudo chown "+FTP_USER+":"+FTP_USER+" "+ converted_folder);
                    Runtime.getRuntime().exec("sudo chmod a+rwx "+converted_folder);
                    // Creacion del archivo donde se almacena la contraseña del cliente ARI
                    String ari_file = ARI_FILE;
                    Accounts.addARICompany(context_name, ari_password, ari_file);
                    // creacion del archivo donde se almacenaran los clientes PJSIP
                    String pjsip_file = company_directory + PJSIP_FILE;
                    Runtime.getRuntime().exec("touch " + pjsip_file);
                    Accounts.addContext(pjsip_file);
                    // creacion del archivo donde se almacenaran las extensiones
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    String ivrs_file = company_directory + IVRS_FILE;
                    Runtime.getRuntime().exec("touch " + extensions_file);
                    Runtime.getRuntime().exec("touch " + ivrs_file);
                    Extensions.addContext(context_name, extensions_file, sipmovil_trunk, ivrs_file);                    
                    // creacion del archivo donde se almacenaran los correos de las extenciones
                    String voicemail_file = company_directory + VOICEMAIL_FILE;
                    Runtime.getRuntime().exec("touch " + voicemail_file);
                    VoiceMail.addContext(context_name, voicemail_file);
                             

                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }

                
            case "EDIT_EXTENSION": 
                System.out.println("FileOperation: EDIT_EXTENSION");
                logger.info("FileOperation: EDIT_EXTENSION");
                try {
                    String old_extension = params.getString("old_extension"); 
                    String new_extension = params.getString("new_extension");
                    String context_name = params.getString("context_name");
                    String time_ring = params.getString("time_ring");
                    String account = params.getString("account");
                    String userName = params.getString("user_name");
//                    String codecs = params.getString("codecs");

                    String company_directory = COMPANY_DIRECTORY+context_name+"/";
                    // Estas lineas solo se ejecutaran para para el manejo de la extension
                    // cuando no la aplicacion no esté usando la interfaz ARI de asterisk                    
//                    String extensions_file = company_directory + EXTENSIONS_FILE;                    
//                    boolean ret = Extensions.createOrUpdateExtension(old_extension, new_extension, 
//                            time_ring, extensions_file);
                    
                    String pjsip_file = company_directory + PJSIP_FILE;
                    String parameter = "callerid";
                    String value = userName+" <"+new_extension+">";
                    boolean ret = Accounts.changeParameter(account, parameter, value, pjsip_file);
                    json.put("response", ret);
                    break;
                } catch (Exception e) {
                }
                
            case "UPDATE_VOICEMAIL_EXTENSION": 
                System.out.println("FileOperation: UPDATE_VOICEMAIL_EXTENSION");
                logger.info("FileOperation: UPDATE_VOICEMAIL_EXTENSION");
                try {  
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    // Creación del directorio para una nueva empresa
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    String extensions_file = company_directory + EXTENSIONS_FILE;          
                    Extensions.updateVoicemail(context, extension, extensions_file);                    
 
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "UPDATE_TRUNK":
                System.out.println("FileOperation: UPDATE_TRUNK");
                logger.info("FileOperation: UPDATE_TRUNK");
                try {  
                    String context = params.getString("context");
                    String sipmovil_trunk = params.getString("sipmovil_trunk");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // creacion de la extension para cuenta webrtc
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    Extensions.updateTrunk(sipmovil_trunk, extensions_file);                    
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "CREATE_USER": 
                System.out.println("FileOperation: CREATE_USER");
                logger.info("FileOperation: CREATE_USER");
                try {  
                    String account = params.getString("account");
                    String password = params.getString("password");
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    String time_ring = params.getString("time_ring");
                    String pin = params.getString("pin");
                    String name = params.getString("name"); 
                    String mail = params.getString("mail");
                    String userName = params.getString("user_name");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // Creación de la nueva cuenta webrtc
                    String accountFile = company_directory+PJSIP_FILE;
                    Accounts.addAccount(account, password, context, accountFile, extension, userName);
                    // Creacion del directorio para almacenar las grabaciones
                    // Estas lineas solo se ejecutaran para para el manejo de la extension
                    // cuando no la aplicacion no esté usando la interfaz ARI de asterisk
//                    String records_folder = company_directory + RECORD_FOLDER +"/"+account+"/";
//                    Runtime.getRuntime().exec("mkdir " + records_folder);
//                    Runtime.getRuntime().exec("sudo chown "+FTP_USER+":"+FTP_USER+" "+ records_folder);
//                    Runtime.getRuntime().exec("sudo chmod a+rwx "+records_folder);
                    // creacion de la extension para cuenta webrtc
//                    String extensions_file = company_directory + EXTENSIONS_FILE;
//                    Extensions.createOrUpdateExtension("no-ext", extension, time_ring, extensions_file);                    
                    // creacion del archivo donde se almacenaran los correos de las extenciones
//                    String voicemail_file = company_directory + VOICEMAIL_FILE;
//                    VoiceMail.addVoiceMail(context, account, pin, name, mail, voicemail_file);
       

                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "CREATE_OR_UPDATE_RINGGROUP": 
                System.out.println("FileOperation: EDIT_GROUP_EXTENSION");
                logger.info("FileOperation: CREATE_OR_UPDATE_RINGGROUP");
                try {  
                    String context = params.getString("context");
                    String old_extension = params.getString("old_extension");
                    String new_extension = params.getString("new_extension");
                    String slugName = params.getString("slugName");
                    String overflowTime = params.getString("overflowTime");
                    String groupName = params.getString("groupName");
                    String retryTime = params.getString("retryTime");
                    String timeout = params.getString("timeout");
                    String strategy = params.getString("strategy");
                    String accounts = params.getString("accounts");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // Actualizacion de la extension para para el grupo de timbrado
                    String extensions_file = company_directory+EXTENSIONS_FILE;
                    Extensions.createOrUpdateRinggroup(old_extension, new_extension,
                            slugName, groupName, overflowTime, extensions_file,
                            retryTime, timeout, strategy, accounts);                    
                    
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "DELETE_GROUP": 
                System.out.println("FileOperation: DELETE_GROUP");
                logger.info("FileOperation: DELETE_GROUP");
                try {  
                    String context = params.getString("context");
                    String slugName = params.getString("slugName");
                    String group_extension = params.getString("group_extension");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // Eliminar extension del grupo de timbrado
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    Extensions.deleteGroupExtension(group_extension, extensions_file);                    
                    // Eliminar el grupo del archivo queues.conf
                    String queues_file = company_directory + QUEUES_FILE;
                    Groups.deleteGroup(slugName, queues_file);
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
              
            case "CREATE_IVR":
                System.out.println("FileOperation: CREATE_IVR");
                logger.info("FileOperation: CREATE_IVR");
                try {  
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    String audio_name = params.getString("audio_name");
                    String ivr_name = params.getString("ivr_name");
                    String wait_time = params.getString("wait_time");
                    String ivrSlug = params.getString("ivrSlug");
                    String options = params.getString("options");
                    String timezone = params.getString("timezone");
                    Boolean data_input = params.getBoolean("data_input");
                    String action = params.getString("action");
                    Boolean call_extension = params.getBoolean("call_extension");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // creacion de la extension para el ivr
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    Extensions.createOrUpdateIVR(extension, extension, ivrSlug, extensions_file);
                    // creacion de la logica del ivr
                    String ivr_file = company_directory + IVRS_FILE;
                    String audio_path = company_directory + AUDIO_FOLDER + audio_name;
                    Ivrs.createOrUpdateIVR(ivrSlug, audio_path, ivr_name, wait_time, 
                        options, timezone, ivr_file, data_input, context, 
                        action, call_extension);
                    json.put("response", true);
                    break;
                } catch (JSONException e) {
                    System.out.println("Entro excepcion CREATE_IVR");
                    e.getStackTrace();
                    logger.info(e.getStackTrace());
                    System.out.println(Arrays.toString(e.getStackTrace()));
                }
                
            case "EDIT_IVR":
                System.out.println("FileOperation: EDIT_IVR");
                logger.info("FileOperation: EDIT_IVR");
                try {  
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    String audio_name = params.getString("audio_name");
                    String ivr_name = params.getString("ivr_name");
                    String wait_time = params.getString("wait_time");
                    String ivrSlug = params.getString("ivrSlug");
                    String options = params.getString("options");
                    String timezone = params.getString("timezone");
                    String before_extension = params.getString("before_extension");
                    Boolean data_input = params.getBoolean("data_input");
                    String action = params.getString("action");
                    Boolean call_extension = params.getBoolean("call_extension");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // edicion de la extension para el ivr
                    if (extension.equals(before_extension) == false) {
                        String extensions_file = company_directory + EXTENSIONS_FILE;
                        Extensions.createOrUpdateIVR(extension, before_extension, ivrSlug, 
                                extensions_file);
                    }
                    
                    // creacion de la logica del ivr
                    String ivr_file = company_directory + IVRS_FILE;
                    String audio_path = company_directory + AUDIO_FOLDER + audio_name;
                    Ivrs.createOrUpdateIVR(ivrSlug, audio_path, ivr_name, wait_time, 
                            options, timezone, ivr_file, data_input, context, 
                            action, call_extension);
                    json.put("response", true);
                    break;
                } catch (JSONException e) {
                    System.out.println("Entro excepcion CREATE_IVR");
                    e.getStackTrace();
                    logger.info(e.getStackTrace());
                    System.out.println(Arrays.toString(e.getStackTrace()));
                }
                
            case "DELETE_IVR": 
                System.out.println("FileOperation: DELETE_IVR");
                logger.info("FileOperation: DELETE_IVR");
                try {  
                    Gson gson = new Gson();
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    String ivrSlug = params.getString("ivrSlug");
                    String audios = params.getString("audios");
                    JsonArray audiosArray = gson.fromJson(audios, JsonArray.class);
                    String company_directory = COMPANY_DIRECTORY+context+"/";                    
                    // Eliminar extension del grupo de timbrado
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    Extensions.deleteGroupExtension(extension, extensions_file);                    
                    // Eliminar el ivr del archivo ivr.conf
                    String ivrs_file = company_directory + IVRS_FILE;
                    Ivrs.deleteIvr(ivrSlug, ivrs_file);
                    // Eliminar archivos de audio
                    String audio_folder = COMPANY_DIRECTORY+context+"/"+AUDIO_FOLDER;
                    for (int i = 0; i < audiosArray.size(); i++) {
                        String element = audiosArray.get(i).getAsString();
                        System.out.println("Eliminando audio: "+element);
                        logger.info("Eliminando audio: "+element); 
                        File file = new File(audio_folder+element); 
                        file.delete();
//                        Runtime.getRuntime().exec("sudo rm -f" + audio_folder+element);
                        System.out.println("Paso elimino audio: "+audio_folder+element);
                    }
                    
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "UPDATE_TIMEZONE": 
                System.out.println("FileOperation: UPDATE_TIMEZONE");
                logger.info("FileOperation: UPDATE_TIMEZONE");
                try {  
                    String ivrs = params.getString("ivrs");
                    String timezone = params.getString("timezone");
                    Gson gson = new Gson();
                    JsonArray ivrArray = gson.fromJson(ivrs, JsonArray.class);
                    for (JsonElement pa : ivrArray) {
                        JsonObject element = pa.getAsJsonObject();
                        String context = element.get("company__slug_name").getAsString();
                        String ivrSlug = element.get("slug_name").getAsString();
                        String company_directory = COMPANY_DIRECTORY+context+"/";
                        String ivr_file = company_directory + IVRS_FILE;
                        Ivrs.updateIvr(ivrSlug, timezone, ivr_file);
                    }
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "UPDATE_DIDS": 
                System.out.println("FileOperation: UPDATE_DIDS");
                logger.info("FileOperation: UPDATE_DIDS");
                try {  
                    String context = params.getString("context");
                    String old_dids = params.getString("old_dids");
                    String new_dids = params.getString("new_dids");
                    String extension = params.getString("extension");
                    String extension_file = DID_INDEX;
                    Extensions.deleteDIDs(old_dids, extension_file);                    
                    // escribir los nuevos DIDs con la extension pertinente
                    Extensions.addDIDs(new_dids, extension, context, 
                            extension_file);

                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "NEW_DIDS": 
                System.out.println("FileOperation: NEW_DIDS");
                logger.info("FileOperation: NEW_DIDS");
                try {  
                    String data = params.getString("data");
                    Gson gson = new Gson();
                    JsonArray didArray = gson.fromJson(data, JsonArray.class); 
                    String extension_file = DID_INDEX;
                    Extensions.newDIDs(didArray, extension_file);
//                    for (JsonElement pa : didsArray) {
//                        JsonObject element = pa.getAsJsonObject();
//                        String context = element.get("context").getAsString();
//                        JsonArray didArray = element.get("dids").getAsJsonArray();
//                        String extension_file = DID_INDEX;
//                        Extensions.addDIDs(didArray, context, extension_file);
//                    }
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "DID_OPERATION": 
                System.out.println("FileOperation: DID_OPERATION");
                logger.info("FileOperation: DID_OPERATION");
                try {  
                    String context = params.getString("context");
                    String did = params.getString("did");
                    String extension = params.getString("extension");
                    String operationx = params.getString("operation");
                    String extension_file = DID_INDEX;
                    Extensions.didOperation(context, did, extension, operationx, 
                            extension_file);
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "DELETE_USER": 
                System.out.println("FileOperation: DELETE_USER");
                logger.info("FileOperation: DELETE_USER");
                try {  
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    String account = params.getString("account");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    //eliminar extension
                    String extension_file = company_directory + EXTENSIONS_FILE;
                    Extensions.deleteGroupExtension(extension, extension_file);
                    //eliminar cuenta pjsip
                    String pjsip_file = company_directory + PJSIP_FILE;
                    Accounts.killAccount(account, pjsip_file);
                    //eliminar cuenta voicemail
                    String voicemail_file = company_directory + VOICEMAIL_FILE;
                    VoiceMail.deleteVoicemail(account, voicemail_file);
                    
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "DELETE_RECORD": 
                System.out.println("FileOperation: DELETE_RECORD");
                logger.info("FileOperation: DELETE_RECORD");
                try {  
                    String context = params.getString("context");
                    String filename = params.getString("filename");
                    String account = params.getString("account");
                    String record_folder = COMPANY_DIRECTORY+context+"/";
                    record_folder = record_folder+RECORD_FOLDER+account+"/";
                    String audio_file = record_folder+filename;
                    //eliminar extension
                    Runtime.getRuntime().exec("rm " + audio_file);
                    
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "GET_COMPANIES_STORAGE": 
                System.out.println("FileOperation: GET_COMPANIES_STORAGE");
                logger.info("FileOperation: GET_COMPANIES_STORAGE");
                try { 
                    JsonArray response = new JsonArray();
                    response = AudioManager.getCompaniesStorage();                    
                    json.put("companies", response);
                    break;
                } catch (Exception e) {
                }
                
            case "GET_COMPANY_STORAGE": 
                System.out.println("FileOperation: GET_COMPANY_STORAGE");
                logger.info("FileOperation: GET_COMPANY_STORAGE");
                try {                     
                    String record_path = params.getString("record_path");
                    JsonObject response = new JsonObject();
                    response = AudioManager.getCompanytInfo(record_path);                    
                    json.put("company", response);
                    break;
                } catch (Exception e) {
                }
                
            case "GET_ACCOUNT_STORAGE": 
                System.out.println("FileOperation: GET_ACCOUNT_STORAGE");
                logger.info("FileOperation: GET_ACCOUNT_STORAGE");
                try { 
                    String record_path = params.getString("record_path");
                    String account_name = params.getString("account");
                    JsonObject response = new JsonObject();
                    response = AudioManager.getAccountInfo(record_path, account_name);                    
                    json.put("company", response);
                    break;
                } catch (Exception e) {
                }
                
            case "GET_COMPANY_RECORDS": 
                System.out.println("FileOperation: GET_COMPANY_RECORDS");
                logger.info("FileOperation: GET_COMPANY_RECORDS");
                try {                     
                    String record_path = params.getString("record_path");
                    JsonObject response = new JsonObject();
                    response = AudioManager.getCompanyRecords(record_path);                    
                    json.put("company", response);
                    break;
                } catch (Exception e) {
                }
                
            case "CONVERT_AUDIO": 
                System.out.println("FileOperation: CONVERT_AUDIO");
                logger.info("FileOperation: CONVERT_AUDIO");
                try {                     
                    String context = params.getString("context");
                    logger.info("context: "+context);
                    String fileName = params.getString("fileName");
                    logger.info("context: "+fileName);
                    String account = params.getString("account");
                    logger.info("context: "+account);
                    String destinationFormat = params.getString("destinationFormat");
                    logger.info("context: "+destinationFormat);
                    
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    String audio_path = company_directory+RECORD_FOLDER+account+"/"+fileName;
                    logger.info("audio_path: "+audio_path);
                    
                    String converted_directory = company_directory+CONVERTED_FOLDER;
                    String converted_file = fileName.replace(AudioManager.fileExtension(fileName), destinationFormat);
                    String converted_path = converted_directory + converted_file;
                    logger.info("converted_path: "+converted_path);

//                    String command = "asterisk -rx 'file convert " + audio_path + " " + converted_path+"'";
//                    String[] command = {"asterisk", "-rx", "'file convert " + audio_path + " " + converted_path+"'"};
                    String[] command = {"asterisk","-rx","\"core show license\""};
                    logger.info("command: "+Arrays.toString(command));
                    Process proc = Runtime.getRuntime().exec(command);
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
                    String s = null;
                    logger.info("antes de while de impresion");
                    while ((s = stdInput.readLine()) != null) {
                        logger.info(s);
                    }                    
                    
                    logger.info("asterisk -rx 'file convert " + audio_path + " " + converted_path+"'");
                    json.put("file_path", converted_directory);
                    break;
                } catch (JSONException | IOException e) {
                    logger.info("entro excepcion CONVERT_AUDIO");
                    logger.info(e);
                    logger.info(e.getStackTrace());
                }
                
            case "COMPANY_STORAGE": 
                System.out.println("FileOperation: COMPANY_STORAGE");
                logger.info("FileOperation: COMPANY_STORAGE");
                try {   
                    String context = params.getString("context");
                    JsonObject response = FileManager.getCompanyInfo(context,
                            COMPANY_DIRECTORY, RECORD_FOLDER);
                    json.put("company", response);
                    break;
                } catch (Exception e) {
                }
                
            case "RELEASE_STORAGE": 
                System.out.println("FileOperation: RELEASE_STORAGE");
                logger.info("FileOperation: RELEASE_STORAGE");
                try {   
                    String context = params.getString("context");
                    long storageLimit = params.getLong("storage_limit");
                    JsonObject response = FileManager.deleteCompanyFiles(context,
                            COMPANY_DIRECTORY, RECORD_FOLDER, storageLimit);
                    json.put("company", response);
                    break;
                } catch (Exception e) {
                }
                
            default:
                try {
                    json.put("response", false); //return false;
                } catch (JSONException ex) {
                }
        }
        
        
        try {
            json.put("response", true); //return false;
            
        } catch (Exception ex) {
        }
        return json;
    }
    
}
