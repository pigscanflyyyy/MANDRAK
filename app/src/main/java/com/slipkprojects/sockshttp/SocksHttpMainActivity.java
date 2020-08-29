package com.slipkprojects.sockshttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.slipkprojects.sockshttp.R;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.widget.AdapterView;
import android.widget.Toast;
import android.view.View;
import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import com.slipkprojects.sockshttp.util.Utils;
import android.util.Log;
import android.widget.TextView;
import android.support.v4.view.GravityCompat;
import android.widget.EditText;
import android.support.design.widget.TextInputEditText;
import com.slipkprojects.sockshttp.DrawerLog;
import android.support.v4.widget.DrawerLayout;
import android.net.Uri;
import android.widget.Button;
import com.slipkprojects.sockshttp.SocksHttpApp;
import android.widget.CheckBox;
import android.support.v4.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import com.slipkprojects.sockshttp.activities.ConfigGeralActivity;
import android.view.LayoutInflater;
import android.content.pm.PackageManager;
import android.text.Html;
import android.support.v7.app.AlertDialog;
import android.content.pm.PackageInfo;
import com.slipkprojects.ultrasshservice.util.SkProtect;
import com.slipkprojects.ultrasshservice.logger.SkStatus;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.widget.LinearLayout;
import com.slipkprojects.sockshttp.fragments.ProxyRemoteDialogFragment;
import android.annotation.TargetApi;
import android.os.Build;
import android.net.VpnService;
import android.content.ActivityNotFoundException;
import android.app.Activity;
import com.slipkprojects.ultrasshservice.logger.ConnectionStatus;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import com.slipkprojects.sockshttp.fragments.ClearConfigDialogFragment;
import com.slipkprojects.sockshttp.activities.ConfigExportFileActivity;
import com.slipkprojects.sockshttp.activities.ConfigImportFileActivity;
import com.slipkprojects.ultrasshservice.config.Settings;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.PersistableBundle;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatRadioButton;
import android.widget.RadioGroup;
import com.slipkprojects.ultrasshservice.config.ConfigParser;
import android.support.v4.app.ActivityCompat;
import android.content.DialogInterface;
import com.slipkprojects.ultrasshservice.tunnel.TunnelManagerHelper;
import com.slipkprojects.ultrasshservice.LaunchVpn;
import com.slipkprojects.sockshttp.activities.AboutActivity;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import com.slipkprojects.sockshttp.model.ViewFragment;
import android.text.InputType;
import android.widget.ImageButton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.support.design.widget.NavigationView;
import android.util.AttributeSet;
import com.slipkprojects.sockshttp.util.GoogleFeedbackUtils;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdListener;
import com.slipkprojects.sockshttp.activities.BaseActivity;
import com.slipkprojects.ultrasshservice.tunnel.TunnelUtils;
import android.text.TextUtils;
import com.slipkprojects.sockshttp.preference.LocaleHelper;
import android.support.annotation.Nullable;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Adapter;

/**
 * Activity Principal
 * @author SlipkHunter
 */

