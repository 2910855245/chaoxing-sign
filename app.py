#!/usr/bin/env python3
"""学习通签到工具 -- 全类型支持"""
import sys
import os
import json
import time
import threading

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from rich.console import Console
from rich.table import Table
from rich.prompt import Prompt, Confirm
from rich.rule import Rule
from rich.style import Style
from rich import box

console = Console()

S_OK = Style(bold=True, color="green")
S_ERR = Style(bold=True, color="red")
S_WARN = Style(bold=True, color="yellow")
S_DIM = Style(dim=True)


def banner():
    console.print(r"""
   _____ _              _                    _   ___
  / ____| |            | |                  | | / (_)
 | |    | |__   __ _  | | _____  _   _  ___| |/ / _ _ __   __ _
 | |    | '_ \ / _` | | |/ / _ \| | | |/ _ \   \| | '_ \ / _` |
 | |____| | | | (_| | |   < (_) | |_| |  __/ |\  \ | | | | (_| |
  \_____|_| |_|\__,_| |_|\_\___/ \__, |\___|_| \_\_|_| |_|\__, |
                                  __/ |                     __/ |
                                 |___/                     |___/
""", style="bold cyan")
    console.print("  学习通签到工具 - Rich Terminal\n", style=S_DIM)


def _load_account():
    path = os.path.join(os.path.dirname(__file__), ".account.json")
    if os.path.isfile(path):
        try:
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            pass
    return None


def _save_account(u, p):
    with open(os.path.join(os.path.dirname(__file__), ".account.json"), "w") as f:
        json.dump({"username": u, "password": p}, f)


def login():
    from chaoxing.session import ChaoxingSession

    saved = _load_account()
    if saved:
        console.print(f"  上次账号: [bold]{saved['username']}[/bold]", style=S_DIM)
        if Confirm.ask("  使用该账号?", default=True):
            u, p = saved["username"], saved["password"]
        else:
            u = Prompt.ask("  学号")
            p = Prompt.ask("  密码", password=True)
    else:
        u = Prompt.ask("  学号")
        p = Prompt.ask("  密码", password=True)

    with console.status("[bold cyan]登录中...", spinner="dots"):
        s = ChaoxingSession()
        ok = s.login(u, p)

    if not ok:
        console.print("  [X] 登录失败", style=S_ERR)
        return None

    _save_account(u, p)
    info = s.get_user_info()
    console.print(f"  [OK] {info['name']}  {info['school_name']}  UID:{s.uid}", style=S_OK)
    return s


def scan(session):
    from chaoxing.crawler import get_course_list

    with console.status("[bold cyan]扫描课程...", spinner="dots"):
        courses = get_course_list(session)

    if not courses:
        console.print("  无课程", style=S_DIM)
        return []

    table = Table(
        title="课程列表",
        box=box.ROUNDED,
        show_lines=True,
        title_style="bold cyan",
        header_style="bold cyan",
    )
    table.add_column("#", width=4, style="dim")
    table.add_column("课程", min_width=20)
    table.add_column("课程ID", style="dim")

    for i, c in enumerate(courses, 1):
        table.add_row(str(i), c["name"], c["courseId"])

    console.print()
    console.print(table)
    console.print(f"\n  共 {len(courses)} 门课程", style=S_DIM)
    return courses


def check_sign(session, courses):
    from chaoxing.signin import get_active_list

    table = Table(
        title="签到活动",
        box=box.ROUNDED,
        show_lines=True,
        title_style="bold cyan",
        header_style="bold cyan",
    )
    table.add_column("课程", min_width=16)
    table.add_column("类型")
    table.add_column("状态")
    table.add_column("活动ID", style="dim")

    total = 0
    pending = 0

    with console.status("[bold cyan]检查签到活动...", spinner="dots"):
        for c in courses:
            acts = get_active_list(session, c["courseId"], c["classId"])
            for a in acts:
                total += 1
                st = a["statusText"]
                if a["status"] == 0:
                    st = f"[yellow]{st}[/yellow]"
                    pending += 1
                elif a["status"] == 1:
                    st = f"[green]{st}[/green]"
                else:
                    st = f"[dim]{st}[/dim]"
                table.add_row(c["name"][:16], a["typeName"], st, str(a["activeId"]))

    console.print()
    if total == 0:
        console.print("  无签到活动", style=S_DIM)
        return
    console.print(table)
    console.print(f"\n  共 {total} 个活动, {pending} 个待签", style=S_DIM)


