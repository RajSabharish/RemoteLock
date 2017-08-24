package rajnatarajan.remotelock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
 * Created by RajSabharish on 17-08-2017.
 */

public class NotificationService extends Service {

    String returnvalue;
    public static final long NOTIFY_INTERVAL_CHECK_IDLE = 120 * 1000; // 2 minutes
    public static final long NOTIFY_INTERVAL_CHECK_WEEKEND = 60 * 1000; // 30 minutes
    private Handler idleHandler = new Handler();
    private Handler weekendHandler = new Handler();
    private Timer idleTimer = null;
    private Timer weekendTimer = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // cancel if already existed
        if(idleTimer != null) {
            idleTimer.cancel();
        } else {
            // recreate new
            idleTimer = new Timer();
        }
        // schedule task
        idleTimer.scheduleAtFixedRate(new checkIdleTimerTask(), 0, NOTIFY_INTERVAL_CHECK_IDLE);

        if(weekendTimer != null) {
            weekendTimer.cancel();
        } else {
            // recreate new
            weekendTimer = new Timer();
        }

        weekendTimer.scheduleAtFixedRate(new checkWeekendStatusTask(), 0, NOTIFY_INTERVAL_CHECK_WEEKEND);
    }

    class checkIdleTimerTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            idleHandler.post(new Runnable() {

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
                                                        .setPriority(NotificationCompat.PRIORITY_MAX)
                                                        .setDefaults(Notification.DEFAULT_ALL)
                                                        .setSmallIcon(R.drawable.notification_lock)
                                                        .setContentTitle("Remote Lock")
                                                        .setContentText("Device idle. Tap for Actions.")
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
                            task.execute(accessKey,"idle");
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

    class checkWeekendStatusTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            weekendHandler.post(new Runnable() {

                @Override
                public void run() {
                    SharedPreferences myPreferences
                            = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    System.out.println("Executing&&&&&&&&&&");
                    if (myPreferences.contains("AccessKey")) {
                        final String accessKey = myPreferences.getString("AccessKey", null);
                        if (accessKey != null) {
                            NotificationService.checksstatus task = new NotificationService.checksstatus() {
                                protected void onPostExecute(String result) {
                                    System.out.println(result);
                                    returnvalue = result;
                                    if (returnvalue.equals("1")) {
                                        final Intent notificationIntent = new Intent(getApplicationContext(), WeekendActivity.class);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                        NotificationCompat.Builder mBuilder =
                                                new NotificationCompat.Builder(NotificationService.this)
                                                        .setPriority(NotificationCompat.PRIORITY_MAX)
                                                        .setDefaults(Notification.DEFAULT_ALL)
                                                        .setSmallIcon(R.drawable.notification_lock)
                                                        .setContentTitle("Remote Lock")
                                                        .setContentText("Turn off the System over weekend and save power. Tap for options.")
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
                            task.execute(accessKey,"weekend");
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
                            String weekend = json_data.getString("weekendstatus");
                            if(args[1].equals("idle")) {
                                if (success == 1 && idle.equals("1")) {
                                    System.out.println("Device connected");
                                    return_result = "1";
                                } else {
                                    System.out.println("No device found / System not idle");
                                    return_result = "0";
                                }
                            }
                            else if(args[1].equals("weekend"))
                            {
                                if(success == 1 && weekend.equals("1")) {
                                    System.out.println("Device connected");
                                    return_result = "1";
                                }
                                else
                                {
                                    System.out.println("No Device Found / Weekend status negative");
                                    return_result = "0";
                                }
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
