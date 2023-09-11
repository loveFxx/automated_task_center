/**
 * 任务库列表管理
 */
var pageCurr;
var form;
var table;
var SystemJson;
var typeJson={"1":"周期","2":"单次","3":"周期性单次"}
var isEnforcedJson={"0":"否","1":"是"}
$(function() {


    layui.use('laydate', function(){
        var laydate = layui.laydate;
        laydate.render({
            elem: '#expectedTime1'
            ,type: 'date'
            ,trigger: 'click'//为了防止一个页面用两次laydate闪退

        });
        laydate.render({
            elem: '#createdTime'
            ,type: 'date'
            ,trigger: 'click'//为了防止一个页面用两次laydate闪退
        });
    });

    $.ajax({//获取系统的的json数组然后转换成{systemId:systemName}的json 为了在表格中显示正确systemName

        type: "POST",
        url: '/getDropDownSystem',
        success: function (data) {
            SystemJson = {};
            let SystemJsonArr=data;
            for (let i = 0; i < SystemJsonArr.length; i++) {
                let key=SystemJsonArr[i]['value']
                let value=SystemJsonArr[i]['name']
                SystemJson[key]=value
            }
        },
        error: function () {

        }
    });

    layui.use('table', function(){
        table = layui.table;
        form = layui.form;

        tableIns=table.render({
            elem: '#taskSrcTable',
            url:'/taskSourceList',
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: true,
            limits: [10, 50, 100, 500, 1000],
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[
                {checkbox: true}
                ,{field:'id', title:'任务库id',align:'center',width:100}
                ,{field:'taskName', title: '任务类型',align:'center',width:150}
                ,{field:'taskState',title:'任务状态',align:'center',width:160, templet: function (d) {
                    var task = d;
                        var result = '';
                        var state = '';
                        if (d.taskState == 0){
                            state = '未执行';
                        }else if (d.taskState == 1){
                            state = '执行中';
                        }else if (d.taskState == 2){
                            state = '执行成功';
                        }else if (d.taskState == 3){
                            state = '执行失败';
                        }else if (d.taskState == 4){
                            state = '未生成任务';
                        }
                    result = state
                        + '<br>'+ '<a lay-event="jumpTaskLogs" class="layui-btn layui-btn-xs" style="background-color: #5FB878">流水</a>'
                         + '<a lay-event="jumpTaskResult" class="layui-btn layui-btn-xs" style="background-color: #5FB878">结果</a>';
                        return result;
                    }}
                ,{field:'priority',title:'优先级',align:'center',width:80}
                ,{field:'workType',title:'大任务类型',align:'center',width:160}
                ,{field:'jobName',title:'调度Job',align:'center',width:160}
                ,{field:'isEnforced',title:'是否强制',align:'center',width:100}
                ,{field: 'type', title: '周期', align: 'center',width:80,templet: function(d){
                        var result = '';
                        var i = d.type;
                        if (i == 1){
                            result = '周期'
                        }else if (i == 2){
                            result = '单次'
                        }else {
                            result = '周期性单次'
                        }
                        return  result+'</span>';
                    } }
                ,{ field: '', title: '参数',width:200,templet: function(d){
                        var result = JSON.stringify(d.params).replaceAll('\\','').substring(1).slice(0,-1);
                        return  result+'</span>';
                    }}
                ,{field:'expectedTime',title:'预计执行时间',align:'center',width:200}
                ,{field:'lastCreateTime',title:'上次生成时间',align:'center',width:200}
                ,{field:'lastResultTime',title:'上次返回结果时间',align:'center',width:200}
                ,{field:'refreshTime',title:'有效时间',align:'center',width:200}
                ,{field:'systemName',title:'创建系统',align:'center',width:100}
                ,{field:'createdAt',title:'创建时间',align:'center',width:200}
                ,{title:'操作',align:'center', toolbar:'#optBar',width:200}
            ]],

            done: function(res, curr){

                // $("[data-field='systemId']").children().each(function(){
                //     $(this).text(SystemJson[$(this).text()])
                // });
                //得到数据总量
                curr = res.pageNum;
                pageCurr=curr;
                $("[data-field='taskState']").children().each(function(){
                    if($(this).text()=='0'){
                        $(this).text("未执行")
                    }else if($(this).text()=='1'){
                        $(this).text("执行中")
                    }else if($(this).text()=='2') {
                        $(this).text("执行成功")
                    }else if($(this).text()=='3') {
                        $(this).text("执行失败")
                    }else if($(this).text()=='4') {
                        $(this).text("任务未生成")
                    }
                });
                $("[data-field='isEnforced']").children().each(function(){
                    if($(this).text()== 0){
                        $(this).text("否")
                    }else if($(this).text()==1){
                        $(this).text("是")
                    }
                });
            }
        });

        //监听工具条
        table.on('tool(taskSrcTable)', function(obj){
            var data = obj.data;
            if(obj.event === 'del'){
                //删除
                delTask(data,data.id);
            }else if (obj.event === 'rePush'){
                rePush(data,data.id);
            } else if (obj.event === 'jumpTaskLogs'){
                jumpTaskLogs(data);
            }else if (obj.event === 'jumpTaskResult'){
                jumpTaskResult(data);
            } else if(obj.event === 'edit'){
                //编辑


                $("#uniqueId").text(data.uniqueId);
                $("#id").val(data.id);
                $("#type").val(typeJson[data.type]);
                $("#expectedTime").val(data.expectedTime);
                $("#taskName").val(data.taskName);
                $("#systemId").val(SystemJson[data.systemId]);
                $("#priority").val(data.priority);
                $("#isEnforced").val(isEnforcedJson[data.isEnforced]);

                layer.open({
                    type:1,
                    title: "任务库详情",
                    fixed:false,
                    resize :false,
                    shadeClose: true,
                    area: ['80%','80%'],
                    content:$('#detailTaskSrc'),
                    success:function(){

                        loadParamsTable(data.params)
                        loadResultsTable(data.id)

                    }
                });
            }
        });

        //监听提交
        form.on('submit(taskSubmit)', function(data){
            // TODO 校验
            formSubmit(data);
            return false;
        });

        //监听提交
        form.on('submit(batchRePushTaskSourceSubmit)', function (data) {
            // TODO 校验
            batchRePushTaskSourceSubmit(data);
            return false;
        });

    });

    loadSelects();



    //搜索框
    layui.use(['form','laydate'], function(){
        var form = layui.form ,layer = layui.layer;
        //TODO 数据校验
        //监听搜索框
        form.on('submit(searchSubmit)', function(data){


            var taskSrc= {};


            taskSrc.id=data.field.idSearch;
            taskSrc.type=data.field.typeSearch;


            if(data.field.stateSearch!='')
                taskSrc.taskState=data.field.stateSearch
            if(data.field.paramSearch!='')
                taskSrc.params=data.field.paramSearch
            if(data.field.expectedTime1!=''){
                taskSrc.expectedTime=data.field.expectedTime1
            }
            if(data.field.createdTime!=''){
                taskSrc.createdAt=data.field.createdTime
            }

            // if(data.field.endTime!='')
            //     taskSrc.endTime=data.field.endTime+' 23:59:59'

            if(data.field.systemSearch!=''){
                var systemStr=data.field.systemSearch.toString();
                systemStr=systemStr.replace(/,/g,"','");
                systemStr="('"+systemStr+"')";
                taskSrc.systemId=systemStr;
            }
            if(data.field.taskNameSearch!=''){
                var taskNameStr=data.field.taskNameSearch.toString();
                taskNameStr=taskNameStr.replace(/,/g,"','");
                taskNameStr="('"+taskNameStr+"')";
                taskSrc.taskIds=taskNameStr;
            }

            //防止layui表格reload where参数保留
            if(data.field.stateSearch=='')
                taskSrc.taskState=null;
            if(data.field.paramSearch=='')
                taskSrc.params=null;
            if(data.field.expectedTime1=='')
                taskSrc.expectedTime=null;
            if(data.field.createdTime=='')
                taskSrc.createdAt=null;
            if(data.field.idSearch=='')
                taskSrc.id=0;//如果搜索框为空 置0 mybatis会把整型的0当做''
            if(data.field.typeSearch=='')
                taskSrc.type=0;
            if(data.field.systemSearch=='')
                taskSrc.systemId=null;
            if(data.field.taskNameSearch=='')
                taskSrc.taskIds=null;




            tableIns.reload({
                where: taskSrc ,
                page: {
                    curr: pageCurr //从当前页码开始
                }
            });
            return false;
        });
    });

    layui.use([ 'form', 'upload'], function () {
        upload = layui.upload;
        upload.render({
            elem: '#upload' //绑定元素
            , url: '/fixTaskSource'//上传接口
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

                    demoListView.append(tr);
                    app.showTable = true;
                    that.content = result;
                    console.log(that.content);
                });
            }
            , done: function (res) {//上传完毕回调
                //执行上传请求后的回调。返回三个参数，分别为：res（服务端响应信息）、index（当前文件的索引）、upload（重新上传的方法，一般在文件上传失败后使用）
                console.log(res);
            }
            , error: function (index, upload) {//执行上传请求出现异常的回调
                //返回index（当前文件的索引）、upload（重新上传的方法
                layer.closeAll('loading');
            }
        });
    });
});

