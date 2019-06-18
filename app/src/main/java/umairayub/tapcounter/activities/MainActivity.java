package umairayub.tapcounter.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import io.objectbox.Box;
import spencerstudios.com.ezdialoglib.Animation;
import spencerstudios.com.ezdialoglib.EZDialog;
import spencerstudios.com.ezdialoglib.EZDialogListener;
import spencerstudios.com.jetdblib.JetDB;
import umairayub.tapcounter.Count;
import umairayub.tapcounter.ObjectBox;
import umairayub.tapcounter.R;

public class MainActivity extends AppCompatActivity {

    //Declearing Variables
    ImageView mButtonPlus;
    ImageView mButtonMinus;
    ImageView mButtonSettings;
    ImageView mButtonSave;
    ImageView mButtonList;
    TextView mButtonReset;
    TextView mTvDisplayCount;
    Context context = MainActivity.this;
    int count = 0;
    boolean isSoundOn;
    boolean isVibrateOn;
    SharedPreferences sharedPreferences;
    MediaPlayer mp;
    BottomSheetDialog bottomSheetDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ObjectBox.init(this);

        mButtonMinus = (ImageView) findViewById(R.id.btn_minus);
        mButtonPlus = (ImageView) findViewById(R.id.btn_plus);
        mButtonSettings = (ImageView) findViewById(R.id.btn_setting);
        mButtonReset = (TextView) findViewById(R.id.btn_reset);
        mButtonList = (ImageView) findViewById(R.id.btn_list);
        mButtonSave = (ImageView) findViewById(R.id.btn_save);
        mTvDisplayCount = findViewById(R.id.tv_display_count);

        mp = MediaPlayer.create(context,R.raw.click);


        count = JetDB.getInt(context, "count", 0);
        mTvDisplayCount.setText(String.valueOf(count));

        mButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        mButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            }
        });

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
                        if (editText.getText().toString().equals("")){
                            Toast.makeText(MainActivity.this, "Name cannot be blank", Toast.LENGTH_SHORT).show();
                        }else {

                            save(count, countName);
                            bottomSheetDialog.dismiss(); }
                        
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
    }


    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

    private void playSound() {
        if (mp.isPlaying()){
            mp.stop();
            mp.release();

            mp = MediaPlayer.create(context,R.raw.click);
            mp.start();
        }else{
            mp = MediaPlayer.create(context,R.raw.click);
            mp.start();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        isVibrateOn = sharedPreferences.getBoolean("vibrate", false);
        isSoundOn = sharedPreferences.getBoolean("sound", false);

    }

    public void save(int count, String name) {
        Box<Count> CountBox = ObjectBox.get().boxFor(Count.class);
        Count count1 = new Count();
        count1.setCount(count);
        count1.setCountName(name);
        CountBox.put(count1);
        Toast.makeText(context, name + " Saved!", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObjectBox.boxStore.close();
    }
}