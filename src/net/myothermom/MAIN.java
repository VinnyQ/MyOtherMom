package net.myothermom;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by IntelliJ IDEA.
 * User: vinny.ly
 * Date: 3/9/12
 * Time: 11:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class MAIN extends Activity implements TextToSpeech.OnInitListener {
    private int MY_DATA_CHECK_CODE = 0;

        private TextToSpeech tts;
        private boolean isOn = false;

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);

            EditText inputText = (EditText) findViewById(R.id.input_text);
            Button speakButton = (Button) findViewById(R.id.speak_button);

            speakButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = inputText.getText().toString();
                    if (text!=null && text.length()>0) {
                        Toast.makeText(MAIN.this, "Saying: " + text, Toast.LENGTH_LONG).show();
                        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
                    }
                }
            });

            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

            final MediaPlayer mp = MediaPlayer.create(this, R.raw.buzzerloud);
            mp.setLooping(true);
            final Button toggleButton = (Button) findViewById(R.id.toggleButton);

             button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isOn = !isOn;

                if (isOn) {
                    mp.start();
                } else {
                    mp.pause();
                }
            }
        });

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
}
