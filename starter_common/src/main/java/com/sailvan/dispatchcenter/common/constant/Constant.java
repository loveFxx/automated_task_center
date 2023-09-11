package com.sailvan.dispatchcenter.common.constant;

import com.alibaba.fastjson.JSONArray;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.sailvan.dispatchcenter.common.domain.TaskBufferMeta;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @date 21-04
 */
public class Constant {
    public static final String SYSTEM_USER_SESSION = "system_user_session";
    public final static String FILTER_REINDEX = "";

    public final static String VERSION_FILE = "version";


    /**
     * 定义JWT的有效时长 有效时间(分钟) token失效时间 分钟
     */
    public static final int TOKEN_VALIDITY_TIME = 60*24*7;

    /**
     * 定义允许刷新JWT的有效时长(在这个时间范围内，用户的JWT过期了，不需要重新登录，后台会给一个新的JWT
     * 这个是Token的刷新机制
     * 允许过期时间(分钟)
     */
    public static final int ALLOW_EXPIRES_TIME = 60*24*10;

    public static final Long EFFECTIVE = 10*365*3600*24L;

    /**
     *  业务系统缓存前缀
     */
    public final static String BUSINESS_REGISTER_PREFIX           = "dispatch_center:business:register:";
    public final static String BUSINESS_REGISTER_VALID_PREFIX     = "dispatch_center:business:register_valid:";
    public final static String BUSINESS_LIMIT_PREFIX              = "dispatch_center:business:limit:";
    public final static String BUSINESS_LIMIT_PREFIX_MONTH        = "dispatch_center:business:limit_month:";
    public final static String BUSINESS_LIMIT_PREFIX_TYPENAME     = "dispatch_center:business:limit_typeName:";
    public final static String CLIENT_CAPTCHA_CODE_LIMIT_PREFIX   = "dispatch_center:client:captcha_code:";
    public final static String PROXY_IP_ACCOUNT_CONTINENTS_PREFIX = "dispatch_center:proxyIp_account_continents:";

    /**
     * 密码(C_KEY)、偏移量(IV_KEY)、算法/模式/补码方式(CIPHER_VALUE)
     * 解密(AUTHORIZATION_DECRYPT)、加密(AUTHORIZATION_ENCRYPT)
     * 请求系统名(AUTHORIZATION_BUSINESS_NAME)
     */
    public final static String C_KEY                       = "7l2f6t1ea0o8f930";
    public final static String B_KEY                       = "U4oR&fP0S^ZHS$U7KGlo";
    public final static String IV_KEY                      = "2023751404128718";
    public final static String CIPHER_VALUE                = "AES/CBC/PKCS5Padding";
    public final static String AUTHORIZATION_DECRYPT       = "Authorization_decrypt_token";
    public final static String AUTHORIZATION_ENCRYPT       = "Authorization_encrypt_token";
    public final static String AUTHORIZATION_BUSINESS_NAME = "Authorization_request_system_name";
    public final static String AUTHORIZATION_REQUEST_URL_NAME = "Authorization_request_request_url";
    public final static String AUTHORIZATION_CLIENT_URL_NAME = "Authorization_client_request_url";

    /**
     * 需要匿名访问的接口
     */
    public static String[] ANON_SHIRO_MAPPING = new String[]
            {"/testLogin","/business/**","/business/register","/client/**","/getProxyIP","/index","/upload"
                    ,"/token/callback", "/token/redirect","/test111","/addTaskSource"
                    ,"/refreshCrawlPlatformProxyIPMapCache","/refreshMachineCache","/refreshMachineCache"};

    /**
     * 登录界面拦截
     */
    public static String[] FILTERED_LOGIN_MAPPING = new String[]
            {"/task/**","/machine/**","/store/**","/permission/**","/home","/businessS/**","/proxyIP/**","/version/**"};

    /**
     * 登录界面不拦截
     */
    public static String[] NOT_FILTERED_LOGIN_MAPPING = new String[]
            {"/","/login","testLogin","/index","/refreshCrawlPlatformProxyIPMapCache","/refreshMachineCache"};