//提交表单
function formSubmit(obj){
    $.ajax({
        type: "POST",
        data: obj.field,
        url: "/updateTaskSourceList",
        success: function (data) {
            if (data.code == SUCCESS_CODE) {
                layer.msg(data.msg,function(){
                    layer.closeAll();
                    load(obj);
                });
            } else {
                layer.msg(data.msg, {icon: 2});
            }
        },
        error: function () {
            layer.msg("操作请求错误，请您稍后再试", {icon: 2}, function(){
                layer.closeAll();
                //加载load方法
                load(obj);//自定义
            });
        }
    });
}

//提交表单
function batchRePushTaskSourceSubmit(obj){
    $.ajax({
        type: "POST",
        data: obj.field,
        url: "/remoteRePushCircleTasks",
        success: function (data) {
            if (data.code == SUCCESS_CODE) {
                layer.msg(data.msg,function(){
                    layer.closeAll();
                    load(obj);
                });
            } else {
                layer.msg(data.msg, {icon: 2});
            }
        },
        error: function () {
            layer.msg("操作请求错误，请您稍后再试", {icon: 2}, function(){
                layer.closeAll();
                //加载load方法
                load(obj);//自定义
            });
        }
    });
}

function addTask(){
   alert("新增")
}


function delTask(obj,id) {
    if(null!=id){
        layer.confirm('您确定要删除'+obj.uniqueId+'的任务吗？', {
            btn: ['确认','返回'] //按钮
        }, function(){
            $.post("/deleteTaskSourceList",{"id":id,"isSingle":obj.isSingle},function(data){
                if (data.code == SUCCESS_CODE) {
                    layer.msg(data.msg, { anim: -1, time: 600 },function(){
                        layer.closeAll();
                        load(obj);
                    });
                } else {
                    layer.msg(data.msg, {icon: 2});
                }
            });
        }, function(){
            layer.closeAll();
        });
    }else {
        layer.msg("数据异常，id为空", {icon: 2});
    }
}


