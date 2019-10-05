package com.common_id.campusguide;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.common_id.campusguide.Adapter.ReviewAdapter;

import static com.common_id.campusguide.UserLoginActivity.MyPreferences;

public class UserReviewActivity extends AppCompatActivity {
    private String IPAddress = "common-id.com";
    private String HttpURL = "http://" + IPAddress + "/campusguide";


    private String IdClass, Floor, RoomName, Name, Address, Longitude, Latitude, IdUser, ReviewHolder;
    private TextView name, room;
    private EditText review;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_review);

        name = findViewById(R.id.name);
        room = findViewById(R.id.room);
        review = findViewById(R.id.et_review);

        //Make cannot be entered
        review.setRawInputType(InputType.TYPE_CLASS_TEXT);
        review.setImeOptions(EditorInfo.IME_ACTION_DONE);

        SharedPreferences pref = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        String Name = pref.getString(UserLoginActivity.mName, "Anda belum mengisi data");
        IdUser = pref.getString(UserLoginActivity.mId, "Anda belum mengisi data");

        name.setText(Name);

        if (this.getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            Longitude = extras.getString("longitude", "0");
            Latitude = extras.getString("latitude", "0");
            RoomName = extras.getString("name", "");
            Floor = extras.getString("floor", "");
            Address = extras.getString("address", "");
            IdClass = extras.getString("id", "0");
            room.setText(RoomName);
        }
    }

    public void CheckEditTextIsEmptyOrNot() {
        boolean cancel = false;
        View focusView = null;

        review.setError(null);//reset error

        ReviewHolder = review.getText().toString();

        if (TextUtils.isEmpty(ReviewHolder) || ReviewHolder.length() > 200) {
            cancel = true;
        }
        if (cancel) {
            Toast.makeText(this, "Masukkan ulasan anda", Toast.LENGTH_LONG).show();
        }
        if (!cancel) {
            ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
//                UserLoginFunction(EmailHolder, PasswordHolder); //memanggil fungsi login
                new SaveDataClass(this).execute();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myAlertDialogTheme));
                builder.setTitle("Error");
                builder.setMessage("Koneksi Internet Tidak ditemukan");
                builder.setCancelable(true);

                builder.setNegativeButton("Coba Lagi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        }
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
            progressDialog = new ProgressDialog(UserReviewActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("CampusGuide");
            progressDialog.setMessage("Loading Data...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpServiceClass httpServiceClass = null;

            httpServiceClass = new HttpServiceClass(HttpURL + "/class.php");

            httpServiceClass.AddParam("id_user", IdUser);
            httpServiceClass.AddParam("id_class", IdClass);
            httpServiceClass.AddParam("ulasan", ReviewHolder);

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

            finish();
            Intent intent = new Intent(UserReviewActivity.this, UserDetailActivity.class);
            intent.putExtra("latitude", Latitude);
            intent.putExtra("longitude", Longitude);
            intent.putExtra("name", RoomName);
            intent.putExtra("floor", Floor);
            intent.putExtra("id", IdClass);
            intent.putExtra("address", Address);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    public void ClosePage(View view) {
        finish();
    }

    public void SavePost(View view) {
        CheckEditTextIsEmptyOrNot();
    }
}
