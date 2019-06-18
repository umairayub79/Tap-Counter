package umairayub.tapcounter.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import umairayub.tapcounter.Count;
import umairayub.tapcounter.R;

public class CountListAdapter  extends ArrayAdapter<Count> {

    Activity context;
    List<Count> countList;
    public CountListAdapter(Activity context, List<Count> countList) {
        super(context, R.layout.list_item,countList);

        this.context = context;
        this.countList = countList;

    }

    public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item,null,true);

        TextView textViewCountName = (TextView) rowView.findViewById(R.id.list_tv_CountName);
        TextView textViewCount = (TextView) rowView.findViewById(R.id.list_tv_Count);

        int count = countList.get(position).getCount();
        String countName = countList.get(position).getCountName();

        textViewCount.setText(String.valueOf(count));
        textViewCountName.setText(countName);

        return rowView;

    }

    @Override
    public int getCount() {
        return countList.size();
    }
}
