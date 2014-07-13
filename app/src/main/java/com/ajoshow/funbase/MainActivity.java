package com.ajoshow.funbase;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ajoshow.funbase.libs.FirebaseAgent;
import com.ajoshow.funbase.utils.Constant;

import funbase.ajoshow.com.funbase.R;


public class MainActivity extends Activity {

    private Button mRegisterBtn;
    private EditText mUserIdEt;
    private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPref = getSharedPreferences(Constant.APP_NAME, Context.MODE_PRIVATE);
        mUserIdEt = (EditText) findViewById(R.id.inviterId);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = mUserIdEt.getText().toString().trim();
                if(id.isEmpty()){
                    Toast.makeText(MainActivity.this, "Invalid ID. Please try again.", Toast.LENGTH_SHORT).show();
                }else{
                    mSharedPref.edit().putString(Constant.USER_ID, id).commit();
                    FirebaseAgent.getInstance().registerOpt(id);
                    startActivity(EventActivity.makeIntent(MainActivity.this));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        FirebaseAgent.getInstance().unregister();
        super.onDestroy();
    }
}
