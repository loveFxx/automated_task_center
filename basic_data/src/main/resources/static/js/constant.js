/**
 *  通用状态值  1是有效 0是无效
 * @type {number}
 */
var STATUS_VALID = 1;
var STATUS_INVALID = 0;
var STATUS_DISABLE = -1;
var STATUS_REMOVE = -10;

var SUCCESS_CODE = 1;
var ERROR_CODE = 8;
var LOCAL_URL = window.location.hostname
var MONITOR_URL = LOCAL_URL+":8999";

/**
 * 任务大类型 1是可爬取平台 2按照账号站点的账号平台
 * @type {number}
 */
var LARGE_TASK_TYPE_CRAWL_PLATFORM = 1;
var LARGE_TASK_TYPE_ACCOUNT_PLATFORM = 2;

// var RESPONSE_BODY = new Array();
RESPONSE_BODY = {
    statusName: 'code', //数据状态的字段名称，默认：code
    statusCode: 200, //成功的状态码，默认：0
    countName: 'totals', //数据总数的字段名称，默认：count
    dataName: 'list' ,//数据列表的字段名称，默认：data
    pageNum: 'pageNum' //当前页码
};

REQUEST_BODY = {
    pageName: 'pageNum', //页码的参数名称，默认：pageNum
    limitName: 'pageSize' //每页数据量的参数名，默认：pageSize
};