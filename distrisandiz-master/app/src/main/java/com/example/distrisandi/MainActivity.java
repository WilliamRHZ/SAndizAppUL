package com.example.distrisandi;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    LinearLayout botonIniciar;
    LinearLayout txtRegistrar;
    private  EditText edUsuario;
    private  EditText edContrasenia;
    ProgressDialog dialogoIniciarSesion;

    String URL = "https://sandiz.com.mx/failisa/WebService/index.php";
   // String URL = "http://10.0.2.2/sandiz/WebService/index.php";
    //String URL = "  https://192.168.8.85/sandizsistema/WebService/v2/index.php";


    JSONParser jsonParser = new JSONParser();

    private SharedPreferences sharedPref;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences setting0 = getSharedPreferences("login_preference", MODE_PRIVATE);
        String value1 = setting0.getString("username", "");
        if (!value1.equals("")) {
            loadSesionUsuario();
        }
            setContentView(R.layout.activity_main);
            //this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setCustomView(R.layout.actionbar_inicio);

            edUsuario = (EditText) findViewById(R.id.editUsuario);
            edContrasenia = (EditText) findViewById(R.id.editContrsaenia);

            botonIniciar = (LinearLayout) findViewById(R.id.btnIniciarSesion);
            txtRegistrar = (LinearLayout) findViewById(R.id.txtRecuperarContrasenia);


            botonIniciar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Intent intent = new Intent(MainActivity.this,Sesion_Usuario.class);
                    //startActivityForResult(intent,0);
                    if(!isNetworkAvailable(MainActivity.this)){
                        AlertDialog.Builder inicio_sinConexion = new AlertDialog.Builder(MainActivity.this);
                        inicio_sinConexion.setIcon(R.drawable.ic_alerta);
                        inicio_sinConexion.setTitle("SIN CONEXION");
                        inicio_sinConexion.setMessage("No estas conectado a Internet");
                        inicio_sinConexion.setCancelable(false);
                        inicio_sinConexion.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        inicio_sinConexion.show();
                    }else{
                        final String username = edUsuario.getText().toString();
                        final String password = edContrasenia.getText().toString();
                        if(username.equals("")){
                            Toast.makeText(MainActivity.this,"INGRESA UN USUARIO",Toast.LENGTH_SHORT).show();
                        }else if(password.equals("")){
                            Toast.makeText(MainActivity.this,"INGRESA LA CONTRASEÑA",Toast.LENGTH_SHORT).show();
                        }else {
                            saveLoadDatos(username, password);
                            AttempLogin attempLogin = new AttempLogin();
                            attempLogin.execute(edUsuario.getText().toString(), edContrasenia.getText().toString(), "");
                            dialogoIniciarSesion = new ProgressDialog(MainActivity.this);
                            dialogoIniciarSesion.setTitle("Iniciando Sesión");
                            dialogoIniciarSesion.setCancelable(false);
                            dialogoIniciarSesion.show();
                        }

                    }



                }
            });
            txtRegistrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(MainActivity.this,"REGISTRAR", Toast.LENGTH_SHORT).s how();
                    createCustomDialog().show();

                }
            });




        }
        private class AttempLogin extends AsyncTask<String, String, JSONObject> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected JSONObject doInBackground(String... args) {
                String email = args[2];
                String password = args[1];
                String name = args[0];

                ArrayList params = new ArrayList();
                params.add(new BasicNameValuePair("username", name));
                params.add(new BasicNameValuePair("password", password));
                if (email.length() > 0)
                    params.add(new BasicNameValuePair("email", email));
                JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
                return json;
            }

            protected void onPostExecute(JSONObject result) {
                try {
                    if (result != null) {
                        //Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
                        String mensaje = result.getString("message");
                        //Toast.makeText(getApplicationContext(), "mensaje1", Toast.LENGTH_LONG).show();
                        //Intent intent = new Intent(MainActivity.this,Sesion_Usuario.class);
                        //startActivityForResult(intent,0);
                        //finish();
                        if (mensaje.equals("Successfully logged in")) {
                            loadSesionUsuario();
                            edContrasenia.setText("");
                            edUsuario.setText("");
                            dialogoIniciarSesion.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(), "DATOS INCORRECTOS", Toast.LENGTH_LONG).show();
                            edContrasenia.setText("");
                            dialogoIniciarSesion.dismiss();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "NO SE PUEDE RECUPERAR DATOS EN EL SERVIDOR", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        public AlertDialog createCustomDialog () {
            final AlertDialog alertDialog;
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(R.layout.cuadro_dialogo_password, null);

            Button CambiarContrasenia = (Button) v.findViewById(R.id.btnRecuperarContrasenia);
            builder.setView(v);
            alertDialog = builder.create();

            CambiarContrasenia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });
            return alertDialog;
        }
        private void saveLoadDatos (String username, String password){
            sharedPref = getSharedPreferences("login_preference", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("username", username);
            editor.putString("password", password);
            editor.apply();
        }
        private void loadSesionUsuario () {
            Intent i = new Intent(getApplicationContext(), Sesion_Usuario.class);
            startActivity(i);


        }
        @Override
    protected void onDestroy(){
        super.onDestroy();
        }

    //CONEXION A INTERNET//
    public static boolean isNetworkAvailable(Context context) {
        if(context == null)  return false;


        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {


            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            }

            else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }
        Log.i("update_statut","Network is available : FALSE ");
        return false;
    }


}
