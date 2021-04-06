
package com.example.distrisandi;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.os.AsyncTask;
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
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.example.distrisandi.JSONParser.json;


public class Sesion_Usuario extends AppCompatActivity{

   String URL = "https://www.sandiz.com.mx/failisa/WebService/datos_usuario.php";
    String URL1 = "https://www.sandiz.com.mx/failisa/WebService/lista_clientes_usuario.php";//lista de clientes
    String URL2 = "https://www.sandiz.com.mx/failisa/WebService/lista_productos.php";
    String URL4 = "https://www.sandiz.com.mx/failisa/WebService/lista_productos_1.php";
    String URL3 = "https://www.sandiz.com.mx/failisa/WebService/lista_clientes_usuario_1.php";//lista de clientes*/
    /*String URL = "https://localhost/failisa/WebService/datos_usuario.php";
    String URL1 = "https://localhost/failisa/WebService/lista_clientes_usuario.php";//lista de clientes
    String URL2 = "https://localhost/failisa/WebService/lista_productos.php";
    String URL4 = "https://localhost/failisa/WebService/lista_productos_1.php";
    String URL3 = "https://localhost/failisa/WebService/lista_clientes_usuario_1.php";//lista de clientes*/
    JSONParser jsonParser = new JSONParser();
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
    public  static ArrayList<String> lista_data = new ArrayList<String>();  //para guardar la lista de clientes
    public  static ArrayList<String> numero_ruta = new ArrayList<>();
    private ProgressDialog progressDialog;
    SweetAlertDialog pDialog;
    public  static ArrayList<String> lista_productod_json = new ArrayList<>();
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
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences setting0 = getSharedPreferences("login_preference", MODE_PRIVATE);
        final String value1 = setting0.getString("username", "");
        if (value1.equals("")) {
            finish();
        }
        setContentView(R.layout.activity_sesion__usuario);

        //INICIAR sERVICIO sEGUNDO pLANO
        Intent intent =new Intent(this,ProgressIntentService.class);
        intent.setAction(Constaints.ACTION_RUN_ISERVICE);
        startService(intent);
        SharedPreferences setting = getSharedPreferences("login_preference", MODE_PRIVATE);
        String value = setting.getString("username", "");
        AttempLogin attempLogin = new AttempLogin();
        attempLogin.execute(value, "");
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
                                        EliminarArchivos_1(new File("/data/data/com.example.distrisandi/databases/"));
                                        EliminarArchivos_2(new File("/data/data/com.example.distrisandi/shared_prefs/"));
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

                else {
                } }
        });
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
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
            File share_clientes = new File("/data/data/com.example.distrisandi/shared_prefs/lista_clientes_usuario.xml");
            File share_productos = new File("/data/data/com.example.distrisandi/shared_prefs/productos.xml");
            if (share_clientes.exists() && share_productos.exists()){
                Toast.makeText(Sesion_Usuario.this,"LOS DATOS YA ESTAN DESCARGADOS", Toast.LENGTH_SHORT).show();
            }

            else{
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

                   /* progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Descarga de Datos");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    */
                    /*
                    String value = correo.getText().toString();
                    tamanioConsulta tamanioConsulta = new tamanioConsulta();
                    tamanioConsulta.execute(value, "");
                     */

                    pDialog = new SweetAlertDialog(Sesion_Usuario.this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#2480D7"));
                    pDialog.setTitleText("Espere ...");
                    String value = correo.getText().toString();
                    tamanioConsulta tamanioConsulta = new tamanioConsulta();
                    tamanioConsulta.execute(value, "");
                    pDialog.setCancelable(false);
                    pDialog.show();


                }
            }

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }




    //DESCARGAR DATOS DEL USUARIO DEL VENDEDOR
    private class AttempLogin extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String name = args[0];

            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("username", name));
            JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
            return json;
        }
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
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
                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    //DESCARGAR LISTA CLIENTES DEL VENDEDOR
    private class tamanioConsulta extends  AsyncTask<String, String, JSONObject>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String username = args[0];

            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("username", username));
            JSONObject json = jsonParser.makeHttpRequest(URL1, "POST", params);
            return json;

        }
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    String mensaje = result.getString("tamanio");
                    tamanio = Integer.parseInt(mensaje);

                    String correo_vendedor = correo.getText().toString();

                    for(contador=0;contador<tamanio;contador++){

                        ListaClientes_1a listaClientes_1a = new ListaClientes_1a();
                        listaClientes_1a.execute(correo_vendedor,String.valueOf(contador),"");
                        //progressDialog.setMessage("Descargando Clientes");
                        pDialog.setContentText("Descargando Clientes");

                    }
                    if(tamanio == 0){
                        //progressDialog.dismiss();
                        pDialog.dismissWithAnimation();
                        Toast.makeText(Sesion_Usuario.this,"NO HAY DATOS QUE DESCARGAR",Toast.LENGTH_SHORT).show();
                    }


                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //DESCARBAR LISTA CLIENTES DEL VENDEDOR
