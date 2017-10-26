package home.mockaraokev2.Actitivy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import home.mockaraokev2.R;

public class Act_Singer extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ListView lvSinger;
    private ArrayAdapter<String> adapterSinger;

    private String[] strSinger;
    private final ArrayList<String> stringsAlpha = new ArrayList<>();
    private final ArrayList<String> listSearch = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_singer);

        Toolbar toolbarSinger = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbarSinger);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        lvSinger = (ListView) findViewById(R.id.lvSinger);
        strSinger = getResources().getStringArray(R.array.singer);

        adapterSinger = new ArrayAdapter<>(Act_Singer.this, R.layout.item_singer, strSinger);
        lvSinger.setAdapter(adapterSinger);

        lvSinger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(Act_Singer.this, Act_Playlist_Show.class);
                intent.putExtra("name", strSinger[i]);
                startActivity(intent);
            }
        });


        //Tạo chuổi Alpha
        Collections.addAll(stringsAlpha, strSinger);
        Collections.sort(stringsAlpha, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        listSearch.clear();
        for (String aStrSinger : strSinger) {
            if (aStrSinger.toUpperCase().contains(newText.toUpperCase())) {
                listSearch.add(aStrSinger);
            }
        }
        adapterSinger = new ArrayAdapter<>(Act_Singer.this, R.layout.item_singer, listSearch);
        lvSinger.setAdapter(adapterSinger);
        adapterSinger.notifyDataSetChanged();
        lvSinger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(Act_Singer.this, Act_Playlist_Show.class);
                intent.putExtra("name", listSearch.get(i));
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_singer, menu);

        MenuItem item = menu.findItem(R.id.searchView);

        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(this);

        EditText edtInput = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        edtInput.setTextColor(Color.WHITE);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.sortHot){

            adapterSinger = new ArrayAdapter<>(Act_Singer.this, R.layout.item_singer, strSinger);
            lvSinger.setAdapter(adapterSinger);
            lvSinger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(Act_Singer.this, Act_Playlist_Show.class);
                    intent.putExtra("name", strSinger[i]);
                    startActivity(intent);
                }
            });

        }
        else if (item.getItemId() == R.id.sortAlpha){
            //Fix new Signer
            adapterSinger = new ArrayAdapter<>(Act_Singer.this, R.layout.item_singer, stringsAlpha);
            lvSinger.setAdapter(adapterSinger);
            lvSinger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(Act_Singer.this, Act_Playlist_Show.class);
                    intent.putExtra("name", stringsAlpha.get(i));
                    startActivity(intent);
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}