    /**
     * 拦截 携带token的 业务端和客户端
     */
    public final static String BUSINESS_PREFIX = "/business";
    public final static String CLIENT_PREFIX = "/client";
    public static String[] FILTERED_TOKEN_MAPPING = new String[]
            {BUSINESS_PREFIX+"/**",CLIENT_PREFIX+"/**"};

    /**
     * 公共控制器--渲染搜索框
     */
    public final static String COMMON_PREFIX = "/common";

    /**
     * 签名key
     */
    public static final String SIGNING_KEY = "auto-task-center-spring-security-@QWer!&Secret^#";

    public static final String PI_WORK_QUEUE = "piserver";
    public static final String PI_TO_TASK_CENTER_QUEUE = "piserver";
    public static final String AUTO_TASK_CENTER = "token-autotaskcenter-getToken-queue";


    /**
     * 任务类型（1：周期性；2：单次性；3：周期单次性）
     */
    public static final int CIRCLE_TASK        = 1;
    public static final int SINGLE_TASK        = 2;
    public static final int CIRCLE_SINGLE_TASK = 3;

    /**
     *  任务大类型 1是可爬取平台 2按照账号站点的账号平台
     */
    public static final int LARGE_TASK_TYPE_CRAWL_PLATFORM   = 1;
    public static final int LARGE_TASK_TYPE_ACCOUNT_PLATFORM = 2;

    /**
     *  特殊任务的前缀，添加任务和获取任务不需要判断当前账号站点的状态
     *   account_status 用来更新账号的机器状态
     */
    public static final String[] TASK_NAME_SPECIAL_PREFIX                = {"account_status","bank_card"};
    public static final String[] TASK_NAME_ACCOUNT_STATUS_SPECIAL_PREFIX = {"account_status"};


    /**
     * 10.201开头 内网VPS
     * 10.30 或 10.202开头重庆账号机
     */
    public static final String INTRANET_VPS_PREFIX                 = "10.201";
    public static final String CHONGQING_ACCOUNT_MACHINE_0_PREFIX  = "10.30";
    public static final String CHONGQING_ACCOUNT_MACHINE_1_PREFIX  = "10.202";

    /**
     * 0账号机 1内网VPS 2外网VPS 3重庆VPS 4重庆账号机
     */
    public static final int MACHINE_TYPE_ACCOUNT_MACHINE           = 0;
    public static final int MACHINE_TYPE_INTRANET_VPS              = 1;
    public static final int MACHINE_TYPE_EXTRANET_VPS              = 2;
    public static final int MACHINE_TYPE_CHONGQING_VPS             = 3;
    public static final int MACHINE_TYPE_CHONGQING_ACCOUNT_MACHINE = 4;


    /**
     *  店铺状态: 0 正常（未运营） 未运营的店铺
     *           1 正常（运营中） 正常的店铺状态
     *           2 关店（不可登录） 一般情况下为已经关店的店铺
     *           3 关店（可登录） 一般情况下，店铺已关或无销售权限，但接口可能还正常
     *           4 暂停运营(假期模式)	暂停运营
     *           -10 无效店铺	废弃店铺，无效店铺等，可软删。需停止一起api或者数据处理行为
     */
    public static final int ACCOUNT_STATUS_NORMAL_NOT_OPERATION    = 0;
    public static final int ACCOUNT_STATUS_NORMAL_IN_OPERATION     = 1;
    public static final int ACCOUNT_STATUS_CLOSE_SHOP_CANNOT_LOGIN = 2;
    public static final int ACCOUNT_STATUS_CLOSE_SHOP_CAN_LOGIN    = 3;
    public static final int ACCOUNT_STATUS_SUSPENSION_OPERATIONS   = 4;
    public static final int ACCOUNT_STATUS_INVALID_SHOP            = -10;

    /**
     *  通用 状态值status，有效状态 1, 无效状态 0, -1禁用 -2,未验证
     */
    public static final int STATUS_VALID      = 1;
    public static final int STATUS_INVALID    = 0;

    public static final int STATUS_DISABLE    = -1;
    public static final int STATUS_UNVERIFIED = -2;
    public static final int STATUS_REMOVE     = -10;

