/**
 * 任务库列表管理
 */
var pageCurr;
var form;
var table;

var basicDataPort=8991;
var monitorPort=8999;

$(function() {

    var afterUrl =  window.location.search.substring(1);
    if (afterUrl != ""){
        $("#taskSourceId").val(afterUrl);
    };

    $("#heartBeatStat").click(function(){
        var date=$("#heartBeatStatDate").val()
        // var url=window.location.origin.replace(basicDataPort,monitorPort);
        var url=MONITOR_URL;
        url+='/download/heartBeatStat/'+date;
        document.getElementById("heartBeatStat").href = url
        document.getElementById("heartBeatStat").setAttribute('download',"")
    });

    $("#taskSuccessStat").click(function(){
        var date=$("#taskSuccessStatDate").val()
        // var url=window.location.origin.replace(basicDataPort,monitorPort)
        var url=MONITOR_URL;
        url+='/download/taskSuccessStat/'+date;
        document.getElementById("taskSuccessStat").href = url
        document.getElementById("taskSuccessStat").setAttribute('download',"")
    });


    $("#taskFailureReasonStat").click(function(){
        var date=$("#taskFailureReasonStatDate").val()
        // var url=window.location.origin.replace(basicDataPort,monitorPort)
        var url=MONITOR_URL;
        url+='/download/taskFailureReasonStat/'+date;
        document.getElementById("taskFailureReasonStat").href = url
        document.getElementById("taskFailureReasonStat").setAttribute('download',"")
    });


    $("#continentAccountTaskStat").click(function(){
        var date=$("#continentAccountTaskStatDate").val()
        // var url=window.location.origin.replace(basicDataPort,monitorPort)
        var url=MONITOR_URL;
        url+='/download/continentAccountTaskStat/'+date;
        document.getElementById("continentAccountTaskStat").href = url
        document.getElementById("continentAccountTaskStat").setAttribute('download',"")
    });




    layui.use('laydate', function(){
        var laydate = layui.laydate;


        var heartBeatStatDate = laydate.render({
            elem: '#heartBeatStatDate'
            ,type: 'date'
            ,trigger: 'click'//为了防止一个页面用两次laydate闪退
            ,min: '2021-11-01'
            ,max: nowDate1()


        });

        var taskSuccessStatDate = laydate.render({
            elem: '#taskSuccessStatDate'
            ,type: 'date'
            ,trigger: 'click'//为了防止一个页面用两次laydate闪退
            ,min: '2021-11-01'
            ,max: nowDate1()


        });


        var taskFailureReasonStatDate = laydate.render({
            elem: '#taskFailureReasonStatDate'
            ,type: 'date'
            ,trigger: 'click'//为了防止一个页面用两次laydate闪退
            ,min: '2021-11-01'
            ,max: nowDate1()

        });


        var continentAccountTaskStatDate = laydate.render({
            elem: '#continentAccountTaskStatDate'
            ,type: 'date'
            ,trigger: 'click'//为了防止一个页面用两次laydate闪退
            ,min: '2021-11-01'
            ,max: nowDate1()

        });

        var start = laydate.render({
            elem: '#startTime'
            ,type: 'date'
            ,trigger: 'click'//为了防止一个页面用两次laydate闪退
            ,min: '2021-11-01'
            ,max: nowDate1()
            ,done: function (value, date, endDate) {
                end.config.min = {
                    year : date.year,
                    month : date.month - 1,
                    date : date.date
                };
            }

        });
        var end = laydate.render({
            elem: '#endTime'
            ,type: 'date'
            ,trigger: 'click'//为了防止一个页面用两次laydate闪退
            ,max: nowDate1()
            ,done: function (value, date, endDate) {
                start.config.max = {
                    year : date.year,
                    month : date.month - 1,
                    date : date.date
                };
            }

        });

        function nowDate1(){
            var now = new Date();
            return now.getFullYear()+"-" + (now.getMonth()+1) + "-" + now.getDate();
        }
    });

    layui.use('table', function(){
        table = layui.table;
        form = layui.form;

        tableIns=table.render({
            elem: '#taskLogsTable',
            url:'/taskLogs',
            method: 'post', //默认：get请求
            where: {"taskSourceId": afterUrl},
            cellMinWidth: 80,
            page: true,
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[
                {field:'', title:'任务库id/任务类型',align:'center',width:"15%",templet: function (data) {
                        if(data.taskSourceId == null && data.taskName == null){
                            return;
                        }
                        return data.taskSourceId+'<br>' + data.taskName
                    }}
                // ,{field:'', title: '结果hash/hash',align:'center',width:"15%",templet: function (data) {
                //         if(data.resultHashKey == null && data.hashKey == null){
                //             return;
                //         }
                //         return data.resultHashKey+'<br>' + data.hashKey
                //     }}
                ,{field:'retryTimes',title:'重试次数',align:'center',width:"10%"}
                ,{field:'event',title:'事件/备注',align:'center',width:"15%",templet: function (data) {
                        if(data.event == null && data.explain == null){
                            return;
                        }
                        var result = '';
                        if(data.event == '1'){
                            result = '生成任务入池';
                        }else if(data.event == '2'){
                            result = '缓冲区入池';
                        }else if(data.event == '3'){
                            result = '出池';
                        }else if(data.event == '4'){
                            result = '返回结果成功';
                        }else if(data.event == '5'){
                            result = '返回结果失败';
                        }else if(data.event == '6'){
                            result = '重新生成任务入池';
                        }else if(data.event == '7'){
                            result = '任务超时结果强制失败';
                        }else if(data.event == '8'){
                            result = '池满，低优先级出池';
                        }else if(data.event == '9'){
                            result = '手工生成任务';
                        }else if(data.event == '10'){
                            result = '手工入池';
                        }
                    return result+'<br>' + data.explain
                    }}
                ,{ field: '', title: '系统端传入参数', width:"15%",templet: function(d){
                        if(d.clientParams.length==0)
                            return "";
                        var result = JSON.stringify(d.clientParams).replaceAll('\\','').substring(1).slice(0,-1);
                        return  result+'</span>';
                    }}
                ,{field:'platform',title:'执行平台',align:'center',width:"10%"}
                , {
                    field: '', title: '账号/大洲', align: 'center',width:"10%",
                    templet: function (data) {
                        if(data.account == null && data.continent == null){
                            return;
                        }
                        return data.account+'<br>' + data.continent
                    }
                }
                ,{field:'remoteIp',title:'远程/代理IP',align:'center',width:"15%",
                    templet: function (d) {
                        var remoteIp = d.remoteIp;
                        if(remoteIp == null || remoteIp == ''){
                            remoteIp = '/';
                        }
                        var proxyIp = d.proxyIp;
                        if(proxyIp == null || proxyIp == ''){
                            proxyIp = '/';
                        }
                        return remoteIp+'<br>' + proxyIp
                    }}
                ,{ field: '', title: '中心端生成参数', width:"15%",templet: function(d){
                        if(d.centerParams.length==0)
                            return "";
                        var result = JSON.stringify(d.centerParams).replaceAll('\\','').substring(1).slice(0,-1);
                        return  result+'</span>';
                    }}


                ,{field:'createdTime',title:'事件时间',align:'center',width:"16%"}

            ]],
            done: function(res, curr){
                formSelects.data('platform', 'server', {
                    url: '/getCrawlPlatform',
                    direction: 'down',
                });

                $("[data-field='event']").children().each(function(){
                    if($(this).text()=='1'){
                        $(this).text("生成任务")
                    }else if($(this).text()=='2'){
                        $(this).text("入池")
                    }else if($(this).text()=='3'){
                        $(this).text("出池")
                    }else if($(this).text()=='4'){
                        $(this).text("返回结果成功")
                    }else if($(this).text()=='5'){
                        $(this).text("返回结果失败")
                    }
                });


                //得到数据总量
                curr = res.pageNum;
                pageCurr=curr;
            }
        });

    });

    loadSelects();
    //搜索框
    layui.use(['form','laydate'], function(){
        var form = layui.form ,layer = layui.layer;
        //TODO 数据校验
        //监听搜索框
        form.on('submit(searchSubmit)', function(data){

            var taskLogs= {};

            taskLogs.resultHashKey=data.field.hashKey;
            taskLogs.platform=data.field.platform;
            taskLogs.remoteIp=data.field.remoteIP;
            taskLogs.proxyIp=data.field.proxyIP;
            taskLogs.event=data.field.event;
            taskLogs.taskSourceId=data.field.taskSourceId;

            if(data.field.startTime!='')
                taskLogs.startTime=data.field.startTime+' 00:00:00'
            if(data.field.endTime!='')
                taskLogs.endTime=data.field.endTime+' 23:59:59'

            //前端处理成('AM00326','AM00083','AM00331')格式 方便Mybatis处理 where in
            if(data.field.account!=''){
                var accountStr=data.field.account.toString();
                //accountStr=accountStr.replace(/,/g,"','");
                //accountStr="('"+accountStr+"')";
                taskLogs.account=accountStr;
            }
            if(data.field.taskName!=''){
                var taskNameStr=data.field.taskName.toString();
                // taskNameStr=taskNameStr.replace(/,/g,"','");
                // taskNameStr="('"+taskNameStr+"')";
                taskLogs.taskName=taskNameStr;
            }

            //防止layui表格reload where参数保留
            if(data.field.startTime==''){
                taskLogs.startTime=null;
            }
            if(data.field.hashKey==''){
                taskLogs.resultHashKey=0;//如果搜索框为空 置0 mybatis会把整型的0当做''
            }
            if(data.field.endTime==''){
                taskLogs.endTime=null;
            }
            if(data.field.account==''){
                taskLogs.account=null;
            }
            if(data.field.taskName==''){
                taskLogs.taskName=null;
            }
            if(data.field.taskSourceId==''){
                taskLogs.taskSourceId=null;
            }

            tableIns.reload({
                where: taskLogs ,
                page: {
                    curr: pageCurr //从当前页码开始
                }
            });
            return false;
        });
    });
});











function loadSelects() {
    formSelects.data('taskNameSearch', 'server', {
        url: '/getDropDownTaskName',
        direction: 'down',
    });
    formSelects.data('accountSearch', 'server', {
        url: '/getDropDownAccount',
        direction: 'down',
    });
}



function load(obj){
    //重新加载table
    tableIns.reload({
        page: {
            curr: pageCurr //从当前页码开始
        }
    });
}

function cleanTask(){
    $("#uniqueIdSearch").val("");

}
