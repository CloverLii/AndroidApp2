package comp575.helloworld;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.widget.Adapter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ArrayList<Contact> contacts = new ArrayList<Contact>();
    private ArrayAdapter<Contact> adapter;
    private ListView contactListView;
    private ContactRepository contactRepository;
    private LiveData<List<Contact>> allContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup Adapter
        contactListView = findViewById(R.id.contactsListView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, contacts);

        //check device orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){

            contactListView.setAdapter(adapter);
            contactListView.setOnItemClickListener(this);
        }

        //register an observer
        contactRepository = new ContactRepository(this);
        contactRepository.getAllContacts().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(@Nullable List<Contact> updateContacts) {
                //update the contacts list when the database changes
                adapter.clear();
                adapter.addAll(updateContacts);

            }
        });

        // get saved contacts in portrait mode
        if (savedInstanceState != null) {
            for (Parcelable contact : savedInstanceState.getParcelableArrayList(
                    "contacts")) {
                contacts.add((Contact) contact);
            }
        } else {
//            contacts.add(new Contact("Joe Bloggs", "joe@bloggs.co.nz", "021123456"));
//            contacts.add(new Contact("Jane Doe", "jane@doe.co.nz", "022123456"));
        }
    }

    public void saveContact(View view) {

        // get input values
        EditText nameField = findViewById(R.id.name);
        String name = nameField.getText().toString();

        EditText emailField = findViewById(R.id.email);
        String email = emailField.getText().toString();

        EditText phoneField = findViewById(R.id.mobile);
        String phone = phoneField.getText().toString();

        Contact newContact = new Contact(name, email, phone);

        // add new contact
        //contacts.add(newContact);
        //Toast.makeText(this, " save new contact successfully", Toast.LENGTH_SHORT).show();


        // update existed contact or add new contact

        allContacts = contactRepository.getAllContacts();
        if(allContacts.getValue().contains(newContact)){
            System.out.print("******** update contact");
            for(Contact c: allContacts.getValue()){
                if(c.name.equals(newContact.name)){
                    c.email = newContact.email;
                    c.mobile = newContact.mobile;
                    contactRepository.update(c);
                }
            }
        }else {
            System.out.print("******** insert new contact");
            contactRepository.insert(newContact);
        }

        // notify adapter change
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    //delete contact
    public void deleteContact(View view){
        allContacts = contactRepository.getAllContacts();

        // get the contact info in edit form
        EditText nameField = findViewById(R.id.name);
        String name = nameField.getText().toString();

        for(Contact c: allContacts.getValue()){
            if(c.name.equals(name)){
                contactRepository.delete(c);
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = (Contact) parent.getAdapter().getItem(position);
        Toast.makeText(parent.getContext(), "Clicked " + contact, Toast.LENGTH_SHORT).show();

        EditText nameField =  (EditText) findViewById(R.id.name);
        nameField.setText(contact.name);


        EditText mobileField = (EditText) findViewById(R.id.mobile);
        mobileField.setText(contact.mobile);

        EditText emailField = (EditText) findViewById(R.id.email);
        emailField.setText(contact.email);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putParcelableArrayList("contacts", contacts);
        super.onSaveInstanceState(savedState);
    }

}