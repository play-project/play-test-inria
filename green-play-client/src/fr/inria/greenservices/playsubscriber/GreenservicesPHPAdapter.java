package fr.inria.greenservices.playsubscriber;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class GreenservicesPHPAdapter {

			
		public static void main(String[] args) {
			GreenservicesPHPAdapter.sendToMGS("130","sms");
		}
public static void sendToMGS(String idUser, String type ) {
		
		try {
			//String url = "http://localhost/greenservices/play/sendFromPlay.php";
			String url = "https://mygreenservices.inria.fr/play/sendFromPlay.php";
			 
			//param : idUser=[idUser]&type=[sms,mail]
			String urlParameters = "idUser=" +idUser+"&type="+type;
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				
				@Override
				public boolean verify(String hostname, SSLSession session) {
					System.out.println(hostname);
					if (hostname.equals("mygreenservices.inria.fr"))
	                    return true;
					return false;
				}
			});
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			
			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
				 
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
	 
			int responseCode = con.getResponseCode();
			/*System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);*/
	 
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	 
			//print result
			System.out.println(response.toString());
			con.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
