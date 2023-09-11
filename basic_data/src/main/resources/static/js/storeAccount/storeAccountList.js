/**
 * 用户管理
 */
var pageCurr;
var form;
var table;
var timer;

$(function() {


    //下拉栏取值需要放在最前面 每次刷新就取值 放后面回显渲染会出问题
    formSelects.data('accountStatusSearch', 'server', {
        url: '/getStoreAccountStatus',
        direction: 'down',
    });

    formSelects.data('accountSiteStatusSearch', 'server', {
        url: '/getAccountSiteStatus',
        direction: 'down',
    });


    formSelects.data('accountSearch', 'server', {
        url: '/getDropDownAccount',
        direction: 'down',
    });


    layui.use('table', function(){
        table = layui.table;
        form = layui.form;

        var idList=$("#idSearch").val()
        console.log(39,idList)

        tableIns=table.render({
            elem: '#storeAccountList',
            url:'/storeAccount',
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: true,
            request: REQUEST_BODY,
            where: {status:-1,haveMachine:-1,id: idList},
            response: RESPONSE_BODY,
            cols: [[
                // {checkbox: true}
                {field:'platform', title:'平台',align:'center',width:'9%'}
                ,{field:'', title:'二次验证验证码',align:'center', width:'13%',
                    templet: function(item){
                        if(item.qrContent!=''){
                            return '<a lay-event="showQrBar" class="layui-btn layui-btn-xs" style="">二次验证验证码</a>';
                        }
                        else{
                            return '';
                        }
                    }}
                ,{field:'account', title:'店铺信息',align:'center',width:'12%',templet: function (d) {

                        var account = d.account;
                        var continents = d.continents;
                        var area = d.area;
                        var haveMachine  ;//0 初始化或账号大洲是空 1有且开启 2有没有开启 3 没有开启且有可用大类型 4没有开启无可用大类型
                        switch (d.haveMachine) {
                            case 0:
                                haveMachine='初始化或账号大洲是空'
                                break;
                            case 1:
                                haveMachine='有且开启'
                                break;
                            case 2:
                                haveMachine='有但没有开启'
                                break;
                            case 3:
                                haveMachine='没有开启且有可用大类型'
                                break;
                            case 4:
                                haveMachine='没有开启无可用大类型'
                                break;


                            default:
                                haveMachine='未知'


                        }
                        if(account == null){
                            account = ''
                        }
                        if(continents == null){
                            continents = ''
                        }
                        if(area == null){
                            area = ''
                        }
                        var result = account+'<br>'+continents+"/"+area+'<br>'+haveMachine;
                        return  result+'</span>'
                    }}
                ,{field:'username', title:'店铺账号',align:'center',width:'15%'}
                ,{field:'', title:'所在机器IP/状态',width:'18%',
                    templet: function (d) {
                        var machineTaskTypeList = d.machineWorkTypeList;
                        var result = '';
                        var i = 0;

                        $.each(machineTaskTypeList, function (n, value) {
                           // alert(value.account+value.machineIp);
                             //if (value.platformType == LARGE_TASK_TYPE_ACCOUNT_PLATFORM && value.status != STATUS_REMOVE) {
                                var machineIp;
                                var status;
                                i += 1;
                                if (i >= 4){
                                    return false;
                                }
                                if (value.machineIp == null || value.machineIp == '') {
                                    machineIp = '-'
                                } else {
                                    machineIp = value.machineIp
                                }
                                if (value.status == 1 && value.machineStatus == 1) {
                                    status = "启用"
                                } else {
                                    status = "禁用"
                                }
                                //lay-event="jumpMachineManage"
                                result += '<a class="layui-table-link" href="javascript:void(0);" onclick="jumpMachineManage(this.innerText)" >'+ machineIp+ '</a>' + '  /  ' + status + '<br>';

                        });
                        if (machineTaskTypeList.length > 3){
                            return result +'<a class="layui-table-link" href="javascript:void(0);" lay-event="allMachineIp">'+ '显示全部'+ '</a>'+ '</span>'
                        };
                        return result + '</span>'
                    }}
                ,{field:'smallType', title: '小类型',align:'center'}
                ,{field:'status', title: '店铺状态',align:'center',width:'15%'}
                ,{field:'verificationStatus', title: '二维码验证状态',align:'center',width:'10%'}
                ,{field:'', title: '代理IP/端口',align:'center',width:'15%',templet: function(d){
                        if(d.proxyIp==null || d.proxyIp==''){
                            return ""+'</span>'
                        }
                        if(d.proxyIpPort==null || d.proxyIpPort==''){
                            return d.proxyIp+'</span>'
                        }
                        return  d.proxyIp +':'+ d.proxyIpPort+'</span>'
                    }}
                ,{title:'操作',align:'center', toolbar:'#optBar',width:300}
            ]],
            done: function(res, curr){

                // 如果是异步请求数据方式，res即为你接口返回的信息。
                // 如果是直接赋值的方式，res即为：{data: [], count: 99} data为当前页数据、count为数据总长度







                $("[data-field='status']").children().each(function(){
                    if($(this).text()=='0'){
                        $(this).text("正常（未运营）")
                    }else if($(this).text()=='1'){
                        $(this).text("正常（运营中）")
                    }else if($(this).text()=='2'){
                        $(this).text("关店（不可登录）")
                    }else if($(this).text()=='3'){
                        $(this).text("关店（可登录）")
                    }else if($(this).text()=='4'){
                        $(this).text("暂停运营(假期模式)")
                    }else if($(this).text()=='-10'){
                        $(this).text("无效店铺")
                    }else if($(this).text()=='-2'){
                        $(this).text("未验证")
                    }
                });
                $("[data-field='verificationStatus']").children().each(function(){
                    if($(this).text()=='0'){
                        $(this).text("未验证")
                    }else if($(this).text()=='1'){
                        $(this).text("1")
                    }else if($(this).text()=='2'){
                        $(this).text("2")
                    }
                });

                //得到数据总量
                curr = res.pageNum;
                pageCurr=curr;
            }
        });

        //监听工具条
        table.on('tool(storeAccountTable)', function(obj){
            var data = obj.data;
            if(obj.event === 'del'){
                //删除
                delStoreAccount(data,data.id);
            } else if(obj.event === 'edit'){
                //编辑
                openStoreAccount(data,"编辑");
            }else if(obj.event === 'allMachineIp'){
                allMachineIp(data,"");
                showAllMachineIp(data);
            }  else if (obj.event === 'detail'){
                siteStatus(data.id)
                $("#platformDetail").val(data.platform);
                $("#accountDetail").val(data.account);
                $("#usernameDetail").val(data.username);
                $("#continentsDetail").val(data.continents);
                if(data.proxyIp==null || data.proxyIp==''){
                    $("#proxyIpDetail").val();
                }else {
                    $("#proxyIpDetail").val(data.proxyIp+":"+data.proxyIpPort);
                }
                layer.open({
                    type:1,
                    title: "店铺账号信息详情",
                    fixed:false,
                    resize :false,
                    shadeClose: true,
                    area: ['550px'],
                    content:$('#detailStoreAccount'),
                    end:function(){
                        cleanStoreAccount();
                    }
                });
            }else if (obj.event === 'showQrBar'){


                layer.open({
                        type:1,
                    title: "验证码窗口",
                    fixed:false,
                    resize :false,
                    shadeClose: true,
                    area: ['550px'],
                    content:$('#qrContentWindow'),
                    //content: '测试回调',

                    success:function(){

                        clearInterval(timer);

                        var usernameInQrWindow=data.username
                        var qrContent=data.qrContent;
                        $.ajax({
                           type: "POST",
                           data:{
                               qrContent : qrContent,

                           },
                           url: "/getToTpCodeByQrContent",
                           success: function (data) {
                               //清空上次打开的
                               $("#code").val();
                               $("#time").val();
                               $("#usernameInQrWindow").val();
                               $("#mailFromQrContent").val();


                               var startTimestamp = Date.parse( new Date())/1000;
                                var startTimeLeft=data.time;
                                var code=data.code;
                                var path=data.path;

                               //立刻显示一下
                               $("#code").val(code);
                               $("#time").val(startTimeLeft);
                               $("#usernameInQrWindow").val(usernameInQrWindow);
                               $("#mailFromQrContent").val(path);

                               timer = setInterval(function () {

                                   var curTimestamp = Date.parse(new Date())/1000;
                                   var timeStampInterval = curTimestamp-startTimestamp;
                                   var curTimeLeft=startTimeLeft-timeStampInterval;

                                   //小于0才发请求 等于0别发
                                   if (curTimeLeft < 0) {
                                       var Object=getTimeLeft(qrContent)
                                       startTimestamp=Date.parse(new Date())/1000;
                                       startTimeLeft=Object.time;
                                       code=Object.code;
                                   }
                                   //负的别显示了
                                   if(curTimeLeft>=0){
                                       $("#code").val(code);
                                       $("#time").val(curTimeLeft);
                                   }
                               }, 100);//不要一秒一刷新 一秒一刷新会在第一次读到剩余时间到第一次执行定时任务时有卡顿感
                           },
                        });
                    },

                    end:function(){
                        clearInterval(timer);
                    }
                });


            }else if (obj.event === 'taskTypeDetail'){
                allTaskType(data,"")
                showAccountTaskType(data);
            }
        });

        //监听提交
        form.on('submit(storeAccountSubmit)', function(data){
            // TODO 校验


            formSubmit(data);
            return false;
        });
    });

    //搜索框
    layui.use(['form','laydate'], function(){
        var form = layui.form ,layer = layui.layer;
        //TODO 数据校验
        //监听搜索框
        form.on('submit(searchSubmit)', function(data){



            //因为这个值0有含义 所以用-1代表空
            if (data.field.status=='') {
                data.field.status=-1;
            }
            console.log(349,data.field)
            //重新加载table
            tableIns.reload({
                where: data.field ,
                page: {
                    curr: pageCurr //从当前页码开始
                }
            });
            return false;
        });
    });
});

