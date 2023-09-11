/**
 * 用户管理
 */
var pageCurr;
var form;
var table;
var upload;
var laydate;
var element;
var xmSelect;
var totalNum;
var globalClientVersion;
var timer;
var $locations = [];
var resetAll =0;
var change =0;

$(function() {






    layui.config({
        base: '/js/',
    })
    $.ajax({//首次加载第1页
        url :'/getVersionList',
        data : {
            'pageNum' : 1,
            'pageSize' : 10
        },
        type : 'post',
        dataType : 'json',
        //async: false,
        success : function(data){
            console.log(" 首次加载第一页")
            totalNum=data["totals"]
            test(data.list);
            loadPage();
        }
    })







    layui.use(['treeTable','upload','table','laydate','element'], function(){
        table = layui.table;
        form = layui.form;
        upload = layui.upload;
        laydate = layui.laydate;
        element = layui.element;

        laydate.render({
            elem: '#version'
        });






        //监听提交
        form.on('submit(versionAddSubmit)', function(data){
            // TODO 校验
            formSubmit(data);
            return false;
        });


        form.on('submit(fileVersionAddSubmit)', function(data){
            // TODO 校验
            console.log("添加子级",data.field)
            formSubmit(data);
            return false;
        });








        form.on('switch(statusFilter)', function(data){

            var id = data.value;
            var status = this.checked ? STATUS_VALID : STATUS_INVALID;
            $.ajax({
                type: "POST",
                data: {id: id,status: status,updateLimit:-1},
                url: "/updateVersion",
                success: function (data) {
                    //{"code":"SUCCESS","msg":"更新成功","data":1}
                    if (data.code == SUCCESS_CODE) {
                        layer.msg(data.msg, { shift: -1, time: 600 },function(){
                            layer.closeAll();
                            //load({id: id,status: status});
                        });
                    } else {
                        layer.msg(data.msg, {icon: 2});
                    }
                },
                error: function () {
                    layer.msg("操作请求错误，请您稍后再试", {icon: 2},function(){
                        layer.closeAll();
                        //加载load方法
                        //load(obj);//自定义
                    });
                }
            });

        });



        form.on('submit(searchSubmit)', function(data){
            event.preventDefault()//阻止button submit默认行为

            globalClientVersion=data.field.clientVersion



            $.ajax({//当搜索框内容变化了 相当于另一轮首次加载
                url :'/getVersionList',
                data : {
                    'clientVersion':data.field.clientVersion,
                    'pageNum' : 1,
                    'pageSize' : 10
                },
                type : 'post',
                dataType : 'json',
                //async: false,
                success : function(data){
                    console.log(" 搜索框变了 加载第一页")

                    totalNum=data["totals"]
                    test(data.list);
                    loadPage();
                }
            })





        });


        $('#sonListAction').click(function(){

            getProcessvalue();
        });

        $('#fatherListAction').click(function(){
            getProcessvalue();
        });


        var index_files = new Array() ;

        //父亲的文件上传组件
        var uploadFatherIns = upload.render({
            elem: '#fatherChooseButton'// 选择文件的按钮
            ,elemList: $('#uploadFatherList') //已选择的文件的预览tbody 准备上传
            ,url: '/uploadVersion'
            ,accept: 'file'
            ,multiple: true
            ,number: 3
            ,auto: false
            ,bindAction: '#fatherListAction'// 上传已选择的按钮
            ,data: {"clientVersion": function(){
                    return $('#clientVersion').val();
                },"id": function(){
                    return $('#fatherVersionId').val();
                }}
            ,choose: function(obj){


                uploadFatherIns.config.elem.next()[0].value = '';//很重要 修复了layui上传的一个bug https://blog.csdn.net/weixin_40425415/article/details/117534788

                var that = this;
                var files = this.files = obj.pushFile(); //将每次选择的文件追加到文件队列
                console.log(files)
                //读取本地文件
                obj.preview(function(index, file, result){

                    //console.log("choose",index,file)
                    index_files.push(index);
                    var tr = $(['<tr id="upload-'+ index +'">'
                        ,'<td>'+ file.name +'</td>'
                        ,'<td>'+ (file.size/1014).toFixed(1) +'kb</td>'
                        ,'<td>'
                        ,'<div class="layui-progress" lay-filter="progress-demo-'+ index +'"><div class="layui-progress-bar" lay-percent=""></div></div>'
                        ,'<div class="layui-progress" lay-filter="progress-demo-'+ index+"copy" +'"><div class="layui-progress-bar" lay-percent=""></div></div>'
                        ,'</td>'
                        ,'<td>'
                        ,'<button class="layui-btn layui-btn-xs demo-reload layui-hide">重传</button>'
                        ,'<button class="layui-btn layui-btn-xs layui-btn-danger demo-delete">删除</button>'
                        ,'</td>'
                        ,'</tr>'].join(''));

                    //单个重传
                    tr.find('.demo-reload').on('click', function(){
                        obj.upload(index, file);
                    });

                    //删除
                    tr.find('.demo-delete').on('click', function(){
                        delete files[index]; //删除对应的文件
                        tr.remove();
                        uploadFatherIns.config.elem.next()[0].value = ''; //清空 input file 值，以免删除后出现同名文件不可选
                    });

                    that.elemList.append(tr);
                    element.render('progress'); //渲染新加的进度条组件


                });
            }
            ,done: function(res, index, upload){ //成功的回调

                if (res.code == ERROR_CODE) {
                    layer.msg(res["msg"], { icon:2 },function(){
                    });
                }
                if (res.code == SUCCESS_CODE) {
                    layer.msg(res["msg"], { icon:1 },function(){
                    });
                }
                fatherMapTable.reload()
                var that = this;
                //element.progress('progress-demo-'+ index, '100%');
                //if(res.code == 0){ //上传成功
                var tr = that.elemList.find('tr#upload-'+ index)
                    ,tds = tr.children();
                tds.eq(3).html(''); //清空操作
                delete this.files[index]; //删除文件队列已经上传成功的文件
                return;
                //}
                this.error(index, upload);
            }
            ,allDone: function(obj){ //多文件上传完毕后的状态回调
                clearInterval(timer);
                fatherMapTable.reload()
                //load();
                $.each(index_files,function(i,value){
                    // alert(JSON.stringify(value))
                    console.log("index_files")
                    $('#upload-'+ value).html('');
                });
                index_files = new Array();

                //上传后重置上传文件名和上传进度
              /*  $.post('/endProcess',function(data){

                });*/
            }
            ,error: function(index, upload){ //错误回调
                clearInterval(timer);
                var that = this;
                var tr = that.elemList.find('tr#upload-'+ index)
                    ,tds = tr.children();
                tds.eq(3).find('.demo-reload').removeClass('layui-hide'); //显示重传
            }
            // ,progress: function(n, elem, e, index){ //注意：index 参数为 layui 2.6.6 新增
            //     element.progress('progress-demo-'+ index_file, n + '%'); //执行进度条。n 即为返回的进度百分比
            // }
        });


        //儿子的文件上传组件
        var uploadSonIns = upload.render({
            elem: '#sonChooseButton'
            ,elemList: $('#uploadSonList') //列表元素对象
            ,url: '/uploadVersion'
            ,accept: 'file'
            ,multiple: true
            ,number: 3
            ,auto: false
            ,bindAction: '#sonListAction'
            ,data: {"clientVersion": function(){
                    return $('#sonClientVersion').val();
                },"clientFileVersion": function(){
                    return $('#sonClientFileVersion').val();
                },"id": function(){
                    return $('#sonVersionId').val();
                }}
            ,choose: function(obj){

                uploadSonIns.config.elem.next()[0].value = '';//很重要 修复了layui上传的一个bug https://blog.csdn.net/weixin_40425415/article/details/117534788


                var that = this;
                var files = this.files = obj.pushFile(); //将每次选择的文件追加到文件队列



                //读取本地文件
                obj.preview(function(index, file, result){
                    index_files.push(index);
                    var tr = $(['<tr id="upload-'+ index +'">'
                        ,'<td>'+ file.name +'</td>'
                        ,'<td>'+ (file.size/1014).toFixed(1) +'kb</td>'
                        ,'<td>'
                        ,'<div class="layui-progress" lay-filter="progress-demo-'+ index +'"><div class="layui-progress-bar" lay-percent=""></div></div>'

                        ,'<div class="layui-progress" lay-filter="progress-demo-'+ index+"copy" +'"><div class="layui-progress-bar" lay-percent=""></div></div>'
                        ,'</td>'
                        ,'<td>'
                        ,'<button class="layui-btn layui-btn-xs demo-reload layui-hide">重传</button>'
                        ,'<button class="layui-btn layui-btn-xs layui-btn-danger demo-delete">删除</button>'
                        ,'</td>'
                        ,'</tr>'].join(''));

                    //单个重传
                    tr.find('.demo-reload').on('click', function(){
                        obj.upload(index, file);
                    });

                    //删除
                    tr.find('.demo-delete').on('click', function(){
                        delete files[index]; //删除对应的文件
                        tr.remove();
                        uploadSonIns.config.elem.next()[0].value = ''; //清空 input file 值，以免删除后出现同名文件不可选
                    });

                    that.elemList.append(tr);
                    element.render('progress'); //渲染新加的进度条组件

                    console.log("uploadFileListIns加载了")


                });
            }
            ,done: function(res, index, upload){ //单个文件上传的回调

                if (res.code == ERROR_CODE) {
                    layer.msg(res["msg"], { icon:2 },function(){
                    });
                }
                if (res.code == SUCCESS_CODE) {
                    layer.msg(res["msg"], { icon:1 },function(){
                    });
                }
                sonMapTable.reload()


                var that = this;
                //element.progress('progress-demo-'+ index, '100%');
                //if(res.code == 0){ //上传成功
                var tr = that.elemList.find('tr#upload-'+ index)
                    ,tds = tr.children();
                tds.eq(3).html(''); //清空操作
                delete this.files[index]; //删除文件队列已经上传成功的文件
                return;
                //}
                this.error(index, upload);
            }
            ,allDone: function(obj){ //多文件上传完毕后的状态回调
                console.log(obj)
                clearInterval(timer);
                sonMapTable.reload()
                $.each(index_files,function(i,value){
                    // alert(JSON.stringify(value))
                    $('#upload-'+ value).html('');
                });
                index_files = new Array();


                //上传后重置上传文件名和上传进度
             /*   $.post('/endProcess',function(data){

                });*/
            }
            ,error: function(index, upload){ //错误回调
                clearInterval(timer);
                var that = this;
                var tr = that.elemList.find('tr#upload-'+ index)
                    ,tds = tr.children();
                tds.eq(3).find('.demo-reload').removeClass('layui-hide'); //显示重传
            }
            // ,progress: function(n, elem, e, index){ //注意：index 参数为 layui 2.6.6 新增
            //     element.progress('progress-demo-'+ index_file, n + '%'); //执行进度条。n 即为返回的进度百分比
            // }
        });

    });




});


