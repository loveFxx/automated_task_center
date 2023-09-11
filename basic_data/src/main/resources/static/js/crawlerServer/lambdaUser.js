/**
 * 用户管理
 */
var pageCurr;
var form;
var table;
var lastData;
var url;
$(function () {
    layui.use('table', function () {
        table = layui.table;
        form = layui.form;
        // xmSelect = layui.xmSelect;
        tableIns = table.render({
            elem: '#lambdaUserList',
            url: '/getLambdaUserList',
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: true,
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[
                {field: 'id', title: 'ID', hide: true},
                {field: 'accountName', title: '用户名', align: 'center',width:"15%"}
                , {field: '', title: 'access_key/accessSecret', align: 'center', width: "30%", templet: function (d) {
                        return d.accessKey + '<br>' + d.accessSecret.substring(0,5)+'*****' + '</span>'
                    }}
                , {field: '', title: 'lambda函数/region', align: 'center', width: "25%", templet: function (d) {
                        return d.functionName + '<br>' + d.region + '</span>'
                    }}
                , {field: 'createdAt', title: '创建时间', align: 'center', width: "20%", templet: function (d) {
                        return d.createdAt  + '</span>'
                    }}
                 , {title: '操作', align: 'center', toolbar: '#optBar', width: "10%"}
            ]],
            done: function (res, curr) {
                //得到数据总量
                curr = res.pageNum;
                pageCurr = curr;
            }
        });

        //监听工具条
        table.on('tool(lambdaUserTable)', function (obj) {
            var data = obj.data;
            if (obj.event === 'del') {
                //删除
                delLambdaUser(data, data.id);
            } else if (obj.event === 'edit') {
                //编辑
                editLambdaUser(data, "编辑");
                //formSelectsData(data);
            }

        });

        //监听添加用户提交
        form.on('submit(userSubmit)', function (data) {
            // TODO 校验
            formSubmit(data);
            return false;
        });
        //监听修改用户提交
        form.on('submit(updateUserSubmit)', function (data) {
            // TODO 校验
            formUpdateSubmit(data);
            return false;
        });
        //监听添加函数提交
        form.on('submit(functionSubmit)', function (data) {
            // TODO 校验
            functionSubmit(data);
            return false;
        });
        //监听添加关联关系提交
        form.on('submit(relationSubmit)', function (data) {
            // TODO 校验
            relationSubmit(data);
            return false;
        });
    });
    loadSelects();

});



//提交表单
function formSubmit(dataInfo) {
    var lambdaUser = {};
    if(dataInfo.field.accountName!=''){
        lambdaUser.accountName=dataInfo.field.accountName
    }
    if(dataInfo.field.accessKey!=''){
        lambdaUser.accessKey=dataInfo.field.accessKey
    }
    if(dataInfo.field.accessSecret!=''){
        lambdaUser.accessSecret=dataInfo.field.accessSecret
    }
    if(dataInfo.field.regionSearch!=''){
        lambdaUser.region=dataInfo.field.regionSearch
    }
    if(dataInfo.field.lambdaFunction!=''){
        lambdaUser.lambdaFunction=dataInfo.field.lambdaFunction
    }

    if(dataInfo.field.accountName ==''){
        lambdaUser.accountName=null
    }
    if(dataInfo.field.accessKey ==''){
        lambdaUser.accessKey=null
    }
    if(dataInfo.field.accessSecret ==''){
        lambdaUser.accessSecret=null
    }
    if(dataInfo.field.regionSearch ==''){
        lambdaUser.region=null
    }
    if(dataInfo.field.lambdaFunction ==''){
        lambdaUser.lambdaFunction=null
    }


    $.ajax({
        type: "POST",
        data: lambdaUser,
        url: "/addLambdaUser",
        success: function (data) {
            $("form")[0].reset();
            layer.msg(data.msg, {shift: -1, time: 800});
            layer.closeAll();
            //cleanUser();
            load();
            loadCache();
        },
        error: function () {
            $("form")[0].reset();
            layer.msg("操作错误", {icon: 2}, function () {
                //layer.closeAll();
                //加载load方法
                load();//自定义
            });
        }
    });
}

function formUpdateSubmit(updateData) {

    $.ajax({
        type: "POST",
        data: updateData.field,
        url: "/updateLambdaUser",
        success: function (data) {
            layer.msg(data.msg,{ shift: -1, time: 1000 });
            //{"code":"SUCCESS","msg":"更新成功","data":1}
            setTimeout(function () {
                layer.closeAll();
                //divSuccess.style.display="none";
            }, 1000);
            load();
        },
        error: function () {
            layer.msg("操作请求错误，请您稍后再试", {icon: 2}, function () {
                layer.closeAll();
                //加载load方法
                load();//自定义
            });
        }
    });
}

//机器用户
function addLambda() {
    loadSelects();
    $("#id").val('');
    form.render('select'); //这个很重要
    layer.open({
        type: 1,
        title: 'lambda用户添加',
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['850px', '80%'],
        content: $('#addLambdaUser'),
        end: function () {
            //cleanUser();
        }

    });
}

