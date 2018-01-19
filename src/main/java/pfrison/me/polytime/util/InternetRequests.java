package pfrison.me.polytime.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import pfrison.me.polytime.android.MainActivity;

/**
 * Contain all methods we need to dialog with the server.
 */
public class InternetRequests {
	public static boolean connectionFail = false;

	/**
	 * Return the raw response from the server. Or null if the server is unreachable.
	 * @param context the context
	 * @param pref the user preference
	 * @return a {@link String} containing the response. Or null if no connexion
     */
	public static String getRawData(Context context, SharedPreferences pref){
        connectionFail = false;
		int groupeTP = pref.getInt(MainActivity.GROUP_TP_KEY, 0);
		String data = "";

		//try connection
		HttpURLConnection connection;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		//no connection
		if(netInfo == null || !netInfo.isConnectedOrConnecting()){
            //no connection
			//save exist ?
            connectionFail = true;
			return null;
        }

		try {
			URL url = new URL("http://dptima3.polytech-lille.net/EdTS6/" + StringWizard.getPageString(groupeTP));
			connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(2000); //2 seconds timeout (max wait for a connection)
            connection.setReadTimeout(4000); //4 seconds timeout (max wait to the end for the response)
			connection.connect();
		} catch (IOException e) {
			//connection failed
            connectionFail = true;
			return null;
		}
		
		//read server answer
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) data += inputLine + "\n";
			in.close();
		} catch (IOException e) {
            //read failled
            connectionFail = true;
            return null;
		}
		//encode data to UTF
		data = StringWizard.ISOtoUTF(data);
		
		//save the data
		SharedPreferences.Editor editPref = pref.edit();
		editPref.putString("rawDataSave" + String.valueOf(groupeTP), data);
		editPref.apply();

		return data;
	}
}