private class ListaClientes_1a extends  AsyncTask<String, String, JSONObject>{
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... args) {
        String username = args[0];
        String contador = args[1];
        will = Integer.parseInt(contador);
        Log.e("contador",String.valueOf(will));


        ArrayList params = new ArrayList();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("contador", contador));
        JSONObject json = jsonParser.makeHttpRequest(URL3, "POST", params);
        return json;
    }
    protected void onPostExecute(JSONObject result) {
        try {
            if (result != null) {
                JSONObject objeto_json = new JSONObject(json);
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
                    String value = correo.getText().toString();
                    ListaProductos listaProductos = new ListaProductos();
                    listaProductos.execute(ruta_cliente,"");
                }

            } else {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


    //DESCARGAR LISTA DE PRODUCTOS DEL VENDEDOR
    private class ListaProductos extends AsyncTask<String, String, JSONObject>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            //progressDialog.setMessage("Descargando lista de productos");
            pDialog.setContentText("Descargando lista de productos");
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String route = args[0];
            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("route",route));
            JSONObject json = jsonParser.makeHttpRequest(URL2, "POST",params);
            return json;
        }
        protected void onPostExecute(JSONObject result){
            try{
                if(result!=null){
                    String mensaje = result.getString("tamanio_1");
                    tamanio_productos = Integer.parseInt(mensaje);
                    for(contador_productos = 0;contador_productos<tamanio_productos;contador_productos++){
                        SharedPreferences setting = getSharedPreferences("lista_clientes_usuario", MODE_PRIVATE);
                        String ruta_cliente = setting.getString("numero_ruta", "");
                        ListaProductos_1 listaProductos_1 = new ListaProductos_1();
                        listaProductos_1.execute(ruta_cliente,String.valueOf(contador_productos),"");
                    }


                }else{

                }
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
    private class ListaProductos_1 extends AsyncTask<String, String, JSONObject>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            //progressDialog.setMessage("Descargando lista de productos");
            pDialog.setContentText("Descargando lista de productos");
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String limite = args[1];
            String route = args[0];
            will_1 = Integer.parseInt(limite);
            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("limite",limite));
            params.add(new BasicNameValuePair("route",route));
            JSONObject json = jsonParser.makeHttpRequest(URL4, "POST",params);
            return json;
        }
        protected void onPostExecute(JSONObject result){
            try{
                if(result!=null){
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray jsonArray = jsonObject.getJSONArray("productos");
                    for ( int i=0;i<jsonArray.length();i++){
                        String id_product = jsonArray.getJSONObject(i).getString("id_product");
                        String codigo_barra = jsonArray.getJSONObject(i).getString("fldbarCode");
                        String fldname = jsonArray.getJSONObject(i).getString("fldname");
                        String precio = jsonArray.getJSONObject(i).getString("fldpurchasePrice");
                        String precion_venta = jsonArray.getJSONObject(i).getString("fldsalePrice");
                        String cantidad_producto = jsonArray.getJSONObject(i).getString("flddistributionAmount");
                        String key_prod = jsonArray.getJSONObject(i).getString("fldkey");
                        lista_productos.put(fldname,precion_venta);
                        lista_productos_nombres.put(fldname,"");
                        lista_productos_id.put(fldname,id_product);
                        codigo_barra_producto.put(codigo_barra,fldname);
                        producto_codigo_barra.put(fldname,codigo_barra);
                        codigo_barra_precioCompra.put(codigo_barra,precio);
                        id_precioCompra.put(id_product,precio);
                        codigo_barra_precioVenta.put(codigo_barra,precion_venta);
                        codigo_barra_distribucion.put(codigo_barra,cantidad_producto);
                        key_producto.put(key_prod,fldname);
                        String lista_productos_string = new Gson().toJson(lista_productos);
                        String lista_productos_string1 = new Gson().toJson(lista_productos_nombres);
                        String lista_producto_codigo_barra= new Gson().toJson(codigo_barra_producto);
                        String lista_codigo_producto = new Gson().toJson(producto_codigo_barra);
                        String lista_producto_id = new Gson().toJson(lista_productos_id);
                        String lista_codigo_barra_precioCompra = new Gson().toJson(codigo_barra_precioCompra);
                        String lista_id_precioCompra = new Gson().toJson(id_precioCompra);
                        String lista_key_producto = new Gson().toJson(key_producto);
                        SharedPreferences sharedPreferences = getSharedPreferences("productos",Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("lista_productos",lista_productos_string).commit();
                        sharedPreferences.edit().putString("lista_productos_nombres",lista_productos_string1).commit();
                        sharedPreferences.edit().putString("lista_producto_codigo_barra",lista_producto_codigo_barra).commit();
                        sharedPreferences.edit().putString("lista_codigo_producto",lista_codigo_producto).commit();
                        sharedPreferences.edit().putString("lista_productos_id",lista_producto_id).commit();
                        sharedPreferences.edit().putString("lista_productos_precioCompra",lista_codigo_barra_precioCompra).commit();
                        sharedPreferences.edit().putString("lista_id_precioCompra",lista_id_precioCompra).commit();
                        sharedPreferences.edit().putString("lista_key_producto",lista_key_producto).commit();
                        //sharedPreferences.edit().putString("lista_productos_distribucion",lista_producto_distribucion).commit();
                        String objetos = sharedPreferences.getString("lista_productos","");
                        //progressDialog.setMessage("Descargando Datos : \n" + jose_1 + "   "+"de" +"   "+ tamanio_productos+ "   "+ "Productos");
                        pDialog.setContentText("Descargando Datos : \n" + will_1 + "   "+"de" +"   "+ tamanio_productos+ "   "+ "Productos");


                        //GUARDAR EN LA BASE DE DATOS DETALLES DE LOS PRODUCTOS
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(Sesion_Usuario.this,"administracion",null,1);
                        SQLiteDatabase bd =admin.getWritableDatabase();
                        ContentValues registro = new ContentValues();
                        registro.put("codigo_producto",codigo_barra);
                        registro.put("nombre_producto",fldname);
                        registro.put("stock_producto",cantidad_producto);
                        registro.put("precio_venta_producto",precion_venta);
                        registro.put("id_producto",id_product);
                        registro.put("key_producto",key_prod);
                        bd.insert("detalles_productos",null,registro);




                    }

                    if(will_1 == tamanio_productos-1){
                        Toast.makeText(Sesion_Usuario.this,"DATOS DESCARGADOS",Toast.LENGTH_SHORT).show();
                        //progressDialog.dismiss();
                        pDialog.dismissWithAnimation();
                    }

                    Log.e("tamanio_1111",String.valueOf(will_1)+ tamanio_productos);


                }else{

                }
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
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