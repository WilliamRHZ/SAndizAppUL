package com.example.distrisandi;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;


/**
 * A simple {@link Fragment} subclass.
 */
public class gastosFragment extends Fragment {
    private static final String[] GASTOS = new String[]{
            "ABONO A POLLOS", "AGUA", "APOYO DESCARGA",
            "BATERIA DE CARRO", "BLOQU.EO", "BOLSA DE ALTO VACIO",
            "BONIFICACIONES", "CAFETERÍA", "CAMBIO DE ACEITE", "CANDADO",
            "CASETA", "COMIDA", "COMISION", "COMPRAS",
            "COMPUTADORA P/VEHICULO", "DEGUSTACION", "DEVOLUCIONES",
            "DIESEL", "DIF PRECIO", "HOSPEDAJE","PROMOCIONES",
    };
    Map<String,String> map_clienter_id = new HashMap<String, String>();
    private TextView teXtFechar;
    private SharedPreferences sharedPrefs;
    private TextView teXtRutar;
    private String strsFecha;
    private String hora;
    private String strDate;
    private String strDate_Folio;
    private String dataefe;
    private String id_usuariou;
    private Button btnAgregarGasto;

    //TextView tvDate;
    //ImageButton tbDate;
    //EditText etDate;
    //ImageView tbDate;
    //ImageView esDate;
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

    private void setContentView(int fragment_gastos) {
    }

    public gastosFragment() {
        // Required empty public constructor
    }

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //OBTENER ID DE CLIENTES
        SharedPreferences share_listaClientes = getActivity().getSharedPreferences("lista_clientes_usuario", Context.MODE_PRIVATE);
        String objetos02 = share_listaClientes.getString("lista_clientes_id", "");
        String objetos02_1 = objetos02.replaceAll("[^\\dA-Za-z, :]", "");
        String[] pairs1 = objetos02_1.split(",");
        for (int i = 0; i < pairs1.length; i++) {
            String pair = pairs1[i];
            String[]keyvalue = pair.split(":");
            map_clienter_id.put(keyvalue[0], String.valueOf(keyvalue[1]));
        }
        ///////////////////////////////
        setContentView(R.layout.fragment_gastos);
        SharedPreferences sharedPreferences1 = getActivity().getSharedPreferences("bluetooth_info", Context.MODE_PRIVATE);
        mac_bluetooth= sharedPreferences1.getString("mac_bluetooth","");
        SharedPreferences setting = getActivity().getSharedPreferences("login_preference", Context.MODE_PRIVATE);
        id_usuariou = setting.getString("username", "");

            final Calendar c = Calendar.getInstance();
            SimpleDateFormat sdt = new SimpleDateFormat("YYYY-MM-dd");
            strsFecha = sdt.format(c.getTime());
            Log.e("fechasssssssss", strsFecha);


            //  teXtFechar = (TextView).findViewById(R.id.txtFecha);

            //Inflate = layout for this fragment
            //return inflater.inflate(R.layout.fragment_gastos, container, false);
            View view = inflater.inflate(R.layout.fragment_gastos, container, false);
            AutoCompleteTextView editText = view.findViewById(R.id.Gastos1);
            editText.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, GASTOS));
           // btnAgregarGasto = (Button)view.findViewById(R.id.btnAgregargasto);
            //Spinner gastos = (Spinner) view.findViewById(R.id.spinnerGastos);
            //Spinner Ruta = (Spinner) view.findViewById(R.id.spinnerRuta);
            //tvDate = (EditText) view.findViewById(R.id.tv_date);//add date picker---------------------------------------------------------------------------------------------<
            //tbDate=(ImageButton) view.findViewById(R.id.tb_date);
            //etDate = (EditText) view.findViewById(R.id.et_date);//add date picker----------------------------------------------------------------------------------------------<
            //esDate=(ImageView) view.findViewById(R.id.es_date);
        teXtFechar = (TextView)view.findViewById(R.id.txtFechar);
        teXtRutar = (TextView) view.findViewById(R.id.txtRutar);



            String[] gastos_valor = {"Todos", "---", "---", "---", "---", "---", "---", "---", "---"};
            String[] Ruta_valor = {"Todos", "---", "---", "---", "---", "---", "---", "---", "---"};
            //gastos.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, gastos_valor));
            //Ruta.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, Ruta_valor));


        insertarFechar();
        insertarRutar();

            //@Override
            // public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            // super.onViewCreated(view, savedInstanceState);
            //  tbDate.setOnClickListener(new View.OnClickListener() {
            //     @Override
            //   public void onClick(View v) {
            //    showDatePicker();
            //}
            //});
        return view;



    }

    public Button getBtnAgregarGasto() {
        return btnAgregarGasto;
    }


    private void insertarFechar(){
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        SimpleDateFormat sdf1 = new SimpleDateFormat("YY");
        int fecha = c.get(Calendar.HOUR_OF_DAY);
        int fecha1 = c.get(Calendar.MINUTE);
        int fecha2 = c.get(Calendar.SECOND);
        hora = String.valueOf(fecha) + ":"+String.valueOf(fecha1) +":"+ String.valueOf(fecha2);
        Log.e("fecha",hora);
        strDate = sdf.format(c.getTime());
        strDate_Folio = sdf1.format(c.getTime());
        teXtFechar.setText(strDate);

    }
    private void insertarRutar() {
        sharedPrefs = getActivity().getSharedPreferences("lista_clientes_usuario", Context.MODE_PRIVATE);
        final String numero_ruta_vendedor = sharedPrefs.getString("numero_ruta", "");
        teXtRutar.setText(numero_ruta_vendedor);
    }





    //Dialogo agregar nuevo gasto
public void Dialogoagregar(final Activity activity){ // muestra dialogo en el dialog activity
    final Dialog dialog = new Dialog(activity);
    dialog.setCancelable(true);
    dialog.setContentView(R.layout.fragment_filtergasto);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    dialog.show();


}

}
    //-------------------------------------------------- data picker 1 ------------------------------------------------------------------------
    /*private void showDatePicker()
    {
        pickdate date = new pickdate();

        Calendar calend =Calendar.getInstance();
       Bundle args = new Bundle();
        args.putInt("year",calend.get(Calendar.YEAR));
        args.putInt("month",calend.get(Calendar.MONTH));
        args.putInt("day",calend.get(Calendar.DAY_OF_MONTH));

        //date.setCallBack(ondate);
        date.show(getFragmentManager(),"Date Picker");

    }
    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        //  tvDate.setText(dayOfMonth + "/" +(monthOfYear+1)+ "/" +year);
        }
    };

}*/
