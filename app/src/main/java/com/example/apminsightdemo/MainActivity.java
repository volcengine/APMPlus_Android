package com.example.apminsightdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.bytedance.memory.test.OOMMaker;
import com.example.apminsightdemo.fragment.ListFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * MainActivity
 *
 * @author steven
 * @date 2011/11/11
 */
public class MainActivity extends FragmentActivity {
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
        lvItemList.add(new ListFragment.LvItem("崩溃模拟", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                throw new RuntimeException("Monitor Exception"); // 执行就崩, 如果应用启动后8秒内崩溃, 则判定为启动崩溃进行上报
            }
        }));
        lvItemList.add(new ListFragment.LvItem("ANR模拟", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                SystemClock.sleep(20000); // 任意处主线程执行, 执行后手动在屏幕上频繁滑动数下, 等几秒就会有ANR弹窗、数据上报
            }
        }));
        lvItemList.add(new ListFragment.LvItem("自定义错误模拟", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                App.mMonitorCrash.reportCustomErr("test err", "type1", new RuntimeException());
            }
        }));
        lvItemList.add(new ListFragment.LvItem("卡顿模拟", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                try {
                    Thread.sleep(2600);
                    testSeriousBlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

        lvItemList.add(new ListFragment.LvItem("卡顿页面", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                startActivity(new Intent(MainActivity.this, BlockListActivity.class));
            }
        }));

        lvItemList.add(new ListFragment.LvItem("hybrid网页监控", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, HybridTestActivity.class));
            }
        }));
        //网络监控支持OkHttp
        lvItemList.add(new ListFragment.LvItem("网络监控模拟", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                try {
                    executeGetAsnWithLog("http://mock-api.com/Rz3yJMnM.mock/get");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        lvItemList.add(new ListFragment.LvItem("网络错误监控模拟", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                try {
                    executeGetAsnWithLog("http://aa.bb.com/get");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        lvItemList.add(new ListFragment.LvItem("内存OOM模拟", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                OOMMaker.createOOM();
            }
        }));

        mFragment.addAllList(lvItemList);
    }

    private void testSeriousBlock() {
        try {
            Thread.sleep(2600);
        } catch (Exception e) {
        }
    }


    private String executeGetAsnWithLog(String url) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient thisClient = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = thisClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();
            }
        });
        return "";
    }

}
