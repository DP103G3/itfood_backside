package tw.dp103g3.itfood_backside;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

public class Common {

    public static final String URL = "http://192.168.43.158:8080/Itfood_Web";

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
