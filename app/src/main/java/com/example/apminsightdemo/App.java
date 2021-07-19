package com.example.apminsightdemo;

import android.app.Application;
import android.util.Log;

import com.apm.insight.MonitorCrash;
import com.apm.insight.NpthInit;
import com.bytedance.apm.insight.ApmInsight;
import com.bytedance.apm.insight.ApmInsightInitConfig;
import com.bytedance.apm.insight.IDynamicParams;
import com.bytedance.applog.AppLog;
import com.bytedance.applog.ILogger;
import com.bytedance.applog.InitConfig;
import com.bytedance.applog.util.UriConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Application
 *
 * @author steven
 * @date 2011/11/11
 */
public class App extends Application {

    public static final String TAG = App.class.getSimpleName();

    public static MonitorCrash mMonitorCrash;
    private InitConfig config;

    @Override
    public void onCreate() {
        super.onCreate();
        initAppLog();
        initCrash();
        initApmInsight();
    }

    /**
     * ApmInsight崩溃监控初始化
     */
    private void initCrash() {
        mMonitorCrash = NpthInit.init(this, config);
    }

    /**
     * ApmInsight性能监控初始化
     */
    private void initApmInsight() {

        ApmInsightInitConfig.Builder builder = ApmInsightInitConfig.builder();
        //分配的aid，当前配置的为平台的测试aid,查看上报数据效果，可以联系客户经理添加平台权限
        builder.aid("194767");
        //卡顿功能
        builder.blockDetect(true);
        //严重卡顿功能
        builder.seriousBlockDetect(true);
        //fps功能
        builder.fpsMonitor(true);
        //测试时候才设置为true,可以看到测试日志，正式设置为false
        builder.debugMode(true);
        //hybrid 网页监控
        builder.enableWebViewMonitor(true);
        //内存监控
        builder.memoryMonitor(true);
        //控制是否打开电量监控
        builder.batteryMonitor(true);
        //支持用户自定义user_id把平台数据和自己用户关联起来，可以不配置
        builder.userId("user_id");
        //设置数据和AppLog数据打通，设备标识did必填。1.3.16版本增加接口
        builder.setDynamicParams(new IDynamicParams() {
            @Override
            public String getUserUniqueID() {
                //可选。依赖AppLog可以通过AppLog.getUserUniqueID()获取，否则可以返回null。
                return AppLog.getUserUniqueID();
            }

            @Override
            public String getAbSdkVersion() {
                //可选。如果依赖AppLog可以通过AppLog.getAbSdkVersion()获取，否则可以返回null。
                return AppLog.getAbSdkVersion();
            }

            @Override
            public String getSsid() {
                //可选。依赖AppLog可以通过AppLog.getSsid()获取，否则可以返回null。
                return AppLog.getSsid();
            }

            @Override
            public String getDid() {
                //必填。设备的唯一标识。如果依赖AppLog可以通过 AppLog.getDid() 获取。也可以自己生成。
                return AppLog.getDid();
            }
        });
        //配置自定义上报地址，私有化部署才需要配置
//        builder.defaultReportDomain("www.xxx.com");

        ApmInsight.getInstance().init(this, builder.build());

    }


    /**
     * Rangers 初始化
     */
    private void initAppLog() {
        /* 初始化开始 */
        config = new InitConfig("194767", "your_channel"); // appid和渠道，appid如不清楚请联系客户成功经理

        //上报域名只支持中国
        config.setUriConfig(UriConstants.DEFAULT);

        // 是否在控制台输出日志，可用于观察用户行为日志上报情况
        config.setLogger(new ILogger() {
            @Override
            public void log(String msg, Throwable t) {
                Log.d(TAG, msg, t);
            }
        });

        // 开启AB测试
        config.setAbEnable(true);

        config.setAutoStart(true);
        AppLog.init(this, config);
        AppLog.setUserUniqueID("uniqueId");
        /* 初始化结束 */

      	/* 自定义 “用户公共属性”（可选，初始化后调用, key相同会覆盖）
      	关于自定义 “用户公共属性” 请注意：1. 上报机制是随着每一次日志发送进行提交，默认的日志发送频率是1分钟，所以如果在一分钟内连续修改自定义用户公共属性，，按照日志发送前的最后一次修改为准， 2. 不推荐高频次修改，如每秒修改一次 */
        Map<String, Object> headerMap = new HashMap<String, Object>();
        headerMap.put("level", 8);
        headerMap.put("gender", "female");
        AppLog.setHeaderInfo((HashMap<String, Object>) headerMap);
    }

}
