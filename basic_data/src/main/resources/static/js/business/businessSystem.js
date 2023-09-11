
var pageCurr;
var form;
var table;
var xmSelect;
var lastData;
$(function() {
    layui.use('xmSelect', function(){
        xmSelect = layui.xmSelect;
    });

    layui.use('table', function(){
        table = layui.table;
        form = layui.form;

        tableIns=table.render({
            elem: '#businessSystemList',
            url:'/businessSystem',
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: true,
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[
                // {checkbox: true}
                {field:'status', title:'启用/禁用',align:'center',width:"10%",templet: '#statusTemp'}
                ,{field:'id', title:'系统Id', align:'center',width:"10%"}
                ,{field:'systemName', title:'系统名称',align:'center',width:"12%"}
                ,{ field: '', title: '接口调用次数已用/总共（每月）',align:'center', width:"22%",templet: function(d){
                        return  d.invokeTimesMonthUsed +'/'+ d.invokeTimesMonth+'</span>'
                }}
                ,{field:'', title:'接口调用频率限制',align:'center',width:"15%",templet: function(d){

                        return  d.invokeTimes +'/'+ d.invokeInterval+'min'+'</span>'
                    }}
                ,{field:'', title:'操作人/操作时间',align:'center',width:"18%",templet: function(d){
                        var username =d.updateUser;
                        var updateTime = d.updateTime;
                        if(username==null || username==''){
                            return  d.updateUser +'/'+updateTime+'</span>'
                        }
                        return  username +'<br>'+updateTime+'</span>'
                    }}
                ,{title:'操作',align:'center', toolbar:'#optBar',width:"20%"}
            ]],
            done: function(res, curr){
                //如果是异步请求数据方式，res即为你接口返回的信息。
                //如果是直接赋值的方式，res即为：{data: [], count: 99} data为当前页数据、count为数据总长度
                if(res.list == null){
                    taskTypeSearch();
                }
                formSelects.data('taskTypeSearch', 'local', {
                    direction: 'down',
                    arr: res.list[0].taskTypeSelect
                });

                if(lastData!=null){
                    var taskType = lastData.field.taskTypeName;
                    if(taskType!=null){
                        var assistAuditArry =taskType.split(",");
                        formSelects.value('taskTypeSearch', assistAuditArry);
                    }
                }

                //得到数据总量
                curr = res.pageNum;
                pageCurr=curr;
             }
        });

        //监听工具条
        table.on('tool(businessSystemListTable)', function(obj){
            var data = obj.data;
            if(obj.event === 'del'){
                //删除
                delBusinessSystem(data,data.id);
            } else if(obj.event === 'edit'){
                //编辑
                $("#setBusinessSystemId").show();
                editBusiness(data,"编辑");
            }else if(obj.event === 'details'){
                //查询详情
                $("#details").show();
                getDetails(data,"详情");
            }
        });

        //监听提交
        form.on('submit(userSubmit)', function(data){
            // TODO 校验

            formSubmit(data);
            return false;
        });

        form.on('switch(status)', function(data){
            var id = data.value;
            var status = this.checked ? STATUS_VALID : STATUS_INVALID;
            $.ajax({
                type: "POST",
                data: {id: id,status: status},
                url: "/updateBusinessSystem",
                success: function (data) {
                    //{"code":"SUCCESS","msg":"更新成功","data":1}
                    if (data.code == SUCCESS_CODE) {
                        layer.msg(data.msg, { shift: -1, time: 600 },function(){
                            layer.closeAll();
                            load({id: id,status: status});
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

        });
    });

    //搜索框
    layui.use(['form','laydate'], function(){
        var form = layui.form ,layer = layui.layer;
        //TODO 数据校验
        //监听搜索框
        form.on('submit(searchSubmit)', function(data){
            //重新加载table
            lastData = data;
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
    taskTypeSearch()
    if(obj.field.id == ''){
        obj.field.id=0;
    }
    $.ajax({
        type: "POST",
        data: obj.field,
        url: "/updateBusinessSystem",
        success: function (data) {

            //{"code":"SUCCESS","msg":"更新成功","data":1}
            if (data.code == SUCCESS_CODE) {
                layer.msg(data.msg, { shift: -1, time: 600 },function(){
                    layer.closeAll();
                    load(obj);
                });
            } else {
                layer.msg(data.msg, {icon: 2});
                layer.closeAll();
                load(obj);
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

function taskTypeSearch() {
    formSelects.data('taskTypeSearch', 'server', {
        url: '/getDropDownTaskId',
        keyName: 'name',
        keyVal: 'id',
        direction: 'down',          //多选下拉方向, auto|up|down
        response: {
            statusCode: 0,          //成功状态码
            statusName: 'code',     //code key
            msgName: 'msg',         //msg key
            dataName: 'data'        //data key
        },
        success: function(id, url, searchVal, result){      //使用远程方式的success回调
        }

    });
}


// 添加
function addBusinessSystem(){
    taskTypeSearch();
    $("#id").val("");
    $("#setBusinessSystemId").hide();
    layer.open({
        type:1,
        title: '业务系统添加',
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['850px','80%'],
        content:$('#setBusinessSystem'),
        end:function(){
            cleanUser();
        }
    });

}


// 编辑
function editBusiness(data,title){
    if(data==null || data==""){
        $("#id").val("");
    }else{
        $("#id").val(data.id);
        $("#systemName").val(data.systemName);
        $("#invokeTimesMonth").val(data.invokeTimesMonth);
        $("#invokeInterval").val(data.invokeInterval);
        $("#invokeTimes").val(data.invokeTimes);
        $("#callbackAddress").val(data.callbackAddress);
        $('#networkType input[name="networkType"]:checked ').val();
        $("#systemVersion").val(data.systemVersion);
        if(data.taskTypeName !=null){
            var taskTypeName =data.taskTypeName.split(",");
            formSelects.value('taskTypeSearch', taskTypeName);
        }

        form.render('select'); //这个很重要
    }
    var pageNum = $(".layui-laypage-skip").find("input").val();
    $("#pageNum").val(pageNum);

    layer.open({
        type:1,
        title: '业务系统设置',
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['850px','80%'],
        content:$('#setBusinessSystem'),
        end:function(){
            cleanUser();
        },
        success: function () {
            $("input[name=networkType][value='1']").prop("checked", false);
            $("input[name=networkType][value='2']").prop("checked", false);
            $("input[name=networkType][value='3']").prop("checked", false);
            $("input[name=networkType][value='4']").prop("checked", false);

            layui.use(['jquery', 'table', 'layer'], function () {


                formSelects.value('systems', []);
                if (data.systems != null) {
                    var Arr = data.systems.split(",");
                    formSelects.value('systems', Arr);
                }
                if (data.networkType == 1) {//按可爬取平台

                    $("input[name=networkType][value='1']").prop("checked", true);
                    $("input[name=networkType][value='2']").prop("checked", false);
                    $("input[name=networkType][value='3']").prop("checked", false);
                    $("input[name=networkType][value='4']").prop("checked", false);
                } else if (data.networkType == 2) {
                    $("input[name=networkType][value='1']").prop("checked", false);
                    $("input[name=networkType][value='2']").prop("checked", true);
                    $("input[name=networkType][value='3']").prop("checked", false);
                    $("input[name=networkType][value='4']").prop("checked", false);
                } else if (data.networkType == 3) {
                    $("input[name=networkType][value='1']").prop("checked", false);
                    $("input[name=networkType][value='2']").prop("checked", false);
                    $("input[name=networkType][value='3']").prop("checked", true);
                    $("input[name=networkType][value='4']").prop("checked", false);
                } else if (data.networkType == 4) {
                    $("input[name=networkType][value='1']").prop("checked", false);
                    $("input[name=networkType][value='2']").prop("checked", false);
                    $("input[name=networkType][value='3']").prop("checked", false);
                    $("input[name=networkType][value='4']").prop("checked", true);
                }
                form.render();

            })

        }
    });
}

// 获取详情
function getDetails(data,title){
    var systemId = data.id;
    window.open("/businessS/businessSystemTask?systemId="+systemId);
}


function delBusinessSystem(obj,id) {
    if (null != id) {
        layer.confirm('您确定要删除' + obj.ip + '的IP吗？', {
            btn: ['确认', '返回'] //按钮
        }, function () {
            $.post("/deleteBusinessSystemById", {"id": id}, function (data) {
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


function load(obj) {
    //重新加载table
    tableIns.reload({
        page: {
            curr: pageCurr //从当前页码开始
        }
    });
}

function cleanUser(){
    $("#id").val("");
    $("#systemName").val("");
    $("#invokeTimesMonth").val("");
    $("#invokeInterval").val("");
    $("#invokeTimes").val("");
    $("#taskTypeName").val("");
    $("#callbackAddress").val("");
    $("#networkType").val("");
    $("#systemVersion").val("");
    $("#updateUser").val("");
}
