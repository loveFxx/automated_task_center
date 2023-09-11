/**
 * 用户管理
 */
var pageCurr;
var form;
var table;
var tips_index;
var lastData;

$(function() {
    layui.use('table', function(){
        table = layui.table;
        form = layui.form;
        tableIns=table.render({
            elem: '#proxyIPList',
            url:'/getProxyIpList',
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: true,
            request: REQUEST_BODY,
            response: RESPONSE_BODY,
            cols: [[
                {checkbox: true},
                {field: 'id', title: 'ID', hide: true},
                {field: 'validStatus', title: '启用/禁用', align: 'center', width: "10%", templet: '#statusTemp'},
                {field:'', title:'代理IP/端口',align:'center', width:"18%",templet: function(d){
                        if(d.ip==null || d.ip==''){
                            return ""+'</span>'
                        }
                        if(d.port==null || d.port==''){
                            return d.ip+'</span>'
                        }
                        return  d.ip +':'+ d.port+'</span>'
                    }}
                ,{field:'crawlPlatformName', title:'可爬取平台',align:'center', width:"15%",templet: function(d){
                        var crawlPlatformName = d.crawlPlatformName;
                        var result = '';
                        if(crawlPlatformName == null){
                            return  result+'</span>'
                        }
                        var assistAuditArry =crawlPlatformName.split(",");
                        var count = 0;
                        $.each(assistAuditArry,function(i,value){
                            count = count+1;
                            if(count>2 && (count+1)%2 == 0){
                                result += '<br>';
                            }
                            if(count%2 == 0){
                                result += '/';
                            }
                            result +=  value;
                        });
                        return  result+'</span>'
                    }}
                ,{field:'', title: '账号平台/店铺代号/大洲',align:'center', width:"20%",templet: function(d){
                        // var proxyIPShops = d.proxyIpShops;
                        var proxyIPShops = d.accountProxies;
                        var result = '';
                        // alert(JSON.stringify(proxyIPShops))
                        $.each(proxyIPShops, function(n, value) {
                            var platform;
                            var account;
                            var continents;
                            if(value.platform==null || value.platform==''){
                                platform = '-'
                            }else {
                                platform = value.platform
                            }
                            if(value.account==null || value.account==''){
                                account = '-'
                            }else {
                                account = value.account
                            }
                            if(value.continents==null || value.continents==''){
                                continents = '-'
                            }else {
                                continents = value.continents
                            }
                            if(result != ''){
                                result += '<br>';
                            }
                            if(platform == '-' && account == '-' && continents =='-'){
                                result += '-';
                            }else {
                                result +=  platform +'/'+ account +'/'+ continents;
                            }
                        });
                        return  result+'</span>'
                    }}
                ,{field:'platformStatus', title: '平台状态',align:'center', width:"10%",templet:function (d) {
                        return  "<div    id=\""+d.id+"\" class='forHover'><span>移入查看</span></div>";
                    }}
                ,{field:'', title: '操作人/操作时间',align:'center', width:"20%",templet: function(d){
                        var username =d.updateUser;
                        var updateTime = d.updateTime;
                        if(username==null || username==''){
                            return  '-' +'/'+updateTime+'</span>'
                        }
                        return  username +'<br>'+updateTime+'</span>'
                    }}
                ,{title:'操作',align:'center', toolbar:'#optBar', width:"21%"}
            ]],
            done: function(res, curr){
                // 如果是异步请求数据方式，res即为你接口返回的信息。
                // 如果是直接赋值的方式，res即为：{data: [], count: 99} data为当前页数据、count为数据总长度
                formSelects.data('crawlPlatformSearch', 'server', {
                    url: '/getCrawlPlatform',
                    direction: 'down',
                });

                formSelects.data('platformSearch', 'server', {
                    url: '/getCrawlPlatform',
                    direction: 'down',
                });

                formSelects.data('platformPlatformSearch', 'server', {
                    url: '/getCrawlPlatform',
                    direction: 'down',
                });

                formSelects.data('accountSearch', 'server', {
                    url: '/getDropDownAccount',
                    direction: 'down',
                });

                if(lastData!=null){
                    var account = lastData.field.account;
                    if(account!=null){
                        var assistAuditArry =account.split(",");
                        formSelects.value('accountSearch', assistAuditArry);
                    }
                    var crawlPlatform = lastData.field.crawlPlatform;
                    if(crawlPlatform!=null){
                        var assistAuditArry =crawlPlatform.split(",");
                        formSelects.value('crawlPlatformSearch', assistAuditArry);
                    }
                    var platformShop = lastData.field.platformShop;
                    if(platformShop!=null){
                        var assistAuditArry =platformShop.split(",");
                        formSelects.value('platformSearch', assistAuditArry);
                    }
                    var platform = lastData.field.platform;
                    if(platform!=null){
                        var assistAuditArry =platform.split(",");
                        formSelects.value('platformPlatformSearch', assistAuditArry);
                    }
                }

                var parentName = document.getElementsByClassName('forHover');
                for (var i = 0; i < parentName.length; i++){
                    var sonJquery=$(parentName[i].parentElement.parentElement);
                    sonJquery.hover(function(){
                       var thisId=$(this).find(".forHover")[0].id
                       var platformTable= '<table id="proxyIPPlatform" name="proxyIPPlatform" lay-filter="test"></table>';
                        $("#proxyIPPlatform").remove();

                        tips_index = layer.tips(platformTable,this,{tips: [2, '#FFFFFF'], time:0});

                        table.render({
                            elem: '#proxyIPPlatform',
                            url:'/proxyIPPlatform',
                            where: {"proxyIpId":thisId},
                             method: 'post', //默认：get请求
                            request: REQUEST_BODY,
                            response: RESPONSE_BODY,
                            cols: [[
                                {field:'platformName', title:'平台',align:'center',width: 200},
                                {field:'', title:'状态/解禁时间',align:'center',width: 300,templet: function(d){
                                        var status = d.status;
                                        var banPeriod = d.banPeriod;
                                        var result = '';
                                        if(status == STATUS_VALID){
                                            return  '正常'+'</span>'
                                        }else if(status == STATUS_DISABLE){
                                            return  "封禁/"+banPeriod+'</span>'
                                        }
                                        return  "无效状态"+'</span>'
                                    }}
                            ]],
                        });

                    },function(){
                        layer.close(tips_index);
                    });
                }

                $("[data-field='platformStatus']").children().each(function(){
                    if($(this).text()=='-1'){
                        $(this).text("")
                    }else if($(this).text()=='1') {
                        $(this).text("正常")
                    }else if($(this).text()=='2') {
                        $(this).text("封禁")
                    }
                });

                //得到数据总量
                curr = res.pageNum;
                pageCurr=curr;
            }
        });

        //监听工具条
        table.on('tool(proxyIPTable)', function(obj){
            var data = obj.data;
            if(obj.event === 'del'){
                //删除
                delProxyIP(data,data.id);
            } else if(obj.event === 'edit'){
                //编辑
                openProxyIP(data,"编辑");
                formSelectsData(data);
            }
        });

        //监听提交
        form.on('submit(proxyIPSubmit)', function(data){
            // TODO 校验
            formSubmit(data);
            return false;
        });

        form.on('switch(validStatus)', function (data) {
            var proxy = {};
            var id = $(this).data("id");
            var status = this.checked ? STATUS_VALID : STATUS_INVALID;
            proxy.id = id;
            proxy.validStatus = status;
            $.ajax({
                    type: "POST",
                    dataType: "json",
                    data: JSON.stringify(proxy),
                    contentType: "application/json;charset=UTF-8",
                    url: "/updateProxyStatus",
                    success: function (data) {
                        if (data.code == SUCCESS_CODE) {
                            layer.msg(data.msg, {shift: -1, time: 600}, function () {
                                layer.closeAll();
                            });
                        } else {
                            layer.msg(data.msg, {icon: 2});
                        }
                    },
                    error: function () {
                        layer.msg("操作请求错误，请您稍后再试", {icon: 2}, function () {
                            layer.closeAll();
                        });
                    }
            });
        });

        form.on('submit(setRate)', function(data){
            var data = JSON.stringify(data.field);
            $.ajax({
                type: "POST",
                data: data,
                contentType: "application/json;charset=UTF-8",
                dataType: 'json',
                url: "/batchSetRate",
                success: function (data) {

                    if (data.code == SUCCESS_CODE) {
                        layer.msg(data.msg, { anim: 0, time: 600 },function(){
                            layer.closeAll();

                            load(obj);
                        });
                    } else {
                        layer.msg(data.msg, {icon: 2});
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
                where: data.field ,
                page: {
                    curr: pageCurr //从当前页码开始
                }
            });
            return false;
        });
    });
});

//提交表单
function formSubmit(obj){
    $.ajax({
        type: "POST",
        data: obj.field,
        url: "/updateProxyIP",
        success: function (data) {
            //{"code":"SUCCESS","msg":"更新成功","data":1}
            if (data.code == SUCCESS_CODE) {
                layer.msg(data.msg, { anim: 0, time: 600 },function(){
                    layer.closeAll();

                    load(obj);
                });
            } else {
                layer.msg(data.msg, {icon: 2});
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

//新增ip
function addProxyIP(){
    openProxyIP(null,"新增");
}


function openProxyIP(data,title){
    if(data==null || data==""){
        $("#id").val("");
    }else{
        $("#id").val(data.id);
        $("#ip").val(data.ip);
        $("#port").val(data.port);
        form.render('select'); //这个很重要
    }
    var pageNum = $(".layui-laypage-skip").find("input").val();
    $("#pageNum").val(pageNum);

    layer.open({
        type:1,
        title: title,
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['650px'],
        content:$('#setProxyIP'),
        end:function(){
            cleanProxyIP();
        }
    });
}

// 选择框回填数据
function formSelectsData(data) {
    var crawlPlatformSelect = data.crawlPlatformSelect;
    formSelects.data('crawlPlatform', 'local', {
        direction: 'down',
        arr: crawlPlatformSelect
    });
    if(data.crawlPlatform != null){
        var assistAuditArry =data.crawlPlatform.split(",");
        formSelects.value('crawlPlatform', assistAuditArry);
    }

}

function delProxyIP(obj,id) {
    if (null != id) {
        layer.confirm('您确定要删除' + obj.ip + '的帐号吗？', {
            btn: ['确认', '返回'] //按钮
        }, function () {
            $.post("/deleteProxyIP", {"id": id}, function (data) {
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


//批量删除任务
$("#batchDeleteProxyIP").bind("click",function () {
    var checkStatus = table.checkStatus('proxyIPList');
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
                url: "/batchDeleteProxyIP",
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

//刷新代理平台
$("#refreshProxyIPPlatform").bind("click",function () {
    $.ajax({
        url: "/refreshProxyIPPlatform",
        type: "post",
        contentType: "application/json;charset=UTF-8",
        dataType: 'json',
        // data: JSON.stringify({"ids": listId}),
        success: function (res) {
            if (res.code == "SUCCESS") {
                load();
            }

        }
    });
});

//刷新可爬取平台
$("#refreshCrawlPlatform").bind("click",function () {
    $.ajax({
        url: "/refreshCrawlPlatform",
        type: "post",
        contentType: "application/json;charset=UTF-8",
        dataType: 'json',
        // data: JSON.stringify({"ids": listId}),
        success: function (res) {
            if (res.code == "SUCCESS") {
                load();
            }

        }
    });
});

//初始化代理池
$("#initProxyPool").bind("click",function () {
    $.ajax({
        url: "/initProxyPool",
        type: "post",
        contentType: "application/json;charset=UTF-8",
        dataType: 'json',
        // data: JSON.stringify({"ids": listId}),
        success: function (res) {
            if (res.code == "SUCCESS") {
                load();
            }
        }
    });
});

layui.use([ 'form', 'upload'], function () {
    upload = layui.upload;
    upload.render({
        elem: '#upload' //绑定元素
        , url: '/upload'//上传接口
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


layui.use([ 'form', 'upload'], function () {
    upload = layui.upload;
    upload.render({
        elem: '#uploadPort' //绑定元素
        , url: '/uploadPort'//上传接口
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

                //obj.resetFile(index, file, '123.jpg'); //重命名文件名，layui 2.3.0 开始新增

                //这里还可以做一些 append 文件列表 DOM 的操作

                //obj.upload(index, file); //对上传失败的单个文件重新上传，一般在某个事件中使用
                //delete files[index]; //删除列表中对应的文件，一般在某个事件中使用
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


function load(obj){
    //重新加载table
    tableIns.reload({
        page: {
            curr: pageCurr //从当前页码开始
        }
    });
}

function cleanProxyIP(){
    // $("#platformStatusSearch").val("-1");
    // $("#ipSearch").val("");
}

$("#setRate").on("click",function(){
    var checkStatus = table.checkStatus('proxyIPList');
    var data = checkStatus.data;
    var ids = [];
    if (data.length > 0) {
        $.each(data, function (key,value) {
            ids.push(value.id);
        });
        $("#proxyIds").val(ids.join(","))
        layer.open({
                type:1,
                title: "设置频率",
                fixed:false,
                resize :false,
                shadeClose: true,
                area: ['800px'],
                content:$('#setRateLayer'),
                end:function(){
                    cleanProxyIP();
                }
            });

    } else {
        layer.msg("请选择需要设置的代理IP");
    }
});
