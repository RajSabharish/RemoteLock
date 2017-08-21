package rajnatarajan.remotelock;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private ProgressDialog pDialog;
    String returnvalue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(this);
        if (myPreferences.contains("AccessKey")) {
            final String accessKey = myPreferences.getString("AccessKey",null);
            if(accessKey!=null) {
                MainActivity.checksstatus task = new checksstatus() {
                    protected void onPostExecute(String result) {
                        pDialog.dismiss();
                        System.out.println(result);
                        returnvalue = result;
                        if (returnvalue.equals("1")) {
                            Intent userLockIntent = new Intent(MainActivity.this,LockActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("uniqueId", accessKey);
                            userLockIntent.putExtras(bundle);
                            startActivity(userLockIntent);
                        }
                        else if (returnvalue.equals("0"))
                        {
                            System.out.println("The Access Key stored is not valid anymore");
                            Intent intent = new Intent(MainActivity.this, GetAccessKeyActivity.class);
                            startActivityForResult(intent, 333);
                        }
                    }
                };
                task.execute(accessKey);
            }
            else
            {
                System.out.println("Access Key is null from Shared Preferences");
            }

        } else {
            Intent intent = new Intent(this, GetAccessKeyActivity.class);
            startActivityForResult(intent, 333);

        }
        //System.out.println("****************************");
        //  }while (uniqueKey!=null);
        // PreferenceManager.getDefaultSharedPreferences(this).edit().putString("AccessKey",uniqueKey).apply();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 333) {
            if (resultCode == RESULT_OK) {
                String uniqueKey = data.getStringExtra("AccessKey");
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("AccessKey",uniqueKey).apply();
                Intent userLockIntent = new Intent(MainActivity.this,LockActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("UniqueId", uniqueKey);
                userLockIntent.putExtras(bundle);
                startActivity(userLockIntent);
            }
        }
    }

    class checksstatus extends AsyncTask<String, String, String> {
        String return_result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                HttpResponse response;
                JSONObject json = new JSONObject();
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                System.out.println(args[0]);
                nameValuePairs.add(new BasicNameValuePair("uniqueId", args[0]));

                try {
                    HttpPost post = new HttpPost("http://54.149.161.48/get_details.php");
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
                            sb.append(line + "\n");
                        }
                        is.close();
                        String result = sb.toString();
                        System.out.println(result);
                        JSONObject json_data = new JSONObject(result);
                        int success = json_data.getInt("success");
                        if(success==1)
                        {
                            System.out.println("Device connected");
                            return_result = "1";
                        }
                        else
                        {
                            System.out.println("No device found");
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


