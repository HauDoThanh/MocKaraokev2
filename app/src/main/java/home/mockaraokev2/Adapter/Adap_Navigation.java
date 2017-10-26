package home.mockaraokev2.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import home.mockaraokev2.Object.NaviObject;
import home.mockaraokev2.R;

/**
 Created by Admin on 5/23/2017.
 */

public class Adap_Navigation extends BaseExpandableListAdapter {

    private final Context context;
    private final List<NaviObject> head;
    private final HashMap<String, List<String>> listHashMap;

    public Adap_Navigation(Context context, List<NaviObject> head, HashMap<String, List<String>> listHashMap) {
        this.context = context;
        this.head = head;
        this.listHashMap = listHashMap;
    }

    @Override
    public int getGroupCount() {
        return head.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listHashMap.get(head.get(groupPosition).getTitle()) == null ? 0 : listHashMap.get(head.get(groupPosition).getTitle()).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return head.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listHashMap.get(head.get(groupPosition).getTitle()).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item = inflater.inflate(R.layout.item_group_navi, parent, false);

        ImageView imageView = item.findViewById(R.id.imageNavigation);
        TextView tvTitle = item.findViewById(R.id.tvTitle);
        TextView tvCount = item.findViewById(R.id.tvCount);

        imageView.setImageResource(head.get(groupPosition).getSrc());
        tvTitle.setText(head.get(groupPosition).getTitle());
        tvCount.setText(head.get(groupPosition).getCount());
        return item;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item = inflater.inflate(R.layout.item_child_navi, parent, false);

        TextView tvType = item.findViewById(R.id.tvType);
        tvType.setText(getChild(groupPosition,childPosition).toString());
        return item;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
