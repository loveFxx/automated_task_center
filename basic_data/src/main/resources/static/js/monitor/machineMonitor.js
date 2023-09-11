var layer;
$(function () {

    var basicDataPort = 8991;
    var monitorPort = 8999;

    //非常重要 下面都用这个
    //var url = window.location.origin.replace(basicDataPort, monitorPort)
    // console.log(url);
    var url = MONITOR_URL;
    var url = '';
    console.log(url);

    var time = new Date().toLocaleString('en-CA', {timeZone: 'Asia/Shanghai', hour12: false}).replace(',', '')

    $("#time").text("数据更新时间：" + time)


    $.ajax({
        type: "POST",
        url: url+"/CountMachineWithHeartBeatTimeout",
        success: function (data) {
                $("#span1").text(data);
        }
    });


    $.ajax({
        type: "POST",
        url: url+"/CountMachineWithoutNetWork",
        success: function (data) {
                $("#span2").text(data);
        }
    });

    $.ajax({
        type: "POST",
        url: url+"/CountMachineLackingDiskspace",
        success: function (data) {
                $("#span3").text(data);
                $("#span3").removeClass("loading");
        }
    });

    $.ajax({
        type: "POST",
        url: url+"/CountMachineLackingMemory",
        success: function (data) {
                $("#span4").text(data);
                $("#span4").removeClass("loading");

        }
    });

    $.ajax({
        type: "POST",
        url: url+"/CountMachineWithBigTimeDiff",
        success: function (data) {
                $("#span5").text(data);
                $("#span5").removeClass("loading");
        }
    });

    layui.use(['table'], function () {
        table = layui.table;

        $.ajax({
            type: "POST",
            url: url+"/getMachineTypeStat",
            success: function (data) {
                table.render({
                    elem: '#machineTypeCountTable',
                    cols: [[
                        {
                            field: '', title: '机器类型', align: "center", templet: function (d) {

                                switch (d.machineType) {
                                    case "0":
                                        return "账号机";
                                        break;
                                    case "1":
                                        return "内网VPS";
                                        break;
                                    case "2":
                                        return "外网VPS";
                                        break;
                                    case "3":
                                        return "重庆VPS";
                                        break;
                                    case "4":
                                        return "重庆账号机";
                                        break;
                                    default:
                                        return "";
                                }

                            }
                        },
                        {field: 'machineType', title: '', align: "center",hide:"true"},


                        {field: 'totalCount', title: '总数', align: "center",templet:function (d){

                                return '<div style="text-align: center;position: relative;">' +
                                    '<div style="display:inline-block; position: absolute;left: 50%;transform: translateX(-50%);">'+d.totalCount +'</div>' +
                                    '<div style="display:inline;float:right; "><a  class="XMLClass6 layui-btn layui-btn-xs" >导出xml</a></div>' +
                                    '</div>'

                            }},

                        {field: 'machineWithLivingHeartbeatCount', title: '具有有效心跳数', align: "center"},
                        {field: 'machineStatusOnCount', title: '开启机器数', align: "center"}

                    ]]
                    , done: function (res, curr, count) {


                    }
                    , data: data
                    , limit: 1000 //每页默认显示的数量
                });
            }
        });

        $.ajax({
            type: "POST",
            url: url+"/getMachineFatherVersionStat",
            success: function (data) {
                table.render({
                    elem: '#FatherVersionStatTable',
                    //id: layTableId,
                    //width:'100%',
                    cols: [[
                        {field: 'version', title: '大版本', align: "center"},


                        {field: '', title: '总数', width:"20%",align: "center",templet:function (d){

                                var total= d.accountMachineCount+d.innerVPSCount+d.overseaVPSCount+d.chongqingVpsCount+d.chongqingAccountMachineCount


                                return '<div style="text-align: center;position: relative;">' +
                                    '<div style="display:inline-block; position: absolute;left: 50%;transform: translateX(-50%);">'+total +'</div>' +
                                    '<div style="display:inline;float:right; "><a  class="XMLClass7 layui-btn layui-btn-xs" >导出xml</a></div>' +
                                    '</div>'


                            }},
                        {field: 'accountMachineCount', title: '账号机', align: "center"},
                        {field: 'innerVPSCount', title: '内网VPS', align: "center"},
                        {field: 'overseaVPSCount', title: '外网VPS', align: "center"},
                        {field: 'chongqingVpsCount', title: '重庆VPS', align: "center"},
                        {field: 'chongqingAccountMachineCount', title: '重庆帐号机', align: "center"}

                    ]]
                    , done: function (res, curr, count) {
                    }
                    , data: data
                    , limit: 1000 //每页默认显示的数量
                });
            }
        });


        $.ajax({
            type: "POST",
            url: url+"/getMachineSonVersionStat",
            success: function (data) {
                table.render({
                    elem: '#SonVersionStatTable',
                    //id: layTableId,
                    // width:200,
                    cols: [[
                        {field: 'version', title: '小版本', align: "center"},
                        {field: '', title: '总数',width:"20%", align: "center",templet:function (d){
                                var total= d.accountMachineCount+d.innerVPSCount+d.overseaVPSCount+d.chongqingVpsCount+d.chongqingAccountMachineCount


                                return '<div style="text-align: center;position: relative;">' +
                                    '<div class="xixi" style="display:inline-block; position: absolute;left: 50%;transform: translateX(-50%);">'+total +'</div>' +
                                    '<div style="display:inline;float:right; "><a   class="XMLClass8 layui-btn layui-btn-xs" >导出xml</a></div>' +
                                    '</div>'
                            }},
                        {field: 'accountMachineCount', title: '账号机', align: "center"},
                        {field: 'innerVPSCount', title: '内网VPS', align: "center"},
                        {field: 'overseaVPSCount', title: '外网VPS', align: "center"},
                        {field: 'chongqingVpsCount', title: '重庆VPS', align: "center"},
                        {field: 'chongqingAccountMachineCount', title: '重庆帐号机', align: "center"}

                    ]]
                    , done: function (res, curr, count) {
                    }
                    , data: data
                    , limit: 1000 //每页默认显示的数量
                });
            }
        });





        $(document).on('click', '.excelClass', function(){

            this.href = url+"/downloadMachineSummaryExcel"
            this.setAttribute('download',"")

        });

        $(document).on('click', '.XMLClass1', function(){

            this.href = url+"/getXMLForMachineMonitor/HeartBeatTimeout"
            this.setAttribute('download',"")

        });

        $(document).on('click', '.XMLClass2', function(){
            this.href = url+"/getXMLForMachineMonitor/NetWorkInvalid"
            this.setAttribute('download',"")

        });

        $(document).on('click', '.XMLClass3', function(){
            this.href = url+"/getXMLForMachineMonitor/lackDiskSpace"
            this.setAttribute('download',"")

        });

        $(document).on('click', '.XMLClass4', function(){
            this.href = url+"/getXMLForMachineMonitor/lackMemory"
            this.setAttribute('download',"")

        });


        $(document).on('click', '.XMLClass5', function(){
            this.href = url+"/getXMLForMachineMonitor/BigTimeDiff"
            this.setAttribute('download',"")

        });


        $(document).on('click', '.XMLClass6', function(){
            var machineType=$(this).parent().parent().parent().parent().prev().find("div").text()

            this.href = url+"/getXMLForMachineMonitor/machineType/"+machineType
            this.setAttribute('download',"")

        });

        $(document).on('click', '.XMLClass7', function(){
            var version=$(this).parent().parent().parent().parent().prev().find("div").text()
            this.href = url+"/getXMLForMachineMonitor/fatherVersion/"+version
            this.setAttribute('download',"")

        });


        $(document).on('click', '.XMLClass8', function(){
           var version=$(this).parent().parent().parent().parent().prev().find("div").text()
            this.href = url+"/getXMLForMachineMonitor/sonVersion/"+version
            this.setAttribute('download',"")

        });




        table.on('tool(machineTypeCountTable)', function(obj){


            if(obj.event === 'showQrBar'){

                $.ajax({
                    type: "GET",
                    url: url+"/getXMLForMachineMonitor/machineType/"+obj.data.machineType,
                    success: function (data) {


                    }
                });


            }

        });

        table.on('tool(FatherVersionStatTable)', function(obj){


            if(obj.event === 'showQrBar'){

                $.ajax({
                    type: "GET",
                    url: url+"/getXMLForMachineMonitor/fatherVersion/"+obj.data.version,
                    success: function (data) {


                    }
                });
            }

        });




    })






});


