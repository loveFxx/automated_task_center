var layer;
$(function () {

    var basicDataPort = 8991;
    var monitorPort = 8999;

    //非常重要 下面都用这个
    // var url = window.location.origin.replace(basicDataPort, monitorPort)
    var url = MONITOR_URL;
    var url = '';
    console.log(url);


    var time = new Date().toLocaleString('en-CA', {timeZone: 'Asia/Shanghai', hour12: false}).replace(',', '')

    $("#time").text("数据更新时间：" + time)


    $.ajax({
        type: "POST",
        url: url + "/statAccount",
        success: function (res) {
            $(".agg-listbox").find('span').removeClass("loading");
            $(".agg-listbox").find('a').removeClass("loading");
            zeroNotJump($("#span7"), Object.keys(res[0])[0]);
            $("#span7").text(Object.keys(res[0])[0]);
            $("#span7_").attr("data", Object.values(res[0])[0]);
            $("#span7").data("idList", "(" + Object.values(res[0])[0] + ")");

            zeroNotJump($("#span8"), Object.keys(res[1])[0]);
            $("#span8").text(Object.keys(res[1])[0]);
            $("#span8_").attr("data", Object.values(res[1])[0]);
            $("#span8").data("idList", "(" + Object.values(res[1])[0] + ")");

            zeroNotJump($("#span9"), Object.keys(res[2])[0]);
            $("#span9").text(Object.keys(res[2])[0]);
            $("#span9_").attr("data", Object.values(res[2])[0]);
            $("#span9").data("idList", "(" + Object.values(res[2])[0] + ")");

            zeroNotJump($("#span10"), Object.keys(res[3])[0]);
            $("#span10").text(Object.keys(res[3])[0]);
            $("#span10_").attr("data", Object.values(res[3])[0]);
            $("#span10").data("idList", "(" + Object.values(res[3])[0] + ")");

            zeroNotJump($("#span11"), Object.keys(res[4])[0]);
            $("#span11").text(Object.keys(res[4])[0]);
            $("#span11_").attr("data", Object.values(res[4])[0]);
            $("#span11").data("idList", "(" + Object.values(res[4])[0] + ")");

            zeroNotJump($("#span12"), Object.keys(res[5])[0]);
            $("#span12").text(Object.keys(res[5])[0]);
            $("#span12_").attr("data", Object.values(res[5])[0]);
            $("#span12").data("idList", "(" + Object.values(res[5])[0] + ")");

        }
    });


    layui.use(['table'], function () {
        table = layui.table;
        $.ajax({
            type: "POST",
            url: url + "/statAccountSite",
            success: function (res) {
                let statusPersonMap = res[0]
                let statusMachineMap = res[1]


                var arr = []
                var sitesCount = 0;
                var circleSitesCount = 0;
                var abnormalTokenAll = 0;
                var abnormalTokenINCircle = 0;
                for (let statusPerson in statusPersonMap) {

                    //去多余的首位逗号
                    let inCircleList = statusPersonMap[statusPerson]['inCircleList'] === "" ? "" : statusPersonMap[statusPerson]['inCircleList'].substr(1)
                    let inCircleCount = statusPersonMap[statusPerson]['inCircleList'].split(",").length - 1
                    let notInCircleList = statusPersonMap[statusPerson]['notInCircleList'] === "" ? "" : statusPersonMap[statusPerson]['notInCircleList'].substr(1)
                    let notInCircleCount = statusPersonMap[statusPerson]['notInCircleList'].split(",").length - 1


                    let is0List = statusPersonMap[statusPerson]['is0List'] === "" ? "" : statusPersonMap[statusPerson]['is0List'].substr(1)
                    let is0Count = statusPersonMap[statusPerson]['is0List'].split(",").length - 1
                    let not0List = statusPersonMap[statusPerson]['not0List'] === "" ? "" : statusPersonMap[statusPerson]['not0List'].substr(1)
                    let not0Count = statusPersonMap[statusPerson]['not0List'].split(",").length - 1


                    let is1List = statusPersonMap[statusPerson]['is1List'] === "" ? "" : statusPersonMap[statusPerson]['is1List'].substr(1)
                    let is1Count = statusPersonMap[statusPerson]['is1List'].split(",").length - 1
                    let not1List = statusPersonMap[statusPerson]['not1List'] === "" ? "" : statusPersonMap[statusPerson]['not1List'].substr(1)
                    let not1Count = statusPersonMap[statusPerson]['not1List'].split(",").length - 1

                    let totalCount = is0Count + not0Count
                    let totalList = is0List + not0List


                    switch (statusPerson) {
                        case '-10':
                            arr.push({
                                statusPerson: '无效店铺',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            sitesCount += totalCount;
                            circleSitesCount += inCircleCount;
                            abnormalTokenAll += totalCount;
                            abnormalTokenINCircle += inCircleCount;
                            break;
                        case '-2':
                            arr.push({
                                statusPerson: '未验证',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            sitesCount += totalCount;
                            circleSitesCount += inCircleCount;
                            abnormalTokenAll += totalCount;
                            abnormalTokenINCircle += inCircleCount;
                            break;
                        case '0':
                            arr.push({
                                statusPerson: '正常（未运营）',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            sitesCount += totalCount;
                            circleSitesCount += inCircleCount;
                            abnormalTokenAll += totalCount;
                            abnormalTokenINCircle += inCircleCount;
                            break;
                        case '1':
                            arr.push({
                                statusPerson: '正常（运营中）',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            sitesCount += totalCount;
                            circleSitesCount += inCircleCount;
                            break;
                        case '2':
                            arr.push({
                                statusPerson: '关店（不可登陆）',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            sitesCount += totalCount;
                            circleSitesCount += inCircleCount;
                            abnormalTokenAll += totalCount;
                            abnormalTokenINCircle += inCircleCount;
                            break;
                        case '3':
                            arr.push({
                                statusPerson: '关店（可登陆）',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })

                            sitesCount += totalCount;
                            circleSitesCount += inCircleCount;
                            abnormalTokenAll += totalCount;
                            abnormalTokenINCircle += inCircleCount;
                            break;
                        case '4':
                            arr.push({
                                statusPerson: '暂停运营（假期模式）',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            sitesCount += totalCount;
                            circleSitesCount += inCircleCount;
                            abnormalTokenAll += totalCount;
                            abnormalTokenINCircle += inCircleCount;

                            break;
                        default:
                            sitesCount += totalCount;
                            circleSitesCount += inCircleCount;
                            abnormalTokenAll += totalCount;
                            abnormalTokenINCircle += inCircleCount;
                            arr.push({
                                statusPerson: statusPerson,
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })

                    }
                }

                $("#span1").text(sitesCount);

                $("#span2").text(circleSitesCount);

                $("#span3").text(abnormalTokenAll);

                $("#span4").text(abnormalTokenINCircle);


                table.render({


                    elem: '#countSiteByPersonTypeTable',
                    cols: [[
                        {field: 'statusPerson', title: 'statusPerson', align: "center"},
                        {
                            title: '全部站点', align: "center", width: "15%",
                            templet: function (d) {
                                return "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.totalList + ">" + d.totalCount + "</span>"
                            }
                        },
                        {
                            title: '有/无周期任务站点', align: "center", width: "15%",
                            templet: function (d) {
                                return "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.inCircleList + ">" + d.inCircleCount + "</span>" +
                                    " : " +
                                    "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.notInCircleList + ">" + d.notInCircleCount + "</span>"
                            }
                        },
                        {
                            title: '有/无机器关联', align: "center", width: "15%",
                            templet: function (d) {
                                return "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.not0List + ">" + d.not0Count + "</span>" +
                                    " : " +
                                    "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.is0List + ">" + d.is0Count + "</span>"
                            }
                        },
                        {
                            title: '有/无可用机器', align: "center", width: "15%",
                            templet: function (d) {
                                return "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.is1List + ">" + d.is1Count + "</span>" +
                                    " : " +
                                    "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.not1List + ">" + d.not1Count + "</span>"
                            }
                        }

                    ]]
                    , done: function (res, curr, count) {

                    }
                    , data: arr
                    , limit: 1000 //每页默认显示的数量
                });


                var arr2 = []
                var abnormalMachineTotal = 0
                var abnormalMachineInCircle = 0


                for (let statusMachine in statusMachineMap) {

                    //去多余的首位逗号
                    let inCircleList = statusMachineMap[statusMachine]['inCircleList'] === "" ? "" : statusMachineMap[statusMachine]['inCircleList'].substr(1)
                    let inCircleCount = statusMachineMap[statusMachine]['inCircleList'].split(",").length - 1
                    let notInCircleList = statusMachineMap[statusMachine]['notInCircleList'] === "" ? "" : statusMachineMap[statusMachine]['notInCircleList'].substr(1)
                    let notInCircleCount = statusMachineMap[statusMachine]['notInCircleList'].split(",").length - 1

                    let is0List = statusMachineMap[statusMachine]['is0List'] === "" ? "" : statusMachineMap[statusMachine]['is0List'].substr(1)
                    let is0Count = statusMachineMap[statusMachine]['is0List'].split(",").length - 1
                    let not0List = statusMachineMap[statusMachine]['not0List'] === "" ? "" : statusMachineMap[statusMachine]['not0List'].substr(1)
                    let not0Count = statusMachineMap[statusMachine]['not0List'].split(",").length - 1

                    let is1List = statusMachineMap[statusMachine]['is1List'] === "" ? "" : statusMachineMap[statusMachine]['is1List'].substr(1)
                    let is1Count = statusMachineMap[statusMachine]['is1List'].split(",").length - 1
                    let not1List = statusMachineMap[statusMachine]['not1List'] === "" ? "" : statusMachineMap[statusMachine]['not1List'].substr(1)
                    let not1Count = statusMachineMap[statusMachine]['not1List'].split(",").length - 1

                    let totalCount = is0Count + not0Count
                    let totalList = is0List + not0List


                    switch (statusMachine) {
                        case '-2':
                            arr2.push({
                                statusMachine: '-2',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '1':
                            arr2.push({
                                statusMachine: '正常',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            break;
                        case '17':
                            arr2.push({
                                statusMachine: '亚马逊后台需要重置密码',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '18':
                            arr2.push({
                                statusMachine: '亚马逊后台大洲没有授权',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '19':
                            arr2.push({
                                statusMachine: '亚马逊后台大洲没有选择默认站点',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })

                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '21':
                            arr2.push({
                                statusMachine: '亚马逊后台大洲没有设置单点登录',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '23':
                            arr2.push({
                                statusMachine: '亚马逊账号被锁定',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '24':
                            arr2.push({
                                statusMachine: '亚马逊密码错误',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '25':
                            arr2.push({
                                statusMachine: '亚马逊站点没有绑定信用卡',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '28':
                            arr2.push({
                                statusMachine: '二步验证二维码无效',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '29':
                            arr2.push({
                                statusMachine: '没有开启二步验证的二维码功能',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '30':
                            arr2.push({
                                statusMachine: '需要上传二维码',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '45':
                            arr2.push({
                                statusMachine: '没有银行卡信息',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        case '51':
                            arr2.push({
                                statusMachine: '获取后台二步验证二维码失败',
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;
                            break;
                        default:
                            arr2.push({
                                statusMachine: statusMachine,
                                totalCount: totalCount,
                                inCircleCount: inCircleCount,
                                notInCircleCount: notInCircleCount,
                                is0Count: is0Count,
                                not0Count: not0Count,
                                is1Count: is1Count,
                                not1Count: not1Count,
                                totalList: totalList,
                                inCircleList: inCircleList,
                                notInCircleList: notInCircleList,
                                is0List: is0List,
                                not0List: not0List,
                                is1List: is1List,
                                not1List: not1List
                            })
                            abnormalMachineTotal += totalCount;
                            abnormalMachineInCircle += inCircleCount;

                    }
                }


                $("#span5").text(abnormalMachineTotal);
                $("#loading5").css('visibility', 'hidden')


                $("#span6").text(abnormalMachineInCircle);
                $("#loading6").css('visibility', 'hidden')


                table.render({
                    elem: '#countSiteByMachineTypeTable',
                    cols: [[
                        {field: 'statusMachine', title: 'statusMachine', align: "center"},
                        {
                            title: '全部站点', align: "center", width: "15%",
                            templet: function (d) {
                                return "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.totalList + ">" + d.totalCount + "</span>"
                            }
                        },
                        {
                            title: '有/无周期任务站点', align: "center", width: "15%",
                            templet: function (d) {
                                return "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.inCircleList + ">" + d.inCircleCount + "</span>" +
                                    " : " +
                                    "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.notInCircleList + ">" + d.notInCircleCount + "</span>"
                            }
                        },

                        {
                            title: '有/无机器关联', align: "center", width: "15%",
                            templet: function (d) {
                                return "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.not0List + ">" + d.not0Count + "</span>" +
                                    " : " +
                                    "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.is0List + ">" + d.is0Count + "</span>"
                            }
                        },

                        {
                            title: '有/无可用机器', align: "center", width: "15%",
                            templet: function (d) {
                                return "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.is1List + ">" + d.is1Count + "</span>" +
                                    " : " +
                                    "<span style='cursor:pointer;color: #0099CC' class='jumpStoreAccountManageClass' data=" + d.not1List + ">" + d.not1Count + "</span>"
                            }
                        }

                    ]]
                    , done: function (res, curr, count) {
                    }
                    , data: arr2
                    , limit: 1000 //每页默认显示的数量
                });


            }
        });


    })


    $(document).on('click', '.jumpStoreAccountManageClass', function () {
        var idList = $(this).attr("data")
        downloadFileByForm(url+"/downloadSiteExcelFromMonitor", idList);
    });

    $(document).on('click', '.downloadAccountClass', function () {
        var idList = $(this).attr("data")
        downloadFileByForm(url+"/downloadAccountExcelFromMonitor", idList);
    });


    //使用post表单方式下载
    function downloadFileByForm(url, idList) {
        var form = $("<form></form>").attr("action", url).attr("method", "post");
        form.append($("<input></input>").attr("type", "hidden").attr("name", "idList").attr("value", idList));
        form.appendTo('body').submit().remove();
    }


    $(document).on('click', '#span7', function () {
        jumpStoreAccountManage($(this).data("idList"));

    });

    $(document).on('click', '#span8', function () {
        jumpStoreAccountManage($(this).data("idList"));

    });

    $(document).on('click', '#span9', function () {
        jumpStoreAccountManage($(this).data("idList"));

    });

    $(document).on('click', '#span10', function () {
        jumpStoreAccountManage($(this).data("idList"));

    });

    $(document).on('click', '#span11', function () {
        jumpStoreAccountManage($(this).data("idList"));

    });

    $(document).on('click', '#span12', function () {
        jumpStoreAccountManage($(this).data("idList"));

    });


});


//数字是0 就不跳了
function zeroNotJump($jqueryObject, value) {
    if (value == 0) {
        $jqueryObject.css("color", "#000000");
        $jqueryObject.next().css('visibility', 'hidden');
        $jqueryObject.parent().css("pointer-events", "none");

    }

}





function jumpStoreAccountManage(data) {
    var idList = data;

    var newWindowIn = window.open("../store/storeAccountManage");
    newWindowIn.onload = function () {
        this.idList = idList;
    };
}

