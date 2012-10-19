package com.quanleimu.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.View;
import android.widget.TextView;

import com.quanleimu.activity.R;
import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.values;
import com.quanleimu.view.fragment.MultiLevelSelectionFragment;
import com.quanleimu.view.fragment.PostParamsHolder;
import com.quanleimu.view.fragment.MultiLevelSelectionFragment.MultiLevelItem;

/**
 * 
 * @author liuchong
 *
 */
public class FilterUtil {

	public static interface FilterSelectListener
	{
		public void onItemSelect(MultiLevelItem item);
		public void onCancel();
	}
	
	public static class CustomizeItem extends MultiLevelSelectionFragment.MultiLevelItem
	{
		
	}

	public static void startSelect(Context context, CustomizeItem[] customizeItems, Filterss fss, final FilterSelectListener listener)
	{
		String title = "选择" + fss.getDisplayName();
		
		final int skipCount = customizeItems == null ? 0 : customizeItems.length;
		
		final ArrayList<MultiLevelSelectionFragment.MultiLevelItem> items = new ArrayList<MultiLevelSelectionFragment.MultiLevelItem>();
		MultiLevelSelectionFragment.MultiLevelItem head = new MultiLevelSelectionFragment.MultiLevelItem();
		head.txt = "所有" + fss.getDisplayName();
		head.id = "";
		items.add(head);
		if (skipCount != 0)
		{
			for (int i=0; i<skipCount; i++)
			{
				items.add(customizeItems[i]);
			}
		}
		
		for (int i = 0; i < fss.getLabelsList()
				.size(); ++i) {
			MultiLevelSelectionFragment.MultiLevelItem t = new MultiLevelSelectionFragment.MultiLevelItem();
			t.txt = fss.getLabelsList().get(i)
					.getLabel();
			t.id = fss.getValuesList().get(i)
					.getValue();
			items.add(t);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setItems(getItemList(items), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which >= 0 && which < items.size())
				{
					listener.onItemSelect(items.get(which));
				}
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onCancel();
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				listener.onCancel();
			}
		});
		
		builder.create().show();
	}
	
	private static CharSequence[] getItemList(List<MultiLevelItem> items)
	{
		CharSequence[] list = new CharSequence[items.size()];
		
		int i=0;
		for (MultiLevelItem item : items)
		{
			list[i++] = item.txt;
		}
		
		return list;
	}
	
	public static void updateFilter(PostParamsHolder holder, MultiLevelItem item, String name)
	{
		if (item == null || name == null)
		{
			return;
		}
		
		if(item.id != null 
				&&!item.id.equals("")){
			holder.put(name, item.txt, item.id);
		}
		else{
			holder.remove(name);
		}
		
	}
	
	
	public static void loadFilterBar(List<Filterss> filters, PostParamsHolder paramsHolder, View[] actionItems)
	{

		final int MAX = actionItems.length;
		int added = 0;
		for (int i=0; i<filters.size() && added < MAX;i++ )
		{
			Filterss f = filters.get(i);
			if (f.getControlType().equals("select"))
			{
				View item = actionItems[added];//filterParent.getChildAt(added); //(View) inflator.inflate(R.layout.filter_item, null);
				item.setTag(f);
				item.setVisibility(View.VISIBLE);
				TextView text = (TextView) item.findViewById(R.id.filter_name);
				initFilterLable(text, paramsHolder, f);
				added++;
			}
		}
		
		while (added < MAX)
		{
			actionItems[added].setVisibility(View.GONE);
			added++;
		}
	}
	
	public static void updateFilterLabel(View[] actionItems, String label, Filterss f)
	{
		for (View v : actionItems)
		{
			Object tag = v.getTag();
			if (tag instanceof Filterss && f.getName().equals( ((Filterss) tag).getName()))
			{
				TextView text = (TextView) v.findViewById(R.id.filter_name);
				text.setText(label);
				break;
			}
		}
	}
	
	private static void initFilterLable(TextView text, PostParamsHolder holder, Filterss filter)
	{
		if (holder != null
				&& holder.containsKey(filter
						.getName())) {
			String preValue = holder.getData(filter
					.getName());
			boolean valid = false;
			String label = holder.getUiData(filter.getName());
			if (label != null) {
				text.setText(label);
				valid = true;
			}
			if (!valid) {
				List<values> values = filter.
						getValuesList();
				for (int z = 0; z < values.size(); ++z) {
					if (values.get(z).getValue()
							.equals(preValue)) {
						text.setText(filter
								.getLabelsList().get(z)
								.getLabel());
						valid = true;
						break;
					}
				}

				if (!valid) {
					text.setText("所有" + filter.getDisplayName());
				}
			}
		} else {
			text.setText("所有" + filter.getDisplayName());
		}
	}
	
	
	
}
