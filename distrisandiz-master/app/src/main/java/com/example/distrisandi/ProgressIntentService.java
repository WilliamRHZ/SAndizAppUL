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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.distrisandi.network.APIClient;
import com.example.distrisandi.network.APIInterface;
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
    private String folio="";
    private String folio_a="";
    private String folio_recibido;
    private APIInterface apiInterface;

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
            apiInterface = APIClient.getClient();
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
            SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd");
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
                        Log.e("mensaje", folio + id_cliente + tipo_operacion + cancelado_op);
                        try {
                            enviarDatosReal(id_cliente, tipo_operacion, estado_operacion, id_caja, id_usuario,
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

    private void enviarDatosReal(String idCliente, String tipoOperacion, String estadoOperacion, String idCaja, String idUsuario, String fechaVEntaProducto, String registrarFecha, String cancelado, String detalles){
        folio_recibido = "";
        Call<String> response = apiInterface.productosVendidos(idCliente,tipoOperacion,estadoOperacion,idCaja,idUsuario,fechaVEntaProducto,registrarFecha,cancelado,detalles);
        response.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try{
                    if(response.body() != null){
                        JSONObject result = new JSONObject(response.body());
                        String mensaje = result.getString("message");
                        if (!mensaje.equals("error")) {
                            if (isNetworkAvailable(ProgressIntentService.this))
                            {
                                folio_recibido = result.getString("message");
                                //Toast.makeText(ProgressIntentService.this,"datos no cambiados",Toast.LENGTH_SHORT).show();
                                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(ProgressIntentService.this, "administracion", null, 1);
                                //AdminSQLiteOpenHelper admin_1 = new AdminSQLiteOpenHelper(ProgressIntentService.this, "administracion1", null, 1);
                                SQLiteDatabase bd = admin.getWritableDatabase();
                                //SQLiteDatabase bd_1 = admin_1.getWritableDatabase();
                                ContentValues actualizar = new ContentValues();
                                ContentValues actualizar_1 = new ContentValues();
                                actualizar.put("estado", "Subido");
                                actualizar_1.put("folio", folio_recibido);
                                actualizar.put("folio", folio_recibido);
                                bd.update("venta_cliente", actualizar, "folio=?", new String[]{folio});
                                bd.update("venta_detalles", actualizar_1, "folio=?", new String[]{folio});
                                bd.close();
                                //bd_1.close();
                                getResult();
                            }
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private JSONArray getResult(){
        Log.e("folio_recibido",folio_recibido);
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(ProgressIntentService.this,"administracion",null,1);
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
        String id_enterprise = "1";

        enviarDatosDetalles(output,id_enterprise);
        return resultSet;
    }

    private void enviarDatosDetalles(String jsonArray, String idEnterprise){
        SharedPreferences setting = getSharedPreferences("lista_clientes_usuario", MODE_PRIVATE);
        String ruta_cliente = setting.getString("numero_ruta", "");
        Call<String> response = apiInterface.productosVendidosDetalles(jsonArray, idEnterprise, ruta_cliente);
        response.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try{
                    if(response.body() != null){
                        JSONObject result = new JSONObject(response.body());
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
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
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
            }else {
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