//提交表单
function formSubmit(obj){
    $.ajax({
        type: "POST",
        data: obj.field,
        url: "/updateStoreAccount",
        success: function (data) {

            //{"code":"SUCCESS","msg":"更新成功","data":1}
            if (data.code == SUCCESS_CODE) {
                layer.msg(data.msg, { anim: 0, time: 600 },function(){
                    layer.closeAll();

                    load(obj);
                });
            } else {
                layer.msg(data.msg, {icon: 2});
            }
        },
        error: function () {
            layer.msg("操作请求错误，请您稍后再试", {icon: 2},function(){
                layer.closeAll();
                //加载load方法
                load(obj);//自定义
            });
        }
    });
}

//新增店铺帐号
function addStoreAccount(){
    openStoreAccount(null,"新增");
}
function openStoreAccount(data,title){
    if(data==null || data==""){
        $("#id").val("");
    }else{
        $("#id").val(data.id);
        $("#platform").val(data.platform);
        $("#shopName").val(data.shopName);
        $("#account").val(data.account);
        $("#username").val(data.username);
        $("#password").val(data.password);
        $("#smallType").val(data.smallType);
        $("#continents").val(data.continents);
        $("#status").val(data.status);
        $("#verificationStatus").val(data.verificationStatus);
        $("#proxyIP").val(data.proxyIP);

        form.render('select'); //这个很重要
    }
    var pageNum = $(".layui-laypage-skip").find("input").val();
    $("#pageNum").val(pageNum);

    layer.open({
        type:1,
        title: title,
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['550px'],
        content:$('#setStoreAccount'),
        end:function(){
            cleanStoreAccount();
        }
    });
}
function allMachineIp(data,title){

    if(data==null || data==""){
        $("#id").val("");
    }else{
        $("#id").val(data.id);
        $("#platform").val(data.platform);
        $("#shopName").val(data.shopName);
        $("#account").val(data.account);
        $("#username").val(data.username);
        $("#password").val(data.password);
        $("#smallType").val(data.smallType);
        $("#continents").val(data.continents);
        $("#status").val(data.status);
        $("#verificationStatus").val(data.verificationStatus);
        $("#proxyIP").val(data.proxyIP);

        form.render('select'); //这个很重要
    }

    layer.open({
        type:1,
        title: title,
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['550px'],
        content:$('#allMachineIp'),
        end:function(){
            cleanStoreAccount();
        }
    });
}


