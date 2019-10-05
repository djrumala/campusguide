package com.common_id.campusguide;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.common_id.campusguide.Adapter.Classroom;
import com.common_id.campusguide.Adapter.Image;
import com.common_id.campusguide.Adapter.ImageAdapter;
import com.common_id.campusguide.Adapter.Review;
import com.common_id.campusguide.Adapter.ReviewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserDetailActivity extends AppCompatActivity implements ReviewAdapter.ReviewAdapterListener, ImageAdapter.ImageAdapteraListener {

    private List<Classroom> classroomList = new ArrayList<>();
    private RecyclerView recyclerView, recViewComment;

    private ProgressBar progressBar;

    private String IPAddress = "common-id.com";
    private String HttpURL = "http://" + IPAddress + "/campusguide";

    private String IdClass, Floor, Name, Address, Longitude, Latitude;
    private ImageButton settingButton;
    private TextView name, floor, address;

    private ImageAdapter horizontalAdapter;
    private ReviewAdapter reviewAdapter;
    private List<Image> imageList = new ArrayList<Image>();
    private List<Review> reviewList = new ArrayList<Review>();

    private String CamerafilePath, EncodedURL, imageURL, Extension;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_EX_STORAGE_CODE = 200;
    private int REQUEST_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        Toolbar mytoolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
        setSupportActionBar(mytoolbar); //mempersiapkan toolbar
        getSupportActionBar().setTitle("");
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view);
        recViewComment = findViewById(R.id.recycler_view2);
        progressBar = findViewById(R.id.progressBar);
        name = findViewById(R.id.name);
        floor = findViewById(R.id.floor);
        address = findViewById(R.id.address);
        settingButton = findViewById(R.id.setting);

        if (this.getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            Longitude = extras.getString("longitude", "0");
            Latitude = extras.getString("latitude", "0");
            Name = extras.getString("name", "");
            Floor = extras.getString("floor", "");
            Address = extras.getString("address", "");
            IdClass = extras.getString("id", "0");
            Log.e("latitude", Latitude);
            Log.e("longitude", Longitude);
//            longitude.setText(Longitude);
//            latitude.setText(Latitude);
            floor.setText("Lantai " + Floor);
            name.setText(Name);
            address.setText(Address);
        }


//        Image image;
//        for (int i = 0; i < 5; i++) {
//            image = new Image();
//            image.setImageId(String.valueOf(i));
//            image.setImageUrl("https://suaramerdekanet.com/wp-content/uploads/2018/05/ruang-kelas.jpg");
//            imageList.add(image);
//        }
        new GetImageDataClass(this).execute();

        horizontalAdapter = new ImageAdapter(this, imageList, this);
        RecyclerView.LayoutManager horizontalLayoutManager = new LinearLayoutManager(UserDetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        recyclerView.setAdapter(horizontalAdapter);

        reviewAdapter = new ReviewAdapter(this, reviewList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recViewComment.setLayoutManager(mLayoutManager);
        recViewComment.setItemAnimator(new DefaultItemAnimator());
        recViewComment.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL)); //using simple divider
//        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recViewComment.setAdapter(reviewAdapter);
//
        new ParseJSonDataClass(this).execute();
//
//        settingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(UserDetailActivity.this, ProfileActivity.class);
//                startActivity(intent);
//                overridePendingTransition(0, 0);
//            }
//        });
    }

    public void AddClassRoom(View view) {
        Intent intent = new Intent(UserDetailActivity.this, AdminFormClassActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onImageClick(Image image) {

    }

    @Override
    public void onReviewClick(Review review) {

    }

    public void AddPhoto(View view) {
//        selectImage();
        Intent intent = new Intent(UserDetailActivity.this, UserUploadActivity.class);
        intent.putExtra("latitude", Latitude);
        intent.putExtra("longitude", Longitude);
        intent.putExtra("name", Name);
        intent.putExtra("floor", Floor);
        intent.putExtra("id", IdClass);
        intent.putExtra("address", Address);
        startActivity(intent);
    }

    public void AddReview(View view) {
        Intent intent = new Intent(UserDetailActivity.this, UserReviewActivity.class);
        intent.putExtra("latitude", Latitude);
        intent.putExtra("longitude", Longitude);
        intent.putExtra("name", Name);
        intent.putExtra("floor", Floor);
        intent.putExtra("id", IdClass);
        intent.putExtra("address", Address);
        startActivity(intent);
    }

    //Menampilkan ulasan
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
            httpServiceClass.AddParam("ulasan", IdClass);

            try {
                httpServiceClass.ExecuteGetRequest();
                if (httpServiceClass.getResponseCode() == 200) {
                    FinalJSonResult = httpServiceClass.getResponse();

                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(FinalJSonResult);
                        JSONObject jsonObject;
                        Review classroom;

                        classroomList = new ArrayList<Classroom>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            classroom = new Review();
                            jsonObject = jsonArray.getJSONObject(i);

                            classroom.setId(String.valueOf(i + 1));
                            classroom.setReview(jsonObject.getString("ulasan"));
                            reviewList.add(classroom);
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

            reviewAdapter.notifyDataSetChanged();
        }
    }

    //Menampilkan ulasan
    class GetImageDataClass extends AsyncTask<Void, Void, Void> {
        public Context context;
        String FinalJSonResult;

        public GetImageDataClass(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpServiceClass httpServiceClass = new HttpServiceClass(HttpURL + "/class.php");
            httpServiceClass.AddParam("image", IdClass);

            try {
                httpServiceClass.ExecuteGetRequest();
                if (httpServiceClass.getResponseCode() == 200) {
                    FinalJSonResult = httpServiceClass.getResponse();

                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(FinalJSonResult);
                        JSONObject jsonObject;
                        Image image;

                        classroomList = new ArrayList<Classroom>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            image = new Image();
                            image.setImageId(String.valueOf(i));
                            image.setImageUrl(jsonObject.getString("foto"));
                            imageList.add(image);
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
            Log.e("ImResp ", FinalJSonResult);
//            progressBar.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.VISIBLE);

            horizontalAdapter.notifyDataSetChanged();

        }
    }

//    //TODO: FOR TOOLBAR
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.menu_add, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //when the action button is tapped
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); //go back to previous activity or fragment
                break;
//            case R.id.menuadd:
//                Intent intent = new Intent(AdminClassActivity.this, AdminFormClassActivity.class);
//                startActivity(intent);
//                overridePendingTransition(0, 0);
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
//        Intent intent = new Intent(UserDetailActivity.this, UserMainActivity.class);
//        startActivity(intent);
//        finish();
    }
}
