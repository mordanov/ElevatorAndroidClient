package elevators.android.elevatorclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

import static android.text.InputType.TYPE_CLASS_NUMBER;

public class AskIpAddressDialog extends DialogFragment {

    private String inputdata = "";

    public interface AskIpAddressListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    AskIpAddressListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AskIpAddressListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AskIpAddressListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());
        input.setMaxLines(1);
        input.setSingleLine(true);
        input.setInputType(TYPE_CLASS_NUMBER);                              // ограничить ввод только цифрами и точкой
        input.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        return builder
                .setView(input)
                .setTitle("Введите IP-адрес сервера")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputdata = input.getText().toString();
                        mListener.onDialogPositiveClick(AskIpAddressDialog.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(AskIpAddressDialog.this);
                        dialog.cancel();
                    }
                })
                .create();
    }

    public String getInputdata() {
        return inputdata;
    }
}