function showAllMachineIp(data) {
    table.render({
        elem: '#showAllMachineIp',
        url: '/storeAccountAndMachine',
        title: '全部机器',
        where: {"account": data.account,"continents": data.continents},
        method: 'post', //默认：get请求
        cellMinWidth: 80,
        //page: true,
        request: REQUEST_BODY,
        response: RESPONSE_BODY,
        cols: [[
            {field:'machineIp', title:'所在机器IP',width:'50%',templet: function (d) {
                    var result = '';
                    result = '<a class="layui-table-link" href="javascript:void(0);" onclick="jumpMachineManage(this.innerText)" >'+ d.machineIp+ '</a>';
                    return result ;
                }},
            {field:'status', title:'所在机器状态',width:'50%',templet: function(d){
                    var result = '';
                    var i = d.status;
                    if (i == 1 && d.machineStatus == 1){
                        result = '启用'
                    }else{
                        result = '禁用'
                    }
                    return  result+'</span>';
                }}
        ]],
        done: function (res, curr) {
            // $("[data-field='platformType']").children().each(function () {
            //     if ($(this).text() == LARGE_TASK_TYPE_ACCOUNT_PLATFORM) {
            //         $(this).text("账号平台")
            //     } else if ($(this).text() == LARGE_TASK_TYPE_CRAWL_PLATFORM) {
            //         $(this).text("可爬取平台")
            //     }
            // });
            // res.code = 0;
            // for (var i = 0; i < res.list.length; i++) {
            //     var select = '_taskType' + res.list[i].id;
            //     formSelects.data(select, 'local', {
            //         direction: 'down',
            //         arr: res.list[i].taskTypeNameArray
            //     });
            //     if (res.list[i].taskTypeName != null && res.list[i].taskTypeName != '') {
            //         var assistAuditArry = res.list[i].taskTypeName.split(",");
            //         formSelects.value(select, assistAuditArry);
            //     } else {
            //         // formSelects.value(select, list[i].taskTypeNameStringArray);
            //     }
            // }
        }
    });
}
function jumpMachineManage(data) {
    window.open("../machine/machineManage?"+data);
}


