package net.myothermom;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: vinny.ly
 * Date: 3/9/12
 * Time: 11:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class MAIN extends Activity implements TextToSpeech.OnInitListener {
    private int MY_DATA_CHECK_CODE = 0;
    private static final int REQUEST_CODE = 1234;
    private ListView wordsList;

    private TextToSpeech tts;
    boolean isOn = false;

    Calendar calendar;
    MediaPlayer mp;

    EditText inputText;
    Button speakButton;

    EditText alarm_time;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //set initial alarm_time
        alarm_time = (EditText) findViewById(R.id.alarm_time);
        calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MINUTE, 10);

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String formattedTime = df.format(startDate);
        alarm_time.setText(formattedTime);

        //create and set toggle button and buzzer
        mp = MediaPlayer.create(this, R.raw.buzzerloud);
        mp.setVolume(0.65f, 0.65f);
        mp.setLooping(true);
        final Button toggleButton = (Button) findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isOn = !isOn;
                    if (isOn) {
                        mp.start();
                    } else {
                    mp.pause();
                }
            }
        });

        //setup clock
        //make clock update constantly
        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread= new Thread(runnable);
        myThread.start();

        //setup voice rec button
        Button speakButton = (Button) findViewById(R.id.speakButton);
        wordsList = (ListView) findViewById(R.id.list);

        // Disable button if no recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
            speakButton.setEnabled(false);
            speakButton.setText("Recognizer not present");
        }

        //set Intent
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    };

    /**
     * Handle the action of the button being clicked
     */
    public void speakButtonClicked(View v)
    {
        startVoiceRecognitionActivity();
    }

    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        startActivityForResult(intent, REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                tts = new TextToSpeech(this, this);
            }
            else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            wordsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    matches));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Toast.makeText(MAIN.this,
                    "Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
        }
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(MAIN.this,
                    "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
        }
    }

    public void setClockText() {
        runOnUiThread(new Runnable() {
        public void run() {
            try{
                TextView txtCurrentTime= (TextView)findViewById(R.id.lbltime);
                    Date dt = new Date();
                    int hours = dt.getHours();
                    int minutes = dt.getMinutes();
                    int seconds = dt.getSeconds();
                    String curTime = hours + ":" + minutes + ":"+ seconds;
                    txtCurrentTime.setText(curTime);

                    //set off alarm
                    Date alarmVal = new Date(alarm_time.toString());
                    if (dt.after(alarmVal)) {
                        isOn = true;
                    }
            }catch (Exception e) {}
            }
        });
    }

    class CountDownRunner implements Runnable{
        // @Override
        public void run() {
                while(!Thread.currentThread().isInterrupted()){
                    try {
                        setClockText();
                        Thread.sleep(3000);

                        if (isOn) {
                            tts.speak("Time to wake up.", TextToSpeech.QUEUE_ADD, null);
                        }

                    } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                    } catch(Exception e){
                    }
                }
        }
    }
}
