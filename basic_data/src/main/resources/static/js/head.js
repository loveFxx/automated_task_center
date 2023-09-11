/**
 * 菜单
 * */
//获取路径uri
var pathUri=window.location.href;
$(function(){
    layui.use('element', function(){
        var element = layui.element;
        // 左侧导航区域（可配合layui已有的垂直导航）
        $.get("/permission/getUserPerms",function(data){
            if(data!=null){
                getMenus(data.perm);
                element.render('nav');
            }else{
                layer.alert("权限不足，请联系管理员",function () {
                    //退出
                    window.location.href="/logout";
                });
            }
        });
    });
})
$(function(){
    layui.use('element', function(){
        var element = layui.element;
    });
})
var isShow = true;  //定义一个标志位
$('.kit-side-fold').click(function(){
    //选择出所有的span，并判断是不是hidden
    $('.layui-nav-item span').each(function(){
        if($(this).is(':hidden')){
            $(this).show();
        }else{
            $(this).hide();
        }
    });
    //判断isshow的状态
    if(isShow){
        $('.layui-side.layui-bg-black').width(0); //设置宽度
        // $('.kit-side-fold i').css('margin-right', '70%');  //修改图标的位置
        //将footer和body的宽度修改
        $('.layui-body').css('left', 0+'px');
        $('.layui-footer').css('left', 0+'px');
        //将二级导航栏隐藏
        $('dd span').each(function(){
            $(this).hide();
        });
        //修改标志位
        isShow =false;
    }else{
        $('.layui-side.layui-bg-black').width(200);
        $('.kit-side-fold i').css('margin-right', '10%');
        $('.layui-body').css('left', 200+'px');
        $('.layui-footer').css('left', 200+'px');
        $('dd span').each(function(){
            $(this).show();
        });
        isShow =true;
    }
});

// $(".crawlerServer-nav-li").click(function () {
//     var crawlerServer = sessionStorage.getItem("crawlerServer-nav-li");
//     // alert(crawlerServer)
//     if(crawlerServer == null){
//         sessionStorage.setItem("crawlerServer-nav-li","id");
//     }else {
//         sessionStorage.removeItem("crawlerServer-nav-li");
//     }
// });
// alert($('#crawlerServer-nav-li').attr('class'));
var getMenus=function(data){
    //回显选中
    var ul=$("<ul class='layui-nav layui-nav-tree' lay-filter='test'></ul>");
    for(var i=0;i < data.length;i++){
        var node=data[i];

        var li=$("<li class='layui-nav-item' flag='"+node.id+"'></li>");
        var a=$("<a class='' href='javascript:;'>"+node.name+"</a>");
        li.append(a);
        //获取子节点
        var childArry = node.childrens;

        if(childArry.length>0){
            a.append("<span class='layui-nav-more'></span>");
            var dl=$("<dl class='layui-nav-child'></dl>");
            for (var y in childArry) {
                var dd=$("<dd><a href='"+childArry[y].url+"'>"+childArry[y].name+"</a></dd>");
                //判断选中状态
                if(pathUri.indexOf(childArry[y].url)>0){
                    li.addClass("layui-nav-itemed");
                    dd.addClass("layui-this")
                }
                dl.append(dd);
            }
            li.append(dl);
        }
        ul.append(li);
    }
    $(".layui-side-scroll").append(ul);
}
//根据菜单主键id获取下级菜单
//id：菜单主键id
//arry：菜单数组信息
function getParentArry(id, arry) {
    var newArry = new Array();
    for (var x in arry) {
        if (arry[x].pId == id)
            newArry.push(arry[x]);
    }
    return newArry;
}


function updateUsePwd(){
    layer.open({
        type:1,
        title: "修改密码",
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['450px'],
        content:$('#pwdDiv')
    });
}