/**
 * 用户管理
 */
var pageCurr;
var form;
var table;
var lastData;
var url;
$(function () {


    var basicDataPort = 8991;
    var monitorPort = 8999;

    //非常重要 下面都用这个
    // url = window.location.origin.replace(basicDataPort, monitorPort);
    url = "http://192.168.201.29:8999";
    var afterUrl =  window.location.search.substring(1);
    if (afterUrl != ""){
        $("#ipSearch").val(afterUrl);
    };

    layui.use('table', function () {
        table = layui.table;
        form = layui.form;
        // xmSelect = layui.xmSelect;
        tableIns = table.render({
            elem: '#machineList',
            url: '/getMachineList',
            where: {"ip": afterUrl},
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: true,
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[
                {field: 'id', title: 'ID', hide: true}
                , {field: 'status', title: '启用/禁用', align: 'center', width: "10%", templet: '#statusTemp'}
                , {field: 'ip', title: '远程连接IP', align: 'center', width: "12%"}
                , {field: 'machineType', title: '机器类型', align: 'center', width: "9%"}
                , {field: 'machineStatus', title: '状态', align: 'center', width: "6%", templet: function (d) {
                        var machineStatus = '';
                        if (d.machineStatus == STATUS_VALID) {
                            machineStatus = '有效'
                            return '<a class="layui-table-link" href="javascript:void(0);" lay-event="detail">' + machineStatus + '</a>'
                        } else {
                            machineStatus = '失效'
                            return '<a class="layui-table-link" style="color: orange" href="javascript:void(0);" lay-event="detail">' + machineStatus + '</a>'
                        }
                    }}
                , {field: '', title: '账号平台/店铺代号/大洲/后台登录方式', width: "22%", templet: function (d) {
                        var machineTaskTypeList = d.machineWorkTypeList;
                        var result = '';
                        $.each(machineTaskTypeList, function (n, value) {
                            if (value.platformType == LARGE_TASK_TYPE_ACCOUNT_PLATFORM ) {
                                var platform;
                                var account;
                                var continents;
                                var loginPlatform;
                                var status;
                                if (value.platform == null || value.platform == '') {
                                    platform = '-'
                                } else {
                                    platform = value.platform
                                }
                                if (value.account == null || value.account == '') {
                                    account = '-'
                                } else {
                                    account = value.account
                                }
                                if (value.continents == null || value.continents == '') {
                                    continents = '-'
                                } else {
                                    continents = value.continents
                                }
                                if (value.loginPlatform == null || value.loginPlatform == 0) {
                                    loginPlatform = '-'
                                } else {
                                    if (value.loginPlatform == 1) {
                                        loginPlatform = "帐号机"
                                    }else if (value.loginPlatform ==2){
                                        loginPlatform = "超级浏览器"
                                    } else{
                                        loginPlatform = "未知数字"
                                    }
                                }

                                if (value.status == -10) {
                                    status="移除"
                                } else if (value.status == -2){
                                    status="未验证"
                                }else if (value.status == -1){
                                    status="禁用"
                                }else if (value.status == 0){
                                    status="无效"
                                }else if (value.status == 1){
                                    status="有效"
                                }

                                if (result != '') {
                                    result += '<br>';
                                }
                                result += platform + '/' + account + '/' + continents + '/' + loginPlatform + '/' +status;
                            }
                        });
                        return result + '</span>'
                    }}
                , {field: '', title: '最后执行情况', align: 'center', width: "20%", templet: function (d) {
                        var lastExecuteTask = d.lastExecuteTask;
                        var lastExecuteWorkType = d.lastExecuteWorkType;
                        var lastExecuteTime = d.lastExecuteTime;
                        if(lastExecuteTask == null){
                            lastExecuteTask = ''
                        }
                        if(lastExecuteWorkType == null){
                            lastExecuteWorkType = ''
                        }
                        if(lastExecuteTime == null){
                            lastExecuteTime = ''
                        }
                        var result = lastExecuteTask+"/"+lastExecuteWorkType+'<br>'+lastExecuteTime;
                        return  result+'</span>'
                    }}
                , {field: 'lastHeartbeat', title: '上次心跳时间', align: 'center', width: "18%"}
                , {field: 'dueTime', title: '过期时间', align: 'center', width: "9%"}
                , {field: 'crawlPlatform', title: '可爬取平台', align: 'center', width: "15%", templet: function (d) {
                        var crawlPlatformName = d.crawlPlatformName;
                        var result = '';
                        if(crawlPlatformName == null){
                            return  result+'</span>'
                        }
                        var assistAuditArry =crawlPlatformName.split(",");
                        var count = 0;
                        $.each(assistAuditArry,function(i,value){
                            count = count+1;
                            if(count>2 && (count+1)%2 == 0){
                                result += '<br>';
                            }
                            if(count%2 == 0){
                                result += '/';
                            }
                            result +=  value;
                        });
                        return  result+'</span>'
                    }}
                , {field: '', title: '操作人/操作时间', align: 'center', width: "18%", templet: function (d) {
                        var username = d.updateUser;
                        var updateTime = d.updateTime;
                        if (username == null || username == '') {
                            return '-' + '/' + updateTime + '</span>'
                        }
                        return username + '<br>' + updateTime + '</span>'
                    }}
                , {title: '操作', align: 'center', toolbar: '#optBar', width: "20%"}
            ]],
            done: function (res, curr) {

                //如果是异步请求数据方式，res即为你接口返回的信息。
                //如果是直接赋值的方式，res即为：{data: [], count: 99} data为当前页数据、count为数据总长度
                if (res.list != null) {
                    formSelects.data('accountSearch', 'local', {
                        direction: 'down',
                        arr: res.list[0].accountSelect
                    });
                    formSelects.data('taskTypeSearch', 'local', {
                        direction: 'down',
                        arr: res.list[0].taskTypeSelect
                    });
                    formSelects.data('crawlPlatformSearch', 'server', {
                        url: '/getCrawlPlatform',
                        direction: 'down',
                    });
                }


                if (lastData != null) {
                    var account = lastData.field.account;
                    if (account != null) {
                        var assistAuditArry = account.split(",");
                        formSelects.value('accountSearch', assistAuditArry);
                    }
                    var taskType = lastData.field.taskType;
                    if (taskType != null) {
                        var assistAuditArry = taskType.split(",");
                        formSelects.value('taskTypeSearch', assistAuditArry);
                    }
                }

                $("[data-field='machineType']").children().each(function () {
                    if ($(this).text() == '0') {
                        $(this).text("账号机")
                    } else if ($(this).text() == '1') {
                        $(this).text("内网VPS")
                    } else if ($(this).text() == '2') {
                        $(this).text("外网VPS")
                    } else if ($(this).text() == '3') {
                        $(this).text("重庆VPS")
                    } else if ($(this).text() == '4') {
                        $(this).text("重庆账号机")
                    }
                });
                //得到数据总量
                curr = res.pageNum;
                pageCurr = curr;
            }
        });

        //监听工具条
        table.on('tool(machineTable)', function (obj) {
            var data = obj.data;
            if (obj.event === 'del') {
                //删除
                delMachine(data, data.id);
            } else if (obj.event === 'edit') {
                //编辑
                editMachine(data, "编辑");
                formSelectsData(data);
                showMachineTaskType(data);
            } else if (obj.event === 'detail') {
                machineDetail(data);
            } else if (obj.event === 'heartBeatLog') {
                heartBeatLog(data);
            }else if (obj.event === 'exeStat') {
                exeStat(data);
            }

        });


        //监听提交
        form.on('submit(userSubmit)', function (data) {
            // TODO 校验
            formSubmit(data);
            return false;
        });

        form.on('switch(status)', function (data) {
            var id = data.value;
            var status = this.checked ? STATUS_VALID : STATUS_INVALID;
            $.ajax({
                type: "POST",
                data: {id: id, status: status},
                url: "/updateStatus",
                success: function (data) {
                    //{"code":"SUCCESS","msg":"更新成功","data":1}
                    if (data.code == SUCCESS_CODE) {
                        layer.msg(data.msg, {shift: -1, time: 600}, function () {
                            layer.closeAll();
                            load({id: id, status: status});
                        });
                    } else {
                        layer.msg(data.msg, {icon: 2});
                    }
                },
                error: function () {
                    layer.msg("操作请求错误，请您稍后再试", {icon: 2}, function () {
                        layer.closeAll();
                        //加载load方法
                        load(obj);//自定义
                    });
                }
            });

        });

        form.on('switch(taskTypeStatus)', function (data) {
            var id = data.value;
            var status = this.checked ? STATUS_VALID : STATUS_INVALID;
            $.ajax({
                type: "POST",
                data: {id: id, status: status},
                url: "/updateMachineTaskTypeStatus",
                success: function (data) {
                    //{"code":"SUCCESS","msg":"更新成功","data":1}
                    if (data.code == SUCCESS_CODE) {
                        layer.msg(data.msg, {shift: -1, time: 600}, function () {
                            layer.closeAll();
                            load({id: id, status: status});
                        });
                    } else {
                        layer.msg(data.msg, {icon: 2});
                    }
                },
                error: function () {
                    layer.msg("操作请求错误，请您稍后再试", {icon: 2}, function () {
                        layer.closeAll();
                        //加载load方法
                        load(obj);//自定义
                    });
                }
            });
        });

        form.on('switch(isBrowserFilter)', function (data) {
            var id = data.value;
            var isBrowser = this.checked ? STATUS_VALID : STATUS_INVALID;
            $.ajax({
                type: "POST",
                data: {id: id, isBrowser: isBrowser},
                url: "/updateMachineWorkTypeIsBrowser",
                success: function (data) {
                    //{"code":"SUCCESS","msg":"更新成功","data":1}
                    if (data.code == SUCCESS_CODE) {
                        layer.msg(data.msg, {shift: -1, time: 600}, function () {
                            layer.closeAll();
                            load({id: id, status: status});
                        });
                    } else {
                        layer.msg(data.msg, {icon: 2});
                    }
                },
                error: function () {
                    layer.msg("操作请求错误，请您稍后再试", {icon: 2}, function () {
                        layer.closeAll();
                        //加载load方法
                        load(obj);//自定义
                    });
                }
            });
        });
    });

    //搜索框
    layui.use(['form', 'laydate'], function () {
        var form = layui.form, layer = layui.layer;
        //TODO 数据校验
        //监听搜索框
        form.on('submit(searchSubmit)', function (data) {
            //重新加载table
            lastData = data;
            tableIns.reload({
                where: data.field,
                page: {
                    curr: pageCurr //从当前页码开始
                }
            });
            return false;
        });
    });
});


