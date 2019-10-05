package com.common_id.campusguide;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.common_id.campusguide.UserLoginActivity.MyPreferences;
import static com.common_id.campusguide.UserLoginActivity.mEmail;

public class ProfileActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private String IPAddress = "common-id.com";
    private String HttpURL = "http://" + IPAddress + "/campusguide";
    private EditText email, phone, mom, birthday, ktp, npwp, idcard, membernumb, department, name;
    private String Name, Email, Phone, Mom, Birthday, KTP, NPWP, IdCard, MemberNumb, Department;
    private Button logoutButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar mytoolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
        setSupportActionBar(mytoolbar); //mempersiapkan toolbar
        getSupportActionBar().setTitle("Profil Saya");
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        logoutButton = findViewById(R.id.logout);

        SharedPreferences pref = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        Name = pref.getString(UserLoginActivity.mName, "Anda belum mengisi data");
        Email = pref.getString(mEmail, "Anda belum mengisi data");

        email.setText(Email);
        name.setText(Name);
        email.setEnabled(false);
        name.setEnabled(false);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(getApplicationContext(), UserLoginActivity.class);
                startActivity(intent);
                //Clear sharepreferences when logging out
                SharedPreferences pref = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.apply();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //when the action button is tapped
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); //go back to previous activity or fragment
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(0,0);
    }
}