//修改
function editLambdaUser(data, title) {
    if (data == null || data == "") {
        $("#id").val('');
    } else {
        $("#id").val(data.id);lambdaAccountId
        $("#lambdaAccountId").val(data.lambdaAccountId);
        $("#accessKeyUpdate").val(data.accessKey);
        $("#accessSecretUpdate").val(data.accessSecret);
        if (data.functionName != null) {
            var Arr = data.functionName.split(",");
            formSelects.value('lambdaFunction', Arr);
        }
        if (data.region != null) {
            var Arr = data.region.split(",");
            formSelects.value('regionSearch', Arr);
        }
        form.render('select'); //这个很重要

    }
    var pageNum = $(".layui-laypage-skip").find("input").val();
    $("#pageNum").val(pageNum);

    //form.render('select'); //这个很重要
    layer.open({
        type: 1,
        title: '修改用户：'+data.accountName,
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['850px', '80%'],
        content: $('#setLambdaUser'),
        end: function () {
            //cleanUser();
        }
    });
}


// 删除机器
function delLambdaUser(obj, id) {
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



function load() {
    //重新加载table
    tableIns.reload({
        page: {
            curr: pageCurr //从当前页码开始
        }
    });
}

// function cleanUser() {
//
//     formSelects.value('lambdaFunction', []);
//     formSelects.value('regionSearch', []);
//     form.render('select');  // 重新渲染
// }


function loadSelects() {
    //添加页面 选择框
    formSelects.data('lambdaUserName', 'server', {
        url: '/getLambdaUserName',
        direction: 'down',
    });
    //添加页面 选择框
    formSelects.data('regionSearch', 'server', {
        url: '/getRegionId',
        direction: 'down',
    });

    //添加页面 选择框
    formSelects.data('lambdaFunction', 'server', {
        url: '/getFunctionName',
        direction: 'down',
    });

    //修改页面选择框
    formSelects.data('regionSearch1', 'server', {
        url: '/getRegionId',
        direction: 'down',
    });
    //修改页面 选择框
    formSelects.data('lambdaFunction1', 'server', {
        url: '/getFunctionName',
        direction: 'down',
    });
}
//添加函数
function addFunction(){
    layer.open({
        type: 1,
        title: '添加Function',
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['850px', '80%'],
        content: $('#addFunction'),
        end: function () {
            cleanFunction();
        }
    });
}

function addUserAndRegionFunction(){
    loadSelects();
    layer.open({
        type: 1,
        title: '添加关联',
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['850px', '80%'],
        content: $('#addUserAndRegionFunction'),
        end:function () {

        }

    });

}

function functionSubmit(data) {
    var awsFunction = {};
    awsFunction.functionName=data.field.lfName;
    awsFunction.processNum=data.field.processNum;
    $.ajax({
        type: "POST",
        data: awsFunction,
        url: "/addFunction",
        success: function (data) {
            //{"code":"SUCCESS","msg":"更新成功","data":1}
            layer.msg(data.msg, {shift: -1, time: 600}, function () {
                layer.closeAll();
            });
            loadCache();



        },
        error: function () {
            layer.msg("操作请求错误，请您稍后再试", {icon: 2}, function () {

                //加载load方法

            });
        }
    });
}

function relationSubmit(data) {
    var lambdaUser1 = {};
    if(data.field.lambdaUserName!=''){
        lambdaUser1.accountName=data.field.lambdaUserName
    }
    if(data.field.userAndRegion!=''){
        lambdaUser1.region=data.field.userAndRegion
    }
    if(data.field.userAndFunctionName!=''){
        lambdaUser1.lambdaFunction=data.field.userAndFunctionName
    }

    if(data.field.lambdaUserName ==''){
        lambdaUser1.accountName=null
    }
    if(data.field.userAndRegion ==''){
        lambdaUser1.region=null
    }
    if(data.field.userAndFunctionName ==''){
        lambdaUser1.lambdaFunction=null
    }
    $.ajax({
        type: "POST",
        data: lambdaUser1,
        url: "/addLambdaUserRelation",
        success: function (data) {
            $("form")[0].reset();
            layer.msg(data.msg, {shift: -1, time: 1000});
            setTimeout(function () {
                layer.closeAll();
                divSuccess.style.display="none";
            }, 1000);
            //cleanUser();
            load();
        },
        error: function () {
            $("form")[0].reset();
            layer.msg("操作错误", {icon: 2}, function () {
                //layer.closeAll();
                //加载load方法
                load();//自定义
            });
        }
    });

}

function loadCache() {
    $.ajax({
        type: "POST",
        data: "",
        url: "/initCache",
        success: function (data) {
        }
    });
}

function cleanFunction(){
    $("#lfName").val("");
    $("#processNum").val("");
};

//选择框回填数据
// function formSelectsData(data) {
//     var regionName = data.region;
//     formSelects.data('region', 'local', {
//         direction: 'down',
//         arr: regionName
//     });
//     if(data.region != null){
//         var assistAuditArry =data.region.split(",");
//         formSelects.value('region',assistAuditArry);
//     }
//
// }
