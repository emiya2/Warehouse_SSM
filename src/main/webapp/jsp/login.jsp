<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>

<head>
    <title id="projectName1"></title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link type="text/css" rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/xznstatic/css/font-awesome.min.css">
    <link type="text/css" rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/xznstatic/css/bootstrap.min.css">
    <link type="text/css" rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/xznstatic/css/animate.css">
    <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/xznstatic/css/all.css">
    <link type="text/css" rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/xznstatic/css/pink-violet.css" id="theme-change"
          class="style-change color-change">
    <link type="text/css" rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/xznstatic/css/style-responsive.css">

    <script type="text/javascript" src="${pageContext.request.contextPath}/resources/assets2/js/jquery.min.js"></script>
</head>

<body id="signin-page">
<div class="page-form">
    <form id="loginForm" action="" method="post" class="form">
        <div class="header-content">
            <h1 id="projectName"></h1>
        </div>
        <div class="body-content">
            <div class="form-group">
                <div class="input-icon right">
                    <input type="text" placeholder="用户名" id="username" name="username" class="form-control" required>
                </div>
            </div>
            <div class="form-group">
                <div class="input-icon right">
                    <input type="password" placeholder="密码" name="password" class="form-control" required>

                </div>
            </div>
            <div class="form-group">
                <div class="input-icon right">


                      <input type="text" name="verifyCode" placeholder="验证码,点击图片刷新！"  required >
                    <span class="cell-5">
                          <img id="verifyCode" src="/cangkuguanlixitong/verify/regist.do" onclick="myRefersh(this)">
                    </span>
                </div>
            </div>
            <div class="form-group">
                <div class="checkbox-list">
                    <label>
                        <input type="radio" name="chk" value="管理员" onclick="checkRole('users')">&nbsp;管理员
                    </label>
                </div>
            </div>
            <div class="form-group">
                <div class="checkbox-list">
                    <label>
                        <input type="radio" name="chk" value="用户" onclick="checkRole('yonghu')">&nbsp;用户
                    </label>
                </div>
            </div>
            <div id="submitBtn" class="form-group">
                <button type="submit" class="btn btn-success" style="width: 100%;" onclick="login()">登 录</button>
            </div>
            <div class="form-group">
                <button type="button" class="btn btn-success" style="width: 100%;"
                        onclick="window.location.href='./modules/yonghu/register.jsp'">注册 用户
                </button>
            </div>
            <div class="clearfix"></div>
            <hr>
            <div class="row mbm text-center">
            </div>
        </div>
    </form>
</div>

<script type="text/javascript" src="${pageContext.request.contextPath}/resources/assets2/js/bootstrap.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery.form.js"></script>

<script type="text/javascript">

    <%@ include file="utils/menu.jsp"%>

    <%@ include file="utils/baseUrl.jsp"%>

    /*
    *   验证码图片刷新
    * */
    function myRefersh(e) {
        const source = e.src; // 获得原来的 src 中的内容

        //console.log( "source : " + source  ) ;
        var index = source.indexOf("?");  // 从 source 中寻找 ? 第一次出现的位置 (如果不存在则返回 -1 )

        //console.log( "index : " + index  ) ;
        if (index > -1) { // 如果找到了 ?  就进入内部
            var s = source.substring(0, index); // 从 source 中截取 index 之前的内容 ( index 以及 index 之后的内容都被舍弃 )
            //console.log( "s : " + s  ) ;

            var date = new Date(); // 创建一个 Date 对象的 一个 实例

            var time = date.getTime(); // 从 新创建的 Date 对象的实例中获得该时间对应毫秒值
            e.src = s + "?time=" + time; // 将 加了 尾巴 的 地址 重新放入到 src 上
            //console.log( e.src ) ;
        } else {
            var date = new Date();
            e.src = source + "?time=" + date.getTime();
        }
    }

    // var role = "";
    var accountTableName = "";

    function checkRole(tableName) {
        // role = roleName;
        $('#loginForm').attr('action', baseUrl + tableName + '/login');
        accountTableName = tableName;
    }

    function login() {
        var roleValue = $("input[name='chk']:checked").val();

        $("#loginForm").ajaxForm(function (res) {
            if (roleValue == "" || roleValue == null) {
                alert("请选择角色后再登录");
            } else {
                if (res.code == 0) {
                    var username = $('#username').val();
                    window.sessionStorage.setItem('accountTableName', accountTableName);
                    window.sessionStorage.setItem('username', username);
                    window.sessionStorage.setItem('token', res.token);
                    window.sessionStorage.setItem('role', res.role);
                    window.sessionStorage.setItem('userId', res.userId);
                    window.location.href = "${pageContext.request.contextPath}/index.jsp";
                } else {
                    alert(res.msg);
                }
            }
        });
    }

    function ready() {
        document.getElementById('projectName').innerHTML = projectName;
        document.getElementById('projectName1').innerHTML = projectName;
    }

    document.addEventListener("DOMContentLoaded", ready);
</script>
</body>

</html>
