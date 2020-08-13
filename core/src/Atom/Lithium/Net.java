package Atom.Lithium;

import org.json.JSONObject;
import org.json.JSONTokener;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import static Atom.Manifest.*;

public class Net extends Lithium {
    public Boolean hasConnection() throws Exception {
        return !getIp().equals("NULL");
    }

    public String getIp() throws Exception {
        URL amazon = new URL("http://checkip.amazonaws.com");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                amazon.openStream()))) {
            IP = in.readLine();
        }
        return IP;
    }

    public JSONObject getHttps(String url){
        String pjson = ClientBuilder.newClient().target(url).request().accept(MediaType.APPLICATION_JSON).get(String.class);
       return new JSONObject(new JSONTokener(pjson));
    }

    public String getProvider() throws Exception {
        String url = "http://ip-api.com/json/?fields=status,message,continent,continentCode,country,countryCode,region,regionName,city,district,zip,lat,lon,timezone,currency,isp,org,as,asname,reverse,mobile,proxy,hosting,query" + getIp();
        String pjson = ClientBuilder.newClient().target(url).request().accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONObject json = new JSONObject(new JSONTokener(pjson));
        if (json.has("status")) {
            
        }
        return IP;
    }

}