function test(data){
    // layui.config({
    //     base: 'plug-in/test/',
    // })

    console.log(data)
    layui.use(['laypage', 'treeTable', 'layer', 'code', 'form'],function(){
        var o = layui.$,
            form = layui.form,
            laypage = layui.laypage,
            layer = layui.layer,
            treeTable = layui.treeTable;
        re = treeTable.render({
            elem: '#tree-table',
            data: data,


            icon_key: 'clientVersion',
            end: function(e){
                form.render();
            }
            ,cols: [

                {field:'status', title:'启用/禁用',align:'center',width:"100px",
                    template: function(item){

                       console.log("item",item)

                        if (item.status==STATUS_VALID)
                            return '<input type="checkbox" name="status" lay-skin="switch" checked   lay-text="开启|关闭"  value="' +item.id + '"lay-filter="statusFilter" >'
                        else
                            return '<input type="checkbox" name="status" lay-skin="switch"  lay-text="开启|关闭"  value="'+ item.id + '"lay-filter="statusFilter" >'



                    }

                },




                {
                    key: 'clientVersion',
                    title: 'clientVersion',
                    width: '50px',
                    align: 'center',

                    template: function(item){
                        if(item.clientFileVersion == null){
                            return item.clientVersion;
                        }else{
                            return '';
                        }
                    }
                },

                {
                    key: 'clientFileVersion',
                    title: 'clientFileVersion',
                    width: '50px',
                    align: 'center',
                    template: function(item){
                        if(item.clientFileVersion == null){
                            return '';
                        }else{
                            return item.clientFileVersion;
                        }
                    }



                },



                {
                    field: 'actions',
                    title: '操作',
                    width: '200px',
                    template: function(item){
                        var tem = [];





                        if(item.pid == 0){
                            tem.push('<a lay-filter="fatherUpload" class="layui-btn  layui-btn-normal layui-btn-sm" style="cursor: pointer;">父亲上传文件</a>');
                            tem.push('<a lay-filter="delete2" class="layui-btn  layui-btn-danger layui-btn-sm" style="">删除</a>');

                            //tem.push('<a class="add-child layui-btn layui-btn-warm layui-btn-sm" lay-filter="add" style="cursor: pointer;">添加子级</a>');
                        }
                        else{
                            tem.push('<a lay-filter="sonUpload" class="layui-btn  layui-btn-warm layui-btn-sm" style="cursor: pointer;">儿子上传文件</a>');

                        }
                        return tem.join('')
                    },
                },
            ]
        });

        // 监听checkbox选择
        treeTable.on('tree(box)',function(data){
            console.log(data)
            //layer.msg(JSON.stringify(data));
        })


        // 监听删除delete2
        treeTable.on('tree(delete2)',function(e){
            console.log(e.item)


            layer.confirm('您确定要删除' + e.item.id + '的版本吗？', {
                btn: ['确认', '返回'] //按钮
            }, function () {



                $.ajax({
                    type: "POST",
                    data: {id:e.item.id,clientVersion:e.item.clientVersion,clientFileVersion:e.item.clientFileVersion},
                    url: "/deleteVersionById",
                    success: function (data) {


                        if (data.code == ERROR_CODE) {
                            layer.msg(data["msg"], { icon:2 },function(){
                                layer.closeAll();
                                load();
                            });
                        }
                        if (data.code == SUCCESS_CODE) {
                            layer.msg(data["msg"], { icon:1 },function(){
                                layer.closeAll();
                                load();
                            });
                        }


                    }
                });



            }, function () {
                layer.closeAll();
            });

        })

        treeTable.on('tree(add)',function(e){
            console.log(e)
            console.log(e.item.id)
            console.log(e.item.clientVersion)

            addFileVersion(e.item.clientVersion,e.item.id)

            // layer.msg('添加操作');
            // // console.log(e.item.id)
            // //return;
            // var d = {'type':'addSonMenu','note':e.item.id};
            // editWindow("#setMenuLeft_addSonMenuTpl",d)
            // form.render()
        })


        console.log("创建了监听")
        treeTable.on('tree(fatherUpload)',function(e){


            console.log(e.item)
            $("#clientVersion").val(e.item.clientVersion);

            //$("#fatherUpdateLimit").val()==""? $("#fatherUpdateLimit").val(e.item.updateLimit):
            //console.log($("#fatherUpdateLimit").val())
           // $("#fatherUpdateLimit").val(e.item.updateLimit);//第一次打开需要从treeTable读值;修改后再打开就不应该读了
            $("#fatherVersionId").val(e.item.id);


                layer.open({
                    type:1,
                    title: '父亲文件上传',
                    fixed:false,
                    resize :false,
                    shadeClose: true,
                    area: ['850px','80%'],
                    content:$('#fatherVersionLayer'),



                    success: function () {

                        $.ajax({
                            type: "POST",
                            data: {versionId:e.item.id},
                            url: "/getVersionById",
                            success: function (data) {
                                //console.log(data.content.updateLimit)
                                $("#fatherUpdateLimit").val(data.content.updateLimit);
                                //layer.msg("修改成功", { shift: -1, time: 600 })

                            }
                        });






                         fatherMapTable=table.render({
                            elem: '#fatherFileMapTable',
                            url:'/getVersionFileMap',
                            method: 'post', //默认：get请求

                            where: {"versionId": e.item.id},

                            response:{
                                statusName: 'code', //数据状态的字段名称，默认：code
                                statusCode: 200, //成功的状态码，默认：0
                                countName: 'totals', //数据总数的字段名称，默认：count
                                dataName: 'list' //数据列表的字段名称，默认：data
                            },


                            cols: [[

                                // {field: 'id', title: 'IDDD', hide: true}
                                // ,{field: 'id', title: 'IDDD', hide: true}
                                 {field: 'id', title: 'ID',hide:true}
                                 ,{field: 'version_id', title: 'version_id',hide:true}
                                ,{field:'file_name', title:'file_name',align:'center'}
                                // ,{field:'client_file_path', title:'client_file_path',align:'center', edit: 'text'}
                                , {  title: 'xxx',edit: 'text',align:'center',templet: function(d){ //TODO 这里加入field会导致bug；


                                        var client_file_path_temp= typeof(d.client_file_path) == "undefined"? "":d.client_file_path

                                        return client_file_path_temp


                                             + '</div>'
                                             +'<div class=""><a class="layui-btn updateMapButton layui-btn-xs" >确认</a></div>'//这里定义了hideClass
                                    }}

                                //,{title:'xxx',edit:'text',align: 'center',templet: '#introduceHtml' }






                                // ,{field:'clientVersion', title:'客户端版本',align:'center',width:"10%"}
                                // ,{field:'clientFileVersion', title:'客户端文件版本',align:'center',width:"15%"}
                                // ,{field:'updateLimit', title:'最大更新限制',align:'center',width:"15%"}
                                ,{title:'操作',align:'center', toolbar:'#optBarUpdateFileMap'}
                            ]],

                             done: function(){



                                 form.on('submit(fatherUpdateLimitSubmit)', function(data){
                                     // TODO 校验



                                     $.ajax({
                                         type: "POST",
                                         data: {id:data.field.fatherVersionId,updateLimit:data.field.fatherUpdateLimit},
                                         url: "/updateVersion",
                                         success: function (data) {
                                             layer.msg("修改成功", { shift: -1, time: 600 })
                                             //TODO 此处应该重载TreeTable 可以用load() 但是搜索条件页数什么就没了



                                         }
                                     });



                                     return false;//阻止表单跳转
                                 });

                                 var $div = $('.updateMapButton');
                                 $div.each(function( index ) {

                                     $(this).on('click',function (e) {

                                         e.stopPropagation();
                                         var $closest=$(this).closest('tr')
                                         var $find=$closest.find('[data-field="id"]')
                                         var $findDiv=$find.find('div')
                                         var fileId=$findDiv.text();
                                         var clientFilePath= $(this).parent().prev().text();

                                         $.ajax({
                                             type: "POST",
                                             data: {id:fileId,clientFilePath:clientFilePath},
                                             url: "/updateVersionFileMap",
                                             success: function (data) {
                                                 layer.msg("修改成功", { shift: -1, time: 600 })
                                             }
                                         });

                                     })
                                 });
                             }
                        });






                        table.on('tool(fatherFileMapTable)', function (obj) {
                           if (obj.event === 'deleteFile') {
                                console.log(obj.data)
                                console.log(e.item.clientVersion)
                                $.ajax({
                                    type: "POST",
                                    data: { fileId: obj.data.id, fileName:obj.data.file_name ,clientVersion:e.item.clientVersion},
                                    url: "/deleteFile",
                                    success: function (data) {


                                        if (data.code == ERROR_CODE) {
                                            layer.msg(data["msg"], { icon:2 },function(){
                                            });
                                        }
                                        if (data.code == SUCCESS_CODE) {
                                            layer.msg(data["msg"], { icon:1 },function(){
                                            });
                                        }


                                        fatherMapTable.reload()



                                    }
                                });
                            }
                        });

                    },


                    end:function(){
                        cleanUser();
                    }
                });



        })


        treeTable.on('tree(sonUpload)',function(e){

            console.log(e)

            $("#sonClientVersion").val(e.item.clientVersion);
            $("#sonClientFileVersion").val(e.item.clientFileVersion);
            $("#sonVersionId").val(e.item.id);
            if(change == 1){
                if(resetAll == 1){
                    $("input[name=resetAll][value='1']").prop("checked","true");
                }else {
                    $("input[name=resetAll][value='0']").prop("checked","true");
                }
            }else {
                if(e.item.resetAll == 1){
                    $("input[name=resetAll][value='1']").prop("checked","true");
                }else {
                    $("input[name=resetAll][value='0']").prop("checked","true");
                }
            }

            form.render();
            layer.open({
                type:1,
                // id:'sonNotUploading',
                title: '儿子文件上传(无内容)',
                fixed:false,
                resize :false,
                shadeClose: true,
                area: ['850px','80%'],
                content:$('#sonVersionLayer'),
                end:function(){
                    cleanUser();
                },
                success:function () {


                    $.ajax({
                        type: "POST",
                        data: {versionId:e.item.id},
                        url: "/getVersionById",
                        success: function (data) {

                            console.log("data",data)


                            console.log("datacontent",data.content)
                            $("#sonUpdateLimit").val(data.content.updateLimit);
                            $("#sonClientFileVersion").val(data.content.clientFileVersion);
                            //layer.msg("修改成功", { shift: -1, time: 600 })

                        }
                    });

                    sonMapTable=table.render({
                        elem: '#sonFileMapTable',
                        url:'/getVersionFileMap',
                        method: 'post', //默认：get请求

                        where: {"versionId": e.item.id},

                        response:{
                            statusName: 'code', //数据状态的字段名称，默认：code
                            statusCode: 200, //成功的状态码，默认：0
                            countName: 'totals', //数据总数的字段名称，默认：count
                            dataName: 'list' //数据列表的字段名称，默认：data
                        },


                        cols: [[

                            // {field: 'id', title: 'IDDD', hide: true}
                            // ,{field: 'id', title: 'IDDD', hide: true}
                             {field: 'id', title: 'ID',hide:true}
                             ,{field: 'version_id', title: 'version_id',hide:true}
                            ,{field:'file_name', title:'file_name',align:'center'}

                            , {  title: 'file_name',edit: 'text',align:'center',width:200,templet: function(d){ //TODO 这里加入field会导致bug；


                                    var file_name_temp= typeof(d.file_name) == "undefined"? "":d.file_name

                                    return file_name_temp

                                        + '</div>'
                                        +'<pp class="" ><a class="layui-btn updateMapButton_2 layui-btn-xs" >确认</a></pp>'//这里定义了hideClass
                                }}


                            , {  title: 'xxx',edit: 'text',align:'center',width:200,templet: function(d){ //TODO 这里加入field会导致bug；


                                    var client_file_path_temp= typeof(d.client_file_path) == "undefined"? "":d.client_file_path

                                    return client_file_path_temp

                                        + '</div>'
                                        +'<pp class="" ><a class="layui-btn updateMapButton layui-btn-xs" >确认</a></pp>'//这里定义了hideClass
                                }}
                            // ,{field:'clientVersion', title:'客户端版本',align:'center',width:"10%"}
                            // ,{field:'clientFileVersion', title:'客户端文件版本',align:'center',width:"15%"}
                            // ,{field:'updateLimit', title:'最大更新限制',align:'center',width:"15%"}
                            ,{title:'操作',align:'center', toolbar:'#optBarUpdateFileMap'}
                        ]],



                        done: function(){

                            form.on('submit(sonUpdateLimitSubmit)', function(data){
                                $.ajax({
                                    type: "POST",
                                    url: "/updateVersion",
                                    data: {id:data.field.sonVersionId,updateLimit:data.field.sonUpdateLimit},
                                    success: function (data) {
                                        layer.msg("修改成功", { shift: -1, time: 600 })

                                    }
                                });
                                return false;//阻止表单跳转
                            });


                            form.on('submit(sonVersionSubmit)', function(data){

                                var divD = document.getElementById("fileLoading");
                                divD.style.display="block";
                                console.log(data.field)
                                $.ajax({
                                    type: "POST",
                                    data: {id:data.field.sonVersionId,clientFileVersion:data.field.sonClientFileVersion,updateLimit:-1,resetAll:$('input[name="resetAll"]:checked').val()},
                                    url: "/updateVersion",
                                    success: function (data) {
                                        divD.style.display="none";
                                        var divSuccess = document.getElementById("upadteSuccess");
                                        divSuccess.style.display="block";
                                        layer.msg("修改成功", { shift: -1, time: 600 });
                                        setTimeout(function () {
                                            layer.closeAll();
                                            divSuccess.style.display="none";
                                        }, 1000);
                                         resetAll = $('input[name="resetAll"]:checked').val();
                                         change = 1;
                                    }
                                });
                                return false;//阻止表单跳转
                            });

                            form.on('submit(sonFileMapSubmit)', function(data){

                                console.log(data.field)
                                $.ajax({
                                    type: "POST",
                                    data: {versionId:data.field.sonVersionId,fileName:data.field.fileName,clientFilePath:data.field.clientFilePath},
                                    url: "/addVersionFileMap",
                                    success: function (data) {
                                        layer.msg("修改成功", { shift: -1, time: 600 })

                                        sonMapTable.reload()

                                    }
                                });
                                return false;//阻止表单跳转
                            });









                            var $div = $('.updateMapButton');
                            $div.each(function( index ) {


                                $(this).on('click',function (e) {

                                    e.stopPropagation();
                                    var $closest=$(this).closest('tr')
                                    var $find=$closest.find('[data-field="id"]')
                                    var $findDiv=$find.find('div')
                                    var fileId=$findDiv.text();
                                    var clientFilePath= $(this).parent().prev().text();

                                    $.ajax({
                                        type: "POST",
                                        data: {id:fileId,clientFilePath:clientFilePath},
                                        url: "/updateVersionFileMap",
                                        success: function (data) {
                                            layer.msg("修改成功", { shift: -1, time: 600 })
                                        }
                                    });

                                })
                            });



                            var $div_2 = $('.updateMapButton_2');
                            $div_2.each(function( index ) {


                                $(this).on('click',function (e) {

                                    e.stopPropagation();
                                    var $closest=$(this).closest('tr')
                                    var $find=$closest.find('[data-field="id"]')
                                    var $findDiv=$find.find('div')
                                    var fileId=$findDiv.text();
                                    var fileName= $(this).parent().prev().text();

                                    //console.log("he",fileName)

                                    $.ajax({
                                        type: "POST",
                                        data: {id:fileId,fileName:fileName},
                                        url: "/updateVersionFileMap",
                                        success: function (data) {
                                            layer.msg("修改成功", { shift: -1, time: 600 })
                                        }
                                    });

                                })
                            });

                        }



                    });



                    table.on('tool(sonFileMapTable)', function (obj) {
                         if (obj.event === 'deleteFile') {
                            console.log(obj.data)
                            console.log(e.item)
                            console.log(e.item.clientVersion)
                            $.ajax({
                                type: "POST",
                                data: { fileId: obj.data.id},
                                url: "/deleteFileMap",
                                success: function (data) {

                                    if (data.code == ERROR_CODE) {
                                        layer.msg(data["msg"], { icon:2 },function(){
                                        });
                                    }
                                    if (data.code == SUCCESS_CODE) {
                                        layer.msg(data["msg"], { icon:1 },function(){
                                        });
                                    }

                                    sonMapTable.reload()



                                }
                            });
                        }
                    });

                }
            });

            //TODO 开始上传了再克隆








        })






    });
}//test end

