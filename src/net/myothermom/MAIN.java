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
import net.zypr.api.API;
import net.zypr.api.vo.WeatherCurrentVO;

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
    private static final String USERNAME = "Joshua";
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
    
    private Button weatherButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        //create and set toggle button and buzzer
        mp = MediaPlayer.create(this, R.raw.buzzerloud);
        mp.setVolume(0.60f, 0.60f);
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
        
        //setup the weather button
        weatherButton = (Button)findViewById(R.id.weatherButton);
        weatherButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
               System.setProperty("net.zypr.api.key", "7f1b439cf0749751ca7c9fa8008d267d");

                try{
                   API.getInstance().getAuth().login("venkynary@gmail.com", "zypr2012");

                    WeatherCurrentVO weather = API.getInstance().getWeather().current("austin,tx");
                   Long currentTemperature = weather.getCurrentTemperature();
                  String description = weather.getDescription();
                    Toast.makeText(MAIN.this, "Saying: Weather report", Toast.LENGTH_LONG).show();
            tts.speak(description, TextToSpeech.QUEUE_ADD, null);

                    Toast.makeText(MAIN.this, "Saying: TEMPERATURE", Toast.LENGTH_LONG).show();
                    tts.speak(currentTemperature.toString() + "Degrees", TextToSpeech.QUEUE_ADD, null);

                    API.getInstance().getAuth().logout();
                   }catch(Exception exception){
                    System.err.println(exception);
                    }
                }

            });

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
        isOn = false;
        mp.pause();
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

            boolean hasMatch = false;
            for (String word : matches) {
                if (word.contains("father") && word.contains("sunflower")) {
                    hasMatch = true;
                    break;
                }
            }
            
            if (hasMatch) {
                tts.speak("Congratulations " + USERNAME + ", you are now awake", TextToSpeech.QUEUE_ADD, null);
            } else {
                mp.start();
            }

            isOn = !hasMatch;

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
                            tts.speak("What did your father teach you", TextToSpeech.QUEUE_ADD, null);
                        }

                    } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                    } catch(Exception e){
                    }
                }
        }
    }
}
