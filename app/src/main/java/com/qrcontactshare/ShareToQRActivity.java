package com.qrcontactshare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.InputStream;

public class ShareToQRActivity extends AppCompatActivity {

    private static final float DEFAULT_BRIGHTNESS = -1f;
    private ImageView qrImageView;
    private float originalBrightness = DEFAULT_BRIGHTNESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_to_qr);

        qrImageView = findViewById(R.id.qrImageView);

        final Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/x-vcard".equals(intent.getType())) {
            final Uri vcardUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (vcardUri != null) {
                try (final InputStream inputStream = getContentResolver().openInputStream(vcardUri)){
                    generateQRCode(new VCardParser(inputStream));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to read contact", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        increaseScreenBrightness();
    }

    @Override
    protected void onPause() {
        super.onPause();
        restoreScreenBrightness();
    }

    private void generateQRCode(VCardParser parser) {
        final QRCodeWriter writer = new QRCodeWriter();
        try {
            final Bitmap bitmap = toBitmap(writer.encode(parser.getText(), BarcodeFormat.QR_CODE, 800, 800));
            qrImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "QR generation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private static Bitmap toBitmap(BitMatrix matrix) {
        final int width = matrix.getWidth();
        final int height = matrix.getHeight();
        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                bmp.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
        return bmp;
    }

    private void increaseScreenBrightness() {
        final Window window = getWindow();
        final WindowManager.LayoutParams layoutParams = window.getAttributes();
        originalBrightness = layoutParams.screenBrightness;
        layoutParams.screenBrightness = 1f;
        window.setAttributes(layoutParams);
    }

    private void restoreScreenBrightness() {
        if (originalBrightness != DEFAULT_BRIGHTNESS) {
            final Window window = getWindow();
            final WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = originalBrightness;
            window.setAttributes(layoutParams);
        }
    }
}