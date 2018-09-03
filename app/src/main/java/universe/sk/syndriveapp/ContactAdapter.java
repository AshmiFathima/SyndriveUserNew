package universe.sk.syndriveapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ContactAdapter extends ArrayAdapter<Contact> implements View.OnClickListener {
    Context context;
    private ArrayList<Contact> contacts;

    SharedPreferences prefs;
    public static final String MY_PREFS_FILENAME = "universe.sk.syndriveapp.Contacts";
    private static final String CONTACT_NAMES = "contactNames";
    private static final String CONTACT_NUMBERS = "contactNumbers";

    // View lookup cache
    private static class ViewHolder {
        TextView contactName, contactNumber;
        ImageView ivCall, ivDelete;
    }

    public ContactAdapter(ArrayList<Contact> data, Context context) {
        super(context, R.layout.row_contact, data);
        this.contacts = data;
        this.context = context;
        prefs = context.getSharedPreferences(MY_PREFS_FILENAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        Contact contact = (Contact) object;

        switch (v.getId())
        {
            case R.id.ivCall:
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel:" + contact.getContactNumber()));
                context.startActivity(intent);
                break;
            case R.id.ivDelete:
                this.contacts.remove(contact);
                this.notifyDataSetChanged();
                SharedPreferences.Editor editor = prefs.edit();
                Set<String> contactNames = prefs.getStringSet(CONTACT_NAMES, new LinkedHashSet<String>());
                Set<String> contactNumbers = prefs.getStringSet(CONTACT_NUMBERS, new LinkedHashSet<String>());
                contactNames.remove(contact.getContactName());
                contactNumbers.remove(contact.getContactNumber());
                editor.putStringSet(CONTACT_NAMES, contactNames);
                editor.putStringSet(CONTACT_NUMBERS, contactNumbers);
                editor.commit();
                Snackbar.make(v, "Deleted", Snackbar.LENGTH_SHORT)
                        .setAction("No action", null).show();
                break;
        }
    }

    private int lastPosition = -1;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Contact contact = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_contact, parent, false);
            viewHolder.contactName = convertView.findViewById(R.id.tvContactName);
            viewHolder.contactNumber = convertView.findViewById(R.id.tvContactNumber);
            viewHolder.ivCall = convertView.findViewById(R.id.ivCall);
            viewHolder.ivDelete = convertView.findViewById(R.id.ivDelete);

            result = convertView;
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        viewHolder.contactName.setText(contact.getContactName());
        viewHolder.contactNumber.setText(contact.getContactNumber());
        viewHolder.ivCall.setOnClickListener(this);
        viewHolder.ivCall.setTag(position);
        viewHolder.ivDelete.setOnClickListener(this);
        viewHolder.ivDelete.setTag(position);

        return convertView; //return the completed view to render on screen
        //return super.getView(position, convertView, parent);
    }
}
