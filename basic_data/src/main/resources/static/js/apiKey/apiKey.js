/**
 * 编码管理
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
            width: 600,
            elem: '#apiKeyList',
            url:'/getCaptchaCodeApiKey',
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: false,
            request: {

            },
            response:{
                statusName: 'code', //数据状态的字段名称，默认：code
                statusCode: 200, //成功的状态码，默认：0
                countName: 'totals', //数据总数的字段名称，默认：count
                dataName: 'list' //数据列表的字段名称，默认：data
            },
            cols: [[
                {field:'id', title:'编号',align:'center',width:"20%"}
                ,{field:'apiKey', title:'编码',align:'center',width:"60%"}
                ,{title:'操作',align:'center', toolbar:'#optBar',width:"20%"}
            ]],
            done: function (res, curr, count) {
                $('.layui-table').css("width","100%");
            }
        });


        //监听工具条
        table.on('tool(apiKeyListTable)', function(obj){
            var data = obj.data;
            openBusiness(data,"update");
        });

        //监听提交
        form.on('submit(userSubmit)', function(data){
            // TODO 校验
            formSubmit(data);
            return false;
        });
    });

});

//提交表单
function formSubmit(obj){
    if(obj.field.id ==""){
        obj.field.id = 0;
    }
    $.ajax({
        type: "POST",
        data: obj.field,
        url: "/updateApiKey",
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

//打开修改页面
function openBusiness(data,title){
    if(data==null || data==""){
        $("#id").val("");
    }else{
        $("#id").val(data.id);
        $("#apiKey").val(data.apiKey);
        form.render('select'); //这个很重要
    }
    $("#idUpdate").val(data.id);
    $("#apiKeyUpdate").val(data.apiKey);
    form.render('select'); //这个很重要
    layer.open({
        type:1,
        title: '更新',
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['850px','80%'],
        content:$('#updateApiKey')
    });
}

function load(obj) {
    //重新加载table
    tableIns.reload({
    });
}
