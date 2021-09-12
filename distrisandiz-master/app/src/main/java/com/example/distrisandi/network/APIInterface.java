package com.example.distrisandi.network;

import com.example.distrisandi.network.model.ProductoVendido;
import com.example.distrisandi.network.model.ProductoVendidoDetalle;
import com.example.distrisandi.network.model.Response;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIInterface {

    @FormUrlEncoded
    @POST("productos_vendidos.php")
    Call<String> productosVendidos(
            @Field("id_cliente")String idCliente,
            @Field("id_tipoOperacion")String idTipoOperacion,
            @Field("id_estadoOperacion")String idEstadoOperacion,
            @Field("id_caja")String idCaja,
            @Field("id_usuario")String idUsuario,
            @Field("fldFechaVentaProducto")String fldFechaVentaProducto,
            @Field("fldRegistrarFecha")String fldRegistrarFecha,
            @Field("fldCancelado")String fldCancelado,
            @Field("detalles")String detalles
    );

    @FormUrlEncoded
    @POST("productos_vendidos_detalles.php")
    Call<String> productosVendidosDetalles(@Field("json_array")String jsonArray, @Field("id_enterprise")String idEnteprise, @Field("route")String rutaCliente);

    @FormUrlEncoded
    @POST("index.php")
    Call<String> login(@Field("username")String username, @Field("password")String password);

    @FormUrlEncoded
    @POST("datos_usuario.php")
    Call<String> datosUsuario(@Field("username")String username);


    @FormUrlEncoded
    @POST("lista_clientes_usuario.php")
    Call<String> listaClientesUsuario(@Field("username")String username);

    @FormUrlEncoded
    @POST("lista_productos.php")
    Call<String> listaProductos(@Field("route") String rutaCliente);

    @FormUrlEncoded
    @POST("lista_productos_1.php")
    Call<String> listaProductos1(@Field("route") String route, @Field("limite") int limite, @Field("user") String username);

    @FormUrlEncoded
    @POST("lista_clientes_usuario_1.php")
    Call<String> listaClientesUsuario1(@Field("username")String correo, @Field("contador")int contador);
}
