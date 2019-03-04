package com.example.recaptcha;

import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    String TAG = MainActivity.class.getSimpleName();
    Button btnverifyCaptcha;
    String SITE_KEY = "6LcoPpUUAAAAAHwXzsUA97iUaco0yOqQaYFZkcZT";
    //    String SITE_KEY = "6LdhSpUUAAAAAGReva41Hechcv8pPj0mEfpF3R5n";
//    String SECRET_KEY = "6LdhSpUUAAAAADSypnN1JhyQUCQxA6AQTvRN4BPg";
    String SECRET_KEY = "6LcoPpUUAAAAAIUp3KHJ-El2PrMksx4XPDWXw9K6";
    RequestQueue queue;

    APIInterface apiInterface;

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnverifyCaptcha = findViewById(R.id.button);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .build();

        mGoogleApiClient.connect();

        apiInterface = BaseRetrofit.getDefaultRetrofit().create(APIInterface.class);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        queue = Volley.newRequestQueue(MainActivity.this);

        btnverifyCaptcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SafetyNet.getClient(MainActivity.this).verifyWithRecaptcha(SITE_KEY).addOnSuccessListener(MainActivity.this, new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                        Log.e(TAG, "onSuccess: ");
                        if (!response.getTokenResult().isEmpty()) {
                            Log.e(TAG, "onSuccess: " + response.getTokenResult());
//                            handleSiteVerify(response.getTokenResult());

                            callApi(response.getTokenResult());
                        }
                    }
                }).addOnFailureListener(MainActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "Unknown type of error: " + e.getMessage());

                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            Log.e(TAG, "Error message: " +
                                    CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
                        } else {
                            Log.e(TAG, "Unknown type of error: " + e.getMessage());
                        }
                    }
                });



             /*   SafetyNet.SafetyNetApi.verifyWithRecaptcha(mGoogleApiClient, SITE_KEY)
                        .setResultCallback(new ResultCallback<SafetyNetApi.RecaptchaTokenResult>() {
                            @Override
                            public void onResult(SafetyNetApi.RecaptchaTokenResult result) {
                                Status status = result.getStatus();

                                if ((status != null) && status.isSuccess()) {

                                    if (!result.getTokenResult().isEmpty()) {
                                        callApi(result.getTokenResult());
                                    }
                                } else {

                                    Log.e(TAG, "error happened!" + status);

                                }
                            }
                        });

            }
        });*/


            }

            private void callApi(String tokenResult) {
                Call<TokenResponse> call = apiInterface.getResponse(SECRET_KEY, tokenResult);

                call.enqueue(new Callback<TokenResponse>() {
                    @Override
                    public void onResponse(Call<TokenResponse> call, retrofit2.Response<TokenResponse> response) {
                        Log.e(TAG, "onResponse: " + response);
                        if (response.body().getSuccess().equals("true")) {
                            Toast.makeText(getApplicationContext(), "You Are Not a Robot!!!", Toast.LENGTH_LONG).show();
                        }


                    }

                    @Override
                    public void onFailure(Call<TokenResponse> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.toString());
                    }
                });
            }


            protected void handleSiteVerify(final String responseToken) {

                Log.e(TAG, "handleSiteVerify: " + responseToken);

                //it is google recaptcha siteverify server
                //you can place your server url
                String url = "https://www.google.com/recaptcha/api/siteverify";
                StringRequest request = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.getBoolean("success")) {
                                        //code logic when captcha returns true Toast.makeText(getApplicationContext(),String.valueOf(jsonObject.getBoolean("success")),Toast.LENGTH_LONG).show();

                                        Toast.makeText(getApplicationContext(), String.valueOf(jsonObject.getBoolean("success")), Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), String.valueOf(jsonObject.getString("error-codes")), Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception ex) {
                                    Log.e(TAG, "JSON exception: " + ex.getMessage());

                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "Error message: " + error.getMessage());
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("secret", SECRET_KEY);
                        params.put("response", responseToken);
                        return params;
                    }
                };
                request.setRetryPolicy(new DefaultRetryPolicy(
                        50000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(request);
            }


        });

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
