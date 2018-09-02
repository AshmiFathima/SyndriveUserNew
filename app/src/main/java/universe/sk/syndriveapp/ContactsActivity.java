package universe.sk.syndriveapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ContactsActivity extends AppCompatActivity {

    ListView lvContactList;
    ArrayList<Contact> contacts = new ArrayList<>();
    private ContactAdapter adapter;
    Button btnSave;

    public static final String MY_PREFS_FILENAME = "universe.sk.syndriveapp.Contacts";
    private static final String CONTACT_NAMES = "contactNames";
    private static final String CONTACT_NUMBERS = "contactNumbers";

    SharedPreferences prefs ;

    private final int REQUEST_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.contacts);
        actionBar.setTitle(" Emergency Contacts");
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        btnSave = findViewById(R.id.btnSave);
        lvContactList = findViewById(R.id.lvContactList);

        Set<String> contactNames = new LinkedHashSet<>();
        Set<String> contactNumbers = new LinkedHashSet<>();

        prefs = getSharedPreferences(MY_PREFS_FILENAME, Context.MODE_PRIVATE);

        // Add initial values (empty)

        if(prefs.getStringSet(CONTACT_NAMES, null) == null){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet(CONTACT_NAMES, contactNames);
            editor.putStringSet(CONTACT_NUMBERS, contactNumbers);
            editor.commit();
        }

        adapter = new ContactAdapter(contacts, getApplicationContext());
        lvContactList.setAdapter(adapter);

        loadData();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactsActivity.this, NavigationActivity.class));
            }
        });

    } // end of onCreate

    private void loadData() {
        // read the contacts from sharedPreferences
        Set<String> contactNames = prefs.getStringSet(CONTACT_NAMES, new LinkedHashSet<String>());
        Set<String> contactNumbers = prefs.getStringSet(CONTACT_NUMBERS, new LinkedHashSet<String>());

        String name, number;
        Iterator<String> itrNames = contactNames.iterator();
        Iterator<String> itrNumbers = contactNumbers.iterator();

        contacts.clear();

        while (itrNames.hasNext()) {
            name = itrNames.next();
            number = itrNumbers.next();
            contacts.add(new Contact(name, number));
        }
        adapter.notifyDataSetChanged();
    } // end of loadData

    private void addData(String name, String number) {

        SharedPreferences.Editor editor = prefs.edit();
        Set<String> contactNames = prefs.getStringSet(CONTACT_NAMES, new LinkedHashSet<String>());
        Set<String> contactNumbers = prefs.getStringSet(CONTACT_NUMBERS, new LinkedHashSet<String>());
        contactNames.add(name);
        contactNumbers.add(number);
        editor.putStringSet(CONTACT_NAMES, contactNames);
        editor.putStringSet(CONTACT_NUMBERS, contactNumbers);
        editor.commit();

    } // end of addData

//    private void removeContactData(String name, String number) {
//        SharedPreferences.Editor editor = prefs.edit();
//        contactNames.remove(name);
//        contactNumbers.remove(number);
//        editor.putStringSet(CONTACT_NAMES, contactNames);
//        editor.putStringSet(CONTACT_NUMBERS, contactNumbers);
//        editor.commit();
//
//    } // end of removeContactData

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.addcontacts, menu);
        //return true;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_contact) {
            // start contact picker intent
            Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(intent, REQUEST_CONTACTS);
            //Toast.makeText(this, "Contact Added", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CONTACTS && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String names[] = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
            Cursor cursor = getContentResolver().query(uri, names, null, null, null);
            cursor.moveToFirst();
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String name = cursor.getString(column);
            cursor.close();
            String numbers[] = {ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor1 = getContentResolver().query(uri, numbers, null, null, null);
            cursor1.moveToFirst();
            column = cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String number = cursor1.getString(column);
            cursor1.close();

            addData(name, number);
            loadData();

        }
    } // end of onActivityResult

} // end of AddContactsActivity