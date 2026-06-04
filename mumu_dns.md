[![MuMu模拟器](https://mumu.res.netease.com/pc/gw/20250207163110/img/logo_dea448be.png)](/)

* [下载](/download/)
* [游戏](/games/)
* [掌上MuMu](/product/pocketmumu/)
* [帮助](/help/)
* [开放平台](https://open.mumu.163.com/)

下载

常见问题
====

查询模拟器各类问题的解决方案

请输入关键词查找相关问题

[MuMu模拟器](/ "MuMu模拟器")

[常见问题](/help/ "常见问题")

[MuMu模拟器（Windows）](/help/win/ "MuMu模拟器（Windows）")

MuMu模拟器网络桥接教程与常见问题

目录

MuMu模拟器网络桥接教程与常见问题
==================

2024-07-03

欢迎各位开发者使用 MuMu模拟器，网站内容更新中，请收藏~

MuMu模拟器网络桥接功能现已全新上线！部分用户在使用模拟器过程中，可能需要实现局域网内 PC、手机、模拟器之间数据传输，或者有抓包需求等，这类均可通过设置网络桥接功能解决。如何使用 MuMu模拟器进行网络桥接呢？Mu酱为大家带来如下教程，让大家快速上手~

注：该功能需要MuMu模拟器在 3.9.2 及以上版本才可使用[>>点击获取最新版本](https://mumu.163.com/download/)

一、网络桥接设置步骤
----------

### 1. 开启网络桥接模式

启动 MuMu模拟器后，点击 MuMu安卓设备右上角菜单-设备设置-网络，开启“网络桥接模式”；

注：若未安装驱动，请点击安装驱动，完成驱动安装后才可勾选该功能。

![](https://nie.res.netease.com/r/pic/20250730/f3ea55a7-1773-4f88-b1fe-3235c3539a84.png)

### 2. 调整桥接设置

根据当前所需，调整为 DHCP 或静态 IP，保存设置后重启模拟器即可。

注：

1. DHCP 为自动获取模式，静态 IP 需要输入自定义 IP 地址、网关、子网掩码、DNS 等信息进行手动调整；
2. 网卡选择请根据当前电脑连接的网络进行选择，如当前设备使用的网络通过 Intel(R) Ethernet Connection (2) I219-V 网卡进行连接的，在网卡选项中将网卡调整到同样的设备选项即可。

![](https://nie.res.netease.com/r/pic/20250730/4a34cef0-8a10-4f1a-80ae-8994d1d42e42.png)

二、常见问题
------

### 1. 如何卸载 MuMu桥接驱动？

可通过如下两种方式进行卸载：

#### 1.1 卸载 MuMu模拟器

卸载 MuMu模拟器会自动卸载 MuMu 的桥接驱动；

#### 1.2 通过命令行卸载

通过以下命令行单独卸载 MuMu 桥接驱动：

打开 Windows 搜索栏，输入 cmd 或 命令提示符，之后以管理员身份运行“命令提示符”，在弹出的窗口内输入以下命令进行卸载；

cd /d C:\Program Files\MuMuVMMVbox\Hypervisor

loadlwf.cmd -u

![](https://nie.res.netease.com/r/pic/20240703/b8082ee6-6aae-419b-a766-7b5fbb57c5a7.png)

提示“uninstalled successfully”表示卸载成功；

![](https://nie.res.netease.com/r/pic/20240703/981f967b-f05e-4773-b76f-b59a05c215ad.png)

也可通过命令查询是否存在MuMu桥接驱动，当无输出信息即表示卸载成功：

driverquery /V | findstr "MuMuVMMNetLwf"

![](https://nie.res.netease.com/r/pic/20240703/dbecc274-9da3-4191-99bc-a6eae49564fc.png)

此时重新打开模拟器设置中心-网络设置，显示需要安装驱动。

![](https://nie.res.netease.com/r/pic/20240703/b373eeb3-ae82-46c0-a77c-cb5a9d5fdcac.png)

### 2. Windows8系统安装桥接驱动提示失败怎么办？

打开Windows搜索栏，输入cmd或 命令提示符，之后以管理员身份运行“命令提示符”，在弹出的窗口内输入以下命令：

Bcdedit.exe -set TESTSIGNING ON

回车后提示“操作成功完成”，此时重启电脑，之后即可在模拟器设置中心-网络设置内进行桥接驱动安装，且安装时请点击“始终安装此驱动程序软件”，否则会安装失败。

![](https://nie.res.netease.com/r/pic/20240703/3a470076-5a88-4027-a483-b24d60d0168b.png)

![](https://nie.res.netease.com/r/pic/20240703/af7a4430-6f3c-471e-b433-47f056f992bc.png)

### 3. 设置网络桥接后，ADB无法连接模拟器？多开模拟器怎么连接？[>>点击跳转MuMu模拟器桥接模式连接adb教程](https://mumu.163.com/help/20240703/35047_1164744.html)

如有更多问题，欢迎加入MuMu模拟器开发者官方微信交流群，与诸多开发者和策划共同交流~

（该群只处理开发者问题，模拟器使用问题请咨询[在线客服](https://wymumumnq.qiyukf.com/client?k=bc15d40a1abb4d66f1438d5054434b52&wp=1&robotShuntSwitch=0&shuntId=0)）

![](https://nie.res.netease.com/r/pic/20240814/c6441833-525f-454e-acad-6d72b4738c42.png)

文章已到底

关键词:

[Clear](javascript:;)

![img](https://mumu.res.netease.com/pc/gw/20250207163110/img/nodata_a12d08e9.png)

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