// 心跳日志
function heartBeatLog(data) {
    $clone = $(".layui-colla-item").clone(true).css('display', 'block')
    $templateClone = $(".layui-colla-item").clone(true)
    layer.open({
        type: 1,
        title: "心跳日志",
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['550px', '80%'],
        content: $('#heartBeatLog')
       ,end: function () {
            cleanUser();}
       ,success: function () {
            $.ajax({
                type: "POST",
                // data: JSON.stringify(obj.field),
                data: {"machineId": data.id},
                url: "/getMachineHeartbeat",
                success: function (data) {
                    if (data.list != null) {
                        for (let i = 0; i < data.list.length; i++) {
                            let curJson = data.list[i]
                            let heartBeatTableData = []
                            for (const key in curJson) {
                                let val = {}
                                if (key == "id" || key == "machineId")
                                    continue;
                                if (key == "diskSpace") {
                                    try{
                                        let diskJson = JSON.parse(curJson[key])
                                        for (const key2 in diskJson) {
                                            let val2 = {}
                                            let free_space = diskJson[key2]['free_space']
                                            let size = diskJson[key2]['size']
                                            let usedNum = size - free_space
                                            usedNum = (usedNum / size * 100).toFixed(2)
                                            val2["row1"] = key2
                                            val2["row2"] = usedNum + "%"
                                            heartBeatTableData.push(val2)
                                        }
                                    }catch (e) {
                                    }

                                    continue
                                }
                                val["row1"] = key
                                val["row2"] = data.list[i][key]
                                heartBeatTableData.push(val)
                            }
                            $curClone = $clone.clone(true)
                            var tableId = "heartBeatLogTable"
                            tableId = tableId + i
                            $curClone.find("table").attr("id", tableId)//赋予每个独一id
                            $(".layui-collapse").append($curClone)
                            $(".layui-colla-title").eq(i + 1).append(curJson["heartbeat"])// 必须在上面添加了克隆折叠面板后再改；因为第0个是模板 所以i+1
                            tableId = "#" + tableId
                            table.render({
                                elem: tableId
                                , width: 350
                                , cols: [[
                                    {field: 'row1', title: 'row1'}
                                    , {field: 'row2', title: 'row2'}
                                ]]
                                , data: heartBeatTableData
                                , done: function () {
                                }
                            });
                        }
                    }
                    $("#heartBeatLog").find('th').hide();//隐藏表头
                },
                error: function () {
                    layer.msg("操作请求错误，请您稍后再试", {icon: 2}, function () {
                    });
                },
            });}
       , end: function () {//关掉弹窗后上次动态添加的然后恢复模板
            //alert("关掉了")
            $(".layui-collapse").empty();
            $(".layui-collapse").append($templateClone);
        }
    });
}