    /**
     *  是否是手动或其他来源更新(非mini)、1是来源其他 0来源mini
     *   非mini的优先级较高
     */
    public static final int STATUS_IS_UPDATE      = 1;
    public static final int STATUS_IS_UPDATE_RESET= 0;

    /**
     * 心跳间隔 5分钟
     */
    public static final long HEAT_BEAT_INTERVAL = 5 * 60;
    public static final String PORT = "7070";

    /**
     * 爬取平台(1:亚马逊;2:沃尔玛;3:速卖通;4:facebook;5:boss)
     */
    public static final Map<String,String> EXECUTE_PLATFORMS= new HashMap<>();

    /**
     *  存放代理IP可爬取平台的select选择框 值是英文名字 废弃
     */
    public static JSONArray crawlPlatformSelectMap = new JSONArray();

    /**
     *  存放任务类型中的 可爬取平台的select选择框 值是id 废弃
     */
    public static JSONArray crawlPlatformSelect = new JSONArray();

    /**
     *  InitPlatformCache的init方法会重置
     *  @see com.common.init.InitPlatformCache#init()
     */
    public static String[] PLATFORMS = {"Amazon","Walmart","Aliexpress","Facebook","AmazonVC"};

    /**
     * 洲和洲的关系,用在刷新mini时,有类似的[美洲]需转换[American]
     * todo: token获取
     */
    public static final Map<String, String> MAP_CONTINENTS = new HashMap<>();
    public static final Map<String, String> MAP_AREA = new HashMap<>();

    /**
     * 站点(国家)与大洲对应关系
     */
    public static final Map<String,String> SITE_CONTINENT_MAP = new HashMap<>();

    public static final Map<String,String> VC_SITE_CONTINENT_MAP = new HashMap<>();

    static {

        MAP_CONTINENTS.put("美洲", "American");
        MAP_CONTINENTS.put("欧洲", "Europe");
        MAP_CONTINENTS.put("日本", "Japan");
        MAP_CONTINENTS.put("澳大利亚", "Australia");
        MAP_CONTINENTS.put("澳洲", "Australia");
        MAP_CONTINENTS.put("印度", "India");

        MAP_AREA.put("美洲", "NA");
        MAP_AREA.put("欧洲", "EU");
        MAP_AREA.put("日本", "FE");
        MAP_AREA.put("澳大利亚", "FE");
        MAP_AREA.put("澳洲", "FE");
        MAP_AREA.put("印度", "EU");


        //'NA' => ['MX', 'CA', 'US', 'BR'],
        SITE_CONTINENT_MAP.put("US","American");
        SITE_CONTINENT_MAP.put("美国","American");
        SITE_CONTINENT_MAP.put("CA","American");
        SITE_CONTINENT_MAP.put("加拿大","American");
        SITE_CONTINENT_MAP.put("MX","American");
        SITE_CONTINENT_MAP.put("墨西哥","American");
        SITE_CONTINENT_MAP.put("BR","American");

        //'EU' => ['DE', 'ES', 'FR', 'IT', 'UK', 'NL', 'SE','PL','AE', 'IN','TR'],
        SITE_CONTINENT_MAP.put("UK","Europe");
        SITE_CONTINENT_MAP.put("英国","Europe");
        SITE_CONTINENT_MAP.put("DE","Europe");
        SITE_CONTINENT_MAP.put("德国","Europe");
        SITE_CONTINENT_MAP.put("FR","Europe");
        SITE_CONTINENT_MAP.put("法国","Europe");
        SITE_CONTINENT_MAP.put("IT","Europe");
        SITE_CONTINENT_MAP.put("意大利","Europe");
        SITE_CONTINENT_MAP.put("ES","Europe");
        SITE_CONTINENT_MAP.put("西班牙","Europe");
        SITE_CONTINENT_MAP.put("SE","Europe");
        SITE_CONTINENT_MAP.put("瑞典","Europe");
        SITE_CONTINENT_MAP.put("NL","Europe");
        SITE_CONTINENT_MAP.put("荷兰","Europe");
        SITE_CONTINENT_MAP.put("PL","Europe");
        SITE_CONTINENT_MAP.put("波兰","Europe");
        SITE_CONTINENT_MAP.put("AE","Europe");
        SITE_CONTINENT_MAP.put("TR","Europe");
        SITE_CONTINENT_MAP.put("BE","Europe");
        SITE_CONTINENT_MAP.put("比利时","Europe");

        //"FE" => ["JP", "AU", "SG"],
        SITE_CONTINENT_MAP.put("JP","Japan");
        SITE_CONTINENT_MAP.put("日本","Japan");

        SITE_CONTINENT_MAP.put("AU","Australia");
        SITE_CONTINENT_MAP.put("澳大利亚","Australia");

        SITE_CONTINENT_MAP.put("IN","India");
        SITE_CONTINENT_MAP.put("印度","India");

        VC_SITE_CONTINENT_MAP.put("NA","US,CA,MX,BR");
        VC_SITE_CONTINENT_MAP.put("EU","DE,ES,FR,IT,UK,NL,SE,PL,AE,IN");
        VC_SITE_CONTINENT_MAP.put("FE","JP,AU,SG");

    }