function loadPage(){

    layui.use(['laypage', 'layer'],function(){
        var laypage = layui.laypage
            ,layer = layui.layer;

        //var total = $('#total').val();

        //分页
        console.log("开始分页",totalNum)
        laypage.render({





            elem: 'pageDemo'
            ,count: totalNum

            ,limit: 10
            ,skin: '#1E9FFF'
            ,layout: ['count', 'prev', 'page', 'next', 'limit', 'skip']
            ,jump: function(obj, first){//first是否首次加载






                if(!first){
                    $.ajax({
                        url :'/getVersionList',
                        data : {
                            'clientVersion':globalClientVersion,
                            'pageNum' : obj.curr,
                            'pageSize' : obj.limit
                        },
                        type : 'post',
                        dataType : 'json',
                        success : function(data){

                            console.log(" load里面的ajax")

                            test(data.list);
                        }
                    });
                }
            }











        });
    })



}

//提交表单
function formSubmit(obj){


    console.log(obj)
    console.log(obj.field)

    var data1={
        fatherVersion:{
            clientVersion:obj.field.clientVersion,
            updateLimit:obj.field.updateLimit
        },
        sonVersion:{
            clientVersion:obj.field.clientVersion,
            clientFileVersion:obj.field.clientVersion_2,
            updateLimit:obj.field.updateLimit_2
        }

    }

    $.ajax({
        type: "POST",
        data: JSON.stringify(data1),
        url: "/addVersion",
        contentType:"application/json",
        success: function (data) {


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


function addVersion(){
    // $("#version").removeAttr("disabled");
    // $("#uploadVersion").hide();
    // $("#uploadVersionSubmit").show();
    // $("#id").val("");
    // $("#setVersionId").hide();
    layer.open({
        type:1,
        title: '版本添加',
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['450px','400px'],
        content:$('#addVersion'),

        success:function (){

        },
        end:function(){
            cleanUser();
        }
    });

}



function addFileVersion(clientVersion,pid){


    $("#clientVersionUpdate2").val(clientVersion);
    $("#pid").val(pid);

    layer.open({
        type:1,
        title: '二级添加',
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['450px','350px'],
        content:$('#addFileVersion'),

        success:function (){



        },
        end:function(){
            cleanUser();
        }
    });

}






function load(obj) {


    //重新加载大版本下拉栏


    //这里不应该只有tableIns被重载 我的表格和laypage都应该重载
    $.ajax({
        url :'/getVersionList',
        data : {
            'pageNum' : 1,
            'pageSize' : 10
        },
        type : 'post',
        dataType : 'json',
        //async: false,
        success : function(data){
            totalNum=data["totals"]
            test(data.list);
            loadPage();
        }
    })





}
function getProcessvalue(){
    //3秒请求一次进度条的数据
    timer = setInterval(function () {
        $.post('/getProcess',function(data){


            for (let dataKey in data) {

                var datavalue=data[dataKey]
                console.log(dataKey,datavalue)
                var currentUploadFileName=dataKey;
                var tdEle = $($("td:contains("+currentUploadFileName+")").parent().find(".layui-progress")[0]).attr("lay-filter")
                var tdEleCopy = $($("td:contains("+currentUploadFileName+")").parent().find(".layui-progress")[1]).attr("lay-filter")


                var count=0;
                for (let datavalueKey in datavalue) {

                    if(count==0){
                        element.progress(tdEle,  datavalue[datavalueKey]+'%');
                    }else {
                        element.progress(tdEleCopy,  datavalue[datavalueKey]+'%');
                    }
                    count++;


                }
            }



        });
    }, 3000);
}



function cleanUser(){
    $("#id").val("");
    $("#systemName").val("");
    $("#invokeTimesMonth").val("");
    $("#invokeInterval").val("");
    $("#invokeTimes").val("");
    $("#taskTypeName").val("");
    $("#callbackAddress").val("");
}



