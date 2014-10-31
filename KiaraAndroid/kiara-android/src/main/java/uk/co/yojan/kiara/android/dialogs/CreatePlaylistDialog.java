package uk.co.yojan.kiara.android.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.events.CreatePlaylistRequest;

public class CreatePlaylistDialog extends DialogFragment {
  @InjectView(R.id.create_playlist_edit) EditText playlistName;

  public CreatePlaylistDialog() {

  }

//  @Override
//  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
//    View view = inflater.inflate(R.layout.create_playlist_dialog, container);
//    ButterKnife.inject(this, view);
//    return view;
//  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Activity activity = getActivity();
    View view = activity.getLayoutInflater().inflate(R.layout.create_playlist_dialog, null);
    ButterKnife.inject(this, view);
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setView(view)
        .setTitle(R.string.playlist_name)
        .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            ((KiaraActivity)getActivity()).getBus()
                .post(new CreatePlaylistRequest(playlistName.getText().toString()));
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // User cancelled the dialog, do nothing.
            dialogInterface.cancel();
          }
        })
        .setIcon(android.R.drawable.btn_plus);

    return builder.create();
  }
}
