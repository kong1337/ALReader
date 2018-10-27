package com.example.administrator.myapplication.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.administrator.myapplication.adapter.MyItemRecyclerViewAdapter;
import com.example.administrator.myapplication.adapter.SectionsPagerAdapter;
import com.example.administrator.myapplication.bean.Constant;
import com.example.administrator.myapplication.ui.fragment.BookDetailFragment;
import com.example.administrator.myapplication.ui.fragment.BookItemFragment;
import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.bean.Novel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "MainActivity";
    private static final int FILE_SELECT_CODE = 0;
    private static final int FILE_READ_CODE = 1;
    private Context context;

    @BindView(R.id.main_vp_container)
    ViewPager mViewPager;
    @BindView(R.id.main_drawer)
    DrawerLayout drawerLayout;
    @BindView(R.id.main_nav)
    NavigationView navigationView;
    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_tabLayout)
    TabLayout tabLayout;

    String currentType = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getBaseContext();

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        this.getExternalFilesDir(null);

        initView();
        initData();

    }

    public void initView(){
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

    }

    public void initData(){

        List<Fragment> fragments = new ArrayList<>();
        for (int i = 0; i < Constant.BOOKURLS.size(); i ++){
            fragments.add(BookItemFragment.newInstance(Constant.BOOKURLS.get(i)));
        }

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.setFragments(fragments);

        mViewPager.setAdapter(mSectionsPagerAdapter);
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){
            case R.id.menu_information:
                Toast.makeText(this, "个人资料", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(Gravity.START);
                break;
            case R.id.menu_localBook:
                currentType = "0";
                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},FILE_READ_CODE);
                } else {
                    getLocalBook();
                }
                drawerLayout.closeDrawer(Gravity.START);
                break;
            case R.id.menu_localBook_else:
                currentType = "1";
                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},FILE_READ_CODE);
                } else {
                    getLocalBook();
                }
                drawerLayout.closeDrawer(Gravity.START);
                break;
                default:
        }
        return true;
    }

    public void getLocalBook(){
        //type 0： 汉语 type 1: 其他语言
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "亲，木有文件管理器啊-_-!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case FILE_READ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLocalBook();
                } else {
                    Toast.makeText(context, "该操作需要读取权限！", Toast.LENGTH_SHORT).show();
                }
                break;
                default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == FILE_SELECT_CODE){
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String string = uri.toString();
            File file;
            String a[] = new String[2];
            //判断文件是否在sd卡中
            String path = new String ();
            if (string.indexOf(String.valueOf(Environment.getExternalStorageDirectory()))!=-1){
                //对Uri进行切割
                a = string.split(String.valueOf(Environment.getExternalStorageDirectory()));
                //获取到file
                file = new File(Environment.getExternalStorageDirectory(),a[1]);
                path = file.getAbsolutePath();
            }else if(string.indexOf(String.valueOf(Environment.getDataDirectory()))!=-1){ //判断文件是否在手机内存中
                //对Uri进行切割
                a =string.split(String.valueOf(Environment.getDataDirectory()));
                //获取到file
                file = new File(Environment.getDataDirectory(),a[1]);
                path = file.getAbsolutePath();
            }else{
                //出现其他没有考虑到的情况
                Log.d(TAG, "onActivityResult: 绝对路径" + getRealPathFromURI(data.getData()));
                path = getRealPathFromURI(data.getData());
                Toast.makeText(context, "获取到绝对路径", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "onActivityResult: " + path);

            Intent intent = new Intent(context,BookReadActivity.class);
            intent.putExtra("bookType","1");
            intent.putExtra("localType",currentType);
            intent.putExtra("bookUrl",path);
            startActivity(intent);
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(null!=cursor&&cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
                Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
