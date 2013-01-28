//xumengyi@baixing.com
package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.os.Bundle;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.baixing.entity.Ad;
import com.baixing.entity.PostGoodsBean;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.imageCache.ImageLoaderManager;
import com.baixing.tracking.Tracker;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.util.Communication;
import com.baixing.util.post.ImageUploader;
import com.baixing.util.post.PostUtil;
import com.baixing.util.post.PostCommonValues;
import com.quanleimu.activity.R;

class EditAdFragment extends PostGoodsFragment{
	private Ad goodsDetail;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		editMode = true;
		super.onCreate(savedInstanceState);				
		goodsDetail = (Ad) getArguments().getSerializable("goodsDetail");
	}
	
	@Override
	public void onResume(){
		this.pv = PV.EDITPOST;
		Tracker.getInstance()
		.pv(this.pv)
		.append(Key.SECONDCATENAME, categoryEnglishName)
		.append(Key.ADID, goodsDetail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))
		.end();
		super.onResume();
	}
	
	protected String getCityEnglishName(){
		if(goodsDetail != null && goodsDetail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME).length() > 0){
			return goodsDetail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME);
		}
		return super.getCityEnglishName();
	}
	
	private void loadIamgeUrl(ArrayList<String> smalls, ArrayList<String> bigs) {
		String[] bList = goodsDetail.getImageList().getBigArray();
		String[] sList = goodsDetail.getImageList().getResize180Array();
		
		if (sList != null && sList.length > 0) {
			for (int i=0; i<sList.length; i++) {
				smalls.add(sList[i]);
				bigs.add(bList[i]);
			}
		}
		
	}
	
//	@Override
//	public void onClick(View v) {
//		if(v.getId() == R.id.myImg){
//			if(goodsDetail != null){
//				ArrayList<String> smalls = new ArrayList<String>();
//				ArrayList<String> bigs = new ArrayList<String>();
//				String big = (goodsDetail.getImageList().getBig());
//				if(big != null && big.length() > 0){
//					big = Communication.replace(big);
//					String[] cbig = big.split(",");
//					for (int j = 0; j < listUrl.size(); j++) {
//						String bigUrl = (cbig == null || cbig.length <= j) ? null : cbig[j];
//						smalls.add(listUrl.get(j));
//						bigs.add(bigUrl);
//					}
//				}
//				if(bigs != null){
//					for(int i = 0; i < bigs.size(); ++ i){
//						ImageUploader.getInstance().addDownloadImage(smalls.get(i), bigs.get(i), null);
//					}
//				}
				
				
//				if(this.imgSelBundle.containsKey(ImageSelectionDialog.KEY_IMG_CONTAINER)){
//					startImgSelDlg(null);
//				}else{					
//					ArrayList<String> smalls = new ArrayList<String>();
//					ArrayList<String> bigs = new ArrayList<String>();
//					String big = (goodsDetail.getImageList().getBig());
//					if(big != null && big.length() > 0){
//						big = Communication.replace(big);
//						String[] cbig = big.split(",");
//						for (int j = 0; j < listUrl.size(); j++) {
//							String bigUrl = (cbig == null || cbig.length <= j) ? null : cbig[j];
//							smalls.add(listUrl.get(j));
//							bigs.add(bigUrl);
//						}
//					}
//					if(bigs != null){
//						List<ImageSelectionDialog.ImageContainer> container = new ArrayList<ImageSelectionDialog.ImageContainer>();
//						for(int i = 0; i < bigs.size(); ++ i){
//							ImageSelectionDialog.ImageContainer ic = new ImageSelectionDialog.ImageContainer();
//							ic.bitmapUrl = bigs.get(i);
//							ic.status = ImageSelectionDialog.ImageStatus.ImageStatus_Normal;
//							ic.thumbnailPath = smalls.get(i);
//							container.add(ic);
//						}
//						ImageSelectionDialog.ImageContainer[] ic = new ImageSelectionDialog.ImageContainer[container.size()];
//						for(int i = 0; i < container.size(); ++ i){
//							ic[i] = new ImageSelectionDialog.ImageContainer();
//							ic[i].set(container.get(i));
//						}
//						startImgSelDlg(ic);
//					}
//				}
//				return;
//			}
//		}
//		super.onClick(v);
//	}
	
	@Override
	protected void mergeParams(HashMap<String, String> list){
		if (goodsDetail != null) {
			list.put("adId", goodsDetail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
		}
		super.mergeParams(list);
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
	protected void buildPostLayout(HashMap<String, PostGoodsBean> pl){
		super.buildPostLayout(pl);
		editPostUI();
	}
	
	private void editPostUI() {
		if(goodsDetail == null) return;
		for(int i = 0; i < layout_txt.getChildCount(); ++ i){
			View v = layout_txt.getChildAt(i);
			PostGoodsBean bean = (PostGoodsBean)v.getTag(PostCommonValues.HASH_POST_BEAN);
			if(bean == null) continue;
			String detailValue = goodsDetail.getValueByKey(bean.getName());
			if(detailValue == null || detailValue.equals(""))continue;
			String displayValue = PostUtil.getDisplayValue(bean, goodsDetail, bean.getName());
			View control = (View)v.getTag(PostCommonValues.HASH_CONTROL);
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
		
			if(bean.getDisplayName().equals(PostCommonValues.STRING_AREA)){
				String strArea = goodsDetail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_AREANAME);
				String[] areas = strArea.split(",");
				if(areas.length >= 2){
					if(control instanceof TextView){
						((TextView)control).setText(areas[areas.length - 1]);
					}
				}
			}
		}

		if (goodsDetail.getImageList() != null) {
//			String b = (goodsDetail.getImageList().getResize180());
//			if(b == null || b.equals("")) return;
//			b = Communication.replace(b);
//			if (b.contains(",")) {
//				String[] c = b.split(",");
//				for (int k = 0; k < c.length; k++) {
//					listUrl.add(c[k]);
//				}
//			}else{
//				listUrl.add(b);
//			}
			
			ArrayList<String> listUrl = new ArrayList<String>();
			if (this.photoList.size() == 0) {
				ArrayList<String> list = new ArrayList<String>();
				loadIamgeUrl(listUrl, list);
				if (list.size() > 0) {
					photoList.addAll(listUrl);
					for (int i=0; i<listUrl.size(); i++) {
						ImageUploader.getInstance().addDownloadImage(listUrl.get(i), list.get(i), null);
					}
				}
			}
			
			if(listUrl.size() > 0){
				ImageLoaderManager.getInstance().showImg(layout_txt.findViewById(R.id.myImg), listUrl.get(0), "", getActivity());
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