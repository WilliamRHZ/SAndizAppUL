package com.example.distrisandi;

public class Clientes {
    private String nombre;
    private String apellido_paterno;
    private String apellido_materno;
    private String ruta;
    private String cliente;

    private String getNombre(){
        return nombre;
    }
    public void setNombre(String nombre){
        this.nombre = nombre;
    }
    public String getApellido_paterno(){
        return apellido_paterno;
    }
    public void setApellido_paterno(String apellido_paterno){
        this.apellido_paterno = apellido_paterno;
    }
    public String getApellido_materno(){
        return apellido_materno;
    }
    public void setApellido_materno(String apellido_materno){
        this.apellido_materno = apellido_materno;
    }
    public String getRuta(){
        return ruta;
    }
    public void setRuta(String ruta){
        this.ruta = ruta;
    }
}