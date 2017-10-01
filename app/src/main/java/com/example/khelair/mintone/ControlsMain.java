package com.example.khelair.mintone;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

//import com.example.khelair.mintone.MyException;

public class ControlsMain extends AppCompatActivity {

    //a little bit more of a limited scope would be nice
    private int     duration    =   3;
    private int     sampleRate  =   8000;
    private int     numSamples  =   duration * sampleRate;
    private int     freq, max_freq, min_freq;
    private boolean playing     =   false;
    private boolean regen       =   true;

    //controls
    private RadioGroup rgrp;
    private SeekBar sbFreq, sbVol;
    private Button btnTogglePlayback, btnSetManually;
    private EditText manualFreqValue;
    private TextView freqSelectedView;

    AudioTrack ouahful;
    AudioManager audioBoss;
    Context appShit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls_main);

        rgrp = (RadioGroup) findViewById(R.id.rgrpFreqPresets);
        btnTogglePlayback = (Button) findViewById(R.id.btnTglPlaying);
        btnSetManually = (Button) findViewById(R.id.btnManualFreqChange);
        btnSetManually.setEnabled(false);
        manualFreqValue = (EditText) findViewById(R.id.edtManualFreq);
        freqSelectedView = (TextView) findViewById(R.id.txtFreqSelected);

        freq = getResources().getInteger(R.integer.freq1);
        max_freq = getResources().getInteger(R.integer.freq_max);
        min_freq = getResources().getInteger(R.integer.freq_min);

        sbFreq = (SeekBar) findViewById(R.id.sbrFreqSlider);
        sbFreq.setProgress(freq);
        sbFreq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar freqSpectrum, int progress, boolean ouah) {
                //freq = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar freqSpectrum) {
                rgrp.clearCheck();
                btnSetManually.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar freqSpectrum) {
                freq = sbFreq.getProgress();
                freqSelectedView.setText(Integer.toString(freq));
                regen = true;
            }
        });
        sbVol = (SeekBar) findViewById(R.id.sbrVolSlider);
        sbVol.setProgress(getResources().getInteger(R.integer.vol_start));
        sbVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int volStep;

            @Override
            public void onProgressChanged(SeekBar volSpectrum, int progress, boolean ouah) {
                //ouah
            }

            @Override
            public void onStartTrackingTouch(SeekBar volSpectrum) {
                btnSetManually.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar volSpectrum) {
                volStep = (audioBoss.getStreamMaxVolume(AudioManager.STREAM_MUSIC) /
                        (getResources().getInteger(R.integer.vol_max) -
                                getResources().getInteger(R.integer.vol_min)));
                audioBoss.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (sbVol.getProgress() * volStep), 0);
            }
        });

        ouahful = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                (2 * numSamples), AudioTrack.MODE_STATIC);
        audioBoss = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        freqSelectedView.setText(getResources().getString(R.string.text_using_preset));
    }

    /*
     *  Widget controls
     */
    public void onTogglePlay(View view) throws MyException {
        byte    sounds[] = initSound(freq);
        /* AudioTrack  ouahful = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                sounds.length, AudioTrack.MODE_STATIC); */

        if (!playing) {
            if (regen) {
                sounds = initSound(freq);
            }
            ouahful = buildTrack(sounds);
            ouahful.setNotificationMarkerPosition(numSamples);

            //should this be handled here?  probably not, methinks
            ouahful.setPlaybackPositionUpdateListener(
             new AudioTrack.OnPlaybackPositionUpdateListener()
              {
                @Override
                public void onPeriodicNotification(AudioTrack track) {
                    // nothing to do
                }

                @Override
                public void onMarkerReached(AudioTrack track) {
                    if (playing) {
                        ouahful.stop();
                        btnTogglePlayback.setText(getResources().getString(R.string.text_off));
                        playing = !playing;
                    }
                }
              });
            btnTogglePlayback.setText(getResources().getString(R.string.text_on));
            try {
                ouahful.play();
            } catch (Exception e) {
                btnTogglePlayback.setText(getResources().getString(R.string.text_off));
                throw new MyException(appShit, "Playback Error");
            }
        } else {
            btnTogglePlayback.setText(getResources().getString(R.string.text_off));
            ouahful.stop(); //need to convert to save the track beyond this method
        }

        if ((freq != getResources().getInteger(R.integer.freq1)) &&
                (freq != getResources().getInteger(R.integer.freq2)) &&
                (freq != getResources().getInteger(R.integer.freq3))) {
            rgrp.clearCheck();
        }

        btnSetManually.setEnabled(false);
        playing = !playing;
    }

    public void onClickGrpFreqPresets(View view) {
        manualFreqValue.setEnabled(false); manualFreqValue.setEnabled(true);
        freqSelectedView.setText(getResources().getString(R.string.text_using_preset));
        btnSetManually.setEnabled(false);
    }

    public void onPresetFreqClick(View view) {
        manualFreqValue.setEnabled(false); manualFreqValue.setEnabled(true);
        freqSelectedView.setText(getResources().getString(R.string.text_using_preset));
        btnSetManually.setEnabled(false);

        switch (rgrp.getCheckedRadioButtonId()) {
            case R.id.rbtOne:
                freq = getResources().getInteger(R.integer.freq1);
                break;
            case R.id.rbtTwo:
                freq = getResources().getInteger(R.integer.freq2);
                break;
            case R.id.rbtThree:
                freq = getResources().getInteger(R.integer.freq3);
                break;
        }

        sbFreq.setProgress(freq);
        regen = true;
    }

    public void onSetManualFreq(View view) throws MyException {
        try {
            freq = getManualFreq();
        } catch (Exception e) {
            manualFreqValue.setEnabled(false); manualFreqValue.setEnabled(true);
            freqSelectedView.setText(getResources().getString(R.string.text_using_preset));
            throw new MyException(appShit, "Unable to fetch frequency");
        }

        regen = true;
        sbFreq.setProgress(freq);
        freqSelectedView.setText(Integer.toString(freq));
        rgrp.clearCheck();

        manualFreqValue.setEnabled(false); manualFreqValue.setEnabled(true);
    }

    public void onClickManualFreqEntry(View view) {
        manualFreqValue.setEnabled(true);
        btnSetManually.setEnabled(true);
    }

    /*
     * Methods mein
     */
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

    public int getManualFreq() throws MyException {
        int ouah;

        try {
            ouah = Integer.parseInt(manualFreqValue.getText().toString());
        } catch (NumberFormatException e) {
            manualFreqValue.setText(Integer.toString(freq));
            throw new MyException(appShit, e.getMessage());
        }

        if ((ouah >= min_freq) && (ouah <= max_freq)) {
            return ouah;
        } else if (ouah < min_freq) {
            //out of range, kill the user for the blasphemy
            Toast.makeText(getApplicationContext(), "Value too low (<" + min_freq + ")",
                    Toast.LENGTH_SHORT).show();
        } else {
            //ditto
            Toast.makeText(getApplicationContext(), "Value too high (>" + max_freq + ")",
                    Toast.LENGTH_SHORT).show();
        }

        manualFreqValue.setText("");
        return freq;
    }

    public void writeConf() throws MyException {
        FileOutputStream confFile;

        try {
            confFile = openFileOutput(getResources().getText(R.string.conf_file_name).toString(),
                    MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            throw new MyException(appShit, "Can't open conf. Wut?");
        }


    }
}
