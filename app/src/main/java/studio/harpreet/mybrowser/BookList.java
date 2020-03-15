package studio.harpreet.mybrowser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookList extends AppCompatActivity {

    DatabaseHelper mydb;
    ListView booklist;
    ListAdapter lviewadapter;
    ArrayList<HashMap<String,String>> userlist;

    LinearLayout empty;

    int multicount = 0;

    ArrayAdapter adapter;
    ArrayList<HashMap<String,String>> multilist = new ArrayList<>();

    String getmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        mydb = new DatabaseHelper(this);
        booklist = findViewById(R.id.booklistview);

        empty = findViewById(R.id.emptyview);
        empty.setVisibility(View.GONE);
        getdata();

        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,userlist);

        booklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object o = booklist.getAdapter().getItem(i);
                if(o instanceof Map)
                {
                    Map map = (Map)o;
                    Intent intent = new Intent(BookList.this,MainActivity.class);
                    intent.putExtra("urlkey",String.valueOf(map.get("Url")));
                    startActivity(intent);
                }
            }
        });

        booklist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        booklist.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                multicount = booklist.getCheckedItemCount();
                actionMode.setTitle(multicount + " Items Selected");
                if(booklist.isItemChecked(i))
                {
                    multilist.add(userlist.get(i));
                }
                else
                {
                    multilist.remove(userlist.get(i));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.book_context_menu,menu);

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
                    case R.id.select_all:

                        for(int i=0 ; i<booklist.getAdapter().getCount(); i++)
                        {
                            booklist.setItemChecked(i,true);
                        }
                        return true;

                    case R.id.Delete:

                        for(HashMap msg : multilist)
                        {
                            HashMap hashmap = (HashMap)msg;
                            getmap = (String)hashmap.get("Id");
                            Integer delete = mydb.delete(getmap);
                            if(delete > 0)
                            {
                                Toast.makeText(BookList.this, "Deleted", Toast.LENGTH_SHORT).show();
                                mydb.alter();
                                getdata();
                            }
                            else
                            {
                                Toast.makeText(BookList.this, "Error Deleting", Toast.LENGTH_SHORT).show();
                            }
                            adapter.remove(msg);

                        }
                        Toast.makeText(BookList.this, multicount+" items deleted", Toast.LENGTH_SHORT).show();
                        multicount = 0;
                        multilist.clear();

                        actionMode.finish();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
    }





    public void getdata()
    {
        userlist = mydb.Showdata();
        if(userlist.isEmpty())
        {
            empty.setVisibility(View.VISIBLE);
            return;
        }

        lviewadapter = new SimpleAdapter(BookList.this,userlist,R.layout.book_custom_list,
                new String[]{"Id","Title","Url"},
                new int[]{R.id.custombookid,R.id.custombooktitle,R.id.custombookurl});
        booklist.setAdapter(lviewadapter);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
