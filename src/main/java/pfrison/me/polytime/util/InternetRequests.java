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

/**
 * Contain all methods we need to dialog with the server.
 */
public class InternetRequests {

	/**
	 * Return the raw response from the server. Or null if the server is unreachable.
	 * @param groupeTP the group TP selected
	 * @param context the context
	 * @param pref the user preference
	 * @return a {@link String} containing the response. Or null if no connexion
     */
	public static String getRawData(int groupeTP, Context context, SharedPreferences pref){
		String data = "";

		//try connection
		HttpURLConnection connection;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		//no connection
		if(netInfo == null || !netInfo.isConnectedOrConnecting()){
            //no connection
			//save exist ?
			return null;
		}

		try {
			URL url = new URL("http://dptima3.polytech-lille.net/" + StringWizard.getPageString(groupeTP));
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
		} catch (IOException e) {
			//connection failed
			return null;
		}
		
		//read server answer
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) data += inputLine + "\n";
			in.close();
		} catch (IOException e) {
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

    /**
     * Return the saved raw data. Or null if there is no save.
     * @param groupeTP the group TP selected
     * @param pref the user preference
     * @return a {@link String} containing the save. Or null if there is no save.
     */
    public static String getSavedData(int groupeTP, SharedPreferences pref){
        return pref.getString("rawDataSave" + String.valueOf(groupeTP), null);
    }
}
