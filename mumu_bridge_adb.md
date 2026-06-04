* [![logo](https://mumu.res.netease.com/pc/gw/20230703114958/img/logo_3468208.png)](/)
* [首页](//mumu.163.com/index.html)
* [更多版本](//mumu.163.com/update/)
* [游戏中心](//mumu.163.com/games/)
* [活动&资讯](//mumu.163.com/news/)
* [常见问题](//mumu.163.com/help/)
* [创作激励](https://mumu.163.com/wh/index.html?)
* [商务合作](//mumu.163.com/business/)
* [![](https://mumu.res.netease.com/pc/gw/20230510161641/img/gv_logo_12acda78.png)
  网易UU远程](https://uuyc.163.com/?source_channel=mumu_gw)

[下载模拟器](https://adl.netease.com/d/g/mumu/c/gw_mumu12 "下载模拟器")

[更多版本](//mumu.163.com/update/)

[游戏中心](//mumu.163.com/games/)

[活动&资讯](//mumu.163.com/news/)

[常见问题](//mumu.163.com/help/)

[创作激励](https://mumu.163.com/wh/index.html?)

[商务合作](//mumu.163.com/business/)

[![](https://mumu.res.netease.com/pc/gw/20230510161641/img/gv_logo_12acda78.png)
网易UU远程](https://uuyc.163.com/?source_channel=mumu_gw)

[下载模拟器](https://adl.netease.com/d/g/mumu/c/gw_mumu12)

[请输入关键词查找相关问题](javascript:; "搜索")

[常见问题首页](//mumu.163.com/help/index.html "常见问题首页") /
[功能介绍](//mumu.163.com/help/index.html#%E5%8A%9F%E8%83%BD%E4%BB%8B%E7%BB%8D "功能介绍") /
MuMu模拟器桥接模式连接adb教程

收起

安装须知

启动异常

如何开启VT

安装APP

网络问题

运行问题

功能介绍

游戏应用相关

MuMu账号

其他

MuMu模拟器桥接模式连接adb教程

最新更新时间：2024-07-03

由于开启桥接网络模式后无法直接通过adb连接，目前可参考以下两种连接方式：

**注：网络桥接功能需要MuMu模拟器12在3.9.2及以上版本才可使用**[**>>点击获取最新版本**](https://mumu.163.com/update/)

**【****目录****】**

[**一、通过adb devices查看当前设备端口进行adb连接和操作**](#a1)

[**二、使用MuMuManager.exe进行adb连接和操作**](#a2)

**一、****通过adb devices查看当前设备端口进行adb连接和操作**

　　运行模拟器时通过adb devices先查看当前设备端口，格式一般是 桥接网络IP:5555

如只运行单个实例则默认连接，可直接进行操作如进入shell：

　　adb shell

![](https://nie.res.netease.com/r/pic/20240703/f42326c6-c561-4292-b9ce-165cd20a63aa.png)

如存在**多个实例**，可先查看对应实例端口再针对性连接即可：

　　adb devices

![](https://nie.res.netease.com/r/pic/20240703/8d901f47-d2f1-4362-a946-5a062998d374.png)

　　使用对应模拟器实例进行操作：

　　adb -s 10.227.84.172:5555 shell

![](https://nie.res.netease.com/r/pic/20240703/3e467e33-20ff-4202-b513-b2827ce5ef1e.png)

当同时运行多个实例时很难将实例和端口对应起来，则需要查看所需操作实例的IP。

　　查看某个模拟器实例对应的IP：

　　点击模拟器主页面-系统应用-设置-关于手机-IP地址

![](https://nie.res.netease.com/r/pic/20240703/607b3df9-7fcc-4c0c-b843-8f21a9f35416.png)

　　点击模拟器系统应用-设置-网络与互联网-互联网-wlan0-IP地址

![](https://nie.res.netease.com/r/pic/20240703/cf1d9683-54c4-472b-8e91-1399fde732dc.png)

　　使用第三方安卓检测软件查看

![](https://nie.res.netease.com/r/pic/20240703/1e7d0ad4-b68d-4cdc-bda1-555b7c594981.png)

**二、****使用MuMuManager.exe进行adb连接和操作**

　　无论开启或关闭桥接模式，均支持使用MuMuManager进行adb连接。

查看模拟器某个实例端口：

　　MuMuManager adb -v 0

![](https://nie.res.netease.com/r/pic/20240703/4db0b2d3-60d5-4eaa-9e08-5cea6b8c5656.png)

连接模拟器某个实例：

　　MuMuManager adb -v 0 connect

![](https://nie.res.netease.com/r/pic/20240703/a9abe03b-d173-49c3-a586-6c4a0506b07b.png)

进入模拟器某个实例shell：

　　MuMuManager adb -v 0 shell

![](https://nie.res.netease.com/r/pic/20240703/abecf397-7eba-42e0-af5d-b0c28eacec5e.png)

使用模拟器某个实例进行其他操作，如获取root：

　　MuMuManager adb -v 0 root

![](https://nie.res.netease.com/r/pic/20240703/b7f597c3-0592-40f6-a802-3047791cef4a.png)

　　更多MuMuManager常用指令可参考：[点击跳转](https://mumu.163.com/help/20230504/35047_1086360.html#a1)

关键词：

[清除](javascript:;)

![img](https://mumu.res.netease.com/pc/zt/20230522172249/img/nodata_9919f6c.png)

暂时未能为您匹配到问题，请换个关键词试试吧~

你还可以：   
1、联系在线客服反馈问题：[点击咨询](https://wymumumnq.qiyukf.com/client?k=bc15d40a1abb4d66f1438d5054434b52&wp=1&robotShuntSwitch=1&robotId=5302691&shuntId=0 "在线客服")   
2、登记问卷反馈问题：<http://163.fm/8Xs3d3Mx>

加载中...

![logo](https://mumu.res.netease.com/pc/gw/20250207163110/img/footer_logo_15cca674.png)
:   [![微博:MuMu模拟器](https://mumu.res.netease.com/pc/gw/20250207163110/img/icon_weibo_370b2dbd.png)](https://weibo.com/u/6271897376 "微博:MuMu模拟器")
    [![B站:MuMu模拟器-Mu酱](https://mumu.res.netease.com/pc/gw/20250207163110/img/icon_blbl_afc4d368.png)](https://space.bilibili.com/109778207#/ "B站:MuMu模拟器-Mu酱")
    [![贴吧:MuMu模拟器吧](https://mumu.res.netease.com/pc/gw/20250207163110/img/icon_baidu_4c01d99d.png)](https://tieba.baidu.com/f?kw=mumu%E6%A8%A1%E6%8B%9F%E5%99%A8 "贴吧:MuMu模拟器吧")

    [![Mu酱企业微信](https://mumu.res.netease.com/pc/gw/20250207163110/img/icon_wechat_40a3e6af.png)](javascript:; "Mu酱企业微信")

    ![Mu酱企业微信](https://mumu.res.netease.com/pc/gw/20250207163110/img/qywx_e86f1c64.png)

产品
:   [下载](/download/ "下载")

资讯
:   [游戏攻略](/news/info/ "游戏攻略")
:   [有奖活动](/news/activity/ "有奖活动")
:   [产品动态](/news/update/ "产品动态")

帮助
:   [在线客服](javascript:;)
:   [常见问题](/help/)

公司
:   [隐私政策](javascript:;)
:   [用户协议](javascript:;)

友情链接
:   [UU远程](https://uuyc.163.com/?source_channel=mumu_gw "UU远程")
:   [UU加速器](https://uu.163.com/ "UU加速器")
:   [网易千千壁纸](https://qianqian.163.com/ "网易千千壁纸")

![netease](https://mumu.res.netease.com/pc/gw/20250207163110/img/logo_netease_603336da.png)

[公司简介](http://gb.corp.163.com/gb/about/overview.html) - [客户服务](http://help.163.com/) - [网易游戏隐私政策及儿童个人信息保护规则](https://unisdk.update.netease.com/html/latest_v90.html) - [网易游戏](http://game.163.com/about/) - [联系我们](http://game.163.com/contact/) - [商务合作](http://game.163.com/contact/business.html) - [加入我们](http://hr.game.163.com/index.html)

*网易公司版权所有 ©1997-2024*
[网络游戏行业防沉迷自律公约](https://game.163.com/fcm/)

![Mu酱企业微信](https://mumu.res.netease.com/pc/gw/20250207163110/img/qywx_e86f1c64.png)

长按识别二维码添加Mu酱

您的电脑主板型号：

本页面为该主板的VT开启教程，如实际操作与教程不符，请自行搜索该主板对应设置教程或[咨询客服](https://wymumumnq.qiyukf.com/client?k=bc15d40a1abb4d66f1438d5054434b52&wp=1&robotShuntSwitch=1&robotId=5302691&shuntId=0)。