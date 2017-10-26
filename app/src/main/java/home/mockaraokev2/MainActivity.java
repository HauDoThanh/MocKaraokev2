package home.mockaraokev2;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import home.mockaraokev2.Actitivy.Act_Favorite;
import home.mockaraokev2.Actitivy.Act_InfoApp;
import home.mockaraokev2.Actitivy.Act_Login;
import home.mockaraokev2.Actitivy.Act_MyRecord;
import home.mockaraokev2.Actitivy.Act_MyVideo;
import home.mockaraokev2.Actitivy.Act_PlayVideo;
import home.mockaraokev2.Actitivy.Act_Playlist_Show;
import home.mockaraokev2.Actitivy.Act_Search;
import home.mockaraokev2.Actitivy.Act_Singer;
import home.mockaraokev2.Actitivy.Act_Support;
import home.mockaraokev2.Adapter.Adap_Navigation;
import home.mockaraokev2.Adapter.Adap_ViewPager;
import home.mockaraokev2.Class.ModelDanhSachPhat;
import home.mockaraokev2.Fragment.Fragment_Hot;
import home.mockaraokev2.Fragment.Fragment_New;
import home.mockaraokev2.Fragment.Fragment_Playlist;
import home.mockaraokev2.Object.NaviObject;
import home.mockaraokev2.Object.VideoObject;


public class MainActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener, GoogleApiClient.OnConnectionFailedListener {

    private final int PERMISSION_ALL = 1;
    private final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private GoogleApiClient mGoogleApiClient;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    //navigation
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private ExpandableListView explvNavigation;
    private List<String> listTypeNavigation;
    private List<NaviObject> listNavigation;
    private SearchView searchView;
    private Intent intent = null;
    private Dialog dialogSuggest;
    private SharedPreferences pre;
    private ModelDanhSachPhat modelDanhSachPhat;

    //Hàm xin quyền truy cập
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        Log.e("logxxx", "Main is create");

        init();

        setupViewPager();
        setupNavigation();
        tabLayout.setupWithViewPager(viewPager);

        eventNavigation();

