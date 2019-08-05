package org.witness.iwitness;

import java.util.ArrayList;
import java.util.Iterator;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.ui.CameraActivity;
import org.witness.informacam.InformaCam;
import org.witness.informacam.InformaCam.LocalBinder;
import org.witness.informacam.ui.WizardActivity;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;

import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.HomeActivity;
import org.witness.iwitness.app.LoginActivity;
import org.witness.iwitness.app.screens.wizard.AddOrganizationsPreference;
import org.witness.iwitness.app.screens.wizard.OriginalImagePreference;
import org.witness.iwitness.utils.Constants;
import org.witness.iwitness.utils.Constants.App;
import org.witness.iwitness.utils.Constants.App.Editor;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.App.Camera;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.Constants.App.Login;
import org.witness.iwitness.utils.Constants.App.Wizard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class IWitness extends Activity implements InformaCamStatusListener {
	Intent init = null;
	Intent route = null;
	int routeCode;
	
	private final static String LOG = Constants.App.Router.LOG;
	private String packageName;
	
	private Handler h = new Handler();
	private ServiceConnection sc;
	
	InformaCam informaCam;
	

	private final static String HOCKEY_APP_ID = "819d2172183272c9d84cd3a4dbd9296b";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		packageName = getClass().getName();
		
		Log.d(LOG, "hello " + packageName);
		init = getIntent();
		
		setContentView(R.layout.activity_main);
		sc = new ServiceConnection() {

			@SuppressWarnings("static-access")
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				informaCam = ((LocalBinder) service).getService().getInstance(IWitness.this); 
				
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				informaCam = null;
			}
			
		};
		bindService(new Intent(this, InformaCam.class), sc, Context.BIND_AUTO_CREATE);
		
		try {
			Iterator<String> i = savedInstanceState.keySet().iterator();
			while(i.hasNext()) {
				String outState = i.next();
				if(outState.equals(Home.TAG) && savedInstanceState.getBoolean(App.TAG)) {
					//onInformaCamStart();
				}
			}
		} catch(NullPointerException e) {}
		
		checkForUpdates(); //hockeyapp
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(LOG, "ON RESUME");
		try {
			informaCam = InformaCam.getInstance(this);
			if(route != null) {
				routeByIntent();
				Log.d(LOG, "we have a route! lets go!");
			} else {
				Log.d(LOG, "route is null now, please wait");
				if(informaCam.isAbsolutelyLoggedIn()) {
					route = new Intent(this, HomeActivity.class);
					routeCode = Home.ROUTE_CODE;
					routeByIntent();
				} else {
					Log.d(LOG, "no, not logged in");
				}
			}
			
		} catch(NullPointerException e) {
			Log.e(LOG, "informacam has not started again yet");
		}
		

		checkForUpdates(); //hockeyapp
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		unbindService(sc);
		super.onDestroy();
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(App.TAG, true);
		Log.d(LOG, "ON SAVE STATE INSTANCE");
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {		
		if(resultCode == Activity.RESULT_CANCELED) {
			Log.d(LOG, "finishing with request code " + requestCode);
			finish();
		} else if(resultCode == Activity.RESULT_OK) {
			route = new Intent(this, HomeActivity.class);
			routeCode = Home.ROUTE_CODE;
			
			switch(requestCode) {
			case Codes.Routes.WIZARD:
				FormUtility.installIncludedForms(this);
				break;
			case Codes.Routes.CAMERA:
				route = new Intent(this, EditorActivity.class);
				routeCode = Editor.ROUTE_CODE;
				
				break;
			case Codes.Routes.EDITOR:
				
				break;
			case Codes.Routes.LOGIN:
				Log.d(LOG, "hi i logged in!");
				informaCam.startup();
				break;
			}
			
			routeByIntent();
		}
	}
	
	private void routeByIntent() {
		Log.d(LOG, "intent is: " + init.getAction());

		if(Intent.ACTION_MAIN.equals(init.getAction())) {
			// do some extra logic if necessary (but not yet...)
		} else if("android.media.action.IMAGE_CAPTURE".equals(init.getAction())) {
			route = new Intent(this, CameraActivity.class);
			routeCode = Camera.ROUTE_CODE;
		} else if(Intent.ACTION_VIEW.equals(init.getAction())) {
			route.setData(init.getData());
		}
		
		route.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(route, routeCode);
	}

	@Override
	public void onInformaCamStart(Intent intent) {
		Log.d(LOG, "STARTING INFORMACAM ON IWITNESS");
		
		int code = intent.getBundleExtra(org.witness.informacam.utils.Constants.Codes.Keys.SERVICE).getInt(org.witness.informacam.utils.Constants.Codes.Extras.MESSAGE_CODE);
		
		switch(code) {
		case org.witness.informacam.utils.Constants.Codes.Messages.Wizard.INIT:
			ArrayList<String> wizardFragments = new ArrayList<String>();
			wizardFragments.add(OriginalImagePreference.class.getName());
			wizardFragments.add(AddOrganizationsPreference.class.getName());
			
			route = new Intent(this, WizardActivity.class);
			route.putStringArrayListExtra(Codes.Extras.WIZARD_SUPPLEMENT, wizardFragments);
			routeCode = Wizard.ROUTE_CODE;
			break;
		case org.witness.informacam.utils.Constants.Codes.Messages.Login.DO_LOGIN:
			route = new Intent(this, LoginActivity.class);
			routeCode = Login.ROUTE_CODE;
			break;
		case org.witness.informacam.utils.Constants.Codes.Messages.Home.INIT:
			route = new Intent(this, HomeActivity.class);
			routeCode = Home.ROUTE_CODE;
			break;
		}
		
		routeByIntent();
	}

	@Override
	public void onInformaCamStop(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(LOG, "I STOPPED INFORMACAM");
	}

	@Override
	public void onInformaStop(Intent intent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInformaStart(Intent intent) {
		// TODO Auto-generated method stub
		
	}
	
	private void checkForCrashes() {
	   CrashManager.register(this, HOCKEY_APP_ID);
	 }

	 private void checkForUpdates() {
	   // Remove this for store builds!
	   UpdateManager.register(this, HOCKEY_APP_ID);
	 }
}
