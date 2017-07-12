package com.company.alex.privategallery;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.company.alex.privategallery.utils.FingerprintHandler;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FingerprintActivity extends AppCompatActivity{

    private static final int REQUEST_CODE = 101;

    //Contenedor donde se guarda la huella
    private KeyStore keyStore;
    //Identificador para saber a qué huella nos referimos
    private static final String KEY_NAME = "alexFingerPrint";
    //Objeto que lo cifra
    private Cipher cipher;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // si tendria que preguntar o no una vez lo rechazas

            //Creamos el AlertDialog en una vez se ha rechazado para poder pedir el acceso siempre que queramos
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Esta app requiere permiso para acceder al almacenamiento").setTitle("Permiso Requerido");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.i("data", "Apretado");
                    checkPermissions();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            checkPermissions();
        }
        //checkPermissions();

        //Keyguard Manager & Fingerprint Manager
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        textView = (TextView) findViewById(R.id.errorText);

        //Comprobar si tiene sensor de huellas. Si no tiene mensaje de error, aunque lo ideal sería pasar a otra Activity
        if (!fingerprintManager.isHardwareDetected())
            textView.setText(R.string.error_message);
        //TODO arreglar si no dispone de sensor de huellas
        else{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                //El dispositivo si tiene sensor, pero no permiso otorgado
                textView.setText(R.string.error_permission);
            } else {
                //Comprobar si tiene alguna huella registrada
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    textView.setText(R.string.error_registro_huella);
                } else {
                    //Comprobar si tiene seguridad en la pantalla bloqueada
                    if (!keyguardManager.isKeyguardSecure()) {
                        textView.setText(R.string.error_pantalla_bloqueo);
                    } else {
                        generateKey();
                        if(cipherInit()) {
                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            FingerprintHandler helper = new FingerprintHandler(this);
                            helper.startAuth(fingerprintManager, cryptoObject);
                        }
                    }
                }
            }
        }
    }

    void checkPermissions() {
        if(hasStoragePermission()) {
            //TODO intent de lo que sea
        } else
            requestStoragePermissions();
    }
    public boolean hasStoragePermission() {
        int writePermissionCheck = ContextCompat.checkSelfPermission( this, "android.permission.WRITE_EXTERNAL_STORAGE");
        int readPermissionCheck = ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE");
        return Build.VERSION.SDK_INT < 23 || writePermissionCheck != -1 && readPermissionCheck != -1;
    }


    public void requestStoragePermissions() {
        int writePermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        ArrayList permissions = new ArrayList();

        if (writePermissions != 0) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readPermissions != 0) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, (String[])permissions.toArray(new String[permissions.size()]), REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("data", "Permiso denegado por el usuario");
                    //TODO arreglar el bucle de si sale don't ask again
                }
                else
                    Log.i("data", "Permiso concedido por el usuario");
                return;

        }    }

    protected void generateKey() { //Estamos creando una clave que solo nuestra huella puede descifrar. TODO APUNTE PARA APP

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        KeyGenerator keyGenerator; //Clave para cifrar el contenido de la app
        try {
            //aLGORITMO PARA PDOER CIFRAR LA CLAVE. AES ES DE LOS MAS SEGUROS
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Error al generar una clave", e);
        }

        try {
            keyStore.load(null);
            /**
             * Primero le pasas una clave (como si fuera una clave pública)
             * Luego cual es el proposito
             * Le decimos como queremos los bloques. Si utilizas un block_mode solo se puede desencriptar usando ese block mode
             */

            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean cipherInit() { //Para encriptar
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES //Cogemos la clave a traves del alia y nos ponemos en modo encriptacion
                    + "/" + KeyProperties.BLOCK_MODE_CBC
                    + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Error al acceder al Cipher Object");
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error al inicializar cipher", e);
        }
    }
}
