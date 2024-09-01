package sg.edu.nus.iss.lovecalculator.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class Calculator implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fname;
    private String sname;
    private String percentage;
    private String result;

    //need to implement id for redis db.
    private String dataId;

    public Calculator() {
    }

    public Calculator(String dataId) {
        this.dataId = dataId;
    }

    public Calculator(String fname, String sname) {
        this.fname = fname;
        this.sname = sname;
    }

    public Calculator(String dataId, String fname, String sname, String percentage, String result) {
        this.dataId = dataId;
        this.fname = fname;
        this.sname = sname;
        this.percentage = percentage;
        this.result = result;
    }

    public Calculator(String fname, String sname, String percentage, String result) {
        this.fname = fname;
        this.sname = sname;
        this.percentage = percentage;
        this.result = result;
    }

    public String getFname() {return fname;}
    public void setFname(String fname) {this.fname = fname;}
    
    public String getSname() {return sname;}
    public void setSname(String sname) {this.sname = sname;}
    
    public String getPercentage() {return percentage;}
    public void setPercentage(String percentage) {this.percentage = percentage;}
    
    public String getResult() {return result;}
    public void setResult(String result) {this.result = result;}

    public String getDataId() {return dataId;}
    public void setDataId(String dataId) {this.dataId = dataId;}
    

    //convert from JsonObject to Java
    // public static Calculator createFromJson(JsonObject j){
    //     Calculator c = new Calculator();
    //     c.percentage = "%s - %s"
    //             .formatted(j.getString("percentage"));
    //     c.result = "%s - %s"
    //             .formatted(j.getString("result"));

        // c.icon = "https://openweathermap.org/img/wn/%s@4x.png"
        //     .formatted(j.getString("icon"));
        //c.icon = "https://openweathermap.org/img/wn/" + j.getString("icon")  + "@4x.png";
        
    //     return c;
    // }

    private synchronized static String generateId(int size){
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString().substring(0, 8);
        return uuidString;
    }

  
    // convert json to Java object. 
    public static Calculator createUserObject(String json) throws IOException{
        Calculator c = new Calculator();
        try(InputStream is = new ByteArrayInputStream(json.getBytes())) {
            JsonReader r = Json.createReader(is);
            JsonObject o = r.readObject();
            c.setFname(o.getString("fname"));
            c.setSname(o.getString("sname"));
            c.setPercentage(o.getString("percentage"));
            c.setResult(o.getString("result"));

        }
        
        c.setDataId(generateId(8));
        return c;
    }

    public static Calculator createUserObjectFromRedis(String json) throws IOException{
        Calculator c = new Calculator();
        try(InputStream is = new ByteArrayInputStream(json.getBytes())) {
            JsonReader r = Json.createReader(is);
            JsonObject o = r.readObject();
            c.setDataId(o.getString("dataId"));
            c.setFname(o.getString("fname"));
            c.setSname(o.getString("sname"));
            c.setPercentage(o.getString("percentage"));
            c.setResult(o.getString("result"));

        }

        return c;
    }


    public JsonObject toJSON(){
        return Json.createObjectBuilder()
                .add("dataId", this.getDataId())
                .add("fname", this.getFname())
                .add("sname", this.getSname())
                .add("percentage", this.getPercentage())
                .add("result", this.getResult())
                .build();
    }

    @Override
    public String toString() {
        return "fname=" + this.getFname() + ", sname=" + this.getSname() + ", percentage=" + this.getPercentage() + ", result=" + this.getResult()
                + ", dataId=" + this.getDataId() + "]";
    }

    

    
}
