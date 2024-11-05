package com.android.oobe.location;

import android.util.Log;
import android.view.View;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.net.Uri;
import android.database.Cursor;
import android.content.ContentResolver;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import java.io.OutputStream;

import android.provider.Settings;
import android.widget.PopupWindow;
import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.core.widget.PopupWindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.app.Activity;

import com.android.oobe.R;
import com.android.oobe.Utils;
import com.android.oobe.exception.ColumnNotFoundException;

public class GpsSetController {
    public static final String REGION_URI = "content://com.boringdroid.systemuiprovider.region";
    private static final String TAG = "GpsSetController";

    ImageView imgSave;

    TextView txtCountry;
    TextView txtProvince;
    TextView txtCity;
    ImageView imgBack;

    List<String> listCountrys;
    List<String> listProvinces;
    List<String> listCitys;

    List<String> listCityGps;

    Context context;

    Activity activity;

    String gpsValue;

    int indexCountry = 0;
    int indexProvince = 0;
    int indexCity = 0;
    boolean isChineseLanguage;

    SimpleAdapter adapterCountry;
    SimpleAdapter adapterProvince;
    SimpleAdapter adapterCity;

    public GpsSetController(Activity activity, View rootView) {
        this.context = activity;
        this.activity = activity;
        initView(rootView);
        isChineseLanguage = Utils.isChineseLanguage(context);
        initData();
    }

