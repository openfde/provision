package com.android.oobe.location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.PopupWindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.oobe.BaseDataBase;
import com.android.oobe.R;
import com.android.oobe.Utils;
import com.android.oobe.application.model.RegionInfo;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GpsSetController {
    private static final String TAG = "GpsSetController";

    ImageView imgSave;
    TextView txtCountry;
    TextView txtProvince;
    TextView txtCity;
    ImageView imgBack;

    List<String> listCountrys;
    List<String> listProvinces;
    List<String> listCitys;

    List<RegionInfo> listAddress;
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

    private static final int MSG_COURTY = 1001;
    private static final int MSG_PROVINCE = 1002;
    private static final int MSG_CITY = 1003;

    public GpsSetController(Activity activity, View rootView) {
        this.context = activity;
        this.activity = activity;
        initView(rootView);
        isChineseLanguage = Utils.isChineseLanguage(context);
        initData();
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_COURTY:
                    if (listCountrys != null) {
                        adapterCountry.notifyDataSetChanged();
                        txtCountry.setText(listCountrys.get(indexCountry));
                        if (listCountrys.size() > 0) {
                            queryProvincesByCountry(listCountrys.get(indexCountry), indexCountry);
                        }
                    } else {
                        listCountrys = new ArrayList<>();
                        adapterCountry.notifyDataSetChanged();
                    }
                    break;

                case MSG_PROVINCE:
                    txtCountry.setText(listCountrys.get(indexCountry));

                    if (listProvinces != null) {
                        txtProvince.setText(listProvinces.get(indexProvince));
                        adapterProvince.notifyDataSetChanged();
                        queryCitysByProvince(listProvinces.get(0), 0);
                    } else {
                        listProvinces = new ArrayList<>();
                        adapterProvince.notifyDataSetChanged();
                    }


                    break;

                case MSG_CITY:
                    txtProvince.setText(listProvinces.get(indexProvince));
                    if (listCitys != null) {
                        txtCity.setText(listCitys.get(indexCity));
                        adapterCity.notifyDataSetChanged();
                        gpsValue = listAddress.get(indexCity).getGps();
                    } else {
                        listCitys = new ArrayList<>();
                        adapterCity.notifyDataSetChanged();
                    }

                    break;

            }

        }
    };

    private void initView(View rootView) {
        txtCountry = (TextView) rootView.findViewById(R.id.txtCountry);
        txtProvince = (TextView) rootView.findViewById(R.id.txtProvince);
        txtCity = (TextView) rootView.findViewById(R.id.txtCity);

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
        String locationGpsInfo = listCountrys.get(indexCountry) + "~" + listProvinces.get(indexProvince) + "~"
                + listCitys.get(indexCity);
        Settings.Global.putString(context.getContentResolver(), "locationGpsInfo", locationGpsInfo);
        Log.w(TAG, "bella locationGps " + locationGps + ", locationGpsInfo = " + locationGpsInfo);

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
        listAddress = new ArrayList<>();
        adapterCountry = new SimpleAdapter(context, listCountrys, new SimpleAdapter.ItemClick() {
            @Override
            public void setOnItemClick(int pos) {
                indexCountry = pos ;
                indexProvince = 0 ;
                indexCity = 0;
                popWindow.dismiss();
                listProvinces.clear();
                queryProvincesByCountry(listCountrys.get(pos), pos);
            }
        });
        adapterProvince = new SimpleAdapter(context, listProvinces, new SimpleAdapter.ItemClick() {
            @Override
            public void setOnItemClick(int pos) {
                indexProvince = pos ;
                indexCity = 0;
                txtProvince.setText(listProvinces.get(pos));
                popWindow.dismiss();
                listCitys.clear();
                queryCitysByProvince(listProvinces.get(pos), pos);
            }
        });
        adapterCity = new SimpleAdapter(context, listCitys, new SimpleAdapter.ItemClick() {
            @Override
            public void setOnItemClick(int pos) {
                indexCity = pos ;
                txtCity.setText(listCitys.get(pos));
                popWindow.dismiss();
                gpsValue = listAddress.get(pos).getGps();
            }
        });


        String locationGps = Settings.Global.getString(context.getContentResolver(), "locationGps");
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

        queryAllCountry();
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

    private void queryAllCountry() {
        try {
            listCountrys.clear();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isChineseLanguage) {
                            listCountrys.addAll(BaseDataBase.getInstance(context).regionDao().getAllZhCoutry());
                        } else {
                            listCountrys.addAll(BaseDataBase.getInstance(context).regionDao().getAllEnCoutry());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Message msg = new Message();
                    msg.what = MSG_COURTY;
                    msg.arg1 = indexCountry;
                    handler.sendMessage(msg);

                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void queryProvincesByCountry(String countryName, int pos) {
        listProvinces.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isChineseLanguage) {
                        listProvinces.addAll(BaseDataBase.getInstance(context).regionDao().getAllZhProvincesByCoutryId(countryName));
                    } else {
                        listProvinces.addAll(BaseDataBase.getInstance(context).regionDao().getAllEnProvincesByCoutryId(countryName));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Message msg = new Message();
                msg.arg1 = pos;
                msg.what = MSG_PROVINCE;
                handler.sendMessage(msg);

            }
        }).start();
    }

    private void queryCitysByProvince(String province, int pos) {
        listAddress.clear();
        listCitys.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listAddress.addAll(BaseDataBase.getInstance(context).regionDao().getAllCitysByProvinceId(province));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (listAddress != null) {
                    listCitys.addAll(listAddress.stream()
                            .map(address -> isChineseLanguage ? address.getCityName() : address.getCityNameEn())
                            .collect(Collectors.toList()));
                }

                Message msg = new Message();
                msg.what = MSG_CITY;
                msg.arg1 = pos;
                handler.sendMessage(msg);

            }
        }).start();
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

}
