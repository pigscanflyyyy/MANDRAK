package com.slipkprojects.sockshttp;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.slipkprojects.sockshttp.activities.AboutActivity;
import com.slipkprojects.sockshttp.activities.ConfigGeralActivity;
import com.slipkprojects.sockshttp.util.Utils;

public class DrawerPanelMain
		implements NavigationView.OnNavigationItemSelectedListener
{
	private AppCompatActivity mActivity;

	public DrawerPanelMain(AppCompatActivity activity) {
		mActivity = activity;
	}


	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle toggle;

	public void setDrawer(Toolbar toolbar) {
		NavigationView drawerNavigationView = (NavigationView) mActivity.findViewById(R.id.drawerNavigationView);
		drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawerLayoutMain);

		// set drawer
		toggle = new ActionBarDrawerToggle(mActivity,
				drawerLayout, toolbar, R.string.open, R.string.cancel);

		drawerLayout.setDrawerListener(toggle);

		toggle.syncState();

		// set app info
		PackageInfo pinfo = Utils.getAppInfo(mActivity);
		if (pinfo != null) {
		}

		// set navigation view
		drawerNavigationView.setNavigationItemSelectedListener(this);
	}

	public ActionBarDrawerToggle getToogle() {
		return toggle;
	}

	public DrawerLayout getDrawerLayout() {
		return drawerLayout;
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();

		switch(id)
		{
			case R.id.miPhoneConfg:
				PackageInfo app_info = Utils.getAppInfo(mActivity);
				if (app_info != null) {
					String aparelho_marca = Build.BRAND.toUpperCase();

					if (aparelho_marca.equals("SAMSUNG") || aparelho_marca.equals("HUAWEY")) {
						Toast.makeText(mActivity, R.string.error_no_supported, Toast.LENGTH_SHORT)
								.show();
					}
					else {
						try {
							Intent in = new Intent(Intent.ACTION_MAIN);
							in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							in.setClassName("com.android.settings", "com.android.settings.RadioInfo");
							mActivity.startActivity(in);
						} catch(Exception e) {
							Toast.makeText(mActivity, R.string.error_no_supported, Toast.LENGTH_SHORT)
									.show();
						}
					}
				}
				break;

			case R.id.miSettings:
				Intent intent = new Intent(mActivity, ConfigGeralActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mActivity.startActivity(intent);
				break;

			case R.id.miAvaliarPlaystore:
				String url = "https://drive.google.com/u/0/uc?id=1upKGhb5Vwdocto-_Ttp1byMSzgTZoTux&export=download";
				Intent intent3 = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mActivity.startActivity(Intent.createChooser(intent3, mActivity.getText(R.string.open_with)));
				break;

			case R.id.miContato:
				url = "https://api.whatsapp.com/send?phone=554799835437&text=Esqueci%20minha%20senha%20ssh";
				Intent intent4 = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mActivity.startActivity(Intent.createChooser(intent4, mActivity.getText(R.string.open_with)));
				break;

			case R.id.miAbout:
				if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
					drawerLayout.closeDrawers();
				}
				Intent aboutIntent = new Intent(mActivity, AboutActivity.class);
				mActivity.startActivity(aboutIntent);
				break;



			/*case R.id.miSendFeedback:
				if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					try {
						GoogleFeedbackUtils.bindFeedback(mActivity);
					} catch (Exception e) {
						Toast.makeText(mActivity, "Não disponível em seu aparelho", Toast.LENGTH_SHORT)
							.show();
						SkStatus.logDebug("Error: " + e.getMessage());
					}
				}
				else {
					Intent email = new Intent(Intent.ACTION_SEND);  
					email.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					email.putExtra(Intent.EXTRA_EMAIL, new String[]{"slipkprojects@gmail.com"});  
					email.putExtra(Intent.EXTRA_SUBJECT, "SocksHttp - " + mActivity.getString(R.string.feedback));  
					//email.putExtra(Intent.EXTRA_TEXT, "");  

					//need this to prompts email client only  
					email.setType("message/rfc822");  

					mActivity.startActivity(Intent.createChooser(email, "Choose an Email client:"));
				}
				break;
				
			/*case R.id.miAbout:
				if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            		drawerLayout.closeDrawers();
        		}
				Intent aboutIntent = new Intent(mActivity, AboutActivity.class);
				mActivity.startActivity(aboutIntent);
				break;*/
		}

		return true;
	}

}