// 机器详情
function machineDetail(data) {
    $('#diskSpace').html("");
    if (data.diskSpace != null) {
        let result = '';
        const diskSpaceJson = JSON.parse(data.diskSpace)
        let maxSpaceNum = 10;//最大空格数
        for (const val in diskSpaceJson) {
            if (result != '') {
                result += '<br>';
            }
            const diskSpaceInnerJson = diskSpaceJson[val]
            result += val + ' '
            let free_space = diskSpaceInnerJson['free_space'] / 1000000000
            free_space = free_space.toFixed(2)
            let size = diskSpaceInnerJson['size'] / 1000000000
            let usedNum = size - free_space
            usedNum = usedNum.toFixed(2)
            let space = ''
            for (let i = 0; i < maxSpaceNum - usedNum.length; i++) {
                space += '&nbsp'
            }
            result += '已用: ' + usedNum + space + '剩余: ' + free_space
        }
        result += '</span>';
        $('#diskSpace').append(result);
    }
    $("#id").val(data.id);
    $("#lastHeartbeat").val(data.lastHeartbeat);
    $("#cpu").val(data.cpu);
    $("#memory").val(data.memory);

    layer.open({
        type: 1,
        title: "机器详情",
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['550px', '80%'],
        content: $('#setMachineDetail'),
        end: function () {
            cleanUser();
        }
    });
}