def do_sign(session, courses):
    """一键签到 -- 支持暴力破解中断"""
    from chaoxing.signin import (
        get_active_list, sign_activity, sign_with_code,
        BRUTE_FORCE_TYPES, SIGN_TYPES
    )

    # 收集所有待签到活动
    all_activities = []
    for c in courses:
        acts = get_active_list(session, c["courseId"], c["classId"])
        for a in acts:
            if a["status"] == 0 and a.get("userStatus") != 1:
                a["course_name"] = c["name"]
                all_activities.append(a)

    if not all_activities:
        console.print("  无待签到活动", style=S_DIM)
        return

    # 分类
    normal_acts = [a for a in all_activities if a["otherId"] not in BRUTE_FORCE_TYPES]
    brute_acts = [a for a in all_activities if a["otherId"] in BRUTE_FORCE_TYPES]

    # 先签普通/位置/二维码
    if normal_acts:
        console.print(f"\n  签到 {len(normal_acts)} 个普通活动...", style=S_WARN)
        for act in normal_acts:
            result = sign_activity(session, act)
            _print_result(act, result)

    # 需要暴力破解的活动
    if not brute_acts:
        console.print("\n  全部完成", style=S_OK)
        return

    # 逐个处理暴力破解活动
    for act in brute_acts:
        _handle_brute_activity(session, act)


def _handle_brute_activity(session, act):
    """处理需要暴力破解的签到活动"""
    from chaoxing.signin import (
        sign_activity, sign_with_code,
        brute_force_gesture_async, brute_force_code_async,
        get_active_detail
    )

    type_name = act["typeName"]
    active_id = str(act["activeId"])

    console.print(f"\n  {'='*40}", style=S_WARN)
    console.print(f"  [!] {act['course_name']} - {type_name}", style=S_WARN)
    console.print(f"  {'='*40}", style=S_WARN)

    if act["otherId"] == 3:
        console.print("  这是手势签到, 老师在手机上画了手势图案", style=S_WARN)
        console.print("  建议: 立刻问室友/同学手势路径", style=S_WARN)
        detail = get_active_detail(session, active_id)
        nc = detail.get("numberCount", 5)
        console.print(f"  手势点数: {nc}", style=S_DIM)
    elif act["otherId"] == 5:
        console.print("  这是签到码签到, 老师在屏幕上显示了数字码", style=S_WARN)
        console.print("  建议: 立刻问室友/同学签到码", style=S_WARN)

    console.print("\n  [自动] 后台暴力破解已启动...", style=S_DIM)
    console.print("  [输入] 知道答案后直接输入, 会中断暴力破解立即签到", style=S_DIM)
    console.print("  [跳过] 直接回车跳过此活动\n", style=S_DIM)

    # 启动暴力破解
    def on_progress(msg):
        # 用 \r 覆盖同一行
        console.print(f"\r  [破解] {msg}", end="", style=S_DIM)

    result = sign_activity(session, act, on_progress=on_progress)
    brute_state = result.get("_brute_state")

    if brute_state is None:
        # 没有暴力破解（可能是已有 code）
        _print_result(act, result)
        return

    # 等待用户输入或暴力破解完成
    user_code = None
    sign_done = False

    def wait_input():
        nonlocal user_code
        try:
            user_code = input()
        except EOFError:
            pass

    input_thread = threading.Thread(target=wait_input, daemon=True)
    input_thread.start()

    # 轮询等待
    while input_thread.is_alive():
        if brute_state.is_stopped() and brute_state.found:
            # 暴力破解成功
            console.print(f"\n  [破解成功] code={brute_state.found}", style=S_OK)
            final = sign_with_code(session, act, brute_state.found)
            _print_result(act, final)
            sign_done = True
            break

        if user_code is not None and user_code.strip():
            # 用户输入了 code
            brute_state.stop()  # 中断暴力破解
            code = user_code.strip()
            console.print(f"\n  [手动输入] code={code}", style=S_OK)
            final = sign_with_code(session, act, code)
            _print_result(act, final)
            sign_done = True
            break

        time.sleep(0.5)

    if not sign_done:
        if brute_state.found:
            final = sign_with_code(session, act, brute_state.found)
            _print_result(act, final)
        else:
            console.print(f"\n  [失败] {type_name} 暴力破解未找到", style=S_ERR)


