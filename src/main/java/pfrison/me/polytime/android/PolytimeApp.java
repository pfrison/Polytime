package pfrison.me.polytime.android;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import pfrison.me.polytime.R;
import pfrison.me.polytime.exceptions.FireDialog;

/**
 * This will be called at app start. I used it to set the behavior on an uncaught exception (app crash)
 */
public class PolytimeApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //when the app crash for unknown reasons, display a dialog to inform the user (including the stackTrace so he can send a bug report)
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                //print stack trace in the log console
                ex.printStackTrace();

                //get the stack trace in a form of a String
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString().replace("\n", "<br>&emsp;");

                //prepare to launch an activity to handle the dialog
                Intent intent = new Intent(getBaseContext(), FireDialog.class);
                Bundle extras = new Bundle();
                extras.putString("title", getResources().getString(R.string.fatalFailTitle));
                extras.putString("message", String.format(getResources().getString(R.string.fatalFailMessage),
                        getResources().getString(R.string.app_name), exceptionAsString));
                extras.putBoolean("canhardreset", true);
                intent.putExtras(extras);
                //launch the activity
                getBaseContext().startActivity(intent);

                //stop the bugged thread so the dialog can start
                System.exit(-1);
            }
        });
    }
}