function exeStat(data) {



    layer.open({
        type: 1,
        title: "exeStat",
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['800px', '800px'],
        content: $('#exeStatWindow'),
        end: function () {

        },
        success: function () {
            var ip= data.ip
            let yesterday=new Date();
            yesterday.setTime(yesterday.getTime()-24*60*60*1000);

            //2021-12-26-16
            let month=yesterday.getMonth()+1;

            if (month.toString().length == 1) {
                month = "0" + month;
            }
            let day=yesterday.getDate();
            if (day.toString().length == 1) {
                day = "0" + day;
            }
            let hour=yesterday.getHours();
            if (hour.toString().length == 1) {
                hour = "0" + hour;
            }
            let period=yesterday.getFullYear()+"-"+month+"-"+day+"-"+hour
            console.log(period)
            $.ajax({
                type: "POST",
                url: url+"/machineExeTaskStat",
                data: {"ip":data.ip,"period":period},
                success: function (data) {
                    console.log(505,data)



                    var dataArr=[]
                    for (let dataKey in data) {
                        dataArr.push({'taskType':dataKey,'stat':data[dataKey]})
                    }
                    table.render({
                        elem: '#machineExeTable',
                        data:dataArr,
                        limit: 1000,
                        cols: [[
                            {field: 'taskType', title: 'taskType'},
                            {field: 'stat', title: 'stat',hide:true},
                            {title:'操作',align:'center', toolbar:'#machineExeToolbar'}
                        ]],
                        done: function () {


                            table.on('tool(machineExeTable)', function (obj) {
                                if (obj.event === 'machineExeEvent') {
                                    openExeStatTable(obj.data.taskType,obj.data.stat)

                                }
                            });

                        }
                    });

                }
            });
        }
    });
}



function openExeStatTable(taskType,stat) {


    layer.open({
        type: 1,
        title: taskType,
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['550px', '600px'],
        content: $('#exeStatTableWindow'),

        success: function () {




            var dataArr=[]
            for (let dataKey in stat) {
                let statElement = stat[dataKey];
                dataArr.push({period:dataKey,totalCount:statElement['totalCount'],successCount:statElement['successCount']
                    ,failureCount:statElement['failureCount'],avgTime:statElement['avgTime'],totalTime:statElement['totalTime']})


            }


            table.render({
                elem: '#machineExeTable2',
                data:dataArr,
                limit:1000,
                cols: [[
                    {field: 'period', title: 'period',width:'30%'},
                    {field: 'totalCount', title: '出池总数'},
                    {field: 'successCount', title: '成功'},
                    {field: 'failureCount', title: '失败'},
                    {field: 'avgTime', title: 'avgTime'},
                    {field: 'totalTime', title: 'totalTime'}
                ]],
                done: function () {
                }
            });



        }
    });
}

