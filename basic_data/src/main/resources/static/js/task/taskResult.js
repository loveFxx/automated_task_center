/**
 * 任务库列表管理
 * loadPage在首次进入页面和搜索后需要重新加载 通过pageLoaded来判断
 */
var form;
var table;
//每次首次进入会设为0 触发搜索后也会设为0
var curPageMaxId=0;
var curPageMinId=0;
//每次重载loadPage都会重置lastPage
var lastPage;
var maxId;
$(function() {

    var afterUrl =  window.location.search.substring(1);
    if (afterUrl != ""){
        $("#taskSourceId").val(afterUrl);
    };

    formSelects.data('taskTypeSearch', 'server', {
        url: '/getDropDownTaskName',
        direction: 'down',
    });
    //首次请求maxId
    $.ajax({
        url :'/getMaxTaskResultId',
        data : {},
        type : 'post',
        success : function(data){
            maxId=data;
            loadLaypage();
        }
    })



    layui.use('table', function(){
        table = layui.table;
        form = layui.form;
        console.log("65,table start rendering")
        tableIns=table.render({
            elem: '#taskResultTable',
            url:'/getTaskResultList',
            method: 'post', //默认：get请求
            where: {"taskSourceId": afterUrl},
            cellMinWidth: 80,
            response: RESPONSE_BODY,
            cols: [[
                {field:'id', title:'id',align:'center',width:"15%"},
                {field:'', title:'任务库ID/客户端返回码',align:'center',width:"20%",templet: function (data) {
                        if(data.taskSourceId == null && data.clientCode == null){
                            return;
                        }
                        return data.taskSourceId+'<br>' + data.clientCode
                    }}
                ,{field:'', title:'任务缓冲区ID/任务类型',align:'center',width:"20%",templet: function (data) {
                        if(data.taskBufferId == null && data.taskType == null){
                            return;
                        }
                        return data.taskBufferId+'<br>' + data.taskType
                    }}

                ,{field:'', title:'resultType/needRetry/errorLevel/clientMsg/clientError/retryTimes',align:'center',width:"20%",templet: function (data) {
                        if(data.resultType == null && data.needRetry == null && data.errorLevel == null){
                            return;
                        }
                        return data.resultType+'/'+ data.needRetry+"/"+data.errorLevel+'<br>'+data.clientMsg+'/'+ data.clientError+"/"+data.retryTimes
                    }}
                ,{field: '', title: '返回结果', align: 'center',width:"20%",
                    templet: function (d) {
                        return JSON.stringify(d.clientResult)
                    }
                }
                ,{field: '', title: '请求参数', align: 'center',width:"20%",
                    templet: function (d) {
                        return JSON.stringify(d.returnParams)
                    }
                }
                ,{field:'refreshTime', title:'refreshTime',align:'center',width:"16%"}
                ,{field:'createdTime', title:'createdTime',align:'center',width:"16%"}
            ]],
            //首页和每次翻页后都会执行done 更新当前页最大最小id
            done: function(res, curr){
                console.log("非搜索的table render over")
                if(res["list"]===null){
                }else {
                    var list=res["list"]
                    var length=list.length
                    curPageMaxId=list[0]["id"]
                    curPageMinId=list[length-1]["id"]
                }
                console.log(136,"curPageMinId,curPageMaxId",curPageMinId,curPageMaxId)
            }
        });

    });




    //搜索框 每次搜索相当于重新进一次首页
    layui.use(['form','laydate'], function(){
        var form = layui.form ,layer = layui.layer;
        //监听搜索框
        form.on('submit(searchSubmit)', function(data){
            console.log(154,"监听搜索框")
            $('[lay-id="taskResultTable"]').hide();
            $('#page').hide();
            var loading = layer.load(0, {
                shade: false,
            });

            loadLaypage();

            layer.close(loading);
            $('[lay-id="taskResultTable"]').show();
            $('#page').show();
            var whereData = {};
            whereData.curPageMinId=null;
            whereData.curPageMaxId=null;
            whereData.taskSourceId=data.field.taskSourceId;
            whereData.taskType=data.field.taskType;
            whereData.taskBufferId=data.field.taskBufferId;
            tableIns.reload({
                where: whereData ,
            });
            return false;
        });
    });
});



//加载laypage组件
function loadLaypage(){
    layui.use('laypage', function(){
        var laypage = layui.laypage;
        laypage.render({
            layout:['prev', 'count', 'page', 'next'],
            elem: 'page'
            ,count: maxId //数据总数，从服务端得到
            ,jump: function(obj, first){
                removeJumpButton();
                //首次不执行
                if(!first){
                    if(obj.curr>lastPage){
                        console.log("点击了下一页从%s到%s",lastPage,obj.curr)
                        curPageMaxId=null;
                    }else{
                        console.log("点击了上一页从%s到%s",lastPage,obj.curr)
                        curPageMinId=null;
                    }
                    lastPage=obj.curr;
                    console.log(175,"翻页类型的reload")
                    tableIns.reload({
                        where: {curPageMinId:curPageMinId,curPageMaxId:curPageMaxId} ,
                    });
                }else{
                    //每次进入首页重载layPage都会重置lastPage
                    lastPage=1;
                }
            }
        });

    });
    console.log("loadLaypage Over")
}


//删除跳页按钮 只能上一页下一页
function removeJumpButton(){
    $(".layui-laypage").children().each(function(i,n){
        var obj = $(n)
        if(!(obj.hasClass("layui-laypage-curr")||obj.hasClass("layui-laypage-prev")||obj.hasClass("layui-laypage-next")||obj.hasClass("layui-laypage-count"))){
            obj.remove()
        }
    });
}
