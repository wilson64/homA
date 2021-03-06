package st.alr.homA;

import java.util.regex.Pattern;

import st.alr.homA.MqttService.MQTT_CONNECTIVITY;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class PreferencesServer extends DialogPreference {
	private Context context;
	private EditText address;
	private EditText port;
    private boolean addressOk = false;
    private boolean portOk = false; 

	public PreferencesServer(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		setDialogLayoutResource(R.layout.preferences_server);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
	}

	@Override
	protected View onCreateDialogView() {
		View root = super.onCreateDialogView();
		address = (EditText) root.findViewById(R.id.serverAddress);
		port = (EditText) root.findViewById(R.id.serverPort);

		return root;
	}

	@Override
	protected void onBindDialogView(View view) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		address.setText(prefs.getString("serverAddress", App.defaultsServerAddress));
		port.setText(prefs.getString("serverPort", App.defaultsServerPort));
	}
	
	private void validateAddress(String s){
// Lazy validation by length until we implement something to check for: local hosts, domains, ipv4, ipv6 	    
//        if (android.util.Patterns.DOMAIN_NAME.matcher(s).matches()
//                || android.util.Patterns.IP_ADDRESS.matcher(s).matches())
	    if(s.length() > 0) 
            addressOk = true;
        else
            addressOk = false;

	}
	
	   private void validatePort(String s) {
           Integer p = 0;
           try {
               p = Integer.parseInt(s.toString());
           } catch (NumberFormatException e) {
           }

           if ((p > 0) && (p <= 65535))
               portOk = true;
           else
               portOk = false;
	   }
	   
	   private void conditionalyEnableConnectButton(){
           View v = getDialog().findViewById(android.R.id.button1);
           if (v == null)
               return;
           Log.v("addressOk", ""+addressOk);
           Log.v("port", ""+portOk);

           if (addressOk && portOk)
               v.setEnabled(true);
           else
               v.setEnabled(false);
           
	   }
	   
    private void conditionallyEnableDisconnectButton() {
        View v = getDialog().findViewById(android.R.id.button2);
        if (v == null)
            return;

        if (MqttService.getConnectivity() == MQTT_CONNECTIVITY.DISCONNECTED_WAITINGFORINTERNET
                || MqttService.getConnectivity() == MQTT_CONNECTIVITY.DISCONNECTED_USERDISCONNECT
                || MqttService.getConnectivity() == MQTT_CONNECTIVITY.DISCONNECTED_DATADISABLED
                || MqttService.getConnectivity() == MQTT_CONNECTIVITY.DISCONNECTED) {
            v.setEnabled(false);
        } else {
            v.setEnabled(true);
        }

    }   
	           
	@Override
	protected void showDialog(Bundle state) {
	       super.showDialog(state);

	    validateAddress(PreferenceManager.getDefaultSharedPreferences(context).getString("serverAddress", App.defaultsServerAddress));
        validatePort(PreferenceManager.getDefaultSharedPreferences(context).getString("serverPort", App.defaultsServerPort));
        conditionalyEnableConnectButton();
        conditionallyEnableDisconnectButton();
        
        
	       address.addTextChangedListener(new TextWatcher() {
	            
	            @Override
	            public void onTextChanged(CharSequence s, int start, int before, int count) {}
	            
	            @Override
	            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	            
	            @Override
	            public void afterTextChanged(Editable s) {
	                validateAddress(s.toString());
	                conditionalyEnableConnectButton();
	            }
	        });

	        port.addTextChangedListener(new TextWatcher() {
	            
	            @Override
	            public void onTextChanged(CharSequence s, int start, int before, int count) {}
	            
	            @Override
	            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	            
	            @Override
	            public void afterTextChanged(Editable s) {
	                validatePort(s.toString());
	                conditionalyEnableConnectButton();
	            }
	        });
	    
	}



    @Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE: // User clicked ok

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = prefs.edit();

				String newAdress = address.getText().toString();
				String newPort = port.getText().toString();

				editor.putString("serverAddress", newAdress);
				editor.putString("serverPort", newPort);

				editor.apply();

				MqttService.getInstance().reconnect();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
			    MqttService.getInstance().disconnect(true);
		}
		super.onClick(dialog, which);
	}

}