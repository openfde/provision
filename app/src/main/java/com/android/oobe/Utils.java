package com.android.oobe;


import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.android.internal.util.CompatibleConfig;
import com.android.oobe.application.model.RegionInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

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

    public static void parseGpsData(Context context) {
        try {
            Log.i("bella","parseGpsData......start");
            InputStream inputStream = context.getResources().openRawResource(R.raw.gps);
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            String jsonString = scanner.hasNext() ? scanner.next() : "";

            JSONArray chinaData = new JSONArray(jsonString);
//            Uri uri = Uri.parse(Constant.REGION_URI + "/REGION_INFO");
//            context.getContentResolver().delete(uri, null, null);
            BaseDataBase.getInstance(context).regionDao().deleteAll();
            int index = 0;
            for (int i = 0; i < chinaData.length(); i++) {
                JSONObject china = chinaData.getJSONObject(i);
                String countryId = "C_00" + i;
                String countryName = china.getJSONArray("name").getString(0);
                String countryEnName = china.getJSONArray("name").getString(1);
                JSONArray provinces = china.getJSONArray("provinces");
                for (int j = 0; j < provinces.length(); j++) {
                    JSONObject province = provinces.getJSONObject(j);
                    String provinceId = "P_00" + i + "00" + j;
                    String provinceName = province.getJSONArray("name").getString(0); // Get the province name
                    String provinceEnName = province.getJSONArray("name").getString(1);
                    JSONArray cities = province.getJSONArray("cities");
                    for (int k = 0; k < cities.length(); k++) {
                        JSONObject city = cities.getJSONObject(k);
                        String cityName = city.getJSONArray("name").getString(0); // Get the city name
                        String cityEnName = city.getJSONArray("name").getString(1);
                        String gpsCoordinates = city.getString("gps"); // Get the GPS coordinates
                        String cityId = "CI_00" + i + "00" + j + "00" + k;


                        RegionInfo regionInfo = new RegionInfo();
                        regionInfo.setCountryId(countryId);
                        regionInfo.setCountryName(countryName);
                        regionInfo.setCountryNameEn(countryEnName);

                        regionInfo.setProvinceId(provinceId);
                        regionInfo.setProvinceName(provinceName);
                        regionInfo.setProvinceNameEn(provinceEnName);

                        regionInfo.setCityId(cityId);
                        regionInfo.setCityName(cityName);
                        regionInfo.setCityNameEn(cityEnName);

                        regionInfo.setGps(gpsCoordinates);

                        regionInfo.setIsDel("0");
                        regionInfo.setCreateDate(getCurDateTime());
                        regionInfo.setEditDate(getCurDateTime());


//                        ContentValues values = new ContentValues();
//                        values.put("COUNTRY_ID", countryId);
//                        values.put("COUNTRY_NAME", countryName);
//                        values.put("COUNTRY_NAME_EN", countryEnName);
//
//                        values.put("PROVINCE_ID", provinceId);
//                        values.put("PROVINCE_NAME", provinceName);
//                        values.put("PROVINCE_NAME_EN", provinceEnName);
//
//                        values.put("CITY_ID", cityId);
//                        values.put("CITY_NAME", cityName);
//                        values.put("CITY_NAME_EN", cityEnName);
//                        values.put("GPS", gpsCoordinates);
//
//                        values.put("IS_DEL", "0");
//                        values.put("CREATE_DATE", getCurDateTime());
//                        values.put("EDIT_DATE", getCurDateTime());
                        BaseDataBase.getInstance(context).regionDao().insert(regionInfo);
                        Log.i("bella","parseGpsData......end");
//                        context.getContentResolver().insert(uri, values);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    public static String getCurDateTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
        return formattedTime;
    }

    public static String ToString(Object ojb) {
        if (ojb == null) {
            return "";
        } else {
            return String.valueOf(ojb).trim();
        }
    }
}