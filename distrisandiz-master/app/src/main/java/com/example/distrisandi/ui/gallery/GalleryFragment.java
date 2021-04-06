package com.example.distrisandi.ui.gallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.distrisandi.JSONParser;
import com.example.distrisandi.R;
import com.example.distrisandi.Sesion_Usuario;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    //-------------VARIABLES----------------//
    //VARIABLES PARA LOS EDIT TEXT DEL CAMPO CAMBIAR NOMBRE
    EditText editNombre, editApellidoPaterno, editApellidoMaterno;
    //VARIABLES PARA TXT CORREO ELECTRONICO Y TIPO USUARIO EN EL CAMPO DATOS CUENTA
    TextView txtCoreoElectronico;
    TextView txtTipo_Usuario;
    //BOTON CONTRASENIA
    LinearLayout botonGuardar;
    LinearLayout botonContrasenia;
    //URL´S PARA COMUNICAR CON PHP
    String URL = "https://sandiz.com.mx/failisa/WebService/cambio_datos_usuarios.php";
   String URL_Contrasenia = "https://sandiz.com.mx/failisa/WebService/cambio_contrasenia_usuario.php";

   //  String URL = "https://192.168.0.9/sandizsistema/WebService/v2/cambio_datos_usuarios.php";
    //String URL_Contrasenia = "https://192.168.0.9/sandizsistema/WebService/v2/cambio_contrasenia_usuario.php";

    JSONParser jsonParser = new JSONParser();
    //VARIABLE PARA SHAREPREFERENCE
    private SharedPreferences sharedPref;
    //VARIABLE GLOBAL PARA NOMBRE_USUARIO
    private String valor_nombre;
    //--------------------------------------------------//
    private GalleryViewModel galleryViewModel;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        //-----------INSTANCIA DE OBJETOS--------------//
        editNombre = (EditText)root.findViewById(R.id.txtNombre);
        editApellidoPaterno = (EditText)root.findViewById(R.id.txtApellidoPaterno);
        editApellidoMaterno = (EditText)root.findViewById(R.id.txtApellidoMaterno);
        txtCoreoElectronico = (TextView)root.findViewById(R.id.txtCoreoElectronico);
        txtTipo_Usuario = (TextView)root.findViewById(R.id.txtRolUsuario);
        botonGuardar = root.findViewById(R.id.btnGuardar);
        botonContrasenia = root.findViewById(R.id.btnContrasenia);
        //--------------------------------------------//

        //OBTENER DATOS GUARDADO EN SHAREPREFERENCE//
        sharedPref = this.getActivity().getSharedPreferences("login_preference", Context.MODE_PRIVATE);
        valor_nombre = sharedPref.getString("username", "");
        //INSERTAR DATOS EN TXTCORREOELECTRONICO//
        txtCoreoElectronico.setText(valor_nombre);
        txtTipo_Usuario.setText("Vendedor");
        //CLICK EN BOTON GUARDAR
        botonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //OBTENER STRINGS EN LOS CAMPOS DE TEXTO
                String datosNombre = editNombre.getText().toString();
                String datosApellidoPaterno = editApellidoPaterno.getText().toString();
                String datosApellidoMaterno = editApellidoMaterno.getText().toString();
                //VALIDAR SI NO ESTAN VACIOS
                if(datosNombre.length()<=0 || datosApellidoPaterno.length()<=0 || datosApellidoMaterno.length()<=0){
                    Toast.makeText(getActivity(),"FALTAN DATOS",Toast.LENGTH_SHORT).show();
                }else {
                    //EJECUTAR ENVIO DE DATOS
                    AttempCambioDatos attempCambioDatos = new  AttempCambioDatos();
                    attempCambioDatos.execute(valor_nombre, datosNombre,datosApellidoPaterno,datosApellidoMaterno,"");
                }

            }
        });
        botonContrasenia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCuadroDialogoContrasenia().show();

            }
        });
        return root;
    }
    //CLASE PARA ENVIO DE DATOS
    private class AttempCambioDatos extends AsyncTask<String, String, JSONObject>{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String ApellidoMaterno = args[3];
            String ApellidoPaterno = args[2];
            String nombre = args[1];
            String id_user = args[0];

            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("id_user",id_user));
            params.add(new BasicNameValuePair("nombre",nombre));
            params.add(new BasicNameValuePair("apellido_paterno", ApellidoPaterno));
            params.add(new BasicNameValuePair("apellido_materno", ApellidoMaterno));
            JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
            return json;
        }
        protected void onPostExecute(JSONObject result){
            try{
                if(result != null){
                    String mensaje = result.getString("message");
                    if(mensaje.equals("exito")){
                        //editNombre.setText("");
                        //editApellidoPaterno.setText("");
                        //editApellidoMaterno.setText("");
                        Toast.makeText(getActivity(),"GUARDADO CORRECTAMENTE", Toast.LENGTH_SHORT).show();
                        createSimpleDialog().show();
                    }else {
                        Toast.makeText(getActivity(),"datos no cambiados",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getActivity(),"no conectado con el servdor", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    //CUADRO DE DIALOGO
    public AlertDialog createSimpleDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Cambio de Datos")
                .setMessage("Para visualizar los cambios, es necesario iniciar sesion nuevamente")
                .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createSimpleDialog().dismiss();
                        editNombre.setText("");
                        editApellidoPaterno.setText("");
                        editApellidoMaterno.setText("");

                    }
                });
        return builder.create();
    }
    //CUADRO DIALOGO CAMBIO CONTRASENIA
    public  AlertDialog createCuadroDialogoContrasenia(){
        final AlertDialog alertDialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.cambio_contrasenia_usuario, null);
        final  EditText editContrasenia = (EditText)view.findViewById(R.id.txtContraseniaAnterior);
        final  EditText editContraseniaNueva = (EditText)view.findViewById(R.id.txtNuevaContrasenia);
        final  EditText editConfirmarContrasenia = (EditText)view.findViewById(R.id.txtConfirmarContrasenia);
        Button btnEnviar = (Button)view.findViewById(R.id.btnModificar);
        Button btnCancelar = (Button)view.findViewById(R.id.btnCancelar);
        builder.setView(view);
        alertDialog = builder.create();
        /*builder.setTitle("Modificar Contraseña");
        final EditText contrasenia_anterior = new EditText(getActivity());
        final EditText contrasenia_nueva = new EditText(getActivity());
        final EditText confirmar_contrasenia = new EditText(getActivity());
        contrasenia_anterior.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        contrasenia_nueva.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmar_contrasenia.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(contrasenia_anterior);
        builder.setView(contrasenia_nueva);
        builder.setView(contrasenia_nueva);
        builder.setPositiveButton("CAMBIAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String viewcontrasenia_anterior = contrasenia_anterior.getText().toString();
                Toast.makeText(getActivity(),viewcontrasenia_anterior,Toast.LENGTH_SHORT).show();

            }
        });*/
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valor_nombre = sharedPref.getString("password", "");
                //Toast.makeText(getActivity(),valor_nombre,Toast.LENGTH_SHORT).show();
                String valorContraseniaActual = editContrasenia.getText().toString();
                String valorContraseniaNueva = editContraseniaNueva.getText().toString();
                String valorConfirmar = editConfirmarContrasenia.getText().toString();
                if(valorContraseniaActual.length()<=0 | valorContraseniaNueva.length()<=0 | valorConfirmar.length()<=0){
                    Toast.makeText(getContext(),"FALTAN DATOS" ,Toast.LENGTH_SHORT ).show();
                } else{
                        if(valor_nombre.equals(valorContraseniaActual)){
                            if(valor_nombre.equals(valorContraseniaNueva)){
                                Toast.makeText(getContext(),"LA CONTRASEÑA NUEVA DEVE DE SER DIFERENTE QUE LA ANTERIOR",Toast.LENGTH_SHORT).show();
                            }else {
                                if(valorContraseniaNueva.equals(valorConfirmar)){
                                    //Toast.makeText(getContext(),"CONTRASEÑA CAMBIADA",Toast.LENGTH_SHORT).show();
                                    valor_nombre = sharedPref.getString("username", "");
                                    AttempCambioContrasenia attempCambioContrasenia = new  AttempCambioContrasenia();
                                    attempCambioContrasenia.execute(valor_nombre, valorConfirmar,"");

                                }else{
                                    Toast.makeText(getContext(),"REVISAR CONTRASEÑA NUEVA", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }else{
                            Toast.makeText(getContext(),"REVISAR CONTRASEÑA ACTUAL", Toast.LENGTH_SHORT).show();
                        }
                }
                /*
                else if (valorConfirmar.equals(valor_nombre)){
                    Toast.makeText(getContext(),"LA CONTRASEÑA NUEVA DEBE DE SER DIFERENTE QUE LA ANTERIOR",Toast.LENGTH_SHORT).show();
                }else if(valorContraseniaNueva != valorConfirmar){
                    Toast.makeText(getContext(),"REVISAR CONTRASEÑA",Toast.LENGTH_SHORT).show();
                }else if(valorContraseniaNueva.equals(valorConfirmar)){
                    Toast.makeText(getContext(),"CONTRASEÑA ENVIADA",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(),"REVISAR CONTRASEÑA",Toast.LENGTH_SHORT).show();
                }*/


            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        return alertDialog;
    }
    private class AttempCambioContrasenia extends AsyncTask<String, String, JSONObject>{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            String valorConfirmar = args[1];
            String id_user = args[0];

            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("id_user",id_user));
            params.add(new BasicNameValuePair("contrasenia",valorConfirmar));
            JSONObject json = jsonParser.makeHttpRequest(URL_Contrasenia, "POST", params);
            return json;
        }
        protected void onPostExecute(JSONObject result){
            try{
                if(result != null){
                    String mensaje = result.getString("message");
                    if(mensaje.equals("exito")){
                        //editNombre.setText("");
                        //editApellidoPaterno.setText("");
                        //editApellidoMaterno.setText("");
                        Toast.makeText(getActivity(),"CONTRASEÑA CAMBIADA", Toast.LENGTH_SHORT).show();
                        //createSimpleDialog().show();
                    }else {
                        Toast.makeText(getActivity(),"datos no cambiados",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getActivity(),"no conectado con el servdor", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}