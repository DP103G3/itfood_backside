package tw.dp103g3.itfood_backside.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood_backside.R;
import tw.dp103g3.itfood_backside.task.CommonTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG_LoginFragment";
    private static final String reEmail = "^\\w+((-\\w+)|(.\\w+))*@[A-Za-z0-9]+((\\.|\\-)[A-Za-z0-9]+)*\\.[A-Za-z]+$";
    private EditText etEmail, etPassword;
    private Button btLogin;
    private String textEmail, textPassword;
    private CommonTask loginTask;
    private TextView tvAdminInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btLogin = findViewById(R.id.btLogin);
        tvAdminInput = findViewById(R.id.tvAdminInput);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                textEmail = etEmail.getText().toString().trim().toLowerCase();
                etEmail.setText(textEmail);
                if (textEmail.isEmpty()) {
                    etEmail.setError(getString(R.string.textInputEmail));
                } else if (!textEmail.matches(reEmail)) {
                    etEmail.setError(getString(R.string.textEmailFormateError));
                }
            }
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            textPassword = etPassword.getText().toString().trim();
            if (!hasFocus) {
                if (textPassword.isEmpty()) {
                    etPassword.setError(getString(R.string.textInputPassword));
                }
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = Url.URL + "/ShopServlet";
                textEmail = etEmail.getText().toString().trim().toLowerCase();
                textPassword = etPassword.getText().toString().trim();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "login");
                jsonObject.addProperty("email", textEmail);
                jsonObject.addProperty("password", textPassword);
                loginTask = new CommonTask(url, jsonObject.toString());
                int shopId = 0;
                try {
                    String result = loginTask.execute().get();
                    shopId = Integer.parseInt(result);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                if (shopId != 0) {
                    SharedPreferences pref =
                            getSharedPreferences(Common.PREFERENCES_ADMIN, Context.MODE_PRIVATE);
                    pref.edit().putString("email", textEmail)
                            .putString("password", textPassword).putInt("shopId", shopId).apply();

                }

                Intent intent = new Intent(MainActivity.this, MainPageActivity.class);
                startActivity(intent);
            }
        });

        //設定按下文字後輸入預設管理帳號
        tvAdminInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etEmail.setText("administrator@gmail.com");
                etPassword.setText("123456789");
            }
        });
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onStop() {
        super.onStop();
        if (loginTask != null) {
            loginTask.cancel(true);
            loginTask = null;
        }
    }
}
