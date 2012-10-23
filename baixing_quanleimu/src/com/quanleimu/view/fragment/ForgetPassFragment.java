package com.quanleimu.view.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.R;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ParameterHolder;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.TimerTask;
import java.util.Timer;


public class ForgetPassFragment extends BaseFragment {
    private EditText mobileEt;
    private Button getCodeBtn;
    private TextView lessTimeTv;
    private EditText codeEt;
    private EditText newPwdEt;
    private EditText rePwdEt;
    private Button postBtn;

    private Timer countTimer;

    final private int MSG_NETWORK_ERROR = 0;
    final private int MSG_SENT_CODE_FINISH = 2;
    final private int MSG_POST_FINISH = 3;
    final private int MSG_POST_ERROR = 1;
    final private int MSG_SENT_CODE_ERROR = 4;
    final private int MSG_TIMER_1SECOND = 5;

    private int lessTime = 60;


	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "找回密码";
		title.m_leftActionHint = "返回";
	}
	public void initTab(TabDef tab){
		tab.m_visible = false;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
            Toast.makeText(getActivity(), "请输入11位手机号", 1).show();
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
            Toast.makeText(getActivity(), tip, 1).show();
            return false;
        }

        return true;
    }

    private void doGetCodeAction() {
        if (checkMobile() == false) {
            return;
        }
        getCodeBtn.setEnabled(false);
        lessTimeTv.setVisibility(View.VISIBLE);

        ParameterHolder params = new ParameterHolder();
        params.addParameter("mobile", mobileEt.getText());

        Communication.executeAsyncGetTask("sendsmscode", params, new Communication.CommandListener() {

            @Override
            public void onServerResponse(String serverMessage) {
                try {
                    JSONObject obj = new JSONObject(serverMessage).getJSONObject("error");
                    if (!"0".equals(obj.getString("code"))) {
                        sendMessage(MSG_SENT_CODE_ERROR, obj.getString("message"));
                    } else  {
                        sendMessage(MSG_SENT_CODE_FINISH, null);
                    }
                } catch (JSONException e) {
                    sendMessage(MSG_SENT_CODE_ERROR, "网络异常");
                }

            }

            @Override
            public void onException(Exception ex) {
                sendMessage(MSG_NETWORK_ERROR, "网络异常");
            }
        });
    }

    private void doPostNewPwdAction() {
        if (checkAllInputs() == false) {
            return;
        }

        ParameterHolder params = new ParameterHolder();
        params.addParameter("mobile", mobileEt.getText());
        params.addParameter("code", codeEt.getText());
        params.addParameter("password", newPwdEt.getText());

        Communication.executeAsyncGetTask("resetpassword", params, new Communication.CommandListener() {

            @Override
            public void onServerResponse(String serverMessage) {
                try {
                    JSONObject obj = new JSONObject(serverMessage).getJSONObject("error");
                    if (!"0".equals(obj.getString("code"))) {
                        sendMessage(MSG_POST_ERROR, obj.getString("message"));
                    } else  {
                        sendMessage(MSG_POST_FINISH, null);
                    }
                } catch (JSONException e) {
                    sendMessage(MSG_SENT_CODE_ERROR, "网络异常");
                }

            }

            @Override
            public void onException(Exception ex) {
                sendMessage(MSG_NETWORK_ERROR, "网络异常");
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
                Toast.makeText(getActivity(), showMsg, 1).show();
                break;
            case MSG_SENT_CODE_ERROR:
                Toast.makeText(getActivity(), showMsg, 1).show();
                getCodeBtn.setEnabled(true);
                break;
            case MSG_SENT_CODE_FINISH:
                Toast.makeText(getActivity(), "验证码发送成功", 1).show();
                disableGetCodeBtn();
                break;
            case MSG_POST_FINISH:
                Toast.makeText(getActivity(), showMsg, 1).show();
                break;
            case MSG_POST_ERROR:
                Toast.makeText(getActivity(), showMsg, 1).show();
                break;
            case MSG_TIMER_1SECOND:
                lessTime--;
                if (lessTime < 0) {
                    countTimer.cancel();
                    lessTime = 60;
                    getCodeBtn.setEnabled(true);
                    lessTimeTv.setVisibility(View.GONE);
                    getCodeBtn.setText("获取验证码");
                } else {
                    lessTimeTv.setText(Integer.toString(lessTime));
                }
                break;
        }
    }

    private void disableGetCodeBtn() {
        getCodeBtn.setEnabled(false);
        getCodeBtn.setText("一分钟后可再次获取");
        TimerTask timerTask = new CountTimeTask();
        countTimer = new Timer(true);
        countTimer.schedule(timerTask, 0, 1000);
    }

    private class CountTimeTask extends TimerTask {
        @Override
        public void run() {
            sendMessage(MSG_TIMER_1SECOND, null);
        }
    }

}
