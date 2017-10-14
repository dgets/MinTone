package com.example.khelair.mintone;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Damon Getsman
 * started: 24 Sept 17
 * finished:
 *
 * This little app, at least upon conception through alpha, is an extremely
 * minimalistic tone generator for 'droid phones. It features three preset
 * radio buttons, a manual numeric entry field, and a seekbar for different
 * frequency selection options.
 *
 * It would be good, at some point, to try to implement this in a fashion
 * capable of audio playback, while other audio resources are being utilized,
 * as well. I believe that's about it for now...
 */
public class ControlsMain extends AppCompatActivity {

    //a little bit more of a limited scope would be nice
    private double  freq;
    private int     duration    =   3;
    private int     sampleRate  =   8000;
    private int     numSamples  =   duration * sampleRate;
    private int     max_freq, min_freq;
    private boolean playing     =   false;
    private boolean regen       =   true;
    private boolean continuous  =   false;
    //private boolean customSet   =   false;

    //controls
    private RadioGroup rgrp;
    //protected RadioButton[] rbts;
    private SeekBar sbFreq, sbVol;
    private Button btnTogglePlayback, btnSetManually;
    private EditText manualFreqValue;
    private TextView freqSelectedView;
    private CheckBox cbxLoop;

    AudioTrack ouahful;
    AudioManager audioBoss;
    Context appShit;

    /**
     * @param savedInstanceState - Bundle (see also every 'droid GUI app)
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls_main);

        //'constants'

        rgrp = (RadioGroup) findViewById(R.id.rgrpFreqPresets);
        /*rbts[0] = (RadioButton) findViewById(R.id.rbtOne);
        rbts[1] = (RadioButton) findViewById(R.id.rbtTwo);
        rbts[2] = (RadioButton) findViewById(R.id.rbtThree);*/
        btnTogglePlayback = (Button) findViewById(R.id.btnTglPlaying);
        btnSetManually = (Button) findViewById(R.id.btnManualFreqChange);
        btnSetManually.setEnabled(false);
        manualFreqValue = (EditText) findViewById(R.id.edtManualFreq);
        freqSelectedView = (TextView) findViewById(R.id.txtFreqSelected);
        cbxLoop = (CheckBox) findViewById(R.id.cbxContinuous);

        freq = getResources().getInteger(R.integer.freq1);
        max_freq = getResources().getInteger(R.integer.freq_max);
        min_freq = getResources().getInteger(R.integer.freq_min);

