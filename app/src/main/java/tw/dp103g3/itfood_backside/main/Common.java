package tw.dp103g3.itfood_backside.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Common {

    public static final String TAG = "TAG_Common";
    public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    public static final String PREFERENCES_ADMIN = "admin";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static int getAdminId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_ADMIN, Context.MODE_PRIVATE);
        return pref.getInt("adminId", 0);
    }

    public static boolean networkConnected(Activity activity) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (connectivityManager == null) {
                return false;
            }
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities actNetWork =
                    connectivityManager.getNetworkCapabilities(network);
            if (actNetWork != null) {
                return actNetWork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        actNetWork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            } else {
                return false;
            }
        } else {
            NetworkInfo networkInfo =
                    connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    public static void ShowToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void ShowToast(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }

    public static void setDialogUi(Dialog dialog, Activity activity) {
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(activity.getWindow().getDecorView().getSystemUiVisibility());
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                wm.updateViewLayout(dialog.getWindow().getDecorView(), dialog.getWindow().getAttributes());
            }
        });
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


}