//机器添加
function batchRePushTaskSource() {
    $("#id").val("");
    layer.open({
        type: 1,
        title: '批量重推',
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['850px', '80%'],
        content: $('#batchRePushTaskSource'),
        end: function () {
            cleanUser();
        },
        success: function () {
            formSelects.data('accountContinentsRePush', 'server', {
                url: '/getValidAccountContinents',
                direction: 'down',
            });
            // formSelects.data('accountSitesRePush', 'server', {
            //     url: '/getValidAccountSites',
            //     direction: 'down',
            // });

            formSelects.data('taskNameRePush', 'server', {
                url: '/getDropDownTaskId',
                direction: 'down',
            });
        }
    });


}

function rePush(obj,id) {
    if(null!=id){
        layer.confirm('您确定要强制入池'+obj.uniqueId+'的任务吗？', {
            btn: ['确认','返回'] //按钮
        }, function(){
            $.post("/forcedEnterPool",{"id":id,"isSingle":obj.isSingle,"uniqueId":obj.uniqueId},function(data){
                if (data.code == SUCCESS_CODE) {
                    layer.msg(data.msg, { anim: -1, time: 600 },function(){
                        layer.closeAll();
                        load(obj);
                    });
                } else {
                    layer.msg(data.msg);
                }
            });
        }, function(){
            layer.closeAll();
        });
    }else {
        layer.msg("数据异常，id为空");
    }
}


