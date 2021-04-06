package com.example.distrisandi;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.xmp.impl.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class TemplatePDF {


    //constructor
    private Context context;
    private File pdfFile;
    private Document document;
    private PdfWriter pdfWriter;
    private Paragraph paragraph;
    private Font fTitle = new Font(Font.FontFamily.COURIER ,20,Font.BOLD);
    private Font fSubtitle = new Font(Font.FontFamily.COURIER,18,Font.NORMAL);
    private Font fText = new Font(Font.FontFamily.COURIER,12,Font.BOLD);
    private Font fHighText = new Font(Font.FontFamily.COURIER,15,Font.NORMAL, BaseColor.BLACK);

    private String folio_venta;

    public TemplatePDF(Context context) {
        this.context = context;

    }

    public void openDocument(){
        createFile();
        try{

            document = new Document(PageSize.A4);
            pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
        }catch (Exception e){
            Log.e("openDocument",e.toString());
        }
    }
    //crear archivo PDF en el dispositivo

    private void createFile(){
        //OBTENER STRING EN CLIPDATA

        SharedPreferences sharedPreferences = context.getSharedPreferences("folio_venta_actual",MODE_PRIVATE);
        folio_venta = sharedPreferences.getString("folio_venta","");
        Log.e("valor",folio_venta);

        int numero_aleatorio = (int)(Math.random()*99999)+1;
        String numero = String.valueOf(numero_aleatorio);
        String numero1 = "_";
        String numero3 = folio_venta+numero1+numero;

            File folder = new File(context.getFilesDir(),numero3);
            if (!folder.exists()) {
                folder.mkdirs();

                pdfFile = new File(folder, "PDF1.pdf");

        }

    }
    public void colseDocument(){
        document.close();
    }
    public void addMetaData(String title, String subject, String autor){
        document.addTitle(title);
        document.addSubject(subject);
        document.addAuthor(autor);
    }
    public  void addTitles(String title, String subTitle, String date){
        try {
            paragraph = new Paragraph();
            addChild(new Paragraph(title, fTitle));
            addChild(new Paragraph(subTitle, fSubtitle));
            addChild(new Paragraph("Fecha:" + date, fHighText));
            paragraph.setSpacingAfter(30);
            document.add(paragraph);
        }catch (Exception e){
            Log.e("addTitles",e.toString());
        }

    }
    private void addChild(Paragraph childParagraph){
        childParagraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(childParagraph);
    }
    public void addParagraph(String text){
        try {
            paragraph = new Paragraph(text, fText);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.setSpacingAfter(5);
            paragraph.setSpacingBefore(5);
            document.add(paragraph);
        }
        catch (Exception e){
            Log.e("addParagraph",e.toString());
        }

    }
    public  void  createTable(String[]header, ArrayList<String[]>clients){
        try {

            paragraph = new Paragraph();
            paragraph.setFont(fText);
            PdfPTable pdfPTable = new PdfPTable(header.length);
            pdfPTable.setWidthPercentage(100);
            PdfPCell pdfPCell;
            int indexC = 0;
            while (indexC < header.length) {
                pdfPCell = new PdfPCell(new Phrase(header[indexC++], fSubtitle));
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(BaseColor.WHITE);
                pdfPCell.setBorder(0);
                pdfPTable.addCell(pdfPCell);
            }
            for (int indexR = 0; indexR < clients.size(); indexR++) {
                String[] row = clients.get(indexR);
                for (indexC = 0; indexC < header.length; indexC++) {
                    pdfPCell = new PdfPCell(new Phrase(row[indexC]));
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBorder(0);
                    pdfPCell.setFixedHeight(40);
                    pdfPTable.addCell(pdfPCell);

                }
            }
            paragraph.add(pdfPTable);
            document.add(paragraph);
        }catch (Exception e)
        {
            Log.e("addParagraph",e.toString());
        }
    }

    public void viewPDF() {
        Intent intent = new Intent(context,ViewPDF.class);
        intent.putExtra("path",pdfFile.getAbsolutePath());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void appViewPDF(Activity activity){/*
        if(pdfFile.exists()){
            //Uri uri=Uri.fromFile(pdfFile);
            String archivo = pdfFile.toString();
            String archivo2 = Environment.getExternalStorageDirectory()+ "/com.example.distrisandi/files/"+folio_venta+"/PDF1";
            Log.e("directorio",archivo2);
            File arch = new File(archivo);
            //Log.e("directorio",arch.toString());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(arch),"application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            try{
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());
                activity.startActivity(intent);
            }catch(ActivityNotFoundException e){
                activity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.adobe.reader")));
                Toast.makeText(activity.getApplicationContext(),"No cuenta con una aplicacion para visualizar PDF",Toast.LENGTH_SHORT).show();


            }
        }else {
            Toast.makeText(activity.getApplicationContext(),"No se econtrole l archivo",Toast.LENGTH_SHORT).show();
        }*/
        String s= String.valueOf(pdfFile);
        File arch = new File(s);
        if (arch.exists()) {
            Uri uri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", arch);
            Intent intent = new Intent(Intent.ACTION_VIEW );
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
               // Utils.showSnackBar(root.getResources().getString(R.string.error_pdf), root);
                activity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.adobe.reader")));
            }
    }}
}
