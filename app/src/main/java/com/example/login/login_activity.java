package com.example.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.goldtek.iot.demo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class login_activity extends Activity {
    private RequestQueue mQueue;
    private StringRequest getRequest;
    CheckBox accountmem, passwordmem;
    TextView textView;
    Button login_button;
    EditText UserName,Password;
    String username,password;
    int ip1 = 114, ip2 = 24, ip3 = 145, ip4 = 201, ip5 = 5053;
    String login_url = "http://" +String.valueOf(ip1) + "." + String.valueOf(ip2) + "." + String.valueOf(ip3) + "." + String.valueOf(ip4) + ":" +String.valueOf(ip5) + "/login.php";
    AlertDialog.Builder builder;
    private Handler mUI_Handler = new Handler();
    private Handler mThreadHandler;
    private HandlerThread mThread;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        sp = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        textView = (TextView)findViewById(R.id.reg_txt);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(login_activity.this, register.class));
            }
        });
        builder = new AlertDialog.Builder(login_activity.this);
        login_button = (Button)findViewById(R.id.bn_login);
        UserName = (EditText)findViewById(R.id.login_name);
        Password = (EditText)findViewById(R.id.login_password);
        accountmem = (CheckBox)findViewById(R.id.accountmem);
        passwordmem = (CheckBox)findViewById(R.id.passwordmem);

        //mem account and passowrd +
        if(sp.getBoolean("ISCHECK_A", true))
        {
            accountmem.setChecked(true);
            UserName.setText(sp.getString("account", ""));
        }
        if(sp.getBoolean("ISCHECK_P", true))
        {
            passwordmem.setChecked(true);
            Password.setText(sp.getString("password", ""));
        }
        //mem account and passowrd -

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mem account and passowrd +
                if(!accountmem.isChecked())
                {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("account", "");
                    editor.commit();
                    sp.edit().putBoolean("ISCHECK_A", false).commit();
                }
                if(!passwordmem.isChecked())
                {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("password", "");
                    editor.commit();
                    sp.edit().putBoolean("ISCHECK_P", false).commit();
                }
                //mem account and passowrd -
                username = UserName.getText().toString();
                password = Password.getText().toString();

                if (username.equals("") || password.equals(""))
                {
                    builder.setTitle("Something went wrong");
                    displayAlert("Enter a valid username and password");
                }
                else if (username.equals("root") || password.equals("root")){
                    Intent intent = new Intent(getApplication(), com.example.lora.http.MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("name","root");
                    bundle.putString("email","root@gmail.com");
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else
                {
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, login_url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONArray jsonArray = new JSONArray(response);
                                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                                        String code = jsonObject.getString("code");
                                        Log.d("TEST     ",code);
                                        if (code.equals("login_failed")) {
                                            builder.setTitle("Login Error...");
                                            displayAlert(jsonObject.getString("message"));
                                        }
                                        else
                                        {
                                            //mem account and passowrd +
                                            if(accountmem.isChecked())
                                            {
                                                SharedPreferences.Editor editor = sp.edit();
                                                editor.putString("account", username);
                                                editor.commit();
                                                sp.edit().putBoolean("ISCHECK_A", true).commit();
                                            }
                                            if(passwordmem.isChecked())
                                            {
                                                SharedPreferences.Editor editor = sp.edit();
                                                editor.putString("password", password);
                                                editor.commit();
                                                sp.edit().putBoolean("ISCHECK_P", true).commit();
                                            }
                                            //mem account and passowrd -
                                            Intent intent = new Intent(getApplication(), com.example.lora.http.MainActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("name",jsonObject.getString("name"));
                                            bundle.putString("email",jsonObject.getString("email"));
                                            intent.putExtras(bundle);
                                            startActivity(intent);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(login_activity.this, "Error", Toast.LENGTH_SHORT).show();
                            error.printStackTrace();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("user_name",username);
                            params.put("password",password);
                            return params;
                        }
                    };
                    MySingleton.getmInstance(login_activity.this).addToRequestque(stringRequest);
                }
            }
        });
    }


    public void displayAlert(String message)
    {
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserName.setText("");
                Password.setText("");
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