//批量删除
$(".delAll_btn").click(function () {
    var checkStatus = table.checkStatus('taskSrcTable'),
        data = checkStatus.data,
        userId = "",
        isSingle = "";
    if (data.length > 0) {
        for (var i in data) {
            userId += data[i].id + ",";
            isSingle += data[i].isSingle + ",";
        }
        console.log(userId);
        layer.confirm('确定删除选中的任务？', {icon: 3, title: '提示信息'}, function (index) {
            $.post('/batchDeleteTaskSourceList', {ids: userId,isSingle: isSingle}, function (data) {
                layer.msg(data.msg);
                tableIns.reload();
                layer.close(index);
            });
        })
    } else {
        layer.msg("请选择需要删除的任务");
    }
});



function loadParamsTable(params) {


    let paramsJson=JSON.parse(params)
    let paramsArr=[]
    for (const item in paramsJson) {
        let val={}
        val["paramName"]=item
        val["paramValue"]=paramsJson[item]
        paramsArr.push(val)
    }

    table.render({
        elem: '#taskSrcParams',
        cols: [[
            {field:'paramName', title:'参数名称',align:'center'}
            ,{field:'paramValue', title:'参数值',align:'center'}

        ]]
        , data:paramsArr
    });
}

function loadResultsTable(id) {

    table.render({
        elem: '#taskSrcResults',
        url:'/getTaskResultFromTaskSrc',
        where: {"taskSourceId":id},
        method: 'post', //默认：get请求
        page: true,
        request: REQUEST_BODY,
        response: RESPONSE_BODY,
        cols: [[
            {field:'createdTime', title: '创建时间',align:'center'}
            ,{field:'id', title: '任务结果id',align:'center'}
            ,{field:'resultHashKey', title:'resultHashKey',align:'center'}
            ,{field:'result', title:'任务结果',align:'center'}
        ]]

    });
}


function loadSelects() {
    formSelects.data('taskNameSearch', 'server', {
        url: '/getDropDownTaskId',
        direction: 'down',
    });



    formSelects.data('systemSearch', 'server', {
        url: '/getDropDownSystem',
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
//跳转任务流水页面
function  jumpTaskLogs(data){
    var taskSourceId = '';
    if (data.isSingle == 0){
        taskSourceId = "circle_"+data.id;
    }else {
        taskSourceId = "single_"+data.id;
    }
    window.open("../task/taskLogs?"+taskSourceId);
}
//跳转任务结果页面
function  jumpTaskResult(data){
    var taskSourceId = '';
    if (data.isSingle == 0){
        taskSourceId = "circle_"+data.id;
    }else {
        taskSourceId = "single_"+data.id;
    }
    window.open("../task/taskResult?"+taskSourceId);
}

function cleanTask(){
    $("#uniqueIdSearch").val("");
    $("#isSingleSearch").val("");
}
