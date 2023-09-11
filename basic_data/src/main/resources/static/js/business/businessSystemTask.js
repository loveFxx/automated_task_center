var pageCurr;
var form;
var table;
var xmSelect;
var lastData;
$(function() {
    layui.use('xmSelect', function(){
        xmSelect = layui.xmSelect;
    });
    var afterUrl =  window.location.search.substring(1);
    var afterEqual = afterUrl.substring(afterUrl.indexOf('=')+1).toUpperCase();
    layui.use('table', function() {
        table = layui.table;
        form = layui.form;
        tableIns=table.render({
            elem: '#businessSystemTaskList',
            url:'/businessSystemTask?systemId='+afterEqual,
            method: 'post', //默认：get请求
            page: true,
            cellMinWidth: 80,
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[
                 {checkbox: true},
                {field:'status', title:'启用/禁用',align:'center',width:"10%",templet: '#statusTemp'}
                ,{field:'systemId', title:'系统Id', align:'center',width:"10%"}
                ,{field:'taskName', title:'任务名称',align:'center',width:240}
                ,{field:'systemName', title:'系统名称',align:'center',width:"12%"}
                ,{field:'taskCallbackAddress', title:'回调地址',align:'center',width:"12%"}
                ,{field:'networkType', title:'网络连接类型',align:'center',width:"12%"}
                ,{title:'操作',align:'center', toolbar:'#optBar'}
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
    });

});