var layer;

var table;
var form;
formSubmit(null);
$(function () {
    layui.use('table', function () {
        table = layui.table;
        form = layui.form;
        form.on('submit(searchSubmit)', function (data) {
            // TODO 校验
            formSubmit(data);
            return false;
        });

        form.on('select(systemName)', function (data) {
            var message = $("select[name='systemName']").val();
            if (message == "") {
                formSelects.data('method', 'local', {
                    direction: 'down',
                    arr: []
                });
                form.render('select');
            } else if (message == "client") {
                formSelects.data('method', 'local', {
                    direction: 'down',
                    arr: [{"name": "register", "value": "register"},
                        {"name": "get_job", "value": "get_job"},
                        {"name": "result_job", "value": "result_job"},
                        {"name": "getProxy", "value": "getProxy"},
                        {"name": "bannedProxy", "value": "bannedProxy"},
                        {"name": "heartBeat", "value": "heartBeat"},
                        {"name": "captcha_code", "value": "captcha_code"},
                        {"name": "account_verify_code", "value": "account_verify_code"},
                    ]
                });
                form.render('select');
            } else {
                formSelects.data('method', 'local', {
                    direction: 'down',
                    arr: [{"name": "register", "value": "register"},
                        {"name": "add_task_source", "value": "add_task_source"},
                        {"name": "bulk_add_task_source", "value": "bulk_add_task_source"},
                        {"name": "delete_task_source", "value": "delete_task_source"},
                        {"name": "get_task_result", "value": "get_task_result"},
                        {"name": "pushAsync", "value": "pushAsync"}
                    ]
                });
                form.render('select');
            }
        });
    });


});

function formSubmit(obj) {
    var dataSubmit;
    var showType;
    var method;
    if (obj == null) {
        dataSubmit = "";
        method = "";
        showType = '1';
    } else {
        dataSubmit = obj.field.systemName;
        showType = obj.field.showType;
        method = obj.field.method;
    }
    $.ajax({
        type: "POST",
        data: {"systemName": dataSubmit, "method": method, "showType": showType},
        url: "/getRequestCountMonitor",
        success: function (data) {
            echartsRequestCount(data);
        }
    });
}

function max(value) {
    if (value.max == 0) {
        return 10
    }
    return value.max * 1.1
}

function min(value) {
    if (value.min * 0.8 == 0) {
        if (value.max == 0) {
            return -20
        }
        return -value.max * 0.2
    }
    return value.min * 0.8
}

function echartsRequestCount(data) {
    var xAxisData = [];
    var requestNumData = [];
    var requestSuccessData = [];
    var requestLimitData = [];
    var requestTimeoutData = [];
    var requestExceptionData = [];
    var requestSuccessRatingData = [];
    var chartZhe = echarts.init(document.getElementById('optionchartZhe'));
    var optionchartZhe = {
        title: {
            text: '请求次数'
        },
        tooltip: {
            "trigger": "axis"
        },
        legend: { //顶部显示 与series中的数据类型的name一致
            data: ['请求总数', '成功次数', '超时次数', '失败次数', '限流次数', '成功率'],
            selected: {
                '请求总数': true,
                '成功次数': true,
                '超时次数': true,
                '失败次数': true,
                '限流次数': true,
                '成功率': true,
            }
        },
        xAxis: {
            data: xAxisData,
            axisTick: {
                alignWithLabel: true
            },
        },
        yAxis: [
            {
                type: "value",
                // name: "请求总数",
                position: "left",
                show: true,
                scale: true,
                axisLabel: {
                    formatter: function () {
                        return "";
                    }
                },
                max: function (value) {
                    return max(value);
                },
                min: function (value) {
                    return min(value);
                },

            },
            {
                "type": "value",
                "name": "成功次数",
                position: "left",
                show: false,
                scale: true,
                max: function (value) {
                    return max(value);
                },
                min: function (value) {
                    return min(value);
                },
            },
            {
                "type": "value",
                "name": "超时次数",
                position: "right",
                show: false,
                scale: true,
                max: function (value) {
                    return max(value);
                },
                min: function (value) {
                    return min(value);
                },
            },
            {
                "type": "value",
                "name": "失败次数",
                position: "right",
                show: false,
                scale: true,
                max: function (value) {
                    return max(value);
                },
                min: function (value) {
                    return min(value);
                },

            },
            {
                "type": "value",
                "name": "限流次数",
                position: "right",
                show: false,
                scale: true,
                max: function (value) {
                    return max(value);
                },
                min: function (value) {
                    return min(value);
                },
            },
            {
                "type": "value",
                "name": "成功率",
                position: "right",
                show: false,
                scale: true,
                max: function (value) {
                    return max(value);
                },
                min: function (value) {
                    return min(value);
                },
            },
        ],
        series: [{
            name: '请求总数',
            type: 'line', //线性
            data: requestNumData,
            yAxisIndex: 0,
        }, {
            name: '成功次数',
            type: 'line', //线性
            data: requestSuccessData,
            yAxisIndex: 1,
        }, {
            name: '超时次数',
            type: 'line', //线性
            data: requestTimeoutData,
            yAxisIndex: 2,
        }, {
            name: '失败次数',
            type: 'line', //线性
            data: requestExceptionData,
            yAxisIndex: 3,
        }, {
            // smooth: true, //曲线 默认折线
            name: '限流次数',
            type: 'line', //线性
            data: requestLimitData,
            yAxisIndex: 4,
        }, {
            // smooth: true, //曲线
            name: '成功率',
            type: 'line', //线性
            data: requestSuccessRatingData,
            yAxisIndex: 5,
        }]
    };
    for (var i = 0; i < data.periods.length; i++) {
        xAxisData.push(data.periods[i]);    //挨个取出销量并填入销量数组
    }
    for (var i = 0; i < data.requestNum.length; i++) {
        requestNumData.push(data.requestNum[i]);
    }
    for (var i = 0; i < data.requestSuccess.length; i++) {
        requestSuccessData.push(data.requestSuccess[i]);
    }
    for (var i = 0; i < data.requestLimit.length; i++) {
        requestLimitData.push(data.requestLimit[i]);
    }
    for (var i = 0; i < data.requestTimeout.length; i++) {
        requestTimeoutData.push(data.requestTimeout[i]);
    }
    for (var i = 0; i < data.requestException.length; i++) {
        requestExceptionData.push(data.requestException[i]);
    }
    for (var i = 0; i < data.requestSuccessRating.length; i++) {
        requestSuccessRatingData.push(data.requestSuccessRating[i]);
    }
    chartZhe.setOption(optionchartZhe, true);
}





