package com.droideve.apps.nearbystores.utils;

import static android.content.Context.WINDOW_SERVICE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRCodeUtil {

    private Context ctx;
    private QRGEncoder qrgEncoder;
    private String text;

    public QRCodeUtil(Context ctx) {
        this.ctx = ctx;
    }

    public static Bitmap generate(Context ctx, String text){
        QRCodeUtil qrc = new QRCodeUtil(ctx);
        return  qrc.generateQRCode(text);
    }

    private Bitmap generateQRCode(String data){

        // below line is for getting
        // the windowmanager service.
        WindowManager manager = (WindowManager) ctx.getSystemService(WINDOW_SERVICE);

        // initializing a variable for default display.
        Display display = manager.getDefaultDisplay();

        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();
        display.getSize(point);

        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(data, null, QRGContents.Type.TEXT, dimen);

        try {
            // getting our qrcode in the form of bitmap.
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            // the bitmap is set inside our image
            // view using .setimagebitmap method.
            return bitmap;
        } catch (WriterException e) {
            // this method is called for
            // exception handling.
           return  null;
        }
    }
}
