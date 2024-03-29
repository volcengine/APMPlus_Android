package com.example.apminsightdemo;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.apm.insight.ExitType;
import com.apm.insight.MonitorCrash;
import com.apm.insight.log.VLog;
import com.bytedance.apm.insight.ApmInsight;
import com.bytedance.apm.insight.ApmInsightInitConfig;
import com.bytedance.apm.insight.IActivityLeakListener;
import com.bytedance.apm.insight.IDynamicParams;


/**
 * Application
 *
 * @author steven
 * @date 2020/11/11
 */
public class App extends Application {
    public static String sSDKAid = "187277";
    public static String sToken = "token";
    public static String did = "device_id";
    public static String uid = "user_id";

    public static MonitorCrash mMonitorCrash;
    private static boolean hasStart;
    private static boolean hasInit;

    @Override
    public void onCreate() {
        super.onCreate();
        initMonitor(this);
    }

    /**
     * 初始化APM监控，不会立即采集数据，需要放到Application的onCreate执行
     *
     * @param application
     */
    private void initMonitor(Application application) {
        if (hasInit) {
            return;
        }
        hasInit = true;
        initCrash(application);
        initApmInsight(application);
    }

    /**
     * 崩溃监控初始化
     */
    public static void initCrash(Application application) {
        MonitorCrash.Config config = MonitorCrash.Config.app(sSDKAid)
                .token(sToken)// 设置鉴权token，可从平台应用信息处获取，token错误无法上报数据
//              .versionCode(1)// 可选，默认取PackageInfo中的versionCode
//              .versionName("1.0")// 可选，默认取PackageInfo中的versionName
//              .channel("test")// 可选，设置App发布渠道，在平台可以筛选
//              .url("www.xxx.com")// 默认不需要，私有化部署才配置上报地址
                //可选，可以设置自定义did，不设置会使用内部默认的
                .dynamicParams(new MonitorCrash.Config.IDynamicParams() {
                    @Override
                    public String getDid() {//返回空会使用内部默认的did
                        return did;
                    }

                    @Override
                    public String getUserId() {
                        return uid;
                    }
                })
                //可选，添加业务自定义数据，在崩溃详情页展示
//              .customData(crashType -> {
//                  HashMap<String, String> map = new HashMap<>();
//                  map.put("app_custom", "app_value");
//                  return map;
//              })
                .exitType(ExitType.EXCEPTION) //上报应用退出原因，EXCEPTION会过滤USER_REQUEST类型
                .enableApmPlusLog(true) // 是否将崩溃信息等写入APMPlus日志，默认false
//            .crashProtect(true)  //是否开启崩溃防护，默认true
                .autoStart(false) // 是否在初始化时自动开启监控，默认为true
//            .debugMode(true) //线下使用的日志开关，线上不要调用或设置为false
                // 可选，添加pv事件的自定义tag，可以用来筛选崩溃率计算的分母数据
                //.pageViewTags(<<Map<String, String>>>)
                .build();
        mMonitorCrash = MonitorCrash.init(application, config);
    }

    /**
     * 性能监控初始化
     */
    public static void initApmInsight(Application application) {
        //必须放到Application的onCreate里面，会注册监听生命周期，不涉及数据采集和隐私合规问题
        ApmInsight.getInstance().init(application);
        //初始化自定日志，配置自定义日志最大占用磁盘，内部一般配置20,代表最大20M磁盘占用。1.4.1版本开始存在这个api
        VLog.init(application, 20);
    }

    /**
     * 启动APM监控，在隐私合规后调用，开始采集数据
     */
    public static void startMonitor(Application application) {
        if (hasStart) {
            return;
        }
        hasStart = true;

        // 如果初始化崩溃组件时未设置autoStart参数或者设置为true，将自动开启监控，不需要调用start方法。
        if (mMonitorCrash != null) {
            mMonitorCrash.start();
        }

        //在同意隐私合规后调用，启动性能组件监控
        ApmInsightInitConfig.Builder builder = ApmInsightInitConfig.builder();
        //必填：设置分配的appid
        builder.aid(sSDKAid);
        //必填：设置平台的app_token
        builder.token(sToken);
        //是否开启卡顿功能
        builder.blockDetect(true);
        //是否开启严重卡顿功能
        builder.seriousBlockDetect(true);
        //是否开启流畅性和丢帧
        builder.fpsMonitor(true);
        //控制是否打开WebVeiw监控
        builder.enableHybridMonitor(true);
        //控制是否打开内存监控
        builder.memoryMonitor(true);
        //控制是否打开电量监控
        builder.batteryMonitor(true);
        //是否打印日志，注：线上release版本要配置为false
        builder.debugMode(true);
        //支持用户自定义user_id把平台数据和自己用户关联起来，可以不配置。1.4.5版本后使用setDynamicParams()方法通过getUserId()回调设置
//        builder.userId("user_id");
        //私有化部署：配置数据上报的域名 （私有化部署才需要配置，内部有默认域名），测试支持设置http://www.xxx.com  默认是https协议
//        builder.defaultReportDomain("www.xxx.com");
        //设置渠道。1.3.16版本增加接口
        builder.channel("google play");
        //打开自定义日志回捞能力，1.4.1版本新增接口
        builder.enableLogRecovery(true);
        //控制是否打开cpu监控能力
        builder.cpuMonitor(true);
        //打开磁盘监控
        builder.diskMonitor(true);
        //打开流量监控
        builder.trafficMonitor(true);
        //控制是否打开用户使用时长的监控
        builder.operateMonitor(true);
        //控制是否打开启动监控，开启需要同时配置apm-plugin插件
        builder.startMonitor(true);
        //控制是否打开页面Activity耗时监控，开启需要同时配置apm-plugin插件
        builder.pageMonitor(true);
        //控制是否打开网络监控，开启需要同时配置apm-plugin插件
        builder.netMonitor(true);
        //打开APMPlus日志能力，可以通过回捞获取APMPlus日志
        builder.enableAPMPlusLocalLog(true);
        builder.detectActivityLeak(new IActivityLeakListener() {
            @Override
            public void onActivityLeaked(Activity activity) {
                Log.e("Activity Leak", activity.getLocalClassName());
            }
        });
        //设置数据和Rangers Applog数据打通，设备标识did必填。1.3.16版本增加接口
        builder.setDynamicParams(new IDynamicParams() {
            @Override
            public String getUserUniqueID() {
                //可选。依赖AppLog可以通过AppLog.getUserUniqueID()获取，否则可以返回null。
                return null;
            }

            @Override
            public String getAbSdkVersion() {
                //可选。如果依赖AppLog可以通过AppLog.getAbSdkVersion()获取，否则可以返回null。
                return null;
            }

            @Override
            public String getSsid() {
                //可选。依赖AppLog可以通过AppLog.getSsid()获取，否则可以返回null。
                return null;
            }

            @Override
            public String getDid() {
                //1.4.0版本及以上，可选，其他版本必填。设备的唯一标识，如果依赖AppLog可以通过 AppLog.getDid() 获取，也可以自己生成。
                return did;
            }

            @Override
            public String getUserId() {
                //1.4.5.cn版本增加的接口
                return uid;
            }
        });
        ApmInsight.getInstance().start(builder.build());
    }

}
