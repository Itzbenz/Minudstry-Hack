package Atom.loginsys;
/*
import java.util.*;
import java.io.*;
import java.lang.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException
*/

public class loginsys {
  /* todo: this
  public static void main(String[] args) {
    new HttpsClient().getLogInfo();
  }
  public void getLogInfo() {
      String https_url = "https://www.940683780397_o7.com/";
      URL url;
      try {
	      url = new URL(https_url);
	      HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
	      print_https_cert(con);
	      print_content(con);
        } catch (MalformedURLException e) {
	      e.printStackTrace();
        } catch (IOException e) {
	      e.printStackTrace();
     }
  }
      
   private void print_https_cert(HttpsURLConnection con){
    if(con!=null){
      try {
      	LS.handle("RS " + con.getResponseCode());
	      LS.handle("CS " + con.getCipherSuite());
	      Certificate[] certs = con.getServerCertificates();
	      for(Certificate cert : certs){
	        LS.handle("CT " + cert.getType());
	        LS.handle("CHT " + cert.hashCode());
	        LS.handle("CPKA " + cert.getPublicKey().getAlgorithm());
	        LS.handle("CPKF " + cert.getPublicKey().getFormat());
	        LS.manifest("FL");
	        } catch (SSLPeerUnverifiedException e) {
		      e.printStackTrace();
	        } catch (IOException e){
		      e.printStackTrace();
        }
      }
    }
  */
}
