package com.example.distrisandi;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AdminSQLiteOpenHelper  extends SQLiteOpenHelper {
    private static AdminSQLiteOpenHelper mInstance = null;

    public AdminSQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static AdminSQLiteOpenHelper getInstance(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version){
        if(mInstance == null)
            mInstance = new AdminSQLiteOpenHelper(context, name, factory, version);
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table venta_cliente(folio text, total text, id_cliente text, tipo_operacion text , estado text, estado_operacion text,importe text,cancelado text, postActualizacionRegistro text, unique(folio))");
        db.execSQL("create table venta_detalles(folio text, codigo_producto text, cantidad_vendido text, peso_producto text,precio_compra text,precio_real text, precio_venta text)");
        db.execSQL("create table detalles_productos(id_producto text primary key, codigo_producto text, key_producto text ,nombre_producto text, stock_producto text,precio_venta_producto text, unique(id_producto) ON CONFLICT REPLACE)");
       // db.execSQL("Create table gastos_vendedor()");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
