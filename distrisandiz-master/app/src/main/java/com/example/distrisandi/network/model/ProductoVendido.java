package com.example.distrisandi.network.model;

import com.google.gson.annotations.SerializedName;

public class ProductoVendido {
    @SerializedName("id_cliente")
    private String idCliente;

    @SerializedName("id_tipoOperacion")
    private String idTipoOperacion;

    @SerializedName("id_estadoOperacion")
    private String idEstadoOperacion;

    @SerializedName("id_caja")
    private String idCaja;

    @SerializedName("id_usuario")
    private String idUsuario;

    @SerializedName("fldFechaVentaProducto")
    private String fldFechaVentaProducto;

    @SerializedName("fldRegistrarFecha")
    private String fldRegistrarFecha;

    @SerializedName("fldCancelado")
    private String fldCancelado;

    @SerializedName("detalles")
    private String detalles;

    public ProductoVendido() {
    }

    public ProductoVendido(String idCliente, String idTipoOperacion, String idEstadoOperacion, String idCaja, String idUsuario, String fldFechaVentaProducto, String fldRegistrarFecha, String fldCancelado, String detalles) {
        this.idCliente = idCliente;
        this.idTipoOperacion = idTipoOperacion;
        this.idEstadoOperacion = idEstadoOperacion;
        this.idCaja = idCaja;
        this.idUsuario = idUsuario;
        this.fldFechaVentaProducto = fldFechaVentaProducto;
        this.fldRegistrarFecha = fldRegistrarFecha;
        this.fldCancelado = fldCancelado;
        this.detalles = detalles;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdTipoOperacion() {
        return idTipoOperacion;
    }

    public void setIdTipoOperacion(String idTipoOperacion) {
        this.idTipoOperacion = idTipoOperacion;
    }

    public String getIdEstadoOperacion() {
        return idEstadoOperacion;
    }

    public void setIdEstadoOperacion(String idEstadoOperacion) {
        this.idEstadoOperacion = idEstadoOperacion;
    }

    public String getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(String idCaja) {
        this.idCaja = idCaja;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getFldFechaVentaProducto() {
        return fldFechaVentaProducto;
    }

    public void setFldFechaVentaProducto(String fldFechaVentaProducto) {
        this.fldFechaVentaProducto = fldFechaVentaProducto;
    }

    public String getFldRegistrarFecha() {
        return fldRegistrarFecha;
    }

    public void setFldRegistrarFecha(String fldRegistrarFecha) {
        this.fldRegistrarFecha = fldRegistrarFecha;
    }

    public String getFldCancelado() {
        return fldCancelado;
    }

    public void setFldCancelado(String fldCancelado) {
        this.fldCancelado = fldCancelado;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }
}
