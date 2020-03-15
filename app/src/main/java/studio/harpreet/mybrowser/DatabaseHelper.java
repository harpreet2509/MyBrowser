package studio.harpreet.mybrowser;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.strictmode.SqliteObjectLeakedViolation;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String Database_name = "Bookmarks.db";
    public static final String Table_name = "BookMarks";
    public static final String col_id = "Id";
    public static final String col_title = "Title";
    public static final String col_Url = "Url";


    public DatabaseHelper(@Nullable Context context) {

        super(context, Database_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + Table_name +" (Id INTEGER PRIMARY KEY AUTOINCREMENT, Title TEXT , Url TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+Table_name);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(String Title,String Url)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(col_title,Title);
        cv.put(col_Url,Url);
        Long result = db.insert(Table_name,null,cv);
        if(result == -1 )
        {
            return false;
        }
        else
        {
            return true;
        }

    }
    public ArrayList<HashMap<String,String>> Showdata()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String,String>> userlist = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from "+Table_name,null);
        while(cursor.moveToNext())
        {
            HashMap<String,String> user = new HashMap<>();
            user.put("Id",cursor.getString(cursor.getColumnIndex(col_id)));
            user.put("Title",cursor.getString(cursor.getColumnIndex(col_title)));
            user.put("Url",cursor.getString(cursor.getColumnIndex(col_Url)));
            userlist.add(user);
        }
        return userlist;
    }
    public boolean update(String id,String Title,String Url)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(col_id,id);
        cv.put(col_title,Title);
        cv.put(col_Url,Url);
        db.update(Table_name,cv,"Id = ?",new String[] { id });
        return true;
    }
    public Integer delete(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(Table_name,"Id = ?",new String[] {id});

    }
    public void alter()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(("UPDATE SQLITE_SEQUENCE SET seq = 0 WHERE NAME = ' "+Table_name+" ' "));
    }
}

