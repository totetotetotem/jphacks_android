package com.fresh_fridge.uthackers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class QRCodeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_qr_code);
        ImageView qrImage = (ImageView) findViewById(R.id.qrImageView);

        Intent intent = getIntent();
        String familyId = intent.getStringExtra("familyToken");


        if (qrImage != null) {
            qrImage.setImageBitmap(createQRCode(familyId, 420, 420));
        }
    }

    private Bitmap createQRCode(String content, int width, int height) {
        Bitmap bitmap = null;
        try {
            BarcodeFormat format = BarcodeFormat.QR_CODE;

            Hashtable<EncodeHintType, ErrorCorrectionLevel> hints = new Hashtable<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, format, width, height, hints);

            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}

