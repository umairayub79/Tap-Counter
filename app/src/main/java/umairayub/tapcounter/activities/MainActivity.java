package umairayub.tapcounter.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Locale;

import io.objectbox.Box;
import spencerstudios.com.ezdialoglib.Animation;
import spencerstudios.com.ezdialoglib.EZDialog;
import spencerstudios.com.ezdialoglib.EZDialogListener;
import spencerstudios.com.jetdblib.JetDB;
import umairayub.tapcounter.Count;
import umairayub.tapcounter.ObjectBox;
import umairayub.tapcounter.R;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_AUDIO_RECORDING = 111;
    //Declearing Variables
    ImageView mButtonPlus;
    ImageView mButtonMinus;
    ImageView mButtonSettings;
    ImageView mButtonSave;
    ImageView mButtonList;
    TextView mButtonReset;
    TextView mTvDisplayCount;
    TextView mDisplayVocalInstructions;
    Context context = MainActivity.this;
    int count = 0;
    boolean isSoundOn;
    boolean isVibrateOn;
    boolean isVocalOn;
    boolean speechIsAvailable;
    boolean isBigButtonModeOn;
    SharedPreferences sharedPreferences;
    MediaPlayer mp;
    BottomSheetDialog bottomSheetDialog;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mButtonMinus = (ImageView) findViewById(R.id.btn_minus);
        mButtonPlus = (ImageView) findViewById(R.id.btn_plus);
        mButtonSettings = (ImageView) findViewById(R.id.btn_setting);
        mButtonReset = (TextView) findViewById(R.id.btn_reset);
        mButtonList = (ImageView) findViewById(R.id.btn_list);
        mButtonSave = (ImageView) findViewById(R.id.btn_save);
        mTvDisplayCount = findViewById(R.id.tv_display_count);
        mDisplayVocalInstructions = findViewById(R.id.display_vocal_instructions);

        mp = MediaPlayer.create(context, R.raw.click);


        count = JetDB.getInt(context, "count", 0);
        mTvDisplayCount.setText(String.valueOf(count));

        mButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                handleTap();

            }
        });

        mButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                handleUntap();
            }
        });

        if (!isBigButtonModeOn) {
            mButtonReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handlereset();
                }
            });
        }


        mButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, SettingsActivity.class);
                startActivity(i);
            }
        });

        mButtonList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListActivity.class);
                startActivity(intent);
            }
        });

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                bottomSheetDialog.setContentView(R.layout.add_item);
                Button btn_d_add = bottomSheetDialog.findViewById(R.id.btn_d_add);
                Button btn_d_close = bottomSheetDialog.findViewById(R.id.btn_d_close);
                final EditText editText = bottomSheetDialog.findViewById(R.id.edt_d_countName);

                btn_d_add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String countName = editText.getText().toString();
                        if (editText.getText().toString().trim().equals("")) {
                            Toast.makeText(MainActivity.this, "Name cannot be blank", Toast.LENGTH_SHORT).show();
                        } else {

                            save(count, countName);
                            bottomSheetDialog.dismiss();
                        }

                    }
                });

                btn_d_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.show();
            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });

        setupRecognizer();

    }

    private boolean handlereset() {
        new EZDialog.Builder(context)
                .setAnimation(Animation.UP)
                .setTitle("Reset")
                .setMessage("Are you sure you want to reset counter?")
                .setPositiveBtnText("Yes")
                .setNegativeBtnText("No")
                .OnPositiveClicked(new EZDialogListener() {
                    @Override
                    public void OnClick() {
                        count = 0;
                        mTvDisplayCount.setText(String.valueOf(count));
                        JetDB.putInt(context, count, "count");

                    }
                })
                .OnNegativeClicked(new EZDialogListener() {
                    @Override
                    public void OnClick() {

                    }
                })
                .build();


        if (isSoundOn) {
            playSound();


        }
        if (isVibrateOn) {
            vibrate();
        }
        return true;
    }

    private void handleUntap() {
        if (count > 0) {
            count--;
            mTvDisplayCount.setText(String.valueOf(count));
            if (isSoundOn) {
                playSound();
            }
            if (isVibrateOn) {
                vibrate();
            }
            JetDB.putInt(context, count, "count");
        }
    }

    private void handleTap() {
        count++;
        mTvDisplayCount.setText(String.valueOf(count));
        if (isSoundOn) {
            playSound();
        }
        if (isVibrateOn) {
            vibrate();
        }
        JetDB.putInt(context, count, "count");
    }

    private void setupRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechIsAvailable = true;
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault());
        } else {
            new EZDialog.Builder(context)
                    .setTitle("Error!")
                    .setMessage("Voice Recognition Service is not Available \n Make sure Google App,Text to Speech are up-to date")
                    .setPositiveBtnText("Continue as is")
                    .setNegativeBtnText("Close App")
                    .OnNegativeClicked(new EZDialogListener() {
                        @Override
                        public void OnClick() {
                            finish();
                        }
                    })
                    .OnPositiveClicked(new EZDialogListener() {
                        @Override
                        public void OnClick() {
                            JetDB.putBoolean(context, false, "vocal");
                            mDisplayVocalInstructions.setVisibility(View.INVISIBLE);

                        }
                    })
                    .build();
        }

    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_AUDIO_RECORDING);

        } else {
            if (speechIsAvailable) {
                startRecognition();
            }

        }

    }

    private void startRecognition() {
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null) {
                    switch (matches.get(0)) {
                        case "tap":
                            handleTap();
                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                            Toast.makeText(context, "+1", Toast.LENGTH_SHORT).show();
                            break;
                        case "untap":
                            handleUntap();
                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                            Toast.makeText(context, "-1", Toast.LENGTH_SHORT).show();
                            break;
                        case "reset":
                            handlereset();
                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

                            break;

                        default:
                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                    }
                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSpeechRecognizer != null)
            mSpeechRecognizer.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSpeechRecognizer.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_AUDIO_RECORDING: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecognition();
                } else {
                    mDisplayVocalInstructions.setVisibility(View.INVISIBLE);
                    JetDB.putBoolean(context, false, "vocal");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

    private void playSound() {
        if (mp.isPlaying()) {
            mp.stop();
            mp.release();

            mp = MediaPlayer.create(context, R.raw.click);
            mp.start();
        } else {
            mp = MediaPlayer.create(context, R.raw.click);
            mp.start();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        isVibrateOn = sharedPreferences.getBoolean("vibrate", false);
        isSoundOn = sharedPreferences.getBoolean("sound", false);
        isVocalOn = sharedPreferences.getBoolean("vocal", false);
        isBigButtonModeOn = JetDB.getBoolean(context, "big", false);
        if (isVocalOn) {
            checkPermission();
            mDisplayVocalInstructions.setVisibility(View.VISIBLE);
        } else {
            mDisplayVocalInstructions.setVisibility(View.INVISIBLE);
        }

    }

    public void save(int count, String name) {
        Box<Count> CountBox = ObjectBox.get().boxFor(Count.class);
        Count count1 = new Count();
        count1.setCount(count);
        count1.setCountName(name);
        CountBox.put(count1);
        Toast.makeText(context, name + " Saved!", Toast.LENGTH_SHORT).show();
    }

}









