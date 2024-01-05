package com.example.apminsightdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.apm.insight.log.ILog;
import com.apm.insight.log.VLog;
import com.apmplus.sdk.cloudmessage.SDKCloudManager;
import com.apmplus.sdk.event.SDKEventManager;
import com.bytedance.apm.insight.ApmInsightAgent;
import com.bytedance.memory.test.OOMMaker;
import com.example.apminsightdemo.fragment.ListFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
        showDialog();
    }


    private void initFragment() {
        mFragment = new ListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.text_home, mFragment).commit();
    }


    private void initData() {
        for (int i = 0; i < 10000; i++) {
            VLog.d("vlog", "vlog:" + i);
        }

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
        lvItemList.add(new ListFragment.LvItem("事件上报模拟", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                HashMap<String, String> dimension = new HashMap<>();
                //维度值
                dimension.put("key1", "value1");
                dimension.put("key2", "value2");
                HashMap<String, Double> metric = new HashMap<>();
                //指标值
                metric.put("metric1", (double) 10);
                metric.put("metric2", 8.8);
                ApmInsightAgent.monitorEvent("event1", dimension, metric);
            }
        }));
        lvItemList.add(new ListFragment.LvItem("SDK事件上报模拟", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                HashMap<String, String> dimension = new HashMap<>();
                //维度值
                dimension.put("key1", "value1");
                dimension.put("key2", "value2");
                HashMap<String, Double> metric = new HashMap<>();
                //指标值 指标值必须为数字
                metric.put("metric1", (double) 10);
                metric.put("metric2", 8.8);
                SDKEventManager.getSDKMonitor(App.sdkAid).monitorEvent("event_test", dimension, metric);
            }
        }));

        lvItemList.add(new ListFragment.LvItem("SDK日志打印主动上报", new ListFragment.OnClick() {
            @Override
            public void click(View view) {
                //日志打印
                ILog iLog = VLog.getInstance(App.sdkAid);
                iLog.i("sdk_cloud", "test i");
                iLog.e("sdk_cloud", "test e");

                //可选：除了支持日志回捞，在用户反馈情况也支持主动上报Vlog日志。主动上报的日志在平台的 单点追查->自定义文件 输入did可以查询到日志
                //上报前一小时的Vlog日志。上报日志会消耗平台事件量，注意合理使用
                SDKCloudManager.getSDKCloud(App.sdkAid).uploadVlog(System.currentTimeMillis() - 60 * 60 * 1000, System.currentTimeMillis());

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

    /**
     * 模拟用户协议和隐私协议弹窗，用户点击同一后开启监控
     */
    private void showDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                //标题
                .setTitle("个人信息保护")
                //内容
                .setMessage("同意《用户协议》和《隐私政策》后开启监控")
                //图标
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App.startMonitor();
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
