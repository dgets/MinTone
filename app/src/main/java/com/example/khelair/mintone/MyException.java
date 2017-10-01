package com.example.khelair.mintone;

import android.widget.Toast;
import android.content.Context;

/**
 * Created by khelair on 9/24/17.
 */

public class MyException extends Exception {
    public MyException(Context ouah, String message) {
        super(message);
        Toast.makeText(ouah, message, Toast.LENGTH_SHORT).show();
    }
}
