package com.android.oobe;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class Utils {

    public static final int PADDING = 94;
    public static final String REGION_ID = "region_id";
    public static final Boolean IS_SELECTED = true;
    public static final Boolean IS_INITIATED = true;

    public static void setFragmentPadding(View view, int left, int top, int right, int bottom) {
        Context context = view.getContext();
        float density = context.getResources().getDisplayMetrics().density;
        // in pixels
        int paddingLeft = Math.round(left * density);
        int paddingTop = Math.round(top * density);
        int paddingRight = Math.round(right * density);
        int paddingBottom = Math.round(bottom * density);
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }


    public static String getFileMD5(File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        int numRead = 0;
        while ((numRead = fis.read(buffer)) > 0) {
            md5.update(buffer, 0, numRead);
        }
        fis.close();
        byte[] md5Bytes = md5.digest();

        // 将字节数组转换为十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < md5Bytes.length; i++) {
            String hex = Integer.toHexString((int) (0xFF & md5Bytes[i]));
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }

        return sb.toString();
    }


    // 将 Base64 字符串解码为 Bitmap
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;
        // 将 Base64 字符串解码为字节数组
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        // 使用 BitmapFactory 将字节数组解码为 Bitmap
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }


    public static boolean isChineseLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.equals("zh");
    }

    public static int ToInt(Object ojb) {
        if (ojb == null) {
            return 0;
        } else {
            try {
                return ToDouble(ojb).intValue();
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    public static Double ToDouble(Object ojb) {
        if (ojb == null) {
            return 0.0;
        } else {
            try {
                return Double.valueOf(ToString(ojb));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }

    public static String ToString(Object ojb) {
        if (ojb == null) {
            return "";
        } else {
            return String.valueOf(ojb).trim();
        }
    }
}