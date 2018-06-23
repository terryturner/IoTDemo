package com.example.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.goldtek.iot.demo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class register extends Activity {
    Button reg_bn;
    EditText Name,Phone,UserName,Password,ConPassword;
    String  name,phone,username,password,conpass;
    AlertDialog.Builder builder;
    int ip1 = 114, ip2 = 32, ip3 = 130, ip4 = 154, ip5 = 5053;
    String reg_url = "http://" +String.valueOf(ip1) + "." + String.valueOf(ip2) + "." + String.valueOf(ip3) + "." + String.valueOf(ip4) + ":" +String.valueOf(ip5) + "/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        reg_bn = (Button)findViewById(R.id.btn_reg);
        Name = (EditText)findViewById(R.id.reg_name);
        Phone = (EditText)findViewById(R.id.reg_phone);
        UserName = (EditText)findViewById(R.id.reg_user_name);
        Password = (EditText)findViewById(R.id.reg_password);
        ConPassword = (EditText)findViewById(R.id.reg_con_password);
        builder = new AlertDialog.Builder(register.this);
        reg_bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = Name.getText().toString();
                phone = Phone.getText().toString();
                username = UserName.getText().toString();
                password = Password.getText().toString();
                conpass = ConPassword.getText().toString();

                if(name.equals("") || phone.equals("") || username.equals("") || password.equals("") || conpass.equals(""))
                {
                    builder.setTitle("Something went wrong ...");
                    builder.setMessage("Please fill all the fields ...");
                    displayAlert("input_error");
                }
                else
                {
                    if(!(password.equals(conpass)))
                    {
                        builder.setTitle("Something went wrong ...");
                        builder.setMessage("Your passwords are not matching ...");
                        displayAlert("input_error");
                    }
                    else
                    {
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, reg_url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONArray jsonArray = new JSONArray(response);
                                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                                            String code = jsonObject.getString("code");
                                            String message = jsonObject.getString("message");
                                            builder.setTitle("Server Response ...");
                                            builder.setMessage(message);
                                            displayAlert(code);
                                        } catch (JSONException e){

                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params = new HashMap<String, String>();
                                params.put("name",name);
                                params.put("email",phone);
                                params.put("user_name",username);
                                params.put("password",password);

                                return params;
                            }
                        };
                        MySingleton.getmInstance(register.this).addToRequestque(stringRequest);
                        //////////
                    }
                }
            }
        });

    }

    public void displayAlert(final String code)
    {
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(code.equals("input_error"))
                {
                    Password.setText("");
                    ConPassword.setText("");
                }
                else if(code.equals("reg_success"))
                {
                    finish();
                }
                else if(code.equals("reg_failed"))
                {
                    Name.setText("");
                    Phone.setText("");
                    UserName.setText("");
                    Password.setText("");
                    ConPassword.setText("");
                }
            }
        });
        AlertDialog alertDialog  = builder.create();
        alertDialog.show();
    }
}
