/**
 * 编码管理
 */
var form;
var table;
var element;
var xmSelect;
$(function() {

    var basicDataPort=8991;
    var monitorPort=8999;

    var url = MONITOR_URL;

    var time = new Date().toLocaleString('en-CA', {timeZone: 'Asia/Shanghai', hour12: false}).replace(',', '')

    $("#time").text("数据更新时间：" + time)

    $(document).on('click', '.invalidIpExcelClass', function(){
        this.href = url+"/downloadExcelFromMonitor/downloadInvalidIpExcel";
        this.setAttribute('download',"")
    });
    $(document).on('click', '.amazonInvalidIpExcelClass', function(){
        this.href = url+"/downloadExcelFromMonitor/downloadAmazonInvalidIpExcel";
        this.setAttribute('download',"")
    });

    $.ajax({
        type: "POST",
        url: "/getProxyIpNum",
        success: function (data) {
            $(".agg-listbox").find('span').removeClass("loading");
            $("#total").text(data.proxyIpTotal);
            $("#invalidNum").text(data.ipInvalidNum);
            $("#amazonTotal").text(data.amazonProxyIpTotal);
            $("#amazonInvalidNum").text(data.amazonIpInvalidNum);
            $("#amazonDaemonTotal").text(data.amazonDaemonTotal);
            $("#amazonDaemonInvalidNum").text(data.amazonDaemonIpInvalidNum);
        }
    });

    //
    layui.use(['table','element'], function(){
        table = layui.table;
        form = layui.form;
        element = layui.element;
        tableIns=table.render({
            width: 600,
            elem: '#platformProxyIpStatus',
            url:'/getplatformProxyIpStatus',
            method: 'post', //默认：get请求
            cellMinWidth: 80,
            page: false,
            request: {
            },
            response:{
                statusName: 'code', //数据状态的字段名称，默认：code
                statusCode: 200, //成功的状态码，默认：0
                countName: 'totals', //数据总数的字段名称，默认：count
                dataName: 'list' //数据列表的字段名称，默认：data
            },
            cols: [[
                {field:'platform', title:'平台',align:'center',width:"20%"},
                {field:'availableProxyIpNum', title:'可用数',align:'center',width:"20%"},
                {field:'banProxyIpNum', title:'封禁数',align:'center',width:"20%"}
            ]],
            done: function (res, curr, count) {
                $('.layui-table').css("width","100%");
            }
        });


        //均匀采样或top20
        form.on('select(barType)', function (data) {
            var barType = data.value;
            $("input[name='barType']").val(barType);
            getProxyMonitor();
        });
        //代理IP
        form.on('select(monitorSearcher)', function (data) {
            var proxyId = data.value;
            $("input[name='proxyId']").val(proxyId);
            getProxyMonitor();
        });
    });

    layui.use('laydate', function(){
        var laydate = layui.laydate;
        var currentTime = getCurrentTime();
        $("#timePoint").val(currentTime);

        laydate.render({
          elem: '#timePoint'
          ,type: 'datetime',
          done: function(value,date,endDate){
            getProxyScatter(value);
          }
        });
    });

});
function getCurrentTime() {
    var date = new Date();//当前时间
    var year = date.getFullYear() //年
    var month = repair(date.getMonth() + 1);//月
    var day = repair(date.getDate());//日
    var hour = repair(date.getHours());//时
    var minute = repair(date.getMinutes());//分
    var second = repair(date.getSeconds());//秒

    //当前时间
    var curTime = year + "-" + month + "-" + day
            + " " + hour + ":" + minute + ":" + second;
    return curTime;
}

//若是小于10就加个0

function repair(i){
    if (i >= 0 && i <= 9) {
        return "0" + i;
    } else {
        return i;
    }
}

function load(obj) {
    //重新加载table
    tableIns.reload({
    });
}