    private void initView(View rootView) {
//        imgSave = (ImageView) rootView.findViewById(R.id.imgSave);
        txtCountry = (TextView) rootView.findViewById(R.id.txtCountry);
        txtProvince = (TextView) rootView.findViewById(R.id.txtProvince);
        txtCity = (TextView) rootView.findViewById(R.id.txtCity);
//        imgBack = (ImageView) rootView.findViewById(R.id.imgBack);
        // imgSave.setColorFilter(R.color.blue, PorterDuff.Mode.SRC_IN);

//        imgBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                activity.finish();
//            }
//        });
        txtCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopWindow(txtCountry, adapterCountry);
            }
        });

        txtProvince.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopWindow(txtProvince, adapterProvince);
            }
        });

        txtCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopWindow(txtCity, adapterCity);
            }
        });
    }


    public void setGps() {
//        String value = listCityGps.get(pos);
        String value = gpsValue;
        String locationGps = indexCountry + "~" + indexProvince + "~"
                + indexCity;
        Settings.Global.putString(context.getContentResolver(), "locationGps", locationGps);
        Log.w(TAG, "gpsValue = " + gpsValue);
        value = value.replace("\n", "").trim();
        String address = "/tmp/unix.str";
        LocalSocket clientSocket = new LocalSocket();
        LocalSocketAddress locSockAddr = new LocalSocketAddress(address, Namespace.FILESYSTEM);
        OutputStream clientOutStream = null;
        try {
            clientSocket.connect(locSockAddr);
            clientOutStream = clientSocket.getOutputStream();
            clientOutStream.write(value.getBytes());
            clientSocket.shutdownOutput();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    public static boolean isChineseLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.equals("zh");
    }

    private void initData() {
        listCountrys = new ArrayList<>();
        listProvinces = new ArrayList<>();
        listCitys = new ArrayList<>();

        adapterCountry = new SimpleAdapter(context, listCountrys, new SimpleAdapter.ItemClick() {
            @Override
            public void setOnItemClick(int pos) {
                txtCountry.setText(listCountrys.get(pos));
                popWindow.dismiss();
                listProvinces.clear();
                List<String> tempPList = queryProvincesByCountry(listCountrys.get(pos));
                if (tempPList != null) {
                    listProvinces.addAll(tempPList);
                    adapterProvince.notifyDataSetChanged();

                    if (pos != indexCountry) {
                        txtProvince.setText(listProvinces.get(0));
                    }
                    indexCountry = pos;
                    List<String> tempCList = queryCitysByProvince(listProvinces.get(0));
                    if (tempCList != null) {
                        listCitys.clear();
                        listCitys.addAll(tempCList);
                        adapterCity.notifyDataSetChanged();
                    }
                }
            }
        });
        adapterProvince = new SimpleAdapter(context, listProvinces, new SimpleAdapter.ItemClick() {
            @Override
            public void setOnItemClick(int pos) {
                txtProvince.setText(listProvinces.get(pos));
                popWindow.dismiss();
                listCitys.clear();
                List<String> tempCList = queryCitysByProvince(listProvinces.get(pos));
                if (tempCList != null) {
                    listCitys.addAll(tempCList);
                    adapterCity.notifyDataSetChanged();
                    if (pos != indexProvince) {
                        txtCity.setText(listCitys.get(0));
                    }
                    indexProvince = pos;
                }
            }
        });
        adapterCity = new SimpleAdapter(context, listCitys, new SimpleAdapter.ItemClick() {
            @Override
            public void setOnItemClick(int pos) {
                txtCity.setText(listCitys.get(pos));
                popWindow.dismiss();
                gpsValue = listCityGps.get(pos);
                indexCity = pos;
            }
        });

//        imgSave.setOnClickListener(view -> {
//            // String locationGps = spCountry.getSelectedItemId() + "~" +
//            // spProvince.getSelectedItemId() + "~"
//            // + spCity.getSelectedItemId();
//            String locationGps = indexCountry + "~" + indexProvince + "~"
//                    + indexCity;
//            Settings.Global.putString(context.getContentResolver(), "locationGps", locationGps);
//            setGps(gpsValue);
//        });

        String locationGps = Settings.Global.getString(context.getContentResolver(), "locationGps");
//        LogTools.i("locationGps: " + locationGps);
        if (locationGps != null) {
            String[] arrLocationGps = locationGps.split("~");
            try {
                indexCountry = Utils.ToInt(arrLocationGps[0]);
                indexProvince = Utils.ToInt(arrLocationGps[1]);
                indexCity = Utils.ToInt(arrLocationGps[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
//            LogTools.i("queryAllCountry  indexCountry: " + indexCountry + ",indexProvince: " + indexProvince
//                    + ",indexCity:"
//                    + indexCity);
            List<String> tempList = queryAllCountry();
            if (tempList != null) {
                listCountrys.addAll(queryAllCountry());
                adapterCountry.notifyDataSetChanged();

                if (tempList.size() > 0) {
                    List<String> tempPList = queryProvincesByCountry(listCountrys.get(indexCountry));
                    if (tempPList != null) {
                        listProvinces.addAll(tempPList);
                        adapterProvince.notifyDataSetChanged();

                        List<String> tempCList = queryCitysByProvince(listProvinces.get(indexProvince));
                        if (tempCList != null) {
                            listCitys.addAll(tempCList);
                            adapterCity.notifyDataSetChanged();
                            txtCountry.setText(listCountrys.get(indexCountry));
                            txtProvince.setText(listProvinces.get(indexProvince));
                            txtCity.setText(listCitys.get(indexCity));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private PopupWindow popWindow;

    @SuppressLint("WrongConstant")
    private void initPopWindow(View v, SimpleAdapter simpleAdapter) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_popup, null, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager lm = new LinearLayoutManager(context);
        lm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(simpleAdapter);
        int height = FrameLayout.LayoutParams.WRAP_CONTENT;

        if (null == popWindow) {
            if (v.getId() == R.id.txtCountry) {
                if (listCountrys.size() > 10) {
                    height = 200;
                }
            } else if (v.getId() == R.id.txtProvince) {
                if (listProvinces.size() > 10) {
                    height = 200;
                }
            } else {
                if (listCitys.size() > 10) {
                    height = 200;
                }
            }
            popWindow = new PopupWindow(view,
                    200, height, true);
            popWindow.setFocusable(false);// 底部导航消失
            popWindow.setSoftInputMode(popWindow.INPUT_METHOD_NEEDED);
            popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            popWindow.setTouchable(true);
            popWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            //
            popWindow.setOutsideTouchable(true);//
            popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {// 消失后的处理
                @Override
                public void onDismiss() {
                    popWindow = null;
                }
            });
            // 要为popWindow设置一个背景才有效
            popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            // PopupWindowCompat.showAsDropDown(popWindow, v, 0, 0, Gravity.START);
            PopupWindowCompat.showAsDropDown(popWindow, v, -50, 10, Gravity.RIGHT);
        }

    }

    private List<String> queryAllCountry() {
        Uri uri = Uri.parse(REGION_URI + "/REGION_COUNTRY");
        Cursor cursor = null;
        Map<String, Object> result = null;
        String selection = null;
        String[] selectionArgs = null;
        List<String> list = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    if (isChineseLanguage) {
                        String COUNTRY_NAME = getStringFromCursor(cursor, "COUNTRY_NAME");
                        list.add(COUNTRY_NAME);
                    } else {
                        String COUNTRY_NAME_EN = getStringFromCursor(cursor, "COUNTRY_NAME_EN");
                        list.add(COUNTRY_NAME_EN);
                    }

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (list == null || list.isEmpty()) queryAllCountry();
        return list;
    }

    private List<String> queryProvincesByCountry(String countryName) {
        Uri uri = Uri.parse(REGION_URI + "/REGION_PROVINCE");
        Cursor cursor = null;
        Map<String, Object> result = null;
        String selection = "COUNTRY_NAME = ?";
        if (!isChineseLanguage) {
            selection = "COUNTRY_NAME_EN = ?";
        }
        String[] selectionArgs = { countryName };
        List<String> list = null;

        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    if (isChineseLanguage) {
                        String PROVINCE_NAME = getStringFromCursor(cursor, "PROVINCE_NAME");
                        list.add(PROVINCE_NAME);
                    } else {
                        String PROVINCE_NAME_EN = getStringFromCursor(cursor, "PROVINCE_NAME_EN");
                        list.add(PROVINCE_NAME_EN);
                    }

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (list == null || list.isEmpty()) queryProvincesByCountry(countryName);
        return list;
    }

    private List<String> queryCitysByProvince(String province) {
        Uri uri = Uri.parse(REGION_URI + "/REGION_INFO");
        Cursor cursor = null;
        Map<String, Object> result = null;
        String selection = "PROVINCE_NAME = ?";
        if (!isChineseLanguage) {
            selection = "PROVINCE_NAME_EN = ?";
        }
        String[] selectionArgs = { province };
        List<String> list = null;
        listCityGps = new ArrayList<>();

        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    String CITY_ID = getStringFromCursor(cursor, "CITY_ID");
                    String GPS = getStringFromCursor(cursor, "GPS");
                    listCityGps.add(GPS);
                    if (isChineseLanguage) {
                        String CITY_NAME = getStringFromCursor(cursor, "CITY_NAME");
                        list.add(CITY_NAME);
                    } else {
                        String CITY_NAME_EN = getStringFromCursor(cursor, "CITY_NAME_EN");
                        list.add(CITY_NAME_EN);
                    }

                } while (cursor.moveToNext());
            }
            gpsValue = listCityGps.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (list == null || list.isEmpty() || gpsValue == null) return queryCitysByProvince(province);
        return list;
    }


    static class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.Holder> {
        Context context;
        List<String> list;
        ItemClick itemClick;

        public interface ItemClick {
            void setOnItemClick(int pos);
        }

        public SimpleAdapter(Context context, List<String> list, ItemClick itemClick) {
            this.context = context;
            this.list = list;
            this.itemClick = itemClick;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SimpleAdapter.Holder(LayoutInflater.from(context).inflate(R.layout.item_location, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.txtName.setText(list.get(position));
            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClick.setOnItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView txtName;
            LinearLayout rootView;

            public Holder(@NonNull View itemView) {
                super(itemView);
                txtName = itemView.findViewById(R.id.txtName);
                rootView = itemView.findViewById(R.id.rootView);
            }
        }
    }

    public static String getStringFromCursor(Cursor cursor, String columnName) throws ColumnNotFoundException {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) {
            throw new ColumnNotFoundException("Column '" + columnName + "' not found in cursor.");
        }
        return cursor.getString(columnIndex);
    }
}
