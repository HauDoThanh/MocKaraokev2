package home.mockaraokev2.Actitivy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import home.mockaraokev2.Adapter.Adap_ItemVideo;
import home.mockaraokev2.Class.ModelHistory;
import home.mockaraokev2.Object.VideoObject;
import home.mockaraokev2.R;
import home.mockaraokev2.network.models.VideoItem;
import home.mockaraokev2.network.models.VideoResult;
import home.mockaraokev2.network.retrofit.Command;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Act_Search extends AppCompatActivity implements SearchView.OnQueryTextListener {

    //SearchView
    private SearchView searchView;
    private String keysearch;

    private LinearLayoutManager linearLayoutManager;

    private RecyclerView listSearchResult;
    private final List<VideoObject> arrSearchResult = new ArrayList<>();
    private Adap_ItemVideo adapSearchResult;

    private boolean isLoading = false;
    private String nextPage;

    //searchNameHint
    private ListView listHint;
    private List<String> arrSearchHint;
    private ArrayAdapter<String> adapHint;

    //SearchHistory
    private LinearLayout linearLayout_History;
    private ModelHistory modelHistory;

    //load data
    private Command command;
    private VideoResult result;
    private ProgressBar progressBar;
    private TextView txtProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_search);

        init();
        event();

        //HintSearch
        arrSearchHint = new ArrayList<>();
        listHint = (ListView) findViewById(R.id.listHint_search);

        adapHint = new ArrayAdapter<String>(Act_Search.this, android.R.layout.simple_list_item_1, arrSearchHint) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        };


        listHint.setAdapter(adapHint);

        listHint.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchView.setQuery(arrSearchHint.get(position), true);
            }
        });

        linearLayout_History = (LinearLayout) findViewById(R.id.lineHis_Search);
        modelHistory = ModelHistory.Instances(this);

        modelHistory.setContext(this);
        modelHistory.setLineHistory(linearLayout_History);
        // Load dữ liệu và tạo button
        modelHistory.loadDataIntoButton();
    }

    private void init() {
        command = Command.getInstance();

        progressBar = (ProgressBar) findViewById(R.id.progress);
        txtProgress = (TextView) findViewById(R.id.txtProgress);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listSearchResult = (RecyclerView) findViewById(R.id.listAct_Search);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        listSearchResult.setLayoutManager(linearLayoutManager);

        adapSearchResult = new Adap_ItemVideo(arrSearchResult, this, true, 5);
        listSearchResult.setAdapter(adapSearchResult);

    }

    private void event() {
        adapSearchResult.setOnItemClickListenerSearch(new Adap_ItemVideo.ClickListenerSearch() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(Act_Search.this, Act_PlayVideo.class);
                intent.putExtra("videoAddFa", arrSearchResult.get(position));
                startActivity(intent);

            }
        });

        listSearchResult.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                totalItemCount = totalItemCount - 1;

                if (totalItemCount == (lastVisibleItem + 25)) {
                    if (isLoading && nextPage.length() > 0) {
                        requestYoutubeItem(nextPage, keysearch);
                        isLoading = false;
                    }
                }
            }
        });
    }

    private void requestYoutubeItem(String pageToken,
                                    String q) {
        command.execute("relevance", pageToken, "video", q, "50", new Callback<VideoResult>() {
            @Override
            public void onResponse(Call<VideoResult> call, Response<VideoResult> response) {
                if (response.isSuccessful()) {
                    result = response.body();
                    getData();
                }
            }

            @Override
            public void onFailure(Call<VideoResult> call, Throwable t) {

            }
        });

    }

    ///getData from retrofit:
    private void getData() {
        for (int i = 0; i < result.getItems().size(); i++) {
            VideoItem.Snippet spi = result.getItems().get(i).getSnippet();
            String id = result.getItems().get(i).getId().getVideoId();
            if (result.getNextPageToken() != null) {
                nextPage = result.getNextPageToken();
                isLoading = true;
            } else {
                nextPage = "";
                isLoading = false;
            }
            arrSearchResult.add(new VideoObject(id, spi.getTitle(),
                    spi.getThumbnails().getMedium().getUrl()));
            adapSearchResult.notifyDataSetChanged();
        }
        progressBar.setVisibility(View.GONE);
        txtProgress.setVisibility(View.GONE);
    }

    private void requestHint(String q) {
        command.execute("relevance", "", "video", q, "5", new Callback<VideoResult>() {
            @Override
            public void onResponse(Call<VideoResult> call, Response<VideoResult> response) {
                VideoResult result = response.body();
                for (int i = 0; i < result.getItems().size(); i++) {
                    VideoItem.Snippet spi = result.getItems().get(i).getSnippet();

                    arrSearchHint.add(spi.getTitle());
                    adapHint.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<VideoResult> call, Throwable t) {

            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchViewMenuItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) searchViewMenuItem.getActionView();

        //Set searchView cho sự kiện Button
        modelHistory.setSearchView(searchView);

        searchView.setOnQueryTextListener(this);
        searchView.onActionViewExpanded();
        EditText editText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        editText.setTextColor(Color.WHITE);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        arrSearchResult.clear();
        progressBar.setVisibility(View.VISIBLE);
        txtProgress.setVisibility(View.VISIBLE);
        keysearch = s;

        requestYoutubeItem("", s);

        //Thêm vào lịch sử
        modelHistory.addHistoryVideo(s);

        //Gợi ý và lịch sử ẩn đi
        linearLayout_History.setVisibility(View.GONE);
        listHint.setVisibility(View.GONE);

        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        arrSearchHint.clear();

        if (s.length() > 0) {
            if (listHint.getVisibility() == View.GONE)
                listHint.setVisibility(View.VISIBLE);
            linearLayout_History.setVisibility(View.GONE);

            requestHint(s);

        } else if (s.length() == 0) {
            linearLayout_History.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
