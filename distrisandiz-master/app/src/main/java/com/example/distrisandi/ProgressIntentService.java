package com.example.distrisandi;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.distrisandi.JSONParser.json;

public class ProgressIntentService extends IntentService {
    private static final String TAG = ProgressIntentService.class.getSimpleName();
    JSONParser jsonParser = new JSONParser();
  /*  String URL = "http://10.0.2.2/sandiz/WebService/productos_vendidos.php";
    String URL_json = "http://10.0.2.2/sandiz/WebService/productos_vendidos_detalles.php";*/
    String URL = "https://www.sandiz.com.mx/failisa/WebService/productos_vendidos.php";
    String URL_json = "https://www.sandiz.com.mx/failisa/WebService/productos_vendidos_detalles.php";



    private String folio="";
    private String folio_a="";
    private   String folio_recibido;


    public ProgressIntentService() {
        super("ProgressIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constaints.ACTION_RUN_ISERVICE.equals(action)) {
                handleActionRun();
            }
        }
    }

    private void handleActionRun() {
        try {
            // Se construye la notificación
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle("Servicio en segundo plano")
                    .setContentText("Procesando...");

            // Bucle de simulación
            boolean prueba = true;
            String estadobase = "No subido";
            SharedPreferences sharedPref = getSharedPreferences("lista_clientes_usuario", Context.MODE_PRIVATE);
            SharedPreferences setting = getSharedPreferences("login_preference", MODE_PRIVATE);
            String id_caja = sharedPref.getString("numero_ruta","");
            String id_usuario = setting.getString("username", "");

            final Calendar c = Calendar.getInstance();
            SimpleDateFormat fecha = new SimpleDateFormat("YYYY-MM-dd");
            String fldFechaVentaProducto = fecha.format(c.getTime());
            while(prueba){
                if (isNetworkAvailable(ProgressIntentService.this)) {

                    Log.d(TAG, 1 + ""); // Logueo
                    Log.e("mensaje", "iniciado");

                    AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(ProgressIntentService.this, "administracion", null, 1);
                    SQLiteDatabase bd = admin.getWritableDatabase();
                    Cursor fila = bd.rawQuery("select folio, estado, id_cliente,tipo_operacion ,estado_operacion,cancelado from venta_cliente where estado" + "='" + estadobase + "' and cancelado='0' limit 1", null);
                    Log.e("tamanio_bd", String.valueOf(fila.getCount()));
                    if (fila.moveToFirst()) {
                        folio = fila.getString(0);
                        String id_cliente = fila.getString(2);
                        String tipo_operacion = fila.getString(3);
                        String estado_operacion = fila.getString(4);
                        String cancelado_op = fila.getString(5);
                        //enviardatos enviar = new enviardatos();
                        int folio_ex = folio.indexOf("FSC");
                        //Log.e("foliossss",String.valueOf(folio_ex));
                        Log.e("mensaje", folio + id_cliente + tipo_operacion + cancelado_op);

                        try {

                            Log.e("mensaje", "Hay datos que Subir");
                            Log.e("foliossss", folio);
                            enviardatos_real enviar_real = new enviardatos_real();
                            enviar_real.execute(id_cliente, tipo_operacion, estado_operacion, id_caja, id_usuario,
                                    fldFechaVentaProducto, fldFechaVentaProducto, cancelado_op, "SIN DETALLES");
                            Log.e("foliossss", "_____________________");
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {


                        }
                        folio_a = folio;
                        Log.e("cliente_vendido10", folio_a);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        startMyOwnForeground();
                    else
                        startForeground(1, new Notification());

                    Intent localIntent = new Intent(Constaints.ACTION_RUN_ISERVICE)
                            .putExtra(Constaints.EXTRA_PROGRESS, 1);

                    // Emisión de {@code localIntent}
                    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

                    // Retardo de 1 segundo en la iteración
                    Thread.sleep(1000);
                }
            }
            // Quitar de primer plano
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.logo1)
                .setContentTitle("DISTRIBUIDORA SANDIZ")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Servicio destruido...", Toast.LENGTH_SHORT).show();

        // Emisión para avisar que se terminó el servicio
        Intent localIntent = new Intent(Constaints.ACTION_PROGRESS_EXIT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        Log.d(TAG, "Servicio destruido...");
    }



    private class enviardatos_real extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            folio_recibido="";

        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String detalles = args[8];
            String fldCancelado = args[7];
            String fldFechaVentaProducto = args[6];
            String fldRegistrarFecha = args[5];
            String id_usuario = args[4];
            String id_caja = args[3];
            String id_estadoOperacion = args[2];
            String id_tipoOperacion = args[1];
            String id_cliente = args[0];
            //String id_ventaProducto = args[0];


            ArrayList params = new ArrayList();
           // params.add(new BasicNameValuePair("id_ventaProducto",id_ventaProducto));
            params.add(new BasicNameValuePair("id_cliente",id_cliente));
            params.add(new BasicNameValuePair("id_tipoOperacion",id_tipoOperacion));
            params.add(new BasicNameValuePair("id_estadoOperacion",id_estadoOperacion));
            params.add(new BasicNameValuePair("id_caja",id_caja));
            params.add(new BasicNameValuePair("id_usuario",id_usuario));
            params.add(new BasicNameValuePair("fldFechaVentaProducto",fldFechaVentaProducto));
            params.add(new BasicNameValuePair("fldRegistrarFecha",fldRegistrarFecha));
            params.add(new BasicNameValuePair("fldCancelado",fldCancelado));
            params.add(new BasicNameValuePair("detalles",detalles));
            JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
           // String folio_r= id_ventaProducto;
            return json;

        }
        protected void onPostExecute(JSONObject result){
            try{

                if(result != null){
                    String mensaje = result.getString("message");
                    if(mensaje.equals("error")){

                    }else {
                        if (isNetworkAvailable(ProgressIntentService.this))
                        {
                            folio_recibido = result.getString("message");
                        //Toast.makeText(ProgressIntentService.this,"datos no cambiados",Toast.LENGTH_SHORT).show();
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(ProgressIntentService.this, "administracion", null, 1);
                        AdminSQLiteOpenHelper admin_1 = new AdminSQLiteOpenHelper(ProgressIntentService.this, "administracion1", null, 1);
                        SQLiteDatabase bd = admin.getWritableDatabase();
                        SQLiteDatabase bd_1 = admin_1.getWritableDatabase();
                        ContentValues actualizar = new ContentValues();
                        ContentValues actualizar_1 = new ContentValues();
                        actualizar.put("estado", "Subido");
                        actualizar_1.put("folio", folio_recibido);
                        actualizar.put("folio", folio_recibido);
                        bd.update("venta_cliente", actualizar, "folio=?", new String[]{folio});
                        bd_1.update("venta_detalles", actualizar_1, "folio=?", new String[]{folio});
                        bd.close();
                        bd_1.close();
                        getResult();
                    }

                    }
                }else {
                    //Toast.makeText(ProgressIntentService.this,"no conectado con el servdor", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    private JSONArray getResult(){
        //Toast.makeText(consulta_ventas_totales.this,"hola mudno",Toast.LENGTH_SHORT).show();
        //eturn  null;
        Log.e("folio_recibido",folio_recibido);
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(ProgressIntentService.this,"administracion1",null,1);
        SQLiteDatabase bd = admin.getWritableDatabase();
        Cursor fila = bd.rawQuery("select  folio, codigo_producto,cantidad_vendido ,peso_producto, precio_compra, precio_real,precio_venta from venta_detalles where folio "+"='" +folio_recibido+"'" ,null);
        JSONArray resultSet     = new JSONArray();

        fila.moveToFirst();
        while (fila.isAfterLast() == false) {

            int totalColumn = fila.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for( int i=0 ;  i< totalColumn ; i++ )
            {
                if( fila.getColumnName(i) != null )
                {
                    try
                    {
                        if( fila.getString(i) != null )
                        {
                            Log.d("TAG_NAME", fila.getString(i) );
                            rowObject.put(fila.getColumnName(i) ,  fila.getString(i) );
                        }
                        else
                        {
                            rowObject.put( fila.getColumnName(i) ,  "" );
                        }
                    }
                    catch( Exception e )
                    {
                        Log.d("TAG_NAME", e.getMessage()  );
                    }
                }
            }
            resultSet.put(rowObject);
            fila.moveToNext();
        }
        fila.close();
        Gson gson = new Gson();
        String output = gson.toJson(resultSet);
        //Toast.makeText(consulta_ventas_totales.this,output,Toast.LENGTH_SHORT).show();
        Log.d("foliossss", output);
        enviardatos_detalles enviar_dato = new enviardatos_detalles();
        String id_enterprise = "1";
        enviar_dato.execute(output,id_enterprise,"");
        return resultSet;
    }

    private class enviardatos_detalles extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String id_enterprise = args[1];
            String json_array = args[0];

            SharedPreferences setting = getSharedPreferences("lista_clientes_usuario", MODE_PRIVATE);
            String ruta_cliente = setting.getString("numero_ruta", "");

            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("json_array",json_array));
            params.add(new BasicNameValuePair("id_enterprise",id_enterprise));
            params.add(new BasicNameValuePair("route",ruta_cliente));

            JSONObject json = jsonParser.makeHttpRequest(URL_json, "POST", params);
            return json;
        }
        protected void onPostExecute(JSONObject result){
            try{
                if(result != null){
                    String mensaje_datos = result.getString("message");
                    Log.e("mensaje1002q",mensaje_datos);
                    if(mensaje_datos.equals("exito") || mensaje_datos.equals(folio_recibido)){
                        if(isNetworkAvailable(ProgressIntentService.this)){
                            Toast.makeText(ProgressIntentService.this,"DATOS GUARDADOS", Toast.LENGTH_SHORT).show();
                            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(ProgressIntentService.this,"administracion",null,1);
                            SQLiteDatabase bd = admin.getWritableDatabase();
                            ContentValues actualizar = new ContentValues();
                            actualizar.put("estado","Subido");
                            bd.update("venta_cliente",actualizar,"folio=?",new String[]{folio_recibido});
                            bd.close();
                        }



                    }else {
                        Toast.makeText(ProgressIntentService.this,"DATOS NO GUARDADOS EN EL SERVIDOR",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //Toast.makeText(consulta_ventas_totales.this,"no conectado con el servdor", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
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
