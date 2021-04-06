package com.example.distrisandi.ui.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.distrisandi.R;
import com.example.distrisandi.Sesion_Usuario;
import com.example.distrisandi.decive_list;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;


public class ToolsFragment extends Fragment  implements Runnable{

    private ToolsViewModel toolsViewModel;

    BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    protected static final String TAG = "TAG";
    private ProgressDialog mBluetoothConnectProgressDialog;
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private TextView textEstadoBluetooth;
    private Button botonConectar;
    private Button botonDesconectar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        toolsViewModel =
                ViewModelProviders.of(this).get(ToolsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tools, container, false);
        //final TextView textView = root.findViewById(R.id.text_tools);

        SharedPreferences sharedPreferences1 = getActivity().getSharedPreferences("bluetooth_info", MODE_PRIVATE);
        final String mac_bluetooth= sharedPreferences1.getString("mac_bluetooth","");


        textEstadoBluetooth = root.findViewById(R.id.textEstadoBluetooth);
        botonConectar = root.findViewById(R.id.btnConectar);
        botonDesconectar = root.findViewById(R.id.btnDesconectar);

        /*toolsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText("dfgg");
            }
        });*/
        if(mac_bluetooth.equals("")){

            textEstadoBluetooth.setText("Desconectado");
            botonDesconectar.setVisibility(View.GONE);

        }
        else {
            textEstadoBluetooth.setText("Conectado");
            botonConectar.setVisibility(View.GONE);


        }
        botonConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DialogoBuscarAgregarImpresora(venta_productos.this);
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(mBluetoothSocket !=null){
                    //Toast.makeText(this,"ya estas conectado"+mBluetoothDevice.getName(),Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder  alertBluetooth = new AlertDialog.Builder(getContext());
                    alertBluetooth.setTitle("CONEXION");
                    alertBluetooth.setIcon(R.drawable.impresora);
                    alertBluetooth.setMessage("Ya estas Conectado a:  "+mBluetoothDevice.getName());
                    alertBluetooth.setCancelable(false);
                    alertBluetooth.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertBluetooth.show();
                }else{
                    Toast.makeText(getContext(),"no estas conectado",Toast.LENGTH_SHORT).show();
                    if(mBluetoothAdapter == null){
                        //Toast.makeText(this,"Lista de Dispositivos",Toast.LENGTH_SHORT).show();
                    }else {
                        if(!mBluetoothAdapter.isEnabled()){
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                            Toast.makeText(getContext(),"Activar Bluetooth",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            ListPairedDevices();
                            Intent connectIntent = new Intent(getContext(), decive_list.class);
                            startActivityForResult(connectIntent,REQUEST_CONNECT_DEVICE);
                            Toast.makeText(getContext(),"Buscando bluuetooth",Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        });
        botonDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"Dispositivo Desconectado",Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPref = getContext().getSharedPreferences("bluetooth_info", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("mac_bluetooth","");
                editor.apply();
                textEstadoBluetooth.setText("Desconectado");

            }
        });
        return root;
    }

    //bluetooth

    public void onActivityResult(int mRequestCode, int mResultCode, Intent mDataIntent){
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);

        switch (mRequestCode){
            case REQUEST_CONNECT_DEVICE:
                if(mResultCode == Activity.RESULT_OK){
                    Bundle mExtra = mDataIntent.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    Log.v(TAG,"Coming incoming address" + mDeviceAddress);
                    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    mBluetoothConnectProgressDialog = ProgressDialog.show(getContext(),"Conectando..",mBluetoothDevice.getName() + ":" +
                            mBluetoothDevice.getAddress(), true, false);
                    Thread mBluetoothConnectThread = new Thread(this);
                    mBluetoothConnectThread.start();
                }
                break;
            case REQUEST_ENABLE_BT:
                if(mResultCode == Activity.RESULT_OK){
                    ListPairedDevices();
                    Intent connectIntent = new Intent(getContext(),decive_list.class);
                    startActivityForResult(connectIntent,REQUEST_CONNECT_DEVICE);
                }else {
                    Toast.makeText(getContext(),"Message",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    private void ListPairedDevices(){
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if(mPairedDevices.size() > 0){
            for(BluetoothDevice mDevice : mPairedDevices){
                Log.v(TAG,"PairedDevices: " + mDevice.getName() + " " + mDevice.getAddress());
            }
        }
    }
    @Override
    public void run() {
        try{
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
            Log.e("hsuihs",mBluetoothSocket.toString());
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mHandler.sendEmptyMessage(0);

        }catch (IOException eConnectException){
            Log.d(TAG,"CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);
            return;
        }
    }
    private void closeSocket(BluetoothSocket nOpenSocket){
        try{
            nOpenSocket.close();
            Log.d(TAG,"SocketClosed");
        }catch (IOException ex){
            Log.d(TAG, "CouldNotCloseSocket");
        }
    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            mBluetoothConnectProgressDialog.dismiss();
            Toast.makeText(getContext(),"Dispositivo Conectado",Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPref = getContext().getSharedPreferences("bluetooth_info", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("mac_bluetooth", mBluetoothDevice.getAddress());
            editor.apply();
            textEstadoBluetooth.setText("Conectado");


        }
    };

}