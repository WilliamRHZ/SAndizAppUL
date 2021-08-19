package com.example.distrisandi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
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
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import cn.pedant.SweetAlert.SweetAlertDialog;
import android.graphics.Color;


//implementacion de Runnable
public class venta_productos extends AppCompatActivity implements Runnable{

    //variables para Controles
    private ListView listView_nombreProducto;
    private ListView listView_subtotal;
    private ListView listView_cantidad;
    private TextView textFecha;
    private TextView textRuta;
    private TextView textTotal;
    private TextView textFolio;
    private TextView textViewListaClientes;
    private TextView editProducto;
    private EditText editCantidad;
    private Button btnAgregarProducto;
    private RadioButton radio_Contado;
    private RadioButton radio_Credito;
    private EditText editFiltro;
    private EditText editFiltroNombreProducto;

    //variables
    private String posicion;
    private String strDate;
    private String strDate_Folio;
    private String hora;
    private  String folio;
    private String venta_cliente;
    private String id_usuario;
    private String strFecha;
    private SharedPreferences sharedPref;
    List<String> items;
    List<Double> precio_item;
    List<String> cantidad_item;
    ArrayAdapter ADP;
    ArrayAdapter ADP_Precio;
    ArrayAdapter ADP_cantidad;
    Map<String, String >map_producto_codigo = new HashMap<String, String>();
    Map<String,String> map_producto_precio = new HashMap<String, String>();
    Map<String,String> map_cliente_id = new HashMap<String, String>();
    Map<String,String> map_producto_codigo_barra = new HashMap<String, String>();
    private Map<String,String> map_producto_precioVenta = new HashMap<String, String>();
    private Map<String,String> map_peso_producto = new HashMap<String, String>();
    private double totalpagar;
    private double valor_stock_1;
    private  double valor_precio;
    private double importe;
    private  double cambio_imprimir;
    String URL = "https://www.sandiz.com.mx/failisa/WebService/productos_vendidos.php";
    String URL_json = "https://www.sandiz.com.mx/failisa/WebService/productos_vendidos_detalles.php";
  /*  String URL = "http://10.0.2.2/sandiz/WebService/productos_vendidos.php";
    String URL_json = "http://10.0.2.2/sandiz/WebService/productos_vendidos_detalles.php";*/
   /* String URL = "https://localhost/failisa/WebService/productos_vendidos.php";
    String URL_json = "https://localhost/failisa/WebService/productos_vendidos_detalles.php";*/
    JSONParser jsonParser = new JSONParser();
    private boolean estado= false;
    private ProgressDialog dialogoFolio;
    private ProgressDialog dialogoContado;


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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //OBTENER ID DE CLIENTES
        SharedPreferences share_listaClientes = getSharedPreferences("lista_clientes_usuario", MODE_PRIVATE);
        String objetos02 = share_listaClientes.getString("lista_clientes_id","");
        String objetos02_1 = objetos02.replaceAll("[^\\dA-Za-z, :]","");
        String[] pairs1 = objetos02_1.split(",");
        for(int i = 0;i<pairs1.length;i++){
            try{
                String pair = pairs1[i];
                String[]keyvalue = pair.split(":");
                map_cliente_id.put(keyvalue[0], String.valueOf(keyvalue[1]));
            }catch (Exception e){
                Log.e("Error indice: "+ i, e.getMessage());
            }
        }
        ///////////////////////////////
        setContentView(R.layout.activity_venta_productos);
        SharedPreferences sharedPreferences1 = getSharedPreferences("bluetooth_info", MODE_PRIVATE);
        mac_bluetooth= sharedPreferences1.getString("mac_bluetooth","");
        SharedPreferences setting = getSharedPreferences("login_preference", MODE_PRIVATE);
        id_usuario = setting.getString("username", "");

        final Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        strFecha = sdf.format(c.getTime());
        Log.e("fechasssssssss",strFecha);


