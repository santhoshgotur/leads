import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by santhosh.gotur on 9/22/17.
 */
public class LeadsParser {

    /***
     *  LeadIds class handles email duplicates
     */
    static Logger logger;
    Set<Leads> leads;

    public LeadsParser(){
        logger = Logger.getLogger(LeadsParser.class.getName());
        leads = new HashSet<>();
    }

    public String toString(){
        JSONArray jsonArray = new JSONArray();
        JSONObject record = null;
        for(Leads lead:leads){
                record = new JSONObject();
                record.put("_id",lead._id);
                record.put("email",lead.email);
                record.put("firstName",lead.firstName);
                record.put("lastName",lead.lastName);
                record.put("address",lead.address);
                record.put("entryDate",lead.entryDate.toString());
                record.put("entryDate",new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+hh:mm").format(lead.entryDate));

                jsonArray.add(record);
        }

        JSONObject leadsJson = new JSONObject();
        leadsJson.put("leads", jsonArray);
        return leadsJson.toJSONString();
    }

    public class Leads {

        String _id;
        String email;
        String firstName;
        String lastName;
        String address;
        Date entryDate;
        
        public String getid(){
            return _id;
        }

        public String getEmail(){
            return email;
        }

        public String getFirstName(){
            return firstName;
        }

        public String getLastName(){
            return lastName;
        }

        public String getAddress(){
            return address;
        }

        public Date getEntryDate(){
            return entryDate;
        }

        public Leads(){
        }

        public Leads(String _id, String email, String firstName, String lastName, String address, Date entryDate) {
            this._id = _id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.address = address;
            this.entryDate = entryDate;
       }

        @Override
        public String toString() {
            return this._id + ":" + this.email + ":" + this.firstName + ":" + this.lastName + ":" + entryDate.toString();
        }

        /**
         * removes email dupes
         */

        @Override
        public int hashCode() {
            return _id.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Leads) {
                Leads lead = (Leads) o;
                return (lead._id.equals(this._id));
            }
            return false;
        }
    }

    /***
     *  LeadEmails class handles email duplicates
     */
    
    class LeadEmails extends Leads {
        
        public LeadEmails(String _id, String email, String firstName, String lastName, String address, Date entryDate) {
            this._id = _id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.address = address;
            this.entryDate = entryDate;
        }

        /**
         * removes email dupes
         */

        @Override
        public int hashCode() {
            return email.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof LeadEmails) {
                LeadEmails lead = (LeadEmails) o;
                return (lead.email.equals(this.email));
            }
            return false;
        }
    }

    public Set<Leads> parse(String filename) throws IOException, ParseException {
        
        String records = new String(Files.readAllBytes(Paths.get(filename)));
        logger.log(Level.INFO, "parsing input json file");

        JSONObject j = (JSONObject) JSONValue.parse(records);
        JSONArray recordsArray = (JSONArray) j.get("leads");
        
        Set<LeadEmails> leadEmails = new HashSet<>();

        JSONObject temp = null;

        logger.log(Level.INFO, "removing records with duplicate _ids");
        
        for(Object record: recordsArray){
            temp = (JSONObject) record;
            leadEmails.add(new LeadEmails(temp.get("_id").toString(),temp.get("email").toString(),
                    temp.get("firstName").toString(), temp.get("lastName").toString(),temp.get("address").toString(),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+hh:mm").parse(temp.get("entryDate").toString())));
        }
        

        logger.log(Level.INFO, "removing records with duplicate emails");
        
        for(LeadEmails l: leadEmails){
            leads.add( new Leads(l.getid(),l.getEmail(),l.getFirstName(),l.getLastName(),l.getAddress(),l.getEntryDate()));
        }
        return leads;
    }



    public static void main(String args[]) throws IOException, ParseException{
        LeadsParser lp = new LeadsParser();
        lp.parse(args[0]);
        String s = lp.toString();
        logger.log(Level.INFO, "Generating output output json file "+args[1]);
        FileWriter f = new FileWriter(args[1]);
        f.write(s);
        f.close();
    }
}