        sbFreq = (SeekBar) findViewById(R.id.sbrFreqSlider);
        sbFreq.setProgress((int) freq);
        sbFreq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar freqSpectrum, int progress, boolean ouah) {
                if ((progress >= (int) (freq + 5)) || (progress <= (int) (freq - 5))) {
                    freq = sbFreq.getProgress();
                    int nakk = (int) freq;
                    freqSelectedView.setText((String.valueOf(nakk)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar freqSpectrum) {
                rgrp.clearCheck();
                btnSetManually.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar freqSpectrum) {
                /*
                 * not sure why, but ever since changing freq to a double for
                 * greater precision, the seekbar frequency selection has been
                 * totally fucked
                 */
                freq = (double) sbFreq.getProgress();
                freqSelectedView.setText(Double.toString(freq)); //do properly
                regen = true;

                //now we need to stop the playing tone (if any), rebuild, and start the new tone
                //actually, rebuilding in the background would be best before stopping
                if (playing) {
                    AudioTrack newTrack = buildTrack(initSound(freq));
                    ouahful.stop();
                    ouahful = newTrack;
                    setATListeners();
                    /*if (continuous) {
                        ouahful.setLoopPoints(0, sampleRate, -1);
                    }*/
                    ouahful.play();
                }
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

        //need to find out what to use instead of the deprecated AudioTrack
        ouahful = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                (2 * numSamples), AudioTrack.MODE_STATIC);
        audioBoss = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        freqSelectedView.setText(getResources().getString(R.string.text_using_preset));
    }

    /*
     *  Widget controls
     *  @param view - (see also every method in the action class)
     *  @throws MyException
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

            setATListeners();
            btnTogglePlayback.setText(getResources().getString(R.string.text_on));
            ouahful.play();
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

    /**
     *
     * @param view View necessary boilerplate
     */
    public void onClickGrpFreqPresets(View view) {
        manualFreqValue.setEnabled(false); manualFreqValue.setEnabled(true);
        freqSelectedView.setText(getResources().getString(R.string.text_using_preset));
        btnSetManually.setEnabled(false);
    }

    /**
     *
     * @param view View necessary boilerplate
     */
    public void onPresetFreqClick(View view) {
        manualFreqValue.setEnabled(false); manualFreqValue.setEnabled(true);
        freqSelectedView.setText(getResources().getString(R.string.text_using_preset));
        btnSetManually.setEnabled(false);

        //if (!customSet) {
            switch (rgrp.getCheckedRadioButtonId()) {
                case R.id.rbtOne:
                    freq = (double) getResources().getInteger(R.integer.freq1);
                    break;
                case R.id.rbtTwo:
                    freq = (double) getResources().getInteger(R.integer.freq2);
                    break;
                case R.id.rbtThree:
                    freq = (double) getResources().getInteger(R.integer.freq3);
                    break;
            }
        //}

        sbFreq.setProgress((int) freq);
        regen = true;

        if (playing) {
            AudioTrack newTrack = buildTrack(initSound(freq));
            ouahful.stop();
            ouahful = newTrack;
            setATListeners();
            ouahful.play();
        }
    }

    /**
     *
     * @param view View necessary boilerplate
     * @throws MyException ouah
     */
    public void onSetManualFreq(View view) throws MyException {
        try {
            freq = getManualFreq();
        } catch (Exception e) {
            manualFreqValue.setEnabled(false);
            manualFreqValue.setEnabled(true);
            freqSelectedView.setText(
                    getResources().getString(R.string.text_using_preset));
            throw new MyException(appShit, "Unable to fetch frequency");
        }

        regen = true;
        sbFreq.setProgress((int) freq);
        freqSelectedView.setText(Double.toString(freq));    //fix when able
        rgrp.clearCheck();

        manualFreqValue.setEnabled(false); manualFreqValue.setEnabled(true);

        if (playing) {
            AudioTrack newTrack = buildTrack(initSound(freq));
            ouahful.stop();
            ouahful = newTrack;
            setATListeners();
            ouahful.play();
        }
    }

    /**
     *
     * @param view View boilerplate
     */
    public void onClickManualFreqEntry(View view) {
        manualFreqValue.setEnabled(true);
        btnSetManually.setEnabled(true);
    }

    /**
     *
     * @param view View boilerplate
     */
    public void onContinuousPlaybackClick(View view) {
        continuous = cbxLoop.isChecked();
        if (playing) {
            ouahful.stop();
            btnTogglePlayback.setText(getResources().getString(R.string.text_off));
            ouahful.setLoopPoints(0, sampleRate, 0);
        }
    }

    //Methods mein
    /**
     * Builds track from samples array
     * @param samples - byte array
     * @return track - AudioTrack
     */
    private AudioTrack buildTrack(byte samples[]) {
        //fix deprecated AudioTrack issue
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, samples.length,
                AudioTrack.MODE_STATIC);

        track.write(samples, 0, samples.length);
        return track;
    }

    /**
     * initial [arbitrary durational] tone generation
     * @param frequency - double value in Hz
     * @return samples byte array
     */
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

    /**
     *
     * @return double
     * @throws MyException ouah
     */
    public double getManualFreq() throws MyException {
        double ouah = 0;

        try {
            ouah = Double.parseDouble(manualFreqValue.getText().toString());
        } catch (NumberFormatException e) {
            manualFreqValue.setText(Double.toString(freq)); //fix when able
            //throw new MyException(appShit, e.getMessage());
        }

        if ((ouah >= min_freq) && (ouah <= max_freq)) {
            return ouah;
        } else if (ouah < min_freq) {
            //out of range, kill the user for the blasphemy
            Toast.makeText(getApplicationContext(),
                    "Value too low (<" + min_freq + ")",
                    Toast.LENGTH_SHORT).show();
        } else {
            //ditto
            Toast.makeText(getApplicationContext(),
                    "Value too high (>" + max_freq + ")",
                    Toast.LENGTH_SHORT).show();
        }

        manualFreqValue.setText("");
        return freq;
    }

    /**
     *
     * @throws MyException ouah
     */
    /*public void writeConf() throws MyException {
        FileOutputStream confFile;

        try {
            confFile = openFileOutput(getResources().getText(R.string.conf_file_name).toString(),
                    MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            throw new MyException(appShit, "Can't open conf. Wut?");
        }

    }*/

    private void setATListeners() {
        ouahful.setPlaybackPositionUpdateListener(
            new AudioTrack.OnPlaybackPositionUpdateListener() {
                    @Override
                    public void onPeriodicNotification(AudioTrack track) {
                        // nothing to do
                    }

                    @Override
                    public void onMarkerReached(AudioTrack track) {
                        if (playing && !continuous) {
                            ouahful.stop();
                            btnTogglePlayback.setText(
                                    getResources().getString(
                                            R.string.text_off));
                            playing = !playing;
                        }
                    }
            });

            if (!continuous) {
                ouahful.setLoopPoints(0, sampleRate, 0);
            } else {
                ouahful.setLoopPoints(0, sampleRate, -1);
            }
    }
}
