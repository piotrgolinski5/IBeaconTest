package pl.gg.ibeacontest.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.gg.ibeaconlibrary.IBeacon;
import pl.gg.ibeacontest.R;

/**
 * Created by test on 28.10.2015.
 */
public class IBeaconListAdapter extends BaseAdapter {
    private List<IBeacon> mList;
    private LayoutInflater mInflater;
    public IBeaconListAdapter(Context context){
        mList = new ArrayList<>();
        mInflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public class Holder
    {
        TextView tv;

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_ibeacon_list, null);

            holder = new Holder();
            holder.tv = (TextView) convertView.findViewById(R.id.adapter_ibeacon_list_tvText);

            convertView.setTag(holder);
        } else {
            holder = (Holder)convertView.getTag();
        }
        holder.tv.setText(mList.get(position).toString());
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    public void setList(List<IBeacon> list){
        mList = list;
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged(){
        super.notifyDataSetChanged();
    }
}
