package com.example.learnabird;

import android.net.Uri;
import android.widget.TextView;

public class Utils {

    //validate textViews for null and empty
    public static boolean validteTextView(TextView txtview){
        if(txtview ==null || txtview.getText().toString().equals("")){
            return false;
        }
        return true;
    }

    //validate image uri for null and empty
    public static boolean validateImage(Uri uri){
        if(uri ==null || uri.equals("")){
            return false;
        }
        return true;
    }

    //validate string for null and equal
    public  static boolean validateSound(String url){
        if(url == null || url.equals("")){
            return false;
        }
        else{
            return true;
        }
    }

}
