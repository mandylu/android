package com.quanleimu.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.entity.PostGoodsBean;
import com.quanleimu.entity.PostMu;
import com.quanleimu.entity.UserBean;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;

public class PostGoods extends BaseActivity {

	private Button post, backBtn;
	public ImageView img1, img2, img3;
	public String backPageName = "";
	public String categoryEnglishName = "";
	public String json = "";
	public TextView tvTitle;
	public LinearLayout layout_txt;
	public List<PostGoodsBean> postList;
	public static final int NONE = 0;
	public static final int PHOTOHRAPH = 1;
	public static final int PHOTOZOOM = 2;
	public static final int PHOTORESOULT = 3;
	public static final int POST_LIST = 4;
	public static final String IMAGEUNSPECIFIED = "image/*";
	private List<TextView> tvlist;
	private int selId;
	private LinkedHashMap<Integer, String> postMap;
	// private LinkedHashMap<Integer, TextView> textViewMap;
	// private LinkedHashMap<Integer, List<CheckBox>> checkBoxMap;
	private LinkedHashMap<Integer, Object> btMap;
	private EditText descriptionEt, titleEt;
	private AlertDialog ad;
	private Button photoalbum, photomake, photocancle;
	private LinkedHashMap<String, Bitmap> bitmap_url; // 上传成功后图片路径
	private ImageView[] imgs;
	private String mobile, password;
	private UserBean user;

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.postgoods);

		super.onCreate(savedInstanceState);
		// 解决自动弹出输入法
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		tvlist = new ArrayList<TextView>();
		postList = new ArrayList<PostGoodsBean>();
		postMap = new LinkedHashMap<Integer, String>();
		// textViewMap = new LinkedHashMap<Integer, TextView>();
		// checkBoxMap = new LinkedHashMap<Integer, List<CheckBox>>();
		bitmap_url = new LinkedHashMap<String, Bitmap>();
		btMap = new LinkedHashMap<Integer, Object>();
		categoryEnglishName = intent.getExtras().getString(
				"categoryEnglishName");
		backPageName = intent.getExtras().getString("backPageName");

		post = (Button) findViewById(R.id.post);
		backBtn = (Button) findViewById(R.id.backBtn);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("发布");
		backBtn.setText(backPageName);
		layout_txt = (LinearLayout) findViewById(R.id.layout_txt);

		post.setOnClickListener(this);
		backBtn.setOnClickListener(this);

		pd = new ProgressDialog(this);
		pd.setTitle("提示");
		pd.setMessage("请稍候...");
		pd.setCancelable(true);

		user = (UserBean) Util.loadDataFromLocate(this, "user");
		if (user == null) {
			bundle.putInt("type", 2);
			bundle.putString("back", backPageName);
			intent.putExtras(bundle);
			intent.setClass(this, Login.class);
			startActivity(intent);
			finish();
		} else {
			mobile = user.getPhone();
			password = user.getPassword();

			PostMu postMu = (PostMu) Util.loadDataFromLocate(this,
					categoryEnglishName + myApp.cityEnglishName);
			if (postMu != null && !postMu.getJson().equals("")) {
				json = postMu.getJson();
				Long time = postMu.getTime();
				if (time + (24 * 3600 * 100) < System.currentTimeMillis()) {
					myHandler.sendEmptyMessage(1);
					new Thread(new GetGoodsListThread(false)).start();
				} else {
					myHandler.sendEmptyMessage(1);
				}
			} else {
				pd.show();
				new Thread(new GetGoodsListThread(true)).start();
			}

		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == img1 || v == img2 || v == img3) {
			for (int i = 0; i < imgs.length; i++) {
				if (imgs[i].equals(v)) {
					Object[] keys;
					try {
						keys = bitmap_url.keySet().toArray();
						if (keys[i] != null) {
							bitmap_url.remove(keys[i]);
							for (int j = 0; j < imgs.length; j++) {
								try {
									keys = bitmap_url.keySet().toArray();
									imgs[j].setImageBitmap(bitmap_url
											.get(keys[j]));
								} catch (Exception e) {
									imgs[j].setImageResource(R.drawable.btn_camera);
								}
							}
						} else {
							showDialog();
						}
					} catch (Exception e) {
						showDialog();
					}

				}
			}

		} else if (v == photoalbum) {
			if (ad.isShowing()) {
				ad.dismiss();
			}
			// Intent intent3 = new Intent(Intent.ACTION_PICK, null);
			// intent3.setDataAndType(
			// MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			// IMAGEUNSPECIFIED);
			// startActivityForResult(intent3, PHOTOZOOM);
			Intent intent3 = new Intent(Intent.ACTION_GET_CONTENT);
			intent3.addCategory(Intent.CATEGORY_OPENABLE);
			intent3.setType(IMAGEUNSPECIFIED);
			startActivityForResult(Intent.createChooser(intent3, "选择图片"),
					PHOTOZOOM);

		} else if (v == photomake) {
			if (ad.isShowing()) {
				ad.dismiss();
			}
			// Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// intent2.putExtra(MediaStore.EXTRA_OUTPUT,
			// Uri.fromFile(new File("/sdcard/", "temp.jpg")));
			// startActivityForResult(intent2, PHOTOHRAPH);
			Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent2.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File("/sdcard/", "temp.jpg")));
			startActivityForResult(intent2, PHOTOHRAPH);

		} else if (v == photocancle) {
			ad.dismiss();
		} else if (v == backBtn) {
			finish();
		}

		else if (v == post) {
			if (check2()) {
				for (int i = 0; i < postList.size(); i++) {
					PostGoodsBean postGoodsBean = postList.get(i);
					if (postGoodsBean.getControlType().equals("input")
							|| postGoodsBean.getControlType()
									.equals("textarea")) {
						EditText et = (EditText) btMap.get(i);
						postMap.put(i, et.getText().toString());
					} else if (postGoodsBean.getControlType()
							.equals("checkbox")) {
						String value = "";
						@SuppressWarnings("unchecked")
						List<CheckBox> l = (List<CheckBox>) btMap.get(i);
						for (int j = 0; j < l.size(); j++) {
							CheckBox c = l.get(j);
							if (c.isChecked()) {
								value = value
										+ postGoodsBean.getValues().get(j);
							}
						}
						postMap.put(i, value);
					}

				}

				pd = ProgressDialog.show(PostGoods.this, "提示", "请稍候...");
				pd.setCancelable(true);
				new Thread(new UpdateThread()).start();
			}

		}
		super.onClick(v);
	}

	private boolean check2() {
		for (int i = 0; i < postList.size(); i++) {
			PostGoodsBean postGoodsBean = postList.get(i);
			if (postGoodsBean.getRequired().endsWith("required")) {
				if (postGoodsBean.getControlType().equals("select")) {
					TextView obj = (TextView) btMap.get(i);
					if (obj.getText().toString().trim().length() == 0 || obj.getText().toString().trim().equals("请选择")) {
						
						Toast.makeText(PostGoods.this,
								"请填写" + postGoodsBean.getDisplayName() + "!",
								0).show();
						return false;
					}
				} else if (postGoodsBean.getControlType().equals("input")) {
					EditText obj = (EditText) btMap.get(i);
					if (obj.getText().toString().trim().length() == 0) {
						Toast.makeText(PostGoods.this,
								"请填写" + postGoodsBean.getDisplayName() + "!",
								0).show();
						return false;
					}
				}
			}
		}
		return true;
	}

	private void showDialog() {
		View view = LinearLayout.inflate(this, R.layout.upload_head, null);
		Builder builder = new AlertDialog.Builder(this);
		builder.setView(view);
		ad = builder.create();

		WindowManager.LayoutParams lp = ad.getWindow().getAttributes();
		lp.y = 300;
		ad.onWindowAttributesChanged(lp);
		ad.show();

		photoalbum = (Button) view.findViewById(R.id.photo_album);
		photoalbum.setOnClickListener(this);
		photomake = (Button) view.findViewById(R.id.photo_make);
		photomake.setOnClickListener(this);
		photocancle = (Button) view.findViewById(R.id.photo_cancle);
		photocancle.setOnClickListener(this);
	}

	class UpdateThread implements Runnable {
		public void run() {

			String apiName = "ad_add";
			ArrayList<String> list = new ArrayList<String>();

			list.add("mobile=" + mobile);
			String password1 = Communication.getMD5(password);
			password1 += Communication.apiSecret;
			String userToken = Communication.getMD5(password1);
			list.add("userToken=" + userToken);
			list.add("categoryEnglishName=" + categoryEnglishName);
			list.add("cityEnglishName=" + myApp.cityEnglishName);

			for (int i = 0; i < postMap.size(); i++) {
				int key = (Integer) postMap.keySet().toArray()[i];

				String values = postMap.get(key);
				if (!values.equals("") && values != null) {
					list.add(URLEncoder.encode(postList.get(key).getName())
							+ "=" + URLEncoder.encode(values));
				}
			}

			for (int i = 0; i < bitmap_url.size(); i++) {
				try {
					list.add("image=" + bitmap_url.keySet().toArray()[i]);
				} catch (Exception e) {

				}
			}
			// list.add("title=" + "111");
			// list.add("description=" +
			// URLEncoder.encode(descriptionEt.getText().toString()));

			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);
				System.out.println("json---->" + json);
				if (json != null) {
					JSONObject jsonObject = new JSONObject(json);
					JSONObject json = jsonObject.getJSONObject("error");
					code = json.getInt("code");
					message = json.getString("message");
					if(code == 0)
					{
						// 发布成功
						myHandler.sendEmptyMessageDelayed(3, 3000);
					}
					else {
						
						myHandler.sendEmptyMessage(2);
					}
				} 
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public int code = -1;
	public String message = "";
	class GetGoodsListThread implements Runnable {

		private boolean isUpdate;

		public GetGoodsListThread(boolean isUpdate) {
			this.isUpdate = isUpdate;
		}

		@Override
		public void run() {

			String apiName = "category_meta_post";
			ArrayList<String> list = new ArrayList<String>();

			list.add("categoryEnglishName=" + categoryEnglishName);
			list.add("cityEnglishName=" + myApp.cityEnglishName);

			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);
				if (json != null) {
					// 获取数据成功
					PostMu postMu = new PostMu();
					postMu.setJson(json);
					postMu.setTime(System.currentTimeMillis());
					Helper.saveDataToLocate(PostGoods.this, categoryEnglishName
							+ myApp.cityEnglishName, postMu);
					if (isUpdate) {
						myHandler.sendEmptyMessage(1);
					}
				} else {
					//{"error":{"code":0,"message":"\u66f4\u65b0\u4fe1\u606f\u6210\u529f"},"id":"191285466"}
					myHandler.sendEmptyMessage(2);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				myHandler.sendEmptyMessage(10);
				e.printStackTrace();
			}

		}
	}

	private Uri uri = null;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == NONE) {
			return;
		}
		// 拍照
		if (requestCode == PHOTOHRAPH) {
			// 设置文件保存路径这里放在跟目录下
			File picture = new File("/sdcard/" + "/temp.jpg");
			uri = Uri.fromFile(picture);
			getBitmap(uri, PHOTOHRAPH); // 直接返回图片
			// startPhotoZoom(uri); //截取图片尺寸
		}

		if (data == null) {
			return;
		}

		// 读取相册缩放图片
		if (requestCode == PHOTOZOOM) {
			uri = data.getData();
			// startPhotoZoom(uri);
			getBitmap(uri, PHOTOZOOM);
		}
		// 处理结果
		if (requestCode == PHOTORESOULT) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				photo.compress(Bitmap.CompressFormat.JPEG, 75, stream); // (0 -
				// 100)压缩文件
				// saveSDCard(photo);
				photo = Util.newBitmap(photo, 135, 135);
				imgs[bitmap_url.size()].setImageBitmap(photo);

				new Thread(new UpLoadThread(photo)).start();

			}
		}

		if (requestCode == POST_LIST) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				int id = extras.getInt("id");
				TextView tv = tvlist.get(selId);
				String txt = postList.get(selId).getLabels().get(id);
				String txtValue = postList.get(selId).getValues().get(id);
				postMap.put(selId, txtValue);
				tv.setText(txt);
			}

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void getBitmap(Uri uri, int id) {
		String path = "";
		Bitmap photo = null;

		path = getRealPathFromURI(uri); // from Gallery

		if (path == null) {
			path = uri.getPath(); // from File Manager
		}
		if (path != null) {
			try {
				photo = BitmapFactory.decodeFile(path);
				photo = Util.newBitmap(photo, 135, 135);
				imgs[bitmap_url.size()].setImageBitmap(photo);
				// imgs[bitmap_url.size()].setPadding(5, 5, 5, 5);
				// imgs[bitmap_url.size()].setBackgroundResource(R.drawable.btn_camera);
				imgs[bitmap_url.size()].setFocusable(true);

				new Thread(new UpLoadThread(photo)).start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	}

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);

		if (cursor == null)
			return null;

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IMAGEUNSPECIFIED);
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 64);
		intent.putExtra("outputY", 64);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, PHOTORESOULT);
	}

	public void saveSDCard(Bitmap photo) {
		try {
			String filepath = "/sdcard/baixing";
			File files = new File(filepath);
			files.mkdir();
			File file = new File(filepath, "temp.jpg");
			FileOutputStream outStream = new FileOutputStream(file);
			String path = file.getAbsolutePath();
			Log.i(path, path);
			photo.compress(CompressFormat.JPEG, 100, outStream);
			outStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 管理线程的Handler
	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (pd.isShowing()) {
				pd.dismiss();
			}
			switch (msg.what) {
			case 1:

				layout_txt.setOrientation(LinearLayout.VERTICAL);
				postList = JsonUtil.getPostGoodsBean(json);
				for (int i = 0; i < postList.size(); i++) {
					final int position = i;
					PostGoodsBean postBean = postList.get(i);
					LinearLayout layout = new LinearLayout(PostGoods.this);
					TextView tvshow = new TextView(PostGoods.this);
					TextView tvcontent = new TextView(PostGoods.this);
					ImageView ivforward = new ImageView(PostGoods.this);
					EditText etcontent = new EditText(PostGoods.this);

					img1 = new ImageView(PostGoods.this);
					img2 = new ImageView(PostGoods.this);
					img3 = new ImageView(PostGoods.this);
					imgs = new ImageView[] { img1, img2, img3 };
					TextView tvlastunit = new TextView(PostGoods.this);
					// ImageView xx = new ImageView(PostGoods.this);
					TextView ttxx = new TextView(PostGoods.this);

					if (i == 0) {
						layout.setBackgroundResource(R.drawable.btn_top_bg);
					} else if (i == postList.size() - 1) {
						layout.setBackgroundResource(R.drawable.btn_down_bg);
					} else {
						layout.setBackgroundResource(R.drawable.btn_m_bg);
					}

					layout.setGravity(Gravity.CENTER_VERTICAL);
					layout.setPadding(10, 10, 10, 10);

					if (postBean.getRequired().equals("required")) {
						// xx.setImageResource(R.drawable.icon);
						// layout.addView(xx);
						ttxx.setGravity(Gravity.TOP);
						ttxx.setTextColor(Color.RED);
						ttxx.setText("*");
						layout.addView(ttxx);
					}

					if (postBean.getControlType().equals("input")) {
						tvshow.setText(postBean.getDisplayName());
						tvshow.setTextSize(18);
						layout.addView(tvshow);
						if (postBean.getName().equals("title")) {
							titleEt = new EditText(PostGoods.this);
							titleEt.setTextSize(16);
							titleEt.setTextColor(0xff595959);
							titleEt.setLayoutParams(new LayoutParams(
									LayoutParams.FILL_PARENT,
									LayoutParams.WRAP_CONTENT, 1));
							titleEt.setBackgroundDrawable(null);
							titleEt.setGravity(Gravity.RIGHT);
							titleEt.setHint("请输入");
							// textViewMap.put(position, titleEt);
							btMap.put(position, titleEt);
							layout.addView(titleEt);
						} else {
							if (postBean.getName().equals("contact")) {
								etcontent.setText(user.getPhone());
							} else {
								etcontent.setText("");
							}
							etcontent.setTextSize(16);
							etcontent.setTextColor(0xff595959);
							etcontent.setLayoutParams(new LayoutParams(
									LayoutParams.FILL_PARENT,
									LayoutParams.WRAP_CONTENT, 1));
							// 设置输入类型；注：设置之后hint属性会消失
							// etcontent.setHint("请输入");
							if (postBean.getNumeric() == 1) {
								etcontent
										.setInputType(InputType.TYPE_CLASS_NUMBER);
							} else {
								etcontent
										.setInputType(InputType.TYPE_CLASS_TEXT);
							}

							etcontent.setBackgroundDrawable(null);
							etcontent.setGravity(Gravity.RIGHT);
							etcontent.setHint("请输入");

							// textViewMap.put(position, etcontent);
							btMap.put(position, etcontent);
							layout.addView(etcontent);
						}
						if (!postBean.getUnit().equals("")) {
							tvlastunit.setTextSize(18);
							tvlastunit.setLayoutParams(new LayoutParams(
									LayoutParams.WRAP_CONTENT,
									LayoutParams.WRAP_CONTENT));
							tvlastunit.setText(postBean.getUnit());
						}
						layout.addView(tvlastunit);
					} else if (postBean.getControlType().equals("select")) {
						tvshow.setText(postBean.getDisplayName());
						tvshow.setTextSize(18);
						tvcontent.setLayoutParams(new LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT, 1));
						tvcontent.setTextSize(16);
						tvcontent.setTextColor(0xff595959);
						LayoutParams lp = new LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT, 1);
						lp.setMargins(0, 0, 20, 0);
						tvcontent.setLayoutParams(lp);
						tvcontent.setGravity(Gravity.RIGHT);
						tvcontent.setText("请选择");
						btMap.put(position, tvcontent);
						ivforward.setLayoutParams(new LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
						ivforward.setImageResource(R.drawable.arrow);

						layout.addView(tvshow);
						layout.addView(tvcontent);
						layout.addView(ivforward);

					} else if (postBean.getControlType().equals("tableSelect")) {
						tvshow.setText(postBean.getDisplayName());
						tvshow.setTextSize(18);
						tvcontent.setLayoutParams(new LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT, 1));
						tvcontent.setTextSize(16);
						tvcontent.setTextColor(0xff595959);
						LayoutParams lp = new LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT, 1);
						lp.setMargins(0, 0, 20, 0);
						tvcontent.setLayoutParams(lp);
						tvcontent.setGravity(Gravity.RIGHT);
						tvcontent.setText("请选择");
						btMap.put(position, tvcontent);
						ivforward.setLayoutParams(new LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
						ivforward.setImageResource(R.drawable.arrow);

						layout.addView(tvshow);
						layout.addView(tvcontent);
						layout.addView(ivforward);

					} else if (postBean.getControlType().equals("checkbox")) {
						layout.setGravity(Gravity.TOP);
						tvshow.setText(postBean.getDisplayName());
						tvshow.setTextSize(18);
						layout.addView(tvshow);
						LinearLayout checkbox_layout = new LinearLayout(
								PostGoods.this);
						checkbox_layout.setGravity(Gravity.TOP);
						checkbox_layout.setOrientation(LinearLayout.VERTICAL);
						List<String> boxes = postBean.getLabels();
						List<CheckBox> boxeslist = new ArrayList<CheckBox>();
						for (int j = 0; j < boxes.size(); j++) {
							String ss = boxes.get(j);
							CheckBox checkbox = new CheckBox(PostGoods.this);
							checkbox.setLayoutParams(new LayoutParams(
									LayoutParams.WRAP_CONTENT,
									LayoutParams.WRAP_CONTENT));
							checkbox.setTextSize(16);
							checkbox.setText(ss);
							boxeslist.add(checkbox);
							checkbox_layout.addView(checkbox);
						}
						layout.addView(checkbox_layout);
						// checkBoxMap.put(position, boxeslist);
						btMap.put(position, boxeslist);
					} else if (postBean.getControlType().equals("textarea")) {
						descriptionEt = new EditText(PostGoods.this);
						descriptionEt.setTextSize(16);
						descriptionEt.setTextColor(0xff595959);
						tvshow.setText(postBean.getDisplayName());
						tvshow.setTextSize(18);
						descriptionEt.setHint("请输入");
						descriptionEt.setGravity(Gravity.TOP);
						descriptionEt.setLines(5);
						descriptionEt.setText(myApp.getPersonMark());
						// textViewMap.put(position, descriptionEt);
						btMap.put(position, descriptionEt);
						layout.setOrientation(LinearLayout.VERTICAL);
						layout.addView(tvshow);
						layout.addView(descriptionEt);
					} else if (postBean.getControlType().equals("image")) {
						img1.setLayoutParams(new LinearLayout.LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT, 1));
						img2.setLayoutParams(new LinearLayout.LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT, 1));
						img3.setLayoutParams(new LinearLayout.LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT, 1));
						img1.setImageResource(R.drawable.btn_camera);
						img2.setImageResource(R.drawable.btn_camera);
						img3.setImageResource(R.drawable.btn_camera);
						img1.setOnClickListener(PostGoods.this);
						img2.setOnClickListener(PostGoods.this);
						img3.setOnClickListener(PostGoods.this);
						layout.addView(img1);
						layout.addView(img2);
						layout.addView(img3);
						btMap.put(position, imgs);
					}

					postMap.put(position, "");
					tvlist.add(tvcontent);

					layout.setTag(postBean);
					layout.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							PostGoodsBean postBean = (PostGoodsBean) v.getTag();
							if (postBean.getControlType().equals("select")) {
								selId = position;
								bundle.putSerializable("postBean", postBean);
								bundle.putString("title",
										postBean.getDisplayName());
								bundle.putString("back", "发布");
								intent.setClass(PostGoods.this,
										PostGoodsSelection.class);
								intent.putExtras(bundle);
								startActivityForResult(intent, POST_LIST);
							} else if (postBean.getControlType().equals(
									"tableSelect")) {
								selId = position;
								bundle.putSerializable("postBean", postBean);
								bundle.putString("title",
										postBean.getDisplayName());
								bundle.putString("back", "发布");
								intent.setClass(PostGoods.this,
										PostGoodsSelection.class);
								intent.putExtras(bundle);
								startActivityForResult(intent, POST_LIST);
							}
						}
					});
					layout_txt.addView(layout);
				}

				break;

			case 2:
				if (pd != null) {
					pd.dismiss();
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(PostGoods.this);
				builder.setTitle("提示:").setMessage(message)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
				
				break;
			case 3:
				try {
					JSONObject jsonObject = new JSONObject(json);
					String id;
					try {
						id = jsonObject.getString("id");
					} catch (Exception e) {
						id = "";
						e.printStackTrace();
					}
					JSONObject json = jsonObject.getJSONObject("error");
					String message = json.getString("message");
					Toast.makeText(PostGoods.this, message, 0).show();
					if (!id.equals("")) {
						// 发布成功
						// Toast.makeText(PostGoods.this, "未显示，请手动刷新",
						// 3).show();
						intent.putExtras(bundle);
						intent.setClass(PostGoods.this, MyCenter.class);
						startActivity(intent);
						finish();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			case 4:

				break;
			case 10:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(PostGoods.this, "网络连接异常", 3).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

	class UpLoadThread implements Runnable {
		private Bitmap b;

		public UpLoadThread() {
			super();
		}

		public UpLoadThread(Bitmap b) {
			super();
			this.b = b;
		}

		public void run() {
			try {
				String result = Communication.uploadPicture(b);
				while (true) {
					if (result != null) {
						bitmap_url.put(result, b);
						break;
					} else {
						Toast.makeText(PostGoods.this, "上传图片失败", 5).show();
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
