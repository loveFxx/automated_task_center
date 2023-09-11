/**
 * 任务监控
 */
var form;
var pageCurr;
var table;
var element;
var xmSelect;
$(function() {
    layui.use(['table','element'], function(){
        table = layui.table;
        form = layui.form;
        element = layui.element;

        //小时单位 池内数量
        tableIns=table.render({
            elem: '#taskMonitor',
            url:'/getAllTaskInPoolMetric',
            method: 'post', //默认：get请求
            page: false,
            request: {},
            response:{
                statusName: 'code', //数据状态的字段名称，默认：code
                statusCode: 200, //成功的状态码，默认：0
                countName: 'totals', //数据总数的字段名称，默认：count
                dataName: 'list' //数据列表的字段名称，默认：data
            },
            cols: [[
                {field:'id', title:'编号',align:'center',width:120,templet:function(data){
                    if (data.taskType == "sum"){
                        return "任务汇总";
                    }else {
                        return data.id;
                    }
                }}
                ,{field:'taskType', title:'任务类型',align:'center',width:150,templet:function(data){
                    if (data.taskType == "sum"){
                        return " ";
                    }else {
                        return data.taskType;
                    }
                }}
                ,{field:'', title:'任务总数',align:'center',width:100,templet:function(data){
                        return data.bufferNum + data.oneHourNum + data.oneToThreeNum + data.threeToFiveNum + data.fiveToTenNum + data.tenToOneDayNum + data.overOneDayNum;
                    }}
                ,{field:'bufferNum', title:'缓冲区总数',align:'center',width:100}
                ,{field:'', title:'池内总数',align:'center',width:100,templet: function(data){
                    var totalsInPool = 0;
                        totalsInPool = data.oneHourNum + data.oneToThreeNum + data.threeToFiveNum + data.fiveToTenNum + data.tenToOneDayNum + data.overOneDayNum;
                    return totalsInPool;
                }}
                ,{field:'oneHourNum', title:'1h之内',align:'center',width:100}
                ,{field:'oneToThreeNum', title:'1~3h之内',align:'center',width:100}
                ,{field:'threeToFiveNum', title:'3~5h之内',align:'center',width:100}
                ,{field:'fiveToTenNum', title:'5~10h之内',align:'center',width:100}
                ,{field:'tenToOneDayNum', title:'10~24h之内',align:'center',width:100}
                ,{field:'overOneDayNum', title:'24h以上',align:'center',width:100}
            ]],
            done: function (res, curr) {
                 //$('.layui-table').css("width","100%");
                curr = res.pageNum;
                pageCurr=curr;
                var pageNum = $(".layui-laypage-skip").find("input").val();
                $("#pageNum").val(pageNum);
            }
        });

        //每小时任务池吞吐量
        tableIns=table.render({
            elem: '#taskIoMetric',
            url:'/getTaskIoMetric',
            method: 'post', //默认：get请求
            page: false,
            request: {},
            response:{
                statusName: 'code', //数据状态的字段名称，默认：code
                statusCode: 200, //成功的状态码，默认：0
                countName: 'totals', //数据总数的字段名称，默认：count
                dataName: 'list' //数据列表的字段名称，默认：data
            },
            cols: [[
                {field:'id', title:'编号',align:'center',width:120,templet:function(data){
                        if (data.taskType == "sum"){
                            return "任务汇总";
                        }else {
                            return data.id;
                        }
                    }}
                ,{field:'taskType', title:'任务类型',align:'center',width:180,templet:function(data){
                        if (data.taskType == "sum"){
                            return " ";
                        }else {
                            return data.taskType;
                        }
                    }}
                ,{field:'', title:'I/O比',align:'center',width:100,templet:function(data){
                        if (data.inPoolNum == 0 ||data.outPoolNum == 0  ){
                            return 0;
                        }else {

                            return fixedDigit(data.inPoolNum/data.outPoolNum,3,true);

                        }
                    }}
                ,{field:'inPoolNum', title:'入池数',align:'center',width:100}
                ,{field:'outPoolNum', title:'出池数',align:'center',width:100}
                ,{field:'createdAt', title:'创建时间',align:'center',width:190}

            ]],
            done: function (res, curr) {
                //$('.layui-table').css("width","100%");
                curr = res.pageNum;
                pageCurr=curr;
                var pageNum = $(".layui-laypage-skip").find("input").val();
                $("#pageNum").val(pageNum);
            }
        });

        //分类任务监控
        tableIns=table.render({
            elem: '#taskMetric',
            url:'/getTaskMetric',
            method: 'post', //默认：get请求
            page: false,
            request: {},
            response:{
                statusName: 'code', //数据状态的字段名称，默认：code
                statusCode: 200, //成功的状态码，默认：0
                countName: 'totals', //数据总数的字段名称，默认：count
                dataName: 'list' //数据列表的字段名称，默认：data
            },
            cols: [[
                {field:'id', title:'编号',align:'center',width:120}

                ,{field:'taskType', title:'任务类型',align:'center',width:160}

                ,{field:'', title:'当天成功率',align:'center',width:150,templet:function(data){
                        if (data.succeedNum == 0 || data.generatedNum == 0){
                            return 0;
                        }else {
                            return fixedDigit(data.succeedNum/data.generatedNum,3,true);
                        }
                    }}
                ,{field:'generatedNum', title:'当天生成任务总数',align:'center',width:100}
                ,{field:'unExecutedNum', title:'当天未执行数',align:'center',width:100}
                ,{field:'totalSucceedNum', title:'总成功数（在所有生成任务中计算）',align:'center',width:150}
                ,{field:'succeedNum', title:'成功数（只在当天生成任务中计算）',align:'center',width:180}
                ,{field:'fileNum', title:'当天文件数',align:'center',width:100}
                ,{field:'failedNum', title:'当天失败数',align:'center',width:100}
                ,{field:'createdAt', title:'创建时间',align:'center',width:170}

            ]],
            done: function (res, curr) {
                //$('.layui-table').css("width","100%");
                curr = res.pageNum;
                pageCurr=curr;
                var pageNum = $(".layui-laypage-skip").find("input").val();
                $("#pageNum").val(pageNum);
            }
        });
        //分类任务监控
        tableIns=table.render({
            elem: '#delayQueue',
            url:'/getDelayQueue',
            method: 'post', //默认：get请求
            page: false,
            request: {},
            response:{
                statusName: 'code', //数据状态的字段名称，默认：code
                statusCode: 200, //成功的状态码，默认：0
                countName: 'totals', //数据总数的字段名称，默认：count
                dataName: 'list' //数据列表的字段名称，默认：data
            },
            cols: [[
                {field:'systemOrTask', title:'',align:'center',width:160}
                ,{field:'oneMinuteDelay', title:'1分钟延迟数',align:'center',width:160}
                ,{field:'tenMinuteDelay', title:'10分钟延迟数',align:'center',width:160}
                ,{field:'fortyMinuteDelay', title:'40分钟延迟数',align:'center',width:160}
                ,{field:'twoHourDelay', title:'2小时延迟数',align:'center',width:160}
                ,{field:'twentyTwoHourDelay', title:'22小时延迟数',align:'center',width:160}

            ]],
            done: function (res, curr) {
                //$('.layui-table').css("width","100%");
                curr = res.pageNum;
                pageCurr=curr;
                var pageNum = $(".layui-laypage-skip").find("input").val();
                $("#pageNum").val(pageNum);
            }
        });

    });




});
// js强制固定多少位小数,是否四舍五入取决于round参数，默认四舍五入
function fixedDigit (vlue, digit, round) {
    if (round) { // 四舍五入
        return (Math.round(parseFloat(vlue)*Math.pow(10,digit))/Math.pow(10,digit)).toFixed(digit)
    } else { // 不四舍五入
        return (parseInt(parseFloat(vlue)*Math.pow(10,digit))/Math.pow(10,digit)).toFixed(digit)
    }
}