//提交表单
function formSubmit(obj) {
    var result = new Array();
    for (var val in obj.field) {
        if (val.substring(0, 9) == "_taskType") {
            console.log(true);
            var id = val.replace("_taskType", "")
            var taskTypeVal = obj.field[val];
            result.push({"id": id, "machineId": obj.field.id, "taskTypeName": taskTypeVal});
        }
    }
    var date = {}
    if(obj.field.id == null || obj.field.id == '' ){
        date = {
            "id": 0,
            "machineType": obj.field.machineType,
            "ip": obj.field.ip
        }
    }else {
        date = {
            "id": obj.field.id, "machineType": obj.field.machineType,
            "username": obj.field.username, "password": obj.field.password,
            "dialUsername": obj.field.dialUsername, "dialPassword": obj.field.dialPassword,
            "dueTime": obj.field.dueTime, "machineTaskTypeLists": JSON.stringify(result),
            "serviceProvider": obj.field.serviceProvider, "operatingSystem": obj.field.operatingSystem,
            "crawlPlatform": obj.field.crawlPlatform, "ip": obj.field.ip,"mac": obj.field.mac,
            "expiring": obj.field.expiring,"maxIO": obj.field.maxIO
        }
    }
    $.ajax({
        type: "POST",
        data: date,
        url: "/updateMachine",
        success: function (data) {
            //{"code":"SUCCESS","msg":"更新成功","data":1}
            if (data.code == SUCCESS_CODE) {
                layer.msg(data.msg, {shift: -1, time: 600}, function () {
                    layer.closeAll();
                    load(obj);
                });
            } else {
                layer.msg(data.msg, {shift: -1, time: 600, icon: 2}, function () {
                    layer.closeAll();
                    load(obj);
                });
            }
        },
        error: function () {
            layer.msg("操作请求错误，请您稍后再试", {icon: 2}, function () {
                layer.closeAll();
                //加载load方法
                load(obj);//自定义
            });
        }
    });
}

//机器添加
function addMachine() {
    $("#id").val(0);
    layer.open({
        type: 1,
        title: '机器添加',
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['850px', '80%'],
        content: $('#addCrawlerServer'),
        end: function () {
            cleanUser();
        }
    });
}

//修改
function editMachine(data, title) {
    if (data == null || data == "") {
        $("#id").val("");
    } else {
        $("#id").val(data.id);
        $("#ip").val(data.ip);
        $("#mac").val(data.mac);
        $("#username").val(data.username);
        $("#password").val(data.password);
        $("#machineType").val(data.machineType);
        $("#dialUsername").val(data.dialUsername);
        $("#dialPassword").val(data.dialPassword);
        $("#dueTime").val(data.dueTime);
        $("#operatingSystem").val(data.operatingSystem);
        $("#serviceProvider").val(data.serviceProvider);
        $("#taskType").val(data.taskType);
        $("#maxIO").val(data.maxIO);
        // formSelects.value('crawlPlatform', data.crawlPlatform);
        form.render('select'); //这个很重要
    }
    var pageNum = $(".layui-laypage-skip").find("input").val();
    $("#pageNum").val(pageNum);

    layer.open({
        type: 1,
        title: '机器设置',
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['850px', '80%'],
        content: $('#setCrawlerServer'),
        end: function () {
            cleanUser();
        }
    });
}


// 查询机器的可执行任务类型
function showMachineTaskType(data) {
    table.render({
        elem: '#showMachineTaskType',
        url: '/showMachineTaskType',
        where: {"machineId": data.id},
        method: 'post', //默认：get请求
        cellMinWidth: 80,
        // page: true,
        request: REQUEST_BODY,
        response: RESPONSE_BODY,
        cols: [[
            , {field: 'id', title: 'ID', hide: true}
            , {field: 'machineId', title: 'machineId', hide: true}
            , {field: 'platform', title: '平台/平台类型', align: 'center', width: "20%", templet: function (d) {
                var platformType = '';
                if (d.platformType == LARGE_TASK_TYPE_ACCOUNT_PLATFORM) {
                    platformType = "账号平台";
                    } else if (d.platformType == LARGE_TASK_TYPE_CRAWL_PLATFORM) {
                    platformType= "可爬取平台";
                    }
                return d.platformName +'<br>'+platformType+ '</span>';
                }}
            , {field: '', title: '账号/大洲', align: 'center', width: "20%", templet: function (d) {
                    var result = '';
                    var account = '';
                    var continents = '';
                    if (d.account == null || d.account == '') {
                        account = '-'
                    } else {
                        account = d.account
                    }
                    if (d.continents == null || d.continents == '') {
                        continents = '-'
                    } else {
                        continents = d.continents
                    }
                    if (result != '') {
                        result += '<br>';
                    }
                    result += account + '/' + continents;
                    return result + '</span>'
                }}
            , {field: '', title: '任务类型', align: 'center', width: "25%", templet: function (d) {
                    return '<select name="_taskType' + d.id + '" id="_taskType' + d.id + '" xm-select="_taskType' + d.id + '" xm-select-search=""  xm-select-search-type="dl">' +
                        +'<option value="">可多选</option>' +
                        '</select>'
                }}
            , {field: 'isBrowser', title: '是否开启浏览器', align: 'center', width: "15%", templet: '#isBrowserTemp'}
            , {field: 'status', title: '启用/禁用', align: 'center', width: "20%", templet: '#taskTypeStatusTemp'} //event: '#taskTypeStatusTemp'
        ]],
        done: function (res, curr) {
            $("[data-field='platformType']").children().each(function () {
                if ($(this).text() == LARGE_TASK_TYPE_ACCOUNT_PLATFORM) {
                    $(this).text("账号平台")
                } else if ($(this).text() == LARGE_TASK_TYPE_CRAWL_PLATFORM) {
                    $(this).text("可爬取平台")
                }
            });
            res.code = 0;
            for (var i = 0; i < res.list.length; i++) {
                var select = '_taskType' + res.list[i].id;
                formSelects.data(select, 'local', {
                    direction: 'down',
                    arr: res.list[i].taskTypeNameArray
                });
                if (res.list[i].taskTypeName != null && res.list[i].taskTypeName != '') {
                    var assistAuditArry = res.list[i].taskTypeName.split(",");
                    formSelects.value(select, assistAuditArry);
                } else {
                    // formSelects.value(select, list[i].taskTypeNameStringArray);
                }
            }
        }
    });
}

