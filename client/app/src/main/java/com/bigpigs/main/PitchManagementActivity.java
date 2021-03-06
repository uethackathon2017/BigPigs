package com.bigpigs.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.bigpigs.API;
import com.bigpigs.CONSTANT;
import com.bigpigs.R;
import com.bigpigs.adapter.PitchManagementAdapter;
import com.bigpigs.model.Pitch;
import com.bigpigs.model.UserModel;
import com.bigpigs.view.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class PitchManagementActivity extends AppCompatActivity {
    private RoundedImageView btAddPitch;
    private RecyclerView recyclerView;
    private PitchManagementAdapter adapter;
    private ArrayList<Pitch> listPitch;
    private OkHttpClient okHttpClient;
    private String listpitchData;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_management);
        userModel = (UserModel) getIntent().getSerializableExtra(CONSTANT.KEY_USER);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Quản lý sân ");
        initView();
        initList();
    }
    public void initList()
    {
        listPitch = new ArrayList<>();
        new GetListPitch().execute();
    }
    private class GetListPitch extends AsyncTask<String,Void,String> {
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            Request request = new Request.Builder()
                    .url(API.GetAllPitchofSystem+1)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "b9494f39-8e39-7533-1896-281ee653703b")
                    .build();
            try {
                okHttpClient = new OkHttpClient();
                okhttp3.Response systemPitchResponse = okHttpClient.newCall(request).execute();
                if (systemPitchResponse.isSuccessful()) {
                    listpitchData = systemPitchResponse.body().string().toString();
                    Log.d("data",listpitchData.toString());
                    JSONObject result = new JSONObject(listpitchData);
                    if(result.getString("status").contains("success"))
                    {
                        JSONArray data = result.getJSONArray("data");
                        for (int i=0;i<data.length();i++)
                        {
                            JSONObject object = data.getJSONObject(i);
                            Pitch p = new Pitch();
                            if(!object.getString("id").equals(null))
                            p.setId(object.getString("id"));
                            if(!object.getString("name").equals(null))
                                p.setName(object.getString("name"));
                            if(!object.getString("type").equals(null))
                                p.setType(object.getString("type"));
                            if(!object.getString("size").equals(null))
                                p.setSize(object.getString("size"));
                            if(!object.getString("description").equals(null))
                                p.setDescription(object.getString("description"));
                            if(!object.getString("system_id").equals(null))
                                p.setSystemId(object.getString("system_id"));
                            listPitch.add(p);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            recyclerView = (RecyclerView) findViewById(R.id.list_pitch);
            LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(PitchManagementActivity.this); // (Context context)
            mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(mLinearLayoutManagerVertical);
            adapter = new PitchManagementAdapter(PitchManagementActivity.this,listPitch);
            recyclerView.setAdapter(adapter);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PitchManagementActivity.this);
            progressDialog.setMessage("Đang thao tác");
            progressDialog.show();
        }
    }
    public void initView()
    {
        btAddPitch = (RoundedImageView) findViewById(R.id.bt_addPitch);
        btAddPitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PitchManagementActivity.this,AddPitchActivity.class);
                intent.putExtra(CONSTANT.KEY_USER,userModel);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