        textFecha = (TextView)findViewById(R.id.txtFecha);
        textRuta = (TextView) findViewById(R.id.txtRuta);
        textTotal = (TextView)findViewById(R.id.txtTotal);
        textViewListaClientes = (TextView)findViewById(R.id.textViewCliente);
        editProducto = (TextView)findViewById(R.id.editNombreProducto);
        editCantidad = (EditText)findViewById(R.id.editCantidad);
        btnAgregarProducto = (Button)findViewById(R.id.btnAgregarProducto);
        listView_cantidad = (ListView)findViewById(R.id.listaProductos_cantidad);
        listView_nombreProducto = (ListView)findViewById(R.id.listaProductos);
        listView_subtotal = (ListView)findViewById(R.id.listaProductos_subtotal);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigationView_venta);

        insertarFecha();
        insertarRuta();
        cargarDatosMap();
        //OBTENER CLICK EN ICONOS DEL BOOTOOMVIEW
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.navigation_buscar){
                    if((textViewListaClientes.getText().toString()).equals("")){
                        Toast.makeText(venta_productos.this,"Primero agrega un cliente",Toast.LENGTH_SHORT).show();
                    }else{
                        DialogoListaProductorVenta(venta_productos.this);
                    }

                }
                if (id == R.id.navigation_limpiar){
                    //IntentPrint("\n     COMERCIALIZADORA FAILI ");
                    if  (items.size()<=0){/*
                        AlertDialog.Builder builder = new AlertDialog.Builder(venta_productos.this);
                        builder.setTitle("LIMPIAR VENTA");
                        builder.setMessage("La venta actuial esta vacio");
                        builder.setIcon(R.mipmap.ic_not);
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        */
                        new SweetAlertDialog(venta_productos.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Limpiar Venta")
                                .setContentText("La venta actual esta vacia")
                                .setConfirmText("OK!")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .show();

                    }
                    else {/*
                        AlertDialog.Builder builder = new AlertDialog.Builder(venta_productos.this);
                        builder.setTitle("LIMPIAR VENTA");
                        builder.setMessage("Desear limpiar la venta actuals?");
                        builder.setIcon(R.mipmap.ic_not);
                        builder.setCancelable(false);
                        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                items.clear();
                                precio_item.clear();
                                cantidad_item.clear();
                                ADP.notifyDataSetChanged();
                                ADP_Precio.notifyDataSetChanged();
                                ADP_cantidad.notifyDataSetChanged();
                                textTotal.setText("0");

                            }
                        });
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();*/
                        new SweetAlertDialog(venta_productos.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("LIMPIAR VENTA")
                                .setContentText("Desea limpiar la venta actual?...")
                                .setConfirmText("Si")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        items.clear();
                                        precio_item.clear();
                                        cantidad_item.clear();
                                        ADP.notifyDataSetChanged();
                                        ADP_Precio.notifyDataSetChanged();
                                        ADP_cantidad.notifyDataSetChanged();
                                        textTotal.setText("0");
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .show();

                    }
                }
                if (id == R.id.navigation_codigo_barra){

                    if((textViewListaClientes.getText().toString()).equals("")){
                        Toast.makeText(venta_productos.this,"Primero agrega un cliente",Toast.LENGTH_SHORT).show();

                    }else{
                        SharedPreferences sharedPreferences = getSharedPreferences("productos", MODE_PRIVATE);
                        String objetos = sharedPreferences.getString("lista_key_producto","");
                        String objetos1 = objetos.replaceAll("[^\\dA-Za-z, ./:]","");
                        String[] pairs = objetos1.split(",");
                        for(int i = 0;i<pairs.length;i++){
                            String pair = pairs[i];
                            String[]keyvalue = pair.split(":");
                            map_producto_codigo_barra.put(keyvalue[0], String.
                                    valueOf(keyvalue[1]));
                        }
                        final Dialog dialog = new Dialog(venta_productos.this);
                        dialog.setCancelable(true);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setContentView(R.layout.dialog_codigo_buscar);
                        final EditText editCodigo = (EditText)dialog.findViewById(R.id.EditBuscar);
                        LinearLayout Buscar = (LinearLayout)  dialog.findViewById(R.id.btnBuscar);
                        Buscar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String codigo = editCodigo.getText().toString();
                                String producto = map_producto_codigo_barra.get(codigo);
                                if(producto==null){
                                    Toast.makeText(venta_productos.this, "No existe un producto con este código",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    editProducto.setText(producto);
                                    dialog.dismiss();
                                }

                            }

                        });
                        dialog.show();
                    }
                }
                if (id == R.id.navigation_Pagar){
                    int existencia_venta = items.size();
                    if(existencia_venta == 0){
                        Toast.makeText(venta_productos.this,"Agrega productos a la venta", Toast.LENGTH_SHORT).show();
                    }
                    else {


                        AlertDialog.Builder alertPagar  = new AlertDialog.Builder(venta_productos.this);
                        alertPagar.setTitle("PAGO");
                        alertPagar.setMessage(" Elige la forma de pago de esta venta");
                        alertPagar.setPositiveButton("CONTADO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                               Dialogopagar(venta_productos.this);
                               venta_cliente = "";

                            }
                        });
                        alertPagar.setNegativeButton("CREDITO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                 DialogopagarCredito(venta_productos.this);
                                 venta_cliente = "";
                            }
                        });
                        alertPagar.show();



                    }
                }

                //BOTON CONSULTA TOTAL
                if (id == R.id.navigation_Consultar){
                    //Toast.makeText(venta_productos.this,"Consultando...", Toast.LENGTH_SHORT).show();0..

                    Intent intent = new Intent(venta_productos.this, consulta_ventas_totales.class);
                    startActivityForResult(intent,0);
                }
                return true;
            }
        });
        //BOTON PARA AGREGAR PRODUCTO EN LA VENTA
        btnAgregarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final  String estado_edit_producto = editProducto.getText().toString();
                final String edit_cantidad = editCantidad.getText().toString();


                AdminSQLiteOpenHelper adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
                SQLiteDatabase sqLiteDatabase = adminSQLiteOpenHelper.getReadableDatabase();
                Cursor fila = sqLiteDatabase.rawQuery("select stock_producto from detalles_productos where nombre_producto "+"='" +estado_edit_producto+"' limit 1",null);
                double valor_stock=0;
                if(fila!=null){
                    if(fila.moveToFirst()){
                        String valor_stock_string = fila.getString(0);
                        valor_stock = Double.parseDouble(valor_stock_string);
                        //valor_stock=(int)valor_stock_bol;
                    }
                }

                if(estado_edit_producto.equals("")){
                    Toast.makeText(venta_productos.this,"Agrega un producto",Toast.LENGTH_SHORT).show();
                }
                else if(edit_cantidad.equals("")) {
                    Toast.makeText(venta_productos.this,"Ingresa una cantidad",Toast.LENGTH_SHORT).show();
                }
                else if(valor_stock==0){
                    Toast.makeText(venta_productos.this,"Stock: "+valor_stock,Toast.LENGTH_SHORT).show();
                }
                else if(Double.parseDouble(edit_cantidad)>valor_stock){
                    Toast.makeText(venta_productos.this,"No tienes suficiente producto\n"+"Stock: "+valor_stock,Toast.LENGTH_SHORT).show();
                }
                else if(estado_edit_producto.equals("") && edit_cantidad.equals("")){
                    Toast.makeText(venta_productos.this,"Agrega un producto y la cantidad",Toast.LENGTH_SHORT).show();
                }

                else{
                    if(items.contains(estado_edit_producto)){
                        AlertDialog.Builder dialogo_existencia = new AlertDialog.Builder(venta_productos.this);
                        dialogo_existencia.setTitle("Ya existe en la venta");
                        dialogo_existencia.setMessage("Este producto ya esta agregado \n " +
                                "       en la venta actual");
                        dialogo_existencia.setIcon(R.drawable.ic_alerta);
                        dialogo_existencia.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                editProducto.setText("");
                                editCantidad.setText("");
                            }
                        });
                        AlertDialog dialog = dialogo_existencia.create();
                        dialog.show();
                    }else {/*
                        AlertDialog.Builder dialogo_existencia = new AlertDialog.Builder(venta_productos.this);
                        dialogo_existencia.setTitle("Stock");
                        dialogo_existencia.setMessage(Html.fromHtml("<center><h3>"+"Existencia "+":   "+"<b>"+valor_stock+"</b>"+"</h3></center>"));
                        dialogo_existencia.setIcon(R.drawable.ic_pagar);
                        dialogo_existencia.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int k) {
                                items.add(editProducto.getText().toString());
                                cantidad_item.add(editCantidad.getText().toString());
                                double uno = Double.parseDouble(editCantidad.getText().toString());
                                double precio = Double.valueOf(map_producto_precio.get(estado_edit_producto));
                                SharedPreferences sharedPreferences = getSharedPreferences("productos", MODE_PRIVATE);
                                String objetos = sharedPreferences.getString("lista_producto_codigo_barra","");
                                String objetos1 = objetos.replaceAll("[^\\dA-Za-z, ./:]","");
                                String[] pairs = objetos1.split(",");
                                for(int i = 0;i<pairs.length;i++){
                                    String pair = pairs[i];
                                    String[]keyvalue = pair.split(":");
                                    map_producto_codigo_barra.put(keyvalue[1], String.valueOf(keyvalue[0]));
                                }
                                double dos = uno*precio;
                                precio_item.add(dos);
                                ADP.notifyDataSetChanged();
                                ADP_Precio.notifyDataSetChanged();
                                ADP_cantidad.notifyDataSetChanged();
                                editProducto.setText("");
                                editCantidad.setText("");
                                double total = Double.parseDouble(textTotal.getText().toString());
                                totalpagar = total+dos;
                                String resultado = String.valueOf(totalpagar);
                                textTotal.setText(resultado);

                            }
                        });
                        AlertDialog dialog = dialogo_existencia.create();
                        dialog.show();*/
                        new SweetAlertDialog(venta_productos.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Stock")
                                .setContentText(String.valueOf(Html.fromHtml("<center><h2>"+"Existencia "+":   "+"<b>"+valor_stock+"</b>"+"</h2></center>")))
                                .setConfirmText("Ok!")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        items.add(editProducto.getText().toString());
                                        cantidad_item.add(editCantidad.getText().toString());
                                        double uno = Double.parseDouble(editCantidad.getText().toString());
                                        double precio = Double.valueOf(map_producto_precio.get(estado_edit_producto));
                                        SharedPreferences sharedPreferences = getSharedPreferences("productos", MODE_PRIVATE);
                                        String objetos = sharedPreferences.getString("lista_producto_codigo_barra","");
                                        String objetos1 = objetos.replaceAll("[^\\dA-Za-z, ./:]","");
                                        String[] pairs = objetos1.split(",");
                                        for(int i = 0;i<pairs.length;i++){
                                            String pair = pairs[i];
                                            String[]keyvalue = pair.split(":");
                                            map_producto_codigo_barra.put(keyvalue[1], String.valueOf(keyvalue[0]));
                                        }
                                        double dos = uno*precio;
                                        precio_item.add(dos);
                                        ADP.notifyDataSetChanged();
                                        ADP_Precio.notifyDataSetChanged();
                                        ADP_cantidad.notifyDataSetChanged();
                                        editProducto.setText("");
                                        editCantidad.setText("");
                                        double total = Double.parseDouble(textTotal.getText().toString());
                                        totalpagar = total+dos;
                                        String resultado = String.valueOf(totalpagar);
                                        textTotal.setText(resultado);


                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .setCancelButton("Cancelar", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .show();
                    }
                }

            }
        });
        items = new ArrayList<>();
        precio_item = new ArrayList<>();
        cantidad_item = new ArrayList<>();
        ADP = new ArrayAdapter(this, R.layout.item_productos_vendidos,R.id.txtlistaProductos,items);
        ADP_Precio = new ArrayAdapter(this, R.layout.item_ventas_totales_total,R.id.txtlistaventas,precio_item);
        ADP_cantidad = new ArrayAdapter(this,R.layout.item_productos_vendidos,R.id.txtlistaProductos,cantidad_item);
        listView_nombreProducto.setAdapter(ADP);
        listView_subtotal.setAdapter(ADP_Precio);
        listView_cantidad.setAdapter(ADP_cantidad);

        //EDITAR CANTIDAD Y PRECIO DE CADA PRODUCTO
        listView_cantidad.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                posicion = String.valueOf(adapterView.getItemIdAtPosition(i));
                EditarProduto(venta_productos.this);


            }
        });


    }

    //METODO PARA BOTONES DEL NAVIGATION VIEW

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opciones_ventas_productos, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.movimientos){/*
            AlertDialog.Builder builder = new AlertDialog.Builder(venta_productos.this);
            builder.setTitle("ATRAS");
            builder.setMessage("Seguro desea Cerrar la Ventana?");
            builder.setIcon(R.drawable.signo_interrogacion);
            builder.setCancelable(false);
            builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //SharedPreferences sharedPreferences_precioVenta = getSharedPreferences("bluetooth_info", MODE_PRIVATE);
                    //String objetos_precioVenta = sharedPreferences_precioVenta.getString("mac_bluetooth","");
                    //sharedPreferences_precioVenta.edit().remove("mac_bluetooth").commit();
                    finish();
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            */
            new SweetAlertDialog(venta_productos.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atras")
                    .setContentText("Seguro desea cerrar la ventana?")
                    .setConfirmText("OK!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            //sDialog.dismissWithAnimation();
                            Intent intent = new Intent(venta_productos.this,Sesion_Usuario.class);
                            startActivity(intent);
                            finish();
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
        if (id == R.id.clientes){
            DialogoListaClientes(venta_productos.this);
        }
        return super.onOptionsItemSelected(item);
    }

    //CUADRO DIALOGO LISTA CLIENTES//
    public void DialogoListaClientes(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_lista_clientes);
        Button btnCancelar = (Button)  dialog.findViewById(R.id.btnCancelarCliente);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        ListView list_clientes = (ListView)dialog.findViewById(R.id.listViewClientes);
        editFiltro = (EditText)dialog.findViewById(R.id.editFiltroNombre);
        SharedPreferences sharedPreferences = getSharedPreferences("lista_clientes_usuario", MODE_PRIVATE);
        String objetos = sharedPreferences.getString("lista_clientes","");
        String uno = objetos.replaceAll("[^\\P{M}, :]","");
        String tres = uno.toString().replace("[", "").replace("]", "");
        String dos = tres.replace("\"","");
        final String [] rueba = dos.split(",");

        List<String>milista = new ArrayList<String>(Arrays.asList(objetos.split(",")));


        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.item_list_clientes,R.id.txtlistaClientes,rueba);
        list_clientes.setAdapter(arrayAdapter);

        //filtro de nombres
        editFiltro.addTextChangedListener(new TextWatcher() {

            @Override            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                arrayAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        arrayAdapter.notifyDataSetChanged();
        list_clientes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                TextView nomnre_cliente = (TextView)view.findViewById(R.id.txtlistaClientes);
                String clienteId = map_cliente_id.get(nomnre_cliente.getText().toString());

                AdminSQLiteOpenHelper adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
                SQLiteDatabase bd = adminSQLiteOpenHelper.getReadableDatabase();
                Cursor contador =bd.rawQuery("SELECT * FROM venta_cliente where id_cliente = ? AND postActualizacionRegistro = ?",new String[]{clienteId, "0"});
                contador.moveToFirst();
                if(contador.getCount() < 2){
                    Log.e("cliente_nombre",nomnre_cliente.getText().toString());
                    textViewListaClientes.setText(nomnre_cliente.getText().toString());
                    sharedPref = getSharedPreferences("nombre_cliente_vendido", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("nombre_cliente",nomnre_cliente.getText().toString());
                    editor.apply();
                }else{
                    Toast.makeText(venta_productos.this, "El cliente supero el limite de ventas", Toast.LENGTH_LONG).show();
                }

                bd.close();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //DIALOG LISTAR PRODUCTO
    public void DialogoListaProductorVenta(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialogo_lista_productos);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ListView listView = (ListView)dialog.findViewById(R.id.listViewProductos);
        Button btnCancelar = (Button) dialog.findViewById(R.id.btnCancelarProductos);
        TextView txtLista = (TextView)dialog.findViewById(R.id.textView);
        editFiltroNombreProducto = (EditText)dialog.findViewById(R.id.editFiltroNombreProducto);
        txtLista.setText("LISTA DE PRODUCTOS");
        SharedPreferences sharedPreferences = getSharedPreferences("productos", MODE_PRIVATE);
        String objetos = sharedPreferences.getString("lista_productos_nombres","");
        String objetos1 = objetos.replaceAll("[^\\dA-Za-z,./ ]","");

        final String [] rueba = objetos1.split(",");
        Arrays.sort(rueba);
        final ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(),R.layout.item_list_productos,R.id.txtlistaProductos,rueba);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView nombre_producto = (TextView)view.findViewById(R.id.txtlistaProductos);
                editProducto.setText(nombre_producto.getText().toString());
                dialog.dismiss();


            }
        });
        editFiltroNombreProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                arrayAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //DIALOGO DE PAGAR CONTADO
    public  void Dialogopagar(final Activity activity){ // muestra dialogo en el dialog activity
        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_pagar);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        //instancias objetos
        LinearLayout botonImprimir = (LinearLayout) dialog.findViewById(R.id.btnImprimir);  //
        final TextView textTotal_1 = (TextView)dialog.findViewById(R.id.txtTotal);
        LinearLayout botonCambio = (LinearLayout) dialog.findViewById(R.id.btnCambio);
        final EditText editImporte = (EditText)dialog.findViewById(R.id.EditImporte);
        final TextView textCambio = (TextView)dialog.findViewById(R.id.txtCambio);
        //click en boton imprimir
        botonImprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //cerrar Alert Dialog y abrir Otro para confirmacion
                if (editImporte.getText().toString().equals("")) {
                    Toast.makeText(venta_productos.this, "Ingrese un importe para poder imprimir", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(textCambio.getText().toString().equals("")){
                        Toast.makeText(venta_productos.this, "El campo cambio, está vacio", Toast.LENGTH_SHORT).show();

                    }else {
                        cambio_imprimir = Double.parseDouble(editImporte.getText().toString())-totalpagar;

                        dialog.dismiss();
                        //IntentPrint("\n     COMERCIALIZADORA FAILI ");
                        if(mac_bluetooth.equals("")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(venta_productos.this);
                            builder.setTitle("No estas conectado a     una impresora");
                            builder.setIcon(R.drawable.ic_alerta);
                            builder.setCancelable(true);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.setNegativeButton("Guardar venta", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    if (!isNetworkAvailable(venta_productos.this)){
                                        //no hay internet
                                        AdminSQLiteOpenHelper consul_folio = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
                                        SQLiteDatabase sql_folio = consul_folio.getReadableDatabase();
                                        Cursor cursor_folio = sql_folio.rawQuery("select folio from venta_cliente where folio like '%FSC%'order by folio desc limit 1",null);
                                        Log.e("mensaje1",String.valueOf(cursor_folio.getCount()));
                                        String folio_sql="";
                                        if(cursor_folio!=null){
                                            if(cursor_folio.moveToFirst()){
                                                Log.e("tipo_operacion",cursor_folio.getString(0));
                                                folio_sql = cursor_folio.getString(0);

                                            }
                                        }
                                        final String folio_inicial="10001";
                                        if(folio_sql.length()<=0){
                                            Log.e("tipo_operacion","no hay folio");//-----------------------------folio de de los tickets fuera de linea -----10001FSC
                                            venta_cliente = folio_inicial+"-"+"FSC";
                                        }
                                        else {
                                            Log.e("tipo_operacion","si hay folio");
                                            //String folio_nuevo = folio_sql.replaceAll("[^\\dd0-9]","");
                                            String folio_nuevo[] = folio_sql.split("-");
                                            int folio_n = Integer.parseInt(folio_nuevo[0]);
                                            int folio_nuevo_int = folio_n+1;
                                            Log.e("folio_nuebvo",String.valueOf(folio_n));
                                            Log.e("folio_nuebvo",String.valueOf(folio_nuevo_int));
                                            venta_cliente = String.valueOf(folio_nuevo_int) + "-FSC";
                                        }

                                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(venta_productos.this, "administracion", null, 1);
                                        SQLiteDatabase bd = admin.getWritableDatabase();
                                        ContentValues registro = new ContentValues();
                                        registro.put("folio", venta_cliente);
                                        registro.put("total", textTotal.getText().toString());
                                        registro.put("id_cliente", map_cliente_id.get(textViewListaClientes.getText()));
                                        registro.put("estado", "No subido");
                                        registro.put("tipo_operacion", "1");
                                        registro.put("estado_operacion", "2");
                                        registro.put("importe","0");
                                        registro.put("cancelado","0");
                                        registro.put("postActualizacionRegistro","0");
                                        bd.insert("venta_cliente", null, registro);
                                        bd.close();
                                        guardardatos();
                                        stock();
                                    }
                                    else {
                                        //si hay internet
                                        enviardatosContado enviar = new enviardatosContado();
                                        enviar.execute(map_cliente_id.get(textViewListaClientes.getText()),"1","2",textRuta.getText().toString(),id_usuario,
                                                strFecha , strFecha,"0", "SIN DETALLES");


                                    }
                                }
                            });
                            builder.show();

                        }else {
                            if (!isNetworkAvailable(venta_productos.this)) {
                                //Toast.makeText(venta_productos.this, "no hay internet", Toast.LENGTH_LONG).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(venta_productos.this);
                                venta_cliente="";
                                builder.setTitle("IMPRIMIR");
                                builder.setIcon(R.drawable.impresora);
                                builder.setMessage("Seguro desea imprimir?");
                                builder.setCancelable(false);
                                builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // = new ProgressDialog(venta_productos.this);
                                       // dialogoContado.setTitle("Espere...");
                                       // dialogoContado.setMessage("Guardando Datos...");
                                       // dialogoContado.show();

                                        AdminSQLiteOpenHelper consul_folio = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
                                        SQLiteDatabase sql_folio = consul_folio.getReadableDatabase();
                                        Cursor cursor_folio = sql_folio.rawQuery("select folio from venta_cliente where folio like '%FSC%'order by folio desc limit 1",null);
                                        Log.e("mensaje1",String.valueOf(cursor_folio.getCount()));
                                        String folio_sql="";
                                        if(cursor_folio!=null){
                                            if(cursor_folio.moveToFirst()){
                                                Log.e("tipo_operacion",cursor_folio.getString(0));
                                                folio_sql = cursor_folio.getString(0);

                                            }
                                        }
                                        final String folio_inicial="10001";
                                        if(folio_sql.length()<=0){
                                            Log.e("tipo_operacion","no hay folio");//-------------------------------------------------------------------------------------------------------------folio
                                            venta_cliente = folio_inicial+"-"+"FSC";
                                        }
                                        else {
                                            Log.e("tipo_operacion","si hay folio");
                                            //String folio_nuevo = folio_sql.replaceAll("[^\\dd0-9]","");
                                            String folio_nuevo[] = folio_sql.split("-");
                                            int folio_n = Integer.parseInt(folio_nuevo[0]);
                                            int folio_nuevo_int = folio_n+1;
                                            Log.e("folio_nuebvo",String.valueOf(folio_n));
                                            Log.e("folio_nuebvo",String.valueOf(folio_nuevo_int));
                                            venta_cliente = String.valueOf(folio_nuevo_int) + "-FSC";
                                        }

                                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(venta_productos.this, "administracion", null, 1);
                                        SQLiteDatabase bd = admin.getWritableDatabase();
                                        ContentValues registro = new ContentValues();
                                        registro.put("folio", venta_cliente);
                                        registro.put("total", textTotal.getText().toString());
                                        registro.put("id_cliente", map_cliente_id.get(textViewListaClientes.getText()));
                                        registro.put("estado", "No subido");
                                        registro.put("tipo_operacion", "1");
                                        registro.put("estado_operacion", "2");
                                        registro.put("importe",importe);
                                        registro.put("cancelado","0");
                                        registro.put("postActualizacionRegistro","0");
                                        bd.insert("venta_cliente", null, registro);
                                        bd.close();
                                        //dialogoContado.setMessage("Imprimiendo");
                                        SharedPreferences setting0 = getSharedPreferences("nombre_cliente_vendido", MODE_PRIVATE);
                                        final String value1 = setting0.getString("nombre_cliente", "");

                                        final  Thread t = new Thread(){
                                            @Override
                                            public void run(){
                                                try{
                                                    for (int m=0;m<2;m++) {
                                                        IntentPrint("\n     COMERCIALIZADORA FAILI.   \n"
                                                                + "          S.A. de C.V     \n " +
                                                                "Calzada Jorge Gomez # 199 Col \n " +
                                                                "Cerro Hueco, Tuxtla Gutierrez \n" +
                                                                "         Chis., Mex.\n" +
                                                                "RFC:CFA1607131N1     " + strDate + "\n" +
                                                                "HORA:" + hora + "          RUTA:" + textRuta.getText().toString() + "\n" +
                                                                "CLIENTE:" + value1 + "\n" +
                                                                "FOLIO:   " + venta_cliente +"\n"+
                                                                "           CONTADO\n" +
                                                                "--------------------------------\n" +
                                                                "DESCRIPCION\n" +
                                                                "CANTIDAD     PRECIO      TOTAL\n" +
                                                                "--------------------------------\n");
                                                        Thread.sleep(2000);
                                                        for (int k = 0; k < items.size(); k++) {
                                                            String cantidad = cantidad_item.get(k);
                                                            String descripcion = items.get(k);
                                                            String precio = map_producto_precio.get(descripcion);
                                                            String total = String.valueOf(precio_item.get(k));
                                                            IntentPrint(descripcion + "\n" + cantidad + "         $" + precio +"         $" + total + "\n");
                                                            Thread.sleep(500);
                                                        }
                                                        Thread.sleep(1000);
                                                        IntentPrint("--------------------------------\n" +
                                                                "   Total:        $" + totalpagar + "\n" +
                                                                "   Efectivo:     $" + importe + "\n" +
                                                                "   Cambio:       $" + cambio_imprimir + "\n" +
                                                                "--------------------------------\n"+
                                                                "    Gracias por su compra!!\n" +
                                                                "   el importe de esta nota\n" +
                                                                "   sera aplicada a la factura \n" +
                                                                "           del dia\n"+
                                                                "\n"+
                                                                "\n"+
                                                                "\n"+
                                                                "\n");
                                                        Thread.sleep(500);

                                                    }

                                                }catch (Exception e){
                                                    Toast.makeText(venta_productos.this, e.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        };
                                        t.start();
                                        guardardatos();
                                        stock();

                                    }

                                });
                                builder.show();

                            } else
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(venta_productos.this);
                            builder.setTitle("IMPRIMIR");
                            builder.setIcon(R.drawable.impresora);
                            builder.setMessage("Seguro desea imprimir?");

                            builder.setCancelable(false);
                            builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogoContado = new ProgressDialog(venta_productos.this);
                                    dialogoContado.setTitle("Espere...");
                                    dialogoContado.setMessage("Enviando Datos...");
                                    dialogoContado.show();
                                    enviardatos enviar = new enviardatos();
                                    enviar.execute(map_cliente_id.get(textViewListaClientes.getText()), "1", "2", textRuta.getText().toString(), id_usuario,
                                            strFecha, strFecha,"0", "SIN DETALLES");

                                }
                            });
                            builder.show();

                        }
                        }

                    }


                }
            }
        });
        botonCambio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String editImporte_String = editImporte.getText().toString();
                if(editImporte_String.equals("")){
                    Toast.makeText(venta_productos.this,"Ingrese un importe", Toast.LENGTH_SHORT).show();
                }
                else {
                    importe = Double.parseDouble(editImporte.getText().toString());
                    double total = Double.parseDouble(textTotal.getText().toString());
                    int comparacion = Double.compare(importe,total);
                    Log.e("comparacionnnnn",String.valueOf(comparacion));
                    if(comparacion<0){
                        Toast.makeText(venta_productos.this,"Ingrese una cantidad correcta", Toast.LENGTH_SHORT).show();
                    }
                    else {
                    double cambio = importe - total;
                    String cambio1 = String.valueOf(cambio);
                    String cambiovalor= "$ "+ cambio1;
                    textCambio.setText(cambiovalor );

                    }
                }
            }
        });
        textTotal_1.setText("$ "+ textTotal.getText().toString());
    }

    //-----------------------------------DIALOGO PAGAR CREDITO-----------------------------------------
    public void DialogopagarCredito(final Activity activity){

        final AlertDialog.Builder alertCredito = new AlertDialog.Builder(this);
        alertCredito.setCancelable(false);
        alertCredito.setIcon(R.drawable.impresora);
        alertCredito.setTitle("CREDITO");
        alertCredito.setMessage("Venta a Cédito");

        //click en boton imprimir
        alertCredito.setPositiveButton("IMPRIMIR A CREDITO",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface,int i) {
                AlertDialog.Builder builder = new AlertDialog.Builder(venta_productos.this);
                builder.setTitle("IMPRIMIR");
                builder.setIcon(R.drawable.impresora);
                builder.setMessage("Seguro desea imprimir?");
                builder.setCancelable(false);
                builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        {
                            if(mac_bluetooth.equals("")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(venta_productos.this);
                                builder.setTitle("No estas conectado a una impresora");
                                builder.setIcon(R.drawable.ic_alerta);
                                builder.setCancelable(true);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.setNegativeButton("Guardar venta", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if (!isNetworkAvailable(venta_productos.this)){
                                            //no hay internet
                                            AdminSQLiteOpenHelper consul_folio = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
                                            SQLiteDatabase sql_folio = consul_folio.getReadableDatabase();
                                            Cursor cursor_folio = sql_folio.rawQuery("select folio from venta_cliente where folio like '%FSC%'order by folio desc limit 1",null);
                                            Log.e("mensaje1",String.valueOf(cursor_folio.getCount()));
                                            String folio_sql="";
                                            if(cursor_folio!=null){

                                                if(cursor_folio.moveToFirst()){
                                                    Log.e("tipo_operacion",cursor_folio.getString(0));
                                                    folio_sql = cursor_folio.getString(0);

                                                }
                                            }
                                            final String folio_inicial="10001";
                                            if(folio_sql.length()<=0){
                                                Log.e("tipo_operacion","no hay folio");
                                                venta_cliente = folio_inicial+"-"+"FSC";
                                            }
                                            else {
                                                Log.e("tipo_operacion","si hay folio");
                                                //String folio_nuevo = folio_sql.replaceAll("[^\\dd0-9]","");
                                                String folio_nuevo[] = folio_sql.split("-");
                                                int folio_n = Integer.parseInt(folio_nuevo[0]);
                                                int folio_nuevo_int = folio_n+1;
                                                Log.e("folio_nuebvo",String.valueOf(folio_n));
                                                Log.e("folio_nuebvo",String.valueOf(folio_nuevo_int));
                                                venta_cliente = String.valueOf(folio_nuevo_int) + "-FSC";
                                            }

                                            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(venta_productos.this, "administracion", null, 1);
                                            SQLiteDatabase bd = admin.getWritableDatabase();
                                            ContentValues registro = new ContentValues();
                                            registro.put("folio", venta_cliente);
                                            registro.put("total", textTotal.getText().toString());
                                            registro.put("id_cliente", map_cliente_id.get(textViewListaClientes.getText()));
                                            registro.put("estado", "No subido");
                                            registro.put("tipo_operacion", "2");
                                            registro.put("estado_operacion", "0");
                                            registro.put("importe","0");
                                            registro.put("cancelado","0");
                                            registro.put("postActualizacionRegistro","0");
                                            bd.insert("venta_cliente", null, registro);
                                            bd.close();
                                            guardardatos();
                                            stock();
                                        }
                                        else {
                                            //si hay internet
                                            enviardatosCredito enviar = new enviardatosCredito();
                                            enviar.execute(map_cliente_id.get(textViewListaClientes.getText()),"2","0",textRuta.getText().toString(),id_usuario,
                                                    strFecha, strFecha,"0","SIN DETALLES");


                                        }
                                    }
                                });
                                builder.show();

                            }else {
                                if (!isNetworkAvailable(venta_productos.this)) {
                                    //Toast.makeText(venta_productos.this, "no hay internet", Toast.LENGTH_LONG).show();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(venta_productos.this);
                                    builder.setTitle("IMPRIMIR");
                                    builder.setIcon(R.drawable.impresora);
                                    builder.setMessage("Seguro desea imprimir?");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogoContado = new ProgressDialog(venta_productos.this);
                                            dialogoContado.setTitle("Espere...");
                                            dialogoContado.setMessage("Guardando Datos...");
                                            dialogoContado.show();

                                            AdminSQLiteOpenHelper consul_folio = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
                                            SQLiteDatabase sql_folio = consul_folio.getReadableDatabase();
                                            Cursor cursor_folio = sql_folio.rawQuery("select folio from venta_cliente where folio like '%FSC%'order by folio desc limit 1",null);
                                            Log.e("mensaje1",String.valueOf(cursor_folio.getCount()));
                                            String folio_sql="";
                                            if(cursor_folio!=null){
                                                if(cursor_folio.moveToFirst()){
                                                    Log.e("tipo_operacion",cursor_folio.getString(0));
                                                    folio_sql = cursor_folio.getString(0);

                                                }
                                            }
                                            final String folio_inicial="10001";
                                            if(folio_sql.length()<=0){
                                                Log.e("tipo_operacion","no hay folio");
                                                venta_cliente = folio_inicial+"-"+"FSC";
                                            }
                                            else {
                                                Log.e("tipo_operacion","si hay folio");
                                                //String folio_nuevo = folio_sql.replaceAll("[^\\dd0-9]","");
                                                String folio_nuevo[] = folio_sql.split("-");
                                                int folio_n = Integer.parseInt(folio_nuevo[0]);
                                                int folio_nuevo_int = folio_n+1;
                                                Log.e("folio_nuebvo",String.valueOf(folio_n));
                                                Log.e("folio_nuebvo",String.valueOf(folio_nuevo_int));
                                                venta_cliente = String.valueOf(folio_nuevo_int) + "-FSC";
                                            }

                                            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(venta_productos.this, "administracion", null, 1);
                                            SQLiteDatabase bd = admin.getWritableDatabase();
                                            ContentValues registro = new ContentValues();
                                            registro.put("folio", venta_cliente);
                                            registro.put("total", textTotal.getText().toString());
                                            registro.put("id_cliente", map_cliente_id.get(textViewListaClientes.getText()));
                                            registro.put("estado", "No subido");
                                            registro.put("tipo_operacion", "2");
                                            registro.put("estado_operacion", "0");
                                            registro.put("importe","0");
                                            registro.put("cancelado","0");
                                            registro.put("postActualizacionRegistro","0");
                                            bd.insert("venta_cliente", null, registro);
                                            bd.close();
                                            dialogoContado.setMessage("Imprimiendo");
                                            SharedPreferences setting0 = getSharedPreferences("nombre_cliente_vendido", MODE_PRIVATE);
                                            final String value1 = setting0.getString("nombre_cliente", "");

                                            final Thread t = new Thread(){
                                                @Override
                                                public void run(){
                                                    try{
                                                        for (int m=0;m<2;m++) {
                                                            IntentPrint("\n     COMERCIALIZADORA FAILI.   \n"
                                                                    + "          S.A. de C.V     \n " +
                                                                    "Calzada Jorge Gomez # 199 Col \n " +
                                                                    "Cerro Hueco, Tuxtla Gutierrez \n" +
                                                                    "         Chis., Mex.\n" +
                                                                    "RFC:CFA1607131N1     " + strDate + "\n" +
                                                                    "HORA:" + hora + "          RUTA:" + textRuta.getText().toString() + "\n" +
                                                                    "CLIENTE:" + value1 + "\n" +
                                                                    "FOLIO:   " + venta_cliente +"\n"+
                                                                    "           CREDITO\n" +
                                                                    "--------------------------------\n" +
                                                                    "DESCRIPCION\n" +
                                                                    "CANTIDAD     PRECIO      TOTAL\n" +
                                                                    "--------------------------------\n");
                                                            Thread.sleep(2000);
                                                            for (int k = 0; k < items.size(); k++) {
                                                                String cantidad = cantidad_item.get(k);
                                                                String descripcion = items.get(k);
                                                                String precio = map_producto_precio.get(descripcion);
                                                                String total = String.valueOf(precio_item.get(k));
                                                                IntentPrint(descripcion + "\n" + cantidad + "         $" + precio +"         $" + total + "\n");
                                                                Thread.sleep(500);
                                                            }
                                                            Thread.sleep(1000);
                                                            IntentPrint("--------------------------------\n" +
                                                                    "   Total:        $" + totalpagar + "\n" +
                                                                    "\n"+
                                                                    "--------------------------------\n"+
                                                                    "   Por este pagare debo(emos) y\n"+
                                                                    "pagare(mos) incondicionalmente\n" +
                                                                    "            a la\n"+
                                                                    " Distribuidora Faili S.A de C.V\n" +
                                                                    "  la cantidad de $"+totalpagar +" MxN\n"+
                                                                    " respaldada por esta nota "+
                                                                    "       de venta a:\n"+
                                                                    value1+"\n" +
                                                                    "  Gracias por su compra :)"+
                                                                    "\n"+
                                                                    "\n"+
                                                                    "\n"+
                                                                    "\n");
                                                            Thread.sleep(500);
                                                        }

                                                    }catch (Exception e){
                                                        Toast.makeText(venta_productos.this, e.toString(), Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            };
                                            t.start();
                                            guardardatos();
                                            stock();
                                            dialogoContado.dismiss();

                                        }

                                    });
                                    builder.show();

                                } else
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(venta_productos.this);
                                    builder.setTitle("IMPRIMIR");
                                    builder.setIcon(R.drawable.impresora);
                                    builder.setMessage("Seguro desea imprimir?");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogoContado = new ProgressDialog(venta_productos.this);
                                            dialogoContado.setTitle("Espere...");
                                            dialogoContado.setMessage("Enviando Datos...");
                                            dialogoContado.show();
                                            enviardatosCredito enviar = new enviardatosCredito();
                                            enviar.execute(map_cliente_id.get(textViewListaClientes.getText()), "2", "0", textRuta.getText().toString(), id_usuario,
                                                    strFecha, strFecha, "0", "SIN DETALLES");

                                        }
                                    });
                                    builder.show();

                                }
                            }

                        }
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
        });
        alertCredito.show();

    }
    //DIALOGO EDITAR_PRODUCTO

    public  void EditarProduto(Activity activity){
        //alert dialog para editar produtco
        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_editar_producto);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));        //mostrar alertDialog
        dialog.show();
        //instanciar objetos
        final EditText edit_cantidad = (EditText) dialog.findViewById(R.id.editCantidad);
        final EditText edit_precio = (EditText) dialog.findViewById(R.id.editPrecio);
        LinearLayout botonCambiar = (LinearLayout) dialog.findViewById(R.id.btnCambiar);
        LinearLayout botonEliminar = (LinearLayout) dialog.findViewById(R.id.btnEliminar);
        //mostrar cantidad en edit_cantidad
        edit_cantidad.setText(cantidad_item.get(Integer.parseInt(posicion)));
        //obtener nombre del producto
        final String nombre = items.get(Integer.parseInt(posicion));
        //obtener precio del proucto
        final String precio_1 = map_producto_precio.get(nombre);
        //mostrar precio del producto en edit_precio
        edit_precio.setText(precio_1);
        //CONSULTA stock EN BD
        AdminSQLiteOpenHelper adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
        SQLiteDatabase sqLiteDatabase = adminSQLiteOpenHelper.getReadableDatabase();
        Cursor fila = sqLiteDatabase.rawQuery("select stock_producto from detalles_productos where nombre_producto "+"='" +nombre+"' limit 1",null);
        Cursor fila_precio = sqLiteDatabase.rawQuery("select precio_venta_producto from detalles_productos where nombre_producto"+"='"+nombre+"' limit 1",null);
        valor_stock_1=0.00;
        if(fila!=null){
            if(fila.moveToFirst()){
                String valor_stock_string = fila.getString(0);
                valor_stock_1 = Double.parseDouble(valor_stock_string);
                //valor_stock_1=(int)valor_stock_bol;

            }
        }
        //precio
        valor_precio=0.00;
        if(fila_precio!=null){
            if(fila_precio.moveToFirst()){
                String valor_precio_string = fila_precio.getString(0);
                valor_precio = Double.parseDouble(valor_precio_string);
                Log.e("precio_bool",valor_precio_string);
                //valor_precio=(int)valor_precio_bol;
            }
        }
        //mostrar Stock en TOAST
        Toast.makeText(venta_productos.this,"STOCK: "+valor_stock_1,Toast.LENGTH_SHORT).show();
        //CLick en Boton Cambiar
        botonCambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Obtener cantidad en el EditText
                String cantidad = edit_cantidad.getText().toString();
                //obtener precio en el EditText.
                String precio = edit_precio.getText().toString();
                //convertir a Double el String precio_1
                double precio_pro = Double.parseDouble(precio_1);
                //convertir a Double el String del precio en EditText
                double precio_ven = Double.parseDouble(precio);
                //obtener el 15%
                double precio_desc = valor_precio-(valor_precio*1/100);
                double precio_max = valor_precio+ (valor_precio*1/100);

                //si precio nuevo es menor que el 15%
                if(Double.compare(precio_ven,precio_desc)<0){
                    Toast.makeText(venta_productos.this,"Error en el precio",Toast.LENGTH_SHORT).show();
                }
                //si no hay suficiente producto
                if(Double.parseDouble(cantidad)>valor_stock_1){
                    Toast.makeText(venta_productos.this,"No tienes suficiente producto\n"+"Stock: "+valor_stock_1,Toast.LENGTH_SHORT).show();
                }
                int comparacion = Double.compare(precio_ven,precio_desc);
                Log.e("comparacion",String.valueOf(precio_ven) +String.valueOf(precio_desc) + " "+String.valueOf(comparacion));

                if(precio_ven>=precio_desc && Double.parseDouble(cantidad)<=valor_stock_1){
                    if(Double.compare(precio_ven,precio_max)>0){
                        Toast.makeText(venta_productos.this,"Error en el precio",Toast.LENGTH_SHORT).show();
                    }else {
                        cantidad_item.set(Integer.parseInt(posicion), cantidad);
                        precio_item.set(Integer.parseInt(posicion), Double.parseDouble(precio));
                        double precio1 = Double.parseDouble(precio);
                        double precio_total = Double.parseDouble(cantidad) * precio1;
                        //  Log.e("wilwilwil",String.valueOf(precio_total));
                        ADP_cantidad.notifyDataSetChanged();
                        precio_item.set(Integer.parseInt(posicion), precio_total);
                        ADP_Precio.notifyDataSetChanged();

                        ArrayList<Double> numbers = new ArrayList<Double>();
                        for (int i = 0; i < precio_item.size(); i++) {

                            numbers.add(precio_item.get(i));
                        }
                        double suma = 0;
                        for (int j = 0; j < numbers.size(); j++) {
                            suma += numbers.get(j);
                        }
                        textTotal.setText(String.valueOf(suma));
                        dialog.dismiss();

                    }
                }


            }
        });
        botonEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double total=0.00;
                items.remove(Integer.parseInt(posicion));
                precio_item.remove(Integer.parseInt(posicion));
                cantidad_item.remove(Integer.parseInt(posicion));
                ADP.notifyDataSetChanged();
                ADP_Precio.notifyDataSetChanged();
                ADP_cantidad.notifyDataSetChanged();
                for(int i=0;i<precio_item.size();i++){
                    double precio = precio_item.get(i);
                    total =total+ precio;

                }

                textTotal.setText(String.valueOf(total));
                dialog.dismiss();
            }
        });

    }

    //------------------------------CUADRO DIALOGO AGREGAR O IMPRIMIR/---------------------------------------/
    public void DialogoBuscarAgregarImpresora(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_agregar_impresora_imprimir);
        dialog.show();
    }

    //---------------------------METODO CHECKRADIOBUTTON
    private void checkRadioButton(){
        radio_Contado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radio_Credito.setChecked(false);
            }
        });
        radio_Credito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radio_Contado.setChecked(false);
            }
        });
    }

    //-----------------------------GUARDAR DATOS EN LA BD--------
    private void guardardatos(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(venta_productos.this,"administracion1",null,1);
        SQLiteDatabase bd = admin.getWritableDatabase();
        ContentValues registro = new ContentValues();
        SharedPreferences sharedPreferences = getSharedPreferences("productos", MODE_PRIVATE);
        String objetos = sharedPreferences.getString("lista_productos_id","");
        String objetos1 = objetos.replaceAll("[^\\dA-Za-z., /:]","");
        String[] pairs = objetos1.split(",");
        for(int i = 0;i<pairs.length;i++){
            String pair = pairs[i];
            String[]keyvalue = pair.split(":");
            map_producto_codigo.put(keyvalue[0], String.valueOf(keyvalue[1]));
        }
        for(int i=0; i< items.size();i++){
            String folio = venta_cliente;
            registro.put("folio",folio);
            registro.put("codigo_producto",map_producto_codigo.get(items.get(i)));
            registro.put("cantidad_vendido",cantidad_item.get(i));
            double pesoProductoTotal = Double.parseDouble(map_peso_producto.get(items.get(i)))* Double.parseDouble(cantidad_item.get(i));
            registro.put("peso_producto",pesoProductoTotal);

            String id_producto = map_producto_codigo.get(items.get(i));
            //int tamanio = map_producto_precioVenta.size();
            String precio_Compra = map_producto_precioVenta.get(id_producto);
            final String nombre = items.get((i));
            //obtener precio del proucto
            final String precio_1 = map_producto_precio.get(nombre);
            //mostrar precio del producto en edit_precio
            //CONSULTA stock EN BD
            AdminSQLiteOpenHelper adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
            SQLiteDatabase sqLiteDatabase = adminSQLiteOpenHelper.getReadableDatabase();
            Cursor fila_precio = sqLiteDatabase.rawQuery("select precio_venta_producto from detalles_productos where nombre_producto"+"='"+nombre+"' limit 1",null);
            double valor_precio1=0;
            if(fila_precio!=null){
                if(fila_precio.moveToFirst()){
                    String valor_precio_string = fila_precio.getString(0);
                    double valor_precio_bol = Double.parseDouble(valor_precio_string);
                    valor_precio1=valor_precio_bol;
                }
            }

            registro.put("precio_compra",precio_Compra);
           // Log.e("precio_venta",precio_Compra);
            //Log.e("precio_venta",String.valueOf(precio_Compra));
            registro.put("precio_real",valor_precio1);
            registro.put("precio_venta",precio_1);
            bd.insert("venta_detalles",null,registro);
        }
        //Toast.makeText(venta_productos.this,String.valueOf(items.size()),Toast.LENGTH_SHORT).show();

        bd.close();
    }
    //STOCK
    private void stock(){

        for(int i=0;i<items.size();i++){
            AdminSQLiteOpenHelper adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
            SQLiteDatabase sqLiteDatabase = adminSQLiteOpenHelper.getReadableDatabase();
            String nombre_producto = items.get(i);
            String cantidad_productos = cantidad_item.get(i);
            Cursor fila = sqLiteDatabase.rawQuery("select stock_producto from detalles_productos where nombre_producto "+"='" +nombre_producto+"' limit 1",null);
            String valor="";
            if(fila!=null){
                if(fila.moveToFirst()){
                    valor = fila.getString(0);
                    double existencia = Double.parseDouble(valor);
                    Double vendido = Double.parseDouble(cantidad_productos);
                    Double stock_actual = existencia - vendido;
                    ContentValues actualizar = new ContentValues();
                    actualizar.put("stock_producto",String.valueOf(stock_actual));
                    sqLiteDatabase.update("detalles_productos",actualizar,"nombre_producto=?",new String[]{nombre_producto});
                    Log.i("producto_vendido",nombre_producto+" "+ valor + " "+cantidad_productos+" "+stock_actual);
                }
                 }
            sqLiteDatabase.close();
            /*
            AlertDialog.Builder alet_guardado = new AlertDialog.Builder(venta_productos.this);
            alet_guardado.setTitle("GUARDADO");
            alet_guardado.setMessage("Ha sido guardado correctamente la venta");
            alet_guardado.setCancelable(false);
            alet_guardado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    items.clear();
                    cantidad_item.clear();
                    precio_item.clear();
                    //textFolio.setText("");
                    textTotal.setText("0");
                    textViewListaClientes.setText("");
                    ADP.notifyDataSetChanged();
                    ADP_cantidad.notifyDataSetChanged();
                    ADP_Precio.notifyDataSetChanged();
                    Intent intent = new Intent(venta_productos.this,venta_productos.class);
                    startActivity(intent);
                }
            });
            alet_guardado.show();

             */
            SweetAlertDialog dialogo = new SweetAlertDialog(venta_productos.this,SweetAlertDialog.SUCCESS_TYPE);
            dialogo.setTitle("GUARDADO");
            dialogo.setContentText("Ha sido guardado correctamente la venta!");
            dialogo.setConfirmText("OK");
            dialogo.setCancelable(true);
            dialogo.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    items.clear();
                    cantidad_item.clear();
                    precio_item.clear();
                    //textFolio.setText("");
                    textTotal.setText("0");
                    textViewListaClientes.setText("");
                    ADP.notifyDataSetChanged();
                    ADP_cantidad.notifyDataSetChanged();
                    ADP_Precio.notifyDataSetChanged();
                    Intent intent = new Intent(venta_productos.this,venta_productos.class);
                    startActivity(intent);
                }
                });
            dialogo.show();



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

    private void insertarFecha(){
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yy");
        int fecha = c.get(Calendar.HOUR_OF_DAY);
        int fecha1 = c.get(Calendar.MINUTE);
        int fecha2 = c.get(Calendar.SECOND);
        hora = String.valueOf(fecha) + ":"+String.valueOf(fecha1) +":"+ String.valueOf(fecha2);
        Log.e("fecha",hora);
        strDate = sdf.format(c.getTime());
        strDate_Folio = sdf1.format(c.getTime());
        textFecha.setText(strDate);

    }
    private void insertarRuta(){
        sharedPref = getSharedPreferences("lista_clientes_usuario", Context.MODE_PRIVATE);
        final String numero_ruta_vendedor = sharedPref.getString("numero_ruta","");
        textRuta.setText(numero_ruta_vendedor);

    }
    private void cargarDatosMap(){
        SharedPreferences sharedPreferences = getSharedPreferences("productos", MODE_PRIVATE);
        String objetos = sharedPreferences.getString("lista_productos","");
        String objetos1 = objetos.replaceAll("[^\\dA-Za-z, ./:]","");
        String[] pairs = objetos1.split(",");
        for(int i = 0;i<pairs.length;i++){
            try{
                String pair = pairs[i];
                String[]keyvalue = pair.split(":");
                String valor1 = keyvalue[1];
                double valor2 = Double.valueOf(valor1);
                map_producto_precio.put(keyvalue[0], String.valueOf(valor2));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        SharedPreferences sharedPreferences_precioVenta = getSharedPreferences("productos", MODE_PRIVATE);
        String objetos_precioVenta = sharedPreferences_precioVenta.getString("lista_id_precioCompra","");
        String objetosa_precio = objetos_precioVenta.replaceAll("[^\\dA-Za-z,ñ, ./:]","");
        String[] pairs_precioVenta = objetosa_precio.split(",");
        for(int z = 0;z<pairs_precioVenta.length;z++){
            try{
                String pair_precioVenta = pairs_precioVenta[z];
                String[]keyvalue_PrecioVenta = pair_precioVenta.split(":");

                map_producto_precioVenta.put(keyvalue_PrecioVenta[0],keyvalue_PrecioVenta[1]);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        String listaPesoProducto = sharedPreferences.getString("lista_peso_producto","");
        String listaPesoProductoReplace = listaPesoProducto.replaceAll("[^\\dA-Za-z, ./:]","");
        String[] values = listaPesoProductoReplace.split(",");
        for(int i = 0;i<values.length;i++){
            try{
                String pair = values[i];
                String[]keyvalue = pair.split(":");
                String valor1 = keyvalue[1];
                double valor2 = Double.parseDouble(valor1);
                map_peso_producto.put(keyvalue[0], String.valueOf(valor2));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {

    }


    private class enviardatos extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();

        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String detalles = args[8];
            String fldCancelado = args[7];
            String fldFechaVentaProducto = args[6];
            String fldRegistrarFecha = args [5];                                                //-----------------------------------data baseeee-----------------------
            String id_usuario = args[4];
            String id_caja = args[3];
            String id_estadoOperacion = args[2];
            String id_tipoOperacion = args[1];
            String id_cliente = args[0];
            //String id_ventaProducto = args[0];

            ArrayList params = new ArrayList();
           //params.add(new BasicNameValuePair("id_ventaProducto",id_ventaProducto));
            params.add(new BasicNameValuePair("id_cliente",id_cliente));
            params.add(new BasicNameValuePair("id_tipoOperacion",id_tipoOperacion));
            params.add(new BasicNameValuePair("id_estadoOperacion",id_estadoOperacion));
            params.add(new BasicNameValuePair("id_caja",id_caja));    //------------------------------data ------baseee----------------------------------------------------
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
                    final String mensaje = result.getString("message");
                    if(mensaje.equals("error_caja_cerrada")){
                        //Log.e("mensaje","error");
                        SweetAlertDialog dialogo = new SweetAlertDialog(venta_productos.this,SweetAlertDialog.ERROR_TYPE);
                        dialogo.setTitle("ALERTA");
                        dialogo.setContentText("Imposible realizar venta, la caja se encuentra cerrada");
                        dialogo.setConfirmText("OK");
                        dialogo.setCancelable(true);
                        dialogo.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                items.clear();
                                cantidad_item.clear();
                                precio_item.clear();
                                //textFolio.setText("");
                                textTotal.setText("0");
                                textViewListaClientes.setText("");
                                ADP.notifyDataSetChanged();
                                ADP_cantidad.notifyDataSetChanged();
                                ADP_Precio.notifyDataSetChanged();
                                Intent intent = new Intent(venta_productos.this,venta_productos.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        dialogo.show();
                    }else if(mensaje.equals("error")){
                        SweetAlertDialog dialogo = new SweetAlertDialog(venta_productos.this,SweetAlertDialog.ERROR_TYPE);
                        dialogo.setTitle("ALERTA");
                        dialogo.setContentText("Ocurrio un error al realizar la venta");
                        dialogo.setConfirmText("OK");
                        dialogo.setCancelable(true);
                        dialogo.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                items.clear();
                                cantidad_item.clear();
                                precio_item.clear();
                                //textFolio.setText("");
                                textTotal.setText("0");
                                textViewListaClientes.setText("");
                                ADP.notifyDataSetChanged();
                                ADP_cantidad.notifyDataSetChanged();
                                ADP_Precio.notifyDataSetChanged();
                                Intent intent = new Intent(venta_productos.this,venta_productos.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        dialogo.show();
                    } else{
                            //dialogoContado.setMessage("Imprimiendo...");
                            Log.e("mensale","exito");
                            SharedPreferences setting0 = getSharedPreferences("nombre_cliente_vendido", MODE_PRIVATE);
                            final String value1 = setting0.getString("nombre_cliente", "");


                        //dialogoContado.dismiss();

                        //dialogoContado.setMessage("Guardando Datos...");
                        AdminSQLiteOpenHelper admin_1 = new AdminSQLiteOpenHelper(venta_productos.this, "administracion", null, 1);
                        SQLiteDatabase bd_1 = admin_1.getWritableDatabase();
                        ContentValues registro = new ContentValues();
                        registro.put("folio", mensaje);
                        registro.put("total", textTotal.getText().toString());
                        registro.put("id_cliente", map_cliente_id.get(textViewListaClientes.getText()));
                        registro.put("estado", "Subido");
                        registro.put("tipo_operacion", "1");
                        registro.put("estado_operacion", "2");
                        registro.put("importe",importe);
                        registro.put("cancelado","0");
                        registro.put("postActualizacionRegistro","0");
                        bd_1.insert("venta_cliente", null, registro);
                        bd_1.close();
                        //guardar detalles
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(venta_productos.this,"administracion1",null,1);
                        SQLiteDatabase bd = admin.getWritableDatabase();
                        ContentValues registro_d = new ContentValues();
                        SharedPreferences sharedPreferences = getSharedPreferences("productos", MODE_PRIVATE);
                        String objetos = sharedPreferences.getString("lista_productos_id","");
                        String objetos1 = objetos.replaceAll("[^\\dA-Za-z., /:]","");
                        String[] pairs = objetos1.split(",");
                        for(int i = 0;i<pairs.length;i++){
                            String pair = pairs[i];
                            String[]keyvalue = pair.split(":");
                            map_producto_codigo.put(keyvalue[0], String.valueOf(keyvalue[1]));
                        }
                        for(int i=0; i< items.size();i++){
                            String folio = venta_cliente;
                            registro_d.put("folio",mensaje);
                            registro_d.put("codigo_producto",map_producto_codigo.get(items.get(i)));
                            registro_d.put("cantidad_vendido",cantidad_item.get(i));
                            double pesoProductoTotal = Double.parseDouble(map_peso_producto.get(items.get(i)))* Double.parseDouble(cantidad_item.get(i));
                            registro_d.put("peso_producto",pesoProductoTotal);

                            String id_producto = map_producto_codigo.get(items.get(i));
                            //int tamanio = map_producto_precioVenta.size();
                            String precio_Compra = map_producto_precioVenta.get(id_producto);
                            final String nombre = items.get((i));
                            //obtener precio del proucto
                            final String precio_1 = map_producto_precio.get(nombre);
                            //mostrar precio del producto en edit_precio
                            //CONSULTA stock EN BD
                            AdminSQLiteOpenHelper adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
                            SQLiteDatabase sqLiteDatabase = adminSQLiteOpenHelper.getReadableDatabase();
                            Cursor fila_precio = sqLiteDatabase.rawQuery("select precio_venta_producto from detalles_productos where nombre_producto"+"='"+nombre+"' limit 1",null);
                            double valor_precio1=0;
                            if(fila_precio!=null){
                                if(fila_precio.moveToFirst()){
                                    String valor_precio_string = fila_precio.getString(0);
                                    double valor_precio_bol = Double.parseDouble(valor_precio_string);
                                    valor_precio1=valor_precio_bol;
                                }
                            }

                            registro_d.put("precio_compra",precio_Compra);
                            // Log.e("precio_venta",precio_Compra);
                            //Log.e("precio_venta",String.valueOf(precio_Compra));
                            registro_d.put("precio_real",valor_precio1);
                            registro_d.put("precio_venta",precio_1);
                            bd.insert("venta_detalles",null,registro_d);
                        }
                        //Toast.makeText(venta_productos.this,String.valueOf(items.size()),Toast.LENGTH_SHORT).show();
                        bd.close();
                        //Toast.makeText(venta_productos.this,"FOLIO RECIBIDO"+mensaje, Toast.LENGTH_SHORT).show();
                        //texto.setTextColor(Color.GREEN);
                        //boton_estado.setImageResource(R.mipmap.ic_check);
                        //finish();
                        //getResult();
                        stock();
                        //obtebner detalles
                        //dialogoContado.setMessage("Enviando Detalles...");
                        AdminSQLiteOpenHelper admin_obdetalles = new AdminSQLiteOpenHelper(venta_productos.this,"administracion1",null,1);
                        SQLiteDatabase bd_obdetalles = admin_obdetalles.getWritableDatabase();
                        Cursor fila = bd_obdetalles.rawQuery("select  folio, codigo_producto,cantidad_vendido ,peso_producto, precio_compra, precio_real,precio_venta from venta_detalles where folio "+"='" +mensaje+"'" ,null);
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
                        //Toast.makeText(venta_productos.this,output, Toast.LENGTH_SHORT).show();
                        enviar_dato.execute(output,id_enterprise,"");
                        //return resultSet;

                        final Thread t = new Thread(){
                            @Override
                            public void run(){
                                try{
                                    for (int m=0;m<2;m++) {
                                        IntentPrint("\n     COMERCIALIZADORA FAILI.   \n"
                                                + "          S.A. de C.V     \n " +
                                                "Calzada Jorge Gomez # 199 Col \n " +
                                                "Cerro Hueco, Tuxtla Gutierrez \n" +
                                                "         Chis., Mex.\n" +
                                                "RFC:CFA1607131N1     " + strDate + "\n" +
                                                "HORA:" + hora + "          RUTA:" + textRuta.getText().toString() + "\n" +
                                                "CLIENTE:" + value1 + "\n" +
                                                "FOLIO:   " + mensaje +"\n"+
                                                "           CONTADO\n" +
                                                "--------------------------------\n" +
                                                "DESCRIPCION\n" +
                                                "CANTIDAD     PRECIO      TOTAL\n" +
                                                "--------------------------------\n");
                                        Thread.sleep(2000);

                                        for (int k = 0; k < items.size(); k++) {
                                            String cantidad = cantidad_item.get(k);
                                            String descripcion = items.get(k);
                                            String precio = map_producto_precio.get(descripcion);
                                            String total = String.valueOf(precio_item.get(k));
                                            IntentPrint(descripcion + "\n" + cantidad + "         $" + precio +"         $" + total + "\n");
                                            Thread.sleep(500);
                                        }
                                        Thread.sleep(1000);
                                        IntentPrint("--------------------------------\n" +
                                                "   Total:        $" + totalpagar + "\n" +
                                                "   Efectivo:     $" + importe + "\n" +
                                                "   Cambio:       $" + cambio_imprimir + "\n" +
                                                "--------------------------------\n"+
                                                "    Gracias por su compra!!\n" +
                                                "   el importe de esta nota\n" +
                                                "   sera aplicada a la factura \n" +
                                                "           del dia\n"+
                                                "\n"+
                                                "\n"+
                                                "\n"+
                                                "\n");

                                        Thread.sleep(500);

                                    }

                                }catch (Exception e){
                                    Toast.makeText(venta_productos.this, e.toString(), Toast.LENGTH_SHORT).show();

                                }
                            }
                        };
                        t.start();
                        if(t.isAlive()){
                            Log.e("proceso",String.valueOf(t.isAlive()));

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
    private class enviardatosCredito extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();

        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String detalles = args[8];
            String fldCancelado = args[7];
            String fldFechaVentaProducto = args[6];  //---------------------------------------data base -----------compra Contado
            String fldRegistarFecha= args [5];
            String id_usuario = args[4];
            String id_caja = args[3];
            String id_estadoOperacion = args[2];
            String id_tipoOperacion = args[1 ];
            String id_cliente = args[0];
            //String id_ventaProducto = args[0];

            ArrayList params = new ArrayList();
            //params.add(new BasicNameValuePair("id_ventaProducto",id_ventaProducto));
            params.add(new BasicNameValuePair("id_cliente",id_cliente));
            params.add(new BasicNameValuePair("id_tipoOperacion",id_tipoOperacion));
            params.add(new BasicNameValuePair("id_estadoOperacion",id_estadoOperacion));
            params.add(new BasicNameValuePair("id_caja",id_caja));
            params.add(new BasicNameValuePair("id_usuario",id_usuario));
            params.add(new BasicNameValuePair("fldFechaVentaProducto",fldFechaVentaProducto));   //data base --------------------------compra Contado--------------------------
            params.add(new BasicNameValuePair("fldRegistrarFecha",fldRegistarFecha));
            params.add(new BasicNameValuePair("fldCancelado",fldCancelado));
            params.add(new BasicNameValuePair("detalles",detalles));
            JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
            return json;

        }
        protected void onPostExecute(JSONObject result){
            try{
                if(result != null){
                    final String mensaje = result.getString("message");
                    if(mensaje.equals("error")){
                        //Log.e("mensaje","error");
                        SweetAlertDialog dialogo = new SweetAlertDialog(venta_productos.this,SweetAlertDialog.ERROR_TYPE);
                        dialogo.setTitle("ALERTA");
                        dialogo.setContentText("Imposible realizar venta, la caja se encuentra cerrada");
                        dialogo.setConfirmText("OK");
                        dialogo.setCancelable(true);
                        dialogo.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                items.clear();
                                cantidad_item.clear();
                                precio_item.clear();
                                //textFolio.setText("");
                                textTotal.setText("0");
                                textViewListaClientes.setText("");
                                ADP.notifyDataSetChanged();
                                ADP_cantidad.notifyDataSetChanged();
                                ADP_Precio.notifyDataSetChanged();
                                Intent intent = new Intent(venta_productos.this,venta_productos.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        dialogo.show();
                    }else {
                        //dialogoContado.setMessage("Imprimiendo...");
                        Log.e("mensale","exito");
                        SharedPreferences setting0 = getSharedPreferences("nombre_cliente_vendido", MODE_PRIVATE);
                        final String value1 = setting0.getString("nombre_cliente", "");
                        if(!mac_bluetooth.equals("")){

                            final Thread t = new Thread(){
                                @Override
                                public void run(){
                                    try{
                                        for (int m=0;m<2;m++) {
                                            IntentPrint("\n     COMERCIALIZADORA FAILI.   \n"
                                                    + "          S.A. de C.V     \n " +
                                                    "Calzada Jorge Gomez # 199 Col \n " +
                                                    "Cerro Hueco, Tuxtla Gutierrez \n" +
                                                    "         Chis., Mex.\n" +
                                                    "RFC:CFA1607131N1     " + strDate + "\n" +
                                                    "HORA:" + hora + "          RUTA:" + textRuta.getText().toString() + "\n" +
                                                    "CLIENTE:" + value1 + "\n" +
                                                    "FOLIO:   " + mensaje +"\n"+
                                                    "           CREDITO\n" +
                                                    "--------------------------------\n" +
                                                    "DESCRIPCION\n" +
                                                    "CANTIDAD     PRECIO      TOTAL\n" +
                                                    "--------------------------------\n");
                                            Thread.sleep(2000);
                                            for (int k = 0; k < items.size(); k++) {
                                                String cantidad = cantidad_item.get(k);
                                                String descripcion = items.get(k);
                                                String precio = map_producto_precio.get(descripcion);
                                                String total = String.valueOf(precio_item.get(k));
                                                IntentPrint(descripcion + "\n" + cantidad + "         $" + precio +"         $" + total + "\n");
                                                Thread.sleep(500);
                                            }
                                            Thread.sleep(1000);
                                            IntentPrint("--------------------------------\n" +
                                                    "   Total:        $" + totalpagar + "\n" +
                                                    "\n"+
                                                    "--------------------------------\n"+
                                                    "   Por este pagare debo(emos) y\n"+
                                                    "pagare(mos) incondicionalmente\n" +
                                                    "            a la\n"+
                                                    " Distribuidora Faili S.A de C.V\n" +
                                                    "  la cantidad de $"+totalpagar +" MxN\n"+
                                                    " respaldada por esta nota "+
                                                    "       de venta a:\n"+
                                                     value1+"\n" +
                                                    "  Gracias por su compra :)"+
                                                    "\n"+
                                                    "\n"+
                                                    "\n"+
                                                    "\n");
                                            Thread.sleep(500);

                                        }

                                    }catch (Exception e){
                                        Toast.makeText(venta_productos.this, e.toString(), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            };
                            t.run();
                        }
                        //dialogoContado.setMessage("Guardando Datos...");
                        AdminSQLiteOpenHelper admin_1 = new AdminSQLiteOpenHelper(venta_productos.this, "administracion", null, 1);
                        SQLiteDatabase bd_1 = admin_1.getWritableDatabase();
                        ContentValues registro = new ContentValues();
                        registro.put("folio", mensaje);
                        registro.put("total", textTotal.getText().toString());
                        registro.put("id_cliente", map_cliente_id.get(textViewListaClientes.getText()));
                        registro.put("estado", "Subido");
                        registro.put("tipo_operacion", "2");
                        registro.put("estado_operacion", "2");
                        registro.put("importe",importe);
                        registro.put("cancelado","0");
                        registro.put("postActualizacionRegistro","0");
                        bd_1.insert("venta_cliente", null, registro);
                        bd_1.close();
                        //guardar detalles
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(venta_productos.this,"administracion1",null,1);
                        SQLiteDatabase bd = admin.getWritableDatabase();
                        ContentValues registro_d = new ContentValues();
                        SharedPreferences sharedPreferences = getSharedPreferences("productos", MODE_PRIVATE);
                        String objetos = sharedPreferences.getString("lista_productos_id","");
                        String objetos1 = objetos.replaceAll("[^\\dA-Za-z., /:]","");
                        String[] pairs = objetos1.split(",");
                        for(int i = 0;i<pairs.length;i++){
                            String pair = pairs[i];
                            String[]keyvalue = pair.split(":");
                            map_producto_codigo.put(keyvalue[0], String.valueOf(keyvalue[1]));
                        }
                        for(int i=0; i< items.size();i++){
                            String folio = venta_cliente;
                            registro_d.put("folio",mensaje);
                            registro_d.put("codigo_producto",map_producto_codigo.get(items.get(i)));
                            registro_d.put("cantidad_vendido",cantidad_item.get(i));
                            double pesoProductoTotal = Double.parseDouble(map_peso_producto.get(items.get(i)))* Double.parseDouble(cantidad_item.get(i));
                            registro_d.put("peso_producto",pesoProductoTotal);

                            String id_producto = map_producto_codigo.get(items.get(i));
                            //int tamanio = map_producto_precioVenta.size();
                            String precio_Compra = map_producto_precioVenta.get(id_producto);
                            final String nombre = items.get((i));
                            //obtener precio del proucto
                            final String precio_1 = map_producto_precio.get(nombre);
                            //mostrar precio del producto en edit_precio
                            //CONSULTA stock EN BD
                            AdminSQLiteOpenHelper adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
                            SQLiteDatabase sqLiteDatabase = adminSQLiteOpenHelper.getReadableDatabase();
                            Cursor fila_precio = sqLiteDatabase.rawQuery("select precio_venta_producto from detalles_productos where nombre_producto"+"='"+nombre+"' limit 1",null);
                            double valor_precio1=0;
                            if(fila_precio!=null){
                                if(fila_precio.moveToFirst()){
                                    String valor_precio_string = fila_precio.getString(0);
                                    double valor_precio_bol = Double.parseDouble(valor_precio_string);
                                    valor_precio1=valor_precio_bol;
                                }
                            }

                            registro_d.put("precio_compra",precio_Compra);
                            // Log.e("precio_venta",precio_Compra);
                            //Log.e("precio_venta",String.valueOf(precio_Compra));
                            registro_d.put("precio_real",valor_precio1);
                            registro_d.put("precio_venta",precio_1);
                            bd.insert("venta_detalles",null,registro_d);
                        }
                        //Toast.makeText(venta_productos.this,String.valueOf(items.size()),Toast.LENGTH_SHORT).show();
                        bd.close();
                        //Toast.makeText(venta_productos.this,"FOLIO RECIBIDO"+mensaje, Toast.LENGTH_SHORT).show();
                        //texto.setTextColor(Color.GREEN);
                        //boton_estado.setImageResource(R.mipmap.ic_check);
                        //finish();
                        //getResult();
                        stock();
                        //obtebner detalles
                       // dialogoContado.setMessage("Enviando Detalles...");
                        AdminSQLiteOpenHelper admin_obdetalles = new AdminSQLiteOpenHelper(venta_productos.this,"administracion1",null,1);
                        SQLiteDatabase bd_obdetalles = admin_obdetalles.getWritableDatabase();
                        Cursor fila = bd_obdetalles.rawQuery("select  folio, codigo_producto,cantidad_vendido ,peso_producto, precio_compra, precio_real,precio_venta from venta_detalles where folio "+"='" +mensaje+"'" ,null);
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
                        //Toast.makeText(venta_productos.this,output, Toast.LENGTH_SHORT).show();
                        enviar_dato.execute(output,id_enterprise,"");
                        //return resultSet;
                    }
                }else {
                    //Toast.makeText(ProgressIntentService.this,"no conectado con el servdor", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }




    private class enviardatosContado extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();

        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String detalles = args[8];
            String fldCancelado = args[7];
            String fldFechaVentaProducto = args[6];    //---------------------------------------------------data base --------------CompraContado
            String fldRegistrarFecha = args [5];
            String id_usuario = args[4];
            String id_caja = args[3];
            String id_estadoOperacion = args[2];
            String id_tipoOperacion = args[1];
            String id_cliente = args[0];
            //String id_ventaProducto = args[0];

            ArrayList params = new ArrayList();
            //params.add(new BasicNameValuePair("id_ventaProducto",id_ventaProducto));
            params.add(new BasicNameValuePair("id_cliente",id_cliente));
            params.add(new BasicNameValuePair("id_tipoOperacion",id_tipoOperacion));
            params.add(new BasicNameValuePair("id_estadoOperacion",id_estadoOperacion));
            params.add(new BasicNameValuePair("id_caja",id_caja));
            params.add(new BasicNameValuePair("id_usuario",id_usuario));
            params.add(new BasicNameValuePair("fldFechaVentaProducto",fldFechaVentaProducto));  //-----------data base ---------------CompraContado-------------------------------------
            params.add(new BasicNameValuePair("fldRegistrarFecha",fldRegistrarFecha));
            params.add(new BasicNameValuePair("fldCancelado",fldCancelado));
            params.add(new BasicNameValuePair("detalles",detalles));
            JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
            return json;

        }
        protected void onPostExecute(JSONObject result){
            try{
                if(result != null){
                    final String mensaje = result.getString("message");
                    if(mensaje.equals("error")){
                        //Log.e("mensaje","error");
                        SweetAlertDialog dialogo = new SweetAlertDialog(venta_productos.this,SweetAlertDialog.ERROR_TYPE);
                        dialogo.setTitle("ALERTA");
                        dialogo.setContentText("Imposible realizar venta, la caja se encuentra cerrada");
                        dialogo.setConfirmText("OK");
                        dialogo.setCancelable(true);
                        dialogo.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                items.clear();
                                cantidad_item.clear();
                                precio_item.clear();
                                //textFolio.setText("");
                                textTotal.setText("0");
                                textViewListaClientes.setText("");
                                ADP.notifyDataSetChanged();
                                ADP_cantidad.notifyDataSetChanged();
                                ADP_Precio.notifyDataSetChanged();
                                Intent intent = new Intent(venta_productos.this,venta_productos.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        dialogo.show();
                    }else {
                        if(!mac_bluetooth.equals("")){
                            dialogoContado.setMessage("Imprimiendo...");
                            Log.e("mensale","exito");
                            SharedPreferences setting0 = getSharedPreferences("nombre_cliente_vendido", MODE_PRIVATE);
                            final String value1 = setting0.getString("nombre_cliente", "");

                            final Thread t = new Thread(){
                                @Override
                                public void run(){
                                    try{
                                        for (int m=0;m<2;m++) {
                                            IntentPrint("\n     COMERCIALIZADORA FAILI.   \n"
                                                    + "          S.A. de C.V     \n " +
                                                    "Calzada Jorge Gomez # 199 Col \n " +
                                                    "Cerro Hueco, Tuxtla Gutierrez \n" +
                                                    "         Chis., Mex.\n" +
                                                    "RFC:CFA1607131N1     " + strDate + "\n" +
                                                    "HORA:" + hora + "          RUTA:" + textRuta.getText().toString() + "\n" +
                                                    "CLIENTE:" + value1 + "\n" +
                                                    "FOLIO:   " + mensaje +"\n"+
                                                    "           CONTADO\n" +
                                                    "--------------------------------\n" +
                                                    "DESCRIPCION\n" +
                                                    "CANTIDAD     PRECIO      TOTAL\n" +
                                                    "--------------------------------\n");
                                            Thread.sleep(2000);
                                            for (int k = 0; k < items.size(); k++) {
                                                String cantidad = cantidad_item.get(k);
                                                String descripcion = items.get(k);
                                                String precio = map_producto_precio.get(descripcion);
                                                String total = String.valueOf(precio_item.get(k));
                                                IntentPrint(descripcion + "\n" + cantidad + "         $" + precio +"         $" + total + "\n");
                                                Thread.sleep(500);
                                            }
                                            Thread.sleep(1000);
                                            IntentPrint("--------------------------------\n" +
                                                    "   Total:        $" + totalpagar + "\n" +
                                                    "   Efectivo:     $" + importe + "\n" +
                                                    "   Cambio:       $" + cambio_imprimir + "\n" +
                                                    "--------------------------------\n"+
                                                    "    Gracias por su compra!!\n" +
                                                    "   el importe de esta nota\n" + //-------------------------------------------------------------------777777777777777777777777777777777
                                                    "   sera aplicada a la factura \n" +
                                                    "           del dia\n"+
                                                    "\n"+
                                                    "\n"+
                                                    "\n"+
                                                    "\n");
                                            Thread.sleep(500);
                                        }

                                    }catch (Exception e){
                                        Toast.makeText(venta_productos.this, e.toString(), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            };
                            t.run();
                        }

                        //dialogoContado.setMessage("Guardando Datos...");
                        AdminSQLiteOpenHelper admin_1 = new AdminSQLiteOpenHelper(venta_productos.this, "administracion", null, 1);
                        SQLiteDatabase bd_1 = admin_1.getWritableDatabase();
                        ContentValues registro = new ContentValues();
                        registro.put("folio", mensaje);
                        registro.put("total", textTotal.getText().toString());
                        registro.put("id_cliente", map_cliente_id.get(textViewListaClientes.getText()));
                        registro.put("estado", "Subido");
                        registro.put("tipo_operacion", "1");
                        registro.put("estado_operacion", "2");
                        registro.put("importe",importe);
                        registro.put("cancelado","0");
                        registro.put("postActualizacionRegistro","0");
                        bd_1.insert("venta_cliente", null, registro);
                        bd_1.close();
                        //guardar detalles
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(venta_productos.this,"administracion1",null,1);
                        SQLiteDatabase bd = admin.getWritableDatabase();
                        ContentValues registro_d = new ContentValues();
                        SharedPreferences sharedPreferences = getSharedPreferences("productos", MODE_PRIVATE);
                        String objetos = sharedPreferences.getString("lista_productos_id","");
                        String objetos1 = objetos.replaceAll("[^\\dA-Za-z., /:]","");
                        String[] pairs = objetos1.split(",");
                        for(int i = 0;i<pairs.length;i++){
                            try{
                                String pair = pairs[i];
                                String[]keyvalue = pair.split(":");
                                map_producto_codigo.put(keyvalue[0], String.valueOf(keyvalue[1]));
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        for(int i=0; i< items.size();i++){
                            String folio = venta_cliente;
                            registro_d.put("folio",mensaje);
                            registro_d.put("codigo_producto",map_producto_codigo.get(items.get(i)));
                            registro_d.put("cantidad_vendido",cantidad_item.get(i));
                            double pesoProductoTotal = Double.parseDouble(map_peso_producto.get(items.get(i)))* Double.parseDouble(cantidad_item.get(i));
                            registro_d.put("peso_producto",pesoProductoTotal);

                            String id_producto = map_producto_codigo.get(items.get(i));
                            //int tamanio = map_producto_precioVenta.size();
                            String precio_Compra = map_producto_precioVenta.get(id_producto);
                            final String nombre = items.get((i));
                            //obtener precio del proucto
                            final String precio_1 = map_producto_precio.get(nombre);
                            //mostrar precio del producto en edit_precio
                            //CONSULTA stock EN BD
                            AdminSQLiteOpenHelper adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(venta_productos.this,"administracion",null,1);
                            SQLiteDatabase sqLiteDatabase = adminSQLiteOpenHelper.getReadableDatabase();
                            Cursor fila_precio = sqLiteDatabase.rawQuery("select precio_venta_producto from detalles_productos where nombre_producto"+"='"+nombre+"' limit 1",null);
                            double valor_precio1=0;
                            if(fila_precio!=null){
                                if(fila_precio.moveToFirst()){
                                    String valor_precio_string = fila_precio.getString(0);
                                    double valor_precio_bol = Double.parseDouble(valor_precio_string);
                                    valor_precio1=valor_precio_bol;
                                }
                            }

                            registro_d.put("precio_compra",precio_Compra);
                            // Log.e("precio_venta",precio_Compra);
                            //Log.e("precio_venta",String.valueOf(precio_Compra));
                            registro_d.put("precio_real",valor_precio1);
                            registro_d.put("precio_venta",precio_1);
                            bd.insert("venta_detalles",null,registro_d);
                        }
                        //Toast.makeText(venta_productos.this,String.valueOf(items.size()),Toast.LENGTH_SHORT).show();
                        bd.close();
                        //Toast.makeText(venta_productos.this,"FOLIO RECIBIDO"+mensaje, Toast.LENGTH_SHORT).show();
                        //texto.setTextColor(Color.GREEN);
                        //boton_estado.setImageResource(R.mipmap.ic_check);
                        //finish();
                        //getResult();
                        stock();
                        //obtebner detalles
                        //dialogoContado.setMessage("Enviando Detalles...");
                        AdminSQLiteOpenHelper admin_obdetalles = new AdminSQLiteOpenHelper(venta_productos.this,"administracion1",null,1);
                        SQLiteDatabase bd_obdetalles = admin_obdetalles.getWritableDatabase();
                        Cursor fila = bd_obdetalles.rawQuery("select  folio, codigo_producto,cantidad_vendido ,peso_producto, precio_compra, precio_real,precio_venta from venta_detalles where folio "+"='" +mensaje+"'" ,null);
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
                        //Toast.makeText(venta_productos.this,output, Toast.LENGTH_SHORT).show();
                        enviar_dato.execute(output,id_enterprise,"");
                        //return resultSet;
                    }
                }else {
                    //Toast.makeText(ProgressIntentService.this,"no conectado con el servdor", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
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
                    String mensaje = result.getString("message");
                    if(mensaje.equals("exito")){
                        //dialogoContado.dismiss();
                        //Toast.makeText(venta_productos.this,"DATOS GUARDADOS", Toast.LENGTH_SHORT).show();


                    }else {
                        //Toast.makeText(ProgressIntentService.this,"DATOS NO GUARDADOS EN EL SERVIDOR",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //Toast.makeText(consulta_ventas_totales.this,"no conectado con el servdor", Toast.LENGTH_SHORT).show();
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
        if(PrintHeader.length>256)
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
