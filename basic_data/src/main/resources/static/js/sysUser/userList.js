/**
 * 用户管理
 */
var pageCurr;
var form;
var xmSelect;
// var formSelects = layui.formSelects;
$(function() {
    layui.use('xmSelect', function(){
        xmSelect = layui.xmSelect;
    });
});
// formSelects.render('permissions');
$(function() {
    layui.use('table', function(){
        var table = layui.table;
        form = layui.form;

        tableIns=table.render({
            elem: '#uesrList',
            url:'/user/getUserList',
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: true,
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[
                {type:'numbers'}
                ,{field:'sysUserName', title:'用户名',align:'center'}
                // ,{field:'roleName', title:'角色类型',align:'center'}
                ,{field:'permissions', title:'权限',align:'center'}
                ,{field:'userPhone', title:'手机号',align:'center'}
                ,{field:'regTime', title: '注册时间',align:'center'}
                ,{field:'userStatus', title: '是否有效',align:'center'}
                ,{title:'操作',align:'center', toolbar:'#optBar'}
            ]],
            done: function(res, curr, count){
                //如果是异步请求数据方式，res即为你接口返回的信息。
                //如果是直接赋值的方式，res即为：{data: [], count: 99} data为当前页数据、count为数据总长度
                //console.log(res);
                //得到当前页码
                console.log(curr);
                $("[data-field='userStatus']").children().each(function(){
                    if($(this).text()=='1'){
                        $(this).text("有效")
                    }else if($(this).text()=='0'){
                        $(this).text("失效")
                    }
                });
                //得到数据总量
                //console.log(count);
                pageCurr=curr;
            }
        });

        //监听工具条
        table.on('tool(userTable)', function(obj){
            var data = obj.data;
            if(obj.event === 'del'){
                //删除
                delUser(data,data.id,data.sysUserName);
            } else if(obj.event === 'edit'){
                //编辑
                openUser(data,"编辑");
            }else if(obj.event === 'recover'){
                //恢复
                recoverUser(data,data.id);
            }
        });

        //监听提交
        form.on('submit(userSubmit)', function(data){
            // TODO 校验
            formSubmit(data);
            return false;
        });
    });

    //搜索框
    layui.use(['form','laydate'], function(){
        var form = layui.form ,layer = layui.layer
            ,laydate = layui.laydate;
        //日期
        laydate.render({
            elem: '#startTime'
        });
        laydate.render({
            elem: '#endTime'
        });
        //TODO 数据校验
        //监听搜索框
        form.on('submit(searchSubmit)', function(data){
            //重新加载table
            load(data);
            return false;
        });
    });
});

//提交表单
function formSubmit(obj){
    $.ajax({
        type: "POST",
        data: $("#userForm").serialize(),
        url: "/user/setUser",
        success: function (data) {
            if (data.code == SUCCESS_CODE) {
                layer.msg(data.msg, {shift: -1, time: 600}, function () {
                    layer.closeAll();
                    load({id: id, status: status});
                });
            } else {
                layer.alert(data.msg);
            }
        },
        error: function () {
            layer.alert("操作请求错误，请您稍后再试",function(){
                layer.closeAll();
                //加载load方法
                load(obj);//自定义
            });
        }
    });
}

//开通用户
function addUser(){
    openUser(null,"开通用户");
}
function openUser(data,title){
    var permissions = null;
    if(data==null || data==""){
        $("#id").val("");
    }else{
        $("#id").val(data.id);
        $("#username").val(data.sysUserName);
        $("#mobile").val(data.userPhone);
        permissions = data.permissionIds;
    }
    // alert(JSON.stringify(data))
    // alert(permissions)

    formSelects.data('permissions', 'server', {
        url: '/permission/parentPermissionList',
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
            console.log(permissions)
            // alert(result.data)
            if(permissions != null){
                var assistAuditArry =permissions.split(",");
                alert(assistAuditArry.toString());

                formSelects.value('permissions', assistAuditArry);
            }

            console.log(result);    //返回的结果
        },
        error: function(id, url, searchVal, err){           //使用远程方式的error回调
            //同上
            console.log(err);   //err对象
        },
        linkage: true
    });

    // layui.formSelects.data('permissions', 'local', {
    //     arr: [
    //         {
    //             "name": "北京",
    //             "value": 1,
    //             "children": [
    //                 {"name": "北京市1", "value": 12, "children": [
    //                         {"name": "朝阳区1", "value": 13, "children": []},
    //                         {"name": "朝阳区2", "value": 14, "children": []},
    //                         {"name": "朝阳区3", "value": 15, "children": []},
    //                         {"name": "朝阳区4", "value": 16, "children": []},
    //                     ]},
    //                 {"name": "北京市2", "value": 17, "children": []},
    //                 {"name": "北京市3", "value": 18, "children": []},
    //                 {"name": "北京市4", "value": 19, "children": []},
    //             ]
    //         },
    //         {
    //             "name": "天津",
    //             "value": 2,
    //             "children": [
    //                 {"name": "天津市1", "value": 51, "children": []},
    //             ]
    //         },
    //     ],
    //     linkage: true
    // });


    var pageNum = $(".layui-laypage-skip").find("input").val();
    $("#pageNum").val(pageNum);
    $.ajax({
        url:'/role/getRoles',
        dataType:'json',
        async: true,
        success:function(data){
            $.each(data,function(index,item){
                if(!roleId){
                    var option = new Option(item.roleName,item.id);
                }else {
                    var option = new Option(item.roleName,item.id);
                    // // 如果是之前的parentId则设置选中
                    if(item.id == roleId) {
                        option.setAttribute("selected",'true');
                    }
                }
                $('#roleId').append(option);//往下拉菜单里添加元素
                form.render('select'); //这个很重要
            })
        }
    });

    layer.open({
        type:1,
        title: title,
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['550px','80%'],
        content:$('#setUser'),
        end:function(){
            cleanUser();
        }
    });
}

function delUser(obj,id,name) {
    var currentUser=$("#currentUser").html();
    if(null!=id){
        if(currentUser==id){
            layer.alert("对不起，您不能执行删除自己的操作！");
        }else{
            layer.confirm('您确定要删除'+name+'用户吗？', {
                btn: ['确认','返回'] //按钮
            }, function(){
                $.post("/user/updateUserStatus",{"id":id,"status":0},function(data){
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
}
//恢复
function recoverUser(obj,id) {
    if(null!=id){
        layer.confirm('您确定要恢复吗？', {
            btn: ['确认','返回'] //按钮
        }, function(){
            $.post("/user/updateUserStatus",{"id":id,"status":1},function(data){
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

function load(obj){
    //重新加载table
    tableIns.reload({
        where: obj.field
        , page: {
            curr: pageCurr //从当前页码开始
        }
    });
}

function cleanUser(){
    $("#username").val("");
    $("#mobile").val("");
    $("#password").val("");
    $('#permissions').html("");
}
