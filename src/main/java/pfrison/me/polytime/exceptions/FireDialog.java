package pfrison.me.polytime.exceptions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;

import pfrison.me.polytime.R;

/**
 * This is an activity used to display an error dialog.
 * Before starting the activity, make sure you passed a Bundle that contain :
 *  - String in "title" and "message" keys
 *  - boolean in "canhardreset" key
 */
public class FireDialog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get what to display
        Bundle extras = getIntent().getExtras();
        String title = extras.getString("title");
        String message = extras.getString("message");
        boolean canHardReset = extras.getBoolean("canhardreset"); //hardrest = erase all user preference (including saved tables)

        //prepare the dialog
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(Html.fromHtml(title));
        alertDialog.setMessage(Html.fromHtml(message));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.neutralErrorDialogAction),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //the neutral action do nothing but stop the app
                        dialog.dismiss();
                        finishAffinity();
                    }
                });
        if(canHardReset) alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.positiveErrorDialogAction),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //the positive action erase all user preference and stop the app
                        SharedPreferences.Editor prefedit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        prefedit.clear();
                        prefedit.apply();

                        dialog.dismiss();
                        finishAffinity();
                    }
                });
        //start the dialog
        alertDialog.show();
    }
}