        onItemFragmentClick();

    }

    //khởi tạo biến
    private void init() {
        //Tablayout
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.main_viewPager);

        //toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nv_view);

        setSupportActionBar(toolbar);

        drawerToggle = setupDrawerToggle();
        drawerLayout.addDrawerListener(drawerToggle);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        pre = getSharedPreferences(Act_Login.KEY_SHARE, MODE_PRIVATE);


        modelDanhSachPhat = ModelDanhSachPhat.Instances(MainActivity.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
    //cài đặt 3 tab

    @Override
    protected void onResume() {
        super.onResume();
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    @Override
    protected void onDestroy() {
        modelDanhSachPhat.deleteAll();

        Runtime.getRuntime().gc();
        super.onDestroy();
    }

    private void setupViewPager() {
        Adap_ViewPager adapViewPager = new Adap_ViewPager(getSupportFragmentManager());
        adapViewPager.addFragment(Fragment_Hot.newInstance(), "Hát nhiều");
        adapViewPager.addFragment(Fragment_New.newInstance(), "Mới nhất");
        adapViewPager.addFragment(Fragment_Playlist.newInstance(), "Tuyển tập");
        viewPager.setAdapter(adapViewPager);
    }

    //cài đặt thanh navigation
    private void setupNavigation() {
        //header navigation:
        View header = navigationView.inflateHeaderView(R.layout.header_navigation);
        TextView navName = header.findViewById(R.id.nav_name);
        TextView navMail = header.findViewById(R.id.nav_mail);
        String name = pre.getString(Act_Login.NAME_ACCOUNT, "");
        String email = pre.getString(Act_Login.MAIL_ACCOUNT, "");
        //String photo=pre.getString(Act_Login.photoEmail,"");
        //imgAnh.setImageURI(Uri.parse(photo));
        navMail.setText(email);
        navName.setText(name);

        explvNavigation = (ExpandableListView) findViewById(R.id.explvNavigation);
        listNavigation = new ArrayList<>();
        listNavigation.add(new NaviObject(R.drawable.ic_music, "Thể loại", "5"));
        listNavigation.add(new NaviObject(R.drawable.ic_singer, "Ca sĩ", ""));
        listNavigation.add(new NaviObject(R.drawable.ic_heart, "Bài hát yêu thích", ""));
        listNavigation.add(new NaviObject(R.drawable.ic_list_video, "Đang phát", ""));
        listNavigation.add(new NaviObject(R.drawable.record, "Ghi âm", ""));
        listNavigation.add(new NaviObject(R.drawable.ic_video, "Video", ""));
        listNavigation.add(new NaviObject(R.drawable.ic_rating, "Đánh giá", ""));
        listNavigation.add(new NaviObject(R.drawable.ic_support, "Hỗ trợ", ""));
        listNavigation.add(new NaviObject(R.drawable.ic_info, "Thông tin ứng dụng", ""));
        listNavigation.add(new NaviObject(R.drawable.ic_logout, "Đăng xuất", ""));

        listTypeNavigation = new ArrayList<>();
        listTypeNavigation.add("Nhạc Trẻ");
        listTypeNavigation.add("Nhạc Trữ Tình");
        listTypeNavigation.add("Nhạc Remix");
        listTypeNavigation.add("Nhạc Rap");
        listTypeNavigation.add("Nhạc Nước Ngoài");

        HashMap<String, List<String>> hashMapNavigation = new HashMap<>();
        hashMapNavigation.put(listNavigation.get(0).getTitle(), listTypeNavigation);

        Adap_Navigation adapterNavigation = new Adap_Navigation(MainActivity.this, listNavigation, hashMapNavigation);
        explvNavigation.setAdapter(adapterNavigation);
    }

    //bắt sự kiện khi click vào navigation
    private void eventNavigation() {
        explvNavigation.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                intent = null;
                switch (listNavigation.get(groupPosition).getTitle()) {
                    case "Ca sĩ":
                        intent = new Intent(MainActivity.this, Act_Singer.class);
                        break;
                    case "Bài hát yêu thích":
                        intent = new Intent(MainActivity.this, Act_Favorite.class);
                        break;
                    case "Đang phát":
                        ArrayList<VideoObject> videos = modelDanhSachPhat.getAllVideo();
                        ArrayList<String> listId = new ArrayList<>();

                        if (videos.size() != 0) {
                            for (int i = 0; i < videos.size(); i++) {
                                listId.add(videos.get(i).getId());
                            }
                            Intent intent2 = new Intent(MainActivity.this, Act_PlayVideo.class);
                            intent2.putParcelableArrayListExtra("listPhat", videos);
                            intent2.putStringArrayListExtra("listIDVideo", listId);
                            intent2.putExtra("stopProgress", "xx");

                            startActivity(intent2);

                        } else
                            Toast.makeText(MainActivity.this, "Danh sách phát rỗng!", Toast.LENGTH_SHORT).show();

                        break;
                    case "Ghi âm":
                        intent = new Intent(MainActivity.this, Act_MyRecord.class);
                        break;
                    case "Video":
                        intent = new Intent(MainActivity.this, Act_MyVideo.class);
                        break;
                    case "Đánh giá":
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        break;
                    case "Hỗ trợ":
                        intent = new Intent(MainActivity.this, Act_Support.class);
                        break;
                    case "Thông tin ứng dụng":
                        intent = new Intent(MainActivity.this, Act_InfoApp.class);

                        break;
                    case "Đăng xuất":
                        AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                        build.setTitle("Đăng xuất");
                        build.setMessage("Bạn có muốn đăng xuất?");

                        build.setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                signOut();
                            }
                        });
                        build.setNegativeButton("Không", null);

                        Dialog d = build.create();
                        //noinspection ConstantConditions
                        d.getWindow().setBackgroundDrawableResource(R.color.background_xam);
                        d.show();

                        break;
                }

                if (intent != null)
                    startActivity(intent);
                return false;
            }
        });

        explvNavigation.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int parentPosition, int childPosition, long l) {
                String child = listTypeNavigation.get(childPosition);
                switch (child) {
                    case "Nhạc Trẻ":
                        startActivity(new Intent(MainActivity.this, Act_Playlist_Show.class)
                                .putExtra("playlistId", "PLRJBX0Ji7rJ-B9x7N79O1s1XxdCAAZSK-")
                                .putExtra("title", "Nhạc Trẻ"));
                        break;
                    case "Nhạc Trữ Tình":
                        startActivity(new Intent(MainActivity.this, Act_Playlist_Show.class)
                                .putExtra("playlistId", "PLRJBX0Ji7rJ-FBPdxJAvf4AodEGz338Xo")
                                .putExtra("title", "Nhạc Trữ Tình"));
                        break;
                    case "Nhạc Remix":
                        startActivity(new Intent(MainActivity.this, Act_Playlist_Show.class)
                                .putExtra("playlistId", "PLRJBX0Ji7rJ8g0CDbMUn9QAYYHRas1iLp")
                                .putExtra("title", "Nhạc Remix"));
                        break;
                    case "Nhạc Nước Ngoài":
                        startActivity(new Intent(MainActivity.this, Act_Playlist_Show.class)
                                .putExtra("playlistId", "PLRJBX0Ji7rJ9mm6laMX1QLg_sqQPS3v2W")
                                .putExtra("title", "Nhạc Nước Ngoài"));
                        break;
                    case "Nhạc Rap":
                        startActivity(new Intent(MainActivity.this, Act_Playlist_Show.class)
                                .putExtra("playlistId", "PLRJBX0Ji7rJ9nlKVJyOCTK4c6h-vu-7Rs")
                                .putExtra("title", "Nhạc Rap"));
                        break;
                }
                return false;
            }
        });
    }

    //cài đặt Drawable toggle
    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    //khởi tạo meu search
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchViewMenuItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) searchViewMenuItem.getActionView();
        //thay đổi icon mặc định của search view
        //     ImageView imgIconSearch = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        //     ImageView imgIconClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        //   imgIconSearch.setImageResource(R.drawable.icon_search);
        //   imgIconClose.setImageResource(R.drawable.ic_x);
        EditText edtInput = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        edtInput.setTextColor(Color.WHITE);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerToggle.syncState();
                searchView.onActionViewCollapsed();
                Intent intent = new Intent(MainActivity.this, Act_Search.class);
                startActivity(intent);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.onActionViewCollapsed();
                drawerToggle.syncState();
                return true;
            }
        });

        searchView.setOnQueryTextListener(this);
        return super.onPrepareOptionsMenu(menu);
    }

    //bắt sự kiện khi search
    @Override
    public boolean onQueryTextSubmit(String s) {
        Intent intent = new Intent(MainActivity.this, Act_Search.class);
        intent.putExtra("keySearch", s.toLowerCase());
        startActivity(intent);
        return true;
    }

    //bắt sự kiện phát video bên fragment
    private void onItemFragmentClick() {
        Fragment_Hot fraHot = new Fragment_Hot();
        fraHot.setOnItemClickListener(new Fragment_Hot.ClickListenerHot() {
            @Override
            public void onItemClick(VideoObject videoObject) {
                dialogSuggest(videoObject);
            }
        });

        Fragment_New fraNew = new Fragment_New();
        fraNew.setOnItemClickListener(new Fragment_New.ClickListenerNew() {
            @Override
            public void onItemClick(VideoObject videoObject) {
                dialogSuggest(videoObject);
            }
        });
    }

    private void dialogSuggest(final VideoObject object) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông báo")
                .setMessage("Bạn nên sử dụng tai nghe có mic để chất lượng âm thanh được tốt nhất")
                .setPositiveButton("Đã hiểu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(object);
                    }
                });

        dialogSuggest = builder.create();
        dialogSuggest.getWindow().setBackgroundDrawableResource(R.color.background_xam);
        dialogSuggest.show();
    }

    private void requestPermissions(final VideoObject object) {
        if (hasPermissions(this, PERMISSIONS)) {
            sentDataToPlayVideo(object);
            dialogSuggest.dismiss();
        } else {
            Toast.makeText(getApplicationContext(), "Để sử dụng tính năng này bạn phải cấp đủ quyền cho ứng dụng!", Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                }
            }, 1000);
        }
    }

    private void sentDataToPlayVideo(VideoObject object) {
        Intent intent = new Intent(MainActivity.this, Act_PlayVideo.class);
        intent.putExtra("videoAddFa", object);
        startActivity(intent);
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return true;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);

    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            startActivity(new Intent(MainActivity.this, Act_Login.class));
                        }
                    }
                });
        SharedPreferences.Editor editor = pre.edit();
        editor.clear();
        editor.apply();
        if (modelDanhSachPhat.deleteAll()) {
            Log.e("destroy", "delete all");
        }
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
