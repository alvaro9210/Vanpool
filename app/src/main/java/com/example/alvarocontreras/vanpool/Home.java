package com.example.alvarocontreras.vanpool;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Home extends Activity implements

        NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback

{
    Button btnRegistrarUsuario;
    //        EditText txtUri;
    EditText txtIdReserva;
    TextView lblResult;

    NfcAdapter nfcAdapter;

    String uri;
    JSONArray response;
    String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnRegistrarUsuario = (Button)findViewById(R.id.btnRegistrar);
        btnRegistrarUsuario.setOnClickListener(handlerRegistrar);

        txtIdReserva = (EditText)findViewById(R.id.txtIdReserva);
        lblResult = (TextView)findViewById(R.id.lblResult);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null){
            Toast.makeText(Home.this,"Dispositivo sin NFC",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(Home.this,"Set callback(s)",Toast.LENGTH_LONG).show();
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }

    View.OnClickListener handlerRegistrar = new View.OnClickListener() {
        public void onClick(View v) {
            lblResult.setText("Enviando...");
            String params = txtIdReserva.getText().toString();
            if (params.length() > 1 ){
                if (params.contains(" ") || params.contains(",")){
                    params.replace(" ","&id=");
                    params.replace(",","&id=");
                }
            }
            params =  "id="+params;
            uri = "http://104.236.6.99/api/cliente_checkin?"+params;
            new JSONParse().execute();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord NdefRecord_0 = inNdefRecords[0];
            String inMsg = new String(NdefRecord_0.getPayload());
            lblResult.setText(inMsg);
            uri = new String(NdefRecord_0.getPayload());
            new JSONParse().execute();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        final String eventString = "onNdefPushComplete\n" + event.toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "mensaje enviado", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String params = txtIdReserva.getText().toString();
        if (params.length() > 1 ){
            if (params.contains(" ") || params.contains(",")){
                params.replace(" ","&id=");
                params.replace(",","&id=");
            }
        }else {
            params = "id=" + params;
        }
        uri = "http://104.236.6.99/api/cliente_checkin?id="+params;
        String stringOut = uri;


        byte[] bytesOut = stringOut.getBytes();
        NdefRecord ndefRecordOut = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,"text/plain".getBytes(),new byte[] {},bytesOut);
        NdefMessage ndefMessageout = new NdefMessage(ndefRecordOut);
        return ndefMessageout;
    }


    private class JSONParse extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lblResult = (TextView)findViewById(R.id.lblResult);
            pDialog = new ProgressDialog(Home.this);
            pDialog.setMessage("Getting Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            // Getting JSON from URL
            JSONObject json = jParser.getJSONFromUrl(uri);
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            try {
                // Getting JSON Array
                result = json.getString("resultado");
                lblResult.setText(result);
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
