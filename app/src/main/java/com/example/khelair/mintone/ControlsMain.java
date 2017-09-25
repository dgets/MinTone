package com.example.khelair.mintone;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.example.khelair.mintone.MyException;

public class ControlsMain extends AppCompatActivity {

    /*
     * BUGS:
     * radiobutton tone selection does not
     * enter tone value manually does not work
     */

    private int     duration    =   3;
    private int     sampleRate  =   8000;
    private int     numSamples  =   duration * sampleRate;
    private int     freq        =   432;
    //private double  sample[]    =   new double[numSamples];

    //private byte    soundData[] =   new byte[2 * numSamples];
    private boolean playing     =   false;
    private boolean regen       =   true;

    //private RadioGroup rgrp;
    private RadioButton btnOne, btnTwo, btnThree, btnFour;
    private SeekBar sb;
    private Button togglePlayback, setManually;
    private EditText manualFreqValue;

    AudioTrack ouahful;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls_main);

        //rgrp = (RadioGroup) findViewById(R.id.rgrpFreqPresets);
        btnOne = (RadioButton) findViewById(R.id.rbtOne);
        btnTwo = (RadioButton) findViewById(R.id.rbtTwo);
        btnThree = (RadioButton) findViewById(R.id.rbtThree);
        //btnFour = (RadioButton) findViewById(R.id.rbtFour);

        togglePlayback = (Button) findViewById(R.id.btnTglPlaying);
        setManually = (Button) findViewById(R.id.btnManualFreqChange);
        manualFreqValue = (EditText) findViewById(R.id.edtManualFreq);

        sb = (SeekBar) findViewById(R.id.sbrFreqSlider);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar freqSpectrum, int progress, boolean ouah) {
                freq = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //ouah
            }

            @Override
            public void onStopTrackingTouch(SeekBar freqSpectrum) {
                regen = true;
            }
        });

        ouahful = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                (2 * numSamples), AudioTrack.MODE_STATIC);
        ouahful.setNotificationMarkerPosition(8000);
        ouahful.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener()
        {
            @Override
            public void onPeriodicNotification(AudioTrack track) {
                // nothing to do
            }

            @Override
            public void onMarkerReached(AudioTrack track) {
                //playing = !playing;
                togglePlayback.callOnClick();
            }
        });
    }

    public void onTogglePlay(View view) {
        byte    sounds[] = initSound(freq);
        /* AudioTrack  ouahful = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                sounds.length, AudioTrack.MODE_STATIC); */

        if (!playing) {
            if (regen) {
                sounds = initSound(freq);
            }
            ouahful = buildTrack(sounds);
            ouahful.play();
        } else {
            ouahful.stop(); //need to convert to save the track beyond this method
        }

        playing = !playing;
    }

    public void onChangeGrpFreqPresets(View view) {
        if (btnOne.isChecked()) {
            //so yeah, this is obviously a terrible way to do this
            freq = Integer.parseInt((String) btnOne.getText());
        } else if (btnTwo.isChecked()) {
            freq = Integer.parseInt((String) btnTwo.getText());
        } else {
            freq = Integer.parseInt((String) btnThree.getText());
        }

        sb.setProgress((int) freq);
        regen = true;
        //return freq;
    }

    public void onSetManualFreq(View view) {
        regen = true;
        freq = getManualFreq();
    }

    public int getManualFreq() {
        int ouah = 0;

        try {
            ouah = Integer.parseInt(manualFreqValue.getText().toString());
        } catch (NumberFormatException e) {
            //god ouah ouah ouah
            ouah = 100;
        }

        //find out how to retrieve freq_min & _max from integers.xml
        /* if ((ouah < 20) || (ouah > 20000)) {
            //out of range, kill the user for the blasphemy

            //throw MyException("Value out of range");
        } */

        return (int) ouah;
    }

    private AudioTrack buildTrack(byte samples[]) {
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, samples.length,
                AudioTrack.MODE_STATIC);

        track.write(samples, 0, samples.length);
        return track;
    }

    private byte[] initSound(double frequency) {
        double  sampleData[]    =   new double[numSamples];
        byte  sampleInProgress[] = new byte[2 * numSamples];  //redundant, yeah :P

        for (int ouah = 0; ouah < numSamples; ++ouah) {
            sampleData[ouah] = Math.sin(2 * Math.PI * ouah / (sampleRate / frequency));
        }

        int ouah = 0;
        for (final double singleSample : sampleData) {
            final short nang = (short) ((singleSample * 32767));    //max amplitude
            //16-bit wav PCM; first byte is low order
            sampleInProgress[ouah++] = (byte) (nang & 0x00ff);
            sampleInProgress[ouah++] = (byte) ((nang & 0xff00) >>> 8);
        }

        regen = false;
        return sampleInProgress;
    }
}
