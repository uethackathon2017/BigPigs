package com.bigpigs.main;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.bigpigs.API;
import com.bigpigs.CONSTANT;
import com.bigpigs.R;
import com.bigpigs.custom.view.RoundedImageView;
import com.bigpigs.model.Price;
import com.bigpigs.model.SystemPitch;
import com.bigpigs.support.ShowToast;
import com.bigpigs.support.TrackGPS;
import com.bigpigs.support.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    private RoundedImageView bt_call,bt_order;
    private int callRequest = 1;
    private int gpsRequest = 2;

    private String lat;
    private String lng;
//    private String getPlace = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lng+"&sensor=false";
//    private String getPlace = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
    private String TAG = "DetailActivity";
//    private SupportMapFragment mapFragment;
    private TextView bt_now;
    private GoogleMap map;
    private TrackGPS gps;
    private LatLng currentLatLng;
    private LatLng start,end;
    private List<Route> listRoute;
    private SystemPitch mSystemPitch;
//    private LatLng xuanthuy = new LatLng(21.036654,105.78218);
    private LatLng target;
    private JSONObject rawDirections;
    private ArrayList<Price> listMondayPrice,listSaturdayPrice;
    private String priceData;
    private OkHttpClient okHttpClient;
    private TableLayout tableLayout;
    private TextView tvSysName,tvDes;
    private TextView tvPhone;
    private Button btView;
    public DetailActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSystemPitch = (SystemPitch) getIntent().getSerializableExtra(CONSTANT.SystemPitch_MODEL);
        listMondayPrice = new ArrayList<>();
        listSaturdayPrice = new ArrayList<>();



        if (mSystemPitch !=null) {
            Log.d(TAG,mSystemPitch.toString());
            target = new LatLng(Double.valueOf(mSystemPitch.getLat()), Double.valueOf(mSystemPitch.getLng()));
        }
        setContentView(R.layout.activity_pitch_detail);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        initView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        bt_order.setOnClickListener(this);
        bt_call.setOnClickListener(this);
//        bt_now.setOnClickListener(this);

    }
    public void initView() {
        bt_call = (RoundedImageView) findViewById(R.id.bt_call);
        bt_order = (RoundedImageView) findViewById(R.id.bt_order);
        tableLayout = (TableLayout) findViewById(R.id.tb_pricing);
        tvDes = (TextView) findViewById(R.id.tv_desc);
        tvSysName = (TextView) findViewById(R.id.tv_syspitch_name);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        btView = (Button) findViewById(R.id.bt_view);
        btView.setOnClickListener(this);

        if(mSystemPitch !=null)
        {
            tvDes.setText(mSystemPitch.getDescription());
            tvSysName.setText(mSystemPitch.getName());
            tvPhone.setText(mSystemPitch.getPhone());
        }

        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        tr.setGravity(Gravity.CENTER);

        TextView b = new TextView(this);
        TextView c = new TextView(this);
        TextView f = new TextView(this);
        TextView g = new TextView(this);

        b.setText("Khung giờ");
        c.setText("Ngày thường");
        f.setText("Thứ 7, Chủ nhật");
        g.setText("Ghi chú");
        b.setTextSize(12);
        c.setTextSize(12);
        f.setTextSize(12);
        g.setTextSize(12);

        b.setPadding(10,0,10,0);
        c.setPadding(10,0,10,0);
        f.setPadding(10,0,10,0);
        g.setPadding(10,0,10,0);

        b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        c.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        f.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        g.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tr.addView(b);
        tr.addView(c);
        tr.addView(f);
        tr.addView(g);
        tableLayout.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        new GetPriceTask().execute();
    }
    private class GetPriceTask extends AsyncTask<String,Void,String> {
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            Request systemPitchRequest = new Request.Builder()
                    .url(API.GetPrice)
                    .build();
            try {
                okHttpClient = new OkHttpClient();
                okhttp3.Response systemPitchResponse = okHttpClient.newCall(systemPitchRequest).execute();
                if (systemPitchResponse.isSuccessful()) {
                    priceData = systemPitchResponse.body().string();
                    JSONObject result = new JSONObject(priceData);
                    if(result.getString("status").contains("success"))
                    {
                        JSONArray data = result.getJSONArray("data");
//                        Log.d("Data",data.toString()+"");
                        for (int i=0;i<data.length();i++)
                        {
                            JSONObject object = data.getJSONObject(i);
                            Price p = new Price();
                            p.setDayOfWeek(object.getString("dateofweek"));
                            p.setDescription(object.getString("description"));
                            p.setPrice(object.getString("price")+".000vnđ");
                            p.setTime(object.getString("time_start").substring(0,5)+"-"+object.getString("time_end").substring(0,5));
                            if(p.getDayOfWeek().contains("Mon")) listMondayPrice.add(p);
                            else listSaturdayPrice.add(p);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                ShowToast.showToastLong(DetailActivity.this,e.getMessage().toString());
            }
            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            for(int j=0;j<listMondayPrice.size();j++)
            {
                Price monday = listMondayPrice.get(j);
                Price saturday = listSaturdayPrice.get(j);
                addRow(monday.getTime(),monday.getPrice(),saturday.getPrice(),monday.getDescription());
            }
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DetailActivity.this);
            progressDialog.setMessage("Đang thao tác");
            progressDialog.show();
        }
    }
    public void addRow(String time,String mondayPrice,String weekendPrice,String des)
    {
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        tr.setGravity(Gravity.CENTER);
        TextView b = new TextView(this);
        TextView c = new TextView(this);
        TextView f = new TextView(this);
        TextView g = new TextView(this);

        b.setText(time);
        c.setText(mondayPrice);
        f.setText(weekendPrice);
        g.setText(des);
        b.setTextSize(12);
        c.setTextSize(12);
        f.setTextSize(12);
        g.setTextSize(12);

        b.setPadding(10,5,10,5);
        c.setPadding(10,5,10,5);
        f.setPadding(10,5,10,5);
        g.setPadding(10,5,10,5);

        b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        c.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        f.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        g.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        b.setGravity(Gravity.CENTER);
        f.setGravity(Gravity.CENTER);
        c.setGravity(Gravity.CENTER);
        g.setGravity(Gravity.CENTER);

        tr.addView(b);
        tr.addView(c);
        tr.addView(f);
        tr.addView(g);
        tableLayout.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_call: {
                ActivityCompat.requestPermissions(DetailActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, callRequest);
                break;
            }
            case R.id.bt_view: {

                Intent intent = new Intent(DetailActivity.this,ListPitchActivity.class);
                intent.putExtra(CONSTANT.SystemPitch_MODEL,mSystemPitch);
                startActivity(intent);
                break;
            }
//            case R.id.bt_now: {
//                startActivity(new Intent(DetailActivity.this,ListPitchActivity.class));
//                break;
//            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == callRequest)
            if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "01266343244"));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(intent);
            }
        if (requestCode == gpsRequest)
            if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                gps = new TrackGPS(DetailActivity.this,DetailActivity.this);
                if(gps.canGetLocation()){
                    double longitude = gps.getLongitude();
                    double latitude = gps .getLatitude();
                    Log.d(TAG,"lat : " + latitude +" lng :"+longitude);
                    currentLatLng = new LatLng(gps.getLatitude(),gps.getLongitude());
                    lat = target.latitude+"";
                    lng = target.longitude+"";
                }
            }
    }
