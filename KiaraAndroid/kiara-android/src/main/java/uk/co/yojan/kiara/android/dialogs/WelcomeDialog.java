package uk.co.yojan.kiara.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import butterknife.ButterKnife;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;


public class WelcomeDialog extends DialogFragment {

  KiaraActivity activity;

  public Button positiveButton;
  public Button negativeButton;

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    activity = (KiaraActivity) getActivity();

    View view = activity.getLayoutInflater().inflate(R.layout.welcome_dialog, null);
    ButterKnife.inject(this, view);

    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        activity.sharedPreferences().edit().putBoolean(Constants.TERMS_AGREED, true).commit();
      }
    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        // User cancelled the dialog
      }
    });

//    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    builder.setView(view);
    return builder.create();
  }

  @Override
  public void onStart() {
    super.onStart();
    AlertDialog d = (AlertDialog) getDialog();
    if (d != null) {
      positiveButton = d.getButton(AlertDialog.BUTTON_POSITIVE);
      negativeButton = d.getButton(AlertDialog.BUTTON_NEGATIVE);
    }
  }
}