// 选择框回填数据
function formSelectsData(data) {
    var crawlPlatformSelect = data.crawlPlatformSelect;
    formSelects.data('crawlPlatform', 'local', {
        direction: 'down',
        arr: crawlPlatformSelect
    });
    if (data.crawlPlatform != null) {
        var assistAuditArry = data.crawlPlatform.split(",");
        formSelects.value('crawlPlatform', assistAuditArry);
    }
}

// 删除机器
function delMachine(obj, id) {
    if (null != id) {
        layer.confirm('您确定要删除' + obj.ip + '的IP吗？', {
            btn: ['确认', '返回'] //按钮
        }, function () {
            $.post("/deleteMachineById", {"id": id}, function (data) {
                if (data.code == SUCCESS_CODE) {
                    layer.msg(data.msg, {shift: -1, time: 600}, function () {
                        layer.closeAll();
                        load(obj);
                    });
                } else {
                    layer.msg(data.msg, {icon: 2});
                }
            });
        }, function () {
            layer.closeAll();
        });
    } else {
        layer.msg("数据异常，id为空", {icon: 2});
    }
}


layui.use([ 'form', 'upload'], function () {
    upload = layui.upload;
    upload.render({
        elem: '#refreshValidMachine' //绑定元素
        , url: '/refreshValidMachine'//上传接口
        , method: 'POST'
        , type: "file"
        , exts: 'xlsx|xls' //允许上传的文件后缀
        , size: 1024 * 1024 * 10 //最大允许上传的文件大小
        , accept: 'file'
        , auto: true//是否选完文件后自动上传。
        , bindAction: '#submit'//指向一个按钮触发上传，一般配合 auto: false 来使用。
        , field: 'content'//设定文件域的字段名
        , before: function (obj) { //obj参数包含的信息，跟 choose回调完全一致
            // layer.load(); //上传loading
        }
        , choose: function (obj) {//选择文件后的回调函数。返回一个object参数
            console.log(obj);
            //将每次选择的文件追加到文件队列
            var files = obj.pushFile();

            //预读本地文件，如果是多文件，则会遍历。(不支持ie8/9)
            obj.preview(function (index, file, result) {
                console.log(index); //得到文件索引
                console.log(file); //得到文件对象
                console.log(result); //得到文件base64编码，比如图片
                var tr = $(['<tr id="upload-' + index + '">', '<td>' + file.name + '</td>', '<td>' + (file.size / 1014).toFixed(1) + 'kb</td>', '<td>等待上传</td>', '<td>', '<button class="layui-btn layui-btn-mini demo-reload layui-hide">重传</button>', '<button class="layui-btn layui-btn-mini layui-btn-danger demo-delete">删除</button>', '</td>', '</tr>'].join(''));

                //单个重传
                tr.find('.demo-reload').on('click', function () {
                    obj.upload(index, file);
                });

                //删除
                tr.find('.demo-delete').on('click', function () {
                    delete files[index]; //删除对应的文件
                    tr.remove();
                    uploadListIns.config.elem.next()[0].value = ''; //清空 input file 值，以免删除后出现同名文件不可选
                    app.showTable = false;
                });
            });
        }
        , done: function (res) {//上传完毕回调
            //执行上传请求后的回调。返回三个参数，分别为：res（服务端响应信息）、index（当前文件的索引）、upload（重新上传的方法，一般在文件上传失败后使用）
            if(res.code == SUCCESS_CODE){
                layer.msg(res.msg, {shift: -1, time: 600}, function () {
                    layer.closeAll();
                });
            }else {
                layer.msg(data.msg, {icon: 2});
            }
        }

    });
});


