package com.example.distrisandi.ui.home;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.distrisandi.AdminSQLiteOpenHelper;
import com.example.distrisandi.ProgressIntentService;
import com.example.distrisandi.R;
import com.example.distrisandi.registros;
import com.example.distrisandi.venta_productos;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;


public class HomeFragment extends Fragment {

    ImageButton btn1;
    ImageButton btn2;
    TextView txtTotal, txtContado, txtCredito;
    private HomeViewModel homeViewModel;


    private SharedPreferences sharedPref;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        /*final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        btn1 = (ImageButton) root.findViewById(R.id.btnMovimientos);
        btn2 = (ImageButton) root.findViewById(R.id.btnRegistros);

        txtTotal = root.findViewById(R.id.txtVentasTotales);
        txtContado = root.findViewById(R.id.txtVentasContado);
        txtCredito = root.findViewById(R.id.txtVentasCredito);

        btn1.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                //sharedPref = getActivity().getSharedPreferences("lista_clientes_usuario",Context.MODE_PRIVATE);
                //String jose2 = sharedPref.getString("lista_clientes","");
                    File share_clientes = new File("/data/data/com.example.distrisandi/shared_prefs/lista_clientes_usuario.xml");
                    File share_productos = new File("/data/data/com.example.distrisandi/shared_prefs/productos.xml");
                    if(share_clientes.exists() && share_productos.exists()){
                        Intent intent = new Intent(getActivity(), venta_productos.class);
                        startActivity(intent);
                        //Toast.makeText(getActivity(), "VENTAS  DE PRODUCTOS", Toast.LENGTH_SHORT).show();

                    }else {
                        //Toast.makeText(getActivity(), "POR FAVOR DESCARGAR LOS DATOS", Toast.LENGTH_SHORT).show();
                        alertDescargarDatos();
                    }



            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),registros.class);
                startActivity(intent);

            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(getActivity(), "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();
        Cursor contado = bd.rawQuery("select total from venta_cliente where tipo_operacion = 1 AND cancelado = 0", null);
        Cursor credito = bd.rawQuery("select total from venta_cliente where tipo_operacion = 2 AND cancelado = 0", null);

        double contadoTotal = 0;
        while(contado.moveToNext()){
            contadoTotal+= contado.getDouble(0);
        }
        txtContado.setText(String.valueOf(contadoTotal));

        double creditoTotal = 0;
        while(credito.moveToNext()){
            creditoTotal+=credito.getDouble(0);
        }
        txtCredito.setText(String.valueOf(creditoTotal));

        txtTotal.setText(String.valueOf(contadoTotal+creditoTotal));
    }

    /* private void alertdialogRegistros(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("REGISTROS");
        builder.setMessage("No disponible...");
        //builder.setIcon(R.mipmap.ic_icono_edit);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
    }*/


    private  void alertDescargarDatos(){
        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Descargar Datos!!");
        //builder.setIcon(R.drawable.ic_alerta);
        builder.setMessage("Porfavor Descarga los Datos para poder realizar los movimientos");
       // builder.setIcon(R.drawable.descargar);
        builder.setCancelable(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        //dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        //dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
         */
        new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Descargar datos!")
                .setContentText("Por favor descarga los datos para poder realizar los movimientos..")
                .setConfirmText("Ok!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();

    }


}