function allTaskType(data,title){

    if(data==null || data==""){
        $("#id").val("");
    }else{
        $("#id").val(data.id);
        $("#platform").val(data.platform);
        $("#shopName").val(data.shopName);
        $("#account").val(data.account);
        $("#username").val(data.username);
        $("#password").val(data.password);
        $("#smallType").val(data.smallType);
        $("#continents").val(data.continents);
        $("#status").val(data.status);
        $("#verificationStatus").val(data.verificationStatus);
        $("#proxyIP").val(data.proxyIP);

        form.render('select'); //这个很重要
    }

    layer.open({
        type:1,
        title: title,
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['850px'],
        content:$('#allTaskType'),
        end:function(){
            cleanStoreAccount();
        }
    });
}
// 查询店铺执行的任务类型及数量
function showAccountTaskType(data) {
    table.render({
        elem: '#showTaskType',
        url: '/getAccountTaskType',
        title: '全部机器',
        where: {"account": data.account,"continents": data.continents},
        method: 'post', //默认：get请求
        cellMinWidth: 80,
        //page: true,
        request: REQUEST_BODY,
        response: RESPONSE_BODY,
        cols: [[
            {field:'period', title: '时段',align:'center',width:120}
            ,{field:'taskType', title: '任务类型',align:'center',width:160}
            ,{field:'taskInPool', title: '入池数',align:'center',width:90}
            ,{field:'taskGet', title: '取任务数',align:'center',width:90}
            ,{field:'taskSuccess', title: '成功数',align:'center',width:90}
            ,{field:'taskFail', title: '失败数',align:'center',width:90}
            ,{field:'createdTime', title: '创建时间',align:'center',width:150}

        ]],
        done: function (res, curr) {
        }
    });

}

