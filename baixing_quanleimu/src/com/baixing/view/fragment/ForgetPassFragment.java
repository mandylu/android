package com.baixing.view.fragment;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.activity.BaseFragment;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.ViewUtil;
import com.quanleimu.activity.R;


public class ForgetPassFragment extends BaseFragment {
	
    private EditText mobileEt;
    private Button getCodeBtn;
    private TextView lessTimeTv;
    private EditText codeEt;
    private EditText newPwdEt;
    private EditText rePwdEt;
    private Button postBtn;

    private CountDownTimer countTimer;

    final private int MSG_NETWORK_ERROR = 0;
    final private int MSG_SENT_CODE_FINISH = 2;
    final private int MSG_POST_FINISH = 3;
    final private int MSG_POST_ERROR = 1;
    final private int MSG_SENT_CODE_ERROR = 4;

	@Override
	public void onResume() {
		this.pv = PV.FORGETPASSWORD;
		Tracker.getInstance().pv(this.pv).end();
		super.onResume();
	}
	
	public void onDestory()
	{
		super.onDestroy();
		countTimer.cancel();
	}
	
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "找回密码";
		title.m_leftActionHint = "返回";
	}
	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootV = inflater.inflate(R.layout.forget_password, null);

        mobileEt = (EditText)rootV.findViewById(R.id.forgetPwdMobileEt);
        getCodeBtn = (Button)rootV.findViewById(R.id.forgetPwdGetCodeBtn);
        lessTimeTv = (TextView)rootV.findViewById(R.id.forgetPwdLessTimeTv);
        codeEt = (EditText)rootV.findViewById(R.id.forgetPwdCodeEt);
        newPwdEt = (EditText)rootV.findViewById(R.id.forgetPwdNewPwdEt);
        rePwdEt = (EditText)rootV.findViewById(R.id.forgetPwdRePwdEt);
        postBtn = (Button)rootV.findViewById(R.id.forgetPwdPostBtn);

        getCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doGetCodeAction();
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPostNewPwdAction();
            }
        });


        return rootV;
	}

    private boolean checkMobile() {
        String mobile = mobileEt.getText().toString();
        if (mobile == null || mobile.length() != 11) {
        	Tracker.getInstance()
			.event(BxEvent.FORGETPASSWORD_SENDCODE_RESULT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, "mobile number not 11 bits!")
			.end();
            ViewUtil.showToast(getActivity(), "请输入11位手机号", false);
            return false;
        }
        return true;
    }

    private boolean checkAllInputs() {
        if (checkMobile() == false) {
            return false;
        }

        String tip = null;
        String code = codeEt.getText().toString();
        String newPwd = newPwdEt.getText().toString();
        String rePwd = rePwdEt.getText().toString();

        if (code.length() <= 0) {
            tip = "请输入验证码";
        } else if (newPwd.length() <= 0) {
            tip = "请输入新密码";
        } else if (rePwd.length() <= 0) {
            tip = "请输入确认密码";
        } else if (newPwd.equals(rePwd) == false) {
            tip = "请确保确认密码与新密码一致";
        }

        if (tip != null) {
        	Tracker.getInstance()
			.event(BxEvent.FORGETPASSWORD_RESETPASSWORD_RESULT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, tip)
			.end();
            ViewUtil.showToast(getActivity(), tip, false);
            return false;
        }

        return true;
    }
    private void postEnableGetCodeBtn()
    {
    	postBtn.post(new Runnable() {
			
			@Override
			public void run() {
				getCodeBtn.setEnabled(true);
		        lessTimeTv.setVisibility(View.GONE);
			}
		});
    }

    private void doGetCodeAction() {
        if (checkMobile() == false) {
            return;
        }
        getCodeBtn.setEnabled(false);
        lessTimeTv.setVisibility(View.VISIBLE);

//        ParameterHolder params = new ParameterHolder();
        ApiParams params = new ApiParams();
        params.addParam("mobile", mobileEt.getText().toString());
//        params.addParameter("mobile", mobileEt.getText());

        BaseApiCommand.createCommand("sendsmscode", true, params).execute(getActivity(), new Callback() {
			
			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				 sendMessage(MSG_NETWORK_ERROR, "网络异常");
			}
			
			@Override
			public void onNetworkDone(String apiName, String responseData) {
				 try {
	                    JSONObject obj = new JSONObject(responseData).getJSONObject("error");
	                    if (!"0".equals(obj.getString("code"))) {
	                    	postEnableGetCodeBtn();
	                        sendMessage(MSG_SENT_CODE_ERROR, obj.getString("message"));
	                    } else  {
	                        sendMessage(MSG_SENT_CODE_FINISH, null);
	                    }
	                } catch (JSONException e) {
	                	postEnableGetCodeBtn();
	                    sendMessage(MSG_SENT_CODE_ERROR, "网络异常");
	                }
			}
		});
    }

    private void doPostNewPwdAction() {
        if (checkAllInputs() == false) {
            return;
        }

        ApiParams params = new ApiParams();
        params.addParam("mobile", mobileEt.getText().toString());
        params.addParam("code", codeEt.getText().toString());
        params.addParam("password", newPwdEt.getText().toString());
        
        BaseApiCommand.createCommand("resetpassword", false, params).execute(getActivity(), new Callback() {
			
			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				sendMessage(MSG_NETWORK_ERROR, "网络异常");
			}
			
			@Override
			public void onNetworkDone(String apiName, String responseData) {
                try {
                    JSONObject obj = new JSONObject(responseData).getJSONObject("error");
                    if (!"0".equals(obj.getString("code"))) {
                        sendMessage(MSG_POST_ERROR, obj.getString("message"));
                    } else  {
                        sendMessage(MSG_POST_FINISH, obj.getString("message"));
                    }
                } catch (JSONException e) {
                    sendMessage(MSG_POST_ERROR, "网络异常");
                }

            }
		});
    }

    @Override
    protected void handleMessage(Message msg, Activity activity, View rootView) {
        super.handleMessage(msg, activity, rootView);

        String showMsg = "不可读的信息…";
        if(msg.obj != null && msg.obj instanceof String){
            showMsg = msg.obj.toString();
        }

        switch (msg.what) {
            case MSG_NETWORK_ERROR:
                ViewUtil.showToast(getActivity(), showMsg, false);
                //tracker
                Tracker.getInstance()
                .event(BxEvent.FORGETPASSWORD_SENDCODE_RESULT)
                .append(Key.FORGETPASSWORD_SENDCODE_RESULT_STATUS, false)
                .append(Key.FORGETPASSWORD_SENDCODE_RESULT_FAIL_REASON, (String)msg.obj)
                .end();
                break;
            case MSG_SENT_CODE_ERROR:
                ViewUtil.showToast(getActivity(), showMsg, false);
                getCodeBtn.setEnabled(true);
              //tracker
                Tracker.getInstance()
                .event(BxEvent.FORGETPASSWORD_RESETPASSWORD_RESULT)
                .append(Key.FORGETPASSWORD_SENDCODE_RESULT_STATUS, false)
                .append(Key.FORGETPASSWORD_SENDCODE_RESULT_FAIL_REASON, (String)msg.obj)
                .end();
                break;
            case MSG_SENT_CODE_FINISH:
                ViewUtil.showToast(getActivity(), "一分钟后可再次获取", false);
                disableGetCodeBtn();
              //tracker
                Tracker.getInstance()
                .event(BxEvent.FORGETPASSWORD_SENDCODE_RESULT)
                .append(Key.FORGETPASSWORD_SENDCODE_RESULT_STATUS, true)
                .end();
                break;
            case MSG_POST_FINISH:
                ViewUtil.showToast(getActivity(), showMsg, false);
                finishFragment();
              //tracker
                Tracker.getInstance()
                .event(BxEvent.FORGETPASSWORD_RESETPASSWORD_RESULT)
                .append(Key.FORGETPASSWORD_RESETPASSWORD_RESULT_STATUS, true)
                .end();
                break;
            case MSG_POST_ERROR:
                ViewUtil.showToast(getActivity(), showMsg, false);
              //tracker
                Tracker.getInstance()
                .event(BxEvent.FORGETPASSWORD_RESETPASSWORD_RESULT)
                .append(Key.FORGETPASSWORD_RESETPASSWORD_RESULT_STATUS, false)
                .append(Key.FORGETPASSWORD_RESETPASSWORD_RESULT_FAIL_REASON, (String)msg.obj)
                .end();
                break;
        }
    }

    private void disableGetCodeBtn() {
        getCodeBtn.setEnabled(false);
        getCodeBtn.setText("验证码发送成功");
        countTimer = new CountDownTimer(60000,1000) {
			
			@Override
			public void onTick(final long millisUntilFinished) {
				lessTimeTv.post(new Runnable() {
					public void run()
					{
						lessTimeTv.setText(Integer.toString((int) (millisUntilFinished/1000)));
					}
				});
			}
			
			@Override
			public void onFinish() {
				getCodeBtn.post(new Runnable() {
					public void run() {
						getCodeBtn.setEnabled(true);
						lessTimeTv.setText("");
						lessTimeTv.setVisibility(View.GONE);
						getCodeBtn.setText("获取验证码");
					}
				});
			}
		}.start();
    }
    
    public boolean hasGlobalTab()
	{
		return false;
	}
}
