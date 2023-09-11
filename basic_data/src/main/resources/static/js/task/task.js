/**
 * 任务管理
 */
var pageCurr;
var form;
var table;
var laytpl;
var xmSelect;


var layTableId = "layTable";
var columnTableIns;


var columnTypeArr = [
    {id: "int", name: 'int'},
    {id: "string", name: 'string'},
    {id: "float", name: 'float'},
    {id: "datetime", name: 'datetime'},
    {id: "date", name: 'date'},
    {id: "json", name: 'json'},
];
var platformArr = [
    {"name": "亚马逊", "value": 1},
    {"name": "沃尔玛", "value": 2},
    {"name": "速卖通", "value": 3},
    {"name": "facebook", "value": 4},
    {"name": "boss", "value": 5}
]


$(function () {




    $.ajax({

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

    layui.use('laytpl', function () {

        laytpl = layui.laytpl;

    });
    layui.use('table', function () {
        table = layui.table;
        form = layui.form;

        tableIns = table.render({
            elem: '#taskList',
            url: '/taskManage',
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: true,
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[

                {field: 'id', title: 'ID', hide: true}
                , {field: 'status', title: '启用/禁用', align: 'center', width: "10%", templet: '#statusTemp'}
                , {field: 'taskName', title: '任务名称', align: 'center'}
                , {field: 'taskExplain', title: '任务类型说明', align: 'center'}
                , {field: 'priority', title: '默认优先级', align: 'center', sort: true}
                , {
                    field: '', title: '任务周期', align: 'center',

                    templet: function (d) {
                        if (d.type == 1) {
                            return '周期性'
                        } else if (d.type == 2) {
                            return '单次性'
                        }
                    }
                }
                , {
                    field: '', title: '任务重置间隔', align: 'center',

                    templet: function (d) {
                        var intervalType = (d.intervalType == 1 ? "自然日" : "自然小时")
                        return d.intervalTimes + "个" + intervalType
                    }

                }
                , {field: '', title: '需求系统', align: 'center' ,
                    templet: function (d) {
                        var arr = d.systems.split(",");
                        var res="";
                        for (let i = 0; i < arr.length; ++i) {
                            if (res != '') {
                                res += '/';
                            }
                            res+=SystemJson[arr[i]];
                        }
                        return res
                    }}
                , {
                    field: '', title: '接口调用频次限制(次/min)', align: 'center',

                    templet: function (d) {
                        return d.apiMaxTimes +"/"+ d.apiTimeLimit+"min"
                    }

                }
                , {
                    field: '', title: '操作人/操作时间', align: 'center',

                    templet: function (d) {
                        return d.updatedUser+"/" + d.updatedAt
                    }

                }

                , {title: '操作', align: 'center', toolbar: '#optBar', width: "15%", fixed: 'right'}
            ]],
            done: function (res, curr) {
                //得到当前页码
                $("[data-field='isSignIn']").children().each(function () {
                    if ($(this).text() == '0') {
                        $(this).text("是")
                    } else if ($(this).text() == '1') {
                        $(this).text("否")
                    }
                });
                //得到数据总量
                curr = res.pageNum;
                pageCurr=curr;
            }
        });

        //监听工具条
        table.on('tool(taskTable)', function (obj) {
            var data = obj.data;
            if (obj.event === 'del') {
                //删除
                delUser(data, data.id);
            } else if (obj.event === 'edit') {
                //编辑
                openEditTask(data, "编辑");
            }

        });

        //监听添加提交
        form.on('submit(AddTaskSubmit)', function (data) {

            var oldData = table.cache[layTableId];
            var columns = new Array();

            for (var i = 0; i < oldData.length; i++) {
                var Column = new Object();
                Column.columnsName = oldData[i].columnsName
                Column.columnExplain = oldData[i].columnExplain
                Column.columnType = oldData[i].type
                Column.isCombined = oldData[i].isCombined
                Column.isRequired = oldData[i].isRequired
                Column.isCombinedUnique = oldData[i].isCombinedUnique
                Column.isReturnFlag = oldData[i].isReturnFlag
                Column.isIdFlag = oldData[i].isIdFlag
                columns.push(Column);
            }


            var task = {};
            task.id = data.field.taskId;
            task.taskName = data.field.taskName;
            task.taskExplain = data.field.taskExplain;
            task.columnList = columns;
            task.systems = data.field.systems;
            task.runMode = data.field.runMode;
            task.produceInterval = data.field.produceInterval;
            task.produceCapacity = data.field.produceCapacity;
            task.produceConcurrency = data.field.produceConcurrency;
            task.apiMaxTimes = data.field.apiMaxTimes;
            task.apiTimeLimit = data.field.apiTimeLimit;
            task.intervalTimes = data.field.intervalTimes;
            task.intervalType = data.field.intervalType;
            task.priority = data.field.priority;
            task.type = $("input[name=type]:checked").val()//这里注意不能用data.filed.type 因为参数的类型也叫ype
            task.largeTaskType = data.field.largeTaskType;
            task.cronExpression = data.field.cronExpression;
            task.executePlatforms = data.field.executePlatforms;
            task.status=data.field.hiddenStatus;
            task.isTimely = data.field.isTimely;
            task.taskAbbreviation = data.field.taskAbbreviation;
            task.awsUserRegionFunctions = data.field.awsUserRegionFunctions;
            task.isCombo = data.field.isCombo;
            task.exMaxTimes = data.field.exMaxTimes;
            task.errorMaxTimes = data.field.errorMaxTimes;
            task.limitRetryTimes = data.field.limitRetryTimes;
            var comboColumns =[];
            $("input[name='comboColumns']").each(function(){
                 comboColumns.push($.trim($(this).val()));
            });
            task.comboColumns = comboColumns.join(",");
            formSubmit(JSON.stringify(task));
            return false;
        });
        //监听修改提交
        form.on('submit(updateTaskSubmit)', function (data) {

            var oldData = table.cache[layTableId];
            var columns = new Array();

            for (var i = 0; i < oldData.length; i++) {
                var Column = new Object();
                Column.columnsName = oldData[i].columnsName
                Column.columnExplain = oldData[i].columnExplain
                Column.columnType = oldData[i].type
                Column.isCombined = oldData[i].isCombined
                Column.isRequired = oldData[i].isRequired
                Column.isCombinedUnique = oldData[i].isCombinedUnique
                Column.isReturnFlag = oldData[i].isReturnFlag
                Column.isIdFlag = oldData[i].isIdFlag
                columns.push(Column);
            }

            var task = {};
            task.id = data.field.taskId;
            task.taskName = data.field.taskName;
            task.taskExplain = data.field.taskExplain;
            task.columnList = columns;
            task.systems = data.field.systems;
            task.runMode = data.field.runMode;
            task.produceInterval = data.field.produceInterval;
            task.produceCapacity = data.field.produceCapacity;
            task.produceConcurrency = data.field.produceConcurrency;
            task.apiMaxTimes = data.field.apiMaxTimes;
            task.apiTimeLimit = data.field.apiTimeLimit;
            task.intervalTimes = data.field.intervalTimes;
            task.intervalType = data.field.intervalType;
            task.priority = data.field.priority;
            task.type = $("input[name=type]:checked").val()//这里注意不能用data.filed.type 因为参数的类型也叫ype
            task.largeTaskType = data.field.largeTaskType;
            task.cronExpression = data.field.cronExpression;
            task.executePlatforms = data.field.executePlatforms;
            task.status=data.field.hiddenStatus;
            task.isTimely = data.field.isTimely;
            task.taskAbbreviation = data.field.taskAbbreviation;
            task.awsUserRegionFunctions = data.field.awsUserRegionFunctionsUpdate;
            task.isCombo = data.field.isCombo;
            task.exMaxTimes = data.field.exMaxTimes;
            task.errorMaxTimes = data.field.errorMaxTimes;
            task.limitRetryTimes = data.field.limitRetryTimes;

            var comboColumns =[];
            $("input[name='comboColumns']").each(function(){
            	comboColumns.push($.trim($(this).val()));
            });
            task.comboColumns = comboColumns.join(",");

            formSubmit(JSON.stringify(task));
            return false;
        });


        form.on('switch(status)', function (data) {
// 得到开关的value值，实际是需要修改的ID值。
            var task = {};
            var id = data.value;
            var status = this.checked ? STATUS_VALID : STATUS_INVALID;

            task.id = id;
            task.status = status;
            $.ajax({

                type: "POST",
                dataType: "json",
                data: JSON.stringify(task),
                contentType: "application/json;charset=UTF-8",
                url: "/updateTaskStatus",
                success: function (data) {
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

    //因为是异步 不能写在打开任务窗口的success里
    formSelects.data('systems', 'server', {
        url: '/getDropDownSystem',
        direction: 'down',
    });

    //选择框里 显示用户名
    // formSelects.data('lambdaUserName', 'server', {
    //     url: '/getLambdaUserName',
    //     direction: 'down',
    // });

    //选择框里 显示区域和函数名regionFunctionName
    formSelects.data('regionFunctionName', 'server', {
        url: '/getRegionFunctionName',
        direction: 'down',
    });

    formSelects.data('executePlatforms', 'server', {
        url: '/getCrawlPlatform',
        direction: 'down',
    });

    //搜索框
    layui.use(['form', 'laydate'], function () {
        var form = layui.form, layer = layui.layer;
        //TODO 数据校验
        //监听搜索框
        form.on('submit(searchSubmit)', function (data) {
            //重新加载table
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

//提交表单
function formSubmit(obj) {
    $.ajax({
        type: "POST",
        dataType: "json",
        data: obj,
        contentType: "application/json;charset=UTF-8",
        url: "/updateTask",
        success: function (data) {
            if (data.code == SUCCESS_CODE) {
                layer.msg(data.msg, { shift: -1, time: 600 },function(){
                    layer.closeAll();
                    load(obj);
                });
            } else {
                layer.msg(data.msg, { shift: -1, time: 600, icon: 2 },function(){
                    layer.closeAll();
                    load(obj);
                });
            }
        },
        error: function () {
            layer.msg("操作请求错误，请您稍后再试", { shift: -1, time: 600, icon: 2 },function(){
                layer.closeAll();
                load(obj);
            });
        }
    });
}

//开通用户
function addTask() {
    var emptyData = {};
    emptyData.columnList = [];
    openAddTask(emptyData, "add");
}

function openEditTask(data, title) {

    $("#addSelect").hide();
    $("#AddTaskSubmit").hide();
    $("#updateTaskSubmit").show();
    $("#updateSelect").show();

    $("#taskId").val(data.id);
    $("#taskName").val(data.taskName);
    $("#priority").val(data.priority);
    $("#cronExpression").val(data.cronExpression);
    $("#taskExplain").val(data.taskExplain);
    $("#runMode").val(data.runMode);
    $("#produceInterval").val(data.produceInterval);
    $("#produceCapacity").val(data.produceCapacity);
    $("#produceConcurrency").val(data.produceConcurrency);
    $("#apiMaxTimes").val(data.apiMaxTimes);
    $("#intervalTimes").val(data.intervalTimes);
    $("#intervalType").val(data.intervalType);
    $("#apiTimeLimit").val(data.apiTimeLimit);
    $("#hiddenStatus").val(data.status);
    $("#taskAbbreviation").val(data.taskAbbreviation);
    $("#exMaxTimes").val(data.exMaxTimes);
    $("#errorMaxTimes").val(data.errorMaxTimes);
    $("#limitRetryTimes").val(data.limitRetryTimes);
    var comboColumns = data.comboColumns.split(",");
    $("#column-box").find("div").remove();

    comboColumns.forEach((value)=>{
            var html = "<div class=\"layui-input-inline\"style=\"width:100px\"><input name=\"comboColumns\" autocomplete=\"off\" class=\"layui-input \" value=\""+$.trim(value)+"   \"/></div>"
            $("#column-box").append(html);
    })

    var awsUserRegionFunctionsUpdate = null;
    awsUserRegionFunctionsUpdate = data.awsUserRegionFunctions;

    formSelects.data('awsUserRegionFunctionsUpdate', 'server', {
        url: '/getUserRegionFunctions',
        keyName: 'value',
        keyVal: 'value',
        direction: 'down',          //多选下拉方向, auto|up|down
        response: {
            statusCode: 0,          //成功状态码
            statusName: 'code',     //code key
            msgName: 'msg',         //msg key
            dataName: 'data'        //data key
        },
        success: function(value, url, searchVal, result){


            if(awsUserRegionFunctionsUpdate != null){

                var assistAuditArry = new Array();
                assistAuditArry.push(awsUserRegionFunctionsUpdate);
                formSelects.value('awsUserRegionFunctionsUpdate', assistAuditArry);
                form.render('select');
            }else {
                formSelects.value('awsUserRegionFunctionsUpdate', []);

            }
            form.render('select');

        },
        error: function(id, url, searchVal, err){           //使用远程方式的error回调
            //同上
        },
        linkage: true
    });

    form.render('select'); //这个很重要


    var pageNum = $(".layui-laypage-skip").find("input").val();
    $("#pageNum").val(pageNum);

    layer.open({
        type: 1,
        title: title,
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['80%', '80%'],
        content: $('#setTask'),
        end: function () {
            cleanUser();
        },


        success: function () {


            //新建task时还原已选择的largeTaskType和type
            $("input[name=largeTaskType][value='1']").prop("checked", false);
            $("input[name=largeTaskType][value='2']").prop("checked", false);
            $("input[name=type][value='1']").prop("checked", false);
            $("input[name=type][value='2']").prop("checked", false);
            $("input[name=isTimely][value='0']").prop("checked", false);
            $("input[name=isTimely][value='1']").prop("checked", false);
            $("input[name=isCombo][value='0']").prop("checked", false);
            $("input[name=isCombo][value='1']").prop("checked", false);

            layui.use(['jquery', 'table', 'layer'], function () {



                if (data.executePlatforms != null) {
                    var Arr = data.executePlatforms.split(",");
                    formSelects.value('executePlatforms', Arr);
                }

                //先清空 否则新建任务类型时会保留上次结果
                formSelects.value('systems', []);
                if (data.systems != null) {
                    var Arr = data.systems.split(",");
                    formSelects.value('systems', Arr);
                }

                if (data.largeTaskType == 1) {//按可爬取平台

                    $("input[name=largeTaskType][value='1']").prop("checked", true);
                    $("input[name=largeTaskType][value='2']").prop("checked", false);
                    formSelects.undisabled('executePlatforms');


                } else if (data.largeTaskType == 2) {
                    $("input[name=largeTaskType][value='1']").prop("checked", false);
                    $("input[name=largeTaskType][value='2']").prop("checked", true);
                    formSelects.disabled('executePlatforms');
                }

                if (data.type == 1) {//周期性
                    $("input[name=type][value='1']").prop("checked", true);
                    $("input[name=type][value='2']").prop("checked", false);
                    $('#cronExpression').removeClass("layui-disabled").prop("disabled", false);

                } else if (data.type == 2) {
                    $("input[name=type][value='1']").prop("checked", false);
                    $("input[name=type][value='2']").prop("checked", true);
                    $('#cronExpression').addClass("layui-disabled").prop("disabled", true);

                }

                if (data.isTimely == 0) {
                    $("input[name=isTimely][value='0']").prop("checked", true);
                    $("input[name=isTimely][value='1']").prop("checked", false);

                } else if (data.isTimely == 1) {
                    $("input[name=isTimely][value='0']").prop("checked", false);
                    $("input[name=isTimely][value='1']").prop("checked", true);
                }

                if (data.isCombo == 0) {
                    $("input[name=isCombo][value='0']").prop("checked", true);
                    $("input[name=isCombo][value='1']").prop("checked", false);

                } else if (data.isCombo == 1) {
                    $("input[name=isCombo][value='0']").prop("checked", false);
                    $("input[name=isCombo][value='1']").prop("checked", true);
                }
                form.render();

                for (var i = 0; i < data.columnList.length; i++) {
                    data.columnList[i].tempId = new Date().valueOf() + i;
                    data.columnList[i].type = data.columnList[i].columnType
                }

                columnTableIns = table.render({
                    elem: '#columnTable',
                    id: layTableId,
                    // width:200,
                    cols: [[        ////表头
                        {title: '序号', type: 'numbers'},


                        {field: 'columnsName', title: '参数名称', align: "center", width: "10%", edit: 'text'},

                        {
                            field: 'type', width: "20%", title: '参数类型', templet: function (d) {
                                var options = renderSelectOptions(columnTypeArr, {
                                    valueField: "id",
                                    textField: "name",
                                    selectedValue: d.type
                                });
                                return '<a lay-event="type"></a><select name="type" class="typeClass" lay-filter="type"><option value="">请选择分类</option>' + options + '</select>';
                            }
                        },
                        {field: 'columnExplain', title: '参数说明', align: "center", width: "10%", edit: 'text'},
                        {
                            field: 'isRequired',
                            title: '必传',
                            width: 85,
                            templet: '#isRequired',
                            event: 'isRequired',
                            align: "center",
                            unresize: true
                        },
                        {
                            field: 'isCombined',
                            title: '中心端生成',
                            width: 105,
                            templet: '#isCombined',
                            event: 'isCombined',
                            align: "center",
                            unresize: true
                        },
                        {
                            field: 'isCombinedUnique',
                            title: '判重',
                            width: 85,
                            templet: '#isCombinedUnique',
                            event: 'isCombinedUnique',
                            align: "center",
                            unresize: true
                        },
                        {
                            field: 'isReturnFlag',
                            title: '结果返回',
                            width: 105,
                            templet: '#isReturnFlag',
                            event: 'isReturnFlag',
                            align: "center",
                            unresize: true
                        },
                        {
                            field: 'isIdFlag',
                            title: 'ID返回',
                            width: 105,
                            templet: '#isIdFlag',
                            event: 'isIdFlag',
                            align: "center",
                            unresize: true
                        },
                        {title: '操作', align: 'center', toolbar: '#optBar2', width: "10%"}

                    ]]
                    , done: function (res, curr, count) {//改type下拉栏css
                        var parentName = document.getElementsByClassName('typeClass');
                        for (var i = 0; i < parentName.length; i++){
                            var domObject = parentName[i];
                            var jqueryObejct=$(domObject);
                            parentName[i].parentElement.style.overflow = "visible"
                        }
                    }

                    , data: data.columnList
                    , limit: 1000 //每页默认显示的数量

                });


                $('.layui-btn[data-type]').unbind("click");//先清除之前的绑定

                $('.layui-btn[data-type]').on('click', function () {
                    var type = $(this).data('type');
                    activeByType(type);
                });

                //$columnDom.find('.input').val("123124");

                form.on('select(type)', function (data) {
                    var elem = data.elem; //得到select原始DOM对象
                    $(elem).prev("a[lay-event='type']").trigger("click");
                });

                table.on('tool(columnTable)', function (obj) {
                    var data = obj.data;


                    var tr = obj.tr;

                    if (obj.event === 'optBar2RemoveColumn') {

                        //removeColumn();

                        layer.confirm('真的删除行么？', function (index) {

                            obj.del(); //删除对应行（tr）的DOM结构，并更新缓存

                            layer.close(index);

                            // alert(index)
                            activeByType('removeEmptyTableCache');
                        });

                    } else if (obj.event === 'isRequired') {
                        var isRequiredVal = tr.find("input[name='isRequired']").prop('checked') ? 1 : 0;
                        $.extend(obj.data, {'isRequired': isRequiredVal})
                        activeByType('updateRow', obj.data);	//更新行记录对象
                    } else if (obj.event === 'isCombined') {
                        var isCombinedVal = tr.find("input[name='isCombined']").prop('checked') ? 1 : 0;
                        $.extend(obj.data, {'isCombined': isCombinedVal})
                        activeByType('updateRow', obj.data);	//更新行记录对象
                    } else if (obj.event === 'isCombinedUnique') {
                        var isCombinedUniqueVal = tr.find("input[name='isCombinedUnique']").prop('checked') ? 1 : 0;
                        $.extend(obj.data, {'isCombinedUnique': isCombinedUniqueVal})
                        activeByType('updateRow', obj.data);	//更新行记录对象
                    }else if (obj.event === 'isReturnFlag') {
                        var isReturnFlagVal = tr.find("input[name='isReturnFlag']").prop('checked') ? 1 : 0;
                        $.extend(obj.data, {'isReturnFlag': isReturnFlagVal})
                        activeByType('updateRow', obj.data);	//更新行记录对象
                    }else if (obj.event === 'isIdFlag') {
                        var isIdFlagVal = tr.find("input[name='isIdFlag']").prop('checked') ? 1 : 0;
                        $.extend(obj.data, {'isIdFlag': isIdFlagVal})
                        activeByType('updateRow', obj.data);	//更新行记录对象
                    }else if (obj.event === 'type') {
                        var select = tr.find("select[name='type']");
                        if (select) {
                            var selectedVal = select.val();
                            if (!selectedVal) {
                                layer.tips("请选择一个分类", select.next('.layui-form-select'), {tips: [3, '#ff5722']}); //吸附提示
                            }
                            $.extend(obj.data, {'type': selectedVal});
                            activeByType('updateRow', obj.data);	//更新行记录对象
                        }

                    }

                });


                form.on('radio(largeTaskType2)', function (data) {

                    // formSelects.value('executePlatforms', []);
                    formSelects.disabled('executePlatforms');
                });


                form.on('radio(largeTaskType1)', function (data) {

                    formSelects.undisabled('executePlatforms');

                });

                form.on('radio(type1)', function (data) {

                    $('#cronExpression').removeClass("layui-disabled").prop("disabled", false);

                });

                form.on('radio(type2)', function (data) {

                    $('#cronExpression').addClass("layui-disabled").prop("disabled", true);

                });


            });


        }



    });
}// openTask end

function delUser(obj, id) {
    if (null != id) {
        layer.confirm('您确定要删除' + obj.taskName + '的任务吗？', {
            btn: ['确认', '返回'] //按钮
        }, function () {
            $.post("/deleteTask", {"id": id}, function (data) {
                if (data.code == SUCCESS_CODE) {
                    layer.msg(data.msg, {anim: -1, time: 600}, function () {
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

//批量删除任务
$("#batchDeleteTask").bind("click", function () {
    var checkStatus = table.checkStatus('taskList');
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
                url: "/batchDeleteTask",
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

function cleanUser() {
    $("#taskNameSearch").val("");
    $("#systemSearch").val("");

}


//定义事件集合
var active = {


    addRow: function () {	//添加一行

        var oldData = table.cache[layTableId];

        var curtempId = new Date().valueOf();
        var newRow = {tempId: curtempId};


        oldData.push(newRow);
        columnTableIns.reload({
            data: oldData
        });
    },
    updateRow: function (obj) {
        var oldData = table.cache[layTableId];

        for (var i = 0, row; i < oldData.length; i++) {
            row = oldData[i];
            if (row.tempId == obj.tempId) {
                $.extend(oldData[i], obj);
                return;
            }
        }
        columnTableIns.reload({
            data: oldData
        });
    },
    removeEmptyTableCache: function () {
        var oldData = table.cache[layTableId];
        for (var i = 0, row; i < oldData.length; i++) {
            row = oldData[i];
            if (!row || !row.tempId) {
                oldData.splice(i, 1);    //删除一项
            }
            continue;
        }
        columnTableIns.reload({
            data: oldData
        });
    },


}


//激活事件
var activeByType = function (type, arg) {
    if (arguments.length === 2) {
        active[type] ? active[type].call(this, arg) : '';
    } else {
        active[type] ? active[type].call(this) : '';
    }
}

function renderSelectOptions(data, settings) {
    settings = settings || {};
    var valueField = settings.valueField || 'value',
        textField = settings.textField || 'text',
        selectedValue = settings.selectedValue || "";
    var html = [];
    for (var i = 0, item; i < data.length; i++) {
        item = data[i];
        html.push('<option value="');
        html.push(item[valueField]);
        html.push('"');
        if (selectedValue && item[valueField] == selectedValue) {
            html.push(' selected="selected"');
        }
        html.push('>');
        html.push(item[textField]);
        html.push('</option>');
    }
    return html.join('');
}

function openAddTask(data, title) {

    $("#addSelect").show();
    $("#AddTaskSubmit").show();
    $("#updateTaskSubmit").hide();
    $("#updateSelect").hide();
    $("#taskId").val(data.id);
    $("#taskName").val(data.taskName);
    $("#priority").val(data.priority);
    $("#cronExpression").val(data.cronExpression);
    $("#taskExplain").val(data.taskExplain);
    $("#runMode").val(data.runMode);
    $("#produceInterval").val(data.produceInterval);
    $("#produceCapacity").val(data.produceCapacity);
    $("#produceConcurrency").val(data.produceConcurrency);
    $("#apiMaxTimes").val(data.apiMaxTimes);
    $("#intervalTimes").val(data.intervalTimes);
    $("#intervalType").val(data.intervalType);
    $("#apiTimeLimit").val(data.apiTimeLimit);
    $("#hiddenStatus").val(data.status);
    $("#taskAbbreviation").val(data.taskAbbreviation);
    $("#exMaxTimes").val(data.exMaxTimes);
    $("#errorMaxTimes").val(data.errorMaxTimes);
    $("#limitRetryTimes").val(data.limitRetryTimes);

    formSelects.data('awsUserRegionFunctions', 'server', {
        url: '/getUserRegionFunctions',
        keyName: 'value',
        keyVal: 'value',
        direction: 'down',          //多选下拉方向, auto|up|down
        response: {
            statusCode: 0,          //成功状态码
            statusName: 'code',     //code key
            msgName: 'msg',         //msg key
            dataName: 'data'        //data key
        },
        success: function(value, url, searchVal, result){
            form.render('select');
        },
        error: function(value, url, searchVal, err){           //使用远程方式的error回调
                                                            //同上
        },
        linkage: true
    });

    form.render('select'); //这个很重要


    var pageNum = $(".layui-laypage-skip").find("input").val();
    $("#pageNum").val(pageNum);

    layer.open({
        type: 1,
        title: title,
        fixed: false,
        resize: false,
        shadeClose: true,
        area: ['80%', '80%'],
        content: $('#setTask'),
        end: function () {
            cleanUser();
        },


        success: function () {


            //新建task时还原已选择的largeTaskType和type
            $("input[name=largeTaskType][value='1']").prop("checked", false);
            $("input[name=largeTaskType][value='2']").prop("checked", false);
            $("input[name=type][value='1']").prop("checked", false);
            $("input[name=type][value='2']").prop("checked", false);
            $("input[name=isTimely][value='0']").prop("checked", false);
            $("input[name=isTimely][value='1']").prop("checked", false);
            $("input[name=isCombo][value='0']").prop("checked", false);
            $("input[name=isCombo][value='1']").prop("checked", false);

            layui.use(['jquery', 'table', 'layer'], function () {

                if (data.executePlatforms != null) {
                    var Arr = data.executePlatforms.split(",");
                    formSelects.value('executePlatforms', Arr);
                }

                //先清空 否则新建任务类型时会保留上次结果
                formSelects.value('systems', []);
                if (data.systems != null) {
                    var Arr = data.systems.split(",");
                    formSelects.value('systems', Arr);
                }

                if (data.largeTaskType == 1) {//按可爬取平台

                    $("input[name=largeTaskType][value='1']").prop("checked", true);
                    $("input[name=largeTaskType][value='2']").prop("checked", false);
                    formSelects.undisabled('executePlatforms');

                } else if (data.largeTaskType == 2) {
                    $("input[name=largeTaskType][value='1']").prop("checked", false);
                    $("input[name=largeTaskType][value='2']").prop("checked", true);
                    formSelects.disabled('executePlatforms');
                }

                if (data.type == 1) {//周期性
                    $("input[name=type][value='1']").prop("checked", true);
                    $("input[name=type][value='2']").prop("checked", false);
                    $('#cronExpression').removeClass("layui-disabled").prop("disabled", false);

                } else if (data.type == 2) {
                    $("input[name=type][value='1']").prop("checked", false);
                    $("input[name=type][value='2']").prop("checked", true);
                    $('#cronExpression').addClass("layui-disabled").prop("disabled", true);

                }

                if (data.isTimely == 0) {
                    $("input[name=isTimely][value='0']").prop("checked", true);
                    $("input[name=isTimely][value='1']").prop("checked", false);

                } else if (data.isTimely == 1) {
                    $("input[name=isTimely][value='0']").prop("checked", false);
                    $("input[name=isTimely][value='1']").prop("checked", true);
                }
                if (data.isCombo == 0) {
                    $("input[name=isCombo][value='0']").prop("checked", true);
                    $("input[name=isCombo][value='1']").prop("checked", false);

                } else if (data.isCombo == 1) {
                    $("input[name=isCombo][value='0']").prop("checked", false);
                    $("input[name=isCombo][value='1']").prop("checked", true);
                }

                form.render();

                for (var i = 0; i < data.columnList.length; i++) {
                    data.columnList[i].tempId = new Date().valueOf() + i;
                    data.columnList[i].type = data.columnList[i].columnType
                }

                columnTableIns = table.render({
                    elem: '#columnTable',
                    id: layTableId,
                    // width:200,
                    cols: [[        ////表头
                        {title: '序号', type: 'numbers'},


                        {field: 'columnsName', title: '参数名称', align: "center", width: "10%", edit: 'text'},

                        {
                            field: 'type', width: "20%", title: '参数类型', templet: function (d) {
                                var options = renderSelectOptions(columnTypeArr, {
                                    valueField: "id",
                                    textField: "name",
                                    selectedValue: d.type
                                });
                                return '<a lay-event="type"></a><select name="type" class="typeClass" lay-filter="type"><option value="">请选择分类</option>' + options + '</select>';
                            }
                        },
                        {field: 'columnExplain', title: '参数说明', align: "center", width: "10%", edit: 'text'},
                        {
                            field: 'isRequired',
                            title: '必传',
                            width: 85,
                            templet: '#isRequired',
                            event: 'isRequired',
                            align: "center",
                            unresize: true
                        },
                        {
                            field: 'isCombined',
                            title: '中心端生成',
                            width: 105,
                            templet: '#isCombined',
                            event: 'isCombined',
                            align: "center",
                            unresize: true
                        },
                        {
                            field: 'isCombinedUnique',
                            title: '判重',
                            width: 85,
                            templet: '#isCombinedUnique',
                            event: 'isCombinedUnique',
                            align: "center",
                            unresize: true
                        },
                        {
                            field: 'isReturnFlag',
                            title: '结果返回',
                            width: 105,
                            templet: '#isReturnFlag',
                            event: 'isReturnFlag',
                            align: "center",
                            unresize: true
                        },
                        {
                            field: 'isIdFlag',
                            title: 'ID返回',
                            width: 105,
                            templet: '#isIdFlag',
                            event: 'isIdFlag',
                            align: "center",
                            unresize: true
                        },
                        {title: '操作', align: 'center', toolbar: '#optBar2', width: "10%"}

                    ]]
                    , done: function (res, curr, count) {//改type下拉栏css
                        var parentName = document.getElementsByClassName('typeClass');
                        for (var i = 0; i < parentName.length; i++){
                            var domObject = parentName[i];
                            var jqueryObejct=$(domObject);
                            parentName[i].parentElement.style.overflow = "visible"
                        }
                    }

                    , data: data.columnList
                    , limit: 1000 //每页默认显示的数量

                });


                $('.layui-btn[data-type]').unbind("click");//先清除之前的绑定

                $('.layui-btn[data-type]').on('click', function () {
                    var type = $(this).data('type');
                    activeByType(type);
                });

                //$columnDom.find('.input').val("123124");

                form.on('select(type)', function (data) {
                    var elem = data.elem; //得到select原始DOM对象
                    $(elem).prev("a[lay-event='type']").trigger("click");
                });

                table.on('tool(columnTable)', function (obj) {
                    var data = obj.data;


                    var tr = obj.tr;

                    if (obj.event === 'optBar2RemoveColumn') {

                        //removeColumn();

                        layer.confirm('真的删除行么？', function (index) {

                            obj.del(); //删除对应行（tr）的DOM结构，并更新缓存

                            layer.close(index);

                            // alert(index)
                            activeByType('removeEmptyTableCache');
                        });

                    } else if (obj.event === 'isRequired') {
                        var isRequiredVal = tr.find("input[name='isRequired']").prop('checked') ? 1 : 0;
                        $.extend(obj.data, {'isRequired': isRequiredVal})
                        activeByType('updateRow', obj.data);	//更新行记录对象
                    } else if (obj.event === 'isCombined') {
                        var isCombinedVal = tr.find("input[name='isCombined']").prop('checked') ? 1 : 0;
                        $.extend(obj.data, {'isCombined': isCombinedVal})
                        activeByType('updateRow', obj.data);	//更新行记录对象
                    } else if (obj.event === 'isCombinedUnique') {
                        var isCombinedUniqueVal = tr.find("input[name='isCombinedUnique']").prop('checked') ? 1 : 0;
                        $.extend(obj.data, {'isCombinedUnique': isCombinedUniqueVal})
                        activeByType('updateRow', obj.data);	//更新行记录对象
                    }else if (obj.event === 'isReturnFlag') {
                        var isReturnFlagVal = tr.find("input[name='isReturnFlag']").prop('checked') ? 1 : 0;
                        $.extend(obj.data, {'isReturnFlag': isReturnFlagVal})
                        activeByType('updateRow', obj.data);	//更新行记录对象
                    }else if (obj.event === 'isIdFlag') {
                        var isIdFlagVal = tr.find("input[name='isIdFlag']").prop('checked') ? 1 : 0;
                        $.extend(obj.data, {'isIdFlag': isIdFlagVal})
                        activeByType('updateRow', obj.data);	//更新行记录对象
                    }else if (obj.event === 'type') {
                        var select = tr.find("select[name='type']");
                        if (select) {
                            var selectedVal = select.val();
                            if (!selectedVal) {
                                layer.tips("请选择一个分类", select.next('.layui-form-select'), {tips: [3, '#ff5722']}); //吸附提示
                            }
                            $.extend(obj.data, {'type': selectedVal});
                            activeByType('updateRow', obj.data);	//更新行记录对象
                        }

                    }

                });


                form.on('radio(largeTaskType2)', function (data) {

                    // formSelects.value('executePlatforms', []);
                    formSelects.disabled('executePlatforms');
                });


                form.on('radio(largeTaskType1)', function (data) {

                    formSelects.undisabled('executePlatforms');

                });

                form.on('radio(type1)', function (data) {

                    $('#cronExpression').removeClass("layui-disabled").prop("disabled", false);

                });

                form.on('radio(type2)', function (data) {

                    $('#cronExpression').addClass("layui-disabled").prop("disabled", true);

                });
            });
        }
    });
}// openTask end

function addComboColumn(){
    var divNode = $("#column-box");
    var html = "<div class=\"layui-input-inline\"style=\"width:100px\"><input name=\"comboColumns\" autocomplete=\"off\" class=\"layui-input \"/></div>"
    divNode.append(html);
}

function deleteComboColumn(){
    var divNode = $("#column-box");
    divNode.children().last().remove();
}







