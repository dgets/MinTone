package com.example.khelair.mintone;

import android.widget.Toast;
import android.content.Context;

/**
 * @author Damon Getsman
 * started: 24 Sept 17
 */
public class MyException extends Exception {
    /**
     * Generic exception
     * @param ouah
     * @param message
     */
    public MyException(Context ouah, String message) {
        super(message);
        Toast.makeText(ouah, message, Toast.LENGTH_SHORT).show();
    }
}
