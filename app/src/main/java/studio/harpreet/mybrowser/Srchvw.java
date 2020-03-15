package studio.harpreet.mybrowser;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

public class Srchvw extends AppCompatActivity {

    SearchView srch;
    ListView searchlist;

int multicount = 0;

    ArrayList<String> list = new ArrayList<>();

    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_srchvw);

        srch = findViewById(R.id.searchView);
        searchlist = findViewById(R.id.SearchListView);

        list.add("Harpreet");
        list.add("Studio");
        list.add("hello");
        list.add("today");
        list.add("we create");
        list.add("Searchview");
        list.add("search");
        list.add("something");
        list.add("in your list");

        adapter = new ArrayAdapter<String>(this, R.layout.srchcustomlist,list);
        searchlist.setAdapter(adapter);

        searchlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = searchlist.getAdapter().getItem(i);
                String itemstr = String.valueOf(item);
                ValidateUrl(itemstr);
            }
        });

        searchlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = searchlist.getAdapter().getItem(i);
                final String itemstr = String.valueOf(item);
                final AlertDialog.Builder builder = new AlertDialog.Builder(Srchvw.this);
                builder.setTitle("Long Click")
                        .setMessage("Meassage")
                        .setPositiveButton("Show", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(Srchvw.this, itemstr, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                builder.setCancelable(true);
                            }
                        })
                        .setNeutralButton("delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                list.remove(itemstr);
                            }
                        });
                builder.show();
                return true;
            }
        });

        searchlist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        searchlist.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                multicount = searchlist.getCheckedItemCount();
                actionMode.setTitle(multicount + " items selected");
                if(searchlist.isItemChecked(i))
                {
                    multicount = multicount+1;
                }
                else
                {
                    multicount = multicount - 1;
                }

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.srch_context_menu,menu);


                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId())
                {

                    case R.id.select:

                        for ( int i=0; i < searchlist.getAdapter().getCount(); i++)
                        {
                            searchlist.setItemChecked(i, true);
                        }
                        return true;

                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });

        srch.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (list.contains(query)) {
                    adapter.getFilter().filter(query);
                    ValidateUrl(query);

                } else {
                    Toast.makeText(Srchvw.this, "no text found", Toast.LENGTH_SHORT).show();

                }
                ValidateUrl(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(list.isEmpty())
                {

                }
                else {
                    adapter.getFilter().filter(newText);

                }
                return false;
            }
        });
    }

    private void ValidateUrl(String url) {
        String prefix = "https://www.google.com/search?q=";

        if(!url.startsWith("http://") && !url.startsWith("https://")&&
                !url.endsWith(".com"))
        {
            url=prefix+url;
        }
        if(url.endsWith(".com") || url.endsWith(".as") || url.endsWith(".uk") || url.endsWith(".biz"))
        {
            if(!url.startsWith("http://") && !url.startsWith("https://"))
            {
                url = "https://"+url;
            }
        }
        Intent in = new Intent(Srchvw.this,MainActivity.class);
        in.putExtra("second",url);
        startActivity(in);

    }
}
