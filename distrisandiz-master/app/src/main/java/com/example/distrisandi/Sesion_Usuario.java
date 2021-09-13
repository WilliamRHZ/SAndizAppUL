package com.example.distrisandi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.distrisandi.network.APIClient;
import com.example.distrisandi.network.APIInterface;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Sesion_Usuario extends AppCompatActivity{
    private SharedPreferences sharedPref;
    private AppBarConfiguration mAppBarConfiguration;
    private TextView nombre_completo;
    private TextView correo;
    private TextView rol;
    private int tamanio = 0;
    private int tamanio_productos = 0;
    private  int contador=0;
    private  int contador_productos=0;
    private int will = 0;
    private int will_1 = 0;
    public  static ArrayList<String> lista_data = new ArrayList<>();  //para guardar la lista de clientes
    public  static ArrayList<String> numero_ruta = new ArrayList<>();
    SweetAlertDialog pDialog;
    Map<String,String> lista_clientes_id = new HashMap<>();
    Map<String,String> lista_productos = new HashMap<>();
    Map<String,String> lista_productos_nombres = new HashMap<>();
    Map<String,String> lista_productos_id = new HashMap<>();
    Map<String,String> codigo_barra_producto = new HashMap<>();
    Map<String,String> producto_codigo_barra = new HashMap<>();
    Map<String,String> codigo_barra_precioCompra = new HashMap<>();
    Map<String,String> codigo_barra_precioVenta = new HashMap<>();
    Map<String,String> codigo_barra_distribucion= new HashMap<>();
    Map<String,String> id_precioCompra= new HashMap<>();
    Map<String,String> key_producto = new HashMap<>();
    Map<String,String> lista_peso_producto = new HashMap<>();

    private APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesion__usuario);

        apiInterface = APIClient.getClient();
        pDialog = new SweetAlertDialog(Sesion_Usuario.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2480D7"));
        pDialog.setTitleText("Espere ...");
        pDialog.setCancelable(false);

        final SharedPreferences setting0 = getSharedPreferences("login_preference", MODE_PRIVATE);
        final String value1 = setting0.getString("username", "");
        if (value1.equals("")) {
            finish();
        }

        //INICIAR sERVICIO sEGUNDO pLANO
        Intent intent =new Intent(this,ProgressIntentService.class);
        intent.setAction(Constaints.ACTION_RUN_ISERVICE);
        startService(intent);
        SharedPreferences setting = getSharedPreferences("login_preference", MODE_PRIVATE);
        String value = setting.getString("username", "");

        getUserInformation(value);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        correo = (TextView)headerView.findViewById(R.id.txtCorreo);
        nombre_completo = (TextView)headerView.findViewById(R.id.txtUsuario);
        rol = (TextView)headerView.findViewById(R.id.txtRol);
        correo.setText(value);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.nav_share){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Sesion_Usuario.this);
                    builder.setTitle("SALIR");
                    builder.setIcon(R.drawable.signo_interrogacion);
                    builder.setCancelable(false);
                    builder.setMessage("Seguro desea salir?");
                    builder.setMessage("Al salir, se borarran todos los datos descargados");
                    onBackPressed();
                    builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final Dialog dialog = new Dialog(Sesion_Usuario.this);
                            dialog.setCancelable(true);
                            dialog.setContentView(R.layout.dialog_pin_cerrar_sesionm);
                            final EditText editpin = (EditText)dialog.findViewById(R.id.EditPIN);
                            LinearLayout botonCerrar = (LinearLayout) dialog.findViewById(R.id.btnCerrar);

                            botonCerrar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String PIN = "1234567890";
                                    String PIN_R = editpin.getText().toString();
                                    if(PIN_R.equals(PIN)){
                                        File share_clientes = new File("/data/data/com.example.distrisandi/shared_prefs/");
                                        EliminarArchivos(new File("/data/data/com.example.distrisandi/files/"));
                                  //      EliminarArchivos_1(new File("/data/data/com.example.distrisandi/databases/"));
                                        EliminarArchivos_2(new File("/data/data/com.example.distrisandi/shared_prefs/"));


                                        SQLiteDatabase db = new AdminSQLiteOpenHelper(Sesion_Usuario.this,"administracion",null,1).getWritableDatabase();
                                        //  stopService(Sesion_Usuario.this.intent);
                                        db.execSQL("DELETE FROM venta_cliente");
                                        db.execSQL("DELETE FROM venta_detalles");
                                        db.execSQL("DELETE FROM detalles_productos");

                                        SQLiteDatabase db1 = new AdminSQLiteOpenHelper(Sesion_Usuario.this,"administracion1",null,1).getWritableDatabase();
                                        //  stopService(Sesion_Usuario.this.intent);
                                        db1.execSQL("DELETE FROM venta_cliente");
                                        db1.execSQL("DELETE FROM venta_detalles");
                                        db1.execSQL("DELETE FROM detalles_productos");
                                        try{
                                            Thread.sleep(2000);
                                            share_clientes.delete();
                                        }catch (InterruptedException e){

                                        }

                                        setting0.edit().clear().commit();
                                        finish();

                                    }else {
                                        Toast.makeText(Sesion_Usuario.this,"PIN incorrecto",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                            dialog.show();

                        }
                    });
                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                if(destination.getId() == R.id.nav_gallery){
                    alertDialogEditarPerfil();

                }
            }
        });
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void getUserInformation(String username){
        Call<String> response = apiInterface.datosUsuario(username);
        response.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    if(response.body() != null){
                        JSONObject result = new JSONObject(response.body());
                        String tipo_usuario = result.getString("id_userType");
                        String nombre = result.getString("fldname");
                        String apellido1 = result.getString("fldfirstName");
                        String apellido2 = result.getString("fldlastName");
                        if(tipo_usuario.equals("3")){
                            rol.setText("Vendedor");
                        }else{
                            rol.setText("Rol Desconcoido");
                        }
                        nombre_completo.setText(nombre + " " + apellido1 + " " + apellido2);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("Error_datos_usuario", t.getMessage());
                Toast.makeText(Sesion_Usuario.this, "Ocurrio un error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opciones_descargar_datos_inicio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.movimientos){
            onBackPressed();


            return true;
        }
        if (id == R.id.descargar){
            if(!isNetworkAvailable(this)){
                AlertDialog.Builder descarga_sinConexion = new AlertDialog.Builder(Sesion_Usuario.this);
                descarga_sinConexion.setIcon(R.drawable.ic_alerta);
                descarga_sinConexion.setTitle("SIN CONEXION");
                descarga_sinConexion.setMessage("No estas conectado a Internet");
                descarga_sinConexion.setCancelable(false);
                descarga_sinConexion.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                descarga_sinConexion.show();
            }else {
           /*     pDialog.getProgressHelper().setBarColor(Color.parseColor("#2480D7"));
                pDialog.setTitleText("Espere ...");*/
                String value = correo.getText().toString();
                consultaTamano(value);
              //  pDialog.setCancelable(false);
                pDialog.show();


            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void consultaTamano(String username){
        Call<String> response = apiInterface.listaClientesUsuario(username);
        response.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    if (response.body() != null) {
                        JSONObject result = new JSONObject(response.body());
                        String mensaje = result.getString("tamanio");
                        tamanio = Integer.parseInt(mensaje);
                        String correo_vendedor = correo.getText().toString();

                        for(contador=0;contador<tamanio;contador++){
                            listaClientesUsuario1(correo_vendedor, contador);
                            pDialog.setContentText("Descargando Clientes");
                        }
                        if(tamanio == 0){
                            if(pDialog.isShowing())
                                pDialog.dismissWithAnimation();
                            Toast.makeText(Sesion_Usuario.this,"NO HAY DATOS QUE DESCARGAR",Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if(pDialog.isShowing())
                        pDialog.dismissWithAnimation();
                Toast.makeText(Sesion_Usuario.this, "Ocurrio un error de conexión", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void listaClientesUsuario1(String correo, final int contador){
        Call<String> response = apiInterface.listaClientesUsuario1(correo, contador);
        response.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    lista_data.clear();
                    will = contador;
                    if (response.body() != null) {
                        JSONObject objeto_json = new JSONObject(response.body());
                        JSONArray jsonArray= objeto_json.getJSONArray("clientes");
                        for ( int k=0; k<jsonArray.length();k++){
                            final JSONObject jsonObject = jsonArray.getJSONObject(k);
                            String cliente_1 = jsonObject.getString("NombreCliente");
                            lista_data.add(cliente_1);

                            String ruta = jsonObject.getString("fldcash");
                            numero_ruta.add(jsonObject.getString("fldcash"));
                            String id_cliente = jsonArray.getJSONObject(k).getString("id_customer");
                            lista_clientes_id.put(cliente_1,id_cliente);
                            String lista_clientes_JSON = new Gson().toJson(lista_data);
                            String numero_ruta_JSON = new Gson().toJson(numero_ruta);

                            sharedPref = getSharedPreferences("lista_clientes_usuario",Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("lista_clientes", lista_clientes_JSON);
                            editor.putString("numero_ruta",ruta);
                            String lista_clientes_id_1 = new Gson().toJson(lista_clientes_id);
                            editor.putString("lista_clientes_id",lista_clientes_id_1).commit();
                            editor.apply();
                            //progressDialog.setMessage("Descargando Datos : \n" + will + "   "+"de" +"   "+ tamanio+ "   "+ "clientes");
                            pDialog.setContentText("Descargando Datos : \n" + will + "   "+"de" +"   "+ tamanio+ "   "+ "clientes");
                        }
                        if(will == tamanio-1){
                            SharedPreferences setting = getSharedPreferences("lista_clientes_usuario", MODE_PRIVATE);
                            String ruta_cliente = setting.getString("numero_ruta", "");
                            listaProductos(ruta_cliente);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if(pDialog.isShowing())
                        pDialog.dismissWithAnimation();
                Toast.makeText(Sesion_Usuario.this, "Ocurrio un error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listaProductos(String rutaCliente){
        pDialog.setContentText("Descargando lista de productos");

        Call<String> response = apiInterface.listaProductos(rutaCliente);
        response.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try{
                    if(response.body() != null){
                        JSONObject result = new JSONObject(response.body());
                        String mensaje = result.getString("tamanio_1");
                        tamanio_productos = Integer.parseInt(mensaje);
                        for(contador_productos = 0;contador_productos<tamanio_productos;contador_productos++){
                            SharedPreferences setting = getSharedPreferences("lista_clientes_usuario", MODE_PRIVATE);
                            String ruta_cliente = setting.getString("numero_ruta", "");
                            listaProductos1(ruta_cliente, contador_productos);
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if(pDialog.isShowing())
                        pDialog.dismissWithAnimation();
                Toast.makeText(Sesion_Usuario.this, "Ocurrio un error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listaProductos1(String rutaCliente, final int contador){
        SharedPreferences setting = getSharedPreferences("login_preference", MODE_PRIVATE);
        String value = setting.getString("username", "");
        Call<String> response = apiInterface.listaProductos1(rutaCliente, contador, value);
        response.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                will_1 = contador;

                SQLiteDatabase bd = null;
                try{
                    if(response.body()!=null){

                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(Sesion_Usuario.this,"administracion",null,1);
                        bd =admin.getWritableDatabase();

                        JSONObject jsonObject = new JSONObject(response.body());
                        if(jsonObject.has("message")){
                            Toast.makeText(Sesion_Usuario.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }else {
                            JSONArray jsonArray = jsonObject.getJSONArray("productos");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String id_product = jsonArray.getJSONObject(i).getString("id_product");
                                String codigo_barra = jsonArray.getJSONObject(i).getString("fldbarCode");
                                String fldname = jsonArray.getJSONObject(i).getString("fldname");
                                String precio = jsonArray.getJSONObject(i).getString("fldpurchasePrice");
                                String precion_venta = jsonArray.getJSONObject(i).getString("fldsalePrice");
                                String cantidad_producto = jsonArray.getJSONObject(i).getString("flddistributionAmount");
                                String key_prod = jsonArray.getJSONObject(i).getString("fldkey");
                                String peso_producto = jsonArray.getJSONObject(i).getString("fldweightInKg");
                                lista_productos.put(fldname, precion_venta);
                                lista_productos_nombres.put(fldname, "");
                                lista_productos_id.put(fldname, id_product);
                                codigo_barra_producto.put(codigo_barra, fldname);
                                producto_codigo_barra.put(fldname, codigo_barra);
                                codigo_barra_precioCompra.put(codigo_barra, precio);
                                id_precioCompra.put(id_product, precio);
                                codigo_barra_precioVenta.put(codigo_barra, precion_venta);
                                codigo_barra_distribucion.put(codigo_barra, cantidad_producto);
                                key_producto.put(key_prod, fldname);
                                lista_peso_producto.put(fldname, peso_producto);
                                String lista_productos_string = new Gson().toJson(lista_productos);
                                String lista_productos_string1 = new Gson().toJson(lista_productos_nombres);
                                String lista_producto_codigo_barra = new Gson().toJson(codigo_barra_producto);
                                String lista_codigo_producto = new Gson().toJson(producto_codigo_barra);
                                String lista_producto_id = new Gson().toJson(lista_productos_id);
                                String lista_codigo_barra_precioCompra = new Gson().toJson(codigo_barra_precioCompra);
                                String lista_id_precioCompra = new Gson().toJson(id_precioCompra);
                                String lista_key_producto = new Gson().toJson(key_producto);
                                String peso_producto_lista = new Gson().toJson(lista_peso_producto);

                                SharedPreferences sharedPreferences = getSharedPreferences("productos", Context.MODE_PRIVATE);
                                sharedPreferences.edit().putString("lista_productos", lista_productos_string).apply();
                                sharedPreferences.edit().putString("lista_productos_nombres", lista_productos_string1).apply();
                                sharedPreferences.edit().putString("lista_producto_codigo_barra", lista_producto_codigo_barra).apply();
                                sharedPreferences.edit().putString("lista_codigo_producto", lista_codigo_producto).apply();
                                sharedPreferences.edit().putString("lista_productos_id", lista_producto_id).apply();
                                sharedPreferences.edit().putString("lista_productos_precioCompra", lista_codigo_barra_precioCompra).apply();
                                sharedPreferences.edit().putString("lista_id_precioCompra", lista_id_precioCompra).apply();
                                sharedPreferences.edit().putString("lista_key_producto", lista_key_producto).apply();
                                sharedPreferences.edit().putString("lista_peso_producto", peso_producto_lista).apply();
                                //sharedPreferences.edit().putString("lista_productos_distribucion",lista_producto_distribucion).commit();
                                //progressDialog.setMessage("Descargando Datos : \n" + jose_1 + "   "+"de" +"   "+ tamanio_productos+ "   "+ "Productos");
                                pDialog.setContentText("Descargando Datos : \n" + will_1 + "   " + "de" + "   " + tamanio_productos + "   " + "Productos");


                                //GUARDAR EN LA BASE DE DATOS DETALLES DE LOS PRODUCTOS
                                // AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(Sesion_Usuario.this,"administracion",null,1);
//                        SQLiteDatabase bd =admin.getWritableDatabase();
                                ContentValues registro = new ContentValues();
                                registro.put("codigo_producto", codigo_barra);
                                registro.put("nombre_producto", fldname);
                                registro.put("stock_producto", cantidad_producto);
                                registro.put("precio_venta_producto", precion_venta);
                                registro.put("id_producto", id_product);
                                registro.put("key_producto", key_prod);
                                registro.put("peso_producto", peso_producto);
                                bd.insert("detalles_productos", null, registro);
                            }

                            if (jsonObject.has("ventasCanceladas")) {
                                JSONArray jsonArrayCanceladas = jsonObject.getJSONArray("ventasCanceladas");
                                for (int i = 0; i < jsonArrayCanceladas.length(); i++) {
                                    ContentValues actualizarRegistro = new ContentValues();
                                    actualizarRegistro.put("cancelado", jsonArrayCanceladas.getJSONObject(i).getString("fldcanceled"));
                                    bd.update("venta_cliente", actualizarRegistro, "id_cliente=? AND folio=?", new String[]{jsonArrayCanceladas.getJSONObject(i).getString("id_customer"), jsonArrayCanceladas.getJSONObject(i).getString("id_productSale")});
                                }
                            }

                            ContentValues actualizarRegistro = new ContentValues();
                            actualizarRegistro.put("postActualizacionRegistro", "1");
                            bd.update("venta_cliente", actualizarRegistro, "", null);


                            if (will_1 == tamanio_productos - 1) {
                                Toast.makeText(Sesion_Usuario.this, "DATOS DESCARGADOS", Toast.LENGTH_SHORT).show();
                                //progressDialog.dismiss();
                            }

                            Log.e("tamanio_1111", String.valueOf(will_1) + tamanio_productos);
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }finally {
                    if(bd != null)
                        bd.close();
                    if(pDialog.isShowing())
                        pDialog.dismissWithAnimation();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if(pDialog.isShowing())
                        pDialog.dismissWithAnimation();
                Toast.makeText(Sesion_Usuario.this, "Ocurrio un error de conexión", Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void alertDialogEditarPerfil(){

        AlertDialog.Builder builder = new AlertDialog.Builder(Sesion_Usuario.this);
        builder.setTitle("EDITAR PERFIL");
        builder.setMessage("No disponible...");
        // builder.setIcon(R.drawable.descargar);
        builder.setCancelable(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onBackPressed();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
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
    void EliminarArchivos(File ArchivoDirectorio){
        if(ArchivoDirectorio.isDirectory()){
            if(ArchivoDirectorio.list().length==0){
                ArchivoDirectorio.delete();
            }else {
                for(String temp:ArchivoDirectorio.list()){
                    File filedelete = new File(ArchivoDirectorio,temp);
                    EliminarArchivos((filedelete));
                }
                if(ArchivoDirectorio.list().length==0){
                    ArchivoDirectorio.delete();
                }
            }
        }else {
            ArchivoDirectorio.delete();
        }
    }
    void EliminarArchivos_1(File ArchivoDirectorio){
        if(ArchivoDirectorio.isDirectory()){
            if(ArchivoDirectorio.list().length==0){
                ArchivoDirectorio.delete();
            }else {
                for(String temp:ArchivoDirectorio.list()){
                    File filedelete = new File(ArchivoDirectorio,temp);
                    EliminarArchivos((filedelete));
                }
                if(ArchivoDirectorio.list().length==0){
                    ArchivoDirectorio.delete();
                }
            }
        }else {
            ArchivoDirectorio.delete();
        }
    }
    void EliminarArchivos_2(File ArchivoDirectorio){
        if(ArchivoDirectorio.isDirectory()){
            if(ArchivoDirectorio.list().length==0){
                ArchivoDirectorio.delete();
            }else {
                for(String temp:ArchivoDirectorio.list()){
                    File filedelete = new File(ArchivoDirectorio,temp);
                    EliminarArchivos((filedelete));
                }
                if(ArchivoDirectorio.list().length==0){
                    ArchivoDirectorio.delete();
                }
            }
        }else {
            ArchivoDirectorio.delete();
        }
    }

}