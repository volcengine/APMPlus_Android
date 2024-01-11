package com.example.apminsightdemo;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.example.apminsightdemo.fragment.ListFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * MainActivity
 *
 * @author steven
 * @date 2011/11/11
 */
public class FirstActivity extends FragmentActivity {
    private ListFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFragment();
        initData();
    }


    private void initFragment() {
        mFragment = new ListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.text_home, mFragment).commit();
    }


    private void initData() {

        List<ListFragment.LvItem> lvItemList = new ArrayList<>();
        lvItemList.add(new ListFragment.LvItem("初始化", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                App.init(getApplication());
                Toast.makeText(FirstActivity.this, "初始化完成", Toast.LENGTH_SHORT).show();
            }
        }));
        lvItemList.add(new ListFragment.LvItem("启动监控", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                showDialog();
            }
        }));

        mFragment.addAllList(lvItemList);
    }



    /**
     * 模拟用户协议和隐私协议弹窗，用户点击同一后开启监控
     */
    private void showDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(FirstActivity.this)
                //标题
                .setTitle("个人信息保护")
                //内容
                .setMessage("同意《用户协议》和《隐私政策》后开启监控")
                //图标
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App.startMonitor(getApplication());
                        startActivity(new Intent(FirstActivity.this,MainActivity.class));
                    }
                })
                .setNegativeButton("不同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        alertDialog.show();

    }

}
