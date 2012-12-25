package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.baixing.entity.GoodsDetail;
import com.baixing.entity.PostGoodsBean;
import com.baixing.entity.GoodsDetail.EDATAKEYS;
import com.baixing.imageCache.SimpleImageLoader;
import com.baixing.tracking.Tracker;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.util.Communication;
import com.baixing.util.PostUtil;
import com.baixing.widget.ImageSelectionDialog;
import com.quanleimu.activity.R;

class EditAdFragment extends PostGoodsFragment{
	private GoodsDetail goodsDetail;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		editMode = true;
		super.onCreate(savedInstanceState);				
		goodsDetail = (GoodsDetail) getArguments().getSerializable("goodsDetail");
	}
	
	@Override
	public void onResume(){
		this.pv = PV.EDITPOST;
		Tracker.getInstance()
		.pv(this.pv)
		.append(Key.SECONDCATENAME, categoryEnglishName)
		.append(Key.ADID, goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
		.end();
		super.onResume();
	}
	
	protected String getCityEnglishName(){
		if(goodsDetail != null && goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME).length() > 0){
			return goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME);
		}
		return super.getCityEnglishName();
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.myImg){
			if(goodsDetail != null){
				if(this.imgSelBundle.containsKey(ImageSelectionDialog.KEY_IMG_CONTAINER)){
					startImgSelDlg(null);
				}else{					
					ArrayList<String> smalls = new ArrayList<String>();
					ArrayList<String> bigs = new ArrayList<String>();
					String big = (goodsDetail.getImageList().getBig());
					if(big != null && big.length() > 0){
						big = Communication.replace(big);
						String[] cbig = big.split(",");
						for (int j = 0; j < listUrl.size(); j++) {
							String bigUrl = (cbig == null || cbig.length <= j) ? null : cbig[j];
							smalls.add(listUrl.get(j));
							bigs.add(bigUrl);
						}
					}
					if(bigs != null){
						List<ImageSelectionDialog.ImageContainer> container = new ArrayList<ImageSelectionDialog.ImageContainer>();
						for(int i = 0; i < bigs.size(); ++ i){
							ImageSelectionDialog.ImageContainer ic = new ImageSelectionDialog.ImageContainer();
							ic.bitmapUrl = bigs.get(i);
							ic.status = ImageSelectionDialog.ImageStatus.ImageStatus_Normal;
							ic.thumbnailPath = smalls.get(i);
							container.add(ic);
						}
						ImageSelectionDialog.ImageContainer[] ic = new ImageSelectionDialog.ImageContainer[container.size()];
						for(int i = 0; i < container.size(); ++ i){
							ic[i] = new ImageSelectionDialog.ImageContainer();
							ic[i].set(container.get(i));
						}
						startImgSelDlg(ic);
					}
				}
				return;
			}
		}
		super.onClick(v);
	}
	
	@Override
	protected void mergeParams(List<String> list){
		if (goodsDetail != null) {
			list.add("adId=" + goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
		}
	}
	
	@Override
	public void initTitle(TitleDef title){
		if(this.goodsDetail != null){
			title.m_leftActionHint = "返回";
		}
		super.initTitle(title);
	}
	
	protected String getAdContact(){
		return goodsDetail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT);
	}
	
	@Override
	protected void editPostUI() {
		if(goodsDetail == null) return;
		for(int i = 0; i < layout_txt.getChildCount(); ++ i){
			View v = layout_txt.getChildAt(i);
			PostGoodsBean bean = (PostGoodsBean)v.getTag(PostUtil.HASH_POST_BEAN);
			if(bean == null) continue;
			String detailValue = goodsDetail.getValueByKey(bean.getName());
			if(detailValue == null || detailValue.equals(""))continue;
			String displayValue = PostUtil.getDisplayValue(bean, goodsDetail, bean.getName());
			View control = (View)v.getTag(PostUtil.HASH_CONTROL);
			if(control instanceof CheckBox){
				if(displayValue.contains(((CheckBox)control).getText())){
					((CheckBox)control).setChecked(true);
				}
				else{
					((CheckBox)control).setChecked(false);
				}
			}else if(control instanceof TextView){
				((TextView)control).setText(displayValue);
			}
			this.params.put(bean.getName(), displayValue, detailValue);
		
			if(bean.getDisplayName().equals(STRING_AREA)){
				String strArea = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
				String[] areas = strArea.split(",");
				if(areas.length >= 2){
					if(control instanceof TextView){
						((TextView)control).setText(areas[areas.length - 1]);
					}
				}
			}
		}

		if (goodsDetail.getImageList() != null) {
			String b = (goodsDetail.getImageList().getResize180());
			if(b == null || b.equals("")) return;
			b = Communication.replace(b);
			if (b.contains(",")) {
				String[] c = b.split(",");
				for (int k = 0; k < c.length; k++) {
					listUrl.add(c[k]);
				}
			}else{
				listUrl.add(b);
			}
			
			if(listUrl.size() > 0){
				SimpleImageLoader.showImg(layout_txt.findViewById(R.id.myImg), listUrl.get(0), "", getActivity());
				((TextView)layout_txt.findViewById(R.id.imgCout)).setText(String.valueOf(listUrl.size()));
				layout_txt.findViewById(R.id.imgCout).setVisibility(View.VISIBLE);
			}else{
				layout_txt.findViewById(R.id.imgCout).setVisibility(View.INVISIBLE);
			}
			
			String big = (goodsDetail.getImageList().getBig());
			if(big != null && big.length() > 0){
				big = Communication.replace(big);
				String[] cbig = big.split(",");
				for(int i = 0; i < cbig.length; ++ i){
					this.bmpUrls.add(cbig[i]);
				}
			}
		}
	}
	
}