package ru.ifmo.md.colloquium2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by dimatomp on 11.11.14.
 */
public class CandidatePreferences extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String nameBefore;
        final View view = getActivity().getLayoutInflater().inflate(R.layout.candidate_name_field, null);
        if (getArguments() != null && getArguments().containsKey("candidateName"))
            nameBefore = getArguments().getString("candidateName");
        else
            nameBefore = null;
        final EditText editText = (EditText) view.findViewById(R.id.candidate_name);
        editText.setText(nameBefore);
        final CheckBox box = (CheckBox) view.findViewById(R.id.remove_him);
        if (nameBefore != null)
            box.setVisibility(View.VISIBLE);
        builder.setTitle("Candidate Name").setView(view)
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                }).setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (nameBefore == null)
                    DataStorage.addCandidate(getActivity(), editText.getText().toString());
                else {
                    if (!box.isChecked())
                        DataStorage.editCandidate(getActivity(), nameBefore, editText.getText().toString());
                    else
                        DataStorage.removeCandidate(getActivity(), nameBefore);
                }
                getActivity().getLoaderManager().restartLoader(0, null, (MainActivity) getActivity()).forceLoad();
                dismiss();
            }
        });
        return builder.create();
    }
}