def _print_result(act, result):
    """打印签到结果"""
    ok = "[OK]" if result.get("success") else "[X]"
    style = S_OK if result.get("success") else S_ERR
    code = result.get("signCode", "")
    msg = result.get("message", "")
    if code:
        msg = f"code={code}"
    console.print(f"  {ok} {act.get('course_name','')[:16]} {act.get('typeName','')} - {msg}", style=style)


def manage_class(session):
    """班级管理"""
    from chaoxing.clazz import (
        get_class_list, get_class_invite_code,
        join_class_by_code, create_class
    )

    while True:
        console.print()
        console.print(Rule("班级管理", style="cyan"))
        console.print("""
  [1] 查看班级列表
  [2] 获取班级邀请码
  [3] 加入班级 (输入邀请码)
  [4] 创建新班级
  [0] 返回
""")
        choice = Prompt.ask("  选择", default="0")

        if choice == "0":
            break
        elif choice == "1":
            _show_class_list(session)
        elif choice == "2":
            _show_invite_code(session)
        elif choice == "3":
            _join_class(session)
        elif choice == "4":
            _create_class(session)


def _show_class_list(session):
    """显示班级列表"""
    from chaoxing.clazz import get_class_list

    courses = scan(session)
    if not courses:
        return

    # 选择课程
    idx = Prompt.ask("  选择课程编号", default="1")
    try:
        idx = int(idx) - 1
        if idx < 0 or idx >= len(courses):
            console.print("  无效编号", style=S_ERR)
            return
    except ValueError:
        console.print("  无效编号", style=S_ERR)
        return

    course = courses[idx]
    with console.status("[bold cyan]获取班级列表...", spinner="dots"):
        classes = get_class_list(session, course['courseId'])

    if not classes:
        console.print("  无班级", style=S_DIM)
        return

    table = Table(
        title=f"{course['name']} - 班级列表",
        box=box.ROUNDED,
        show_lines=True,
        title_style="bold cyan",
        header_style="bold cyan",
    )
    table.add_column("#", width=4, style="dim")
    table.add_column("班级名称", min_width=20)
    table.add_column("学生数", width=8)
    table.add_column("班级ID", style="dim")

    for i, c in enumerate(classes, 1):
        table.add_row(
            str(i),
            c.get('name', '未知'),
            str(c.get('studentcount', 0)),
            str(c.get('id', ''))
        )

    console.print()
    console.print(table)


