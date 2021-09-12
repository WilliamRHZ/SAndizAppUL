package com.example.distrisandi.network.model;

import com.google.gson.annotations.SerializedName;

public class ProductoVendidoDetalle {
    @SerializedName("json_array")
    private String jsonArray;

    @SerializedName("id_enterprise")
    private String idEnterprise;

    @SerializedName("route")
    private String route;

    public ProductoVendidoDetalle(String jsonArray, String idEnterprise, String route) {
        this.jsonArray = jsonArray;
        this.idEnterprise = idEnterprise;
        this.route = route;
    }

    public ProductoVendidoDetalle() {
    }

    public String getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(String jsonArray) {
        this.jsonArray = jsonArray;
    }

    public String getIdEnterprise() {
        return idEnterprise;
    }

    public void setIdEnterprise(String idEnterprise) {
        this.idEnterprise = idEnterprise;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}