//    public String getAddress(double latitude,double longitude) throws Exception
//    {
//        Geocoder geocoder;
//        List<Address> addresses;
//        geocoder = new Geocoder(this, Locale.getDefault());
//
//        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//
//        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//        String city = addresses.get(0).getLocality();
//        String state = addresses.get(0).getAdminArea();
//        String country = addresses.get(0).getCountryName();
//        String postalCode = addresses.get(0).getPostalCode();
//        String knownName = addresses.get(0).getFeatureName();
//        return addresses.toString();
//    }
//    public void drawLine(GoogleMap map,LatLng start,LatLng finish)
//    {
//        GoogleDirection.withServerKey("AIzaSyAbx2lqsUq1OfOCHUbk7N_DPlyNzeP1n6s")
//                .from(start)
//                .to(finish)
//                .transportMode(TransportMode.DRIVING)
//                .execute(this);
//    }
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        if(mapFragment !=null) {
//            map = googleMap;
//            Utils.moveCamera(new LatLng(Double.valueOf(mSystemPitch.getLat()),Double.valueOf(mSystemPitch.getLng())),mSystemPitch.getName(),12,map);
//        }
//    }
//    public void drawPolyline(GoogleMap map,LatLng start,LatLng end)
//    {
//        map.addPolyline(new PolylineOptions().add(start,end).color(Color.BLUE).width(7));
//    }
//    @Override
//    public void onDirectionSuccess(Direction direction, String rawBody) {
//        Log.d(TAG,rawBody);
//        JSONObject routes, legs;
//        JSONArray steps;
//        if(direction.getRouteList().size()>0)
//        {
//        Route route = direction.getRouteList().get(0);
//        if (route != null)
//        {
//            if(route.getLegList().size()>0)
//            {
//            Leg leg = route.getLegList().get(0);
//            List<Step> stepList = leg.getStepList();
//            Log.d("Size ", stepList.size() + "");
//            for (int i = 0; i < stepList.size() - 1; i++) {
//            }
//            map.addMarker(new MarkerOptions().position(leg.getEndLocation().getCoordination()));
//            map.addMarker(new MarkerOptions().position(leg.getStartLocation().getCoordination()));
//
//            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
//            PolylineOptions polylineOptions = DirectionConverter.createPolyline(this, directionPositionList, 5,
//                    getResources().getColor(R.color.bluehistory));
//
//            map.addPolyline(polylineOptions);
//          }
//        }
//        }
//        else
//        {
//
//
//        }
//
//    }
//    @Override
//    public void onDirectionFailure(Throwable t) {
//            Log.d(TAG,"Direction Failed");
//    }
}

