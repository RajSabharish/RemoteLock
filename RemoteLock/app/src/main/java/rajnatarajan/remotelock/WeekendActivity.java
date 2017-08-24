package rajnatarajan.remotelock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class WeekendActivity extends AppCompatActivity {

    String accessKey,returnvalue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekend);
        Button shutDownbutton = (Button) findViewById(R.id.shutDownButton);
        Button cancelbutton = (Button) findViewById(R.id.cancelButton);
        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences myPreferences
                        = PreferenceManager.getDefaultSharedPreferences(WeekendActivity.this);
                if (myPreferences.contains("AccessKey") && myPreferences.getString("AccessKey", null) != null) {
                    accessKey=myPreferences.getString("AccessKey", null);
                    WeekendActivity.updatestatus task = new WeekendActivity.updatestatus() {
                        protected void onPostExecute(String result) {
                            System.out.println(result);
                            returnvalue = result;
                            if (returnvalue.equals("1")) {
                                Toast.makeText(WeekendActivity.this, "Weekend reminder canceled", Toast.LENGTH_LONG).show();
                            } else if (returnvalue.equals("0")) {
                                Toast.makeText(WeekendActivity.this, "Device not found", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(WeekendActivity.this, MainActivity.class);
                                finishAffinity();
                                startActivity(i);
                            }
                        }
                    };
                    task.execute(accessKey, "resetweekend");
                }
                else{
                    System.out.println("Invalid Access Key");
                }
                finish();
            }
        });
        shutDownbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SharedPreferences myPreferences
                        = PreferenceManager.getDefaultSharedPreferences(WeekendActivity.this);
                if (myPreferences.contains("AccessKey") && myPreferences.getString("AccessKey", null) != null) {
                    accessKey=myPreferences.getString("AccessKey", null);
                    WeekendActivity.updatestatus task = new WeekendActivity.updatestatus() {
                        protected void onPostExecute(String result) {
                            System.out.println(result);
                            returnvalue = result;
                            if (returnvalue.equals("1")) {
                                Toast.makeText(WeekendActivity.this, "Shut Down device command sent", Toast.LENGTH_LONG).show();
                            } else if (returnvalue.equals("0")) {
                                Toast.makeText(WeekendActivity.this, "Device not found", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(WeekendActivity.this, MainActivity.class);
                                finishAffinity();
                                startActivity(i);
                            }
                        }
                    };
                    task.execute(accessKey, "weekend");
                }
                else{
                    System.out.println("Invalid Access Key");
                }
                final Intent emptyIntent = new Intent();
                PendingIntent pendingIntent = PendingIntent.getActivity(WeekendActivity.this, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(WeekendActivity.this)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setSmallIcon(R.drawable.notification_lock)
                                .setContentTitle("Remote Lock")
                                .setContentText("Shutdown command sent to device!")
                                .setContentIntent(pendingIntent);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, mBuilder.build());
                finish();
            }
        });
    }

    class updatestatus extends AsyncTask<String, String, String> {
        String return_result;


        @Override
        protected String doInBackground(String... args) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                HttpResponse response;
                JSONObject json = new JSONObject();
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("uniqueId", args[0]));
                nameValuePairs.add(new BasicNameValuePair("actionToken",args[1]));

                try {
                    HttpPost post = new HttpPost("http://54.149.161.48/update_status.php");
                    post.addHeader("Content-Type","application/x-www-form-urlencoded");
                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                    response = client.execute(post);
                    if(response!=null){
                        InputStream is = response.getEntity().getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"),8);
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null)
                        {
                            sb.append(line);
                        }
                        is.close();
                        String result = sb.toString();
                        System.out.println(result);
                        //JSONObject json_data = new JSONObject(result);
                        //int success = json_data.getInt("success");
                        if(result.equals("Success"))
                        {
                            System.out.println("Database Updated");
                            return_result = "1";
                        }
                        else
                        {
                            System.out.println("Failed to update database");
                            return_result = "0";
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }catch (Throwable t)
            {
                t.printStackTrace();
            }
            return return_result;
        }
    }
}