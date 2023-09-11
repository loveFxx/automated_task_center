/**
 * 平台管理
 */

var form;
var table;
var element;
var xmSelect;
$(function() {
    layui.use(['table','element'], function(){
        table = layui.table;
        form = layui.form;
        element = layui.element;

        tableIns=table.render({
            elem: '#platformList',
            url:'/getPlatformList',
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: true,
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[
                {field:'id', title:'编号',hide: true}
                ,{field:'platformName', title:'平台名称',align:'center',width:"15%"}
                ,{field:'platformNameZh', title:'平台中文名称',align:'center',width:"15%"}
                ,{field:'isBrowser', title:'是否开启浏览器',align:'center',width:"15%",templet: function (d) {
                        if (d.isBrowser == 1) {
                            return '开启'
                        } else if (d.isBrowser == 0) {
                            return '不开启'
                        }
                    }}
                ,{field:'createdAt', title:'创建时间',align:'center',width:"20%"}
                ,{field:'updatedAt', title:'更新时间',align:'center',width:"20%"}
                ,{title:'操作',align:'center', toolbar:'#optBar',width:"15%"}
            ]],
            done: function (res, curr, count) {
                $('.layui-table').css("width","100%");
            }
        });

        //监听工具条
        table.on('tool(platformListTable)', function(obj){
            var data = obj.data;
            if(obj.event === 'delete'){
                //删除
                delPlatform(data,data.id);
            }else if(obj.event === 'update'){
                updatePlatform(data,"update");
            }
        });


        //监听提交
        form.on('submit(userAddSubmit)', function(data){
            // TODO 校验
            addFormSubmit(data);
            return false;
        });

        //监听提交
        form.on('submit(userUpdateSubmit)', function(data){
            // TODO 校验
            updateFormSubmit(data);
            return false;
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
                where: data.field
            });
            return false;
        });
    });
});

//提交添加表单
function addFormSubmit(obj){
    if(obj.field.id ==""){
        obj.field.id = 0;
    }
    $.ajax({
        type: "POST",
        data: obj.field,
        url: "/addPlatform",
        success: function (data) {
            //{"code":"SUCCESS","msg":"更新成功","data":1}
            if (data.code == SUCCESS_CODE) {
                layer.msg(data.msg, { shift: -1, time: 600 },function(){
                    layer.closeAll();
                    load(obj);
                });
            } else {
                layer.msg(data.msg, {icon: 2, time: 600 },function(){
                    layer.closeAll();
                    load(obj);
                });
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


//提交修改表单
function updateFormSubmit(obj){
    if(obj.field.id ==""){
        obj.field.id = 0;
    }
    obj.field['isBrowser'] = obj.field.isBrowserUpdate;
    $.ajax({
        type: "POST",
        data: obj.field,
        url: "/updatePlatform",
        success: function (data) {
            //{"code":"SUCCESS","msg":"更新成功","data":1}
            if (data.code == SUCCESS_CODE) {
                layer.msg(data.msg, { shift: -1, time: 600 },function(){
                    layer.closeAll();
                    load(obj);
                });
            } else {
                layer.msg(data.msg, {icon: 2, time: 600 },function(){
                    layer.closeAll();
                    load(obj);
                });
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

function addPlatform(){
    layer.open({
        type:1,
        title: '添加',
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['80%', '80%'],
        content:$('#addPlatform')
    });
}

function updatePlatform(data,title){
    $("#idUpdate").val(data.id);
    $("#platformNameUpdate").val(data.platformName);
    $("#platformNameZhUpdate").val(data.platformNameZh);
    $("input[name=isBrowserUpdate][value='0']").prop("checked", false);
    $("input[name=isBrowserUpdate][value='1']").prop("checked", false);
    if (data.isBrowser == 0) {
        $("input[name=isBrowserUpdate][value='0']").prop("checked", true);
    } else if (data.isBrowser == 1) {
        $("input[name=isBrowserUpdate][value='1']").prop("checked", true);
    }
    form.render();
    layer.open({
        type:1,
        title: '更新',
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['80%', '80%'],
        content:$('#updatePlatform'),
        end:function(){
        },success:function () {

        }
    });

}

function delPlatform(obj,id) {
    if (null != id) {
        layer.confirm('您确定要删除' + obj.platformNameZh + '平台吗？', {
            btn: ['确认', '返回'] //按钮
        }, function () {
            $.post("/deletePlatformById", {"id": id,"platform": obj.platform}, function (data) {
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
    });
}

function addComboColumn(){
    var divNode = $("#column-box");
    var html = "<div id=\"box-list\">在<div class=\"layui-input-inline\" style=\"width:50px\"><input name=\"intervalTime\" autocomplete=\"off\" class=\"layui-input \"/></div><div class=\"layui-input-inline\"><select name=\"intervalType\"><option value=\"1\">分</option><option value=\"2\">时</option><option value=\"3\">天</option></select></div>内达到<div class=\"layui-input-inline\" style=\"width:50px\"><input name=\"maxBannedTimes\" autocomplete=\"off\" class=\"layui-input \"/></div>次上限延迟<div class=\"layui-input-inline\" style=\"width:50px\"><input name=\"delayTime\" autocomplete=\"off\" class=\"layui-input \"/></div><div class=\"layui-input-inline\"><select name=\"delayType\"><option value=\"1\">分</option><option value=\"2\">时</option></select></div></div>"
    divNode.append(html);
}

function deleteComboColumn(){
    var divNode = $("#column-box");
    divNode.children().last().remove();
}
