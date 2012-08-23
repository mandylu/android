package com.quanleimu.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.quanleimu.activity.R;
import com.quanleimu.entity.ChatSession;
import com.quanleimu.widget.PullToRefreshListView;
import com.quanleimu.adapter.SessionListAdapter;

public class SessionListView extends BaseView implements View.OnClickListener, PullToRefreshListView.OnRefreshListener{
	private List<ChatSession> sessions = null;
	public SessionListView(Context ctx, List<ChatSession> sessions){
		super(ctx);
		this.sessions = sessions;
		init();
	}
	
	private void init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.sessionlist, null));

//		PullToRefreshListView plv = (PullToRefreshListView)this.findViewById(R.id.lv_sessionlist);
		ListView plv = (ListView)this.findViewById(R.id.lv_sessionlist);
		SessionListAdapter adapter = new SessionListAdapter(this.getContext(), sessions);
		plv.setAdapter(adapter);
//		plv.setPullToRefreshEnabled(false);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.btnCancel:
				break;
		}
	}
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_leftActionHint = "返回";
		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
//		title.m_rightActionHint = "编辑";
		title.m_title = "私信";
		title.m_visible = true;
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
		return tab;
	}

	@Override
	public void onRefresh() {
	}
}