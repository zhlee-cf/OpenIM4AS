package com.open.im.activity;

import java.io.IOException;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.iqregister.AccountManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyUtils;
import com.open.im.utils.XMPPConnectionUtils;

public class RegisterActivity extends Activity {

	private EditText et_username, et_pwd;
	private Button btn_register;
	private RegisterActivity act;
	protected ProgressDialog pd;
	private EditText et_nick;
	private static final int REGISTER_SUCCESS = 101;
	private static final int REGISTER_FAIL = 102;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_userinfo);

		init();

		register();
	}

	/**
	 * 注册点击监听
	 */
	private void register() {
		btn_register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				final String username = et_username.getText().toString().trim();
				final String nickname = et_nick.getText().toString().trim();
				final String password = et_pwd.getText().toString().trim();

				if (TextUtils.isEmpty(username)) {
					MyUtils.showToast(act, "用户名不能为空");
					return;
				} else if (TextUtils.isEmpty(nickname)) {
					MyUtils.showToast(act, "昵称不能为空");
					return;
				} else if (TextUtils.isEmpty(password)) {
					MyUtils.showToast(act, "密码不能为空");
					return;
				}

				pd = new ProgressDialog(act);
				pd.setMessage("拼命加载中，请稍后...");
				pd.show();

				// 注册用户
				registerUser(username, password, nickname);
			}

		});
	}

	/**
	 * 方法 注册用户
	 * 
	 * @param username
	 * @param password
	 */
	private void registerUser(final String username, final String password, final String nickname) {
		new Thread() {
			public void run() {
				// 模拟注册耗时
				SystemClock.sleep(1000);
				try {
					XMPPConnectionUtils.initXMPPConnection();
					AbstractXMPPConnection connection = MyApp.connection;
					if (!connection.isConnected()) {
						connection.connect();
					}
					// 获得账户管理者
					AccountManager accountManager = AccountManager.getInstance(connection);

					// 创建新用户
					// accountManager.createAccount(username, password);
//					Map<String, String> attributes = new HashMap<String, String>();
//					attributes.put("name", username);
					accountManager.createAccount(username, password);

					sp.edit().putString("username", username).commit();
					sp.edit().putString("password", password).commit();
					sp.edit().putString("nickname", nickname).commit();
					handler.sendEmptyMessage(REGISTER_SUCCESS);

					// 注册成功后跳转到注册成功界面
					Intent intent = new Intent(act, RegisterSuccessActivity.class);
					act.startActivity(intent);
					finish();

				} catch (NoResponseException e) {
					handler.sendEmptyMessage(REGISTER_FAIL);
					e.printStackTrace();
				} catch (XMPPErrorException e) {
					if (e.getXMPPError().toString().contains(XMPPError.Condition.conflict.toString())) {
						pd.dismiss();
						MyUtils.showToast(act, "用户已存在");
					}
					// XMPPError: conflict - cancel
					// conflict
					e.printStackTrace();
				} catch (NotConnectedException e) {
					handler.sendEmptyMessage(REGISTER_FAIL);
					e.printStackTrace();
				} catch (SmackException e) {
					handler.sendEmptyMessage(REGISTER_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					handler.sendEmptyMessage(REGISTER_FAIL);
					e.printStackTrace();
				} catch (XMPPException e) {
					handler.sendEmptyMessage(REGISTER_FAIL);
					e.printStackTrace();
				}
			};
		}.start();
	}

	/**
	 * 初始化
	 */
	private void init() {
		act = this;

		et_username = (EditText) findViewById(R.id.et_username);
		et_pwd = (EditText) findViewById(R.id.et_pwd);
		et_nick = (EditText) findViewById(R.id.et_nick);
		btn_register = (Button) findViewById(R.id.btn_register);

		sp = getSharedPreferences(MyConstance.SP_NAME, 0);
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			pd.dismiss();
			switch (msg.what) {
			case REGISTER_SUCCESS:
				MyUtils.showToast(act, "注册成功");
				break;
			case REGISTER_FAIL:
				MyUtils.showToast(act, "注册失败");
				break;
			default:
				break;
			}
		};
	};
	private SharedPreferences sp;
}