package com.moviles2.proyectomov2_deepbluemarket.utils;

import android.content.Context;
import android.os.Environment;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import com.moviles2.proyectomov2_deepbluemarket.models.Producto;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportGenerator {

    public static File generarReporteProductos(
            Context context,
            List<Producto> productos
    ) throws Exception {

        File carpeta = context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS
        );

        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }

        String fecha = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(new Date());

        File pdfFile = new File(
                carpeta,
                "ReporteProductos_" + fecha + ".pdf"
        );

        PdfWriter writer = new PdfWriter(pdfFile);

        PdfDocument pdfDocument = new PdfDocument(writer);

        Document document = new Document(pdfDocument);

        document.add(new Paragraph("DEEP BLUE MARKET"));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Reporte de Productos Publicados"));
        document.add(new Paragraph("Fecha: " + new Date()));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("------------------------------------"));

        double totalInventario = 0;

        for (Producto p : productos) {

            totalInventario += p.getPrecio();

            document.add(
                    new Paragraph(
                            "ID: " + p.getId()
                    )
            );

            document.add(
                    new Paragraph(
                            "Título: " + p.getTitulo()
                    )
            );

            document.add(
                    new Paragraph(
                            "Categoría: " + p.getCategoria()
                    )
            );

            document.add(
                    new Paragraph(
                            "Precio: Bs. " + p.getPrecio()
                    )
            );

            document.add(
                    new Paragraph(
                            "Estado: " + p.getEstado()
                    )
            );

            document.add(
                    new Paragraph(
                            "Descripción: " + p.getDescripcion()
                    )
            );

            document.add(
                    new Paragraph(
                            "------------------------------------"
                    )
            );
        }

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Cantidad de productos: " + productos.size()));
        document.add(new Paragraph("Valor total publicado: Bs. " + totalInventario));

        document.close();

        return pdfFile;
    }
}