//代理IP失效详情弹窗表格展示
function invalidIpDetail(){
    layer.open({
        type:1,
        title:"失效代理IP",
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['650px'],
        content:$('#invalidIp'),
        end:function(){
        }
    });
    table.render({
        elem: '#showInvalidIp',
        url: '/getInvalidIp',
        title: '',
        method: 'post', //默认：get请求
        cellMinWidth: 80,
        //page: true,
        request: REQUEST_BODY,
        response: RESPONSE_BODY,
        cols: [[
            {field:'proxyIp', title:'代理IP',width:180},
            {field:'proxyIpPort', title:'端口',width:80},
            {field:'platform', title:'平台',width:100},
            {field:'account', title:'账号',width:100},
            {field:'continents', title:'站点',width:100}
        ]]
    });

}

//亚马逊代理IP失效详情弹窗表格展示
function amazonInvalidIpDetail(){
    layer.open({
        type:1,
        title:"失效代理IP",
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['650px'],
        content:$('#amazonInvalidIp'),
        end:function(){
        }
    });
    table.render({
        elem: '#showAmazonInvalidIp',
        url: '/getAmazonInvalidIp',
        title: '',
        method: 'post', //默认：get请求
        cellMinWidth: 80,
        //page: true,
        request: REQUEST_BODY,
        response: RESPONSE_BODY,
        cols: [[
            {field:'proxyIp', title:'代理IP',width:180},
            {field:'proxyIpPort', title:'端口',width:80},
            {field:'platform', title:'平台',width:100},
            {field:'account', title:'账号',width:100},
            {field:'continents', title:'站点',width:100}
        ]]
    });

}

