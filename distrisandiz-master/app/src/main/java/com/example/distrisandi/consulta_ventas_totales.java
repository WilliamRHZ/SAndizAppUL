     package com.example.distrisandi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class consulta_ventas_totales extends AppCompatActivity  {
    private ListView listView_ventas;
    final ArrayList<String> ventas = new ArrayList<>();
    final ArrayList<String> total_venta = new ArrayList<>();
    final ArrayList<String>lista_estados = new ArrayList<>();
    private boolean estado= false;
    JSONParser jsonParser = new JSONParser();
   /* String URL = "http://10.0.2.2/sandiz/WebService/productos_vendidos.php";
    String URL_json = "http://10.0.2.2/sandiz/WebService/productos_vendidos_detalles.php";*/
   String URL = "https://www.sandiz.com.mx/sandiztapachula/WebService/productos_vendidos.php";
    String URL_json = "https://www.sandiz.com.mx/sandiztapachula/WebService/productos_vendidos_detalles.php";
    /*String URL = "https://www.sandiz.com.mx/failisa/WebService/productos_vendidos.php";
    String URL_json = "https://www.sandiz.com.mx/failisa/WebService/productos_vendidos_detalles.php";*/
    private String id_cliente="";
    private String id_tipoOperacion="";
    private String id_estadoOperacion="";

    //BLUETOOTH
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    BluetoothDevice bluetoothDevice;
    OutputStream outputStream;
    InputStream inputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    String value = "";
    private String mac_bluetooth;
    private String numero_ruta_vendedor;
    Map<String,String> map_cliente_id = new HashMap<String, String>();
    Map<String,String> map_cliente_id_1 = new HashMap<String, String>();
    Map<String,String> map_cliente_id_subir = new HashMap<String, String>();
    private Context context;
    private String txtFolio;
    ProgressDialog progresoSubiendo;
    ProgressDialog progressoDialogMasivo;

    private String folio_nuevo;
    @Override
    protected void onCreate(Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        SharedPreferences share_nom_cliente = getSharedPreferences("lista_clientes_usuario", MODE_PRIVATE);
        String obj_cliente = share_nom_cliente.getString("lista_clientes_id","");
        String obj_clientes = obj_cliente.replaceAll("[^\\dA-Za-z, :]","");
        String[] pairs_cliente = obj_clientes.split(",");
        for(int i = 0;i<pairs_cliente.length;i++) {
            try{
                String pair_cliente = pairs_cliente[i];
                Log.e("kkkkk",pair_cliente);
                String[] keyvalue_cliente = pair_cliente.split(":");
                map_cliente_id_1.put(keyvalue_cliente[1], String.valueOf(keyvalue_cliente[0]));
                map_cliente_id_subir.put(keyvalue_cliente[0], String.valueOf(keyvalue_cliente[1]));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
            //CONSULTAR EN LA BD TODAS LAS VENTAS
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion",null,1);
        SQLiteDatabase bd = admin.getWritableDatabase();
        Cursor fila = bd.rawQuery("select * from venta_cliente",null);

        String[]encabezados = new String[]{"Folio","Total","Estado","Nombre_Cliente"};
        final int[]to = new int[]{R.id.txtFolio_vendido,R.id.txtTotal_vendido,R.id.txtEstado_vendido,R.id.txtNombrecliente};
        List<HashMap<String,String>>fillMaps = new ArrayList<HashMap<String,String>>();
        if(fila.moveToFirst()){
            do{
                HashMap<String,String>map = new HashMap<String,String>();
                map.put("Folio",fila.getString(0));
                ventas.add(fila.getString(0));
                map.put("Total",fila.getString(1));
                total_venta.add(fila.getString(1));
                map.put("Estado",fila.getString(4));
                lista_estados.add(fila.getString(4));
                map.put("Nombre_Cliente",map_cliente_id_1.get(fila.getString(2)));
                fillMaps.add(map);
            }while (fila.moveToNext());
        }
        bd.close();

        //OBTENER ESTADO DE INTERNET
        if (!isNetworkAvailable(this)) {
                estado=false;
        }
        else {
            estado=true;

        }
        SharedPreferences sharedPreferences1 = getSharedPreferences("bluetooth_info", MODE_PRIVATE);
        mac_bluetooth= sharedPreferences1.getString("mac_bluetooth","");
        setContentView(R.layout.activity_consulta_ventas_totales);

//
        SharedPreferences sharedPref = getSharedPreferences("lista_clientes_usuario", Context.MODE_PRIVATE);
        numero_ruta_vendedor = sharedPref.getString("numero_ruta","");


        /////////////////////////////////////////////////////////////////
        //INSTANCIAR LISTVIEW
        listView_ventas = (ListView)findViewById(R.id.listview_venta_totales);
        //CREAR EL ADAPTADOR
        final SimpleAdapter adapter = new SimpleAdapter(this,fillMaps,R.layout.item_ventas_totales,encabezados,to){
            @Override
            public View getView(int position,View convertView,ViewGroup parent){
                View view = super.getView(position,convertView,parent);
                TextView tEstado = view.findViewById(R.id.txtEstado_vendido);
                TextView tTotal = view.findViewById(R.id.txtTotal_vendido);
                TextView tFolio = view.findViewById(R.id.txtFolio_vendido);
                TextView tNombre = view.findViewById(R.id.txtNombrecliente);
                TextView tSigno = view.findViewById(R.id.tSigno);

                AdminSQLiteOpenHelper consul_folio = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion",null,1);
                SQLiteDatabase sql_folio = consul_folio.getReadableDatabase();
                Cursor cursor_folio = sql_folio.rawQuery("select cancelado, tipo_operacion from venta_cliente where folio='"+tFolio.getText().toString()+"'",null);
                String folio_sql="";

                if(cursor_folio!=null){
                    if(cursor_folio.moveToFirst()){
                        do{
                            //folio_sql = cursor_folio.getString(0);
                            //int posicion_item = ventas.indexOf(folio_sql);
                            //Log.e("folios_canc",String.valueOf(posicion_item));
                            String cancelado = cursor_folio.getString(0);
                            if(cancelado.equals("1")/*position == posicion_item*/) {
                                tEstado.setText(tEstado.getText() + "/Cancelado");
                                tEstado.setTextColor(Color.WHITE);
                                tTotal.setTextColor(Color.WHITE);
                                tFolio.setTextColor(Color.WHITE);
                                tNombre.setTextColor(Color.WHITE);
                                tSigno.setTextColor(Color.WHITE);
                                view.setBackgroundColor(Color.RED);
                            }else if(cursor_folio.getString(0).equals("0")/*position==0*/){
                                if(cursor_folio.getString(1).equals("2")){
                                    tEstado.setTextColor(Color.WHITE);
                                    tTotal.setTextColor(Color.WHITE);
                                    tFolio.setTextColor(Color.WHITE);
                                    tNombre.setTextColor(Color.WHITE);
                                    tSigno.setTextColor(Color.WHITE);
                                    view.setBackgroundColor(Color.BLUE);
                                }else{
                                    tEstado.setTextColor(Color.BLACK);
                                    tTotal.setTextColor(Color.BLACK);
                                    tFolio.setTextColor(Color.parseColor("#1A237E"));
                                    tNombre.setTextColor(Color.BLACK);
                                    tSigno.setTextColor(Color.BLACK);
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                }
                            }
                        }
                        while (cursor_folio.moveToNext());
                    }
                }
               return view;
            }
        };
        //PASAR EL ADAPTER AL LISTVIEW
        listView_ventas.setAdapter(adapter);
        //OBTENER FECHA
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat fecha = new SimpleDateFormat("dd-MM-yyyy");
        final String fecha_actual = fecha.format(c.getTime());
        int f_hora = c.get(Calendar.HOUR_OF_DAY);
        int f_min= c.get(Calendar.MINUTE);
        int f_sec = c.get(Calendar.SECOND);
        final String hora_actual = String.valueOf(f_hora)+":"+String.valueOf(f_min)+":"+String.valueOf(f_sec);
        Log.e("willwillwill",fecha_actual);

        //CLICK EN LISTVIEW
        listView_ventas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //obtener posiccion del click
                //Log.e("folio_nuevo",folio_nuevo_r);
                String posicion = String.valueOf(adapterView.getItemIdAtPosition(i));
                //convertir a entero
                //int pos = Integer.parseInt(posicion);
                //obtener folio de la venta
                final TextView textFolio = (TextView)view.findViewById(R.id.txtFolio_vendido);
                final TextView txtnombreCilente = (TextView)view.findViewById(R.id.txtNombrecliente);
                txtFolio = textFolio.getText().toString();
                //Log.e("foliofolio",txtFolio);
                //obtener nombre cliente
                SharedPreferences sharedPreferences1 = getSharedPreferences("lista_clientes_usuario", MODE_PRIVATE);
                String obj_cliente = sharedPreferences1.getString("lista_clientes_id","");
                String lis_cliente = obj_cliente.replaceAll("[^\\dA-Za-z, :]","");
                String[] pais_cliente = lis_cliente.split(",");
                for(int k = 0;k<pais_cliente.length;k++){
                    String pair = pais_cliente[k];
                    String[]keyvalue = pair.split(":");
                    map_cliente_id.put(keyvalue[1], String.valueOf(keyvalue[0]));
                    Log.e("nombre_cliente",keyvalue[1] + "="+ keyvalue[0]);
                }
                //consultar en la base de datos
                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion",null,1);
                SQLiteDatabase bd = admin.getWritableDatabase();
                final Cursor fila = bd.rawQuery("select estado, id_cliente,tipo_operacion ,estado_operacion from venta_cliente where folio" +"='" +txtFolio+"'" ,null);
                String valor = "";
                    if(fila!=null){
                        if(fila.moveToFirst()){
                            valor=fila.getString(0);
                            id_cliente = fila.getString(1);
                            id_tipoOperacion = fila.getString(2);
                            id_estadoOperacion = fila.getString(3);
                            final String estado_de_operacion;
                            if(id_tipoOperacion.equals("1")){
                                estado_de_operacion="CONTADO";
                            }else {
                                estado_de_operacion="CREDITO";
                            }
                            Log.e("estadooperacionnn",id_tipoOperacion);
                            if(valor.equals("Subido")) {
                                final String nombre_cliente_vendido = map_cliente_id.get(id_cliente);
                                new SweetAlertDialog(consulta_ventas_totales.this, SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("IMPRIMIR")
                                        .setContentText("Desea imprimir esta venta?")
                                        .setConfirmText("SI")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                if(mac_bluetooth.equals("")){
                                                    Toast.makeText(consulta_ventas_totales.this,"No hay impresora conectada",Toast.LENGTH_SHORT).show();
                                                }
                                                else {
                                                    AdminSQLiteOpenHelper admin_reimprimir = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion1",null,1);
                                                    SQLiteDatabase bd_reimprimir = admin_reimprimir.getWritableDatabase();
                                                    String folio_venta = txtFolio;
                                                    Log.e("jojojojo",folio_venta);
                                                    final Cursor fila_reimprimir = bd_reimprimir.rawQuery("select codigo_producto,cantidad_vendido,precio_venta from venta_detalles where folio" +"='" +txtFolio+"'" ,null);
                                                    final Thread t = new Thread(){
                                                        @Override
                                                        public void run(){
                                                            try{
                                                                double total_venta = 0.00;
                                                                IntentPrint("\n    COMERCIALIZADORA FAILI"+
                                                                        "\n          S.A.de C.V "+
                                                                        "\nCalzada Jorge Gomez # 203 Col"+
                                                                        "\nCerro Hueco, Tuxtla Gutierrez"+
                                                                        "\n       Chis., Mex. "+
                                                                        "\n"+
                                                                        "\nRFC:EESN700923QR2     "+ fecha_actual+
                                                                        "\nHORA:" + hora_actual+ "          RUTA:"+numero_ruta_vendedor+
                                                                        "\nCLIENTE:" + nombre_cliente_vendido +
                                                                        "\nFOLIO:   " + txtFolio +
                                                                        "\n"+
                                                                        "\n          "+estado_de_operacion +
                                                                        "\n--------------------------------"+
                                                                        "\nDESCRIPCION"+
                                                                        "\nCANTIDAD       PRECIO      TOTAL"+
                                                                        "\n--------------------------------"+"\n");
                                                                Thread.sleep(900);
                                                                double total_venta = 0.00;
                                                                if(fila_reimprimir.moveToFirst()){
                                                                    do{
                                                                        String codigo = fila_reimprimir.getString(0);
                                                                        String cantidad = fila_reimprimir.getString(1);
                                                                        String subtotal = fila_reimprimir.getString(2);
                                                                        double subtotal_producto = Double.parseDouble(cantidad) * Double.parseDouble(subtotal);
                                                                        total_venta += subtotal_producto;
                                                                        AdminSQLiteOpenHelper admin_nombre_producto = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion",null,1);
                                                                        SQLiteDatabase bd_nombre_producto = admin_nombre_producto.getWritableDatabase();
                                                                        Cursor fila_nombre_producto = bd_nombre_producto.rawQuery("select nombre_producto from detalles_productos where id_producto" +"='" +codigo+"' limit 1" ,null);
                                                                        String nombre_producto="";
                                                                        if(fila_nombre_producto.moveToFirst()){
                                                                            nombre_producto = fila_nombre_producto.getString(0);
                                                                        }
                                                                        IntentPrint("\n"+nombre_producto +
                                                                                "\n"+cantidad+"         "+"$"+subtotal+ "         "+"$"+ String.valueOf(subtotal_producto));
                                                                        Thread.sleep(150);
                                                                    }while (fila_reimprimir.moveToNext());
                                                                }
                                                                Thread.sleep(200);
                                                                IntentPrint("\n--------------------------------"+
                                                                        "\n"+"          Total :      $" + total_venta+
                                                                        "\n"+
                                                                        "\n--------------------------------"+
                                                                        "\n    Gracias por su compra!!"+      //CORRECION EN LA IMPRESION DEL TICKET ..CRED Y CONT---------------------------------------------------------------------
                                                                        "\nEl importe de esta nota sera"+
                                                                        "\naplicada a la factura del dia."+
                                                                        "\n"+
                                                                        "\n"+
                                                                        "\n");
                                                                try {
                                                                    Thread.sleep(200);
                                                                    Toast.makeText(consulta_ventas_totales.this,"IMPRESION TERMINADA",Toast.LENGTH_LONG).show();
                                                                    Log.e("wil234","impresion terminada");
                                                                }catch (InterruptedException e){

                                                                }
                                                            }catch (Exception e){

                                                            }
                                                        }
                                                    };
                                                    t.start();

                                                }
                                                sDialog.dismissWithAnimation();
                                            }
                                        })
                                        .setCancelButton("NO", new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                sDialog.dismissWithAnimation();
                                            }
                                        })
                                        .show();

                            }
                            else if(valor.equals("No subido")){
                                final Dialog dialog = new Dialog(consulta_ventas_totales.this);
                                dialog.setCancelable(true);
                                dialog.setContentView(R.layout.dialog_detalles_ventas);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.show();
                                TextView texto_estado = (TextView)dialog.findViewById(R.id.txtestadoVenta);
                                texto_estado.setText("Su venta no esta guardada en el servidor");
                                LinearLayout botonCancelar= (LinearLayout)dialog.findViewById(R.id.btnCancelar);
                                LinearLayout botonSubirVenta = (LinearLayout)dialog.findViewById(R.id.btnSubirVenta);
                                LinearLayout botonImprimir = (LinearLayout)dialog.findViewById(R.id.btnImprimir);
                                botonCancelar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AdminSQLiteOpenHelper cancelar_venta = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion",null,1);
                                        SQLiteDatabase sql_cancelarVenta = cancelar_venta.getReadableDatabase();
                                        Cursor cursor_cancelar = sql_cancelarVenta.rawQuery("select cancelado from venta_cliente where folio "+"='"+txtFolio+"'",null);
                                        if(cursor_cancelar!=null){
                                            if(cursor_cancelar.moveToFirst()){
                                                if(cursor_cancelar.getString(0).equals("0")) {

                                                    Log.e("tipo_operacion", cursor_cancelar.getString(0));
                                                    AdminSQLiteOpenHelper cancelar = new AdminSQLiteOpenHelper(consulta_ventas_totales.this, "administracion", null, 1);
                                                    SQLiteDatabase sql_cancelar = cancelar.getReadableDatabase();
                                                    ContentValues cont_cancelar = new ContentValues();
                                                    cont_cancelar.put("cancelado", "1");
                                                    sql_cancelar.update("venta_cliente", cont_cancelar, "folio=?", new String[]{txtFolio});
                                                    AdminSQLiteOpenHelper regresar = new AdminSQLiteOpenHelper(consulta_ventas_totales.this, "administracion1", null, 1);
                                                    SQLiteDatabase bd_regresar = regresar.getWritableDatabase();
                                                    Cursor fila_regresar = bd_regresar.rawQuery("select folio,codigo_producto,cantidad_vendido from venta_detalles where folio " + "='" + txtFolio + "'", null);
                                                    if (fila_regresar.moveToFirst()) {
                                                        do {
                                                            String folio = fila_regresar.getString(0);
                                                            String codigo_vendido = fila_regresar.getString(1);
                                                            String cantidad_vendido = fila_regresar.getString(2);
                                                            Log.e("regresar", folio + codigo_vendido + cantidad_vendido);
                                                            AdminSQLiteOpenHelper sumar_stock = new AdminSQLiteOpenHelper(consulta_ventas_totales.this, "administracion", null, 1);
                                                            SQLiteDatabase bd_sumar_stock = sumar_stock.getWritableDatabase();
                                                            Cursor fila_sumar_stock = bd_sumar_stock.rawQuery("select id_producto,codigo_producto,stock_producto from detalles_productos where id_producto " + "='" + codigo_vendido + "'", null);
                                                            Log.e("sumar_fila", String.valueOf(fila_sumar_stock.getCount()));
                                                            if (fila_sumar_stock.moveToFirst()) {
                                                                do {
                                                                    String id = fila_sumar_stock.getString(0);
                                                                    String codigo = fila_sumar_stock.getString(1);
                                                                    String stock = fila_sumar_stock.getString(2);
                                                                    Log.e("stock_add", id + " " + codigo + " " + stock);
                                                                    double stock_actual = Double.parseDouble(stock);
                                                                    double stock_real = stock_actual + Double.parseDouble(cantidad_vendido);
                                                                    Log.i("sock_real", String.valueOf(stock_real));
                                                                    ContentValues value_stock = new ContentValues();
                                                                    value_stock.put("stock_producto", String.valueOf(stock_real));
                                                                    bd_sumar_stock.update("detalles_productos", value_stock, "id_producto=?", new String[]{id});

                                                                }
                                                                while (fila_sumar_stock.moveToNext());
                                                            }
                                                            bd_sumar_stock.close();
                                                        } while (fila_regresar.moveToNext());

                                                    }
                                                    bd_regresar.close();

                                                    try {
                                                        Thread.sleep(200);
                                                        dialog.dismiss();
                                                        Toast.makeText(consulta_ventas_totales.this, "CANCELADO", Toast.LENGTH_LONG).show();
                                                    } catch (InterruptedException e) {

                                                    }
                                                }

                                                else{
                                                    Toast.makeText(consulta_ventas_totales.this,"ya esta cancelado",Toast.LENGTH_LONG).show();
                                                }

                                            }

                                        }



                                    }
                                });
                                botonSubirVenta.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        if(estado==true){
                                            AlertDialog.Builder alertSubirVenta = new AlertDialog.Builder(consulta_ventas_totales.this);
                                            alertSubirVenta.setIcon(R.drawable.impresora);
                                            alertSubirVenta.setTitle("SUBIR VENTA");
                                            alertSubirVenta.setMessage("Deseas Subir la venta Seleccionada?");
                                            alertSubirVenta.setCancelable(false);
                                            alertSubirVenta.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Log.e("WilliamAlvarezH",txtFolio);
                                                    String estadobase="No subido";
                                                    AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion",null,1);
                                                    SQLiteDatabase bd = admin.getWritableDatabase();
                                                    Cursor fila = bd.rawQuery("select folio, estado, id_cliente,tipo_operacion ,estado_operacion,cancelado from venta_cliente where folio" +"='" +txtFolio+"'" ,null);
                                                    Log.e("tamanio_bd",String.valueOf(fila.getCount()));
                                                    SharedPreferences sharedPref = getSharedPreferences("lista_clientes_usuario", Context.MODE_PRIVATE);
                                                    SharedPreferences setting = getSharedPreferences("login_preference", MODE_PRIVATE);
                                                    String id_caja = sharedPref.getString("numero_ruta","");
                                                    String id_usuario = setting.getString("username", "");
                                                    final SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd");
                                                    final String fecha_actual = fecha.format(c.getTime());
                                                    if(fila.moveToFirst()){
                                                        do{
                                                            String folio = fila.getString(0);
                                                            String id_cliente = fila.getString(2);
                                                            String tipo_operacion = fila.getString(3);
                                                            String estado_operacion = fila.getString(4);
                                                            String cancelado_op = fila.getString(5);
                                                            progresoSubiendo = new ProgressDialog(consulta_ventas_totales.this);
                                                            progresoSubiendo.setTitle("Espere por favor...");
                                                            progresoSubiendo.setMessage("Subiendo venta actual");
                                                            progresoSubiendo.setCancelable(false);
                                                            progresoSubiendo.show();
                                                            enviardatos enviar = new enviardatos();
                                                            enviar.execute(map_cliente_id_subir.get(txtnombreCilente.getText().toString()),tipo_operacion,estado_operacion,id_caja,id_usuario,fecha_actual,fecha_actual,
                                                                   cancelado_op,"SIN DETALLES");

                                                        }while (fila.moveToNext());
                                                    }
                                                }
                                            });
                                            alertSubirVenta.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            alertSubirVenta.show();
                                        }
                                        else {
                                            Log.e("estado_subida","No tienes conexion a internet");
                                            AlertDialog.Builder alert_sinConexion = new AlertDialog.Builder(consulta_ventas_totales.this);
                                            alert_sinConexion.setIcon(R.drawable.ic_alerta);
                                            alert_sinConexion.setTitle("SIN CONEXION");
                                            alert_sinConexion.setMessage("No tienes conexion a internet y no se puede subir al servidor, conectate a internet por favor.");
                                            alert_sinConexion.setCancelable(false);
                                            alert_sinConexion.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {


                                                }
                                            });
                                            AlertDialog dialog_sinConexion = alert_sinConexion.create();
                                            dialog_sinConexion.show();
                                            dialog_sinConexion.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
                                            dialog_sinConexion.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.GRAY);
                                            dialog_sinConexion.getButton(AlertDialog.BUTTON_POSITIVE).setGravity(Gravity.CENTER_HORIZONTAL);
                                            dialog_sinConexion.getButton(AlertDialog.BUTTON_POSITIVE).setPadding(10,0,10,0);
                                        }
                                    }
                                });
                                botonImprimir.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        AlertDialog.Builder alert_Imprimir_subido = new AlertDialog.Builder(consulta_ventas_totales.this);
                                        alert_Imprimir_subido.setTitle("IMPRIMIR");
                                        alert_Imprimir_subido.setIcon(R.drawable.impresora);
                                        alert_Imprimir_subido.setCancelable(true);
                                        alert_Imprimir_subido.setMessage("Desea reimprimir esta venta?");
                                        final String nombre_cliente_vendido = map_cliente_id.get(id_cliente);
                                        alert_Imprimir_subido.setPositiveButton("IMPRIMIR", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                AdminSQLiteOpenHelper admin_reimprimir = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion1",null,1);
                                                SQLiteDatabase bd_reimprimir = admin_reimprimir.getWritableDatabase();
                                                String folio_venta = txtFolio;
                                                final Cursor fila_reimprimir = bd_reimprimir.rawQuery("select codigo_producto,cantidad_vendido,precio_venta from venta_detalles where folio" +"='" +folio_venta+"'" ,null);
                                                final Thread t = new Thread(){
                                                    @Override
                                                    public void run(){
                                                        try{
                                                            double total_venta = 0.00;
                                                            IntentPrint("\n    COMERCIALIZADORA FAILI"+
                                                                    "\n          S.A.de C.V "+
                                                                    "\nCalzada Jorge Gomez # 203 Col"+
                                                                    "\nCerro Hueco, Tuxtla Gutierrez"+
                                                                    "\n       Chiapas, Mexico "+
                                                                    "\nRFC:CFA1607131N1     "+ fecha_actual+
                                                                    "\nHORA:" + hora_actual+ "          RUTA:"+numero_ruta_vendedor+
                                                                    "\nCLIENTE:" + nombre_cliente_vendido +
                                                                    "\nFOLIO:   " + txtFolio +
                                                                    "\n contado   "+estado_de_operacion +
                                                                    "\n--------------------------------"+
                                                                    "\nDESCRIPCION"+
                                                                    "\nCANTIDAD       PRECIO      TOTAL"+
                                                                    "\n--------------------------------\n");
                                                            Thread.sleep(900);
                                                            if(fila_reimprimir.moveToFirst()){
                                                                do{
                                                                    String codigo = fila_reimprimir.getString(0);
                                                                    String cantidad = fila_reimprimir.getString(1);
                                                                    String subtotal = fila_reimprimir.getString(2);
                                                                    double subtotal_producto = Double.parseDouble(cantidad) * Double.parseDouble(subtotal);
                                                                    total_venta += subtotal_producto;
                                                                    AdminSQLiteOpenHelper admin_nombre_producto = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion",null,1);
                                                                    SQLiteDatabase bd_nombre_producto = admin_nombre_producto.getWritableDatabase();
                                                                    Cursor fila_nombre_producto = bd_nombre_producto.rawQuery("select nombre_producto from detalles_productos where id_producto" +"='" +codigo+"' limit 1" ,null);
                                                                    String nombre_producto="";
                                                                    if(fila_nombre_producto.moveToFirst()){
                                                                        nombre_producto = fila_nombre_producto.getString(0);
                                                                    }
                                                                    IntentPrint("\n"+nombre_producto +
                                                                            "\n"+cantidad+"       "+"$"+subtotal+ "       "+"$"+ String.valueOf(subtotal_producto));
                                                                    Thread.sleep(150);
                                                                }while (fila_reimprimir.moveToNext());
                                                            }
                                                            Thread.sleep(150);
                                                            IntentPrint("\n--------------------------------"+
                                                                    "\n"+"          Total :      $" + total_venta+
                                                                    "\n"+
                                                                    "\n--------------------------------"+
                                                                    "\n    Gracias por su compra de credito aqui!!"+
                                                                    "\nEl importe de esta nota sera"+
                                                                    "\naplicada a la factura del dia."+// VALOR 2 PARA REIMPRESIÓN A CREDITO----CORRECION  DE VALOR DE OPERACION  /*datos evaluado para la generacion de tickes// */
                                                                    "\n"+
                                                                    "\n"+
                                                                    "\n"+
                                                                    "\n");
                                                            try {
                                                                Thread.sleep(200);
                                                                Toast.makeText(consulta_ventas_totales.this,"IMPRESION TERMINADA",Toast.LENGTH_LONG).show();
                                                                Log.e("jos234","impresion terminada");
                                                            }catch (InterruptedException e){

                                                            }

                                                        }catch (Exception e){

                                                        }
                                                    }
                                                };
                                                t.start();

                                            }
                                        });
                                        alert_Imprimir_subido.show();

                                    }
                                });

                            }
                            else {
                                Toast.makeText(consulta_ventas_totales.this,"Error al consultar ventas",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                bd.close();
            }
        });
        listView_ventas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //String posicion = String.valueOf(parent.getItemIdAtPosition(position));
                final TextView textFolio_venta = (TextView)view.findViewById(R.id.txtFolio_vendido);
                final CharSequence[] items = {"Detalles Venta","Imprimir"};
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(consulta_ventas_totales.this);
                alertDialog.setCancelable(true);
                alertDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                //Toast.makeText(consulta_ventas_totales.this,textFolio.getText().toString(),Toast.LENGTH_LONG).show();
                                AdminSQLiteOpenHelper admin_venta = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion",null,1);
                                SQLiteDatabase bd_venta = admin_venta.getWritableDatabase();
                                Cursor fila_venta = bd_venta.rawQuery("select * from venta_cliente where folio" +"='" +textFolio_venta.getText().toString()+"' limit 1" ,null);
                                if(fila_venta.moveToFirst()){
                                    AdminSQLiteOpenHelper admin_detalles = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion1",null,1);
                                    SQLiteDatabase bd_detalles = admin_detalles.getWritableDatabase();
                                    Cursor fila_detalles = bd_detalles.rawQuery("select * from venta_detalles where folio"+"='"+textFolio_venta.getText().toString()+"'",null);
                                    Toast.makeText(consulta_ventas_totales.this,String.valueOf(fila_detalles.getCount()),Toast.LENGTH_LONG).show();
                                    if(fila_detalles.moveToFirst()){
                                        do {
                                            //nombre_producto = fila_nombre_producto.getString(0);
                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(consulta_ventas_totales.this);
                                            alertDialog.setTitle("Detalles");
                                            alertDialog.setMessage("Folio:   " + fila_venta.getString(0) + "\n" +
                                                    "Total:   " + fila_venta.getString(1) + "\n" +
                                                    "Tipo Operacion:   " + fila_venta.getString(3) + "\n" +
                                                    "______________________" + "\n" +
                                                    "Codigo:"+fila_detalles.getString(1)+"\n"+
                                                    "Cantidad:"+fila_detalles.getString(2)+"\n"+
                                                    "Precio:"+fila_detalles.getString(6)+"\n");

                                            alertDialog.show();
                                        }
                                        while(fila_detalles.moveToNext());

                                    }

                                }

                                break;
                            case 1:
                                Toast.makeText(consulta_ventas_totales.this,"imprimir",Toast.LENGTH_LONG).show();
                                break;
                        }

                    }
                });
                alertDialog.create().show();
                return false;
            }
        });
    }
    //MENU EN ACTIONBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.opciones_consulta_ventas_totales,menu);
        return true;
    }
    //CLICK EN LOS MENUS DE ACTIONBAR
    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.subirTodo){
            //Toast.makeText(consulta_ventas_totales.this,"NO ES POSIBLE SUBIR",Toast.LENGTH_LONG).show();
            //return true;
            try {
                Intent intent = new Intent(consulta_ventas_totales.this, ProgressIntentService.class);
                startService(intent);
                Thread.sleep(2000);

            }catch (InterruptedException e){

            }


           // Intent intent = new Intent(consulta_ventas_totales.this, MainActivity.class);
            //startActivity(intent);
            //super.finish();
            return true;
            }
        return super.onOptionsItemSelected(item);
    }

    //OBTENER ESTADO DE CONEXION A INTERNET
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


    //ENVIAR DATOS AL SERVIDOR 
    private class enviardatos extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String detalles = args[8];
            String fldCancelado = args[7];
            String fldRegistrarFecha = args[6];
            String fldFechaVentaProducto = args[5];
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
            return json;
        }
        protected void onPostExecute(JSONObject result){

            try{
                if(result != null){
                    folio_nuevo = result.getString("message");
                    Log.d("ventaproducto", folio_nuevo);
                    if(folio_nuevo.equals("error")){
                        progresoSubiendo.dismiss();
                        Toast.makeText(consulta_ventas_totales.this,"Datos no cambiados",Toast.LENGTH_SHORT).show();
                    }else {
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion",null,1);
                        AdminSQLiteOpenHelper admin_detalles = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion1",null,1);
                        SQLiteDatabase bd = admin.getWritableDatabase();
                        SQLiteDatabase bd_detalles = admin_detalles.getWritableDatabase();
                        ContentValues actualizar = new ContentValues();
                        ContentValues actualizar_detalles = new ContentValues();
                        actualizar.put("estado","Subido");
                        actualizar.put("folio",folio_nuevo);
                        actualizar_detalles.put("folio",folio_nuevo);
                        bd.update("venta_cliente",actualizar,"folio=?",new String[]{txtFolio});
                        bd_detalles.update("venta_detalles",actualizar_detalles,"folio=?",new String[]{txtFolio});
                        bd.close();
                        bd_detalles.close();
                        Toast.makeText(consulta_ventas_totales.this,"Guardado correctamente", Toast.LENGTH_SHORT).show();
                        try{
                            Thread.sleep(5000);
                            getResult();
                        }catch (InterruptedException e){

                        }

                    }
                }else {
                    Toast.makeText(consulta_ventas_totales.this,"No conectado con el servidor", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    //ENVIAR DATOS MASIVOS AL SERVIDOR
    private class enviardatos_masivo extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String detalles = args[8];
            String fldCancelado = args[7];
            String fldFechaVentaProducto = args[6];
            String id_usuario = args[5];
            String id_caja = args[4];
            String id_estadoOperacion = args[3];
            String id_tipoOperacion = args[2];
            String id_cliente = args[1];
            String id_ventaProducto = args[0];
            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("id_ventaProducto",id_ventaProducto));
            params.add(new BasicNameValuePair("id_cliente",id_cliente));
            params.add(new BasicNameValuePair("id_tipoOperacion",id_tipoOperacion));
            params.add(new BasicNameValuePair("id_estadoOperacion",id_estadoOperacion));
            params.add(new BasicNameValuePair("id_caja",id_caja));
            params.add(new BasicNameValuePair("id_usuario",id_usuario));
            params.add(new BasicNameValuePair("fldFechaVentaProducto",fldFechaVentaProducto));
            params.add(new BasicNameValuePair("fldRegistrarFecha",fldFechaVentaProducto));
            params.add(new BasicNameValuePair("fldCancelado",fldCancelado));
            params.add(new BasicNameValuePair("detalles",detalles));
            JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
            return json;
        }
        protected void onPostExecute(JSONObject result){
            try{
                if(result != null){
                    String mensaje = result.getString("message");
                    if(mensaje.equals("exito")){
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion",null,1);
                        AdminSQLiteOpenHelper admin_detalles = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion1",null,1);
                        SQLiteDatabase bd = admin.getWritableDatabase();
                        SQLiteDatabase bd_detalles = admin_detalles.getWritableDatabase();
                        ContentValues actualizar = new ContentValues();
                        ContentValues actualizar_detalles = new ContentValues();
                        actualizar.put("estado","Subido");
                       // actualizar.put("folio",folio_nuevo_r);
                        //actualizar_detalles.put("folio",folio_nuevo_r);

                        bd.update("venta_cliente",actualizar,"folio=?",new String[]{txtFolio});
                        bd_detalles.update("venta_detalles",actualizar_detalles,"folio=?",new String[]{txtFolio});
                        bd.close();
                        bd_detalles.close();

                        Toast.makeText(consulta_ventas_totales.this,"Guardado correctamente", Toast.LENGTH_SHORT).show();
                        try{
                            Thread.sleep(5000);
                            //getResult();
                            progressoDialogMasivo.dismiss();
                        }catch (InterruptedException e){

                        }
                    }else {
                        Toast.makeText(consulta_ventas_totales.this,"Datos no cambiados",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(consulta_ventas_totales.this,"No conectado con el servdor", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    //OBTENER JSON DE LOS DETALLES
    private JSONArray getResult(){
        //eturn  null;
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(consulta_ventas_totales.this,"administracion1",null,1);
        SQLiteDatabase bd = admin.getWritableDatabase();
        Cursor fila = bd.rawQuery("select  folio, codigo_producto,cantidad_vendido ,peso_producto, precio_compra, precio_real,precio_venta from venta_detalles where folio "+"='" +folio_nuevo+"'" ,null);
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
        Log.d("ventaproducto", output);
        enviardatos_detalles enviar_dato = new enviardatos_detalles();
        String id_enterprise = "1";
        enviar_dato.execute(output,id_enterprise,"");
        return resultSet;
    }

    //ENVIAR DETALLES AL SERVIDOR
    private class enviardatos_detalles extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String id_enterprise = args[1];
            String json_array = args[0];

            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("json_array",json_array));
            params.add(new BasicNameValuePair("id_enterprise",id_enterprise));

            JSONObject json = jsonParser.makeHttpRequest(URL_json, "POST", params);
            return json;
        }
        protected void onPostExecute(JSONObject result){
            try{
                if(result != null){
                    String mensaje = result.getString("message");
                    Log.d("ventaproducto", mensaje);
                    if(mensaje.equals("error")){
                        Toast.makeText(consulta_ventas_totales.this,"Datos no guardados en el servidor",Toast.LENGTH_SHORT).show();

                    }else {
                        Toast.makeText(consulta_ventas_totales.this,"Datos guardados", Toast.LENGTH_SHORT).show();
                        progresoSubiendo.dismiss();

                    }
                }else {
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    public void IntentPrint(String txtvalue)
    {
        byte[] buffer = txtvalue.getBytes();
        byte[] PrintHeader = { (byte) 0xAA, 0x55,2,0 };
        PrintHeader[3]=(byte) buffer.length;
        InitPrinter();
        if(PrintHeader.length>128)
        {
            value+="\nValue is more than 128 size\n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        }
        else
        {
            try
            {
                outputStream.write(txtvalue.getBytes());
                outputStream.close();
                socket.close();
            }
            catch(Exception ex)
            {
                value+=ex.toString()+ "\n" +"Excep IntentPrint \n";
                Toast.makeText(this, value, Toast.LENGTH_LONG).show();
            }
        }
    }
    public void InitPrinter()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try
        {
            if(!bluetoothAdapter.isEnabled())
            {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0)
            {
                for(BluetoothDevice device : pairedDevices)
                {
                    if(device.getAddress().equals(mac_bluetooth)) //Note, you will need to change this to match the name of your device
                    {
                        bluetoothDevice = device;
                        break;
                    }
                }

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                socket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                beginListenForData();
            }
            else
            {
                value+="No Devices found";
                Toast.makeText(this, value, Toast.LENGTH_LONG).show();
                return;
            }
        }
        catch(Exception ex)
        {
            value+=ex.toString()+ "\n" +" InitPrinter \n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        }
    }
    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );
                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Log.d("e", data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }
                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

