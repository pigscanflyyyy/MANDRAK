package com.slipkprojects.sockshttp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.slipkprojects.sockshttp.R;
import com.slipkprojects.sockshttp.SocksHttpMainActivity;
import com.slipkprojects.ultrasshservice.config.Settings;
import com.slipkprojects.ultrasshservice.logger.SkStatus;

public class ClearConfigDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog dialog = new AlertDialog.Builder(getActivity()).
			create();
		dialog.setTitle(getActivity().getString(R.string.att));
		dialog.setMessage(getActivity().getString(R.string.alert_clear_settings));

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getActivity().getString(R.
			string.yes),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Settings.clearSettings(getContext());
					
					// limpa logs
					SkStatus.clearLog();
					
					SocksHttpMainActivity.updateMainViews(getContext());
					
					Toast.makeText(getActivity(), R.string.success_clear_settings, Toast.LENGTH_SHORT)
						.show();
				}
			}
		);

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getActivity().getString(R.
			string.no),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismiss();
				}
			}
		);
		
		return dialog;
	}

}
