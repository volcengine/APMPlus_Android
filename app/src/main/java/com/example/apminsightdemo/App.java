package com.example.apminsightdemo;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.apm.applog.AppLog;
import com.apm.insight.ExitType;
import com.apm.insight.MonitorCrash;
import com.apm.insight.log.VLog;
import com.apm.insight.log.VLogConfig;
import com.apmplus.sdk.cloudmessage.SDKCloudManager;
import com.apmplus.sdk.event.SDKEventIDynamicParams;
import com.apmplus.sdk.event.SDKEventInitConfig;
import com.apmplus.sdk.event.SDKEventManager;
import com.bytedance.apm.insight.ApmInsight;
import com.bytedance.apm.insight.ApmInsightInitConfig;
import com.bytedance.apm.insight.IActivityLeakListener;
import com.bytedance.apm.insight.IDynamicParams;
import com.monitor.cloudmessage.config.SDKCloudInitConfig;
import com.monitor.cloudmessage.config.SDKIDynamicParams;


/**
 * Application
 *
 * @author steven
 * @date 2020/11/11
 */
public class App extends Application {
    public static String sSDKAid = "187277";
    public static String sdkAid = "240734";
    public static String sdkToken = "aa77e9b33b8b45a3ab7c8efb94728a31";

    public static String sToken = "token";
    public static String did = "device_id";
    public static String uid = "user_id";

    public static final String TAG = App.class.getSimpleName();

    public static MonitorCrash mMonitorCrash;
    private static boolean hasStart;

    @Override
    public void onCreate() {
        super.onCreate();
        initCrash();
        initApmInsight();
        initSDKMonitor();
    }

    /**
     * ApmInsight崩溃监控初始化
     */
    private void initCrash() {
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
        mMonitorCrash = MonitorCrash.init(this, config);
    }

    /**
     * ApmInsight性能监控初始化
     */
    private void initApmInsight() {
        //必须放到Application的onCreate里面，会注册监听生命周期，不涉及数据采集和隐私合规问题
        ApmInsight.getInstance().init(this);
        //初始化自定日志，配置自定义日志最大占用磁盘，内部一般配置20,代表最大20M磁盘占用。1.4.1版本开始存在这个api
        VLog.init(this, 20);
    }

    /**
     * 在隐私合规后调用，开始采集数据启动监控
     */
    public static void startMonitor() {
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

    private void initSDKMonitor() {
        //初始化sdk崩溃监控
        MonitorCrash.Config config = MonitorCrash.Config.sdk(sdkAid)
//                .token({{AppToken}})// 数据鉴权token，可从平台应用信息处获取，token错误无法上报数据
                .versionCode(4) // 必须SDK版本号
                .versionName("1.4") // 必须SDK版本名称
                .keyWords("a.b.c", "d.e.b") //设置可能出现在崩溃堆栈内的特定字符串（比如包名）不设置不过滤，不支持正则表达式，对Java崩溃和ANR生效
                .soList("a.so", "b.so", "c.so") //Native崩溃监控，不设置不上报，不支持正则表达式
                //可选，可以设置自定义did，不设置会使用内部默认的
                //.dynamicParams(new MonitorCrash.Config.IDynamicParams() {
                //    @Override
                //    public String getDid() {//返回空会使用内部默认的did
                //        return null;
                //    }
                //
                //    @Override
                //    public String getUserId() {
                //        return null;
                //    }
                //})
                //可选，添加业务自定义数据，在崩溃详情页展示
                //.customData(crashType -> {
                //    HashMap<String, String> map = new HashMap<>();
                //    map.put("app_custom", "app_value");
                //    return map;
                //})
                //.pageViewTags(map) // 设置PV自定义维度
                .build();
        MonitorCrash monitorCrash = MonitorCrash.initSDK(this, config);

        //初始化事件打点
        SDKEventInitConfig.Builder builder = SDKEventInitConfig.builder()
                .aid(sdkAid)//平台的aid
                .token(sdkToken)//平台的token
                .context(getApplicationContext())//context
                .channel("channel")//SDK渠道
                .appVersion("1.0.0")//sdk 的版本号
                .updateVersionCode("10001")//sdk 的小版本号
                .hostAid("APM_DEMO")// 宿主App的唯一标识
                .debugMode(true)//debug模式，会打印测试日志。正式环境设置为release
                .setDynamicParams(new SDKEventIDynamicParams() {
                    @Override
                    public String getDid() {
                        //必填，返回设备标识 did，也可以使用业务自己的did
                        return AppLog.getInstance(sSDKAid).getDid();
                    }

                    @Override
                    public String getUserId() {
                        //返回用户标识 user id,没有可以为空
                        return "";
                    }
                });
        SDKEventManager.init(builder.build());


        initCloud();

    }

    private void initCloud(){

        //初始化Vlog日志实例。
        VLogConfig vConfig = new VLogConfig.Builder(getApplicationContext())
                .setLogDirPath(this.getFilesDir() + "/Vlog/" + sdkAid)//sSDKAid 为平台申请的aid
                .setMaxDirSize(5 * 1024 * 1024)  // max directory size
                .setSubProcessMaxDirSizeRatio(0.1f) // max directory size of each instance for sub process
                .setLogFileExpDays(14) // log file expired days
                .build();
        VLog.createInstance(vConfig, sdkAid);

        //初始化回捞组件
        SDKCloudInitConfig.Builder builder = SDKCloudInitConfig.builder();
        builder.aid(sdkAid);//平台的aid
        builder.token(sdkToken);//平台的token
        builder.context(getApplicationContext());//上下文context
        builder.debugMode(true);//测试阶段设置为debug有log排查日志。线上配置为false
        builder.channel("channel");// sdk渠道
        builder.updateVersionCode("1.1.4");// sdk的版本号
        builder.setDynamicParams(new SDKIDynamicParams() {
            @Override
            public String getDid() {
                //必填，设备唯一标识 did。当前写法为内部默认生成的did,也可以返回业务自己定义的did
                return did;
            }

            @Override
            public String getUserId() {
                //用户唯一标识userid,可以为空
                return "";
            }
        });
        //拉取平台配置的回捞命令时机：回捞组件默认初始化时候会拉取一次命令.
        SDKCloudManager.getInstance().init(builder.build());
    }

}
