package rajnatarajan.remotelock;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Sabarish on 17-08-2017.
 */

public class NotificationService extends Service {

    String returnvalue;
    public static final long NOTIFY_INTERVAL = 120 * 1000; // 10 seconds
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // cancel if already existed
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    class TimeDisplayTimerTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    SharedPreferences myPreferences
                            = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    System.out.println("Executing%%%%%%%%%");
                    if (myPreferences.contains("AccessKey")) {
                        final String accessKey = myPreferences.getString("AccessKey", null);
                        if (accessKey != null) {
                            NotificationService.checksstatus task = new NotificationService.checksstatus() {
                                protected void onPostExecute(String result) {
                                    System.out.println(result);
                                    returnvalue = result;
                                    if (returnvalue.equals("1")) {
                                        final Intent notificattionIntent = new Intent(getApplicationContext(), IdleLockActivity.class);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, notificattionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                        NotificationCompat.Builder mBuilder =
                                                new NotificationCompat.Builder(NotificationService.this)
                                                        .setSmallIcon(R.drawable.notification_lock)
                                                        .setContentTitle("Remote Lock")
                                                        .setContentText("Device idle")
                                                        .setContentIntent(pendingIntent);
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.notify(1, mBuilder.build());
                                    } else if (returnvalue.equals("0")) {
                                        System.out.println("The Access Key stored is not valid anymore");

                                    } else if (returnvalue.equals("2")) {
                                        System.out.println("No network connection available");
                                    }
                                }
                            };
                            task.execute(accessKey);
                        } else {
                            System.out.println("Access Key is null from Shared Preferences");
                        }

                    } else {
                        stopService(new Intent(getBaseContext(), NotificationService.class));
                        // Toast.makeText(this, "Service Stoped", Toast.LENGTH_LONG).show();
                    }

                    // Let it continue running until it is stopped.
                   // Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
                    //return START_STICKY;
                }

            });
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    class checksstatus extends AsyncTask<String, String, String> {
        String return_result;


        @Override
        protected String doInBackground(String... args) {
            try {
                ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
                if(netInfo!=null) {
                    HttpClient client = new DefaultHttpClient();
                    HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                    HttpResponse response;
                    JSONObject json = new JSONObject();
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    System.out.println(args[0]);
                    nameValuePairs.add(new BasicNameValuePair("uniqueId", args[0]));

                    try {
                        HttpPost post = new HttpPost("http://54.149.161.48/get_details.php");
                        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
                        post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                        response = client.execute(post);
                        if (response != null) {
                            InputStream is = response.getEntity().getContent();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                            StringBuilder sb = new StringBuilder();
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            is.close();
                            String result = sb.toString();
                            System.out.println(result);
                            JSONObject json_data = new JSONObject(result);
                            int success = json_data.getInt("success");
                            String idle = json_data.getString("idletime");
                            if (success == 1&&idle.equals("1")) {
                                System.out.println("Device connected");
                                return_result = "1";
                            } else {
                                System.out.println("No device found");
                                return_result = "0";
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                   return_result="2";
                }
            }catch (Throwable t)
            {
                t.printStackTrace();
            }
            return return_result;
        }

    }

}
