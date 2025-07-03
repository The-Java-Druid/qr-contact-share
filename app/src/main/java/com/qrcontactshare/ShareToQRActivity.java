package com.qrcontactshare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.InputStream;

public class ShareToQRActivity extends AppCompatActivity {

    private static final float DEFAULT_BRIGHTNESS = -1f;
    private static final String TAG = "ShareToQRActivity";
    private static final QRCodeWriter qrCodeWriter = new QRCodeWriter();
    private float originalBrightness = DEFAULT_BRIGHTNESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge layout for Android 15+
        EdgeToEdge.enable(this);
        new Thread(this::initializeAds).start();
        setContentView(R.layout.activity_share_to_qr);
        final ImageView qrImageView = findViewById(R.id.qrImageView);
        final Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/x-vcard".equals(intent.getType())) {
            final Uri vcardUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (vcardUri != null) {
                try (final InputStream inputStream = getContentResolver().openInputStream(vcardUri)){
                    qrImageView.setImageBitmap(generateQRCode(new VCardParser(inputStream)));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to read contact", e);
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

    private static Bitmap generateQRCode(VCardParser parser) {
        try {
            return toBitmap(qrCodeWriter.encode(parser.getText(), BarcodeFormat.QR_CODE, 800, 800));
        } catch (WriterException e) {
            Log.e(TAG, "Failed to generate QR code", e);
            throw new RuntimeException(e);
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

    private void initializeAds() {
        MobileAds.initialize(this, ShareToQRActivity::onAddInitialized);
        runOnUiThread(this::loadAdd);
    }

    private void loadAdd() {
        ((AdView)findViewById(R.id.adView))
                .loadAd(new AdRequest.Builder().build());
    }

    private static void onAddInitialized(InitializationStatus initializationStatus) {
        Log.i(TAG, "Initialized ad manager with status: " + initializationStatus.getAdapterStatusMap());
    }

}