layui.use([ 'form', 'upload'], function () {
    upload = layui.upload;
    upload.render({
        elem: '#clientCheckScreen' //绑定元素
        , url: '/clientCheckScreen'//上传接口
        , method: 'POST'
        , type: "file"
        , exts: 'xlsx|xls' //允许上传的文件后缀
        , size: 1024 * 1024 * 10 //最大允许上传的文件大小
        , accept: 'file'
        , auto: true//是否选完文件后自动上传。
        , bindAction: '#submit'//指向一个按钮触发上传，一般配合 auto: false 来使用。
        , field: 'content'//设定文件域的字段名
        , before: function (obj) { //obj参数包含的信息，跟 choose回调完全一致
            // layer.load(); //上传loading
        }
        , done: function (res) {//上传完毕回调
            //执行上传请求后的回调。返回三个参数，分别为：res（服务端响应信息）、index（当前文件的索引）、upload（重新上传的方法，一般在文件上传失败后使用）
            // console.log(res);
            if(res.code == SUCCESS_CODE){
                layer.msg(res.msg, {shift: -1, time: 600}, function () {
                        layer.closeAll();
                });
            }else {
                layer.msg(data.msg, {icon: 2});
            }

            //
        }
    });
});

layui.use([ 'form', 'upload'], function () {
    upload = layui.upload;
    upload.render({
        elem: '#offInValidMachine' //绑定元素
        , url: '/offInValidMachine'//上传接口
        , method: 'POST'
        , type: "file"
        , exts: 'xlsx|xls' //允许上传的文件后缀
        , size: 1024 * 1024 * 10 //最大允许上传的文件大小
        , accept: 'file'
        , auto: true//是否选完文件后自动上传。
        , bindAction: '#submit'//指向一个按钮触发上传，一般配合 auto: false 来使用。
        , field: 'content'//设定文件域的字段名
        , before: function (obj) { //obj参数包含的信息，跟 choose回调完全一致
            // layer.load(); //上传loading
        }
        , choose: function (obj) {//选择文件后的回调函数。返回一个object参数
            console.log(obj);
            //将每次选择的文件追加到文件队列
            var files = obj.pushFile();

            //预读本地文件，如果是多文件，则会遍历。(不支持ie8/9)
            obj.preview(function (index, file, result) {
                console.log(index); //得到文件索引
                console.log(file); //得到文件对象
                console.log(result); //得到文件base64编码，比如图片
                var tr = $(['<tr id="upload-' + index + '">', '<td>' + file.name + '</td>', '<td>' + (file.size / 1014).toFixed(1) + 'kb</td>', '<td>等待上传</td>', '<td>', '<button class="layui-btn layui-btn-mini demo-reload layui-hide">重传</button>', '<button class="layui-btn layui-btn-mini layui-btn-danger demo-delete">删除</button>', '</td>', '</tr>'].join(''));

                //单个重传
                tr.find('.demo-reload').on('click', function () {
                    obj.upload(index, file);
                });

                //删除
                tr.find('.demo-delete').on('click', function () {
                    delete files[index]; //删除对应的文件
                    tr.remove();
                    uploadListIns.config.elem.next()[0].value = ''; //清空 input file 值，以免删除后出现同名文件不可选
                    app.showTable = false;
                });

                // demoListView.append(tr);
                // app.showTable = true;
                // that.content = result;
                // console.log(that.content);
            });
        }
        , done: function (res) {//上传完毕回调
            //执行上传请求后的回调。返回三个参数，分别为：res（服务端响应信息）、index（当前文件的索引）、upload（重新上传的方法，一般在文件上传失败后使用）
            if(res.code == SUCCESS_CODE){
                layer.msg(res.msg, {shift: -1, time: 600}, function () {
                    layer.closeAll();
                });
            }else {
                layer.msg(data.msg, {icon: 2});
            }
        }

    });
});


