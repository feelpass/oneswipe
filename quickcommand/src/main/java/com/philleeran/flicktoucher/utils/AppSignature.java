package com.philleeran.flicktoucher.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class AppSignature {

    private static final String PUBKEY_DYNAMIC_G = "6d683ae1534a2f36001dc342ecbf51614e654f1a";

    public static String get(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = null;

        try {
            packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        Signature[] signatures = packageInfo.signatures;

        byte[] cert = signatures[0].toByteArray();

        InputStream input = new ByteArrayInputStream(cert);

        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        X509Certificate c = null;
        try {
            c = (X509Certificate) cf.generateCertificate(input);
        } catch (CertificateException e) {
            return e.toString();
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(c.getPublicKey().getEncoded());

            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<publicKey.length;i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i]);
                if(appendString.length()==1)hexString.append("0");
                hexString.append(appendString);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e1) {
            return e1.toString();
        }
    }

    /*
     * backup/restore over google drive only works if the app is compiled with the DynamicG certificate
     * (permission com.dynamicg.timerec.plugin3.ACCESS with android:protectionLevel="signature")
     * hence we hide these items on other builds (f-droid)
     */
    public static boolean isMatchingCertificate(Context context) {
        return PUBKEY_DYNAMIC_G.equals(AppSignature.get(context));
    }
}