function getProxyMonitor(){
    var beginTime = $("#beginTime").val();
    var endTime = $("#endTime").val();
    var barType = $("input[name='barType']").val();
    var proxyId = $("input[name='proxyId']").val();

    $.ajax({
        type: "POST",
        data: {"barType":barType,"beginTime":beginTime,"endTime":endTime,"workType":"AmazonDaemon"},
        url: "/getProxyMonitor",
        success: function (data) {
            var ipList = data.ipList;
            var succeedNumList = data.succeedNumList
            var bannedNumList = data.bannedNumList;
            var bannedRateList = data.bannedRateList;

            //基于节点初始chart
            var rateBar = echarts.init(document.getElementById('rateBar'));
            option = {
              tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'cross' }
              },
              legend: {},
              xAxis: [
                {
                  type: 'category',
                  axisTick: {
                    alignWithLabel: true
                  },
                  data: ipList,
                  axisLabel: {
                      formatter: function(value) {
                      var res = value;
                      if(res.length > 50) {
                            var arr1 = res.split(":");
                            var arr2 = arr1[1].split("-");
                            res = arr2[6]+":"+arr1[3];
                        }
                        return res;
                      }
                  }
                }
              ],
              yAxis: [
                {
                  type: 'value',
                  name: '计数',
                  position: 'right',
                  axisLabel: {
                    formatter: '{value}个'
                  }
                },
                {
                  type: 'value',
                  name: '禁用率',
                  min: 0,
                  max: 100,
                  position: 'left',
                  axisLabel: {
                    formatter: '{value} %'
                  }
                }
              ],
              series: [
                {
                  name: '成功数',
                  type: 'bar',
                  yAxisIndex: 0,
                  data: succeedNumList,
                  stack: 'x'
                },
                {
                  name: '禁用数',
                  type: 'bar',
                  yAxisIndex: 0,
                  data: bannedNumList,
                  stack: 'x'
                },
                {
                  name: '禁用率',
                  type: 'line',
                  smooth: true,
                  yAxisIndex: 1,
                  data: bannedRateList,
                  markLine: {
                              data: [
                                    {
                                      yAxis: 80,
                                      symbol:"none", // 去掉警戒线最后面的箭头
                                      silent:false, // 鼠标悬停事件  true没有，false有
                                      name: '警戒线', // 警戒线
                                      lineStyle: { // 警戒线样式
                                          type: 'dashed',
                                          color: 'red'
                                      },
                                      label: {
                                           position: 'end',//将警示值放在哪个位置，三个值“start”,"middle","end"  开始  中点 结束
                                           formatter: '警戒线', // 名称
                                           fontSize: 14
                                      },
                                    },
                    ]
                  }
                }
              ]
            };
            // 使用刚指定的配置项和数据显示图表。
            rateBar.setOption(option);

            rateBar.on('click', function(params) {
                var ip = params.name;
                var proxyId = ip.split("-")[0];
                layui.use(['form'], function(){
                    $("#monitorSearcher").val(proxyId);
                    layui.form.render("select");

                    $("input[name='proxyId']").val(proxyId);
                    getProxyMonitor();
                });

            });
        }
    });

    //单个IP按时间分块
    $.ajax({
        type: "POST",
        data: {"proxyId":proxyId,"beginTime":beginTime,"endTime":endTime,"workType":"AmazonDaemon"},
        url: "/getTimeBlockByProxy",
        success: function (data) {
            var timeList = data.timeList;
            var succeedNumList = data.succeedNumList
            var bannedNumList = data.bannedNumList;
            var bannedRateList = data.bannedRateList;

            //基于节点初始chart
            var timeBar = echarts.init(document.getElementById('timeBar'));
            option = {
              tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'cross' }
              },
              legend: {},
              xAxis: [
                {
                  type: 'category',
                  axisTick: {
                    alignWithLabel: true
                  },
                  data: timeList
                }
              ],
              yAxis: [
                {
                  type: 'value',
                  name: '计数',
                  position: 'right',
                  axisLabel: {
                    formatter: '{value}个'
                  }
                },
                {
                  type: 'value',
                  name: '禁用率',
                  min: 0,
                  max: 100,
                  position: 'left',
                  axisLabel: {
                    formatter: '{value} %'
                  }
                }
              ],
              series: [
                {
                  name: '成功数',
                  type: 'bar',
                  yAxisIndex: 0,
                  data: succeedNumList,
                  stack: 'x'
                },
                {
                  name: '禁用数',
                  type: 'bar',
                  yAxisIndex: 0,
                  data: bannedNumList,
                  stack: 'x'
                },
                {
                  name: '禁用率',
                  type: 'line',
                  smooth: true,
                  yAxisIndex: 1,
                  data: bannedRateList,
                  markLine: {
                              data: [
                                    {
                                      yAxis: 80,
                                      symbol:"none", // 去掉警戒线最后面的箭头
                                      silent:false, // 鼠标悬停事件  true没有，false有
                                      name: '警戒线', // 警戒线
                                      lineStyle: { // 警戒线样式
                                          type: 'dashed',
                                          color: 'red'
                                      },
                                      label: {
                                           position: 'end',//将警示值放在哪个位置，三个值“start”,"middle","end"  开始  中点 结束
                                           formatter: '警戒线', // 名称
                                           fontSize: 14
                                      },
                                    },
                    ]
                  }
                }
              ]
            };
            timeBar.setOption(option);
        }
    });
}
getProxyMonitor();

