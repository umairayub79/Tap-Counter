package umairayub.tapcounter.activities;

;import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.Query;
import spencerstudios.com.ezdialoglib.Animation;
import spencerstudios.com.ezdialoglib.EZDialog;
import spencerstudios.com.ezdialoglib.EZDialogListener;
import spencerstudios.com.ezdialoglib.Font;
import umairayub.tapcounter.Count;
import umairayub.tapcounter.Count_;
import umairayub.tapcounter.ObjectBox;
import umairayub.tapcounter.R;
import umairayub.tapcounter.adapter.CountListAdapter;


public class ListActivity extends AppCompatActivity {

    TextView tv_noSavedCounts;
    ListView listView;
    CountListAdapter countListAdapter;
    private Query<Count> countQuery;
    Box<Count> countBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView = (ListView) findViewById(R.id.listView);
        tv_noSavedCounts = (TextView) findViewById(R.id.tv_noSavedCounts);
        countBox = ObjectBox.get().boxFor(Count.class);
        countQuery = countBox.query().order(Count_.countName).build();

        UpdateCounts();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShowDialog(position);
                
            }
        });
    }
    public void UpdateCounts(){
        List<Count> countList = countQuery.find();
        countListAdapter = new CountListAdapter(ListActivity.this,countList);
        listView.setAdapter(countListAdapter);
        int size = countListAdapter.getCount();
        if (size != 0){
            listView.setVisibility(View.VISIBLE);
            tv_noSavedCounts.setVisibility(View.GONE);
        }else {
            listView.setVisibility(View.GONE);
            tv_noSavedCounts.setVisibility(View.VISIBLE);

        }
    }

    public void ShowDialog(final int position){
        new EZDialog.Builder(ListActivity.this)
                .setMessage("Do you want to delete this count?")
                .setTitle("Delete")
                .setPositiveBtnText("YES")
                .setNegativeBtnText("No")
                .setAnimation(Animation.UP)
                .setFont(Font.COMFORTAA)

                .OnPositiveClicked(new EZDialogListener() {
                    @Override
                    public void OnClick() {
                        Count count = countListAdapter.getItem(position);
                        countBox.remove(count);
                        UpdateCounts();
                        Toast.makeText(ListActivity.this, "Count Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .OnNegativeClicked(new EZDialogListener() {
                    @Override
                    public void OnClick() {

                    }
                })
                .build();

    }
}
