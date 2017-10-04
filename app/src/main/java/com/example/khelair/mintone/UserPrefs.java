package com.example.khelair.mintone;

/**
 * @author Damon Getsman
 */
public class UserPrefs {
    //private int     presetsLimit = 3;

    private int     presets[];
    private int     duration;
    private int     cntr = 0;

    //getters & setters
    /* public void setPresets(int freqs[]) {
        do {
            ControlsMain.rbts[cntr]
        }
        this.presets = freqs;
    } */

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