layui.use([ 'form', 'upload'], function () {
    upload = layui.upload;
    upload.render({
        elem: '#uploadUserPwd' //绑定元素
        , url: '/uploadUserPwd'//上传接口
        , method: 'POST'
        , type: "file"
        , exts: 'xlsx|xls' //允许上传的文件后缀
        , size: 1024 * 1024 * 10 //最大允许上传的文件大小
        , accept: 'file'
        , auto: true//是否选完文件后自动上传。
        , bindAction: '#submit'//指向一个按钮触发上传，一般配合 auto: false 来使用。
        , field: 'content'//设定文件域的字段名
        , before: function (obj) { //obj参数包含的信息，跟 choose回调完全一致
            // layer.load(); //上传loading
        }
        , choose: function (obj) {//选择文件后的回调函数。返回一个object参数
            console.log(obj);
            //将每次选择的文件追加到文件队列
            var files = obj.pushFile();

            //预读本地文件，如果是多文件，则会遍历。(不支持ie8/9)
            obj.preview(function (index, file, result) {
                console.log(index); //得到文件索引
                console.log(file); //得到文件对象
                console.log(result); //得到文件base64编码，比如图片
                var tr = $(['<tr id="upload-' + index + '">', '<td>' + file.name + '</td>', '<td>' + (file.size / 1014).toFixed(1) + 'kb</td>', '<td>等待上传</td>', '<td>', '<button class="layui-btn layui-btn-mini demo-reload layui-hide">重传</button>', '<button class="layui-btn layui-btn-mini layui-btn-danger demo-delete">删除</button>', '</td>', '</tr>'].join(''));

                //单个重传
                tr.find('.demo-reload').on('click', function () {
                    obj.upload(index, file);
                });

                //删除
                tr.find('.demo-delete').on('click', function () {
                    delete files[index]; //删除对应的文件
                    tr.remove();
                    uploadListIns.config.elem.next()[0].value = ''; //清空 input file 值，以免删除后出现同名文件不可选
                    app.showTable = false;
                });
            });
        }
        , done: function (res) {//上传完毕回调
            //执行上传请求后的回调。返回三个参数，分别为：res（服务端响应信息）、index（当前文件的索引）、upload（重新上传的方法，一般在文件上传失败后使用）
            if(res.code == SUCCESS_CODE){
                layer.msg(res.msg, {shift: -1, time: 600}, function () {
                    layer.closeAll();
                });
            }else {
                layer.msg(data.msg, {icon: 2});
            }
        }

    });
});


//批量删除任务
$("#batchDeleteMachine").bind("click", function () {
    var checkStatus = table.checkStatus('crawlerServerList');
    var listId = [];
    //获得所有选中行的数据
    var datas = checkStatus.data;
    //进行遍历所有选中行数据，拿出每一行的id存储到数组中
    $.each(datas, function (i, data) {
        listId.push(data.id);
    });

    if (listId.length <= 0) {
        layer.msg("请选择要删除的行", {icon: 2})
    } else {
        layer.confirm('真的删除这些行吗', function (index) {
            $.ajax({
                url: "/batchDeleteMachine",
                type: "post",
                contentType: "application/json;charset=UTF-8",
                dataType: 'json',
                data: JSON.stringify({"ids": listId}),
                success: function (res) {
                    if (res.code == "SUCCESS") {
                        load(JSON.stringify({"ids": listId}));
                    }
                }
            });
            layer.close(index);
            //向服务端发送删除指令
        });
    }
});

function load(obj) {
    //重新加载table
    tableIns.reload({
        page: {
            curr: pageCurr //从当前页码开始
        }
    });
}


//刷新mini
$("#refreshMiNiMachine").bind("click", function () {
    $.ajax({
        url: "/refreshMiNiMachine",
        type: "post",
        contentType: "application/json;charset=UTF-8",
        dataType: 'json',
        // data: JSON.stringify({"ids": listId}),
        success: function (res) {
            if (res.code == "SUCCESS") {
                load();
            }
        }
    });
});


//刷新机器执行任务
$("#refreshMachineWorkTypeTask").bind("click", function () {
    $.ajax({
        url: "/refreshMachineWorkTypeTask",
        type: "post",
        contentType: "application/json;charset=UTF-8",
        dataType: 'json',
        // data: JSON.stringify({"ids": listId}),
        success: function (res) {
            if (res.code == "SUCCESS") {
                load();
            }
        }
    });
});

function cleanUser() {
    $("#machineTypeSearch").val("-1");
    $("#ipSearch").val(null);
}