    /**
     * 存储所有入池的key集合，为重启初始化使用，redis存储
     */
    public final static String SUFFIX_AFTER    = "_5s_times_after";
    public final static String SUFFIX_BEFORE   = "_5s_times_before";
    public final static String ADD_TASK_SOURCE = "add_task_source";

    /**
     * 等待任务结果的任务集合，redis存储
     */
    public static final String TASK_WAIT_RESULT = "task_wait_result";

    /**
     * 任务前缀
     */
    public static final String TASK_PREFIX = "pool:";

    public static final Map<String, Map<String, String>> MAP_BUS_SYSTEM = new HashMap<>();


    public static final Map<Integer,String> NETWORK_MAP = new HashMap<>();

    static {
        NETWORK_MAP.put(2,"idc");
        NETWORK_MAP.put(3,"sz");
        NETWORK_MAP.put(4,"hk");
    }

    /**
     * 爬取与账号机出池量
     */
    public static final Map<String,Integer> TASK_POP_NUM_MAP = new HashMap<>();


    /**
     * 无限制时间任务
     */
    public static final List<String>  FOREVER_TASKS = new ArrayList<>();

    /**
     * 缓冲区的优先级队列
     */
    public static PriorityBlockingQueue<Integer> priorityBlockingQueue = new PriorityBlockingQueue<Integer>(10, new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o2-o1;
        }
    });

    public static BloomFilter bloomFilter = BloomFilter.create(
            //Funnel接口实现类的实例，它用于将任意类型T的输入数据转化为Java基本类型的数据（byte、int、char等等）。这里是会转化为byte。
            Funnels.stringFunnel(Charset.forName("utf-8")),
            //期望插入元素总个数n
            5000000,
            //误差率p
            0.01);


    /**
     * 任务运行模式
     */
    public static final int LOCAL_MACHINE = 0;

    public static final int LAMBDA = 1;

    public static final int LOCAL_MACHINE_AND_LAMBDA = 2;

    /**
     * 任务类型对应Lambda信息
     */
    public static final ConcurrentHashMap<String, HashMap<String,String>> TASK_LAMBDA_MAP = new ConcurrentHashMap<>();

    public static final AtomicInteger LAMBDA_REQUEST_LIMIT = new AtomicInteger(4);

    public static final String COOKIE = "cookie";

    /**
     * 任务类型对应任务池数
     */
    public static final ConcurrentHashMap<String, AtomicInteger> taskTypeInPoolNum = new ConcurrentHashMap<>();

    /**
     * 任务类型对应入Lambda任务池数
     */
    public static final ConcurrentHashMap<String, AtomicInteger> taskTypeInLambdaNum = new ConcurrentHashMap<>();

    /**
     * 实际任务运行位置
     */
    public static final int RUN_INIT = -1;

    public static final int MACHINE = 0;

    public static final int LAMBDA_CONTAINER = 1;
}
