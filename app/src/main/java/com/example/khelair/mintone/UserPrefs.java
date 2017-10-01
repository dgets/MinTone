package com.example.khelair.mintone;

/* import com.example.khelair.mintone.MyException;

import android.content.Context;
import android.content.res.Resources;
//import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;

import static android.content.Context.MODE_PRIVATE; */
//import java.io.File;

/**
 * Created by khelair on 9/30/17.
 */

public class UserPrefs {
    //private int     presetsLimit = 3;

    private int     presets[];
    private int     duration;

    //getters & setters
    public void setPresets(int freqs[]) {
        this.presets = freqs;
    }

    public int[] getPresets() {
        return this.presets;
    }

    public void setDuration(int toneTime) {
        this.duration = toneTime;
    }

    public int getDuration() {
        return this.duration;
    }

    //methods
}
