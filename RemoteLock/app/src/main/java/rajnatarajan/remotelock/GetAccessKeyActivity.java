package rajnatarajan.remotelock;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class GetAccessKeyActivity extends AppCompatActivity {
    String uniqueKey;
    private ProgressDialog pDialog;
    String returnvalue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_access_key);

        Button connectbutton = (Button) findViewById(R.id.connectButton);

        connectbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText editText = (EditText) findViewById(R.id.accessKeyText);
                final String accessKey = editText.getText().toString();
                checksstatus task = new checksstatus(){
                    protected void onPostExecute(String result){
                        pDialog.dismiss();
                        System.out.println(result);
                        returnvalue=result;
                        if(returnvalue.equals("1")){
                            Intent intent = new Intent();
                            intent.putExtra("AccessKey", accessKey);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                        else if(returnvalue.equals("0")){
                            Toast.makeText(GetAccessKeyActivity.this,"Device not found",Toast.LENGTH_LONG).show();
                        }
                    }
                };
                task.execute(accessKey);

            }
        });

    }

    class checksstatus extends AsyncTask<String, String, String> {
        String return_result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(GetAccessKeyActivity.this);
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
                    System.out.println("@@@@@@@@@@@@@@@@@@@@@");
                    response = client.execute(post);
                    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%");
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
