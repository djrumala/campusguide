package com.common_id.campusguide;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.common_id.campusguide.Adapter.Classroom;
import com.common_id.campusguide.Adapter.ClassroomAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminClassActivity extends AppCompatActivity implements ClassroomAdapter.PriceAdapterListener {

    private ClassroomAdapter classroomAdapter;
    private List<Classroom> classroomList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private String IPAddress = "common-id.com";
    private String HttpURL = "http://" + IPAddress + "/campusguide";

    private String IdClass;
    private ImageButton settingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_class);

//        Toolbar mytoolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
//        setSupportActionBar(mytoolbar); //mempersiapkan toolbar
//        getSupportActionBar().setTitle("Ruang Kelas");
//        //getSupportActionBar().setDisplayShowHomeEnabled(true);
////        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progressBar);
        settingButton = findViewById(R.id.setting);

        classroomAdapter = new ClassroomAdapter(this, classroomList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL)); //using simple divider
//        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(classroomAdapter);

        new ParseJSonDataClass(this).execute();

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminClassActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    public void AddClassRoom(View view) {
        Intent intent = new Intent(AdminClassActivity.this, AdminFormClassActivity.class);
        intent.putExtra("id","0");
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    class ParseJSonDataClass extends AsyncTask<Void, Void, Void> {
        public Context context;
        String FinalJSonResult;

        public ParseJSonDataClass(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpServiceClass httpServiceClass = new HttpServiceClass(HttpURL + "/class.php");
            httpServiceClass.AddParam("list", "1");

            try {
                httpServiceClass.ExecuteGetRequest();
                if (httpServiceClass.getResponseCode() == 200) {
                    FinalJSonResult = httpServiceClass.getResponse();

                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(FinalJSonResult);
                        JSONObject jsonObject;
                        Classroom classroom;

                        classroomList = new ArrayList<Classroom>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            classroom = new Classroom();
                            jsonObject = jsonArray.getJSONObject(i);

                            classroom.setId(jsonObject.getString("id"));
                            classroom.setName(jsonObject.getString("nama"));
                            classroom.setFloor(jsonObject.getString("floor"));
                            classroom.setLongitude(jsonObject.getString("long"));
                            classroom.setLatitude(jsonObject.getString("lat"));
                            classroomList.add(classroom);
                        }

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //}

                } else {
                    Toast.makeText(context, httpServiceClass.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.e("Respon ", FinalJSonResult);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

//            classroomAdapter.notifyDataSetChanged();
            classroomAdapter = new ClassroomAdapter(AdminClassActivity.this, classroomList, AdminClassActivity.this);
            recyclerView.setAdapter(classroomAdapter);
        }
    }

    @Override
    public void onClassroomSelected(Classroom classroom) {
        String Floor = classroom.getFloor();
        String IdClass = classroom.getId();
        String Name = classroom.getName();
        String Longitude = classroom.getLongitude();
        String Latitude = classroom.getLatitude();

        finish();
        Intent intent = new Intent(this, AdminFormClassActivity.class);
        intent.putExtra("latitude", Latitude);
        intent.putExtra("longitude", Longitude);
        intent.putExtra("name", Name);
        intent.putExtra("id", IdClass);
        intent.putExtra("floor", Floor);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onClassroomLongClick(Classroom classroom) {
        classroom.getFloor();
        IdClass = classroom.getId();
        final CharSequence[] dialogitem = {"Hapus"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(AdminClassActivity.this);
        dialog.setCancelable(true);
        dialog.setItems(dialogitem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        DialogDelete();
                        break;
                }
            }
        }).show();
    }

    public void DialogDelete() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(new ContextThemeWrapper
                (this, R.style.myAlertDialogTheme));
        builder.setTitle("CampusGuide");
        builder.setMessage("Apakah Anda yakin untuk menghapus kelas ini?");
        builder.setIcon(R.drawable.logo);
        builder.setCancelable(true);

        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new SaveDataClass(AdminClassActivity.this).execute();
            }

        });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        android.support.v7.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private class SaveDataClass extends AsyncTask<Void, Void, Void> {
        public Context context;
        String FinalResult;
        ProgressDialog progressDialog;

        public SaveDataClass(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(AdminClassActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("CampusGuide");
            progressDialog.setMessage("Loading Data...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpServiceClass httpServiceClass = null;

            httpServiceClass = new HttpServiceClass(HttpURL + "/class.php");

            httpServiceClass.AddParam("hapus", "1");
            httpServiceClass.AddParam("id", IdClass);
            try {
                httpServiceClass.ExecutePostRequest();
                if (httpServiceClass.getResponseCode() == 200) {
                    FinalResult = httpServiceClass.getResponse();

                } else {
                    Toast.makeText(context, httpServiceClass.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            new ParseJSonDataClass(AdminClassActivity.this).execute();
        }
    }

//    //TODO: FOR TOOLBAR
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.menu_add, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) { //when the action button is tapped
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                onBackPressed(); //go back to previous activity or fragment
//                break;
//            case R.id.menuadd:
//                Intent intent = new Intent(AdminClassActivity.this, AdminFormClassActivity.class);
//                startActivity(intent);
//                overridePendingTransition(0, 0);
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onBackPressed() {
        finishAffinity();
//        Intent intent = new Intent(RoomsActivity.this, MainActivity.class);
//        startActivity(intent);
//        finish();
    }
}
