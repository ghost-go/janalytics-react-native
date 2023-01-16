package cn.jiguang.plugins.analytics;

import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import cn.jiguang.analytics.android.api.Account;
import cn.jiguang.analytics.android.api.AccountCallback;
import cn.jiguang.analytics.android.api.BrowseEvent;
import cn.jiguang.analytics.android.api.CalculateEvent;
import cn.jiguang.analytics.android.api.CountEvent;
import cn.jiguang.analytics.android.api.Currency;
import cn.jiguang.analytics.android.api.Event;
import cn.jiguang.analytics.android.api.JAnalyticsInterface;
import cn.jiguang.analytics.android.api.LoginEvent;
import cn.jiguang.analytics.android.api.PurchaseEvent;
import cn.jiguang.analytics.android.api.RegisterEvent;

public class JAnalyticsModule extends ReactContextBaseJavaModule {

    private static final String TAG = "JAnalyticsModule";

    ReactApplicationContext reactAppContext;

    public JAnalyticsModule(ReactApplicationContext reactAppContext) {
        super(reactAppContext);
        this.reactAppContext = reactAppContext;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @ReactMethod
    public void init() {
        JAnalyticsInterface.init(reactAppContext);
    }

    @ReactMethod
    public void setDebugMode(ReadableMap map) {
        if (map.hasKey("enable")) {
            JAnalyticsInterface.setDebugMode(map.getBoolean("enable"));
        }
    }

    @ReactMethod
    public void initCrashHandler() {
        JAnalyticsInterface.initCrashHandler(reactAppContext);
    }

    @ReactMethod
    public void stopCrashHandler() {
        JAnalyticsInterface.stopCrashHandler(reactAppContext);
    }

    @ReactMethod
    public void onEvent(ReadableMap map) {
        String type = map.getString("type");
        switch (type) {
            case "register":
                String registerMethod = map.getString("method");
                boolean registerSuccess = map.getBoolean("success");
                RegisterEvent registerEvent = new RegisterEvent(registerMethod, registerSuccess);
                sendEvent(registerEvent, map);
                break;
            case "login":
                String loginMethod = map.getString("method");
                boolean loginSuccess = map.getBoolean("success");
                LoginEvent loginEvent = new LoginEvent(loginMethod, loginSuccess);
                sendEvent(loginEvent, map);
                break;
            case "purchase":
                String goodsId = map.getString("goodsId");
                String goodsType = map.getString("goodsType");
                String goodsName = map.getString("goodsName");
                double price = map.getDouble("price");
                boolean purchaseSuccess = map.getBoolean("success");
                String currency = map.getString("currency");
                int count = map.getInt("count");
                PurchaseEvent purchaseEvent;
                if (currency.equals(Currency.CNY.name())) {
                    purchaseEvent = new PurchaseEvent(goodsId, goodsName, price, purchaseSuccess, Currency.CNY, goodsType, count);
                } else {
                    purchaseEvent = new PurchaseEvent(goodsId, goodsName, price, purchaseSuccess, Currency.USD, goodsType, count);
                }
                sendEvent(purchaseEvent, map);
                break;
            case "browse":
                String id = map.getString("id");
                String name = map.getString("name");
                String contentType = map.getString("contentType");
                float duration = (float) map.getDouble("duration");
                BrowseEvent browseEvent = new BrowseEvent(id, name, contentType, duration);
                sendEvent(browseEvent, map);
                break;
            case "count":
                id = map.getString("id");
                CountEvent countEvent = new CountEvent(id);
                sendEvent(countEvent, map);
                break;
            default:
                id = map.getString("id");
                double value = map.getDouble("value");
                CalculateEvent calculateEvent = new CalculateEvent(id, value);
                sendEvent(calculateEvent, map);
        }
    }

    private void sendEvent(Event event, ReadableMap map) {
        if (map.hasKey("extra")) {
            ReadableMap extra = map.getMap("extra");
            ReadableMapKeySetIterator iterator = extra.keySetIterator();
            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();
                event.addKeyValue(key, extra.getString(key));
            }
        }
        JAnalyticsInterface.onEvent(reactAppContext, event);
    }

