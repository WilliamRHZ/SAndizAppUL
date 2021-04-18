package com.example.distrisandi;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class devolucionesFragment extends Fragment {
    //variable para controles
    Map<String, String> map_client3_id = new HashMap<String, String>();
    Map<String, String> map_producto_codigo_barra = new HashMap<String, String>();
    private TextView teXtFechar;
    private SharedPreferences sharedPrefs;
    private TextView teXtRutar;
    private String strsFecha;
    private String hora;
    private String strDate;
    private String strDate_Folio;
    private String dataefe;
    private String id_usuariow;
    private String venta_cliente;
    List<String> items;
    List<Double> precio_item;
    List<String> cantidad_item;
    ArrayAdapter ADP;
    ArrayAdapter ADP_Precio;
    ArrayAdapter ADP_cantidad;
    private TextView textTotal;
    private TextView textViewListaClientez;
    private TextView editProductorx;
    private TextView editProducto;
    private EditText editFiltroNombreProductox;


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

    EditText tcDate;
    ImageButton tsDate;

    private void setContentView(int fragment_devoluciones) {
    }


    public devolucionesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //OBTENER ID DE CLIENTES
        SharedPreferences share_listaClientes = getActivity().getSharedPreferences("lista_clientes_usuario", Context.MODE_PRIVATE);
        String objetos02 = share_listaClientes.getString("lista_clientes_id", "");
        String objetos02_1 = objetos02.replaceAll("[^\\dA-Za-z, :]", "");
        String[] pairs1 = objetos02_1.split(",");
        for (int i = 0; i < pairs1.length; i++) {
            try{
                String pair = pairs1[i];
                String[] keyvalue = pair.split(":");
                map_client3_id.put(keyvalue[0], String.valueOf(keyvalue[1]));
                super.onCreate(savedInstanceState);
                setContentView(R.layout.fragment_devoluciones);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        ///////////////////////////////
        setContentView(R.layout.fragment_devoluciones);
        SharedPreferences sharedPreferences1 = getActivity().getSharedPreferences("bluetooth_info", Context.MODE_PRIVATE);
        mac_bluetooth = sharedPreferences1.getString("mac_bluetooth", "");
        SharedPreferences setting = getActivity().getSharedPreferences("login_preference", Context.MODE_PRIVATE);
        id_usuariow = setting.getString("username", "");

        final Calendar c = Calendar.getInstance();
        SimpleDateFormat sdt = new SimpleDateFormat("YYYY-MM-dd");
        strsFecha = sdt.format(c.getTime());
        Log.e("fechasssssssss", strsFecha);

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_devoluciones, container, false);

        teXtFechar = (TextView) view.findViewById(R.id.tc_date);
        teXtRutar = (TextView) view.findViewById(R.id.tc_ruts);
        editProducto = (TextView) view.findViewById(R.id.editNombreProductox);
        textViewListaClientez = (TextView) view.findViewById(R.id.textViewClientex);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) view.findViewById(R.id.navigationView_venta);
        insertarFechar();
        insertarRutar();
        //cargarDatosMap();
/*
        //OBTENER CLICK EN ICONOS DEL BOOTOOMVIEW
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.navigation_buscar) {
                    if ((textViewListaClientez.getText().toString()).equals("")) {
                        Toast.makeText(getActivity(), "Primero agrega un cliente", Toast.LENGTH_SHORT).show();
                    } else {
                        //DialogoListaProductorVentarx(devolucionesFragment.this);
                    }

                }
                if (id == R.id.navigation_limpiar) {
                    //IntentPrint("\n     COMERCIALIZADORA FAILI ");
                    if (items.size() <= 0) {/*
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
                   /*     new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
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

                    } else {
                        new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
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
                if (id == R.id.navigation_codigo_barra) {

                    if ((textViewListaClientez.getText().toString()).equals("")) {
                        Toast.makeText(getActivity(), "Primero agrega un cliente", Toast.LENGTH_SHORT).show();

                    } else {
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("productos", Context.MODE_PRIVATE);
                        String objetos = sharedPreferences.getString("lista_key_producto", "");
                        String objetos1 = objetos.replaceAll("[^\\dA-Za-z, ./:]", "");
                        String[] pairs = objetos1.split(",");
                        for (int i = 0; i < pairs.length; i++) {
                            String pair = pairs[i];
                            String[] keyvalue = pair.split(":");
                            map_producto_codigo_barra.put(keyvalue[0], String.valueOf(keyvalue[1]));
                        }
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.setCancelable(true);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setContentView(R.layout.dialog_codigo_buscar);
                        final EditText editCodigo = (EditText) dialog.findViewById(R.id.EditBuscar);
                        LinearLayout Buscar = (LinearLayout) dialog.findViewById(R.id.btnBuscar);
                        Buscar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String codigo = editCodigo.getText().toString();
                                String producto = map_producto_codigo_barra.get(codigo);
                                if (producto == null) {
                                    Toast.makeText(getActivity(), "No existe un producto con este código", Toast.LENGTH_SHORT).show();
                                } else {
                                    editProducto.setText(producto);
                                    dialog.dismiss();
                                }

                            }

                        });
                        dialog.show();
                    }
                }
                if (id == R.id.navigation_Pagar) {
                    int existencia_venta = items.size();
                    if (existencia_venta == 0) {
                        Toast.makeText(getActivity(), "Agrega productos a la venta", Toast.LENGTH_SHORT).show();
                    } else {


                        AlertDialog.Builder alertPagar = new AlertDialog.Builder(getActivity());
                        alertPagar.setTitle("PAGO");
                        alertPagar.setMessage(" Elige la forma de pago de esta venta");
                        alertPagar.setPositiveButton("CONTADO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //    Dialogopagar(venta_productos.this);
                                venta_cliente = "";

                            }
                        });
                        alertPagar.setNegativeButton("CREDITO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //   DialogopagarCredito(venta_productos.this);
                                venta_cliente = "";
                            }
                        });
                        alertPagar.show();


                    }
                }
                return true;
            }
       });

    }
/*
                //DIALOG LISTAR PRODUCTO
                public void DialogoListaProductorVentarx (Activity activity){
                    final Dialog dialog = new Dialog(activity);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.dialogo_lista_productos);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    ListView listView = (ListView) dialog.findViewById(R.id.listViewProductos);
                    Button btnCancelar = (Button) dialog.findViewById(R.id.btnCancelarProductos);
                    TextView txtLista = (TextView) dialog.findViewById(R.id.textView);
                    editFiltroNombreProductox = (EditText) dialog.findViewById(R.id.editFiltroNombreProducto);
                    txtLista.setText("LISTA DE PRODUCTOS");
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("productos", Context.MODE_PRIVATE);
                    String objetos = sharedPreferences.getString("lista_productos_nombres", "");
                    String objetos1 = objetos.replaceAll("[^\\dA-Za-z,./ ]", "");

                    final String[] rueba = objetos1.split(",");
                    Arrays.sort(rueba);
                    final ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_list_productos, R.id.txtlistaProductos, rueba);
                    listView.setAdapter(arrayAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            TextView nombre_producto = (TextView) view.findViewById(R.id.txtlistaProductos);
                            editProductorx.setText(nombre_producto.getText().toString());
                            dialog.dismiss();


                        }
                    });
                    editFiltroNombreProductox.addTextChangedListener(new TextWatcher() {
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
                return true;
                }
            });*/
        return view;
        }

   private void insertarFechar() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        SimpleDateFormat sdf1 = new SimpleDateFormat("YY");
        int fecha = c.get(Calendar.HOUR_OF_DAY);
        int fecha1 = c.get(Calendar.MINUTE);
        int fecha2 = c.get(Calendar.SECOND);
        hora = String.valueOf(fecha) + ":" + String.valueOf(fecha1) + ":" + String.valueOf(fecha2);
        Log.e("fecha", hora);
        strDate = sdf.format(c.getTime());
        strDate_Folio = sdf1.format(c.getTime());
        teXtFechar.setText(strDate);

    }

    private void insertarRutar() {
        sharedPrefs = getActivity().getSharedPreferences("lista_clientes_usuario", Context.MODE_PRIVATE);
        final String numero_ruta_vendedor = sharedPrefs.getString("numero_ruta", "");
        teXtRutar.setText(numero_ruta_vendedor);
    }
}