public class SocksHttpMainActivity extends BaseActivity
		implements DrawerLayout.DrawerListener,
		View.OnClickListener, RadioGroup.OnCheckedChangeListener,
		CompoundButton.OnCheckedChangeListener, SkStatus.StateListener
{
	private static final String TAG = SocksHttpMainActivity.class.getSimpleName();
	private static final String UPDATE_VIEWS = "MainUpdate";
	public static final String OPEN_LOGS = "com.slipkprojects.sockshttp:openLogs";

	private DrawerLog mDrawer;
	private DrawerPanelMain mDrawerPanel;

	private Settings mConfig;
	private Toolbar toolbar_main;
	private Handler mHandler;

	private LinearLayout mainLayout;
	private LinearLayout loginLayout;
	private LinearLayout proxyInputLayout;
	private TextView proxyText;
	private RadioGroup metodoConexaoRadio;
	private LinearLayout payloadLayout;
	private TextInputEditText payloadEdit;
	private SwitchCompat customPayloadSwitch;
	private Button starterButton;

	private ImageButton inputPwShowPass;
	private TextInputEditText inputPwUser;
	private TextInputEditText inputPwPass;

	private LinearLayout configMsgLayout;
	private TextView configMsgText;

	private AdView adsBannerView;
	private Spinner servidores;

	private Spinner Playload;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mHandler = new Handler();
		mConfig = new Settings(this);
		mDrawer = new DrawerLog(this);
		mDrawerPanel = new DrawerPanelMain(this);

		SharedPreferences prefs = getSharedPreferences(SocksHttpApp.PREFS_GERAL, Context.MODE_PRIVATE);

		boolean showFirstTime = prefs.getBoolean("connect_first_time", true);
		int lastVersion = prefs.getInt("last_version", 0);

		// se primeira vez
		if (showFirstTime)
		{
			SharedPreferences.Editor pEdit = prefs.edit();
			pEdit.putBoolean("connect_first_time", false);
			pEdit.apply();

			Settings.setDefaultConfig(this);

			showBoasVindas();
		}

		try {
			int idAtual = ConfigParser.getBuildId(this);

			if (lastVersion < idAtual) {
				SharedPreferences.Editor pEdit = prefs.edit();
				pEdit.putInt("last_version", idAtual);
				pEdit.apply();

				// se estiver atualizando
				if (!showFirstTime) {
					if (lastVersion <= 12) {
						Settings.setDefaultConfig(this);
						Settings.clearSettings(this);

						Toast.makeText(this, "As configurações foram limpas para evitar bugs",
								Toast.LENGTH_LONG).show();
					}
				}

			}
		} catch(IOException e) {}


		// set layout
		doLayout();

		// verifica se existe algum problema
		//SkProtect.CharlieProtect();

		// recebe local dados
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_VIEWS);
		filter.addAction(OPEN_LOGS);

		LocalBroadcastManager.getInstance(this)
				.registerReceiver(mActivityReceiver, filter);

		doUpdateLayout();
	}


	/**
	 * Layout
	 */

	private void doLayout() {
		setContentView(R.layout.activity_main_drawer);

		toolbar_main = (Toolbar) findViewById(R.id.toolbar_main);
		mDrawerPanel.setDrawer(toolbar_main);
		setSupportActionBar(toolbar_main);

		mDrawer.setDrawer(this);


		// set ADS
		adsBannerView = (AdView) findViewById(R.id.adBannerMainView);

		if (!BuildConfig.DEBUG) {
			//adsBannerView.setAdUnitId(SocksHttpApp.ADS_UNITID_BANNER_MAIN);
		}



		mainLayout = (LinearLayout) findViewById(R.id.activity_mainLinearLayout);
		loginLayout = (LinearLayout) findViewById(R.id.activity_mainInputPasswordLayout);
		starterButton = (Button) findViewById(R.id.activity_starterButtonMain);

		inputPwUser = (TextInputEditText) findViewById(R.id.activity_mainInputPasswordUserEdit);
		inputPwPass = (TextInputEditText) findViewById(R.id.activity_mainInputPasswordPassEdit);

		inputPwShowPass = (ImageButton) findViewById(R.id.activity_mainInputShowPassImageButton);

		((TextView) findViewById(R.id.activity_mainAutorText))
				.setOnClickListener(this);

		proxyInputLayout = (LinearLayout) findViewById(R.id.activity_mainInputProxyLayout);
		proxyText = (TextView) findViewById(R.id.activity_mainProxyText);

		/*Spinner spinnerTunnelType = (Spinner) findViewById(R.id.activity_mainTunnelTypeSpinner);
		 String[] items = new String[]{"SSH DIRECT", "SSH + PROXY", "SSH + SSL (beta)"};
		 //create an adapter to describe how the items are displayed, adapters are used in several places in android.
		 //There are multiple variations of this, but this is the basic variant.
		 ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
		 //set the spinners adapter to the previously created one.
		 spinnerTunnelType.setAdapter(adapter);*/





		metodoConexaoRadio = (RadioGroup) findViewById(R.id.activity_mainMetodoConexaoRadio);
		customPayloadSwitch = (SwitchCompat) findViewById(R.id.activity_mainCustomPayloadSwitch);

		starterButton.setOnClickListener(this);
		proxyInputLayout.setOnClickListener(this);

		payloadLayout = (LinearLayout) findViewById(R.id.activity_mainInputPayloadLinearLayout);
		payloadEdit = (TextInputEditText) findViewById(R.id.activity_mainInputPayloadEditText);

		configMsgLayout = (LinearLayout) findViewById(R.id.activity_mainMensagemConfigLinearLayout);
		configMsgText = (TextView) findViewById(R.id.activity_mainMensagemConfigTextView);

		// fix bugs
		if (mConfig.getPrefsPrivate().getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			if (mConfig.getPrefsPrivate().getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				inputPwUser.setText(mConfig.getPrivString(Settings.USUARIO_KEY));
				inputPwPass.setText(mConfig.getPrivString(Settings.SENHA_KEY));
			}
		}
		else {
			payloadEdit.setText(mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY));
		}


		final SharedPreferences pref = mConfig.getPrefsPrivate();

		//Declarando o xml do spinner
		Playload = (Spinner) findViewById(R.id.payload);

		List<String> ListaPlayload = new ArrayList<String>();
		ListaPlayload.add("Payload-[01]");
		ListaPlayload.add("Payload-[02]");
		ListaPlayload.add("Payload-[03]");
		//ListaPlayload.add("TIM 1");
		//ListaPlayload.add("TIM 2");
		//ListaPlayload.add("TIM 3");
		//ListaPlayload.add("CLARO 1");
		//ListaPlayload.add("CLARO 2");
		//ListaPlayload.add("OI 1");
		ArrayAdapter<String> AdptadorPlayload = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ListaPlayload);
		AdptadorPlayload.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Preenche o Spinner com a lista de servidores
		Playload.setAdapter(AdptadorPlayload);
		//Carrega a posição salva do servidor selecionado
		Playload.setSelection(pref.getInt("Playload", 0));
		//Função ao clicar em 1 dos servidores da lista
		Playload.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

			@Override
			public void onNothingSelected(AdapterView<?> p1)
			{
				// TODO: Implement this method
			}

			@Override
			public void onItemSelected(AdapterView<?> p1, View p2, int position, long p4)
			{
				try
				{
					//Salva a posição do servidor para quando entrar no app ele continuar selecionado
					pref.edit().putInt("Playload", position).apply();
					//Usar payload costumizada
					pref.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
					//Define as configurações do servidor de acordo com o servidor selecionado
					if(position == 0) {
                        //########################################################################
						pref.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, "HTTP/MMSC [lf]Host: www.travsim.com[crlf]Connection: keep-alive[lf][lf][").apply();


					}else if(position == 1){
                        //########################################################################
						pref.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, "CONNECT 127.0.0.1:22[split][lf] HTTP/1.0 [lf]Host: www.google.com[rotate=HOST ][crlf][crlf][").apply();

					}else if(position == 2){

						pref.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, "[protocol] [crlf]Host: www.joox.com[crlf][crlf][crlf]X-Online-Host: www.pinterest.com;www.garena.com.com[crlf][crlf]").apply();

					}else if(position == 3){

						pref.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, "[protocol] [crlf]Host: www.joox.com[crlf][crlf][crlf]X-Online-Host: www.pinterest.com;www.garena.com.com[crlf][crlf]").apply();

					}else if(position == 4){

						pref.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, "PROPFIND /? HTTP/GET Host: www.Alipay.com X-Online-Host: www.Alipay.com Connection: Keep-Alive b User-Agent: Android = 9 (Linnux)/?/?/?/n/r/n/r/n/r/n/r").apply();
					}else if(position == 5){

						pref.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, "ACL HTTP/1.1[crlf]Host: plus.google.com[crlf]").apply();

					}else if(position == 6){

						pref.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, "UNLOCK HTTP/1.0\nOPTIONS http://www.myspace.com.br HTTP/1.1\nHost: http://www.google.com.br\n").apply();

					}else if(position == 7){

						pref.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, "GET HTTP/1.1 [crlf]Host: m.twitter.com[crlf]").apply();

					}else if(position == 8){

						pref.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, "GET HTTP/1.1 [crlf]Host: m.waze.com[crlf]").apply();

					}else if(position == 9){

						pref.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, "PROPFIND http://www.waze.com HTTP/1.0[crlf]Host: http://www.waze.com[crlf]").apply();


					}
					//Atualiza informações
					doUpdateLayout();
				}
				catch (Exception e)
				{}
			}

		});

		metodoConexaoRadio.setOnCheckedChangeListener(this);
		customPayloadSwitch.setOnCheckedChangeListener(this);
		inputPwShowPass.setOnClickListener(this);

		//Onde fica salvo as coisas no aparelho
		final SharedPreferences sPrefs = mConfig.getPrefsPrivate();

		//Declarando o xml do spinner
		servidores = (Spinner) findViewById(R.id.serverSpin);

		// Aqui sao os nomes que vcs quiserem pro servidor, pode ser oq vc quiser(nao importa)
		List<String> ListaServidores = new ArrayList<String>();
		ListaServidores.add("Server-[01]");
		ListaServidores.add("Server-[02]");
		ListaServidores.add("Server-[03]");
		//ListaServidores.add("OI ");


		// Criando adaptador para receber os servidores
		ArrayAdapter<String> AdptadorServidores = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ListaServidores);

		// Definindo layout para o Adaptador
		AdptadorServidores.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Preenche o Spinner com a lista de servidores
		servidores.setAdapter(AdptadorServidores);


		//Carrega a posição salva do servidor selecionado
		servidores.setSelection(sPrefs.getInt("Servidor", 0));

		//Função ao clicar em 1 dos servidores da lista

		servidores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> p1, View p2, int position, long p4)
			{

				try
				{
					//Salva a posição do servidor para quando entrar no app ele continuar selecionado
					sPrefs.edit().putInt("Servidor", position).apply();

					//Usar payload costumizada
					sPrefs.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();

					//Define as configurações do servidor de acordo com o servidor selecionado

					if(position == 0) {
						//Informações do SERVIDOR ########################################################################
						sPrefs.edit().putString(Settings.SERVIDOR_KEY, "191.252.192.178").apply();
						sPrefs.edit().putString(Settings.SERVIDOR_PORTA_KEY, "80").apply();
						//Informações do SERVIDOR ########################################################################


						//Servidor 01 VIVO DIRECT SOCKS 8080,80 | VIVO SQUID 80 SOCKS 8799 ####################
						sPrefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY).apply();
						sPrefs.edit().putString(Settings.PROXY_IP_KEY, "191.252.192.178").apply();
						sPrefs.edit().putString(Settings.PROXY_PORTA_KEY, "80").apply();
						//Servidor 01 ########################################################################


					}else if(position == 1){
						//Servidor 02 TIM ########################################################################
						sPrefs.edit().putString(Settings.PROXY_IP_KEY, "191.252.202.254").apply();
						sPrefs.edit().putString(Settings.PROXY_PORTA_KEY, "80").apply();
						sPrefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY).apply();
                       //Servidor 02 ########################################################################


					}else if(position == 2){
						//Servidor 03 CLARO ########################################################################
						sPrefs.edit().putString(Settings.PROXY_IP_KEY, "191.252.222.16").apply();
						sPrefs.edit().putString(Settings.PROXY_PORTA_KEY, "80").apply();
						sPrefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY).apply();
                         //Servidor 03 ########################################################################


					}else if(position == 3){
						//Servidor 04 OI ########################################################################
						sPrefs.edit().putString(Settings.PROXY_IP_KEY, "SEU IP AQUI").apply();
						sPrefs.edit().putString(Settings.PROXY_PORTA_KEY, "8080").apply();
						sPrefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY).apply();
						//Servidor 04 ########################################################################
					}
					//Atualiza informações
					doUpdateLayout();
				}
				catch (Exception e)
				{}
			}

			@Override
			public void onNothingSelected(AdapterView<?> p1)
			{

			}
		});

		//Sistema de usuario e senha na tela inicial by @JefersonBR
		final SharedPreferences prefsTxt = mConfig.getPrefsPrivate();
		inputPwUser.setText(prefsTxt.getString(Settings.USUARIO_KEY, ""));
		inputPwPass.setText(prefsTxt.getString(Settings.SENHA_KEY, ""));
		inputPwUser.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				if(!s.toString().isEmpty()) {
					prefsTxt.edit().putString(Settings.USUARIO_KEY, s.toString()).apply();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		inputPwPass.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				if(!s.toString().isEmpty()) {
					prefsTxt.edit().putString(Settings.SENHA_KEY, s.toString()).apply();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
	}

	private void doUpdateLayout() {
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		boolean isRunning = SkStatus.isTunnelActive();
		int tunnelType = prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);

		setStarterButton(starterButton, this);
		setPayloadSwitch(tunnelType, !prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true));

		String proxyStr = getText(R.string.no_value).toString();

		if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			proxyStr = "*******";
			proxyInputLayout.setEnabled(false);
		}
		else {
			String proxy = mConfig.getPrivString(Settings.PROXY_IP_KEY);

			if (proxy != null && !proxy.isEmpty())
				proxyStr = String.format("%s:%s", proxy, mConfig.getPrivString(Settings.PROXY_PORTA_KEY));
			proxyInputLayout.setEnabled(!isRunning);
		}

		proxyText.setText(proxyStr);


		switch (tunnelType) {
			case Settings.bTUNNEL_TYPE_SSH_DIRECT:
				((AppCompatRadioButton) findViewById(R.id.activity_mainSSHDirectRadioButton))
						.setChecked(true);
				break;

			case Settings.bTUNNEL_TYPE_SSH_PROXY:
				((AppCompatRadioButton) findViewById(R.id.activity_mainSSHProxyRadioButton))
						.setChecked(true);
				break;
		}

		int msgVisibility = View.VISIBLE;
		int loginVisibility = View.VISIBLE;
		String msgText = "opa";
		boolean enabled_radio = !isRunning;

		if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {

			if (prefs.getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				loginVisibility = View.VISIBLE;

				inputPwUser.setText(mConfig.getPrivString(Settings.USUARIO_KEY));
				inputPwPass.setText(mConfig.getPrivString(Settings.SENHA_KEY));

				inputPwUser.setEnabled(!isRunning);
				inputPwPass.setEnabled(!isRunning);
				inputPwShowPass.setEnabled(!isRunning);

				//inputPwPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			}

			String msg = mConfig.getPrivString(Settings.CONFIG_MENSAGEM_KEY);
			if (!msg.isEmpty()) {
				msgText = msg.replace("\n", "<br/>");
				msgVisibility = View.VISIBLE;
			}

			if (mConfig.getPrivString(Settings.PROXY_IP_KEY).isEmpty() ||
					mConfig.getPrivString(Settings.PROXY_PORTA_KEY).isEmpty()) {
				enabled_radio = false;
			}
		}

		loginLayout.setVisibility(loginVisibility);
		configMsgText.setText(msgText.isEmpty() ? "" : Html.fromHtml(msgText));
		configMsgLayout.setVisibility(msgVisibility);

		// desativa/ativa radio group
		for (int i = 0; i < metodoConexaoRadio.getChildCount(); i++) {
			metodoConexaoRadio.getChildAt(i).setEnabled(enabled_radio);
		}
	}


	private synchronized void doSaveData() {
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		SharedPreferences.Editor edit = prefs.edit();

		if (mainLayout != null && !isFinishing())
			mainLayout.requestFocus();

		if (!prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			if (payloadEdit != null && !prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true)) {
				edit.putString(Settings.CUSTOM_PAYLOAD_KEY, payloadEdit.getText().toString());
			}
		}
		else {
			if (prefs.getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				edit.putString(Settings.USUARIO_KEY, inputPwUser.getEditableText().toString());
				edit.putString(Settings.SENHA_KEY, inputPwPass.getEditableText().toString());
			}
		}

		edit.apply();
	}


	/**
	 * Tunnel SSH
	 */

	public void startOrStopTunnel(Activity activity) {
		if (SkStatus.isTunnelActive()) {
			TunnelManagerHelper.stopSocksHttp(activity);
		}
		else {
			// oculta teclado se vísivel, tá com bug, tela verde
			//Utils.hideKeyboard(activity);

			Settings config = new Settings(activity);

			if (config.getPrefsPrivate()
					.getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				if (inputPwUser.getText().toString().isEmpty() ||
						inputPwPass.getText().toString().isEmpty()) {
					Toast.makeText(this, R.string.error_userpass_empty, Toast.LENGTH_SHORT)
							.show();
					return;
				}
			}

			Intent intent = new Intent(activity, LaunchVpn.class);
			intent.setAction(Intent.ACTION_MAIN);

			if (config.getHideLog()) {
				intent.putExtra(LaunchVpn.EXTRA_HIDELOG, true);
			}

			activity.startActivity(intent);
		}
	}

	private void setPayloadSwitch(int tunnelType, boolean isCustomPayload) {
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		boolean isRunning = SkStatus.isTunnelActive();

		customPayloadSwitch.setChecked(isCustomPayload);

		if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			payloadEdit.setEnabled(false);

			if (mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY).isEmpty()) {
				customPayloadSwitch.setEnabled(false);
			}
			else {
				customPayloadSwitch.setEnabled(!isRunning);
			}

			if (!isCustomPayload && tunnelType == Settings.bTUNNEL_TYPE_SSH_PROXY)
				payloadEdit.setText(Settings.PAYLOAD_DEFAULT);
			else
				payloadEdit.setText("*******");
		}
		else {
			customPayloadSwitch.setEnabled(!isRunning);

			if (isCustomPayload) {
				payloadEdit.setText(mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY));
				payloadEdit.setEnabled(!isRunning);
			}
			else if (tunnelType == Settings.bTUNNEL_TYPE_SSH_PROXY) {
				payloadEdit.setText(Settings.PAYLOAD_DEFAULT);
				payloadEdit.setEnabled(false);
			}
		}

		if (isCustomPayload || tunnelType == Settings.bTUNNEL_TYPE_SSH_PROXY) {
			payloadLayout.setVisibility(View.GONE);
		}
		else {
			payloadLayout.setVisibility(View.GONE);
		}
	}

	public void setStarterButton(Button starterButton, Activity activity) {
		String state = SkStatus.getLastState();
		boolean isRunning = SkStatus.isTunnelActive();

		if (starterButton != null) {
			int resId;

			SharedPreferences prefsPrivate = new Settings(activity).getPrefsPrivate();

			if (SkStatus.SSH_INICIANDO.equals(state)) {
				resId = R.string.stop;
				starterButton.setEnabled(true);
			}
			else if (SkStatus.SSH_PARANDO.equals(state)) {
				resId = R.string.state_stopping;
				starterButton.setEnabled(true);
			}
			else {
				resId = isRunning ? R.string.stop : R.string.start;
				starterButton.setEnabled(true);
			}

			starterButton.setText(resId);
		}
	}



	@Override
	public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onPostCreate(savedInstanceState, persistentState);
		if (mDrawerPanel.getToogle() != null)
			mDrawerPanel.getToogle().syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerPanel.getToogle() != null)
			mDrawerPanel.getToogle().onConfigurationChanged(newConfig);
	}

	private boolean isMostrarSenha = false;

	@Override
	public void onClick(View p1)
	{
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		switch (p1.getId()) {
			case R.id.activity_starterButtonMain:
				doSaveData();
				startOrStopTunnel(this);
				break;

			case R.id.activity_mainInputProxyLayout:
				if (!prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
					doSaveData();

					DialogFragment fragProxy = new ProxyRemoteDialogFragment();
					fragProxy.show(getSupportFragmentManager(), "proxyDialog");
				}
				break;

			case R.id.activity_mainAutorText:
				String url = "https://api.whatsapp.com/send?phone=554799835437";
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(Intent.createChooser(intent, getText(R.string.open_with)));
				break;

			case R.id.activity_mainInputShowPassImageButton:
				isMostrarSenha = !isMostrarSenha;
				if (isMostrarSenha) {
					inputPwPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					inputPwShowPass.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_visibility_black_24dp));
				}
				else {
					inputPwPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					inputPwShowPass.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_visibility_off_black_24dp));
				}
				break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup p1, int p2)
	{
		SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();

		switch (p1.getCheckedRadioButtonId()) {
			case R.id.activity_mainSSHDirectRadioButton:
				edit.putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);
				proxyInputLayout.setVisibility(View.GONE);
				break;

			case R.id.activity_mainSSHProxyRadioButton:
				edit.putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY);
				proxyInputLayout.setVisibility(View.GONE);
				break;
		}

		edit.apply();

		doSaveData();
		doUpdateLayout();
	}

	@Override
	public void onCheckedChanged(CompoundButton p1, boolean p2)
	{
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		SharedPreferences.Editor edit = prefs.edit();

		switch (p1.getId()) {
			case R.id.activity_mainCustomPayloadSwitch:
				edit.putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, !p2);
				setPayloadSwitch(prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT), p2);
				break;
		}

		edit.apply();

		doSaveData();
	}

	protected void showBoasVindas() {
		new AlertDialog.Builder(this)
				. setTitle(R.string.attention)
				. setMessage(R.string.first_start_msg)
				. setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface di, int p) {
						// ok
					}
				})
				. setCancelable(false)
				. show();
	}

	@Override
	public void updateState(final String state, String msg, int localizedResId, final ConnectionStatus level, Intent intent)
	{
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				doUpdateLayout();
			}
		});

		switch (state) {
			case SkStatus.SSH_CONECTADO:
				// carrega ads banner
				break;
		}
	}


	/**
	 * Recebe locais Broadcast
	 */

	private BroadcastReceiver mActivityReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null)
				return;

			if (action.equals(UPDATE_VIEWS) && !isFinishing()) {
				doUpdateLayout();
			}
			else if (action.equals(OPEN_LOGS)) {
				if (mDrawer != null && !isFinishing()) {
					DrawerLayout drawerLayout = mDrawer.getDrawerLayout();

					if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
						drawerLayout.openDrawer(GravityCompat.END);
					}
				}
			}
		}
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerPanel.getToogle() != null && mDrawerPanel.getToogle().onOptionsItemSelected(item)) {
			return true;
		}

		// Menu Itens
		switch (item.getItemId()) {

			case R.id.miLimparConfig:
				if (!SkStatus.isTunnelActive()) {
					DialogFragment dialog = new ClearConfigDialogFragment();
					dialog.show(getSupportFragmentManager(), "alertClearConf");
				} else {
					Toast.makeText(this, R.string.error_tunnel_service_execution, Toast.LENGTH_SHORT)
							.show();
				}
				break;

			case R.id.miSettings:
				Intent intentSettings = new Intent(this, ConfigGeralActivity.class);
				//intentSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentSettings);
				break;


			// logs opções
			case R.id.miLimparLogs:
				mDrawer.clearLogs();
				break;

			case R.id.miExit:
				if (Build.VERSION.SDK_INT >= 16) {
					finishAffinity();
				}

				System.exit(0);
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		DrawerLayout layout = mDrawer.getDrawerLayout();

		if (mDrawerPanel.getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
			mDrawerPanel.getDrawerLayout().closeDrawers();
		}
		else if (layout.isDrawerOpen(GravityCompat.END)) {
			// fecha drawer
			layout.closeDrawers();
		}
		else {
			// mostra opção para sair
			showExitDialog();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mDrawer.onResume();

		//doSaveData();
		//doUpdateLayout();

		SkStatus.addStateListener(this);

	}

	@Override
	protected void onPause()
	{
		super.onPause();

		doSaveData();

		SkStatus.removeStateListener(this);

		if (adsBannerView != null) {
			adsBannerView.pause();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		mDrawer.onDestroy();

		LocalBroadcastManager.getInstance(this)
				.unregisterReceiver(mActivityReceiver);

		if (adsBannerView != null) {
			adsBannerView.destroy();
		}
	}


	/**
	 * DrawerLayout Listener
	 */

	@Override
	public void onDrawerOpened(View view) {
		if (view.getId() == R.id.activity_mainLogsDrawerLinear) {
			toolbar_main.getMenu().clear();
			getMenuInflater().inflate(R.menu.logs_menu, toolbar_main.getMenu());
		}
	}

	@Override
	public void onDrawerClosed(View view) {
		if (view.getId() == R.id.activity_mainLogsDrawerLinear) {
			toolbar_main.getMenu().clear();
			getMenuInflater().inflate(R.menu.main_menu, toolbar_main.getMenu());
		}
	}

	@Override
	public void onDrawerStateChanged(int stateId) {}
	@Override
	public void onDrawerSlide(View view, float p2) {}


	/**
	 * Utils
	 */

	public static void updateMainViews(Context context) {
		Intent updateView = new Intent(UPDATE_VIEWS);
		LocalBroadcastManager.getInstance(context)
				.sendBroadcast(updateView);
	}

	public void showExitDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this).
				create();
		dialog.setTitle(getString(R.string.attention));
		dialog.setMessage(getString(R.string.alert_exit));

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.
						string.exit),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						Utils.exitAll(SocksHttpMainActivity.this);
					}
				}
		);

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.
						string.minimize),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// minimiza app
						Intent startMain = new Intent(Intent.ACTION_MAIN);
						startMain.addCategory(Intent.CATEGORY_HOME);
						startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(startMain);
					}
				}
		);

		dialog.show();
	}
}