    /**
     * 开始页面统计，和 stopLogPageView 成对调用，需要在页面的生命周期中调用，否则会对生命周期造成影响。
     *
     * @param map includes pageName
     */
    @ReactMethod
    public void onPageStart(ReadableMap map) {
        String name = map.getString("pageName");
        if (getCurrentActivity() != null)
            JAnalyticsInterface.onPageStart(getCurrentActivity(), name);
    }

    /**
     * 结束页面统计，和 startLogPageView 成对调用，需要在页面的生命周期中调用，否则会对生命周期造成影响。
     *
     * @param map includes pageName
     */
    @ReactMethod
    public void onPageEnd(ReadableMap map) {
        String name = map.getString("pageName");
        if (getCurrentActivity() != null)
            JAnalyticsInterface.onPageEnd(getCurrentActivity(), name);
    }

    @ReactMethod
    public void setChannel(ReadableMap map) {
        String channel = map.getString("channel");
        JAnalyticsInterface.setChannel(reactAppContext, channel);
    }

    @ReactMethod
    public void setAnalyticsReportPeriod(ReadableMap map) {
        int period = map.getInt("period");
        JAnalyticsInterface.setAnalyticsReportPeriod(reactAppContext, period);
    }

    /**
     * 设置账户维度模型
     */
    @ReactMethod
    public void identifyAccount(ReadableMap map, final Callback success, final Callback fail) {
        String accountID = map.getString("accountID");
        Account account = new Account(accountID);
        if (map.hasKey("creationTime")) {
            long creationTime = map.getInt("creationTime");
            account.setCreationTime(creationTime); // 账户创建的时间戳
        }
        if (map.hasKey("name")) {
            String name = map.getString("name");
            account.setName(name);
        }
        if (map.hasKey("sex")) {
            int sex = map.getInt("sex");
            account.setSex(sex);
        }
        if (map.hasKey("paid")) {
            int paid = map.getInt("paid");
            account.setPaid(paid);
        }
        if (map.hasKey("birthday")) {
            String birthday = map.getString("birthday");
            account.setBirthdate(birthday); // "19880920"是yyyyMMdd格式的字符串
        }
        if (map.hasKey("phone")) {
            String phone = map.getString("phone");
            account.setPhone(phone);
        }
        if (map.hasKey("email")) {
            String email = map.getString("email");
            account.setEmail(email);
        }
        if (map.hasKey("weiboID")) {
            String weiboID = map.getString("weiboID");
            account.setWeiboId(weiboID);
        }
        if (map.hasKey("wechatID")) {
            String wechatID = map.getString("wechatID");
            account.setWechatId(wechatID);
        }
        if (map.hasKey("qqID")) {
            String qqID = map.getString("qqID");
            account.setQqId(qqID);
        }
        if (map.hasKey("extras")) {
            ReadableMap extras = map.getMap("extras");
            if (extras != null) {
                ReadableMapKeySetIterator iterator = extras.keySetIterator();
                while (iterator.hasNextKey()) {
                    String key = iterator.nextKey();
                    if (TextUtils.isEmpty(key)) {
                        return;
                    }
                    String value = extras.getString(key);
                    account.setExtraAttr(key, value); // key如果为空，或者以极光内部namespace(符号$)开头，会设置失败并打印日志
                }
            }
        }

        JAnalyticsInterface.identifyAccount(reactAppContext, account, new AccountCallback() {
            @Override
            public void callback(int code, String msg) {
                if (code == 0) {
                    success.invoke();
                } else {
                    fail.invoke(msg);
                }
            }
        });
    }

    @ReactMethod
    public void detachAccount(final Callback success, final Callback fail) {
        JAnalyticsInterface.detachAccount(reactAppContext, new AccountCallback() {
            @Override
            public void callback(int code, String msg) {
                if (code == 0) {
                    success.invoke();
                } else {
                    fail.invoke(msg);
                }
            }
        });
    }

}