function getProxyScatter(timePoint){

    $.ajax({
        type: "POST",
        data: {"timePoint":timePoint,"workType":"AmazonDaemon"},
        url: "/getProxyScatter",
        success: function (data) {
            var ipList = data.ipList;
            var openTimestampList = data.openTimestampList;

            var minDate = new Date(Date.parse(openTimestampList[0]));
            var resDate = new Date(minDate.setSeconds(minDate.getSeconds() -60));
            var minTime = resDate.toLocaleString();

            var maxDate = new Date(Date.parse(openTimestampList[openTimestampList.length-1]));
            var maxResDate = new Date(maxDate.setSeconds(maxDate.getSeconds() +60));
            var maxTime = maxResDate.toLocaleString();


            if(maxResDate.getTime()<Date.now()){
                var currentDate = new Date();
                var a = new Date(currentDate.setSeconds(currentDate.getSeconds() +60));
                maxTime = a.toLocaleString();
            }

            var scatter = echarts.init(document.getElementById('scatter'));
                        option = {
                          tooltip: {},
                          legend: {
                            data: ['时间戳',]
                          },
                          xAxis: {
                            data: ipList
                          },
                          yAxis: {
                            type: "time",
                            min: minTime,
                            max: maxTime
                          },
                          series: [
                            {
                              type: 'scatter',
                              data: openTimestampList,
                              markLine: {
                                        data: [
                                            {
                                                yAxis: timePoint,
                                                symbol:"none", // 去掉警戒线最后面的箭头
                                                silent:false, // 鼠标悬停事件  true没有，false有
                                                name: '当前时间', // 警戒线
                                                lineStyle: { // 警戒线样式
                                                    type: 'dashed',
                                                    color: 'red'
                                                },
                                                label: {
                                                     position: 'end',//将警示值放在哪个位置，三个值“start”,"middle","end"  开始  中点 结束
                                                     formatter: '当前时间', // 名称
                                                     fontSize: 14
                                                 },
                                            },
                                        ]
                                      }
                            }
                          ]
                        };
                        // 使用刚指定的配置项和数据显示图表。
                        scatter.setOption(option);
        }
    });

    $.ajax({
            type: "POST",
            data: {"timePoint":timePoint,"workType":"AmazonDaemon"},
            url: "/getProxyInterval",
            success: function (data) {
                var timeList = data.timeList;
                var validNumList = data.validNumList
                var invalidNumList = data.invalidNumList;

                //基于节点初始chart
                var proxyInterval = echarts.init(document.getElementById('proxyInterval'));
                option = {
                  tooltip: {
                    trigger: 'axis',
                    axisPointer: { type: 'cross' }
                  },
                  legend: {},
                  xAxis: [
                    {
                      type: 'category',
                      axisTick: {
                        alignWithLabel: true
                      },
                      data: timeList
                    }
                  ],
                  yAxis: [
                    {
                      type: 'value',
                      name: '计数',
                      position: 'left',
                      axisLabel: {
                        formatter: '{value}个'
                      }
                    }
                  ],
                  series: [
                    {
                      name: '有效数',
                      type: 'bar',
                      yAxisIndex: 0,
                      data: validNumList,
                      stack: 'x'
                    },
                    {
                      name: '失效数',
                      type: 'bar',
                      yAxisIndex: 0,
                      data: invalidNumList,
                      stack: 'x'
                    }
                  ]
                };
                // 使用刚指定的配置项和数据显示图表。
                proxyInterval.setOption(option);
            }
        });
}
getProxyScatter(getCurrentTime());

amazonDaemonProxySearcher("monitorSearcher");

$('#timeRange').daterangepicker({
		  timePicker: true, //显示时间
		           timePicker24Hour: true, //时间制
		           timePickerSeconds: true, //时间显示到秒
		          startDate: moment().subtract(1, 'hours'), //设置开始日期
		         endDate: moment(new Date()), //设置结束器日期

		           "opens": "center",
		           ranges: {
                     '一小时': [moment().subtract(1, 'hours'), moment()],
                     '三小时': [moment().subtract(3, 'hours'), moment()],
		             '一天': [moment().subtract(1, 'days'), moment()],
		             '两天': [moment().subtract(2, 'days'), moment()]
		         },
		         showWeekNumbers: true,
		         locale: {
		             format: "YYYY-MM-DD HH:mm:ss", //设置显示格式
		             applyLabel: '确定', //确定按钮文本
		             cancelLabel: '取消', //取消按钮文本
		             customRangeLabel: '自定义',
		             daysOfWeek: ['日', '一', '二', '三', '四', '五', '六'],
		              monthNames: ['一月', '二月', '三月', '四月', '五月', '六月',
		                 '七月', '八月', '九月', '十月', '十一月', '十二月'
		              ],
		              firstDay: 1
		         },
		     }, function(start, end, label) {
		         $("#beginTime").val(start.format('YYYY-MM-DD HH:mm:ss'));
		         $("#endTime").val(end.format('YYYY-MM-DD HH:mm:ss'));
		         getProxyMonitor();
		     });
