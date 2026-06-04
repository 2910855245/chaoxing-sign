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

MuMu模拟器如何连接 adb？

目录

MuMu模拟器如何连接 adb？
================

2023-03-01

欢迎各位开发者使用MuMu模拟器，网站内容更新中，请收藏~

一、MuMu模拟器端口查看
-------------

MuMu模拟器现已支持 adb 同时连接多个模拟器进行调试的操作，可以点击模拟器安卓设备右上角菜单-设备诊断，获取查看 MuMu模拟器的 ADB 调试端口。

### 1. MuMu模拟器12 版本

单开的 MUMU模拟器12 可通过模拟器右上角菜单-问题诊断，获取 ADB 调试端口；

![](https://nie.res.netease.com/r/pic/20230418/1cce4cd1-d6f7-4f5c-a999-6bd00d3b732f.png)

点击 MuMu多开器12，启动需要运行的模拟器，点击右上角的 ADB 图标，即可查看当前正在运行的模拟器 ADB 端口信息；

![](https://nie.res.netease.com/r/pic/20230418/9c03d181-379d-4576-bd42-98a29b5245f3.png)

### 2. MuMu模拟器 5.0 版本

点击模拟器右上角菜单-设置中心-ADB 端口，或点击安卓设备右上角菜单-设备诊断，获取查看 MuMu模拟器本体以及多开模拟器的 ADB 调试端口。

![](https://nie.res.netease.com/r/pic/20250701/3c38c235-616b-4c0b-9b70-e1db6d55d04e.png)

![](https://nie.res.netease.com/r/pic/20250812/b566f96c-adef-4c45-860e-eb559d26ec92.jpg)

### 3. MuMu模拟器 ADB 端口信息

规则如下：

#### 3.1 原 MuMu模拟器12（MuMu模拟器 5.0 版本通用）：

模拟器端口为动态端口，一般模拟器本体（即多开器内序号 0）的端口为 16384，多开的模拟器会在此基础上加 32，如：

0 号 16384，1 号 16416，2 号 16448，以此类推；

如果模拟器端口被占用了，会在原端口加 1，如：

16384 被占用，加 1，即 16385；

另，多开的端口不受占用后的端口影响，依旧在 16384 的基础上加 32。

#### 3.2 仅适用于 MuMu模拟器 5.0 版本：

默认新增 5555 为首个模拟器 ADB 端口；

多开模拟器端口号按 +2 递增（如 5555、5557、5559…）。

二、MuMu模拟器如何连接 adb？
------------------

MuMu模拟器 adb 连接方式具体如下。

### 1. MuMu模拟器12 连接方式

#### 1.1 打开命令提示符

打开 MuMu模拟器12 安装路径，参考下图，点击地址栏输入 CMD，再点击回车键呼出命令提示符窗口；

注：adb文件所在路径为：~\Netease\MuMuPlayer-12.0\shell

![](https://nie.res.netease.com/r/pic/20230413/25cea8e1-7701-40bb-bc97-303ea3839790.png)

#### 1.2 连接 adb 端口

在命令提示符窗口内输入 adb.exe connect 127.0.0.1:XXXXX，之后点击回车。

注：XXXXX 为模拟器端口号，请参考打开的模拟器问题诊断内展示端口号或 MuMu多开器12 内的 ADB 端口信息后再输入。

![](https://nie.res.netease.com/r/pic/20230413/7c523a9e-4e54-4138-b27a-d147498d4ab0.png)

#### 1.3 进入 adb shell 界面

再输入 adb.exe shell，点击回车即可。

![](https://nie.res.netease.com/r/pic/20230413/66db1fc7-7b55-4dff-87ae-4dd370543c4f.png)

### 2. MuMu模拟器 5.0 版本连接方式

#### 2.1 打开命令提示符

打开 MuMu模拟器安装路径，参考下图，点击地址栏输入 CMD，再点击回车键呼出命令提示符窗口；

![](https://nie.res.netease.com/r/pic/20250701/99f26a5a-f0b1-4e63-b91a-07500a55a9e3.png)

#### 2.2 连接 adb 端口

在命令提示符窗口内输入 adb devices，之后点击回车。

![](https://nie.res.netease.com/r/pic/20250701/f20a6bfa-b57b-4bcd-b63b-3835330fdcf7.png)

#### 2.3 进入 adb shell 界面

再输入 adb shell，点击回车即可。

![](https://nie.res.netease.com/r/pic/20250701/0c198a9d-babe-4b7c-b407-022bf4a178fd.png)

三、MuMu模拟器如何连接多开模拟器的 adb？
------------------------

### 1. MuMu模拟器12 连接方式

在 MuMu模拟器多开器12 内查看对应运行模拟器的端口号之后，参考以下步骤操作：

#### 1.1 打开命令提示符

打开 MuMu模拟器12 安装路径，参考下图，点击地址栏输入 CMD，再点击回车键呼出命令提示符窗口；

![](https://nie.res.netease.com/r/pic/20230413/25cea8e1-7701-40bb-bc97-303ea3839790.png)

#### 1.2 连接 adb 端口

在命令提示符窗口内输入 adb.exe connect 127.0.0.1:XXXXX，之后点击回车。

注：XXXXX 为模拟器端口号，请参考打开的模拟器问题诊断内展示端口号或 MuMu多开器12 内的 ADB 端口信息后再输入。

![](https://nie.res.netease.com/r/pic/20230418/58d9fddf-3b0e-449d-8ff7-dcbebe972315.png)

若需要进入到指定的 adb 内进行 shell 的操作，请参考以下步骤。

##### 1.2.1 查看已连接设备

输入命令 adb devices，查看已连接的设备列表；

##### 1.2.2 查看需要连接的设备号

找到要进入的设备的设备号；

![](https://nie.res.netease.com/r/pic/20230418/8493c450-5113-445a-9f57-b2b7978a3330.png)

##### 1.2.3 输入对应设备号进行 adb 连接

在命令行中输入“adb -s 设备号 shell”命令，例如adb -s 127.0.0.1:16384 shell，按下回车键，即可进入指定的adb进行shell操作。

![](https://nie.res.netease.com/r/pic/20230418/81e8db83-0e97-452b-9ea9-3f39ba11d8cc.png)

### 2. MuMu模拟器 5.0 版本连接方式

#### 2.1 打开命令提示符

打开 MuMu模拟器安装路径，参考下图，点击地址栏输入 CMD，再点击回车键呼出命令提示符窗口；

![](https://nie.res.netease.com/r/pic/20250701/2d9e322f-a8c8-4d4a-a703-20d6333d9e05.png)

#### 2.2 连接指定 adb 端口

在命令提示符窗口内输入 adb devices，之后点击回车。

![](https://nie.res.netease.com/r/pic/20250701/a4bbe7ef-4520-4e61-9194-a3c41b734c55.png)

在命令行中输入“adb -s 设备号 shell”命令，例如 adb -s emulator-5562 shell，按下回车键，即可进入指定的 adb 进行 shell 操作。

![](https://nie.res.netease.com/r/pic/20250701/eed7b96e-376b-4866-8c3e-e814dbf7e217.png)

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