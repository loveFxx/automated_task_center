/**
 * 任务监控
 */
var form;
var pageCurr;
var table;
var element;
var xmSelect;
$(function() {

    layui.use(['table','element'], function(){
        table = layui.table;
        form = layui.form;
        element = layui.element;

        tableIns=table.render({
            elem: '#taskFailMonitor',
            url:'/getTaskExecutedException',
            method: 'post', //默认：get请求
            page: true,
            request: REQUEST_BODY,
            response:{
                statusName: 'code', //数据状态的字段名称，默认：code
                statusCode: 200, //成功的状态码，默认：0
                countName: 'totals', //数据总数的字段名称，默认：count
                dataName: 'list' //数据列表的字段名称，默认：data
            },
            cols: [[
                {field:'id', title:'编号',align:'center',width:120}
                ,{field:'taskType', title:'任务类型',align:'center',width:150}
                ,{field:'error', title:'报错',align:'center',width:500}
                ,{field:'num', title:'数量',align:'center',width:100}
                ,{field:'createdAt', title:'创建时间',align:'center',width:180}
            ]],
            done: function (res, curr) {
                //$('.layui-table').css("width","100%");
                curr = res.pageNum;
                pageCurr=curr;
                var pageNum = $(".layui-laypage-skip").find("input").val();
                $("#pageNum").val(pageNum);
            }
        });

    });

    //搜索框
    layui.use(['form','laydate'], function(){
        var form = layui.form ,layer = layui.layer;
        //TODO 数据校验
        //监听搜索框
        form.on('submit(searchSubmit)', function(data){
            var taskee= {};
            if(data.field.taskTypeSearch!=''){
                var taskTypeStr=data.field.taskTypeSearch.toString();
                // taskTypeStr=taskTypeStr.replace(/,/g,"','");
                // taskTypeStr="('"+taskTypeStr+"')";
                taskee.taskType=taskTypeStr;
            }

            //防止layui表格reload where参数保留
            if(data.field.taskNameSearch=='')
                taskExecutedException.taskTypes=null;

            tableIns.reload({
                where: taskee,
                page: {
                    curr: pageCurr //从当前页码开始
                }
            });
            return false;
        });
    });
    loadSelects();

});

function loadSelects() {
    formSelects.data('taskTypeSearch', 'server', {
        url: '/getFailTaskType',
        direction: 'down',
    });
}