def _show_invite_code(session):
    """显示班级邀请码"""
    from chaoxing.clazz import get_class_list, get_class_invite_code

    courses = scan(session)
    if not courses:
        return

    # 选择课程
    idx = Prompt.ask("  选择课程编号", default="1")
    try:
        idx = int(idx) - 1
        if idx < 0 or idx >= len(courses):
            console.print("  无效编号", style=S_ERR)
            return
    except ValueError:
        console.print("  无效编号", style=S_ERR)
        return

    course = courses[idx]
    with console.status("[bold cyan]获取班级列表...", spinner="dots"):
        classes = get_class_list(session, course['courseId'])

    if not classes:
        console.print("  无班级", style=S_DIM)
        return

    # 显示班级列表
    for i, c in enumerate(classes, 1):
        console.print(f"  [{i}] {c.get('name', '未知')} (ID: {c.get('id', '')})")

    # 选择班级
    idx = Prompt.ask("  选择班级编号", default="1")
    try:
        idx = int(idx) - 1
        if idx < 0 or idx >= len(classes):
            console.print("  无效编号", style=S_ERR)
            return
    except ValueError:
        console.print("  无效编号", style=S_ERR)
        return

    clazz = classes[idx]
    with console.status("[bold cyan]获取邀请码...", spinner="dots"):
        invite_data = get_class_invite_code(
            session, course['courseId'], str(clazz['id'])
        )

    if not invite_data:
        console.print("  获取邀请码失败", style=S_ERR)
        return

    console.print()
    console.print(f"  班级: {clazz.get('name', '未知')}", style=S_OK)
    console.print(f"  邀请码: {invite_data.get('invitecode', '未知')}", style=S_OK)
    console.print(f"  有效期: {invite_data.get('validtime', '未知')}", style=S_DIM)


def _join_class(session):
    """加入班级"""
    from chaoxing.clazz import join_class_by_code

    invite_code = Prompt.ask("  输入邀请码")
    if not invite_code.strip():
        console.print("  邀请码不能为空", style=S_ERR)
        return

    with console.status("[bold cyan]加入班级...", spinner="dots"):
        result = join_class_by_code(session, invite_code.strip())

    if result.get('success'):
        console.print(f"  [OK] {result.get('message', '加入成功')}", style=S_OK)
    else:
        console.print(f"  [X] {result.get('message', '加入失败')}", style=S_ERR)
        console.print("  建议: 在学习通APP中手动输入邀请码加入", style=S_DIM)


def _create_class(session):
    """创建新班级"""
    from chaoxing.clazz import create_class

    courses = scan(session)
    if not courses:
        return

    # 选择课程
    idx = Prompt.ask("  选择课程编号", default="1")
    try:
        idx = int(idx) - 1
        if idx < 0 or idx >= len(courses):
            console.print("  无效编号", style=S_ERR)
            return
    except ValueError:
        console.print("  无效编号", style=S_ERR)
        return

    course = courses[idx]
    class_name = Prompt.ask("  输入班级名称")
    if not class_name.strip():
        console.print("  班级名称不能为空", style=S_ERR)
        return

    with console.status("[bold cyan]创建班级...", spinner="dots"):
        result = create_class(session, course['courseId'], class_name.strip())

    if result.get('success'):
        console.print(f"  [OK] {result.get('message', '创建成功')}", style=S_OK)
    else:
        console.print(f"  [X] {result.get('message', '创建失败')}", style=S_ERR)


def main():
    banner()
    session = login()
    if not session:
        if Confirm.ask("  重试?", default=True):
            session = login()
        if not session:
            return

    courses = []
    while True:
        console.print()
        console.print(Rule("功能", style="cyan"))
        console.print("""
  [1] 扫描课程
  [2] 检查签到活动
  [3] 一键签到 (自动暴力破解)
  [4] 班级管理
  [0] 退出
""")
        choice = Prompt.ask("  选择", default="1")
        if choice == "1":
            courses = scan(session)
        elif choice == "2":
            if not courses:
                courses = scan(session)
            if courses:
                check_sign(session, courses)
        elif choice == "3":
            if not courses:
                courses = scan(session)
            if courses:
                do_sign(session, courses)
        elif choice == "4":
            manage_class(session)
        elif choice == "0":
            console.print("\n  再见\n", style=S_DIM)
            break


if __name__ == "__main__":
    main()
