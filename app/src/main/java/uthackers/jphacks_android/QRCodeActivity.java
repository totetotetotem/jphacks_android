package uthackers.jphacks_android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * Created by totetotetotem on 2016/10/28.
 */

public class QRCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_qr_code);
        ImageView qrImage = (ImageView) findViewById(R.id.qrImageView);

        Intent intent = getIntent();
        String familyId = intent.getStringExtra("familyId");
        //TODO あくせすとーくんとかの処理をかく

        try {
            qrImage.setImageBitmap(createQRCode("freshfridge://freshfridge.com/openwith?familyId" + familyId, 177, 177));
        } catch (NullPointerException e) {
            e.printStackTrace();
            finish();
        }
    }

    private Bitmap createQRCode(String content, int width, int height) {
        Bitmap bitmap = null;
        try {
            BarcodeFormat format = BarcodeFormat.QR_CODE;

            Hashtable hints = new Hashtable();
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