function delStoreAccount(obj,id) {
    if (null != id) {
        layer.confirm('您确定要删除' + obj.account + '的帐号吗？', {
            btn: ['确认', '返回'] //按钮
        }, function () {
            $.post("/deleteStoreAccount", {"id": id}, function (data) {
                if (data.code == SUCCESS_CODE) {
                    layer.msg(data.msg, { shift: -1, time: 600 }, function () {
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
    }else {
        layer.msg("数据异常，id为空", {icon: 2});
    }
}


//批量删除任务
$("#batchDeleteStoreAccount").bind("click",function () {
    var checkStatus = table.checkStatus('storeAccountList');
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
                url: "/batchDeleteStoreAccount",
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

//刷新代理IP
$("#refreshProxyIP").bind("click",function () {
    $.ajax({
        url: "/refreshProxyIP",
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


//刷新代理IP
$("#refreshAccountSites").bind("click",function () {
    $.ajax({
        url: "/refreshAccountSites",
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

function siteStatus(id) {
    table.render({
        elem: '#siteStatus',
        url:'/storeAccountSites',
        where: {"accountId":id},
        method: 'post', //默认：get请求
        cellMinWidth: 80,
        // page: true,
        request: REQUEST_BODY,
        response: RESPONSE_BODY,
        cols: [[
            ,{field:'id', title:'ID', hide: true}
            ,{field:'accountId', title:'ID', hide: true}
            ,{field:'site', title:'站点',align:'center',width:"15%"}
            // ,{field:'statusMachine', title:'状态(机器验证)',align:'center',width:"35%"}
            ,{field:'statusMachineMean', title:'状态(机器验证)',align:'center',width:"35%"}
            ,{field:'statusPerson', title: '状态(人工验证)',align:'center',width:"35%"}
            ,{field:'status', title: '状态',align:'center',width:"15%"}
        ]],
        done: function(res, curr){
            $("[data-field='statusMachine']").children().each(function(){
                if($(this).text()=='0'){
                    $(this).text("正常（未运营）")
                }else if($(this).text()=='1'){
                    $(this).text("正常（运营中）")
                }else if($(this).text()=='2'){
                    $(this).text("关店（不可登录）")
                }else if($(this).text()=='3'){
                    $(this).text("关店（可登录）")
                }else if($(this).text()=='4'){
                    $(this).text("暂停运营(假期模式)")
                }else if($(this).text()=='-10'){
                    $(this).text("无效店铺")
                }else if($(this).text()=='-2'){
                    $(this).text("未验证")
                }
            });
            $("[data-field='statusPerson']").children().each(function(){
                if($(this).text()=='0'){
                    $(this).text("正常（未运营）")
                }else if($(this).text()=='1'){
                    $(this).text("正常（运营中）")
                }else if($(this).text()=='2'){
                    $(this).text("关店（不可登录）")
                }else if($(this).text()=='3'){
                    $(this).text("关店（可登录）")
                }else if($(this).text()=='4'){
                    $(this).text("暂停运营(假期模式)")
                }else if($(this).text()=='-10'){
                    $(this).text("无效店铺")
                }else if($(this).text()=='-2'){
                    $(this).text("未验证")
                }
            });

            $("[data-field='status']").children().each(function(){
                if($(this).text()=='0'){
                    $(this).text("不正常")
                }else if($(this).text()=='1'){
                    $(this).text("正常")
                }
            });
        }
    });
}

//刷新mini
$("#refreshMiNi").bind("click",function () {
    $.ajax({
        url: "/refreshMiNi",
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

function load(obj){
    //重新加载table
    tableIns.reload({
        page: {
            curr: pageCurr //从当前页码开始
        }
    });
}

function cleanStoreAccount(){
    $("#smallTypeSearch").val("-1");
    $("#accountSearch").val("");
}

function getTimeLeft(qrContent) {

    var code=0;
    var time=0;

    console.log("请求了一次",qrContent)
    $.ajax({
        type: "POST",
        async: false,
        data:{
            qrContent : qrContent,
        },
        url: "/getToTpCodeByQrContent",
        success: function (data) {
            code=data.code;
            time=data.time;
        },
        error: function () {

        }
    });

    return {
        code:code,
        time:time
    } ;



}
