/**
 * 角色管理
 */
//'formSelects.render('permissions');
var pageCurr;
var form;
var xmSelect;
// var formSelects = layui.formSelects;
$(function() {
    layui.use('xmSelect', function(){
        xmSelect = layui.xmSelect;
    });
});

// formSelects.render('roleDesc');

$(function() {

    layui.use('table', function(){
        var table = layui.table;
        form = layui.form;
        // xmSelect = layui.xmSelect;

        tableIns=table.render({
            elem: '#roleList',
            url:'/role/getRoleList',
            method: 'get', //默认：get请求
            cellMinWidth: 80,
            page: true,
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[
                {type:'numbers'}
                ,{field:'roleName', title:'角色名称',align:'center'}
                ,{field:'roleDesc', title:'角色描述',align:'center'}
                ,{field:'permissions', title:'权限',align:'center'}
                ,{field:'createTime', title:'创建时间',align:'center'}
                ,{field:'updateTime', title:'更新时间',align:'center'}
                ,{field:'roleStatus', title:'是否有效',align:'center'}
                ,{fixed:'right',title:'操作',align:'center', toolbar:'#optBar'}
            ]],
            done: function(res, curr, count){
                $("[data-field='roleStatus']").children().each(function(){
                    if($(this).text()=='1'){
                        $(this).text("有效")
                    }else if($(this).text()=='0'){
                        $(this).text("失效")
                    }
                });
                pageCurr=curr;

            }
        });


        //监听工具条
        table.on('tool(roleTable)', function(obj){
            var data = obj.data;
            if(obj.event === 'del'){
                //删除
                delRole(data,data.id);
            } else if(obj.event === 'edit'){
                //编辑
                edit(data);
            }else if(obj.event === 'recover'){
                //恢复
                recoverRole(data,data.id);
            }
        });

        //监听提交
        form.on('submit(roleSubmit)', function(data){
            formSubmit(data);
            return false;
        });

    });

});

//提交表单
function formSubmit(obj){
    $.ajax({
        type: "post",
        data: $("#roleForm").serialize(),
        url: "/role/setRole",
        success: function (data) {
            if (data.code == SUCCESS_CODE) {
                layer.alert(data.msg,function(){
                    layer.closeAll();
                    load(obj);
                });
            } else {
                layer.alert(data.msg);
            }
        },
        error: function () {
            layer.alert("操作请求错误，请您稍后再试",function(){
                layer.closeAll();
                load(obj);
            });
        }
    });
}

//新增
function add() {
    edit(null,"新增");
}
//打开编辑框
function edit(data,title){
    var pid = null;
    if(data == null){
        $("#id").val("");
    }else{
        //回显数据
        $("#id").val(data.id);
        $("#roleName").val(data.roleName);
        $("#roleDesc").val(data.roleDesc);
        pid = data.permissionIds;
    }

    // var permissions = xmSelect.render({
    //     el: '#permissions',
    //     autoRow: true,
    //     data: []
    // });

    formSelects.data('permissions', 'server', {
        url: '/permission/parentPermissionList',
        keyName: 'name',
        keyVal: 'id',
        direction: 'auto',          //多选下拉方向, auto|up|down
        response: {
            statusCode: 0,          //成功状态码
            statusName: 'code',     //code key
            msgName: 'msg',         //msg key
            dataName: 'data'        //data key
        },
        success: function(id, url, searchVal, result){      //使用远程方式的success回调

            console.log(pid)
            if(pid != null){
                var assistAuditArry =pid.split(",");
                formSelects.value('permissions', assistAuditArry);
                // var permissions = xmSelect.render({
                //     el: '#permissions',
                //     data: assistAuditArry
                // })
                // permissions.update({
                //     data: assistAuditArry,
                //     filterable: true,
                // })
            }

            console.log(result);    //返回的结果
        },
        error: function(id, url, searchVal, err){           //使用远程方式的error回调
                                                            //同上
            console.log(err);   //err对象
        },
    });


    var pageNum = $(".layui-laypage-skip").find("input").val();
    $("#pageNum").val(pageNum);

    layer.open({
        type:1,
        title: title,
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['550px','550px'],
        content:$('#setRole'),
        end:function(){
            cleanRole();
        }
    });
}

//重新加载table
function load(obj){
    tableIns.reload({
        where: obj.f
        , page: {
            curr: pageCurr //从当前页码开始
        }
    });
}

//删除
function delRole(obj,id) {
    if(null!=id){
        layer.confirm('您确定要删除吗？', {
            btn: ['确认','返回'] //按钮
        }, function(){
            $.post("/role/updateRoleStatus",{"id":id,"status":0},function(data){
                if (data.code == SUCCESS_CODE) {
                    layer.alert(data.msg,function(){
                        layer.closeAll();
                        load(obj);
                    });
                } else {
                    layer.alert(data.msg);
                }
            });
        }, function(){
            layer.closeAll();
        });
    }
}
//恢复
function recoverRole(obj,id) {
    if(null!=id){
        layer.confirm('您确定要恢复吗？', {
            btn: ['确认','返回'] //按钮
        }, function(){
            $.post("/role/updateRoleStatus",{"id":id,"status":1},function(data){
                if (data.code == SUCCESS_CODE) {
                    layer.alert(data.msg,function(){
                        layer.closeAll();
                        load(obj);
                    });
                } else {
                    layer.alert(data.msg);
                }
            });
        }, function(){
            layer.closeAll();
        });
    }
}


function cleanRole() {
    $("#roleName").val("");
    $("#roleDesc").val("");
    $("#permissions").val("");
}

