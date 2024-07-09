package com.android.provision;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.provider.SyncStateContract;
import android.util.Base64;
import android.util.Log;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

public class Utils {
    public static final String SURFFIX_SVG = ".svg";
    public static final String SURFFIX_SVGZ = ".svgz";
    public static final String SURFFIX_PNG = ".png";

    public static Drawable getImage(String imageStr, String iconType, String name, Context context) {
        Log.d("TAG", "getImage() called with: imageStr = [" + imageStr.length() + "], iconType = [" + iconType + "], name = [" + name + "]");
        if (SURFFIX_SVG.equals(iconType) || SURFFIX_SVGZ.equals(iconType)) {
            try {
                byte[] decode = Base64.decode(imageStr, Base64.DEFAULT);
                SVG svg = SVG.getFromString(new String(decode));
                Drawable drawable = new PictureDrawable(svg.renderToPicture());
                return drawable;
            } catch (SVGParseException e) {
                e.printStackTrace();
            }
        } else if (SURFFIX_PNG.equals(iconType)) {
            byte[] decode = Base64.decode(imageStr, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
            if (bitmap != null) {
                return new BitmapDrawable(bitmap);
            }
        }
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
        return new BitmapDrawable(bitmap);
    }
}
